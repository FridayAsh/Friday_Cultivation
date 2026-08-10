package com.friday.cultivation.spell;

import net.minecraft.network.chat.Component;

/**
 * 法术类型枚举（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.spell.SpellType）
 */
public enum SpellType {
    PASSIVE("passive"),
    ACTIVE("active");

    private final String id;

    private SpellType(String id) {
        this.id = id;
    }

    public String id() { return this.id; }

    public Component displayName() {
        return Component.translatable("spell_type.friday_cultivation." + this.id);
    }
}