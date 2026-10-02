package com.koala.reactingreactions.content.electrolysis;

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

import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;

/**
 * On an electrode or a vat terminal: it only remembers its vat's controller (pushed by the controller) so the goggles can show
 * the electrode types and the voltage when you look at an electrode, instead of on every wall of the vat.
 */
public class ElectrodeInfoBlockEntity extends SmartBlockEntity implements IHaveGoggleInformation {
    @Nullable
    private BlockPos controllerPos;

    public ElectrodeInfoBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if (!Objects.equals(pos, controllerPos)) {
            controllerPos = pos;
            setChanged();
            sendData();
        }
    }

    @Nullable
    public ElectrolysisVatControllerBlockEntity controller() {
        return level != null && controllerPos != null && level.getBlockEntity(controllerPos) instanceof ElectrolysisVatControllerBlockEntity vat ? vat : null;
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
        ElectrolysisVatControllerBlockEntity vat = controller();
        return vat != null && vat.addElectrodeTooltip(tooltip, isPlayerSneaking);
    }
}
