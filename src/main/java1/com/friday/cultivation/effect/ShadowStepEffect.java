package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 影遁丹效果 — 完整复刻原模组 ShadowStepEffect。
 * 纯标记效果，隐身逻辑在事件监听中处理。
 */
public class ShadowStepEffect extends MobEffect {
    public ShadowStepEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x6030A0);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
