package com.koala.reactingreactions.content.steam;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.content.kinetics.base.HorizontalKineticBlock;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SteamTurbineBlock extends HorizontalKineticBlock implements IBE<SteamTurbineBlockEntity> {
    public SteamTurbineBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) {
        return face == state.getValue(HORIZONTAL_FACING).getOpposite();
    }

    @Override
    public Direction.Axis getRotationAxis(BlockState state) {
        return state.getValue(HORIZONTAL_FACING).getAxis();
    }

    @Override
    public Class<SteamTurbineBlockEntity> getBlockEntityClass() {
        return SteamTurbineBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends SteamTurbineBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.STEAM_TURBINE.get();
    }
}
