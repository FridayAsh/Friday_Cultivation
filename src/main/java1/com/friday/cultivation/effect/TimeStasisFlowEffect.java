package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** 时间凝滞流逝效果（照搬原模组 TimeStasisFlowEffect） */
public class TimeStasisFlowEffect extends MobEffect {
    public TimeStasisFlowEffect() {
        super(MobEffectCategory.BENEFICIAL, 14543103);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
