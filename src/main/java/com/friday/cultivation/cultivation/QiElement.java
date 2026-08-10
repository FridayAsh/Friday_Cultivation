/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation;

import net.minecraft.network.chat.Component;

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
        return (float)(this.rgb >> 16 & 0xFF) / 255.0f;
    }

    public float g() {
        return (float)(this.rgb >> 8 & 0xFF) / 255.0f;
    }

    public float b() {
        return (float)(this.rgb & 0xFF) / 255.0f;
    }

    public Component displayName() {
        return Component.translatable((String)("element.friday_cultivation." + this.id));
    }

    public static QiElement byId(String id) {
        for (QiElement e : QiElement.values()) {
            if (!e.id.equals(id)) continue;
            return e;
        }
        return PURE;
    }
}

