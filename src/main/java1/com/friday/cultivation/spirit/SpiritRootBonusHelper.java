package com.friday.cultivation.spirit;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.technique.Technique;
import net.minecraft.core.Holder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

/**
 * 灵根加成辅助 — 完整复刻原模组 SpiritRootBonusHelper
 * 包含：元素法术倍率（主/次/克/生五行+混沌组合）、非元素倍率、剑伤倍率、总体法术倍率、
 * 须弥火莲倍率、灵气吸收倍率、法术消耗倍率、五行混沌组合判断、功法学习/装备判断、环境增益（寒地/雨雷）
 */
public final class SpiritRootBonusHelper {

    private SpiritRootBonusHelper() {}

    private static SpiritRoot rootOf(Player player) {
        if (player == null) return SpiritRoot.NONE;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        return ic != null ? ic.getSpiritRoot() : SpiritRoot.NONE;
    }

    private static SpiritRootBonus bonusOf(Player player) {
        return rootOf(player).bonus();
    }

    /** 玩家元素法术倍率（按灵根主/次/克/生五行+混沌组合） */
    public static double spellElementMultiplier(Player player, QiElement element) {
        if (element == null || element == QiElement.PURE) {
            return nonElementSpellMultiplier(player);
        }
        SpiritRoot root = rootOf(player);
        return spellElementMultiplier(root, element, hasChaosCombo(player));
    }

