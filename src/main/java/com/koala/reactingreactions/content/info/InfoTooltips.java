package com.koala.reactingreactions.content.info;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

import java.util.List;

/** Adds the chemical formula to item tooltips: this mod's items, and known compounds from vanilla and other mods (see {@link CompoundInfo}). */
public class InfoTooltips {
    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        CompoundInfo.Entry entry = CompoundInfo.findForItem(event.getItemStack());
        if (entry == null || !entry.hasFormula()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        tooltip.add(Math.min(1, tooltip.size()), entry.formulaLine().copy().withStyle(ChatFormatting.DARK_AQUA));
    }
}
