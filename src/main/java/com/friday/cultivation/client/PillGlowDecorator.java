/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.Font
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.client.IItemDecorator
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.item.PillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class PillGlowDecorator
implements IItemDecorator {
    private static final int CYCLE_TICKS = 60;
    private static final int[] LAYER_SIZES = new int[]{4, 8, 12, 16};
    private static final int[] LAYER_BASE_ALPHA = new int[]{175, 115, 55, 20};

    public boolean render(GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
        Item item = stack.getItem();
        if (!(item instanceof PillItem)) {
            return false;
        }
        PillItem pill = (PillItem)item;
        PillTier tier = pill.tier();
        Integer rgbObj = tier.color().getColor();
        if (rgbObj == null) {
            return false;
        }
        int rgb = rgbObj & 0xFFFFFF;
        Minecraft mc = Minecraft.getInstance();
        long time = mc.level != null ? mc.level.getGameTime() : 0L;
        double phase = (double)(time % 60L) / 60.0;
        float pulse = (float)(0.65 + 0.35 * Math.sin(phase * 2.0 * Math.PI));
        for (int i = 0; i < LAYER_SIZES.length; ++i) {
            int alpha = (int)((float)LAYER_BASE_ALPHA[i] * pulse);
            if (alpha < 3) continue;
            int color = alpha << 24 | rgb;
            int sz = LAYER_SIZES[i];
            int inset = (16 - sz) / 2;
            gui.fill(x + inset, y + inset, x + inset + sz, y + inset + sz, color);
        }
        return true;
    }
}

