package com.koala.reactingreactions.content.ponder;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.multiblock.FluidCompatibility;
import com.koala.reactingreactions.content.multiblock.MachineTiers;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Writes the Ponder schematics ({@code ./gradlew runExportPonder}): builds each scene of {@link PonderSchematics} in the
 * overworld, lets its machines form, and saves it the way a Structure Block would. Every scene is built with its corner at the
 * world origin, so positions our block entities save (a wall's controller, a controller's shell) are already the positions
 * Ponder places them at. Only registered when the {@code reactingreactions.exportPonder} property names the output folder.
 */
public final class PonderSchematicExporter {
    public static final String PROPERTY = "reactingreactions.exportPonder";
    // Enough for every controller's lazy tick to scan its shell a few times.
    private static final int FORM_TICKS = 60;
    private static final int SETTLE_TICKS = 10;
    // Cleared around the origin before each build: larger than any scene.
    private static final int CLEAR_SIZE = 12;

    /** One scene: its size and what to place. */
    public record Scene(String name, int sizeX, int sizeY, int sizeZ, Consumer<Build> build) {
    }

    /**
     * The blocks of one build, placed in order, plus what to do once its machines have formed. A {@link #shifted} view writes into
     * the same build with its coordinates moved, so a layout written around the origin can be placed anywhere.
     */
    public static final class Build {
        final Map<BlockPos, BlockState> blocks;
        final List<Consumer<ServerLevel>> afterForming;
        private final BlockPos offset;

        public Build() {
            this(new LinkedHashMap<>(), new ArrayList<>(), BlockPos.ZERO);
        }

        private Build(Map<BlockPos, BlockState> blocks, List<Consumer<ServerLevel>> afterForming, BlockPos offset) {
            this.blocks = blocks;
            this.afterForming = afterForming;
            this.offset = offset;
        }

        public Build shifted(int dx, int dy, int dz) {
            return new Build(blocks, afterForming, offset.offset(dx, dy, dz));
        }

        /** The world position of a position in this view. */
        public BlockPos at(int x, int y, int z) {
            return offset.offset(x, y, z);
        }

        /** Runs once the machines have formed, with the world position of (x, y, z). */
        public Build after(int x, int y, int z, BiConsumer<ServerLevel, BlockPos> step) {
            BlockPos pos = at(x, y, z);
            afterForming.add(level -> step.accept(level, pos));
            return this;
        }

        /** {@code properties} as {@code "name=value"}. */
        public Build set(int x, int y, int z, String block, String... properties) {
            BlockPos pos = at(x, y, z);
            blocks.remove(pos);
            blocks.put(pos, state(block, properties));
            return this;
        }

        public Build fill(int x1, int y1, int z1, int x2, int y2, int z2, String block, String... properties) {
            for (BlockPos pos : BlockPos.betweenClosed(x1, y1, z1, x2, y2, z2)) {
                set(pos.getX(), pos.getY(), pos.getZ(), block, properties);
            }
            return this;
        }

        /** Create's Ponder floor: a white concrete and snow checkerboard on layer 0. */
        public Build floor(int size) {
            for (int x = 0; x < size; x++) {
                for (int z = 0; z < size; z++) {
                    set(x, 0, z, (x + z) % 2 == 0 ? "minecraft:white_concrete" : "minecraft:snow_block");
                }
            }
            return this;
        }

        /** Pours fluid into whatever tank sits at the position, once formed. */
        public Build fillTank(int x, int y, int z, String fluid, int amount) {
            BlockPos pos = at(x, y, z);
            afterForming.add(level -> {
                IFluidHandler tank = level.getCapability(Capabilities.FluidHandler.BLOCK, pos, null);
                var stack = new FluidStack(BuiltInRegistries.FLUID.get(ResourceLocation.parse(fluid)), amount);
                if (tank == null || tank.fill(stack, IFluidHandler.FluidAction.EXECUTE) == 0) {
                    throw new IllegalStateException("could not fill " + fluid + " at " + pos.toShortString());
                }
            });
            return this;
        }

        /** Puts items into whatever inventory sits at the position (a depot, a machine), once formed. */
        public Build insert(int x, int y, int z, String item, int count) {
            BlockPos pos = at(x, y, z);
            afterForming.add(level -> {
                var handler = level.getCapability(Capabilities.ItemHandler.BLOCK, pos, null);
                ItemStack left = new ItemStack(BuiltInRegistries.ITEM.get(ResourceLocation.parse(item)), count);
                for (int slot = 0; handler != null && slot < handler.getSlots() && !left.isEmpty(); slot++) {
                    left = handler.insertItem(slot, left, false);
                }
                if (handler == null || !left.isEmpty()) {
                    throw new IllegalStateException("could not insert " + item + " at " + pos.toShortString());
                }
            });
            return this;
        }

        private static BlockState state(String block, String... properties) {
            ResourceLocation id = ResourceLocation.parse(block);
            if (!BuiltInRegistries.BLOCK.containsKey(id)) {
                throw new IllegalArgumentException("unknown block " + block);
            }
            BlockState state = BuiltInRegistries.BLOCK.get(id).defaultBlockState();
            for (String property : properties) {
                String[] parts = property.split("=", 2);
                state = with(state, parts[0], parts[1]);
            }
            return state;
        }

