/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.item.weapon;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;

public interface TieredWeapon {
    public ItemTier tier();

    public QiElement element();

    public int spellBonusPct();

    default public int spellQiCostReductionPct() {
        return switch (this.tier()) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 5;
            case MID -> 7;
            case HIGH -> 10;
            case SUPREME -> 15;
            case IMMORTAL -> 20;
            case GREAT_EMPEROR -> 50;
        };
    }

    default public boolean isSwordWeapon() {
        return false;
    }
}

