/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.SwordItem
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.registry.ModEffects;
import java.util.ArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;

public final class PhysiqueBonusHelper {
    private static final int INVERSE_MARK_DURATION_TICKS = 600;
    private static final double INVERSE_BASE_FIVE_ELEMENT_MULTIPLIER = 1.1;
    private static final double INVERSE_BASE_FIVE_ELEMENT_COST_MULTIPLIER = 0.9;
    private static final double INVERSE_STACK_DAMAGE_PER_LAYER = 0.25;
    private static final double INVERSE_STACK_COST_REDUCTION_PER_LAYER = 0.25;
    private static final double CHAOS_BODY_SPELL_DAMAGE_MULTIPLIER = 1.3;
    private static final double CHAOS_BODY_CULTIVATION_REQUIREMENT_MULTIPLIER = 10.0;
    private static final double BLOOD_FIEND_BLOOD_SPELL_MULTIPLIER = 1.2;
    private static final double BLOOD_FIEND_BLOOD_SPELL_COST_MULTIPLIER = 0.8;
    private static final double FIRE_BODY_COST_MULTIPLIER = 0.8;
    private static final double ICE_BODY_COST_MULTIPLIER = 0.8;
    private static final double ALCHEMY_HEART_SUCCESS_BONUS = 0.1;
    private static final double ALCHEMY_HEART_QI_COST_MULTIPLIER = 0.5;

    private PhysiqueBonusHelper() {
    }

    public static Physique physiqueOf(Player player) {
        if (player == null) {
            return Physique.MORTAL_BODY;
        }
        return CultivationCapability.get(player).map(CultivationData::getPhysique).orElse(Physique.MORTAL_BODY);
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        Physique physique = PhysiqueBonusHelper.physiqueOf(player);
        double multiplier = physique.bonus().spellMultiplier(spell);
        multiplier = PhysiqueBonusHelper.applySpecialSpellDamageRules(physique, spell, PhysiqueBonusHelper.hasFiveElementChaosBodyCombo(player), multiplier);
        multiplier = PhysiqueBonusHelper.applyPlayerOnlySpellDamageRules(player, physique, spell, multiplier);
        return Math.max(0.0, multiplier);
    }

