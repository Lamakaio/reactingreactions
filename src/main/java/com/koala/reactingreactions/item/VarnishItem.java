package com.koala.reactingreactions.item;

import com.koala.reactingreactions.registry.CRRBlocks;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/** Used on planks, varnishes them; used on a stripped log or wood, puts its bark back. One varnish each time. */
public class VarnishItem extends Item {
    /** Stripped blocks back to their logs: the axe's strip table, read backwards. */
    private static Map<Block, Block> unstripped;

    public VarnishItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState varnished = varnished(level.getBlockState(pos));
        if (varnished == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            level.setBlock(pos, varnished, Block.UPDATE_ALL_IMMEDIATE);
            level.playSound(null, pos, SoundEvents.HONEYCOMB_WAX_ON, SoundSource.BLOCKS, 1.0F, 1.0F);
            // Vanilla's waxing sparkle.
            level.levelEvent(3003, pos, 0);
            if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
                context.getItemInHand().shrink(1);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    private static BlockState varnished(BlockState state) {
        if (state.is(BlockTags.PLANKS) && !state.is(CRRBlocks.VARNISHED_PLANKS.get())) {
            return CRRBlocks.VARNISHED_PLANKS.get().defaultBlockState();
        }
        Block log = unstripped().get(state.getBlock());
        if (log == null) {
            return null;
        }
        BlockState restored = log.defaultBlockState();
        return state.hasProperty(BlockStateProperties.AXIS) && restored.hasProperty(BlockStateProperties.AXIS)
                ? restored.setValue(BlockStateProperties.AXIS, state.getValue(BlockStateProperties.AXIS)) : restored;
    }

    private static Map<Block, Block> unstripped() {
        if (unstripped == null) {
            Map<Block, Block> map = new HashMap<>();
            AxeItem.STRIPPABLES.forEach((log, stripped) -> map.put(stripped, log));
            unstripped = map;
        }
        return unstripped;
    }
}
