package com.friday.cultivation;

import net.minecraft.network.chat.Component;

/**
 * 灵气元素枚举 — 完整复刻原模组 QiElement。
 * <p>
 * 8 种元素：PURE / METAL / WOOD / WATER / FIRE / EARTH / ICE / LIGHTNING，每种带 id 和 RGB 颜色。
 * 提供 r/g/b 浮点颜色分量（用于粒子/特效渲染），displayName() 用于 UI。
 * </p>
 */
public enum QiElement {
    PURE("pure", 16770728),
    METAL("metal", 13158608),
    WOOD("wood", 0x66CC66),
    WATER("water", 6730495),
    FIRE("fire", 0xFF6644),
    EARTH("earth", 0xCCAA66),
    ICE("ice", 12116223),
    LIGHTNING("lightning", 14738943);

    private final String id;
    private final int rgb;

    private QiElement(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String id() {
        return this.id;
    }

    public int rgb() {
        return this.rgb;
    }

    public float r() {
        return (float) (this.rgb >> 16 & 0xFF) / 255.0f;
    }

    public float g() {
        return (float) (this.rgb >> 8 & 0xFF) / 255.0f;
    }

    public float b() {
        return (float) (this.rgb & 0xFF) / 255.0f;
    }

    public Component displayName() {
        return Component.translatable("element.friday_cultivation." + this.id);
    }

    public static QiElement byId(String id) {
        for (QiElement e : QiElement.values()) {
            if (!e.id.equals(id)) continue;
            return e;
        }
        return PURE;
    }
}
