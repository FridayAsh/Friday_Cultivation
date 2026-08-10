/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectCategory
 */
package com.friday.cultivation.cultivation.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class ClearMindEffect
extends MobEffect {
    public ClearMindEffect() {
        super(MobEffectCategory.BENEFICIAL, 8446207);
    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }
}

