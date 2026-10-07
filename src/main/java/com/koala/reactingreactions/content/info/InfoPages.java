package com.koala.reactingreactions.content.info;

import com.koala.reactingreactions.ReactingReactions;
import com.koala.reactingreactions.content.electrolysis.SmallElectrolyserBlockEntity;
import com.koala.reactingreactions.content.electrolysis.recipe.ElectrolysisRecipe;
import com.koala.reactingreactions.content.reaction.FermentationBarrelBlockEntity;
import com.koala.reactingreactions.content.reaction.SmallReactionChamberBlockEntity;
import com.koala.reactingreactions.content.reaction.recipe.ReactionRecipe;
import com.koala.reactingreactions.registry.CRRRecipeTypes;

import net.minecraft.client.Minecraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Information pages: only for Rich Veins (how they spawn and what they do), the multiblocks (how they are built; see
 * {@link InfoPageTexts}) and the single-block machines (what they can and cannot make, read from the live recipes).
 * {@link #build()} produces the pages as plain data, which the JEI and EMI plugins each attach their own way. Nothing here may
 * touch JEI or EMI: either can be installed without the other.
 */
public final class InfoPages {
    /** One information page: the item(s) it's attached to, and its text as a list of lines (an empty {@link Component} is a blank line). */
    public record Page(List<ItemStack> items, List<Component> lines) {
    }

    private InfoPages() {
    }

    public static List<Page> build() {
        List<Page> pages = new ArrayList<>();
        for (InfoPageTexts.Page page : InfoPageTexts.PAGES) {
            List<Component> lines = new ArrayList<>();
            for (int i = 0; i < page.lines().size(); i++) {
                if (i > 0) {
                    lines.add(Component.empty());
                }
                lines.add(Component.translatable(page.langKey(i)));
            }
            pages.add(new Page(items(page.items()), lines));
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            return pages;
        }
        var recipes = level.getRecipeManager();

        Set<String> electrolyserCan = new LinkedHashSet<>();
        Set<String> electrolyserCannot = new LinkedHashSet<>();
        for (RecipeHolder<ElectrolysisRecipe> holder : recipes.getAllRecipesFor(CRRRecipeTypes.ELECTROLYSIS.get())) {
            (SmallElectrolyserBlockEntity.canRun(holder.value()) ? electrolyserCan : electrolyserCannot).addAll(outputs(holder.value().getFluidResults(), holder.value().getRollableResults().stream().map(o -> o.getStack()).toList()));
        }
        pages.add(new Page(items(List.of("small_electrolyser")), machinePage("small_electrolyser", electrolyserCan, electrolyserCannot)));

        Set<String> barrelCan = new LinkedHashSet<>();
        Set<String> barrelCannot = new LinkedHashSet<>();
        for (RecipeHolder<ReactionRecipe> holder : recipes.getAllRecipesFor(CRRRecipeTypes.REACTION.get())) {
            (FermentationBarrelBlockEntity.canRun(holder.value()) ? barrelCan : barrelCannot).addAll(outputs(holder.value().getFluidResults(), holder.value().getRollableResults().stream().map(o -> o.getStack()).toList()));
        }
        pages.add(new Page(items(List.of("fermentation_barrel")), machinePage("fermentation_barrel", barrelCan, barrelCannot)));

        Set<String> chamberCan = new LinkedHashSet<>();
        Set<String> chamberCannot = new LinkedHashSet<>();
        for (RecipeHolder<ReactionRecipe> holder : recipes.getAllRecipesFor(CRRRecipeTypes.REACTION.get())) {
            (SmallReactionChamberBlockEntity.canRun(holder.value()) ? chamberCan : chamberCannot).addAll(outputs(holder.value().getFluidResults(), holder.value().getRollableResults().stream().map(o -> o.getStack()).toList()));
        }
        pages.add(new Page(items(List.of("small_reaction_chamber")), machinePage("small_reaction_chamber", chamberCan, chamberCannot)));
        return pages;
    }

    private static List<Component> machinePage(String machine, Set<String> can, Set<String> cannot) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("reactingreactions.jei.page." + machine + ".intro"));
        lines.add(Component.empty());
        lines.add(Component.translatable("reactingreactions.jei.page.can", String.join(", ", can)));
        cannot.removeAll(can);
        if (!cannot.isEmpty()) {
            lines.add(Component.empty());
            lines.add(Component.translatable("reactingreactions.jei.page." + machine + ".cannot", String.join(", ", cannot)));
        }
        return lines;
    }

    private static Set<String> outputs(List<FluidStack> fluids, List<ItemStack> items) {
        Set<String> names = new LinkedHashSet<>();
        for (FluidStack fluid : fluids) {
            names.add(fluid.getHoverName().getString());
        }
        for (ItemStack item : items) {
            names.add(item.getHoverName().getString());
        }
        return names;
    }

    private static List<ItemStack> items(List<String> ids) {
        List<ItemStack> stacks = new ArrayList<>();
        for (String id : ids) {
            Item item = BuiltInRegistries.ITEM.get(ReactingReactions.asResource(id));
            if (item != Items.AIR) {
                stacks.add(new ItemStack(item));
            }
        }
        return stacks;
    }
}
