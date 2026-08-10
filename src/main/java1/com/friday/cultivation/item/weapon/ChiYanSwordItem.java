package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** 赤炎剑 — 完整复刻原模组 ChiYanSwordItem。击中施加燃烧效果。 */
public class ChiYanSwordItem extends SpiritSwordItem {
    private static final int BURN_SECONDS = 5;

    public ChiYanSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.FIRE, attackDamage, spellBonusPct);
    }

    @Override
    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
        target.setSecondsOnFire(BURN_SECONDS);
    }

    @Override
    @Nullable
    protected Component onHitEffectTooltip() {
        return Component.translatable("tooltip.friday_cultivation.weapon.on_hit.burn", BURN_SECONDS).withStyle(ChatFormatting.GOLD);
    }
}
