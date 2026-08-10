package com.friday.cultivation.spell;

import com.friday.cultivation.spirit.QiElement;
import net.minecraft.network.chat.Component;

/**
 * 法术元素枚举（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.spell.SpellElement）
 */
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

    public String id() { return this.id; }
    public QiElement matchingQi() { return this.matchingQi; }

    public Component displayName() {
        return Component.translatable("spell_element.friday_cultivation." + this.id);
    }
}