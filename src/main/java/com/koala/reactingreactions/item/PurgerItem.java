package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.drill.DerrickBlockEntity;
import com.koala.reactingreactions.content.multiblock.MultiblockWallBlockEntity;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.blockEntity.behaviour.fluid.SmartFluidTankBehaviour;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.IItemHandler;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Set;

/** Shift-right-click empties a block: items drop at the clicked face, fluids and gases are destroyed. */
public class PurgerItem extends Item {
    // Bounds the loops on creative or bottomless storage.
    private static final int MAX_PULLS = 64;

    public PurgerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        Vec3 drop = Vec3.atCenterOf(pos.relative(context.getClickedFace()));
        int mb = clearOwnTanks(level, pos);
        int items = 0;
        for (IFluidHandler fluids : handlers(level, pos, Capabilities.FluidHandler.BLOCK)) {
            mb += drainAll(fluids);
        }
        for (IItemHandler inventory : handlers(level, pos, Capabilities.ItemHandler.BLOCK)) {
            items += dropAll(level, inventory, drop);
        }
        if (mb == 0 && items == 0) {
            player.displayClientMessage(Component.literal("Nothing to purge"), true);
            return InteractionResult.SUCCESS;
        }
        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 0.8F, 0.8F);
        player.displayClientMessage(Component.literal("Purged " + mb + " mB and " + items + " items"), true);
        return InteractionResult.SUCCESS;
    }

    /** Create-style tanks, emptied directly so even those closed to extraction from outside are cleared. */
    private static int clearOwnTanks(Level level, BlockPos pos) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MultiblockWallBlockEntity wall) {
            be = wall.getController();
        } else if (be instanceof DerrickBlockEntity frame) {
            be = frame.getController();
        }
        if (!(be instanceof SmartBlockEntity smart)) {
            return 0;
        }
        int cleared = 0;
        for (BlockEntityBehaviour behaviour : smart.getAllBehaviours()) {
            if (behaviour instanceof SmartFluidTankBehaviour tanks) {
                // Multi-tank ones, like the basin, are emptied through the capability instead.
                cleared += tanks.getPrimaryHandler().getFluidAmount();
                tanks.getPrimaryHandler().setFluid(FluidStack.EMPTY);
            }
        }
        return cleared;
    }

    /** The block's handlers from every side, each once. */
    private static <T> Set<T> handlers(Level level, BlockPos pos, net.neoforged.neoforge.capabilities.BlockCapability<T, Direction> capability) {
        Set<T> found = Collections.newSetFromMap(new IdentityHashMap<>());
        T unsided = level.getCapability(capability, pos, null);
        if (unsided != null) {
            found.add(unsided);
        }
        for (Direction side : Direction.values()) {
            T handler = level.getCapability(capability, pos, side);
            if (handler != null) {
                found.add(handler);
            }
        }
        return found;
    }

    private static int drainAll(IFluidHandler handler) {
        int drained = 0;
        for (int tank = 0; tank < handler.getTanks(); tank++) {
            for (int pull = 0; pull < MAX_PULLS; pull++) {
                FluidStack contained = handler.getFluidInTank(tank);
                if (contained.isEmpty()) {
                    break;
                }
                int taken = handler.drain(contained.copy(), IFluidHandler.FluidAction.EXECUTE).getAmount();
                if (taken <= 0) {
                    break;
                }
                drained += taken;
            }
        }
        return drained;
    }

    private static int dropAll(Level level, IItemHandler handler, Vec3 at) {
        int dropped = 0;
        for (int slot = 0; slot < handler.getSlots(); slot++) {
            for (int pull = 0; pull < MAX_PULLS; pull++) {
                ItemStack taken = handler.extractItem(slot, 64, false);
                if (taken.isEmpty()) {
                    break;
                }
                dropped += taken.getCount();
                Containers.dropItemStack(level, at.x, at.y, at.z, taken);
            }
        }
        return dropped;
    }
}