    public static double spellDamageMultiplier(Physique physique, Spell spell) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = p.bonus().spellMultiplier(spell);
        multiplier = PhysiqueBonusHelper.applySpecialSpellDamageRules(p, spell, false, multiplier);
        multiplier = PhysiqueBonusHelper.applySharedSpellDamageRules(p, spell, multiplier);
        if (p == Physique.INNATE_SWORD_BODY && PhysiqueBonusHelper.isNonSwordElementalSpell(spell)) {
            multiplier *= 0.2;
        }
        return Math.max(0.0, multiplier);
    }

    public static double spellDamageMultiplier(WanderingCultivatorEntity npc, Spell spell) {
        if (npc == null) {
            return PhysiqueBonusHelper.spellDamageMultiplier(Physique.MORTAL_BODY, spell);
        }
        Physique physique = npc.getPhysique();
        double multiplier = (physique == null ? Physique.MORTAL_BODY : physique).bonus().spellMultiplier(spell);
        multiplier = PhysiqueBonusHelper.applySpecialSpellDamageRules(physique, spell, PhysiqueBonusHelper.hasFiveElementChaosBodyCombo(npc), multiplier);
        multiplier = PhysiqueBonusHelper.applySharedSpellDamageRules(physique, spell, multiplier);
        if (physique == Physique.INNATE_SWORD_BODY && PhysiqueBonusHelper.isNonSwordElementalSpell(spell)) {
            multiplier *= 0.2;
        }
        return Math.max(0.0, multiplier);
    }

    public static double swordSpellMultiplier(Player player, Spell spell) {
        if (player == null || spell == null || !spell.isSwordSpell()) {
            return 1.0;
        }
        return PhysiqueBonusHelper.physiqueOf(player).bonus().swordSpellMult();
    }

    public static double swordDamageMultiplier(Player player) {
        if (player == null) {
            return 1.0;
        }
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) {
            return 1.0;
        }
        return PhysiqueBonusHelper.physiqueOf(player).bonus().swordSpellMult();
    }

    public static double meleeDamageMultiplier(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player).bonus().meleeDmgMult();
    }

    public static double hpMultiplier(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player).bonus().hpMult();
    }

    public static double hpBonus(Player player) {
        if (player == null) {
            return 0.0;
        }
        Physique physique = PhysiqueBonusHelper.physiqueOf(player);
        double mult = PhysiqueBonusHelper.hpMultiplier(player);
        double flat = physique.bonus().maxHpBonus();
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        double base = attr != null ? attr.getBaseValue() : 20.0;
        return flat + (mult > 1.0 ? (mult - 1.0) * base : 0.0);
    }

    public static int qiAbsorbRangeBonus(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player).bonus().qiAbsorbRange();
    }

    public static double qiAbsorbMultiplier(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player).bonus().qiAbsorbMult();
    }

    public static double qiAbsorbMultiplierForElement(Player player, QiElement element) {
        double multiplier = PhysiqueBonusHelper.qiAbsorbMultiplier(player);
        return PhysiqueBonusHelper.sanitizeMultiplier(multiplier *= PhysiqueBonusHelper.qiAbsorbElementMultiplier(PhysiqueBonusHelper.physiqueOf(player), element));
    }

    public static double qiAbsorbMultiplierForElement(Physique physique, QiElement element) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = p.bonus().qiAbsorbMult();
        return PhysiqueBonusHelper.sanitizeMultiplier(multiplier *= PhysiqueBonusHelper.qiAbsorbElementMultiplier(p, element));
    }

    public static double qiAbsorbElementMultiplier(Player player, QiElement element) {
        return PhysiqueBonusHelper.qiAbsorbElementMultiplier(PhysiqueBonusHelper.physiqueOf(player), element);
    }

    public static double qiAbsorbElementMultiplier(Physique physique, QiElement element) {
        Physique p;
        Physique physique2 = p = physique == null ? Physique.MORTAL_BODY : physique;
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && PhysiqueBonusHelper.isFiveElementQi(element)) {
            return 1.15;
        }
        return 1.0;
    }

    public static double generalQiCostMultiplier(Player player) {
        return PhysiqueBonusHelper.generalQiCostMultiplier(PhysiqueBonusHelper.physiqueOf(player));
    }

    public static double generalQiCostMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return PhysiqueBonusHelper.sanitizeMultiplier(p.bonus().qiCostMult());
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        int stacks;
        Physique physique = PhysiqueBonusHelper.physiqueOf(player);
        double multiplier = PhysiqueBonusHelper.spellQiCostMultiplier(physique, spell);
        if (physique == Physique.INVERSE_FIVE_ELEMENTS_BODY && PhysiqueBonusHelper.isBasicFiveElementSpell(spell) && (stacks = CultivationCapability.get(player).map(data -> data.getActiveInverseFiveElementStacks(player.level().getGameTime())).orElse(0).intValue()) > 0) {
            multiplier *= Math.max(0.0, 1.0 - (double)stacks * 0.25);
        }
        return PhysiqueBonusHelper.sanitizeMultiplier(multiplier);
    }

    public static double spellQiCostMultiplier(Physique physique, Spell spell) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = 1.0;
        if (p == Physique.INNATE_SWORD_BODY) {
            if (spell != null && spell.isSwordSpell()) {
                multiplier *= 0.5;
            } else if (PhysiqueBonusHelper.isNonSwordElementalSpell(spell)) {
                multiplier *= 2.0;
            }
        }
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && PhysiqueBonusHelper.isBasicFiveElementSpell(spell)) {
            multiplier *= 0.9;
        }
        if (p == Physique.HEAVENLY_FIRE_BODY && spell != null && spell.element() == SpellElement.FIRE) {
            multiplier *= 0.8;
        }
        if (p == Physique.MYSTIC_ICE_BODY && spell != null && (spell.element() == SpellElement.WATER || spell.element() == SpellElement.ICE)) {
            multiplier *= 0.8;
        }
        if (p == Physique.BLOOD_FIEND_BODY && spell != null && spell.isBloodSpell()) {
            multiplier *= 0.8;
        }
        return PhysiqueBonusHelper.sanitizeMultiplier(multiplier);
    }

    public static double damageTakenMultiplier(Player player) {
        return PhysiqueBonusHelper.sanitizeMultiplier(PhysiqueBonusHelper.physiqueOf(player).bonus().damageTakenMult());
    }

    public static double maxQiMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return PhysiqueBonusHelper.sanitizeMultiplier(p.bonus().maxQiMult());
    }

    public static double cultivationRequirementMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return p == Physique.CHAOS_BODY ? 10.0 : 1.0;
    }

    public static boolean grantsResistanceRegen(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player).bonus().resistanceRegen();
    }

    public static boolean canCultivate(Player player) {
        return !PhysiqueBonusHelper.physiqueOf(player).bonus().cannotCultivate();
    }

    public static boolean canCultivate(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return !p.bonus().cannotCultivate();
    }

    public static double alchemySuccessChanceBonus(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player) == Physique.ALCHEMY_HEART_BODY ? 0.1 : 0.0;
    }

    public static double alchemyQiCostMultiplier(Player player) {
        return PhysiqueBonusHelper.physiqueOf(player) == Physique.ALCHEMY_HEART_BODY ? 0.5 : 1.0;
    }

    public static int extraZhenyuanPerMinor(Physique physique) {
        return physique == Physique.CHAOS_BODY ? 1 : 0;
    }

    public static void onSpellCast(ServerPlayer player, Spell spell) {
        if (player == null || spell == null) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(data -> {
            boolean chainSuccess;
            if (data.getPhysique() != Physique.INVERSE_FIVE_ELEMENTS_BODY) {
                return;
            }
            QiElement castElement = PhysiqueBonusHelper.fiveElementOf(spell);
            if (castElement == null) {
                return;
            }
            long now = player.level().getGameTime();
            QiElement previous = data.hasActiveInverseFiveElementMark(now) ? data.getInverseFiveElementMark() : null;
            boolean bl = chainSuccess = previous != null && PhysiqueBonusHelper.nextInverseElement(previous) == castElement;
            if (chainSuccess) {
                int stacks = data.getActiveInverseFiveElementStacks(now) + 1;
                data.setInverseFiveElementStacks(stacks, now + 600L);
                player.addEffect(new MobEffectInstance((MobEffect)ModEffects.INVERSE_FIVE_ELEMENTS.get(), 600, Math.max(0, stacks - 1), false, true, true));
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.inverse_five_elements.chain", (Object[])new Object[]{castElement.displayName(), stacks}).withStyle(ChatFormatting.AQUA), true);
            } else if (previous != null) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.inverse_five_elements.break", (Object[])new Object[]{previous.displayName(), castElement.displayName()}).withStyle(ChatFormatting.GRAY), true);
            } else {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.inverse_five_elements.mark", (Object[])new Object[]{castElement.displayName(), PhysiqueBonusHelper.nextInverseElement(castElement).displayName()}).withStyle(ChatFormatting.AQUA), true);
            }
            data.setInverseFiveElementMark(castElement, now + 600L);
            CapabilityEvents.syncToClient(player);
        });
    }

    public static void grantChaosBodyMinorBreakthroughSpell(ServerPlayer player, CultivationData data) {
        if (player == null || data == null || data.getPhysique() != Physique.CHAOS_BODY) {
            return;
        }
        ArrayList<Spell> candidates = new ArrayList<Spell>();
        for (Spell spell : Spell.values()) {
            if (spell == Spell.IMMORTAL_INCANTATION || data.hasSpell(spell)) continue;
            candidates.add(spell);
        }
        if (candidates.isEmpty()) {
            return;
        }
        Spell learned = (Spell)((Object)candidates.get(player.getRandom().nextInt(candidates.size())));
        data.learnSpell(learned);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.chaos_body.learn_spell", (Object[])new Object[]{learned.displayNameForRealm(data.getRealm())}).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    private static boolean isNonSwordElementalSpell(Spell spell) {
        if (spell == null || spell.isSwordSpell()) {
            return false;
        }
        SpellElement element = spell.element();
        return element != null && element != SpellElement.NONE;
    }

    public static boolean isFiveElementQi(QiElement element) {
        return element == QiElement.METAL || element == QiElement.WOOD || element == QiElement.WATER || element == QiElement.FIRE || element == QiElement.EARTH;
    }

    public static QiElement fiveElementOf(Spell spell) {
        if (spell == null) {
            return null;
        }
        SpellElement element = spell.element();
        if (element == null) {
            return null;
        }
        QiElement qi = element.matchingQi();
        return PhysiqueBonusHelper.isFiveElementQi(qi) ? qi : null;
    }

    public static QiElement nextInverseElement(QiElement current) {
        return switch (current) {
            case WOOD -> QiElement.WATER;
            case WATER -> QiElement.METAL;
            case METAL -> QiElement.EARTH;
            case EARTH -> QiElement.FIRE;
            case FIRE -> QiElement.WOOD;
            default -> QiElement.WOOD;
        };
    }

    private static double applySpecialSpellDamageRules(Physique physique, Spell spell, boolean fiveElementChaosCombo, double currentMultiplier) {
        Physique p;
        Physique physique2 = p = physique == null ? Physique.MORTAL_BODY : physique;
        if (p == Physique.FIVE_ELEMENT_CHAOS_BODY && PhysiqueBonusHelper.isBasicFiveElementSpell(spell)) {
            return fiveElementChaosCombo ? 1.5 : 0.5;
        }
        return currentMultiplier;
    }

    private static double applySharedSpellDamageRules(Physique physique, Spell spell, double currentMultiplier) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = currentMultiplier;
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && PhysiqueBonusHelper.isBasicFiveElementSpell(spell)) {
            multiplier *= 1.1;
        }
        if (p == Physique.CHAOS_BODY && spell != null) {
            multiplier *= 1.3;
        }
        if (p == Physique.BLOOD_FIEND_BODY && spell != null && spell.isBloodSpell()) {
            multiplier *= 1.2;
        }
        return multiplier;
    }

    private static double applyPlayerOnlySpellDamageRules(Player player, Physique physique, Spell spell, double currentMultiplier) {
        int stacks;
        double multiplier = PhysiqueBonusHelper.applySharedSpellDamageRules(physique, spell, currentMultiplier);
        if (physique == Physique.INVERSE_FIVE_ELEMENTS_BODY && PhysiqueBonusHelper.isBasicFiveElementSpell(spell) && (stacks = CultivationCapability.get(player).map(data -> data.getActiveInverseFiveElementStacks(player.level().getGameTime())).orElse(0).intValue()) > 0) {
            multiplier *= 1.0 + (double)stacks * 0.25;
        }
        return multiplier;
    }

    public static boolean hasFiveElementChaosBodyCombo(Player player) {
        if (player == null) {
            return false;
        }
        return CultivationCapability.get(player).map(data -> data.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY && RealmTopology.isAtLeast(data.getRealm(), Realm.NASCENT_SOUL) && data.getLearnedTechniques().contains(Technique.FIVE_ELEMENT_CHAOS_ART.id()) && Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(data.getEquippedTechniqueId())).orElse(false);
    }

    public static boolean hasFiveElementChaosBodyCombo(WanderingCultivatorEntity npc) {
        if (npc == null) {
            return false;
        }
        return npc.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY && RealmTopology.isAtLeast(npc.getRealm(), Realm.NASCENT_SOUL) && Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(npc.getTechniqueId());
    }

    private static boolean isBasicFiveElementSpell(Spell spell) {
        return PhysiqueBonusHelper.fiveElementOf(spell) != null;
    }

    private static double sanitizeMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            return 1.0;
        }
        return Math.max(0.0, multiplier);
    }
}

