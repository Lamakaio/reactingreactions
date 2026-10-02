package com.koala.reactingreactions.item;

import com.koala.reactingreactions.content.toxic.ChemicalFlaskEntity;
import com.koala.reactingreactions.registry.CRRDataComponents;

import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileItem;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;

/**
 * A thrown flask of a toxic fluid, filled by a Spout (500 mB into a glass bottle); the fluid is kept on the stack. The Chemical
 * Flask spills it where it lands; the Blast Flask, made with gunpowder, sets it off there instead. See {@link ChemicalFlaskEntity}.
 */
public class ChemicalFlaskItem extends Item implements ProjectileItem {
    public static final int AMOUNT_MB = 500;
    private final boolean blast;

    public ChemicalFlaskItem(Properties properties, boolean blast) {
        super(properties);
        this.blast = blast;
    }

    public boolean isBlast() {
        return blast;
    }

    public static FluidStack contents(ItemStack stack) {
        return stack.getOrDefault(CRRDataComponents.FLUID_TANK.get(), SimpleFluidContent.EMPTY).copy();
    }

    @Override
    public Component getName(ItemStack stack) {
        FluidStack fluid = contents(stack);
        return fluid.isEmpty() ? super.getName(stack) : Component.translatable(getDescriptionId(stack) + ".filled", fluid.getHoverName());
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (contents(stack).isEmpty()) {
            return InteractionResultHolder.fail(stack);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SPLASH_POTION_THROW, SoundSource.PLAYERS, 0.5F,
                0.4F / (level.getRandom().nextFloat() * 0.4F + 0.8F));
        if (!level.isClientSide) {
            ChemicalFlaskEntity flask = new ChemicalFlaskEntity(level, player, stack.copyWithCount(1));
            flask.shootFromRotation(player, player.getXRot(), player.getYRot(), -20.0F, 0.5F, 1.0F);
            level.addFreshEntity(flask);
        }
        player.awardStat(Stats.ITEM_USED.get(this));
        stack.consume(1, player);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public Projectile asProjectile(Level level, Position pos, ItemStack stack, Direction direction) {
        return new ChemicalFlaskEntity(level, pos.x(), pos.y(), pos.z(), stack.copyWithCount(1));
    }

    @Override
    public DispenseConfig createDispenseConfig() {
        // Lobbed like a splash potion.
        return DispenseConfig.builder().uncertainty(DispenseConfig.DEFAULT.uncertainty() * 0.5F).power(DispenseConfig.DEFAULT.power() * 1.25F).build();
    }
}
