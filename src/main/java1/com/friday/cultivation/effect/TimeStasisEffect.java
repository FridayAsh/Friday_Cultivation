package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 时间凝滞效果（照搬原模组 TimeStasisEffect） */
public class TimeStasisEffect extends MobEffect {
    public TimeStasisEffect() {
        super(MobEffectCategory.HARMFUL, 12567756);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
