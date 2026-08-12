/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation;

import net.minecraft.network.chat.Component;

public enum ItemTier {
    LOW("low", -6567512),
    MID("mid", -10770200),
    HIGH("high", -4161296),
    SUPREME("supreme", -6528),
    IMMORTAL("immortal", -2068440),
    GREAT_EMPEROR("great_emperor", 16755200);

    private final String id;
    private final int rgb;

    private ItemTier(String id, int rgb) {
        this.id = id;
        this.rgb = rgb;
    }

    public String id() {
        return this.id;
    }

    public int rgb() {
        return this.rgb;
    }

    public Component displayName() {
        return Component.translatable((String)("item_tier.friday_cultivation." + this.id));
    }
}

