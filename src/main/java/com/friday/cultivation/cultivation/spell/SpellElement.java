/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.cultivation.spell;

import com.friday.cultivation.cultivation.QiElement;
import net.minecraft.network.chat.Component;

public enum SpellElement {
    NONE("none", null),
    METAL("metal", QiElement.METAL),
    WOOD("wood", QiElement.WOOD),
    WOOD_FIRE("wood_fire", null),
    WATER("water", QiElement.WATER),
    ICE("ice", QiElement.ICE),
    FIRE("fire", QiElement.FIRE),
    EARTH("earth", QiElement.EARTH),
    LIGHTNING("lightning", QiElement.LIGHTNING);

    private final String id;
    private final QiElement matchingQi;

    private SpellElement(String id, QiElement matchingQi) {
        this.id = id;
        this.matchingQi = matchingQi;
    }

    public String id() {
        return this.id;
    }

    public QiElement matchingQi() {
        return this.matchingQi;
    }

    public Component displayName() {
        return Component.translatable((String)("spell_element.friday_cultivation." + this.id));
    }
}

