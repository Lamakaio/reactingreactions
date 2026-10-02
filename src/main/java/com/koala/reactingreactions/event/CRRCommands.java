package com.koala.reactingreactions.event;

import com.koala.reactingreactions.content.toxic.Toxicity;
import com.koala.reactingreactions.registry.CRRBlocks;
import com.mojang.brigadier.arguments.FloatArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.tterrag.registrate.util.entry.BlockEntry;

import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.RegisterCommandsEvent;

import java.util.LinkedHashMap;
import java.util.Map;

/** {@code /richvein <type> [radius]}: finds the nearest Rich Vein of a type among the chunks already generated around the player. */
public class CRRCommands {
    private static final int DEFAULT_RADIUS_CHUNKS = 12;
    private static final int MAX_RADIUS_CHUNKS = 32;

    private static Map<String, BlockEntry<?>> veins() {
        Map<String, BlockEntry<?>> veins = new LinkedHashMap<>();
        veins.put("oil", CRRBlocks.RICH_OIL_VEIN);
        veins.put("asurine", CRRBlocks.RICH_ASURINE_VEIN);
        veins.put("crimsite", CRRBlocks.RICH_CRIMSITE_VEIN);
        veins.put("ochrum", CRRBlocks.RICH_OCHRUM_VEIN);
        veins.put("veridium", CRRBlocks.RICH_VERIDIUM_VEIN);
        veins.put("scoria", CRRBlocks.RICH_SCORIA_VEIN);
        veins.put("tuff", CRRBlocks.RICH_TUFF_VEIN);
        veins.put("granite", CRRBlocks.RICH_GRANITE_VEIN);
        veins.put("diorite", CRRBlocks.RICH_DIORITE_VEIN);
        return veins;
    }

    @SubscribeEvent
    public static void onRegisterCommands(RegisterCommandsEvent event) {
        LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal("richvein")
                .requires(source -> source.hasPermission(2))
                .then(Commands.argument("type", StringArgumentType.word())
                        .suggests((context, builder) -> SharedSuggestionProvider.suggest(veins().keySet(), builder))
                        .executes(context -> find(context.getSource(), StringArgumentType.getString(context, "type"), DEFAULT_RADIUS_CHUNKS))
                        .then(Commands.argument("radius", IntegerArgumentType.integer(1, MAX_RADIUS_CHUNKS))
                                .executes(context -> find(context.getSource(), StringArgumentType.getString(context, "type"),
                                        IntegerArgumentType.getInteger(context, "radius")))));
        event.getDispatcher().register(command);
        event.getDispatcher().register(Commands.literal("toxicity")
                .requires(source -> source.hasPermission(2))
                .then(Commands.literal("get")
                        .executes(context -> showGauge(context.getSource(), context.getSource().getPlayerOrException()))
                        .then(Commands.argument("player", EntityArgument.player())
                                .executes(context -> showGauge(context.getSource(), EntityArgument.getPlayer(context, "player")))))
                .then(Commands.literal("set")
                        .then(Commands.argument("value", FloatArgumentType.floatArg(0, 100))
                                .executes(context -> setGauge(context.getSource(), context.getSource().getPlayerOrException(),
                                        FloatArgumentType.getFloat(context, "value")))
                                .then(Commands.argument("player", EntityArgument.player())
                                        .executes(context -> setGauge(context.getSource(), EntityArgument.getPlayer(context, "player"),
                                                FloatArgumentType.getFloat(context, "value")))))));
    }

    private static int showGauge(CommandSourceStack source, ServerPlayer player) {
        float value = Toxicity.gauge(player);
        source.sendSuccess(() -> Component.literal(player.getName().getString() + "'s toxicity gauge: " + String.format("%.1f", value) + " / 100"), false);
        return 1;
    }

    private static int setGauge(CommandSourceStack source, ServerPlayer player, float value) {
        Toxicity.setGauge(player, value);
        source.sendSuccess(() -> Component.literal("Set " + player.getName().getString() + "'s toxicity gauge to " + String.format("%.1f", value)), true);
        return 1;
    }

    private static int find(CommandSourceStack source, String type, int radius) {
        BlockEntry<?> entry = veins().get(type);
        if (entry == null) {
            source.sendFailure(Component.literal("Unknown vein '" + type + "'. Choose one of: " + String.join(", ", veins().keySet())));
            return 0;
        }
        Block block = entry.get();
        ServerLevel level = source.getLevel();
        BlockPos origin = BlockPos.containing(source.getPosition());
        ChunkPos centre = new ChunkPos(origin);
        BlockPos nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                // Never generates anything: chunks that do not exist yet are skipped.
                ChunkAccess chunk = level.getChunk(centre.x + dx, centre.z + dz, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                LevelChunkSection[] sections = chunk.getSections();
                for (int index = 0; index < sections.length; index++) {
                    LevelChunkSection section = sections[index];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.is(block))) {
                        continue;
                    }
                    int baseY = chunk.getSectionYFromSectionIndex(index) << 4;
                    for (int x = 0; x < 16; x++) {
                        for (int y = 0; y < 16; y++) {
                            for (int z = 0; z < 16; z++) {
                                if (section.getBlockState(x, y, z).is(block)) {
                                    BlockPos found = new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                                    double distance = found.distSqr(origin);
                                    if (distance < nearestDistance) {
                                        nearestDistance = distance;
                                        nearest = found;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (nearest == null) {
            source.sendFailure(Component.literal("No " + entry.get().getName().getString() + " within " + radius + " chunks (only chunks that are already generated are searched)."));
            return 0;
        }
        BlockPos pos = nearest;
        String coordinates = pos.getX() + " " + pos.getY() + " " + pos.getZ();
        Component message = Component.literal(entry.get().getName().getString() + " at ")
                .append(Component.literal("[" + coordinates + "]").withStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)
                        .withClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/tp @s " + coordinates))))
                .append(Component.literal(String.format(" (%.0f blocks away)", Math.sqrt(nearestDistance))));
        source.sendSuccess(() -> message, false);
        return 1;
    }
}
