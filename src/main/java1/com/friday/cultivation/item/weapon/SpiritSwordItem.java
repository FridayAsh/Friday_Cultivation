package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
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

/**
 * 灵剑基类 — 完整复刻原模组 SpiritSwordItem
 */
public abstract class SpiritSwordItem extends SwordItem implements TieredWeapon {
    private final ItemTier tier;
    private final QiElement element;
    private final int attackDamage;
    private final int spellBonusPct;

    protected SpiritSwordItem(ItemTier tier, QiElement element, int attackDamage, int spellBonusPct) {
        super((Tier) Tiers.NETHERITE, attackDamage - (int) Tiers.NETHERITE.getAttackDamageBonus() - 1, -2.4f, new Item.Properties().rarity(rarityFor(tier)).fireResistant());
        this.tier = tier;
        this.element = element;
        this.attackDamage = attackDamage;
        this.spellBonusPct = spellBonusPct;
    }

    private static Rarity rarityFor(ItemTier tier) {
        return switch (tier) {
            case LOW -> Rarity.COMMON;
            case MID -> Rarity.UNCOMMON;
            case HIGH -> Rarity.RARE;
            case SUPREME -> Rarity.EPIC;
            case IMMORTAL -> Rarity.EPIC;
        };
    }

    @Override
    public ItemTier tier() { return this.tier; }

    @Override
    public QiElement element() { return this.element; }

    @Override
    public int spellBonusPct() { return this.spellBonusPct; }

    @Override
    public int spellQiCostReductionPct() { return 0; }

    @Override
    public boolean isSwordWeapon() { return true; }

    @Override
    public boolean hurtEnemy(@NotNull ItemStack stack, @NotNull LivingEntity target, @NotNull LivingEntity attacker) {
        if (SoulStateHandler.canOrdinaryAffect((Entity) attacker, (Entity) target)) {
            this.applyOnHitEffect(target, attacker);
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {}

    @Nullable
    protected Component onHitEffectTooltip() { return null; }

    @NotNull
    @Override
    public Component getName(@NotNull ItemStack stack) {
        return TooltipUtils.tieredName(Component.translatable(this.getDescriptionId(stack)), this.tier);
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(TooltipUtils.tierElementLine(this.tier, this.element));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add(TooltipUtils.costLine(Component.translatable("tooltip.friday_cultivation.weapon.attack", this.attackDamage)));
        Component onHit = this.onHitEffectTooltip();
        if (onHit != null) {
            tooltip.add(TooltipUtils.effectLine(onHit));
        }
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.weapon.spell_bonus", this.spellBonusPct)));
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.weapon.spell_qi_cost_reduction", this.spellQiCostReductionPct())));
        super.appendHoverText(stack, level, tooltip, flag);
    }
}
