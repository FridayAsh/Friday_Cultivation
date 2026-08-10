package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 重力压制效果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.effect.GravitySuppressionEffect）
 * 效果：降低重力影响（增加下落距离倍数）
 */
public class GravitySuppressionEffect extends MobEffect {

    public GravitySuppressionEffect() {
        super(MobEffectCategory.NEUTRAL, 0x99DDFF);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "f1924001-aa01-4abc-8def-cafebabe0001", 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}