package com.friday.cultivation;

import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;

/**
 * 品阶枚举 — 复刻自原模组 com.xiaoxiang.cultivation.cultivation.ItemTier
 * LOW/MID/HIGH/SUPREME/IMMORTAL 五档，用于法术/功法/物品品阶
 */
public enum ItemTier {
    LOW("low", 0x9A9A98),
    MID("mid", 0x55FF55),
    HIGH("high", 0x55FFFF),
    SUPREME("supreme", 0xFFAA00),
    IMMORTAL("immortal", 0xFF5555);

    private final String id;
    private final int rgb;

    ItemTier(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String id() { return id; }
    public int rgb() { return rgb; }

    /** 由 rgb 决定的 ChatFormatting 颜色（用于文本样式） */
    public ChatFormatting color() {
        int r = (this.rgb >> 16) & 0xFF;
        int g = (this.rgb >> 8) & 0xFF;
        int b = this.rgb & 0xFF;
        // 高亮的绿色/青色/红色等用对应颜色；灰色走 GRAY
        if (r > 200 && g < 100 && b < 100) return ChatFormatting.RED;
        if (r > 200 && g > 100 && b < 50) return ChatFormatting.GOLD;
        if (r < 100 && g > 200 && b < 100) return ChatFormatting.GREEN;
        if (r < 100 && g > 200 && b > 200) return ChatFormatting.AQUA;
        if (r > 200 && g > 200 && b > 200) return ChatFormatting.WHITE;
        if (Math.abs(r - g) < 20 && Math.abs(g - b) < 20 && r < 200) return ChatFormatting.GRAY;
        return ChatFormatting.WHITE;
    }

    public Component displayName() {
        return Component.translatableWithFallback("item_tier.friday_cultivation." + id, switch (this) {
            case LOW -> "下品";
            case MID -> "中品";
            case HIGH -> "上品";
            case SUPREME -> "极品";
            case IMMORTAL -> "仙品";
        });
    }

    public static ItemTier byId(String id) {
        for (ItemTier t : values()) if (t.id.equals(id)) return t;
        return LOW;
    }
}
