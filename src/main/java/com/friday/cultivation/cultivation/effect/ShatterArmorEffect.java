/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.Attributes
 */
package com.friday.cultivation.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ShatterArmorEffect
extends MobEffect {
    private static final String UUID_ARMOR = "f17e0001-a001-4f1a-8c01-b0dd0a000001";
    private static final String UUID_TOUGHNESS = "f17e0002-a002-4f1a-8c02-b0dd0a000002";

    public ShatterArmorEffect() {
        super(MobEffectCategory.HARMFUL, 0xD8D8E8);
        this.addAttributeModifier(Attributes.ARMOR, UUID_ARMOR, -0.99, AttributeModifier.Operation.MULTIPLY_TOTAL);
        this.addAttributeModifier(Attributes.ARMOR_TOUGHNESS, UUID_TOUGHNESS, -0.99, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}

