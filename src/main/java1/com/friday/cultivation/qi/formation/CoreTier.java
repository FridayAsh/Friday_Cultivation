package com.friday.cultivation.qi.formation;

import com.friday.cultivation.ItemTier;

/**
 * 阵基核心等级（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.formation.CoreTier）
 */
public enum CoreTier {
    LOW(ItemTier.LOW, 100L),
    MID(ItemTier.MID, 1000L),
    HIGH(ItemTier.HIGH, 10000L),
    SUPREME(ItemTier.SUPREME, 100000L),
    IMMORTAL(ItemTier.IMMORTAL, 1000000L);

    private final ItemTier itemTier;
    private final long maxQi;

    CoreTier(ItemTier itemTier, long maxQi) {
        this.itemTier = itemTier;
        this.maxQi = maxQi;
    }

    public ItemTier itemTier() { return this.itemTier; }
    public long maxQi() { return this.maxQi; }

    public String idPrefix() {
        return this.itemTier.id();
    }
}