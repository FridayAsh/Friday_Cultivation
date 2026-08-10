package com.friday.cultivation.alchemy;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.world.item.Item;

public final class PillEffectSpecs {
    private static final Map<Item, PillEffectSpec> OVERRIDES = new HashMap<>();
    private PillEffectSpecs() {}
    public static void reset() { OVERRIDES.clear(); }
    public static void override(Item item, PillEffectSpec spec) { OVERRIDES.put(item, spec); }
    public static int qiAmount(Item item, int fallback) {
        PillEffectSpec spec = OVERRIDES.get(item);
        return spec != null && spec.qi() != null ? spec.qi() : fallback;
    }
    public static float rejuvenationHeal(Item item, PillTier tier, float maxHealth) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null) {
            if (Boolean.TRUE.equals(spec.healFull())) return maxHealth;
            if (spec.heal() != null) return Math.max(0.0f, spec.heal());
        }
        return switch (tier) { case LOW -> 4.0f; case MID -> 10.0f; case HIGH, SUPREME -> maxHealth; default -> 0.0f; };
    }
    public static boolean rejuvenationHealFull(Item item, PillTier tier) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null) { if (Boolean.TRUE.equals(spec.healFull())) return true; if (spec.heal() != null) return false; }
        return tier == PillTier.HIGH || tier == PillTier.SUPREME;
    }
    public static int regenerationTicks(Item item, PillTier tier) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null && spec.regenerationTicks() != null) return Math.max(0, spec.regenerationTicks());
        return tier == PillTier.SUPREME ? 2400 : 0;
    }
    public static int regenerationAmplifier(Item item, PillTier tier) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null && spec.regenerationAmplifier() != null) return Math.max(0, spec.regenerationAmplifier());
        return tier == PillTier.SUPREME ? 1 : 0;
    }
    public static int absorptionTicks(Item item, PillTier tier) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null && spec.absorptionTicks() != null) return Math.max(0, spec.absorptionTicks());
        return tier == PillTier.SUPREME ? 2400 : 0;
    }
    public static int absorptionAmplifier(Item item, PillTier tier) {
        PillEffectSpec spec = OVERRIDES.get(item);
        if (spec != null && spec.absorptionAmplifier() != null) return Math.max(0, spec.absorptionAmplifier());
        return 0;
    }
}
