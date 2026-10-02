package com.koala.reactingreactions.datagen;

import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

/** Writes everything collected by the generator classes in this package as data-pack JSON. */
public class CRRDataProvider implements DataProvider {
    private final PackOutput output;

    public CRRDataProvider(PackOutput output) {
        this.output = output;
    }

    @Override
    public CompletableFuture<?> run(CachedOutput cache) {
        Data data = new Data();
        CRRCraftingRecipes.register(data);
        ChaseGearRecipes.register(data);
        CRRProcessingRecipes.register(data);
        CRRMachineRecipes.register(data);
        CRRReactionRecipes.register(data);
        CompatRecipes.register(data);
        FamilyRecipes.register(data);
        MiscData.register(data);
        KubeJsRecipeSchemas.register(data);

        Path root = output.getOutputFolder(PackOutput.Target.DATA_PACK);
        List<CompletableFuture<?>> writes = new ArrayList<>();
        for (Data.Entry entry : data.entries()) {
            Path path = root.resolve(entry.namespace()).resolve(entry.dir()).resolve(entry.path() + ".json");
            writes.add(DataProvider.saveStable(cache, entry.json(), path));
        }
        return CompletableFuture.allOf(writes.toArray(new CompletableFuture<?>[0]));
    }

    @Override
    public String getName() {
        return "Create: Reacting Reactions data";
    }
}
