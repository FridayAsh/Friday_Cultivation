/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.entity.LivingEntity
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item.weapon;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.item.weapon.SpiritSwordItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

public class HanBingSwordItem
extends SpiritSwordItem {
    private static final int FROZEN_OVERFLOW_TICKS = 80;

    public HanBingSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.ICE, attackDamage, spellBonusPct);
    }

    @Override
    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
        int newFrozen = target.getTicksRequiredToFreeze() + 80;
        if (target.getTicksFrozen() < newFrozen) {
            target.setTicksFrozen(newFrozen);
        }
    }

    @Override
    @Nullable
    protected Component onHitEffectTooltip() {
        return Component.translatable((String)"tooltip.friday_cultivation.weapon.on_hit.freeze").withStyle(ChatFormatting.AQUA);
    }
}

