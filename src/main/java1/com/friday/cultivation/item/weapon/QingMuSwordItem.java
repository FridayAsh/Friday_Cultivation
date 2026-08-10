package com.friday.cultivation.item.weapon;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.spirit.QiElement;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

/** 青木剑 — 完整复刻原模组 QingMuSwordItem。击中施加中毒效果。 */
public class QingMuSwordItem extends SpiritSwordItem {
    private static final int POISON_DURATION = 200;

    public QingMuSwordItem(ItemTier tier, int attackDamage, int spellBonusPct) {
        super(tier, QiElement.WOOD, attackDamage, spellBonusPct);
    }

    @Override
    protected void applyOnHitEffect(LivingEntity target, LivingEntity attacker) {
        if (!SoulStateHandler.canOrdinaryAffect((Entity) attacker, (Entity) target)) return;
        int amp = this.tier().ordinal();
        target.addEffect(new MobEffectInstance(MobEffects.POISON, POISON_DURATION, amp));
    }

    @Override
    @Nullable
    protected Component onHitEffectTooltip() {
        int level = this.tier().ordinal() + 1;
        return Component.translatable("tooltip.friday_cultivation.weapon.on_hit.poison", romanNumeral(level)).withStyle(ChatFormatting.GREEN);
    }

    private static String romanNumeral(int n) {
        return switch (n) {
            case 1 -> "I"; case 2 -> "II"; case 3 -> "III"; case 4 -> "IV"; case 5 -> "V";
            default -> String.valueOf(n);
        };
    }
}
