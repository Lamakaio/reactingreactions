package com.koala.reactingreactions.content.multiblock;

import com.simibubi.create.api.equipment.goggles.IHaveGoggleInformation;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import org.jetbrains.annotations.Nullable;

import java.util.List;

/** A shell block that forwards pipes, hoppers and goggles to its machine's controller. */
public class MultiblockWallBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    @Nullable
    private BlockPos controllerPos;

    public MultiblockWallBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<MultiblockWallBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> {
            MultiblockControllerBlockEntity<?> controller = be.getController();
            return controller == null ? null : controller.getFluidCapabilityAt(be.getBlockPos());
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, ctx) -> {
            MultiblockControllerBlockEntity<?> controller = be.getController();
            return controller == null ? null : controller.getItemCapability();
        });
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if (pos == null ? controllerPos != null : !pos.equals(controllerPos)) {
            controllerPos = pos;
            setChanged();
            sendData();
            notifyCapabilityChanged();
        }
    }

    public void notifyCapabilityChanged() {
        if (level == null) {
            return;
        }
        level.invalidateCapabilities(worldPosition);
        // Create's pipes keep a capability cache that stays invalid after one invalidation; a neighbour update rebuilds it.
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Nullable
    public MultiblockControllerBlockEntity<?> getController() {
        if (level == null || controllerPos == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof MultiblockControllerBlockEntity<?> controller ? controller : null;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide || !(getBlockState().getBlock() instanceof MultiblockPartBlock<?> part) || part.spec() == null) {
            return;
        }
        if (controllerPos == null) {
            MultiblockControllerBlockEntity.Spec<?> spec = part.spec();
            BlockPos found = HollowBoxScanner.findController(level, worldPosition, spec.shell(), spec.controller());
            if (found != null) {
                setControllerPos(found);
            }
            return;
        }
        MultiblockControllerBlockEntity<?> controller = getController();
        if (controller == null) {
            setControllerPos(null);
            return;
        }
        // Kept while the controller is merely unformed, so a wall doesn't flicker in and out during rebuilds.
        HollowBoxScanner.Result structure = controller.getStructure();
        if (structure != null && !HollowBoxScanner.isShellCell(structure, worldPosition)) {
            setControllerPos(null);
        }
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        controllerPos = compound.contains("Controller") ? NbtUtils.readBlockPos(compound, "Controller").orElse(null) : null;
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (controllerPos != null) {
            compound.put("Controller", NbtUtils.writeBlockPos(controllerPos));
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        MultiblockControllerBlockEntity<?> controller = getController();
        return controller != null && controller.addToGoggleTooltipAt(tooltip, isPlayerSneaking, worldPosition);
    }
}
