package com.koala.reactingreactions.event;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Glass in the {@code c:glass_blocks} and {@code c:glass_panes} tags melts near lava and in the Nether; Reinforced Glass does not. */
public class CRRGlassMeltEvents {
    private static final TagKey<Block> GLASS_BLOCKS = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "glass_blocks"));
    private static final TagKey<Block> GLASS_PANES = TagKey.create(Registries.BLOCK, ResourceLocation.fromNamespaceAndPath("c", "glass_panes"));

    @SubscribeEvent
    public static void onNeighborNotify(BlockEvent.NeighborNotifyEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        BlockPos pos = event.getPos();
        if (shouldMelt(level, pos, event.getState())) {
            melt(level, pos);
        }
    }

    @SubscribeEvent
    public static void onEntityPlace(BlockEvent.EntityPlaceEvent event) {
        if (!(event.getLevel() instanceof Level level) || level.isClientSide()) {
            return;
        }
        BlockPos pos = event.getPos();
        if (shouldMelt(level, pos, event.getPlacedBlock())) {
            melt(level, pos);
        }
    }

    private static boolean shouldMelt(Level level, BlockPos pos, BlockState state) {
        if (!state.is(GLASS_BLOCKS) && !state.is(GLASS_PANES)) {
            return false;
        }
        return level.dimension() == Level.NETHER || isNextToLava(level, pos);
    }

    private static boolean isNextToLava(Level level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (level.getFluidState(pos.relative(direction)).is(FluidTags.LAVA)) {
                return true;
            }
        }
        return false;
    }

    private static void melt(Level level, BlockPos pos) {
        level.removeBlock(pos, false);
        level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.0F);
    }
}
