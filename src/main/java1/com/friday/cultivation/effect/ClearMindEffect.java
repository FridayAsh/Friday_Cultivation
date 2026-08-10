package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 清心丹效果 — 完整复刻原模组 ClearMindEffect。
 * 纯标记效果，不清除负面效果（清除逻辑在 ClearMindPillItem.finishUsingItem 中）。
 */
public class ClearMindEffect extends MobEffect {
    public ClearMindEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x80C0FF);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}
