package com.koala.reactingreactions.content.multiblock;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidActionResult;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.fluids.FluidUtil;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.items.wrapper.PlayerInvWrapper;

/** Buckets and other fluid containers used on a machine fill its inputs, or drain it when empty. */
public final class FluidContainerInteraction {
    private FluidContainerInteraction() {
    }

    /** @return SUCCESS if a fluid was moved, or null to let the block's normal interaction continue. */
    public static ItemInteractionResult tryInteract(Player player, InteractionHand hand, Level level, BlockPos pos, BlockHitResult hit) {
        // Claimed on both sides, or the client would also use the item (placing a bucket's water in the world).
        boolean containerOnMachine = FluidUtil.getFluidHandler(player.getItemInHand(hand)).isPresent()
                && level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit.getDirection()) != null;
        if (level.isClientSide) {
            return containerOnMachine ? ItemInteractionResult.sidedSuccess(true) : null;
        }
        // An empty container drains outputs first.
        ItemStack held = player.getItemInHand(hand);
        // getFluidContained is empty for an empty container too, so check for a handler with nothing in it.
        if (FluidUtil.getFluidHandler(held).isPresent() && FluidUtil.getFluidContained(held).isEmpty()) {
            IFluidHandler face = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, hit.getDirection());
            // A single tank, such as a tower row, is its own source.
            IFluidHandler source = face instanceof InputFillOutputDrainWrapper machine ? machine.drainSource() : face;
            if (source != null && source.getFluidInTank(0).isEmpty() && !(face instanceof InputFillOutputDrainWrapper)) {
                source = null;
            }
            if (source != null && held.is(Items.BUCKET) && tryFillVanillaBucket(player, hand, held, source)) {
                return ItemInteractionResult.sidedSuccess(false);
            }
            if (source != null) {
                FluidActionResult filled = FluidUtil.tryFillContainerAndStow(held, source, new PlayerInvWrapper(player.getInventory()),
                        Integer.MAX_VALUE, player, true);
                if (filled.isSuccess()) {
                    player.setItemInHand(hand, filled.getResult());
                    return ItemInteractionResult.sidedSuccess(false);
                }
            }
        }
        if (FluidUtil.interactWithFluidHandler(player, hand, level, pos, hit.getDirection())) {
            return ItemInteractionResult.sidedSuccess(false);
        }
        // Nothing moved (say the tank is full): still consumed, so the bucket does not spill into the world.
        return containerOnMachine ? ItemInteractionResult.sidedSuccess(false) : null;
    }

    /** Our virtual fluids have no bucket of their own, so an empty bucket is filled with the matching "<fluid>_bucket" item directly. */
    private static boolean tryFillVanillaBucket(Player player, InteractionHand hand, ItemStack held, IFluidHandler source) {
        FluidStack fluid = source.getFluidInTank(0);
        if (fluid.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }
        ResourceLocation fluidId = BuiltInRegistries.FLUID.getKey(fluid.getFluid());
        Item bucket = BuiltInRegistries.ITEM.get(fluidId.withSuffix("_bucket"));
        if (bucket == Items.AIR) {
            return false;
        }
        FluidStack drained = source.drain(new FluidStack(fluid.getFluid(), FluidType.BUCKET_VOLUME), IFluidHandler.FluidAction.EXECUTE);
        if (drained.getAmount() < FluidType.BUCKET_VOLUME) {
            return false;
        }
        ItemStack filled = new ItemStack(bucket);
        if (!player.getAbilities().instabuild) {
            held.shrink(1);
        }
        if (held.isEmpty()) {
            player.setItemInHand(hand, filled);
        } else if (!player.getInventory().add(filled)) {
            player.drop(filled, false);
        }
        player.level().playSound(null, player.blockPosition(), SoundEvents.BUCKET_FILL, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}
