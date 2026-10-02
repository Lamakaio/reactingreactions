package com.koala.reactingreactions.content.multiblock.attachment;

import com.koala.reactingreactions.content.multiblock.MultiblockControllerBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Only there for the needle: the client eases it toward the gauge's reading. */
public class GaugeBlockEntity extends BlockEntity {
    private float needle;
    private long lastFrame = -1;

    public GaugeBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** The needle's position, 0 to 1, eased toward the reading once per game tick. Client side. */
    public float needle() {
        if (level == null) {
            return 0;
        }
        long tick = level.getGameTime();
        if (tick != lastFrame) {
            lastFrame = tick;
            MultiblockControllerBlockEntity<?> machine = MachineAttachments.machineFor(level, worldPosition, getBlockState());
            float target = machine == null ? 0 : machine.gaugeSignal(getBlockState().getValue(AttachmentBlock.READING) == AttachmentBlock.Reading.FILL) / 15F;
            needle += (target - needle) * 0.3F;
        }
        return needle;
    }
}
