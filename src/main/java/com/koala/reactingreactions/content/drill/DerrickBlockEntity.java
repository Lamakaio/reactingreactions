package com.koala.reactingreactions.content.drill;

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
import java.util.Objects;

/**
 * Block entity of a {@link DerrickBlock}: only remembers its controller (pushed by the controller each time it validates the
 * structure) so pipes on any Derrick block reach the controller's tanks. Trusses have none, so they are not part of the I/O.
 */
public class DerrickBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    @Nullable
    private BlockPos controllerPos;

    public DerrickBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public static void registerCapabilities(RegisterCapabilitiesEvent event, BlockEntityType<DerrickBlockEntity> type) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, type, (be, ctx) -> {
            DerrickControllerBlockEntity controller = be.getController();
            return controller == null ? null : controller.getFluidCapability();
        });
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, type, (be, ctx) -> {
            DerrickControllerBlockEntity controller = be.getController();
            return controller == null ? null : controller.getItemCapability();
        });
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if (Objects.equals(pos, controllerPos)) {
            return;
        }
        controllerPos = pos;
        setChanged();
        notifyCapabilityChanged();
    }

    /** {@code invalidateCapabilities} alone is not enough: neighbours must also be told, or Create's cached pipe connections stay dead. */
    private void notifyCapabilityChanged() {
        if (level == null) {
            return;
        }
        level.invalidateCapabilities(worldPosition);
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
    }

    @Nullable
    public DerrickControllerBlockEntity getController() {
        if (level == null || controllerPos == null) {
            return null;
        }
        return level.getBlockEntity(controllerPos) instanceof DerrickControllerBlockEntity controller ? controller : null;
    }

    @Override
    public void lazyTick() {
        super.lazyTick();
        if (level == null || level.isClientSide || controllerPos == null) {
            return;
        }
        DerrickControllerBlockEntity controller = getController();
        // Not cleared while the controller merely has not (re)validated yet, e.g. right after a reload.
        if (controller == null || (controller.isFormed() && !controller.isIoBlock(worldPosition))) {
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
        DerrickControllerBlockEntity controller = getController();
        return controller != null && controller.addFrameTooltip(tooltip);
    }
}
