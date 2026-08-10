package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;

/** 玄铁剑 — 完整复刻原模组 XuanIronSwordItem */
public class XuanIronSwordItem extends SpiritSwordItem {
    public XuanIronSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.METAL, attackDamage, spellBonusPct);
    }
}
