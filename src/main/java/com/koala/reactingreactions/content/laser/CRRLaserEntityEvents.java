package com.koala.reactingreactions.content.laser;

import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.animal.Wolf;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;

/** Cats and dogs (wolves) chase laser dots. */
public class CRRLaserEntityEvents {
    private CRRLaserEntityEvents() {
    }

    @SubscribeEvent
    public static void onJoinLevel(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Cat cat) {
            cat.goalSelector.addGoal(3, new ChaseLaserDotGoal(cat));
        } else if (event.getEntity() instanceof Wolf wolf) {
            wolf.goalSelector.addGoal(3, new ChaseLaserDotGoal(wolf));
        }
    }
}
