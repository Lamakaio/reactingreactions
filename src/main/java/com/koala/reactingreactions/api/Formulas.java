package com.koala.reactingreactions.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Add, change or hide the chemical formula shown on any item, block or fluid tooltip. Made for KubeJS startup scripts (they run
 * on both the client, where tooltips are drawn, and the server), but any mod can call it too:
 *
 * <pre>
 * // kubejs/startup_scripts/formulas.js
 * const Formulas = Java.loadClass('com.koala.reactingreactions.api.Formulas')
 * Formulas.item('minecraft:iron_ingot', 'Fe')          // an item or block
 * Formulas.fluid('minecraft:water', 'H2O')             // a fluid (its bucket follows)
 * Formulas.tag('c:gems/quartz', 'SiO2')                // every item in an item tag
 * Formulas.hide('minecraft:sugar')                     // no formula line at all
 * </pre>
 *
 * Entries set here win over the mod's own tables. A formula starting with "~" is marked as approximate; the "~" is not shown.
 */
public final class Formulas {
    /** Formula per item or fluid id; an empty string hides the line. */
    private static final Map<String, String> BY_ID = new ConcurrentHashMap<>();
    /** Formula per item tag id. */
    private static final Map<String, String> BY_TAG = new ConcurrentHashMap<>();

    private Formulas() {
    }

    public static void item(String id, String formula) {
        BY_ID.put(id, formula);
    }

    public static void fluid(String id, String formula) {
        BY_ID.put(id, formula);
    }

    public static void tag(String tag, String formula) {
        BY_TAG.put(tag.startsWith("#") ? tag.substring(1) : tag, formula);
    }

    public static void hide(String id) {
        BY_ID.put(id, "");
    }

    /** The scripted formula for an id: null if none was set, "" if it was hidden. */
    public static String byId(String id) {
        return BY_ID.get(id);
    }

    public static Map<String, String> byTag() {
        return BY_TAG;
    }
}
