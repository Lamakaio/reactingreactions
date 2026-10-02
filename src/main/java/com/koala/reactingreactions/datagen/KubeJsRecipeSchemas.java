package com.koala.reactingreactions.datagen;

/**
 * KubeJS recipe schemas for our recipe types, giving scripts a fluent builder; inert without KubeJS. Distillation and
 * reaction extend Create's processing schema with heat; electrolysis and the oven are written in full because they take
 * no heat. The component types come from the KubeJS Create addon.
 */
final class KubeJsRecipeSchemas {
    private KubeJsRecipeSchemas() {
    }

    /** The two fields every one of these recipe types shares via Create's own {@code ProcessingRecipeParams}. */
    private static final String INGREDIENTS_AND_RESULTS = """
            {
              "name": "results",
              "role": "output",
              "type": {
                "type": "list",
                "component": {"type": "either", "left": "fluid_stack", "right": "create:processing_output"}
              }
            },
            {
              "name": "ingredients",
              "role": "input",
              "type": {
                "type": "list",
                "component": {"type": "either", "left": "create:sized_fluid_ingredient", "right": "ingredient"},
                "spread": {"type": "either", "left": "ignore", "right": "sized_ingredient"}
              }
            },
            {
              "name": "processing_time",
              "role": "input",
              "type": "ticks",
              "optional": 100,
              "always_write": true
            }
            """;

    static void register(Data data) {
        data.raw("kubejs/recipe_schema", "distillation_recipe", """
                {"parent": "create:base/processing_with_time"}
                """);

        data.raw("kubejs/recipe_schema", "reaction_recipe", """
                {
                  "parent": "create:base/processing_with_time",
                  "keys": [
                    {"name": "min_rpm", "role": "other", "type": "float", "optional": 0.0, "function_names": ["minRpm"]},
                    {"name": "max_rpm", "role": "other", "type": "float", "optional": 0.0, "function_names": ["maxRpm"]}
                  ],
                  "merge": {"keys": true}
                }
                """);

        data.raw("kubejs/recipe_schema", "electrolysis_recipe", ("""
                {
                  "keys": [
                    %s,
                    {"name": "min_voltage", "role": "other", "type": "double", "optional": 0.0, "function_names": ["minVoltage"]},
                    {
                      "name": "electrodes", "role": "other", "optional": [],
                      "type": {"type": "list", "component": "id", "min": 0}
                    }
                  ],
                  "unique": ["results"]
                }
                """).formatted(INGREDIENTS_AND_RESULTS));

        data.raw("kubejs/recipe_schema", "airless_oven_recipe", ("""
                {
                  "keys": [%s],
                  "unique": ["results"]
                }
                """).formatted(INGREDIENTS_AND_RESULTS));
    }
}
