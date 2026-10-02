package com.koala.reactingreactions.datagen;

import java.util.ArrayList;
import java.util.List;

import static com.koala.reactingreactions.datagen.Data.*;

/**
 * Recipe families that repeat across the 16 dye colours (or a set of wood types): vanilla dyeing recipes changed to
 * accept this mod's paints through the {@code c:dyes/*} tags, bleach turning any coloured block white again, and
 * varnish restoring stripped logs.
 */
final class FamilyRecipes {
    private static final String[] COLORS = {"white", "orange", "magenta", "light_blue", "yellow", "lime", "pink", "gray",
            "light_gray", "cyan", "purple", "blue", "brown", "green", "red", "black"};
    private static final String[] WOODS = {"acacia", "birch", "cherry", "dark_oak", "jungle", "mangrove", "oak", "spruce"};

    private FamilyRecipes() {
    }

    static void register(Data data) {
        dyeing(data.ns("minecraft"));
        whitening(data);
        barkRestore(data);
    }

    /** Vanilla's dyeing recipes, re-registered under vanilla's own ids so they replace the originals and take any {@code c:dyes/<colour>}. */
    private static void dyeing(Data d) {
        for (String c : COLORS) {
            String dye = "#c:dyes/" + c;
            d.shapeless(c + "_concrete_powder", "building", res("minecraft:" + c + "_concrete_powder", 8),
                    dye, x(4, "minecraft:sand"), x(4, "minecraft:gravel")).group("concrete_powder");
            d.shaped(c + "_stained_glass", "building", res("minecraft:" + c + "_stained_glass", 8), "###", "#X#", "###")
                    .key('#', "minecraft:glass").key('X', dye).group("stained_glass");
            d.shaped(c + "_stained_glass_pane_from_glass_pane", "misc", res("minecraft:" + c + "_stained_glass_pane", 8), "###", "#$#", "###")
                    .key('#', "minecraft:glass_pane").key('$', dye).group("stained_glass_pane");
            d.shaped(c + "_terracotta", "building", res("minecraft:" + c + "_terracotta", 8), "###", "#X#", "###")
                    .key('#', "minecraft:terracotta").key('X', dye).group("stained_terracotta");
            for (String kind : new String[] {"bed", "carpet", "wool"}) {
                d.shapeless("dye_" + c + "_" + kind, "building", res("minecraft:" + c + "_" + kind, 1),
                        dye, otherColours(c, kind)).group(kind);
            }
        }
    }

    /** Every other colour of the block, alphabetically, with white last (white is the "undyed" one). */
    private static Object otherColours(String self, String kind) {
        List<String> names = new ArrayList<>(List.of("black", "blue", "brown", "cyan", "gray", "green", "light_blue", "light_gray", "lime",
                "magenta", "orange", "pink", "purple", "red", "yellow"));
        names.remove(self);
        if (!self.equals("white")) {
            names.add("white");
        }
        Object[] items = names.stream().map(n -> "minecraft:" + n + "_" + kind).toArray();
        return any(items);
    }

    /** A bottle of bleach turns any coloured block back to its white version. */
    private static void whitening(Data d) {
        for (String c : COLORS) {
            if (c.equals("white")) {
                continue;
            }
            for (String kind : new String[] {"carpet", "concrete_powder", "concrete", "stained_glass_pane", "stained_glass", "terracotta", "wool"}) {
                d.shapeless("crafting/whitening/" + c + "_" + kind + "_to_white", "misc", res("minecraft:white_" + kind, 1),
                        "reactingreactions:bleach_bottle", "minecraft:" + c + "_" + kind);
            }
        }
    }

    /** Varnish puts the bark back on a stripped log. */
    private static void barkRestore(Data d) {
        for (String wood : WOODS) {
            d.shapeless("crafting/bark_restore/" + wood + "_bark_restore", "building", res("minecraft:" + wood + "_log", 1),
                    "reactingreactions:varnish", "minecraft:stripped_" + wood + "_log");
        }
    }
}
