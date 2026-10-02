package com.koala.reactingreactions.content.fluids;

import com.koala.reactingreactions.ReactingReactions;
import com.simibubi.create.content.fluids.PipeAttachmentModel;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Create's pipe model (with its rims, drains and arms), every quad on Create's copper pipe sprites moved onto this mod's plastic
 * ones: Create's textures are only referenced, never copied.
 */
public class PlasticPipeModel extends BakedModelWrapper<BakedModel> {
    private static final Map<ResourceLocation, ResourceLocation> SPRITES = Map.of(
            ResourceLocation.fromNamespaceAndPath("create", "block/pipes"), ReactingReactions.asResource("block/plastic_pipes"),
            ResourceLocation.fromNamespaceAndPath("create", "block/pipes_connected"), ReactingReactions.asResource("block/plastic_pipes_connected"));
    // The block format: 8 ints per vertex, the texture coordinates at 4 and 5.
    private static final int STRIDE = 8;
    private static final int UV = 4;

    private final Map<BakedQuad, BakedQuad> retextured = new ConcurrentHashMap<>();

    public PlasticPipeModel(BakedModel original) {
        super(original);
    }

    /** Create's pipe attachments (rims, drains, arms) around the block's own model, retextured. A method reference, client only. */
    public static BakedModel wrap(BakedModel original) {
        return new PlasticPipeModel(PipeAttachmentModel.withAO(original));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        return retexture(super.getQuads(state, side, rand, data, renderType));
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return retexture(super.getQuads(state, side, rand));
    }

    @Override
    public TextureAtlasSprite getParticleIcon(ModelData data) {
        return sprite(ReactingReactions.asResource("block/plastic_pipes"));
    }

    private List<BakedQuad> retexture(List<BakedQuad> quads) {
        List<BakedQuad> out = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            out.add(retextured.computeIfAbsent(quad, PlasticPipeModel::moved));
        }
        return out;
    }

    private static BakedQuad moved(BakedQuad quad) {
        ResourceLocation target = SPRITES.get(quad.getSprite().contents().name());
        if (target == null) {
            return quad;
        }
        TextureAtlasSprite from = quad.getSprite();
        TextureAtlasSprite to = sprite(target);
        int[] vertices = quad.getVertices().clone();
        for (int i = 0; i + STRIDE <= vertices.length; i += STRIDE) {
            float u = Float.intBitsToFloat(vertices[i + UV]);
            float v = Float.intBitsToFloat(vertices[i + UV + 1]);
            vertices[i + UV] = Float.floatToRawIntBits(to.getU(from.getUOffset(u)));
            vertices[i + UV + 1] = Float.floatToRawIntBits(to.getV(from.getVOffset(v)));
        }
        return new BakedQuad(vertices, quad.getTintIndex(), quad.getDirection(), to, quad.isShade(), quad.hasAmbientOcclusion());
    }

    private static TextureAtlasSprite sprite(ResourceLocation id) {
        return Minecraft.getInstance().getModelManager().getAtlas(InventoryMenu.BLOCK_ATLAS).getSprite(id);
    }
}
