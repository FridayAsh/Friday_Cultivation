package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 掌雷眩晕效果 — 严格复刻自原模组 com.xiaoxiang.cultivation.cultivation.effect.PalmThunderStunEffect
 * <p>
 * HARMFUL 类别，颜色 14273791（淡紫）；每 tick 都触发视觉（isDurationEffectTick=true）。
 */
public class PalmThunderStunEffect extends MobEffect {
    public PalmThunderStunEffect() {
        super(MobEffectCategory.HARMFUL, 14273791);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
