package com.koala.reactingreactions.item;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.event.CRREquipmentEvents;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** The Plasma Multitool's mining shapes: which blocks break along with the one mined. */
public final class AreaMining {
    public static final List<String> SHAPES = List.of("single", "3x3", "5x5", "3x3x3", "tunnel", "vein");
    private static final int VEIN_LIMIT = 64;
    private static boolean breaking;
    private static final Map<UUID, Double> GREASE_CARRY = new HashMap<>();

    private AreaMining() {
    }

    /** The face of {@code origin} the player is looking at. */
    public static Direction face(Player player, BlockPos origin) {
        HitResult hit = player.pick(player.blockInteractionRange(), 1.0F, false);
        if (hit instanceof BlockHitResult blockHit && blockHit.getBlockPos().equals(origin)) {
            return blockHit.getDirection();
        }
        return Direction.getNearest(player.getLookAngle()).getOpposite();
    }

    /** The other blocks the multitool would break with {@code origin}, limited by the grease in its tank. */
    public static List<BlockPos> extraBlocks(Level level, Player player, ItemStack tool, BlockPos origin) {
        return player.isShiftKeyDown() ? List.of() : extraBlocks(level, tool, origin, face(player, origin), player.isCreative());
    }

    /** The same, for a known face; {@code free} skips the fuel limit. */
    public static List<BlockPos> extraBlocks(Level level, ItemStack tool, BlockPos origin, Direction face, boolean free) {
        if (!(tool.getItem() instanceof PlasmaMultitoolItem item)) {
            return List.of();
        }
        BlockState originState = level.getBlockState(origin);
        Set<BlockPos> area = new LinkedHashSet<>();
        switch (SHAPES.get(item.setting(tool, "shape"))) {
            case "3x3" -> plane(area, origin, face, 1, 1);
            case "5x5" -> plane(area, origin, face, 2, 1);
            case "3x3x3" -> plane(area, origin, face, 1, 3);
            case "tunnel" -> {
                for (int depth = 0; depth < 3; depth++) {
                    BlockPos step = origin.relative(face.getOpposite(), depth);
                    area.add(step);
                    if (face.getAxis().isHorizontal()) {
                        area.add(step.below());
                    }
                }
            }
            case "vein" -> vein(level, area, origin, originState);
            default -> {
            }
        }
        area.remove(origin);
        float hardness = originState.getDestroySpeed(level, origin);
        int affordable = (int) (FluidTankHolder.contents(tool).getAmount() / Math.max(0.001, Config.number(Config.MULTITOOL_MB_PER_BLOCK, 0.1)));
        if (free) {
            affordable = Integer.MAX_VALUE;
        }
        List<BlockPos> extra = new ArrayList<>();
        for (BlockPos pos : area) {
            BlockState state = level.getBlockState(pos);
            float other = state.getDestroySpeed(level, pos);
            if (extra.size() < affordable && !state.isAir() && other >= 0 && other <= hardness * 2 + 1 && tool.getDestroySpeed(state) > 1) {
                extra.add(pos);
            }
        }
        return extra;
    }

    /** A square of radius {@code radius} across the face, {@code depth} blocks deep into it. */
    private static void plane(Set<BlockPos> area, BlockPos origin, Direction face, int radius, int depth) {
        Direction.Axis axis = face.getAxis();
        for (int d = 0; d < depth; d++) {
            BlockPos centre = origin.relative(face.getOpposite(), d);
            for (int a = -radius; a <= radius; a++) {
                for (int b = -radius; b <= radius; b++) {
                    area.add(switch (axis) {
                        case X -> centre.offset(0, a, b);
                        case Y -> centre.offset(a, 0, b);
                        case Z -> centre.offset(a, b, 0);
                    });
                }
            }
        }
    }

    /** Connected blocks of the same kind, diagonals included. */
    private static void vein(Level level, Set<BlockPos> area, BlockPos origin, BlockState originState) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>(List.of(origin));
        area.add(origin);
        while (!queue.isEmpty() && area.size() < VEIN_LIMIT) {
            BlockPos current = queue.poll();
            for (BlockPos next : BlockPos.betweenClosed(current.offset(-1, -1, -1), current.offset(1, 1, 1))) {
                if (area.size() < VEIN_LIMIT && !area.contains(next) && level.getBlockState(next).is(originState.getBlock())) {
                    BlockPos found = next.immutable();
                    area.add(found);
                    queue.add(found);
                }
            }
        }
    }

    /** Breaks the extra blocks as the player would, paying for them from the tank. Called when the player mines {@code origin}. */
    public static void breakExtra(ServerPlayer player, ItemStack tool, BlockPos origin) {
        if (breaking) {
            return;
        }
        List<BlockPos> extra = extraBlocks(player.level(), player, tool, origin);
        if (extra.isEmpty()) {
            return;
        }
        breaking = true;
        try {
            int broken = 0;
            for (BlockPos pos : extra) {
                if (player.gameMode.destroyBlock(pos)) {
                    broken++;
                }
            }
            if (!player.isCreative()) {
                CRREquipmentEvents.consumeFractional(GREASE_CARRY, player, broken * Config.number(Config.MULTITOOL_MB_PER_BLOCK, 0.1), tool);
            }
        } finally {
            breaking = false;
        }
    }
}
