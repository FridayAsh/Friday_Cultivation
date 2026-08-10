package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;

/**
 * 分阶武器接口 — 复刻原模组 TieredWeapon（含 default spellQiCostReductionPct / isSwordWeapon）
 */
public interface TieredWeapon {
    /** 武器品阶 */
    ItemTier tier();

    /** 武器元素 */
    QiElement element();

    /** 法术伤害加成百分比（如 20 = +20%） */
    int spellBonusPct();

    /** 法术灵气消耗减免百分比（如 10 = -10%） */
    default int spellQiCostReductionPct() {
        return switch (this.tier()) {
            case LOW -> 5;
            case MID -> 7;
            case HIGH -> 10;
            case SUPREME -> 15;
            case IMMORTAL -> 20;
        };
    }

    /** 是否为剑类武器 */
    default boolean isSwordWeapon() {
        return false;
    }
}
