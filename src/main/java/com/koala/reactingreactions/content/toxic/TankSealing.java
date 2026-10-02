package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.registry.CRRItems;
import com.simibubi.create.content.fluids.tank.FluidTankBlockEntity;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import org.jetbrains.annotations.Nullable;

/**
 * A Gasket on a Create Fluid Tank: the whole tank (its controller holds the flag) stops leaking. Used on the tank it goes in;
 * sneaking with an empty hand takes it back out, and breaking the controller drops it.
 */
public final class TankSealing {
    /** Mixed into Create's Fluid Tank block entity (FluidTankBlockEntityMixin). */
    public interface Sealable {
        boolean crr$isSealed();

        void crr$setSealed(boolean sealed);
    }

    private TankSealing() {
    }

    @Nullable
    private static FluidTankBlockEntity controllerAt(Level level, BlockPos pos) {
        return level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank ? tank.getControllerBE() : null;
    }

    public static boolean isSealed(FluidTankBlockEntity tank) {
        FluidTankBlockEntity controller = tank.getControllerBE();
        return controller instanceof Sealable sealable && sealable.crr$isSealed();
    }

    /** Puts a gasket on the tank at {@code pos}; PASS when there is no tank, FAIL when it is already sealed. */
    public static InteractionResult seal(Level level, BlockPos pos, Player player, ItemStack gasket) {
        FluidTankBlockEntity controller = controllerAt(level, pos);
        if (!(controller instanceof Sealable sealable)) {
            return InteractionResult.PASS;
        }
        if (sealable.crr$isSealed()) {
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide) {
            sealable.crr$setSealed(true);
            controller.setChanged();
            controller.sendData();
            if (!player.getAbilities().instabuild) {
                gasket.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.COPPER_PLACE, SoundSource.BLOCKS, 1.0F, 0.9F);
        }
        return InteractionResult.SUCCESS;
    }

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        Player player = event.getEntity();
        if (!player.isShiftKeyDown() || event.getHand() != InteractionHand.MAIN_HAND || !player.getMainHandItem().isEmpty()) {
            return;
        }
        Level level = event.getLevel();
        FluidTankBlockEntity controller = controllerAt(level, event.getPos());
        if (!(controller instanceof Sealable sealable) || !sealable.crr$isSealed()) {
            return;
        }
        if (!level.isClientSide) {
            sealable.crr$setSealed(false);
            controller.setChanged();
            controller.sendData();
            player.getInventory().placeItemBackInInventory(new ItemStack(CRRItems.GASKET.get()));
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
