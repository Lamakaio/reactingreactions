package com.koala.reactingreactions.content.compat;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.drill.DerrickBlock;
import com.koala.reactingreactions.content.drill.DerrickBlockEntity;
import com.koala.reactingreactions.content.drill.DerrickControllerBlock;
import com.koala.reactingreactions.content.drill.DerrickControllerBlockEntity;
import com.koala.reactingreactions.content.drill.RichOreVeinBlock;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.koala.reactingreactions.content.multiblock.ProcessingMachineBlockEntity;
import com.koala.reactingreactions.content.toxic.AtmosphericScrubberBlock;
import com.koala.reactingreactions.content.toxic.AtmosphericScrubberBlockEntity;
import com.koala.reactingreactions.content.toxic.LeakPoolEntity;
import com.koala.reactingreactions.content.toxic.ToxicFluid;
import com.koala.reactingreactions.content.toxic.Toxicity;
import com.koala.reactingreactions.content.toxic.ToxicityTooltips;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import snownee.jade.api.Accessor;

import snownee.jade.api.BlockAccessor;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.fluid.JadeFluidObject;
import snownee.jade.api.view.ClientViewGroup;
import snownee.jade.api.view.FluidView;
import snownee.jade.api.view.IClientExtensionProvider;
import snownee.jade.api.view.IServerExtensionProvider;
import snownee.jade.api.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/** Jade overlay lines for the Derrick, the Scrubber, Rich Veins and leak pools, and machine tanks. Loaded only when Jade is installed. */
@WailaPlugin
public class CRRJadePlugin implements IWailaPlugin {
    private static final FilledTanks FILLED_TANKS = new FilledTanks();

    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerFluidStorage(FILLED_TANKS, ProcessingMachineBlockEntity.class);
        registration.registerFluidStorage(FILLED_TANKS, MultiblockWallBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerFluidStorageClient(FILLED_TANKS);
        registration.registerBlockComponent(new RichVeinProvider(), RichOreVeinBlock.class);
        registration.registerBlockComponent(new DerrickProvider(), DerrickControllerBlock.class);
        registration.registerBlockComponent(new DerrickProvider(), DerrickBlock.class);
        registration.registerBlockComponent(new ScrubberProvider(), AtmosphericScrubberBlock.class);
        registration.registerEntityComponent(new PoolProvider(), LeakPoolEntity.class);
    }

    private static ResourceLocation uid(String name) {
        return ReactingReactions.asResource(name);
    }

    private static class RichVeinProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            int level = accessor.getBlockState().getValue(RichOreVeinBlock.RICHNESS);
            tooltip.add(Component.literal("Richness: " + level + " / " + RichOreVeinBlock.MAX_LEVEL));
        }

        @Override
        public ResourceLocation getUid() {
            return uid("rich_vein");
        }
    }

    private static class DerrickProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            BlockEntity be = accessor.getBlockEntity();
            DerrickControllerBlockEntity controller = be instanceof DerrickControllerBlockEntity own ? own
                    : be instanceof DerrickBlockEntity frame ? frame.getController() : null;
            if (controller == null) {
                return;
            }
            tooltip.add(Component.literal(controller.isFormed() ? (controller.isRunning() ? "Working" : "Idle") : "Not formed"));
            if (controller.isFormed()) {
                tooltip.add(Component.literal(controller.headDescription() + ", vein richness " + controller.getRichness()));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return uid("derrick");
        }
    }

    private static class ScrubberProvider implements IBlockComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            if (accessor.getBlockEntity() instanceof AtmosphericScrubberBlockEntity scrubber) {
                tooltip.add(Component.literal(scrubber.isActive() ? "Working" : "Idle"));
                tooltip.add(Component.literal(scrubber.isInRoom() ? "In a room" : "In the open"));
            }
        }

        @Override
        public ResourceLocation getUid() {
            return uid("scrubber");
        }
    }

    private static class PoolProvider implements IEntityComponentProvider {
        @Override
        public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
            if (accessor.getEntity() instanceof LeakPoolEntity pool) {
                FluidStack fluid = new FluidStack(pool.getFluid(), pool.getAmount());
                tooltip.add(Component.literal(fluid.getHoverName().getString() + ", " + pool.getAmount() + " mB"));
                ToxicFluid data = Toxicity.of(pool.getFluid());
                if (data.toxicity() > 0) {
                    tooltip.add(ToxicityTooltips.line(data.toxicity(), data));
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return uid("leak_pool");
        }
    }

    /** Only the tanks of a machine that hold something: its empty ones would only clutter the overlay. */
    private static class FilledTanks implements IServerExtensionProvider<CompoundTag>, IClientExtensionProvider<CompoundTag, FluidView> {
        @Override
        public List<ViewGroup<CompoundTag>> getGroups(Accessor<?> accessor) {
            if (!(accessor instanceof BlockAccessor block)) {
                return null;
            }
            IFluidHandler handler = block.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, block.getPosition(), null);
            if (handler == null) {
                return null;
            }
            List<CompoundTag> filled = new ArrayList<>();
            for (int i = 0; i < handler.getTanks(); i++) {
                FluidStack fluid = handler.getFluidInTank(i);
                if (!fluid.isEmpty()) {
                    filled.add(FluidView.writeDefault(JadeFluidObject.of(fluid.getFluid(), fluid.getAmount(), fluid.getComponentsPatch()),
                            handler.getTankCapacity(i)));
                }
            }
            return List.of(new ViewGroup<>(filled));
        }

        @Override
        public List<ClientViewGroup<FluidView>> getClientGroups(Accessor<?> accessor, List<ViewGroup<CompoundTag>> groups) {
            return ClientViewGroup.map(groups, FluidView::readDefault, null);
        }

        @Override
        public ResourceLocation getUid() {
            return uid("filled_tanks");
        }
    }
}
