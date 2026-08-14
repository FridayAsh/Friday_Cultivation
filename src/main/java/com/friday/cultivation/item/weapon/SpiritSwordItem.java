/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
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
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.util.ShimmerColors;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class SpiritSwordItem
extends SwordItem
implements TieredWeapon {
    private final ItemTier tier;
    private final QiElement element;
    private final int attackDamage;
    private final int spellBonusPct;

    protected SpiritSwordItem(ItemTier tier, QiElement element, int attackDamage, int spellBonusPct) {
        super((Tier)Tiers.NETHERITE, attackDamage - (int)Tiers.NETHERITE.getAttackDamageBonus() - 1, -2.4f, new Item.Properties().rarity(SpiritSwordItem.rarityFor(tier)).fireResistant());
        this.tier = tier;
        this.element = element;
        this.attackDamage = attackDamage;
        this.spellBonusPct = spellBonusPct;
    }

    private static Rarity rarityFor(ItemTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> Rarity.COMMON;
            case MID -> Rarity.UNCOMMON;
            case HIGH -> Rarity.RARE;
            case SUPREME -> Rarity.EPIC;
            case SAGE -> Rarity.EPIC;
            case IMMORTAL -> Rarity.EPIC;
            case GREAT_EMPEROR -> Rarity.EPIC;
        };
    }

    @Override
    public ItemTier tier() {
        return this.tier;
    }

    @Override
    public QiElement element() {
        return this.element;
    }

    @Override
    public int spellBonusPct() {
        return this.spellBonusPct;
    }

    @Override
    public boolean isSwordWeapon() {
        return true;
    }

    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (SoulStateHandler.canOrdinaryAffect((Entity)attacker, (Entity)target)) {
            this.applyOnHitEffect(target, attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
    }

    @Nullable
    protected Component onHitEffectTooltip() {
        return null;
    }

    @NotNull
    public Component getName(@NotNull ItemStack stack) {
        if (this.tier == ItemTier.GREAT_EMPEROR) {
            return ShimmerColors.buildShimmeringName(Component.translatable((String)this.getDescriptionId(stack)).getString(), ShimmerColors.DIVINE_FLAME);
        }
        if (this.tier == ItemTier.SAGE) {
            return ShimmerColors.buildShimmeringName(Component.translatable((String)this.getDescriptionId(stack)).getString(), ShimmerColors.SAGE_AURA);
        }
        return TooltipUtils.tieredName((Component)Component.translatable((String)this.getDescriptionId(stack)), this.tier);
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        if (this.tier == ItemTier.GREAT_EMPEROR) {
            // [帝兵] [元素]：帝兵品阶用金红流动，元素标签保留
            MutableComponent tierBadge = Component.literal((String)"[").withStyle(ChatFormatting.DARK_GRAY).append(ShimmerColors.buildShimmeringName(Component.translatable((String)"item_tier.friday_cultivation.great_emperor_weapon").getString(), ShimmerColors.DIVINE_FLAME)).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty().append((Component)tierBadge).append((Component)Component.literal((String)" ")).append((Component)TooltipUtils.elementBadge(this.element)));
        } else if (this.tier == ItemTier.SAGE) {
            // [圣兵] [元素]：圣兵品阶用圣辉紫流动，元素标签保留
            MutableComponent sageBadge = Component.literal((String)"[").withStyle(ChatFormatting.DARK_GRAY).append(ShimmerColors.buildShimmeringName(Component.translatable((String)"item_tier.friday_cultivation.sage_weapon").getString(), ShimmerColors.SAGE_AURA)).append((Component)Component.literal((String)"]").withStyle(ChatFormatting.DARK_GRAY));
            tooltip.add(Component.empty().append((Component)sageBadge).append((Component)Component.literal((String)" ")).append((Component)TooltipUtils.elementBadge(this.element)));
        } else {
            tooltip.add((Component)TooltipUtils.tierElementLine(this.tier, this.element));
        }
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"tooltip.friday_cultivation.weapon.attack", (Object[])new Object[]{this.attackDamage})));
        Component onHit = this.onHitEffectTooltip();
        if (onHit != null) {
            tooltip.add((Component)TooltipUtils.effectLine(onHit));
        }
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.weapon.spell_bonus", (Object[])new Object[]{this.spellBonusPct})));
        tooltip.add((Component)TooltipUtils.effectLine((Component)Component.translatable((String)"tooltip.friday_cultivation.weapon.spell_qi_cost_reduction", (Object[])new Object[]{this.spellQiCostReductionPct()})));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}

