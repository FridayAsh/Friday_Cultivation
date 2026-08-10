package com.friday.cultivation.technique;

import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.spirit.QiElement;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * 武器加成辅助 — 完整复刻原模组 WeaponBonusHelper
 * 主手为分阶武器时，剑类法术或元素匹配则法伤×(1+spellBonusPct%)；双手中取最高灵气消耗减免
 */
public final class WeaponBonusHelper {
    private WeaponBonusHelper() {}

    /** 武器法术伤害倍率（主手分阶武器 + 剑法/元素匹配） */
    public static double spellDamageMultiplier(LivingEntity entity, Spell spell) {
        if (entity == null || spell == null) return 1.0;
        ItemStack mainHand = entity.getMainHandItem();
        Item item = mainHand.getItem();
        if (!(item instanceof TieredWeapon weapon)) return 1.0;
        boolean swordMatch = spell.isSwordSpell() && weapon.isSwordWeapon();
        boolean elementMatch = matchesElement(spell.element(), weapon.element());
        if (!swordMatch && !elementMatch) return 1.0;
        return 1.0 + (double) weapon.spellBonusPct() / 100.0;
    }

    /** 法术灵气消耗倍率（双手取最高减免） */
    public static double spellQiCostMultiplier(LivingEntity entity) {
        int reductionPct = spellQiCostReductionPct(entity);
        if (reductionPct <= 0) return 1.0;
        return Math.max(0.0, 1.0 - (double) reductionPct / 100.0);
    }

    /** 法术灵气消耗减免百分比（双手取最高） */
    public static int spellQiCostReductionPct(LivingEntity entity) {
        if (entity == null) return 0;
        int mainHand = spellQiCostReductionPct(entity.getMainHandItem());
        int offHand = spellQiCostReductionPct(entity.getOffhandItem());
        return Math.max(mainHand, offHand);
    }

    private static int spellQiCostReductionPct(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return 0;
        Item item = stack.getItem();
        if (!(item instanceof TieredWeapon weapon)) return 0;
        return Math.max(0, weapon.spellQiCostReductionPct());
    }

    /** 法术元素与武器元素是否匹配（NONE匹配PURE，WOOD_FIRE匹配木/火，其他按matchingQi） */
    private static boolean matchesElement(SpellElement spellEl, QiElement weaponEl) {
        if (spellEl == null || weaponEl == null) return false;
        if (spellEl == SpellElement.NONE) return weaponEl == QiElement.PURE;
        if (spellEl == SpellElement.WOOD_FIRE) return weaponEl == QiElement.WOOD || weaponEl == QiElement.FIRE;
        QiElement qi = spellEl.matchingQi();
        return qi != null && qi == weaponEl;
    }
}
