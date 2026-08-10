package com.friday.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

/**
 * 碎甲效果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.effect.ShatterArmorEffect）
 */
public class ShatterArmorEffect extends MobEffect {

    public ShatterArmorEffect() {
        super(MobEffectCategory.HARMFUL, 0x666666);
        this.addAttributeModifier(Attributes.ARMOR, "f1929001-ff01-4def-8abc-cafebabe0001", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, "f1929002-ff02-4abc-9def-cafebabe0002", -0.5, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}