package com.koala.reactingreactions.content.laser;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

/** Cats and dogs chase the nearest laser dot. */
public class ChaseLaserDotGoal extends Goal {
    private static final double RADIUS = 16.0;
    private static final double SPEED = 1.2;

    private final Mob mob;
    private Vec3 target;

    public ChaseLaserDotGoal(Mob mob) {
        this.mob = mob;
        setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        return findTarget();
    }

    @Override
    public boolean canContinueToUse() {
        return findTarget();
    }

    private boolean findTarget() {
        if (!(mob.level() instanceof ServerLevel level)) {
            return false;
        }
        target = LaserTargets.nearestTo(level, mob.position(), RADIUS);
        return target != null;
    }

    @Override
    public void tick() {
        if (target == null) {
            return;
        }
        mob.getLookControl().setLookAt(target.x, target.y, target.z);
        // Along the ground only: when the dot is out of reach, the pet just watches it.
        mob.getNavigation().moveTo(target.x, mob.getY(), target.z, SPEED);
    }

    @Override
    public void stop() {
        target = null;
    }
}
