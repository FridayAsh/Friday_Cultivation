package com.friday.cultivation.util;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

/**
 * 逐字符时间流动渐变色名称（参考 DivineArsenal 帝炎系列实现）。
 * 金红色调用于帝分类（帝法/帝兵/大帝）最高品质物品的文本。
 */
public final class ShimmerColors {
    /** 帝炎金红：金→橙→红→橙 循环 */
    public static final int[] DIVINE_FLAME = {0xFFD700, 0xFFA500, 0xFF6600, 0xFF3300, 0xFF6600, 0xFFA500};
    /** 帝法乱码流动色（偏暗金红） */
    public static final int[] DIVINE_MYSTERY = {0xCC6600, 0xFF8800, 0xFF4400, 0xAA2200, 0xFF4400, 0xFF8800};

    private ShimmerColors() {
    }

    public static MutableComponent buildShimmeringName(String text, int[] colors) {
        return ShimmerColors.buildShimmeringName(text, colors, false);
    }

    public static MutableComponent buildShimmeringName(String text, int[] colors, boolean obfuscated) {
        long time = System.currentTimeMillis();
        double t = (double)(time % 1000L) / 1000.0;
        MutableComponent result = Component.empty();
        for (int i = 0; i < text.length(); ++i) {
            char c = text.charAt(i);
            double phase = (t + (double)i * 0.08) % 1.0;
            double seg = phase * (double)(colors.length - 1);
            int idx = (int)seg;
            double frac = seg - (double)idx;
            int c1 = colors[idx];
            int c2 = colors[(idx + 1) % colors.length];
            int r = (int)((double)(c1 >> 16 & 0xFF) * (1.0 - frac) + (double)(c2 >> 16 & 0xFF) * frac);
            int g = (int)((double)(c1 >> 8 & 0xFF) * (1.0 - frac) + (double)(c2 >> 8 & 0xFF) * frac);
            int b = (int)((double)(c1 & 0xFF) * (1.0 - frac) + (double)(c2 & 0xFF) * frac);
            int rgb = r << 16 | g << 8 | b;
            if (obfuscated) {
                result.append(Component.literal(String.valueOf(c)).withStyle(style -> style.withColor(rgb).withBold(true).withObfuscated(true)));
            } else {
                result.append(Component.literal(String.valueOf(c)).withStyle(style -> style.withColor(rgb).withBold(true)));
            }
        }
        return result;
    }
}
