package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.util.TooltipUtils;
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

import java.util.List;

/**
 * 勾魂索武器 — 完全照搬原模组 xiaoxiang.cultivation.item.weapon.SoulHookItem（71 行完整版）。
 */
public class SoulHookItem extends SwordItem implements TieredWeapon {
    public static final int ATTACK_DAMAGE = 15;
    private static final ItemTier TIER = ItemTier.LOW;
    private static final QiElement ELEMENT = QiElement.PURE;

    public SoulHookItem(Item.Properties properties) {
        super((Tier) Tiers.NETHERITE, ATTACK_DAMAGE - (int) Tiers.NETHERITE.getAttackDamageBonus() - 1, -2.4f, properties);
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

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(TooltipUtils.tierElementLine(this.tier(), this.element()));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.stats");
        tooltip.add(TooltipUtils.costLine(Component.translatable("tooltip.friday_cultivation.weapon.attack", ATTACK_DAMAGE)));
        tooltip.add(TooltipUtils.effectLine(Component.translatable("tooltip.friday_cultivation.weapon.spell_qi_cost_reduction", this.spellQiCostReductionPct())));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.soul_hook")));
    }
}
