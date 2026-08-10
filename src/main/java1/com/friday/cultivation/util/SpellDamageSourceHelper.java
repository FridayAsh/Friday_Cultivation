package com.friday.cultivation.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * 法术伤害源助手 - 构造带法术标记的 DamageSource。
 * 完全照搬原 mod: xiaoxiang.cultivation.util.SpellDamageSourceHelper
 */
public final class SpellDamageSourceHelper {
    private SpellDamageSourceHelper() {
    }

    public static DamageSource directSpell(Player attacker) {
        if (attacker == null) return attacker.damageSources().magic();
        return attacker.damageSources().indirectMagic(attacker, attacker);
    }

    public static DamageSource directSpell(LivingEntity attacker) {
        if (attacker == null) return attacker.damageSources().magic();
        return attacker.damageSources().indirectMagic(attacker, attacker);
    }

    public static DamageSource indirectSpell(Entity owner, LivingEntity attacker) {
        if (attacker == null) return attacker.damageSources().magic();
        return attacker.damageSources().indirectMagic(attacker, owner);
    }
}
