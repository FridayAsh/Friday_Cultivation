package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 经脉冻结效果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.effect.MeridianFrozenEffect）
 */
public class MeridianFrozenEffect extends MobEffect {

    public MeridianFrozenEffect() {
        super(MobEffectCategory.HARMFUL, 0x88CCFF);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "f1925001-bb01-4def-9abc-cafebabe0001", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}