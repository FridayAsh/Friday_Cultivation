package com.friday.cultivation.physique;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.technique.Technique;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;

import java.util.ArrayList;

/**
 * 体质加成辅助 — 完整复刻原模组 PhysiqueBonusHelper
 * 包含：法术伤害倍率（玩家/NPC/枚举重载）、剑法倍率、近战倍率、HP倍率、灵气吸收、灵气消耗、受伤倍率、
 * 修炼限制、炼丹加成、逆五行连锁、混沌体突破学法术等全部方法
 */
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

    private PhysiqueBonusHelper() {}

    private static Physique physiqueOf(Player player) {
        if (player == null) return Physique.MORTAL_BODY;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic.getPhysique() : Physique.MORTAL_BODY;
    }

    /** 玩家体质法术伤害倍率（含特殊规则 + 玩家专属逆五行stack加成） */
    public static double spellDamageMultiplier(Player player, Spell spell) {
        Physique physique = physiqueOf(player);
        double multiplier = physique.bonus().spellMultiplier(spell);
        multiplier = applySpecialSpellDamageRules(physique, spell, hasFiveElementChaosBodyCombo(player), multiplier);
        multiplier = applyPlayerOnlySpellDamageRules(player, physique, spell, multiplier);
        return Math.max(0.0, multiplier);
    }

    /** 枚举重载：体质法术伤害倍率（不含玩家专属规则，含NPC共享规则） */
    public static double spellDamageMultiplier(Physique physique, Spell spell) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = p.bonus().spellMultiplier(spell);
        multiplier = applySpecialSpellDamageRules(p, spell, false, multiplier);
        multiplier = applySharedSpellDamageRules(p, spell, multiplier);
        if (p == Physique.INNATE_SWORD_BODY && isNonSwordElementalSpell(spell)) {
            multiplier *= 0.2;
        }
        return Math.max(0.0, multiplier);
    }

    /** NPC体质法术伤害倍率 */
    public static double spellDamageMultiplier(WanderingCultivatorEntity npc, Spell spell) {
        if (npc == null) return spellDamageMultiplier(Physique.MORTAL_BODY, spell);
        Physique physique = npc.getPhysique();
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = p.bonus().spellMultiplier(spell);
        multiplier = applySpecialSpellDamageRules(p, spell, hasFiveElementChaosBodyCombo(npc), multiplier);
        multiplier = applySharedSpellDamageRules(p, spell, multiplier);
        if (p == Physique.INNATE_SWORD_BODY && isNonSwordElementalSpell(spell)) {
            multiplier *= 0.2;
        }
        return Math.max(0.0, multiplier);
    }

    /** 剑类法术倍率（仅剑类法术有效） */
    public static double swordSpellMultiplier(Player player, Spell spell) {
        if (player == null || spell == null || !spell.isSwordSpell()) return 1.0;
        return physiqueOf(player).bonus().swordSpellMult();
    }

    /** 剑类近战伤害倍率（主手为剑时生效） */
    public static double swordDamageMultiplier(Player player) {
        if (player == null) return 1.0;
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) return 1.0;
        return physiqueOf(player).bonus().swordSpellMult();
    }

    /** 近战伤害倍率 */
    public static double meleeDamageMultiplier(Player player) {
        return physiqueOf(player).bonus().meleeDmgMult();
    }

    /** HP倍率 */
    public static double hpMultiplier(Player player) {
        return physiqueOf(player).bonus().hpMult();
    }

    /** HP加成（固定值 + 倍率基于基础值） */
    public static double hpBonus(Player player) {
        if (player == null) return 0.0;
        Physique physique = physiqueOf(player);
        double mult = hpMultiplier(player);
        double flat = physique.bonus().maxHpBonus();
        AttributeInstance attr = player.getAttribute(Attributes.MAX_HEALTH);
        double base = attr != null ? attr.getBaseValue() : 20.0;
        return flat + (mult > 1.0 ? (mult - 1.0) * base : 0.0);
    }

    /** 灵气吸收范围加成 */
    public static int qiAbsorbRangeBonus(Player player) {
        return physiqueOf(player).bonus().qiAbsorbRange();
    }

    /** 灵气吸收倍率 */
    public static double qiAbsorbMultiplier(Player player) {
        return physiqueOf(player).bonus().qiAbsorbMult();
    }

    /** 灵气吸收倍率（按元素，含逆五行加成） */
    public static double qiAbsorbMultiplierForElement(Player player, QiElement element) {
        double multiplier = qiAbsorbMultiplier(player);
        return sanitizeMultiplier(multiplier * qiAbsorbElementMultiplier(physiqueOf(player), element));
    }

    /** 灵气吸收倍率（枚举重载，按元素） */
    public static double qiAbsorbMultiplierForElement(Physique physique, QiElement element) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = p.bonus().qiAbsorbMult();
        return sanitizeMultiplier(multiplier * qiAbsorbElementMultiplier(p, element));
    }

    /** 灵气吸收元素倍率（玩家） */
    public static double qiAbsorbElementMultiplier(Player player, QiElement element) {
        return qiAbsorbElementMultiplier(physiqueOf(player), element);
    }

    /** 灵气吸收元素倍率（玩家，根包 QiElement 适配——照搬原模组 cultivation.QiElement 语义） */
    public static double qiAbsorbElementMultiplier(Player player, com.friday.cultivation.QiElement element) {
        return qiAbsorbElementMultiplier(physiqueOf(player), element);
    }

    /** 灵气吸收元素倍率（枚举）：逆五行体吸收五行+15% */
    public static double qiAbsorbElementMultiplier(Physique physique, QiElement element) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && isFiveElementQi(element)) {
            return 1.15;
        }
        return 1.0;
    }

    /** 灵气吸收元素倍率（枚举，根包 QiElement 适配——照搬原模组 cultivation.QiElement 语义） */
    public static double qiAbsorbElementMultiplier(Physique physique, com.friday.cultivation.QiElement element) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && isFiveElementQi(element)) {
            return 1.15;
        }
        return 1.0;
    }

    /** 通用灵气消耗倍率（玩家） */
    public static double generalQiCostMultiplier(Player player) {
        return generalQiCostMultiplier(physiqueOf(player));
    }

    /** 通用灵气消耗倍率（枚举） */
    public static double generalQiCostMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return sanitizeMultiplier(p.bonus().qiCostMult());
    }

    /** 法术灵气消耗倍率（玩家，含逆五行stack减耗） */
    public static double spellQiCostMultiplier(Player player, Spell spell) {
        Physique physique = physiqueOf(player);
        double multiplier = spellQiCostMultiplier(physique, spell);
        if (physique == Physique.INVERSE_FIVE_ELEMENTS_BODY && isBasicFiveElementSpell(spell)) {
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                int stacks = ic.getActiveInverseFiveElementStacks(player.level().getGameTime());
                if (stacks > 0) {
                    multiplier *= Math.max(0.0, 1.0 - stacks * INVERSE_STACK_COST_REDUCTION_PER_LAYER);
                }
            }
        }
        return sanitizeMultiplier(multiplier);
    }

    /** 法术灵气消耗倍率（枚举，按体质+法术元素） */
    public static double spellQiCostMultiplier(Physique physique, Spell spell) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = 1.0;
        if (p == Physique.INNATE_SWORD_BODY) {
            if (spell != null && spell.isSwordSpell()) {
                multiplier *= 0.5;
            } else if (isNonSwordElementalSpell(spell)) {
                multiplier *= 2.0;
            }
        }
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && isBasicFiveElementSpell(spell)) {
            multiplier *= INVERSE_BASE_FIVE_ELEMENT_COST_MULTIPLIER;
        }
        if (p == Physique.HEAVENLY_FIRE_BODY && spell != null && spell.element() == SpellElement.FIRE) {
            multiplier *= FIRE_BODY_COST_MULTIPLIER;
        }
        if (p == Physique.MYSTIC_ICE_BODY && spell != null
                && (spell.element() == SpellElement.WATER || spell.element() == SpellElement.ICE)) {
            multiplier *= ICE_BODY_COST_MULTIPLIER;
        }
        if (p == Physique.BLOOD_FIEND_BODY && spell != null && spell.isBloodSpell()) {
            multiplier *= BLOOD_FIEND_BLOOD_SPELL_COST_MULTIPLIER;
        }
        return sanitizeMultiplier(multiplier);
    }

    /** 受伤倍率 */
    public static double damageTakenMultiplier(Player player) {
        return sanitizeMultiplier(physiqueOf(player).bonus().damageTakenMult());
    }

    /** 最大灵气倍率（枚举） */
    public static double maxQiMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return sanitizeMultiplier(p.bonus().maxQiMult());
    }

    /** 修炼需求倍率（混沌体10倍） */
    public static double cultivationRequirementMultiplier(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return p == Physique.CHAOS_BODY ? CHAOS_BODY_CULTIVATION_REQUIREMENT_MULTIPLIER : 1.0;
    }

    /** 是否提供抗性回血 */
    public static boolean grantsResistanceRegen(Player player) {
        return physiqueOf(player).bonus().resistanceRegen();
    }

    /** 是否可修炼 */
    public static boolean canCultivate(Player player) {
        return !physiqueOf(player).bonus().cannotCultivate();
    }

    /** 是否可修炼（枚举） */
    public static boolean canCultivate(Physique physique) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        return !p.bonus().cannotCultivate();
    }

    /** 炼丹成功率加成（丹心体+10%） */
    public static double alchemySuccessChanceBonus(Player player) {
        return physiqueOf(player) == Physique.ALCHEMY_HEART_BODY ? ALCHEMY_HEART_SUCCESS_BONUS : 0.0;
    }

    /** 炼丹灵气消耗倍率（丹心体0.5倍） */
    public static double alchemyQiCostMultiplier(Player player) {
        return physiqueOf(player) == Physique.ALCHEMY_HEART_BODY ? ALCHEMY_HEART_QI_COST_MULTIPLIER : 1.0;
    }

    /** 每小境界额外真元（混沌体+1） */
    public static int extraZhenyuanPerMinor(Physique physique) {
        return physique == Physique.CHAOS_BODY ? 1 : 0;
    }

    /** 施法时触发（逆五行体连锁判断 + 标记更新） */
    public static void onSpellCast(ServerPlayer player, Spell spell) {
        if (player == null || spell == null) return;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return;
        if (ic.getPhysique() != Physique.INVERSE_FIVE_ELEMENTS_BODY) return;
        QiElement castElement = fiveElementOf(spell);
        if (castElement == null) return;
        long now = player.level().getGameTime();
        QiElement previous = ic.hasActiveInverseFiveElementMark(now) ? ic.getInverseFiveElementMark() : null;
        boolean chainSuccess = previous != null && nextInverseElement(previous) == castElement;
        if (chainSuccess) {
            int stacks = ic.getActiveInverseFiveElementStacks(now) + 1;
            ic.setInverseFiveElementStacks(stacks, now + INVERSE_MARK_DURATION_TICKS);
            player.addEffect(new MobEffectInstance(ModEffects.INVERSE_FIVE_ELEMENTS.get(), INVERSE_MARK_DURATION_TICKS, Math.max(0, stacks - 1), false, true, true));
            player.displayClientMessage(Component.translatable("message.friday_cultivation.inverse_five_elements.chain", castElement.displayName(), stacks).withStyle(ChatFormatting.AQUA), true);
        } else if (previous != null) {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.inverse_five_elements.break", previous.displayName(), castElement.displayName()).withStyle(ChatFormatting.GRAY), true);
        } else {
            player.displayClientMessage(Component.translatable("message.friday_cultivation.inverse_five_elements.mark", castElement.displayName(), nextInverseElement(castElement).displayName()).withStyle(ChatFormatting.AQUA), true);
        }
        ic.setInverseFiveElementMark(castElement, now + INVERSE_MARK_DURATION_TICKS);
        CapabilityEvents.syncToClient(player);
    }

    /** 混沌体小境界突破时随机学习一个未学法术 */
    public static void grantChaosBodyMinorBreakthroughSpell(ServerPlayer player, CultivationData data) {
        if (player == null || data == null || data.getPhysique() != Physique.CHAOS_BODY) return;
        ArrayList<Spell> candidates = new ArrayList<>();
        for (Spell spell : Spell.values()) {
            if (spell == Spell.IMMORTAL_INCANTATION || data.hasSpell(spell)) continue;
            candidates.add(spell);
        }
        if (candidates.isEmpty()) return;
        Spell learned = candidates.get(player.getRandom().nextInt(candidates.size()));
        data.learnSpell(learned.id());
        player.displayClientMessage(Component.translatable("message.friday_cultivation.chaos_body.learn_spell", learned.displayNameForRealm(data.getRealm())).withStyle(ChatFormatting.LIGHT_PURPLE), false);
    }

    // ── 私有辅助 ──

    private static boolean isNonSwordElementalSpell(Spell spell) {
        if (spell == null || spell.isSwordSpell()) return false;
        SpellElement element = spell.element();
        return element != null && element != SpellElement.NONE;
    }

    public static boolean isFiveElementQi(QiElement element) {
        return element == QiElement.METAL || element == QiElement.WOOD
                || element == QiElement.WATER || element == QiElement.FIRE || element == QiElement.EARTH;
    }

    /** 五行判断（根包 QiElement 适配——照搬原模组 cultivation.QiElement 语义） */
    public static boolean isFiveElementQi(com.friday.cultivation.QiElement element) {
        return element == com.friday.cultivation.QiElement.METAL || element == com.friday.cultivation.QiElement.WOOD
                || element == com.friday.cultivation.QiElement.WATER || element == com.friday.cultivation.QiElement.FIRE
                || element == com.friday.cultivation.QiElement.EARTH;
    }

    public static QiElement fiveElementOf(Spell spell) {
        if (spell == null) return null;
        SpellElement element = spell.element();
        if (element == null) return null;
        QiElement qi = element.matchingQi();
        return isFiveElementQi(qi) ? qi : null;
    }

    /** 逆五行相生顺序：金→水→木→火→土→金 */
    public static QiElement nextInverseElement(QiElement current) {
        if (current == null) return QiElement.WOOD;
        return switch (current) {
            case METAL -> QiElement.WATER;
            case WATER -> QiElement.WOOD;
            case WOOD -> QiElement.FIRE;
            case FIRE -> QiElement.EARTH;
            case EARTH -> QiElement.METAL;
            default -> QiElement.WOOD;
        };
    }

    private static double applySpecialSpellDamageRules(Physique physique, Spell spell, boolean fiveElementChaosCombo, double currentMultiplier) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        if (p == Physique.FIVE_ELEMENT_CHAOS_BODY && isBasicFiveElementSpell(spell)) {
            return fiveElementChaosCombo ? 1.5 : 0.5;
        }
        return currentMultiplier;
    }

    private static double applySharedSpellDamageRules(Physique physique, Spell spell, double currentMultiplier) {
        Physique p = physique == null ? Physique.MORTAL_BODY : physique;
        double multiplier = currentMultiplier;
        if (p == Physique.INVERSE_FIVE_ELEMENTS_BODY && isBasicFiveElementSpell(spell)) {
            multiplier *= INVERSE_BASE_FIVE_ELEMENT_MULTIPLIER;
        }
        if (p == Physique.CHAOS_BODY && spell != null) {
            multiplier *= CHAOS_BODY_SPELL_DAMAGE_MULTIPLIER;
        }
        if (p == Physique.BLOOD_FIEND_BODY && spell != null && spell.isBloodSpell()) {
            multiplier *= BLOOD_FIEND_BLOOD_SPELL_MULTIPLIER;
        }
        return multiplier;
    }

    private static double applyPlayerOnlySpellDamageRules(Player player, Physique physique, Spell spell, double currentMultiplier) {
        double multiplier = applySharedSpellDamageRules(physique, spell, currentMultiplier);
        if (physique == Physique.INVERSE_FIVE_ELEMENTS_BODY && isBasicFiveElementSpell(spell)) {
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                int stacks = ic.getActiveInverseFiveElementStacks(player.level().getGameTime());
                if (stacks > 0) {
                    multiplier *= 1.0 + stacks * INVERSE_STACK_DAMAGE_PER_LAYER;
                }
            }
        }
        return multiplier;
    }

    /** 玩家是否拥有五行混沌体组合（混沌体+元婴以上+已学且装备五行混沌功法） */
    public static boolean hasFiveElementChaosBodyCombo(Player player) {
        if (player == null) return false;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return false;
        return ic.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY
                && ic.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal()
                && ic.getLearnedTechniques().contains(Technique.FIVE_ELEMENT_CHAOS_ART.id())
                && Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(ic.getEquippedTechniqueId());
    }

    /** NPC是否拥有五行混沌体组合 */
    public static boolean hasFiveElementChaosBodyCombo(WanderingCultivatorEntity npc) {
        if (npc == null) return false;
        return npc.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY
                && npc.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal()
                && Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(npc.getTechniqueId());
    }

    private static boolean isBasicFiveElementSpell(Spell spell) {
        return fiveElementOf(spell) != null;
    }

    private static double sanitizeMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) return 1.0;
        return Math.max(0.0, multiplier);
    }
}
