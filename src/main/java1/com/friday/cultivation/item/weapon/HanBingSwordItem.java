package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** 寒冰剑 — 完整复刻原模组 HanBingSwordItem。击中施加冻结效果。 */
public class HanBingSwordItem extends SpiritSwordItem {
    private static final int FROZEN_OVERFLOW_TICKS = 80;

    public HanBingSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.ICE, attackDamage, spellBonusPct);
    }

    @Override
    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
        int newFrozen = target.getTicksFrozen() + FROZEN_OVERFLOW_TICKS;
        if (target.getTicksFrozen() < newFrozen) {
            target.setTicksFrozen(newFrozen);
        }
    }

    @Override
    @Nullable
    protected Component onHitEffectTooltip() {
        return Component.translatable("tooltip.friday_cultivation.weapon.on_hit.freeze").withStyle(ChatFormatting.AQUA);
    }
}
