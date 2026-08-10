/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public final class SpellDamageSourceHelper {
    private SpellDamageSourceHelper() {
    }

    public static DamageSource indirectSpell(Entity direct, @Nullable LivingEntity owner) {
        if (owner != null) {
            return direct.damageSources().indirectMagic(direct, (Entity)owner);
        }
        return direct.damageSources().magic();
    }

    public static DamageSource directSpell(LivingEntity caster) {
        return caster.damageSources().indirectMagic((Entity)caster, (Entity)caster);
    }
}

