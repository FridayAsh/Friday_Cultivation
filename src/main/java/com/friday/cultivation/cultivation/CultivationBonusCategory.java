/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation;

import org.jetbrains.annotations.Nullable;

public enum CultivationBonusCategory {
    BODY_DEFENSE("body_defense"),
    MOVEMENT_SPEED("movement_speed"),
    JUMP_HEIGHT("jump_height"),
    MELEE_DAMAGE("melee_damage"),
    MINING_SPEED("mining_speed"),
    SPELL_DAMAGE("spell_damage"),
    MAX_QI("max_qi"),
    QI_RECOVERY("qi_recovery"),
    CULTIVATION_EFFICIENCY("cultivation_efficiency");

    private final String id;

    private CultivationBonusCategory(String id) {
        this.id = id;
    }

    public String id() {
        return this.id;
    }

    public String labelKey() {
        return "screen.friday_cultivation.bonus_category." + this.id;
    }

    public String descriptionKey() {
        return "screen.friday_cultivation.bonus_category." + this.id + ".desc";
    }

    @Nullable
    public static CultivationBonusCategory byId(String id) {
        if (id == null) {
            return null;
        }
        for (CultivationBonusCategory category : CultivationBonusCategory.values()) {
            if (!category.id.equals(id)) continue;
            return category;
        }
        return null;
    }
}

