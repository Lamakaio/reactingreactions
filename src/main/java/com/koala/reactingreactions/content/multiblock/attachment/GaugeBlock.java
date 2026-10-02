package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.registry.CRRBlockEntities;
import com.simibubi.create.foundation.block.IBE;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.shapes.VoxelShape;

/** The Machine Gauge: an attachment whose needle, drawn by {@link GaugeRenderer}, shows what it reads. */
public class GaugeBlock extends AttachmentBlock implements IBE<GaugeBlockEntity> {
    public GaugeBlock(Properties properties, VoxelShape shape) {
        super(properties, Kind.GAUGE, shape);
    }

    @Override
    public Class<GaugeBlockEntity> getBlockEntityClass() {
        return GaugeBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends GaugeBlockEntity> getBlockEntityType() {
        return CRRBlockEntities.MACHINE_GAUGE.get();
    }
}
