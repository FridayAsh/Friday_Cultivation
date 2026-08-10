package com.friday.cultivation.client;

import com.friday.cultivation.alchemy.PillTier;
import com.friday.cultivation.item.PillItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

/**
 * 丹药光晕装饰器（严格照搬原模组 com.xiaoxiang.cultivation.client.PillGlowDecorator）
 */
public class PillGlowDecorator implements IItemDecorator {
    private static final int CYCLE_TICKS = 60;
    private static final int[] LAYER_SIZES = new int[]{4, 8, 12, 16};
    private static final int[] LAYER_BASE_ALPHA = new int[]{175, 115, 55, 20};

    @Override
    public boolean render(GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
        if (!(stack.getItem() instanceof PillItem pill)) {
            return false;
        }
        PillTier tier = pill.tier();
        int rgb = tier.color().getColor();
        if ((rgb & 0xFF000000) == 0) {
            return false;
        }
        Minecraft mc = Minecraft.getInstance();
        long time = mc.level != null ? mc.level.getGameTime() : 0L;
        double phase = (double)(time % CYCLE_TICKS) / (double)CYCLE_TICKS;
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