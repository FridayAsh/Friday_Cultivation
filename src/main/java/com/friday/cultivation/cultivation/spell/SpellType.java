/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation.spell;

import net.minecraft.network.chat.Component;

public enum SpellType {
    PASSIVE("passive"),
    ACTIVE("active");

    private final String id;

    private SpellType(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public Component displayName() {
        return Component.translatable((String)("spell_type.friday_cultivation." + this.id));
    }
}

