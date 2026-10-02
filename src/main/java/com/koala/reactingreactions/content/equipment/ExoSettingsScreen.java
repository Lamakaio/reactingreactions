package com.koala.reactingreactions.content.equipment;

import com.koala.reactingreactions.item.ExoSettings;
import com.koala.reactingreactions.network.CRRExoSettingPayload;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

/** Every chase item worn or carried, one row each, with a button per setting. */
public class ExoSettingsScreen extends Screen {
    private static final int ROW_HEIGHT = 24;
    private static final int BUTTON_WIDTH = 110;
    private static final int NAME_WIDTH = 150;
    // Armor first (head to feet), then the offhand and the rest of the inventory.
    private static final int[] SLOT_ORDER = slotOrder();

    private final List<Integer> slots = new ArrayList<>();
    private int scroll;

    public ExoSettingsScreen() {
        super(Component.translatable("reactingreactions.exo.title"));
    }

    private static int[] slotOrder() {
        int[] order = new int[41];
        int i = 0;
        for (int slot = 39; slot >= 36; slot--) {
            order[i++] = slot;
        }
        order[i++] = Inventory.SLOT_OFFHAND;
        for (int slot = 0; slot < 36; slot++) {
            order[i++] = slot;
        }
        return order;
    }

    private Inventory inventory() {
        return minecraft.player.getInventory();
    }

    @Override
    protected void init() {
        slots.clear();
        for (int slot : SLOT_ORDER) {
            if (inventory().getItem(slot).getItem() instanceof ExoSettings.Configurable) {
                slots.add(slot);
            }
        }
        scroll = Math.max(0, Math.min(scroll, slots.size() - visibleRows()));
        rebuild();
    }

    private int visibleRows() {
        return Math.max(1, (height - 60) / ROW_HEIGHT);
    }

    private int left() {
        return (width - NAME_WIDTH - 3 * (BUTTON_WIDTH + 4)) / 2;
    }

    private void rebuild() {
        clearWidgets();
        for (int row = 0; row < visibleRows() && scroll + row < slots.size(); row++) {
            int slot = slots.get(scroll + row);
            ItemStack stack = inventory().getItem(slot);
            ExoSettings.Configurable item = (ExoSettings.Configurable) stack.getItem();
            int x = left() + NAME_WIDTH;
            for (ExoSettings.Option option : item.exoOptions()) {
                addRenderableWidget(Button.builder(label(option, item.setting(stack, option.key())), button -> {
                    // Fetched again: the server may have replaced the stack since.
                    ItemStack current = inventory().getItem(slot);
                    if (current.getItem() != item) {
                        return;
                    }
                    int next = (item.setting(current, option.key()) + 1) % option.values().size();
                    item.set(current, option.key(), next);
                    PacketDistributor.sendToServer(new CRRExoSettingPayload(slot, option.key(), next));
                    button.setMessage(label(option, next));
                }).bounds(x, 40 + row * ROW_HEIGHT, BUTTON_WIDTH, 20).build());
                x += BUTTON_WIDTH + 4;
            }
        }
    }

    private static Component label(ExoSettings.Option option, int value) {
        String key = "reactingreactions.exo." + option.key();
        return Component.translatable(key).append(": ").append(Component.translatable(key + "." + option.values().get(value)));
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = Math.max(0, slots.size() - visibleRows());
        int next = Math.max(0, Math.min(max, scroll - (int) Math.signum(scrollY)));
        if (next != scroll) {
            scroll = next;
            rebuild();
        }
        return true;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(font, title, width / 2, 16, 0xFFFFFF);
        if (slots.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("reactingreactions.exo.none"), width / 2, height / 2, 0xA0A0A0);
            return;
        }
        for (int row = 0; row < visibleRows() && scroll + row < slots.size(); row++) {
            ItemStack stack = inventory().getItem(slots.get(scroll + row));
            int y = 40 + row * ROW_HEIGHT;
            graphics.renderItem(stack, left(), y + 2);
            graphics.drawString(font, stack.getHoverName(), left() + 20, y + 6, 0xFFFFFF);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