        private static <T extends Comparable<T>> BlockState with(BlockState state, String name, String value) {
            @SuppressWarnings("unchecked")
            Property<T> property = (Property<T>) state.getBlock().getStateDefinition().getProperty(name);
            if (property == null) {
                throw new IllegalArgumentException(state.getBlock() + " has no property " + name);
            }
            T parsed = property.getValue(value).orElseThrow(() -> new IllegalArgumentException(name + " cannot be " + value));
            return state.setValue(property, parsed);
        }
    }

    private static Path output;
    private static List<Scene> queue;
    private static int index;
    private static int ticks;
    private static final List<String> failed = new ArrayList<>();

    private PonderSchematicExporter() {
    }

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        output = Path.of(System.getProperty(PROPERTY));
        FluidCompatibility.bypass = true;
        queue = PonderSchematics.all();
        index = 0;
        ticks = -1;
        ServerLevel level = event.getServer().overworld();
        for (int cx = -1; cx <= 1; cx++) {
            for (int cz = -1; cz <= 1; cz++) {
                level.setChunkForced(cx, cz, true);
            }
        }
    }

    @SubscribeEvent
    public static void onServerTick(ServerTickEvent.Post event) {
        if (queue == null) {
            return;
        }
        MinecraftServer server = event.getServer();
        ServerLevel level = server.overworld();
        if (index >= queue.size()) {
            ReactingReactions.LOGGER.info("Exported {} Ponder schematics to {}", queue.size() - failed.size(), output);
            if (!failed.isEmpty()) {
                ReactingReactions.LOGGER.error("Ponder schematics that failed: {}", failed);
            }
            queue = null;
            server.halt(false);
            return;
        }
        Scene scene = queue.get(index);
        ticks++;
        try {
            if (ticks == 0) {
                build(level, scene);
            } else if (ticks == FORM_TICKS) {
                Build build = new Build();
                scene.build().accept(build);
                build.afterForming.forEach(step -> step.accept(level));
            } else if (ticks == FORM_TICKS + SETTLE_TICKS) {
                save(level, scene);
                index++;
                ticks = -1;
            }
        } catch (Exception e) {
            // On to the next scene, so one failure does not leave every later schematic stale.
            ReactingReactions.LOGGER.error("Ponder schematic {} failed", scene.name(), e);
            failed.add(scene.name());
            index++;
            ticks = -1;
        }
    }

    private static void build(ServerLevel level, Scene scene) {
        // Barriers around the cleared box keep terrain, water and mobs out; they are outside every scene.
        BlockPos.betweenClosed(-1, -1, -1, CLEAR_SIZE, CLEAR_SIZE, CLEAR_SIZE).forEach(pos -> {
            boolean edge = pos.getX() == -1 || pos.getY() == -1 || pos.getZ() == -1
                    || pos.getX() == CLEAR_SIZE || pos.getY() == CLEAR_SIZE || pos.getZ() == CLEAR_SIZE;
            level.setBlock(pos, edge ? Blocks.BARRIER.defaultBlockState() : Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        });
        Build build = new Build();
        scene.build().accept(build);
        place(level, build);
    }

    /** Places a build's blocks, then lets the ones placed before their neighbours (pipes, connected parts) take their final shape. */
    static void place(ServerLevel level, Build build) {
        build.blocks.forEach((pos, state) -> level.setBlock(pos, state, Block.UPDATE_ALL));
        for (BlockPos pos : build.blocks.keySet()) {
            BlockState state = level.getBlockState(pos);
            BlockState shaped = Block.updateFromNeighbourShapes(state, level, pos);
            if (shaped != state) {
                level.setBlock(pos, shaped, Block.UPDATE_ALL);
            }
        }
    }

    private static void save(ServerLevel level, Scene scene) throws IOException {
        // Saved as plain blocks: a scene builds a machine block by block and assembles it once complete (Story.assemble).
        for (BlockPos pos : BlockPos.betweenClosed(BlockPos.ZERO, new BlockPos(scene.sizeX() - 1, scene.sizeY() - 1, scene.sizeZ() - 1))) {
            BlockState state = level.getBlockState(pos);
            if (state.hasProperty(MachineTiers.PART) && state.getValue(MachineTiers.PART) != 0) {
                level.setBlock(pos, state.setValue(MachineTiers.PART, 0), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            }
        }
        StructureTemplate template = new StructureTemplate();
        template.fillFromWorld(level, BlockPos.ZERO, new Vec3i(scene.sizeX(), scene.sizeY(), scene.sizeZ()), false, Blocks.STRUCTURE_VOID);
        CompoundTag tag = template.save(new CompoundTag());
        Files.createDirectories(output);
        NbtIo.writeCompressed(tag, output.resolve(scene.name() + ".nbt"));
        ReactingReactions.LOGGER.info("Ponder schematic {} saved", scene.name());
    }
}
