package com.koala.reactingreactions.content.drill;

import com.koala.reactingreactions.registry.CRRBlocks;
import com.koala.reactingreactions.registry.CRRFeatures;
import com.koala.reactingreactions.registry.CRRFluids;
import com.mojang.serialization.Codec;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Runs once per chunk after the other underground features and does two things, both on the chunk it is placed for:
 * <ul>
 * <li>Turns one block of some veins of each rock in the {@code rich_vein} data map into its Rich Vein. A vein is a connected group of
 * that rock's blocks within the chunk; Create's veins are not hookable, so this finds them after the fact.</li>
 * <li>Cleans up the oil veins: crude oil source blocks touching air become oil shale (no oil seeping out of cave walls), and each oil
 * vein gets one Rich Oil Vein block.</li>
 * </ul>
 */
public class RichVeinFeature extends Feature<NoneFeatureConfiguration> {
    private static final int SCAN_TOP_Y = 128;

    public RichVeinFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    private record Target(Block rock, RichVein vein) {
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        RandomSource random = context.random();
        int x0 = context.origin().getX();
        int z0 = context.origin().getZ();
        int minY = level.getMinBuildHeight();
        int maxY = Math.min(level.getMaxBuildHeight(), SCAN_TOP_Y);

        Map<Block, Target> targets = new HashMap<>();
        BuiltInRegistries.BLOCK.getDataMap(CRRFeatures.RICH_VEIN).forEach((key, vein) -> {
            Block rock = BuiltInRegistries.BLOCK.get(key);
            targets.put(rock, new Target(rock, vein));
        });
        Block crudeOil = CRRFluids.CRUDE_OIL.get().getSource().defaultFluidState().createLegacyBlock().getBlock();

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        // Pass 1: oil sources touching air become oil shale.
        List<BlockPos> exposedOil = new ArrayList<>();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    pos.set(x0 + x, y, z0 + z);
                    if (level.getBlockState(pos).getBlock() == crudeOil && touchesAir(level, pos, x0, z0)) {
                        exposedOil.add(pos.immutable());
                    }
                }
            }
        }
        for (BlockPos exposed : exposedOil) {
            level.setBlock(exposed, CRRBlocks.OIL_SHALE.get().defaultBlockState(), 2);
        }

        // Pass 2: find the veins and give each one a Rich Vein block.
        Set<BlockPos> visited = new HashSet<>();
        boolean placedAny = !exposedOil.isEmpty();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y < maxY; y++) {
                    pos.set(x0 + x, y, z0 + z);
                    Target target = targets.get(level.getBlockState(pos).getBlock());
                    if (target == null || visited.contains(pos)) {
                        continue;
                    }
                    List<BlockPos> vein = collect(level, pos.immutable(), target, visited, crudeOil, x0, z0, minY, maxY);
                    if (vein.size() >= target.vein().minVeinSize() && random.nextFloat() < target.vein().chance()) {
                        BlockPos chosen = vein.get(random.nextInt(vein.size()));
                        level.setBlock(chosen, target.vein().rich().defaultBlockState()
                                .setValue(RichOreVeinBlock.RICHNESS, target.vein().rollRichness(random)), 2);
                        placedAny = true;
                    }
                }
            }
        }
        return placedAny;
    }

    private static boolean touchesAir(WorldGenLevel level, BlockPos pos, int x0, int z0) {
        for (Direction direction : Direction.values()) {
            BlockPos neighbour = pos.relative(direction);
            // Only the chunk being generated (plus what is already readable) is looked at; leave anything outside it alone.
            if (neighbour.getX() < x0 || neighbour.getX() >= x0 + 16 || neighbour.getZ() < z0 || neighbour.getZ() >= z0 + 16) {
                continue;
            }
            if (level.getBlockState(neighbour).isAir()) {
                return true;
            }
        }
        return false;
    }

    /** The connected group of the target's blocks (oil sources count as part of an oil vein) starting at {@code start}, inside the chunk. */
    private static List<BlockPos> collect(WorldGenLevel level, BlockPos start, Target target, Set<BlockPos> visited, Block crudeOil,
            int x0, int z0, int minY, int maxY) {
        List<BlockPos> rockBlocks = new ArrayList<>();
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        visited.add(start);
        while (!queue.isEmpty()) {
            BlockPos current = queue.poll();
            BlockState state = level.getBlockState(current);
            if (state.getBlock() == target.rock()) {
                rockBlocks.add(current);
            }
            for (Direction direction : Direction.values()) {
                BlockPos next = current.relative(direction);
                if (next.getX() < x0 || next.getX() >= x0 + 16 || next.getZ() < z0 || next.getZ() >= z0 + 16 || next.getY() < minY || next.getY() >= maxY
                        || visited.contains(next)) {
                    continue;
                }
                Block block = level.getBlockState(next).getBlock();
                if (block == target.rock() || (target.rock() == CRRBlocks.OIL_SHALE.get() && block == crudeOil)) {
                    visited.add(next);
                    queue.add(next);
                }
            }
        }
        return rockBlocks;
    }
}
