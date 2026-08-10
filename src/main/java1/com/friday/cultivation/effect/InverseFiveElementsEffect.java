package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * 逆五行效果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.effect.InverseFiveElementsEffect）。
 * <p>颜色 8712417（0x84F5F1，青绿色），分类 BENEFICIAL。</p>
 */
public class InverseFiveElementsEffect extends MobEffect {
    public InverseFiveElementsEffect() {
        super(MobEffectCategory.BENEFICIAL, 8712417);
    }
}
