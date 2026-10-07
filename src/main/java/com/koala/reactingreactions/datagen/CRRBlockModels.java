package com.koala.reactingreactions.datagen;

import com.koala.reactingreactions.content.electrolysis.ElectrodeBlockBase;
import com.koala.reactingreactions.content.induction.InductionHeaterCoil;
import com.koala.reactingreactions.content.induction.InductionHeaterConnectorBlock;
import com.koala.reactingreactions.content.multiblock.MultiblockWindows;
import com.koala.reactingreactions.content.toxic.GasVentBlock;
import com.koala.reactingreactions.content.multiblock.MultiblockWindows.WindowShape;
import com.tterrag.registrate.providers.DataGenContext;
import com.tterrag.registrate.providers.RegistrateBlockstateProvider;
import com.tterrag.registrate.util.nullness.NonNullBiConsumer;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Half;
import net.neoforged.neoforge.client.model.generators.BlockModelBuilder;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelBuilder;
import net.neoforged.neoforge.client.model.generators.ModelFile;

import java.util.ArrayList;
import java.util.List;

/** Blockstate and model generators shared by the block registrations. */
public final class CRRBlockModels {
    private static final ModelFile BLOCK = new ModelFile.UncheckedModelFile(ResourceLocation.withDefaultNamespace("block/block"));

    private CRRBlockModels() {
    }

    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> of(ModelMaker maker) {
        return (ctx, p) -> p.simpleBlock(ctx.get(), maker.make(ctx.getName(), p));
    }

