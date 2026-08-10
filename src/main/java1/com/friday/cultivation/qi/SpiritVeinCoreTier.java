package com.friday.cultivation.qi;

import com.friday.cultivation.ItemTier;
import net.minecraft.world.item.Rarity;

/**
 * 灵脉核心等级（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.SpiritVeinCoreTier）
 */
public enum SpiritVeinCoreTier {
    LOW(ItemTier.LOW, 100L, 1L, 1L, Rarity.UNCOMMON),
    MID(ItemTier.MID, 1000L, 10L, 10L, Rarity.RARE),
    HIGH(ItemTier.HIGH, 10000L, 100L, 100L, Rarity.RARE),
    SUPREME(ItemTier.SUPREME, 100000L, 1000L, 1000L, Rarity.EPIC),
    IMMORTAL(ItemTier.IMMORTAL, 1000000L, 10000L, 10000L, Rarity.EPIC);

    private final ItemTier itemTier;
    private final long maxQi;
    private final long orbGain;
    private final long supplyPerSecond;
    private final Rarity rarity;

    SpiritVeinCoreTier(ItemTier itemTier, long maxQi, long orbGain, long supplyPerSecond, Rarity rarity) {
        this.itemTier = itemTier;
        this.maxQi = maxQi;
        this.orbGain = orbGain;
        this.supplyPerSecond = supplyPerSecond;
        this.rarity = rarity;
    }

    public ItemTier itemTier() { return this.itemTier; }
    public long maxQi() { return this.maxQi; }
    public long orbGain() { return this.orbGain; }
    public long supplyPerSecond() { return this.supplyPerSecond; }
    public Rarity rarity() { return this.rarity; }

    public String idPrefix() {
        return this.itemTier.id();
    }
}