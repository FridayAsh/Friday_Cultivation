/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.Tier
 *  net.minecraft.world.item.Tiers
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item.weapon;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoulHookItem
extends SwordItem
implements TieredWeapon {
    public static final int ATTACK_DAMAGE = 15;
    private static final ItemTier TIER = ItemTier.LOW;
    private static final QiElement ELEMENT = QiElement.PURE;

    public SoulHookItem(Item.Properties properties) {
        super((Tier)Tiers.NETHERITE, 15 - (int)Tiers.NETHERITE.getAttackDamageBonus() - 1, -2.4f, properties);
    }

    @Override
    public ItemTier tier() {
        return TIER;
    }

    @Override
    public QiElement element() {
        return ELEMENT;
    }

    @Override
    public int spellBonusPct() {
        return 0;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add((Component)TooltipUtils.tierElementLine(this.tier(), this.element()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"tooltip.friday_cultivation.weapon.attack", (Object[])new Object[]{15})));
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.weapon.spell_qi_cost_reduction", (Object[])new Object[]{this.spellQiCostReductionPct()})));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.soul_hook")));
    }
}

