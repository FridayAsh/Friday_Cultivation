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

public class ChiYanSwordItem
extends SpiritSwordItem {
    private static final int BURN_SECONDS = 5;

    public ChiYanSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.FIRE, attackDamage, spellBonusPct);
    }

    @Override
    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
        target.setRemainingFireTicks(100);
    }

    @Override
    @Nullable
    protected Component onHitEffectTooltip() {
        return Component.translatable((String)"tooltip.friday_cultivation.weapon.on_hit.burn", (Object[])new Object[]{5}).withStyle(ChatFormatting.GOLD);
    }
}

