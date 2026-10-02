package com.koala.reactingreactions.content.toxic;

import com.koala.reactingreactions.Config;
import com.koala.reactingreactions.item.ChemicalFlaskItem;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRItems;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LevelEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * A thrown {@link ChemicalFlaskItem}. It shatters where it lands: a Chemical Flask spills its 500 mB there (a pool, or a vent for
 * a gas), a Blast Flask of a flammable fluid goes off as a full 500 mB pool would if it caught fire.
 */
public class ChemicalFlaskEntity extends ThrowableItemProjectile {
    public ChemicalFlaskEntity(EntityType<? extends ChemicalFlaskEntity> type, Level level) {
        super(type, level);
    }

    public ChemicalFlaskEntity(Level level, LivingEntity thrower, ItemStack flask) {
        super(CRREntities.CHEMICAL_FLASK.get(), thrower, level);
        setItem(flask);
    }

    public ChemicalFlaskEntity(Level level, double x, double y, double z, ItemStack flask) {
        super(CRREntities.CHEMICAL_FLASK.get(), x, y, z, level);
        setItem(flask);
    }

    @Override
    protected Item getDefaultItem() {
        return CRRItems.CHEMICAL_FLASK.get();
    }

    @Override
    protected double getDefaultGravity() {
        // A splash potion's arc.
        return 0.05;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!(level() instanceof ServerLevel level)) {
            return;
        }
        ItemStack stack = getItem();
        FluidStack fluid = ChemicalFlaskItem.contents(stack);
        Vec3 at = result.getLocation();
        // The open block in front of whatever it hit, so a pool lands on that face's side.
        BlockPos pos = result instanceof BlockHitResult blockHit && result.getType() == HitResult.Type.BLOCK
                ? blockHit.getBlockPos().relative(blockHit.getDirection()) : BlockPos.containing(at);
        level.levelEvent(LevelEvent.PARTICLES_SPELL_POTION_SPLASH, pos, fluid.isEmpty() ? 0x909090 : Leaks.colorOf(fluid.getFluid()) & 0xFFFFFF);
        if (!fluid.isEmpty() && Config.toxicityEnabled()) {
            ToxicFluid toxic = Toxicity.of(fluid);
            boolean blast = stack.getItem() instanceof ChemicalFlaskItem flask && flask.isBlast();
            if (blast && toxic.flammable() && Ignition.enabled()) {
                // Burning it still releases fumes.
                Contamination.addFrom(level, pos, toxic, Leaks.contaminationOf(toxic, fluid.getAmount()));
                Ignition.ignite(level, at, toxic, fluid.getAmount());
            } else {
                Leaks.spill(level, pos, fluid.getFluid(), fluid.getAmount());
            }
        }
        discard();
    }
}
