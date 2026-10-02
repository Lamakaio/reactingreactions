package com.koala.reactingreactions;

import com.koala.reactingreactions.content.equipment.worn.AccessoriesWornRenderer;
import com.koala.reactingreactions.content.equipment.worn.CuriosWornRenderer;
import com.koala.reactingreactions.content.equipment.worn.WornModels;
import com.koala.reactingreactions.content.render.CRRPartialModels;
import com.koala.reactingreactions.content.laser.ClientLaserPointers;
import com.koala.reactingreactions.content.ponder.CRRPonderPlugin;
import com.koala.reactingreactions.content.toxic.ClientContamination;
import com.koala.reactingreactions.content.toxic.HazeParticle;
import com.koala.reactingreactions.content.toxic.LeakPoolRenderer;
import com.koala.reactingreactions.content.toxic.ToxicStainRenderer;
import com.koala.reactingreactions.content.toxic.ToxicityHud;
import com.koala.reactingreactions.item.FluidTankHolder;
import com.koala.reactingreactions.network.CRRDoubleJumpPayload;
import com.koala.reactingreactions.registry.CRREntities;
import com.koala.reactingreactions.registry.CRRItems;
import com.koala.reactingreactions.registry.CRRParticles;
import com.simibubi.create.foundation.item.ItemDescription;
import com.simibubi.create.foundation.item.TooltipModifier;

import net.createmod.catnip.lang.FontHelper;
import net.createmod.ponder.foundation.PonderIndex;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.RegisterParticleProvidersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.network.PacketDistributor;

@Mod(value = ReactingReactions.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = ReactingReactions.MODID, value = Dist.CLIENT)
public class ReactingReactionsClient {
    private static boolean airborneLastTick;

    public ReactingReactionsClient(ModContainer container) {
        CRRPartialModels.init();
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        NeoForge.EVENT_BUS.addListener(ReactingReactionsClient::onClientTick);
        NeoForge.EVENT_BUS.addListener(ToxicStainRenderer::render);
    }

    @SubscribeEvent
    static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(CRREntities.LEAK_POOL.get(), LeakPoolRenderer::new);
        event.registerEntityRenderer(CRREntities.CHEMICAL_FLASK.get(), ThrownItemRenderer::new);
    }

    /** The green vignette of toxic air and poisoning, and the "Toxic air" warning. */
    @SubscribeEvent
    static void onRegisterWarningLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CROSSHAIR, ReactingReactions.asResource("toxic_air_warning"), ToxicityHud::renderVignette);
    }

    /** The toxicity gauge: a small bar above the food bar, only shown while it is above zero. */
    @SubscribeEvent
    static void onRegisterGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, ReactingReactions.asResource("toxicity_gauge"), ToxicityHud::renderGauge);
    }

    /** The flight pack's fuel, shown while flying. */
    @SubscribeEvent
    static void onRegisterFuelGaugeLayer(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.FOOD_LEVEL, ReactingReactions.asResource("aerozine_fuel_gauge"),
                (graphics, deltaTracker) -> {
                    Minecraft minecraft = Minecraft.getInstance();
                    LocalPlayer player = minecraft.player;
                    if (player == null || minecraft.options.hideGui || !player.getAbilities().flying) {
                        return;
                    }
                    ItemStack chest = player.getItemBySlot(EquipmentSlot.CHEST);
                    if (!chest.is(CRRItems.EXO_CHESTPLATE.get())) {
                        return;
                    }
                    int amount = FluidTankHolder.contents(chest).getAmount();
                    int capacity = ((FluidTankHolder) chest.getItem()).tankCapacityMb(chest);
                    int width = 81;
                    int x = graphics.guiWidth() / 2 - 10 - width;
                    int y = graphics.guiHeight() - 49 - 12;
                    int filled = Math.round(width * amount / (float) capacity);
                    // Green when full, red when low.
                    float t = 1.0F - amount / (float) capacity;
                    int red = Math.min(255, Math.round(510 * t));
                    int green = Math.min(255, Math.round(510 * (1 - t)));
                    graphics.fill(x - 1, y - 1, x + width + 1, y + 5, 0xFF101010);
                    graphics.fill(x, y, x + filled, y + 4, 0xFF000000 | (red << 16) | (green << 8) | 0x20);
                    if (amount <= capacity / 10) {
                        String text = "Low fuel";
                        graphics.drawString(minecraft.font, text, x + width / 2 - minecraft.font.width(text) / 2, y - 10, 0xFFFF6060, true);
                    }
                });
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            PonderIndex.addPlugin(new CRRPonderPlugin());
            registerItemDescriptions();
            registerBowProperties();
            if (ModList.get().isLoaded("accessories")) {
                AccessoriesWornRenderer.register();
            } else if (ModList.get().isLoaded("curios")) {
                CuriosWornRenderer.register();
            }
        });
    }

    @SubscribeEvent
    static void onRegisterParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(CRRParticles.TOXIC_HAZE.get(), HazeParticle.Provider::new);
    }

    @SubscribeEvent
    static void onRegisterModels(ModelEvent.RegisterAdditional event) {
        WornModels.registerModels(event);
    }

    // Vanilla only registers these for its own bow.
    private static void registerBowProperties() {
        ItemProperties.register(CRRItems.TITANIUM_BOW.get(), ResourceLocation.withDefaultNamespace("pull"), (stack, level, entity, seed) ->
                entity == null || entity.getUseItem() != stack ? 0 : (stack.getUseDuration(entity) - entity.getUseItemRemainingTicks()) / 20.0F);
        ItemProperties.register(CRRItems.TITANIUM_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (stack, level, entity, seed) ->
                entity != null && entity.isUsingItem() && entity.getUseItem() == stack ? 1 : 0);
    }

    // Create's hold-shift descriptions, for every item with a tooltip.summary lang key.
    private static void registerItemDescriptions() {
        BuiltInRegistries.ITEM.forEach(item -> {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(ReactingReactions.MODID)) {
                TooltipModifier.REGISTRY.register(item, new ItemDescription.Modifier(item, FontHelper.Palette.GREEN));
            }
        });
    }

    private static void onClientTick(ClientTickEvent.Post event) {
        ClientContamination.tick();
        ClientLaserPointers.tick();
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        if (player == null) {
            return;
        }
        // No event fires for a jump pressed mid-air, so the thrusters' double jump is detected here. Only a press while already
        // airborne the tick before counts: the press that jumps off the ground has left it by now too.
        if (minecraft.options.keyJump.consumeClick() && !player.onGround() && airborneLastTick) {
            PacketDistributor.sendToServer(new CRRDoubleJumpPayload());
        }
        airborneLastTick = !player.onGround();
    }
}
