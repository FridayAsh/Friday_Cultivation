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

public class RootedEffect
extends MobEffect {
    private static final String UUID_MOVE = "f17e0003-a003-4f1a-8c03-b0dd0a000003";

    public RootedEffect() {
        super(MobEffectCategory.HARMFUL, 0x66CC66);
        this.addAttributeModifier(Attributes.MOVEMENT_SPEED, UUID_MOVE, -1.0, AttributeModifier.Operation.MULTIPLY_TOTAL);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}

