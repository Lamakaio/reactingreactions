package com.koala.reactingreactions.content.steel;

import com.koala.reactingreactions.content.reaction.ReactionChamberControllerBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;

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

/** A Create shaft in a steel casing. When it is the stirring shaft of a Reaction Chamber, its goggles also show the stirring speed. */
public class SteelEncasedShaftBlockEntity extends KineticBlockEntity {
    @Nullable
    private BlockPos chamberPos;

    public SteelEncasedShaftBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public void setControllerPos(@Nullable BlockPos pos) {
        if (!Objects.equals(pos, chamberPos)) {
            chamberPos = pos;
            setChanged();
            sendData();
        }
    }

    @Override
    protected void read(CompoundTag compound, Provider registries, boolean clientPacket) {
        super.read(compound, registries, clientPacket);
        chamberPos = compound.contains("Chamber") ? NbtUtils.readBlockPos(compound, "Chamber").orElse(null) : null;
    }

    @Override
    public void write(CompoundTag compound, Provider registries, boolean clientPacket) {
        if (chamberPos != null) {
            compound.put("Chamber", NbtUtils.writeBlockPos(chamberPos));
        }
        super.write(compound, registries, clientPacket);
    }

    @Override
    public boolean addToGoggleTooltip(List<Component> tooltip, boolean isPlayerSneaking) {
        boolean added = super.addToGoggleTooltip(tooltip, isPlayerSneaking);
        if (level != null && chamberPos != null && level.getBlockEntity(chamberPos) instanceof ReactionChamberControllerBlockEntity chamber) {
            chamber.addStirringTooltip(tooltip);
            return true;
        }
        return added;
    }
}
