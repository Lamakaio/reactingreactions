package com.koala.reactingreactions.content.equipment;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.item.AreaMining;
import com.koala.reactingreactions.item.ExoSettings;
import com.koala.reactingreactions.item.NeonBladeItem;
import com.koala.reactingreactions.item.PlasmaMultitoolItem;
import com.koala.reactingreactions.registry.CRRItems;
import com.mojang.blaze3d.platform.InputConstants;
import com.simibubi.create.content.equipment.goggles.GogglesItem;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import org.lwjgl.glfw.GLFW;

/** The chase gear's client side: the settings key, sneak-use opening it, the mining outline, goggles and the blade's glow. */
@EventBusSubscriber(modid = ReactingReactions.MODID, value = Dist.CLIENT)
public final class ExoClient {
    public static final KeyMapping SETTINGS_KEY = new KeyMapping("key.reactingreactions.exo_settings", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K,
            "key.categories.reactingreactions");

    private ExoClient() {
    }

    @SubscribeEvent
    static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(SETTINGS_KEY);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemProperties.register(CRRItems.NEON_BLADE.get(), ReactingReactions.asResource("lit"), (stack, level, entity, seed) -> NeonBladeItem.lit(stack) ? 1 : 0);
            GogglesItem.addIsWearingPredicate(player -> player.getItemBySlot(EquipmentSlot.HEAD).is(CRRItems.EXO_HELMET.get()));
        });
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (SETTINGS_KEY.consumeClick()) {
            if (minecraft.player != null && minecraft.screen == null) {
                minecraft.setScreen(new ExoSettingsScreen());
            }
        }
    }

    private static boolean opensSettings(ItemStack stack) {
        return stack.getItem() instanceof ExoSettings.Configurable && !(stack.getItem() instanceof ArmorItem);
    }

    @SubscribeEvent
    static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (event.getLevel().isClientSide && event.getEntity().isShiftKeyDown() && opensSettings(event.getItemStack())) {
            Minecraft.getInstance().setScreen(new ExoSettingsScreen());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getLevel().isClientSide && event.getEntity().isShiftKeyDown() && opensSettings(event.getItemStack())) {
            Minecraft.getInstance().setScreen(new ExoSettingsScreen());
            event.setCancellationResult(InteractionResult.SUCCESS);
            event.setCanceled(true);
        }
    }

    /** Outlines the extra blocks the multitool's shape will break. */
    @SubscribeEvent
    static void onHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        ItemStack tool = minecraft.player == null ? ItemStack.EMPTY : minecraft.player.getMainHandItem();
        if (!(tool.getItem() instanceof PlasmaMultitoolItem)) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        var lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        for (BlockPos pos : AreaMining.extraBlocks(minecraft.level, minecraft.player, tool, event.getTarget().getBlockPos())) {
            LevelRenderer.renderLineBox(event.getPoseStack(), lines, new AABB(pos).move(-camera.x, -camera.y, -camera.z), 0, 0, 0, 0.4F);
        }
    }
}