    /** One model in four random turns, whatever the state. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> randomlyTurned(ModelMaker maker) {
        return (ctx, p) -> {
            ModelFile model = maker.make(ctx.getName(), p);
            p.getVariantBuilder(ctx.get()).forAllStates(state -> ConfiguredModel.allYRotations(model, 0, false));
        };
    }

    /** Only the block's own (plain) model: its blockstate, with formed pieces, comes from {@code tools/machine_models.py}. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> modelOnly(ModelMaker maker) {
        return (ctx, p) -> maker.make(ctx.getName(), p);
    }

    public interface ModelMaker {
        ModelFile make(String name, RegistrateBlockstateProvider p);
    }

    private static BlockModelBuilder builder(RegistrateBlockstateProvider p, String name, ResourceLocation particle) {
        return p.models().getBuilder(name).parent(BLOCK).texture("particle", particle);
    }

    /** Side texture on the four sides, end texture on top and bottom. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> column(String side, String end) {
        return (ctx, p) -> p.simpleBlock(ctx.get(), p.models().cubeColumn(ctx.getName(), p.modLoc("block/" + side), p.modLoc("block/" + end)));
    }

    /** A full cube, every face tinted except the top when {@code tintTop} is false. */
    public static ModelMaker tintedCube(ResourceLocation side, ResourceLocation top, ResourceLocation bottom, boolean tintTop) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, side).texture("side", side).texture("top", top).texture("bottom", bottom);
            var cube = model.element().from(0, 0, 0).to(16, 16, 16);
            for (Direction face : Direction.values()) {
                var f = cube.face(face).uvs(0, 0, 16, 16).texture(face == Direction.UP ? "#top" : face == Direction.DOWN ? "#bottom" : "#side").cullface(face);
                if (face != Direction.UP || tintTop) {
                    f.tintindex(0);
                }
                f.end();
            }
            cube.end();
            return model;
        };
    }

    /** A {@link #plate} on the bottom of the block, or flipped onto the top by the {@code half} property (the Floor Drain). */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> halfPlate(ResourceLocation texture, int height) {
        return (ctx, p) -> {
            ModelFile model = plate(texture, height).make(ctx.getName(), p);
            p.getVariantBuilder(ctx.get()).forAllStates(state -> ConfiguredModel.builder().modelFile(model)
                    .rotationX(state.getValue(BlockStateProperties.HALF) == Half.TOP ? 180 : 0).build());
        };
    }

    /** A full-width plate {@code height} pixels tall. */
    public static ModelMaker plate(ResourceLocation texture, int height) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, texture).texture("t", texture);
            var plate = model.element().from(0, 0, 0).to(16, height, 16);
            for (Direction face : Direction.values()) {
                var f = face.getAxis().isVertical() ? plate.face(face).uvs(0, 0, 16, 16) : plate.face(face).uvs(0, 16 - height, 16, 16);
                f.texture("#t");
                if (face != Direction.UP) {
                    f.cullface(face);
                }
                f.end();
            }
            plate.end();
            return model;
        };
    }

    /** A cube two pixels short at the back, with a front face (north), for machines taking a shaft from behind. */
    public static ModelMaker frontBox(ResourceLocation side, ResourceLocation front, ResourceLocation top) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, side).texture("side", side).texture("front", front).texture("top", top);
            var body = model.element().from(0, 0, 0).to(16, 16, 14);
            for (Direction face : Direction.values()) {
                body.face(face).texture(face == Direction.NORTH ? "#front" : face.getAxis() == Direction.Axis.Y ? "#top" : "#side").end();
            }
            body.end();
            return model;
        };
    }

    /** The Atmospheric Scrubber: an inset housing open at the back for the shaft, its grille north, two filter cartridges on top. */
    public static ModelMaker scrubber(ResourceLocation side, ResourceLocation front) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, side).texture("side", side).texture("front", front);
            model.element().from(1, 0, 1).to(15, 14, 14)
                    .allFaces((d, f) -> f.texture(d == Direction.NORTH ? "#front" : "#side").uvs(0, 0, 16, 16)).end();
            for (int x : new int[] {3, 9}) {
                model.element().from(x, 14, 4).to(x + 4, 16, 11).allFaces((d, f) -> f.texture("#side").uvs(4, 4, 12, 12)).end();
            }
            return model;
        };
    }

    /** A drill bit pointing down: a collar, four teeth, then narrowing sections turned alternately by 45 degrees. */
    public static ModelMaker drillBit(ResourceLocation collar, ResourceLocation bit) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, bit).texture("collar", collar).texture("bit", bit);
            int[][] collarBoxes = {{3, 12, 3, 13, 16, 13}, {7, 8, 3, 9, 10, 4}, {7, 8, 12, 9, 10, 13}, {3, 8, 7, 4, 10, 9}, {12, 8, 7, 13, 10, 9}};
            for (int[] b : collarBoxes) {
                box(model, b, "#collar").end();
            }
            int[][] sections = {{4, 8, 12}, {5, 5, 9}, {6, 2, 6}, {7, 0, 3}};
            for (int i = 0; i < sections.length; i++) {
                int inset = sections[i][0];
                var element = box(model, new float[] {inset, sections[i][1], inset, 16 - inset, sections[i][2], 16 - inset}, "#bit");
                if (i % 2 == 1) {
                    turned(element, 8, 8);
                }
                element.end();
            }
            return model;
        };
    }

    /** An oil drill's roller bit: a collar, a shank and four turned cones around a centre jet. */
    public static ModelMaker rollerBit(ResourceLocation collar, ResourceLocation shank, ResourceLocation cone) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, collar).texture("collar", collar).texture("shank", shank).texture("cone", cone);
            box(model, new int[] {3, 12, 3, 13, 16, 13}, "#collar").end();
            box(model, new int[] {5, 5, 5, 11, 12, 11}, "#shank").end();
            for (float cx : new float[] {5.5F, 10.5F}) {
                for (float cz : new float[] {5.5F, 10.5F}) {
                    turned(box(model, new float[] {cx - 1.75F, 2, cz - 1.75F, cx + 1.75F, 6, cz + 1.75F}, "#cone"), cx, cz).end();
                }
            }
            turned(box(model, new int[] {6, 0, 6, 10, 3, 10}, "#shank"), 8, 8).end();
            return model;
        };
    }

    /** The Small Electrolyser: a tank between a base and a lid, with the two wire terminals on top. */
    public static ModelMaker smallElectrolyser(ResourceLocation casing, ResourceLocation panel, ResourceLocation terminal) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, casing).texture("casing", casing).texture("panel", panel).texture("terminal", terminal);
            box(model, new int[] {0, 0, 0, 16, 2, 16}, "#casing").end();
            model.element().from(1, 2, 1).to(15, 13, 15)
                    .allFaces((d, f) -> f.texture(d.getAxis() == Direction.Axis.Y ? "#casing" : "#panel").uvs(1, 2, 15, 13)).end();
            box(model, new int[] {0, 13, 0, 16, 14, 16}, "#casing").end();
            box(model, new int[] {3, 14, 7, 5, 16, 9}, "#terminal").end();
            box(model, new int[] {11, 14, 7, 13, 16, 9}, "#terminal").end();
            return model;
        };
    }

    private static ModelBuilder<BlockModelBuilder>.ElementBuilder box(BlockModelBuilder model, int[] b, String texture) {
        return box(model, new float[] {b[0], b[1], b[2], b[3], b[4], b[5]}, texture);
    }

    private static ModelBuilder<BlockModelBuilder>.ElementBuilder box(BlockModelBuilder model, float[] b, String texture) {
        return model.element().from(b[0], b[1], b[2]).to(b[3], b[4], b[5]).allFaces((d, f) -> f.texture(texture));
    }

    /** Turns an element 45 degrees around a vertical axis through (x, z). */
    private static ModelBuilder<BlockModelBuilder>.ElementBuilder turned(ModelBuilder<BlockModelBuilder>.ElementBuilder element, float x, float z) {
        return element.rotation().angle(45).axis(Direction.Axis.Y).origin(x, 8, z).end();
    }

    /** Boxes given as {x0, y0, z0, x1, y1, z1}, all faces with one texture. */
    public static ModelMaker boxes(ResourceLocation texture, int[][] boxes) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, texture).texture("t", texture);
            for (int[] b : boxes) {
                model.element().from(b[0], b[1], b[2]).to(b[3], b[4], b[5]).allFaces((d, f) -> f.texture("#t")).end();
            }
            return model;
        };
    }

    /**
     * Flat pebbles lying on the floor, each {cx, cz, width, depth, height, y angle, raised}: a slab, with a smaller one on top when
     * raised is 1. Each pebble takes its own patch of the texture.
     */
    public static ModelMaker pebbles(ResourceLocation texture, float[][] pebbles) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, texture).texture("t", texture);
            for (int i = 0; i < pebbles.length; i++) {
                float[] q = pebbles[i];
                float x = q[0], z = q[1], w = q[2], d = q[3], h = q[4];
                List<float[]> slabs = new ArrayList<>();
                slabs.add(new float[] {x - w / 2, 0, z - d / 2, x + w / 2, h, z + d / 2});
                if (q[6] > 0) {
                    slabs.add(new float[] {x - w / 2 + 1, h, z - d / 2 + 1, x + w / 2 - 1.5F, h + 1, z + d / 2 - 0.5F});
                }
                float v = (i * 5) % 12;
                for (float[] b : slabs) {
                    float sx = b[3] - b[0], sy = b[4] - b[1], sz = b[5] - b[2];
                    var element = model.element().from(b[0], b[1], b[2]).to(b[3], b[4], b[5]).allFaces((dir, f) -> {
                        f.texture("#t");
                        switch (dir) {
                            case UP, DOWN -> f.uvs(b[0], b[2], b[3], b[5]);
                            case NORTH, SOUTH -> f.uvs(b[0], v, b[0] + sx, v + sy);
                            default -> f.uvs(b[2], v, b[2] + sz, v + sy);
                        }
                    });
                    if (q[5] != 0) {
                        element.rotation().angle(q[5]).axis(Direction.Axis.Y).origin(x, 0, z).end();
                    }
                    element.end();
                }
            }
            return model;
        };
    }

    /** A rock texture with a tinted fleck overlay. */
    public static ModelMaker richVein(ResourceLocation rock, ResourceLocation flecks) {
        return (name, p) -> {
            BlockModelBuilder model = builder(p, name, rock).texture("all", rock).texture("flecks", flecks);
            var base = model.element().from(0, 0, 0).to(16, 16, 16);
            var overlay = model.element().from(-0.01F, -0.01F, -0.01F).to(16.01F, 16.01F, 16.01F);
            for (Direction face : Direction.values()) {
                base.face(face).texture("#all").cullface(face).end();
                overlay.face(face).texture("#flecks").tintindex(0).end();
            }
            base.end();
            overlay.end();
            return model;
        };
    }

    // ---- multiblock shells, drawn as thin plates like a Create fluid tank ----

    /** A wall whose four sides can turn into windows: middle, rounded-bottom (floor row) and rounded-top (roof row). */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> windowedWall(String side, String end) {
        return (ctx, p) -> {
            itemCube(p, ctx.getName(), side, end);
            var builder = p.getMultipartBuilder(ctx.get());
            for (Direction face : Direction.values()) {
                var outer = MultiblockWindows.OUTER.get(face);
                boolean vertical = face.getAxis() == Direction.Axis.Y;
                String faceTexture = vertical ? end : side;
                builder.part().modelFile(faceModel(p, ctx.getName() + "_face_" + face.getName(), face, faceTexture, side)).addModel()
                        .condition(MultiblockWindows.WINDOW, WindowShape.NONE).condition(outer, true).end();
                if (vertical) {
                    // A windowed wall keeps its top and bottom faces.
                    builder.part().modelFile(faceModel(p, ctx.getName() + "_cap_" + face.getName(), face, faceTexture, side)).addModel()
                            .condition(MultiblockWindows.WINDOW, WindowShape.MIDDLE, WindowShape.BOTTOM, WindowShape.TOP)
                            .condition(outer, true).end();
                    continue;
                }
                for (WindowShape shape : new WindowShape[] {WindowShape.MIDDLE, WindowShape.BOTTOM, WindowShape.TOP}) {
                    builder.part().modelFile(windowFaceModel(p, ctx.getName() + "_window_" + shape.getSerializedName() + "_" + face.getName(), face, side, shape))
                            .addModel().condition(MultiblockWindows.WINDOW, shape).condition(outer, true).end();
                }
            }
        };
    }

    /** A controller in the same thin shell as the walls, never a window. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> shellBlock(String side, String end) {
        return (ctx, p) -> {
            itemCube(p, ctx.getName(), side, end);
            var builder = p.getMultipartBuilder(ctx.get());
            for (Direction face : Direction.values()) {
                String texture = face.getAxis() == Direction.Axis.Y ? end : side;
                builder.part().modelFile(faceModel(p, ctx.getName() + "_face_" + face.getName(), face, texture, side)).addModel()
                        .condition(MultiblockWindows.OUTER.get(face), true).end();
            }
        };
    }

    /**
     * A fixed-size machine's shell block: only its plain, unformed cube ({@code tinted} for the walls). Its blockstate, with the
     * formed pieces, is written by {@code tools/machine_models.py}.
     */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> tieredShell(String side, String end, boolean tinted) {
        return (ctx, p) -> {
            if (!tinted) {
                itemCube(p, ctx.getName(), side, end);
                return;
            }
            builder(p, ctx.getName(), p.modLoc("block/" + side)).texture("side", p.modLoc("block/" + side)).texture("end", p.modLoc("block/" + end))
                    .element().from(0, 0, 0).to(16, 16, 16)
                    .allFaces((face, f) -> f.texture(face.getAxis() == Direction.Axis.Y ? "#end" : "#side").cullface(face).tintindex(0)).end();
        };
    }

    /** The full cube, only used as the block item's model. */
    private static void itemCube(RegistrateBlockstateProvider p, String name, String side, String end) {
        if (side.equals(end)) {
            p.models().cubeAll(name, p.modLoc("block/" + side));
        } else {
            p.models().cubeColumn(name, p.modLoc("block/" + side), p.modLoc("block/" + end));
        }
    }

    /** One outer face of a block as a 1px plate. */
    private static BlockModelBuilder faceModel(RegistrateBlockstateProvider p, String name, Direction face, String texture, String particle) {
        BlockModelBuilder model = builder(p, name, p.modLoc("block/" + particle)).texture("face", p.modLoc("block/" + texture));
        boolean negative = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        float lo = negative ? 0 : 15;
        float hi = negative ? 1 : 16;
        var element = switch (face.getAxis()) {
            case X -> model.element().from(lo, 0, 0).to(hi, 16, 16);
            case Y -> model.element().from(0, lo, 0).to(16, hi, 16);
            case Z -> model.element().from(0, 0, lo).to(16, 16, hi);
        };
        element.face(face).uvs(0, 0, 16, 16).texture("#face").end();
        element.face(face.getOpposite()).uvs(0, 0, 16, 16).texture("#face").end();
        element.end();
        // Ambient occlusion on 1px plates only makes dark patches.
        model.ao(false);
        return model;
    }

    /** The window on one side of a block, using Create's tank window texture. */
    private static BlockModelBuilder windowFaceModel(RegistrateBlockstateProvider p, String name, Direction face, String side, WindowShape shape) {
        BlockModelBuilder model = builder(p, name, p.modLoc("block/" + side))
                .texture("frame", p.modLoc("block/" + side))
                .texture("window", ResourceLocation.fromNamespaceAndPath("create", "block/fluid_tank_window"));
        // Same pane ranges as Create: the bottom window is rounded at y 4, the top one at y 12.
        boolean bottom = shape == WindowShape.BOTTOM;
        boolean top = shape == WindowShape.TOP;
        float paneY0 = bottom ? 4 : 0;
        float paneY1 = top ? 12 : 16;
        float[] uv = bottom ? new float[] {0, 4, 8, 16} : top ? new float[] {0, 0, 8, 12} : new float[] {8, 0, 16, 16};
        boolean alongX = face.getAxis() == Direction.Axis.Z;
        boolean negative = face.getAxisDirection() == Direction.AxisDirection.NEGATIVE;
        float depth0 = negative ? 0 : 15;
        float paneDepth = negative ? 0.95F : 15.05F;
        // Frame: a 4px strip on each side, plus a strip where the window is rounded away.
        for (float[] box : new float[][] {{0, 0, 4, 16}, {12, 0, 16, 16}, {4, 0, 12, paneY0}, {4, paneY1, 12, 16}}) {
            if (box[3] <= box[1] || box[2] <= box[0]) {
                continue;
            }
            ModelBuilder<BlockModelBuilder>.ElementBuilder e = alongX
                    ? model.element().from(box[0], box[1], depth0).to(box[2], box[3], depth0 + 1)
                    : model.element().from(depth0, box[1], box[0]).to(depth0 + 1, box[3], box[2]);
            e.face(face).uvs(box[0], 16 - box[3], box[2], 16 - box[1]).texture("#frame").end();
            e.face(face.getOpposite()).uvs(box[0], 16 - box[3], box[2], 16 - box[1]).texture("#frame").end();
            // Only the side facing the pane: the others sit flush against other plates and would z-fight.
            Direction tangentPlus = alongX ? Direction.EAST : Direction.SOUTH;
            if (box[1] == 0 && box[3] == 16) {
                e.face(box[0] == 0 ? tangentPlus : tangentPlus.getOpposite()).uvs(0, 0, 1, 16).texture("#frame").end();
            } else {
                e.face(box[1] == 0 ? Direction.UP : Direction.DOWN).uvs(0, 0, 8, 1).texture("#frame").end();
            }
            e.end();
        }
        ModelBuilder<BlockModelBuilder>.ElementBuilder pane = alongX
                ? model.element().from(4, paneY0, paneDepth).to(12, paneY1, paneDepth)
                : model.element().from(paneDepth, paneY0, 4).to(paneDepth, paneY1, 12);
        pane.face(face).uvs(uv[0], uv[1], uv[2], uv[3]).texture("#window").tintindex(0).end();
        pane.face(face.getOpposite()).uvs(uv[0], uv[1], uv[2], uv[3]).texture("#window").tintindex(0).end();
        pane.end();
        model.ao(false);
        return model;
    }

    // ---- single machines ----

    /** An electrode rod with a clamp ring at its base, rotated to its {@code facing}. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> electrode(ResourceLocation clamp) {
        return (ctx, p) -> {
            ResourceLocation rod = p.modLoc("block/" + ctx.getName());
            BlockModelBuilder model = builder(p, ctx.getName(), rod).texture("rod", rod).texture("clamp", clamp);
            box(model, new int[] {6, 0, 6, 10, 16, 10}, "#rod").end();
            box(model, new int[] {5, 0, 5, 11, 2, 11}, "#clamp").end();
            p.getVariantBuilder(ctx.get()).forAllStates(state -> facing(model, state.getValue(ElectrodeBlockBase.FACING)));
        };
    }

    /** A Gas Vent: a flange, a pipe and a hood with a grille, the model pointing up and turned to its {@code facing}. */
    /** The Gas Vent's stack (tools/machine_models.py), turned to point where it vents. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> gasVent() {
        return (ctx, p) -> {
            ModelFile model = p.models().getExistingFile(p.modLoc("block/gas_vent_stack"));
            p.getVariantBuilder(ctx.get()).forAllStates(state -> facing(model, state.getValue(GasVentBlock.FACING)));
        };
    }

    /** {@code model}, drawn pointing up, turned to point towards {@code facing}. */
    private static ConfiguredModel[] facing(ModelFile model, Direction facing) {
        ConfiguredModel.Builder<?> builder = ConfiguredModel.builder().modelFile(model);
        switch (facing) {
            case DOWN -> builder.rotationX(180);
            case EAST -> builder.rotationX(90).rotationY(90);
            case NORTH -> builder.rotationX(90);
            case SOUTH -> builder.rotationX(90).rotationY(180);
            case WEST -> builder.rotationX(90).rotationY(270);
            default -> {
            }
        }
        return builder.build();
    }

    /** Create Aeronautics' adjustable burner models: the fire or the soulful one by the {@code variant} property. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> gasDiffuser() {
        return (ctx, p) -> p.getVariantBuilder(ctx.get()).forAllStates(state -> {
            boolean soulful = state.getValues().entrySet().stream()
                    .anyMatch(e -> e.getKey().getName().equals("variant") && e.getValue() instanceof StringRepresentable named
                            && named.getSerializedName().equals("soulful"));
            return ConfiguredModel.builder()
                    .modelFile(new ModelFile.UncheckedModelFile(ResourceLocation.fromNamespaceAndPath("aeronautics",
                            soulful ? "block/adjustable_burner/block_soulful" : "block/adjustable_burner/block_fire")))
                    .build();
        });
    }

    /** An induction plate with coil overlays joining its neighbours, and wire posts on the connector. */
    public static <T extends Block> NonNullBiConsumer<DataGenContext<Block, T>, RegistrateBlockstateProvider> inductionHeater(boolean terminals) {
        return (ctx, p) -> {
            String name = ctx.getName();
            ResourceLocation plate = p.modLoc("block/induction_plate");
            ResourceLocation coilTexture = p.modLoc("block/induction_coil");
            BlockModelBuilder cube = builder(p, name + "_base", plate).texture("plate", plate);
            addPlateCube(cube);
            BlockModelBuilder coil = builder(p, name + "_coil", plate).texture("coil", coilTexture);
            addCoil(coil);
            BlockModelBuilder circle = builder(p, name + "_circle", plate).texture("coil", p.modLoc("block/induction_coil_circle"));
            addCoil(circle);
            BlockModelBuilder posts = null;
            if (terminals) {
                posts = builder(p, name + "_terminals", plate).texture("plate", plate);
                addPosts(posts);
            }
            // The item's model: plate, one coil segment, posts.
            BlockModelBuilder item = builder(p, name, plate).texture("plate", plate).texture("coil", coilTexture);
            addPlateCube(item);
            addCoil(item);
            if (terminals) {
                addPosts(item);
            }

            var multipart = p.getMultipartBuilder(ctx.get());
            multipart.part().modelFile(cube).addModel().end();
            for (Direction side : Direction.Plane.HORIZONTAL) {
                multipart.part().modelFile(coil).rotationY(((int) side.toYRot() + 180) % 360).addModel()
                        .condition(InductionHeaterCoil.of(side), true).end();
            }
            multipart.part().modelFile(circle).addModel().condition(InductionHeaterCoil.CIRCLE, true).end();
            if (terminals) {
                for (Direction facing : Direction.values()) {
                    multipart.part().modelFile(posts)
                            .rotationX(facing == Direction.UP ? 270 : facing == Direction.DOWN ? 90 : 0)
                            .rotationY(facing.getAxis().isVertical() ? 0 : ((int) facing.toYRot() + 180) % 360).addModel()
                            .condition(InductionHeaterConnectorBlock.FACING, facing).end();
                }
            }
        };
    }

    private static void addPlateCube(BlockModelBuilder model) {
        var base = model.element().from(0, 0, 0).to(16, 16, 16);
        for (Direction face : Direction.values()) {
            base.face(face).texture("#plate").cullface(face).end();
        }
        base.end();
    }

    private static void addCoil(BlockModelBuilder model) {
        var overlay = model.element().from(0, 15.99F, 0).to(16, 16.01F, 16);
        overlay.face(Direction.UP).texture("#coil").tintindex(0).end();
        overlay.end();
    }

    private static void addPosts(BlockModelBuilder model) {
        for (int x : new int[] {4, 10}) {
            var post = model.element().from(x, 6, -2).to(x + 2, 10, 0);
            for (Direction face : Direction.values()) {
                post.face(face).texture("#plate").end();
            }
            post.end();
        }
    }
}
