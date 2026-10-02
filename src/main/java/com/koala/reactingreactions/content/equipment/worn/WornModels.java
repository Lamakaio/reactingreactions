package com.koala.reactingreactions.content.equipment.worn;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.registry.CRRItems;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tterrag.registrate.util.entry.ItemEntry;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.Map;

/**
 * Draws worn equipment as the 3D models of {@code CRRWornModels} ({@code models/worn/<item>.json}) on a humanoid's posed body,
 * for whichever slot mod's renderer calls it.
 */
public final class WornModels {
    /** Where a model sits on the body. */
    private enum Attach {
        FACE, BACK, FEET, RIGHT_ANKLE, HANDS, RIGHT_HAND, NECK, HIP
    }

    private static final Map<ItemEntry<?>, Attach> ATTACH = Map.ofEntries(
            Map.entry(CRRItems.GAS_MASK, Attach.FACE), Map.entry(CRRItems.OXYGEN_MASK, Attach.FACE),
            Map.entry(CRRItems.AEROZINE_THRUSTERS, Attach.BACK), Map.entry(CRRItems.FLAME_RETARDANT_CLOAK, Attach.BACK),
            Map.entry(CRRItems.SPRING_BOOTS, Attach.FEET), Map.entry(CRRItems.DIVING_FINS, Attach.FEET), Map.entry(CRRItems.CHEMICAL_BOOTS, Attach.FEET),
            Map.entry(CRRItems.RACING_ANKLET, Attach.RIGHT_ANKLE), Map.entry(CRRItems.CHEMICAL_GLOVES, Attach.HANDS),
            Map.entry(CRRItems.DIGGING_RING, Attach.RIGHT_HAND), Map.entry(CRRItems.MAGNESIUM_KNUCKLE, Attach.RIGHT_HAND),
            Map.entry(CRRItems.HELIUM_LOCKET, Attach.NECK), Map.entry(CRRItems.ANCHOR_CHARM, Attach.HIP));
    private static final Direction[] FACES_AND_NULL = {Direction.DOWN, Direction.UP, Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST, null};

    private WornModels() {
    }

    public static Iterable<ItemEntry<?>> items() {
        return ATTACH.keySet();
    }

    private static ModelResourceLocation location(Item item) {
        return ModelResourceLocation.standalone(ReactingReactions.asResource("worn/" + BuiltInRegistries.ITEM.getKey(item).getPath()));
    }

    public static void registerModels(ModelEvent.RegisterAdditional event) {
        ATTACH.keySet().forEach(entry -> event.register(location(entry.get())));
    }

    public static void render(ItemStack stack, HumanoidModel<?> body, PoseStack ms, MultiBufferSource buffer, int light) {
        Attach attach = ATTACH.entrySet().stream().filter(e -> stack.is(e.getKey().get())).map(Map.Entry::getValue).findFirst().orElse(null);
        if (attach == null) {
            return;
        }
        BakedModel model = Minecraft.getInstance().getModelManager().getModel(location(stack.getItem()));
        VertexConsumer consumer = buffer.getBuffer(Sheets.cutoutBlockSheet());
        // Anchors in the part's own pixels (y down, front at -z), then in the JSON model's (y up, front at +z).
        switch (attach) {
            case FACE -> draw(ms, consumer, model, light, body.head, 0, -4, -4, 8, 8, 0);
            case BACK -> draw(ms, consumer, model, light, body.body, 0, 6, 2, 8, 8, 0);
            case FEET -> {
                draw(ms, consumer, model, light, body.rightLeg, 0, 12, 0, 8, 0, 8);
                draw(ms, consumer, model, light, body.leftLeg, 0, 12, 0, 8, 0, 8);
            }
            case RIGHT_ANKLE -> draw(ms, consumer, model, light, body.rightLeg, 0, 12, 0, 8, 0, 8);
            // The arms' cubes are off-centre from their pivots: the hands sit 1px out from it.
            case HANDS -> {
                draw(ms, consumer, model, light, body.rightArm, -1, 10, 0, 8, 0, 8);
                draw(ms, consumer, model, light, body.leftArm, 1, 10, 0, 8, 0, 8);
            }
            case RIGHT_HAND -> draw(ms, consumer, model, light, body.rightArm, -1, 10, 0, 8, 0, 8);
            case NECK -> draw(ms, consumer, model, light, body.body, 0, 0, -2, 8, 16, 0);
            case HIP -> draw(ms, consumer, model, light, body.body, 0, 12, -2, 8, 0, 0);
        }
    }

    private static void draw(PoseStack ms, VertexConsumer consumer, BakedModel model, int light, ModelPart part,
                             float partX, float partY, float partZ, float modelX, float modelY, float modelZ) {
        ms.pushPose();
        part.translateAndRotate(ms);
        ms.translate(partX / 16, partY / 16, partZ / 16);
        ms.mulPose(Axis.XP.rotationDegrees(180));
        ms.translate(-modelX / 16, -modelY / 16, -modelZ / 16);
        RandomSource random = RandomSource.create(42);
        for (Direction side : FACES_AND_NULL) {
            for (var quad : model.getQuads(null, side, random, ModelData.EMPTY, null)) {
                consumer.putBulkData(ms.last(), quad, 1, 1, 1, 1, light, OverlayTexture.NO_OVERLAY);
            }
        }
        ms.popPose();
    }
}