    /** 枚举重载：元素法术倍率 */
    public static double spellElementMultiplier(SpiritRoot root, QiElement element, boolean chaosCombo) {
        if (root == null) root = SpiritRoot.NONE;
        if (element == null || element == QiElement.PURE) {
            return root.bonus().nonElementSpellMult();
        }
        // 五行混沌+组合+基础五行 = 2.0倍
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS && chaosCombo && isFiveElementBasic(element)) {
            return 2.0;
        }
        SpiritRootBonus bonus = root.bonus();
        // 变异冰灵根对水元素按主元素倍率
        if (root == SpiritRoot.MUTANT_ICE && element == QiElement.WATER) {
            return bonus.primaryElementMult();
        }
        if (bonus.primaryElement() == element) return bonus.primaryElementMult();
        if (bonus.secondaryElement() == element) return bonus.secondaryElementMult();
        if (bonus.counterElement() == element) return bonus.counterElementMult();
        // 五行混沌无组合时基础五行=0.5倍
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS && isFiveElementBasic(element)) {
            return 0.5;
        }
        return bonus.offElementMult();
    }

    /** 非元素法术倍率 */
    public static double nonElementSpellMultiplier(Player player) {
        return bonusOf(player).nonElementSpellMult();
    }

    /** 剑类近战伤害倍率（委托体质） */
    public static double swordDamageMultiplier(Player player) {
        return PhysiqueBonusHelper.swordDamageMultiplier(player);
    }

    /** 剑类法术倍率（委托体质） */
    public static double swordSpellMultiplier(Player player, Spell spell) {
        return PhysiqueBonusHelper.swordSpellMultiplier(player, spell);
    }

    /**
     * 灵根总体法术倍率（含须弥火莲特殊处理）
     * = 灵根元素倍率 × 体质法术倍率
     */
    public static double overallSpellMultiplier(Player player, Spell spell) {
        if (player == null || spell == null) return 1.0;
        double base;
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            base = buddhaFireLotusSpellMultiplier(player);
        } else {
            SpellElement spellElement = spell.element();
            if (spellElement != null && spellElement != SpellElement.NONE) {
                QiElement qi = spellElement.matchingQi();
                base = qi != null ? spellElementMultiplier(player, qi) : nonElementSpellMultiplier(player);
            } else {
                base = nonElementSpellMultiplier(player);
            }
        }
        return base * PhysiqueBonusHelper.spellDamageMultiplier(player, spell);
    }

    /** 是否可施放须弥火莲（原模组始终true） */
    public static boolean canCastBuddhaFireLotus(Player player) {
        return true;
    }

    /** 玩家是否拥有指定元素灵根 */
    public static boolean hasRootElement(Player player, QiElement element) {
        return hasRootElement(rootOf(player), element);
    }

    /** 玩家是否拥有纯灵根（隐灵根/五行混沌） */
    public static boolean hasPureRoot(Player player) {
        return hasPureRoot(rootOf(player));
    }

    /**
     * 须弥火莲倍率 = (木元素倍率 + 火元素倍率) / 2，最低0.1
     */
    public static double buddhaFireLotusSpellMultiplier(Player player) {
        if (player == null) return 1.0;
        double total = spellElementMultiplier(player, QiElement.WOOD) + spellElementMultiplier(player, QiElement.FIRE);
        return Math.max(0.1, total / 2.0);
    }

    /** 枚举重载：是否拥有指定元素灵根（三/四/五灵根含全五行） */
    public static boolean hasRootElement(SpiritRoot root, QiElement element) {
        if (root == null || element == null) return false;
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS || root == SpiritRoot.TRIPLE
                || root == SpiritRoot.QUADRUPLE || root == SpiritRoot.FIVE_ROOT) {
            return isFiveElementBasic(element);
        }
        SpiritRootBonus bonus = root.bonus();
        return bonus.primaryElement() == element || bonus.secondaryElement() == element;
    }

    /** 枚举重载：是否纯灵根（隐灵根/五行混沌） */
    public static boolean hasPureRoot(SpiritRoot root) {
        return root == SpiritRoot.HEAVENLY_HIDDEN || root == SpiritRoot.FIVE_ELEMENT_CHAOS;
    }

    /** 近战伤害倍率（委托体质） */
    public static double meleeDamageMultiplier(Player player) {
        return PhysiqueBonusHelper.meleeDamageMultiplier(player);
    }

    /** HP倍率（委托体质） */
    public static double hpMultiplier(Player player) {
        return PhysiqueBonusHelper.hpMultiplier(player);
    }

    /** HP加成（委托体质） */
    public static double hpBonus(Player player) {
        return PhysiqueBonusHelper.hpBonus(player);
    }

    /** 是否可修炼（委托体质） */
    public static boolean canCultivate(Player player) {
        return PhysiqueBonusHelper.canCultivate(player);
    }

    /**
     * 灵气吸收倍率（按灵根稀有度）：
     * SSR 1.5 / SR 1.25 / R 四灵根0.9其他1.0 / NORMAL 五行混沌0.75五灵根0.8其他1.0
     * 变异冰/雷 环境加成 ×1.5（寒地/雨雷）
     */
    public static double qiAbsorptionMultiplier(Player player) {
        if (player == null) return 1.0;
        SpiritRoot root = rootOf(player);
        double mult = switch (root.rarity()) {
            case SSR -> 1.5;
            case SR -> 1.25;
            case R -> root == SpiritRoot.QUADRUPLE ? 0.9 : 1.0;
            case NORMAL -> root == SpiritRoot.FIVE_ELEMENT_CHAOS ? 0.75 : (root == SpiritRoot.FIVE_ROOT ? 0.8 : 1.0);
            default -> 1.0;
        };
        if (root == SpiritRoot.MUTANT_ICE && isColdBiome(player)) {
            mult *= 1.5;
        } else if (root == SpiritRoot.MUTANT_LIGHTNING && isRainOrThunder(player)) {
            mult *= 1.5;
        }
        return mult;
    }

    /** 玩家法术灵气消耗倍率 */
    public static double spellQiCostMultiplier(Player player, Spell spell) {
        if (player == null || spell == null) return 1.0;
        return spellQiCostMultiplier(rootOf(player), spell);
    }

    /** 枚举重载：法术灵气消耗倍率（须弥火莲取木火均值） */
    public static double spellQiCostMultiplier(SpiritRoot root, Spell spell) {
        if (spell == null) return 1.0;
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            double wood = spellQiCostMultiplierForElement(root, QiElement.WOOD);
            double fire = spellQiCostMultiplierForElement(root, QiElement.FIRE);
            return Math.max(0.1, (wood + fire) / 2.0);
        }
        SpellElement spellElement = spell.element();
        QiElement element = (spellElement == null || spellElement == SpellElement.NONE) ? QiElement.PURE : spellElement.matchingQi();
        if (element == null) element = QiElement.PURE;
        return spellQiCostMultiplierForElement(root, element);
    }

    /** 灵根对指定元素的灵气消耗倍率（克制2.0/主元素SSR0.7其他0.9/次0.9/变异0.8/隐0.5/其他1.0） */
    private static double spellQiCostMultiplierForElement(SpiritRoot root, QiElement element) {
        if (root == null) root = SpiritRoot.NONE;
        if (element == QiElement.PURE) {
            return root == SpiritRoot.HEAVENLY_HIDDEN ? 0.5 : 1.0;
        }
        SpiritRootBonus bonus = root.bonus();
        if (bonus.counterElement() == element) return 2.0;
        if (bonus.primaryElement() == element) {
            return root.rarity() == SpiritRoot.Rarity.SSR ? 0.7 : 0.9;
        }
        if (bonus.secondaryElement() == element) return 0.9;
        if (root == SpiritRoot.MUTANT_ICE && element == QiElement.WATER) return 0.8;
        if (root == SpiritRoot.MUTANT_LIGHTNING && element == QiElement.LIGHTNING) return 0.8;
        return 1.0;
    }

    /**
     * 五行混沌组合判断：五行混沌灵根 + 已学且装备五行混沌功法 + 元婴以上境界
     */
    public static boolean hasChaosCombo(Player player) {
        if (player == null) return false;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return false;
        if (ic.getSpiritRoot() != SpiritRoot.FIVE_ELEMENT_CHAOS) return false;
        if (!ic.getLearnedTechniques().contains(Technique.FIVE_ELEMENT_CHAOS_ART.id())) return false;
        if (!Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(ic.getEquippedTechniqueId())) return false;
        return ic.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal();
    }

    /** 是否五行基础元素（金木水火土） */
    private static boolean isFiveElementBasic(QiElement element) {
        return element == QiElement.METAL || element == QiElement.WOOD
                || element == QiElement.WATER || element == QiElement.FIRE || element == QiElement.EARTH;
    }

    /** 寒地生物群系判断（温度<0.2 即为寒地） */
    private static boolean isColdBiome(Player player) {
        if (player == null || player.level() == null) return false;
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        return biome.value().getBaseTemperature() < 0.2f;
    }

    /** 雨天或雷暴判断 */
    private static boolean isRainOrThunder(Player player) {
        return player != null && player.level() != null
                && (player.level().isRaining() || player.level().isThundering());
    }

    /** 是否可学习功法（青帝长生需木灵根或炼体境；五行混沌功法需五行混沌灵根/体+元婴） */
    public static boolean canLearnTechnique(Player player, Technique technique) {
        if (player == null || technique == null) return true;
        if (technique == Technique.QINGDI_LONGEVITY) {
            return hasWoodRootOrBodyIntegration(player);
        }
        if (technique != Technique.FIVE_ELEMENT_CHAOS_ART) return true;
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return false;
        boolean hasChaosAptitude = ic.getSpiritRoot() == SpiritRoot.FIVE_ELEMENT_CHAOS
                || ic.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY;
        if (!hasChaosAptitude) return false;
        return ic.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal();
    }

    /** 是否可装备功法（同可学习判断） */
    public static boolean canEquipTechnique(Player player, Technique technique) {
        return canLearnTechnique(player, technique);
    }

    /** 有木灵根或炼体境以上（青帝长生功法条件） */
    private static boolean hasWoodRootOrBodyIntegration(Player player) {
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic == null) return false;
        if (ic.getRealm().ordinal() >= Realm.BODY_INTEGRATION.ordinal()) return true;
        SpiritRoot root = ic.getSpiritRoot();
        if (root == SpiritRoot.HEAVENLY_WOOD || root == SpiritRoot.FIVE_ELEMENT_CHAOS
                || root == SpiritRoot.TRIPLE || root == SpiritRoot.QUADRUPLE) {
            return true;
        }
        SpiritRootBonus bonus = root.bonus();
        return bonus.primaryElement() == QiElement.WOOD || bonus.secondaryElement() == QiElement.WOOD;
    }
}
