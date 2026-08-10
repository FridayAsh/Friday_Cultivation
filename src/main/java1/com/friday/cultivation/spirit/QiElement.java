package com.friday.cultivation.spirit;

import net.minecraft.network.chat.Component;

/**
 * 灵气元素 — 五行+变异
 */
public enum QiElement {
    PURE("pure", 0xFFFFFF, "纯"),
    METAL("metal", 0xFFD700, "金"),
    WOOD("wood", 0x00FF00, "木"),
    WATER("water", 0x0000FF, "水"),
    FIRE("fire", 0xFF4500, "火"),
    EARTH("earth", 0x8B4513, "土"),
    ICE("ice", 0x00FFFF, "冰"),
    LIGHTNING("lightning", 0x9370DB, "雷");

    private final String id;
    private final int rgb;
    private final String chineseName;

    QiElement(String id, int rgb, String chineseName) {
        this.id = id;
        this.rgb = rgb;
        this.chineseName = chineseName;
    }

    public String id() { return id; }
    public int rgb() { return rgb; }
    public float r() { return ((rgb >> 16) & 0xFF) / 255f; }
    public float g() { return ((rgb >> 8) & 0xFF) / 255f; }
    public float b() { return (rgb & 0xFF) / 255f; }

    public Component displayName() {
        return Component.translatableWithFallback("qi_element.friday_cultivation." + id, chineseName);
    }

    public static QiElement byId(String id) {
        for (QiElement e : values()) if (e.id.equals(id)) return e;
        return PURE;
    }
}
