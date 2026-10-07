package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;
import com.koala.reactingreactions.content.reaction.SmallReactionChamberBlock;
import com.simibubi.create.content.fluids.FluidTransportBehaviour;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * A Gasket on anything that leaks (tanks, basins, pipes, pumps, valves, other mods' machines built on Create's fluid behaviours):
 * it stops leaking. For a Create Fluid Tank, its controller holds the flag for the whole tank. Used on the block it goes in; sneaking with an empty hand takes it
 * back out, and breaking the block drops it.
 */
public final class TankSealing {
    /** Mixed into every Create smart block entity (SmartBlockEntityMixin). */
    public interface Sealable {
        boolean crr$isSealed();

        void crr$setSealed(boolean sealed);
    }

    private TankSealing() {
    }

    /**
     * The block entity holding the flag for the block at {@code pos}, or null if it cannot take a Gasket: anything that can leak
     * (a tank or a fluid pipe, pump or valve), except this mod's multiblocks, which take one as an upgrade.
     */
    @Nullable
    private static SmartBlockEntity holderAt(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        // Either half of a Small Reaction Chamber: its lower half holds the flag.
        BlockEntity be = level.getBlockEntity(state.getBlock() instanceof SmallReactionChamberBlock ? SmallReactionChamberBlock.lowerPos(state, pos) : pos);
        if (be instanceof FluidTankBlockEntity tank) {
            return tank.getControllerBE();
        }
        if (!(be instanceof SmartBlockEntity smart) || be instanceof MultiblockControllerBlockEntity<?>) {
            return null;
        }
        for (BlockEntityBehaviour behaviour : smart.getAllBehaviours()) {
            if (behaviour instanceof FluidTransportBehaviour || behaviour instanceof SmartFluidTankBehaviour) {
                return smart;
            }
        }
        return null;
    }

    public static boolean isSealed(@Nullable BlockEntity be) {
        BlockEntity holder = be instanceof FluidTankBlockEntity tank ? tank.getControllerBE() : be;
        return holder instanceof Sealable sealable && sealable.crr$isSealed();
    }

    public static void appendTooltip(List<Component> tooltip, BlockEntity be) {
        if (isSealed(be)) {
            tooltip.add(Component.literal("    Sealed with a Gasket: no leaks").withStyle(ChatFormatting.GRAY));
        }
    }

    /** Puts a gasket on the block at {@code pos}; PASS when it cannot take one, FAIL when it is already sealed. */
    public static InteractionResult seal(Level level, BlockPos pos, Player player, ItemStack gasket) {
        SmartBlockEntity holder = holderAt(level, pos);
        if (!(holder instanceof Sealable sealable)) {
            return InteractionResult.PASS;
        }
        if (sealable.crr$isSealed()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            sealable.crr$setSealed(true);
            holder.setChanged();
            holder.sendData();
            if (!player.getAbilities().instabuild) {
                gasket.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
        }
        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return;
        }
        Level level = event.getLevel();
        SmartBlockEntity holder = holderAt(level, event.getPos());
        if (!(holder instanceof Sealable sealable) || !sealable.crr$isSealed()) {
            return;
        }
        if (!level.isClientSide) {
            sealable.crr$setSealed(false);
            holder.setChanged();
            holder.sendData();
            player.getInventory().placeItemBackInInventory(new ItemStack(CRRItems.GASKET.get()));
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
