package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 定身效果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.effect.RootedEffect）
 */
public class RootedEffect extends MobEffect {

    public RootedEffect() {
        super(MobEffectCategory.HARMFUL, 0x664422);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, "f1928001-ee01-4abc-9def-cafebabe0001", -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}