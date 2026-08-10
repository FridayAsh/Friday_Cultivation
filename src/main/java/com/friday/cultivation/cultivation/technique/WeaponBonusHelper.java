/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 */
package com.friday.cultivation.cultivation.technique;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.item.weapon.TieredWeapon;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public final class WeaponBonusHelper {
    private WeaponBonusHelper() {
    }

    public static double spellDamageMultiplier(LivingEntity entity, Spell spell) {
        if (entity == null || spell == null) {
            return 1.0;
        }
        ItemStack mainHand = entity.getMainHandItem();
        Item item = mainHand.getItem();
        if (!(item instanceof TieredWeapon)) {
            return 1.0;
        }
        TieredWeapon weapon = (TieredWeapon)item;
        boolean swordMatch = spell.isSwordSpell() && weapon.isSwordWeapon();
        boolean elementMatch = WeaponBonusHelper.matchesElement(spell.element(), weapon.element());
        if (!swordMatch && !elementMatch) {
            return 1.0;
        }
        return 1.0 + (double)weapon.spellBonusPct() / 100.0;
    }

    public static double spellQiCostMultiplier(LivingEntity entity) {
        int reductionPct = WeaponBonusHelper.spellQiCostReductionPct(entity);
        if (reductionPct <= 0) {
            return 1.0;
        }
        return Math.max(0.0, 1.0 - (double)reductionPct / 100.0);
    }

    public static int spellQiCostReductionPct(LivingEntity entity) {
        if (entity == null) {
            return 0;
        }
        int mainHand = WeaponBonusHelper.spellQiCostReductionPct(entity.getMainHandItem());
        int offHand = WeaponBonusHelper.spellQiCostReductionPct(entity.getOffhandItem());
        return Math.max(mainHand, offHand);
    }

    private static int spellQiCostReductionPct(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        Item item = stack.getItem();
        if (!(item instanceof TieredWeapon)) {
            return 0;
        }
        TieredWeapon weapon = (TieredWeapon)item;
        return Math.max(0, weapon.spellQiCostReductionPct());
    }

    private static boolean matchesElement(SpellElement spellEl, QiElement weaponEl) {
        if (spellEl == null || weaponEl == null) {
            return false;
        }
        if (spellEl == SpellElement.NONE) {
            return weaponEl == QiElement.PURE;
        }
        if (spellEl == SpellElement.WOOD_FIRE) {
            return weaponEl == QiElement.WOOD || weaponEl == QiElement.FIRE;
        }
        QiElement qi = spellEl.matchingQi();
        return qi != null && qi == weaponEl;
    }
}

