package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 神行丹效果 — 完整复刻原模组 DivineStrideEffect。
 * 纯标记效果，实际加速由 MobEffects.MOVEMENT_SPEED 和 MobEffects.JUMP 在 DivineStridePillItem 中施加。
 */
public class DivineStrideEffect extends MobEffect {
    public DivineStrideEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x88CCFF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
