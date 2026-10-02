package com.koala.reactingreactions.content.ponder;

import com.koala.reactingreactions.ReactingReactions;

import net.createmod.ponder.api.registration.PonderPlugin;
import net.createmod.ponder.api.registration.PonderSceneRegistrationHelper;
import net.createmod.ponder.api.registration.PonderTagRegistrationHelper;
import net.minecraft.resources.ResourceLocation;

public class CRRPonderPlugin implements PonderPlugin {
    @Override
    public String getModId() {
        return ReactingReactions.MODID;
    }

    @Override
    public void registerScenes(PonderSceneRegistrationHelper<ResourceLocation> helper) {
        CRRPonderScenes.register(helper);
    }

    @Override
    public void registerTags(PonderTagRegistrationHelper<ResourceLocation> helper) {
        CRRPonderScenes.registerTags(helper);
    }
}
