/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.Holder
 *  net.minecraft.tags.BiomeTags
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.biome.Biome
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.SpiritRootBonus;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.cultivation.technique.Technique;
import net.minecraft.core.Holder;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.biome.Biome;

public final class SpiritRootBonusHelper {
    private SpiritRootBonusHelper() {
    }

    private static SpiritRoot rootOf(Player player) {
        if (player == null) {
            return SpiritRoot.NONE;
        }
        return CultivationCapability.get(player).map(CultivationData::getSpiritRoot).orElse(SpiritRoot.NONE);
    }

    private static SpiritRootBonus bonusOf(Player player) {
        return SpiritRootBonusHelper.rootOf(player).bonus();
    }

    public static double spellElementMultiplier(Player player, QiElement element) {
        if (element == null || element == QiElement.PURE) {
            return SpiritRootBonusHelper.nonElementSpellMultiplier(player);
        }
        SpiritRoot root = SpiritRootBonusHelper.rootOf(player);
        return SpiritRootBonusHelper.spellElementMultiplier(root, element, SpiritRootBonusHelper.hasChaosCombo(player));
    }

    public static double spellElementMultiplier(SpiritRoot root, QiElement element, boolean chaosCombo) {
        if (root == null) {
            root = SpiritRoot.NONE;
        }
        if (element == null || element == QiElement.PURE) {
            return root.bonus().nonElementSpellMult();
        }
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS && chaosCombo && SpiritRootBonusHelper.isFiveElementBasic(element)) {
            return 2.0;
        }
        SpiritRootBonus bonus = root.bonus();
        if (root == SpiritRoot.MUTANT_ICE && element == QiElement.WATER) {
            return bonus.primaryElementMult();
        }
        if (bonus.primaryElement() == element) {
            return bonus.primaryElementMult();
        }
        if (bonus.secondaryElement() == element) {
            return bonus.secondaryElementMult();
        }
        if (bonus.counterElement() == element) {
            return bonus.counterElementMult();
        }
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS && SpiritRootBonusHelper.isFiveElementBasic(element)) {
            return 0.5;
        }
        return bonus.offElementMult();
    }

    public static double nonElementSpellMultiplier(Player player) {
        return SpiritRootBonusHelper.bonusOf(player).nonElementSpellMult();
    }

    public static double swordDamageMultiplier(Player player) {
        return PhysiqueBonusHelper.swordDamageMultiplier(player);
    }

    public static double swordSpellMultiplier(Player player, Spell spell) {
        return PhysiqueBonusHelper.swordSpellMultiplier(player, spell);
    }

    public static double overallSpellMultiplier(Player player, Spell spell) {
        QiElement qi;
        SpellElement spellElement;
        if (player == null || spell == null) {
            return 1.0;
        }
        double base = spell == Spell.BUDDHA_FIRE_LOTUS ? SpiritRootBonusHelper.buddhaFireLotusSpellMultiplier(player) : ((spellElement = spell.element()) != null && spellElement != SpellElement.NONE ? ((qi = spellElement.matchingQi()) != null ? SpiritRootBonusHelper.spellElementMultiplier(player, qi) : SpiritRootBonusHelper.nonElementSpellMultiplier(player)) : SpiritRootBonusHelper.nonElementSpellMultiplier(player));
        return base * PhysiqueBonusHelper.spellDamageMultiplier(player, spell);
    }

    public static boolean canCastBuddhaFireLotus(Player player) {
        return true;
    }

    public static boolean hasRootElement(Player player, QiElement element) {
        return SpiritRootBonusHelper.hasRootElement(SpiritRootBonusHelper.rootOf(player), element);
    }

    public static boolean hasPureRoot(Player player) {
        return SpiritRootBonusHelper.hasPureRoot(SpiritRootBonusHelper.rootOf(player));
    }

    public static double buddhaFireLotusSpellMultiplier(Player player) {
        if (player == null) {
            return 1.0;
        }
        double total = SpiritRootBonusHelper.spellElementMultiplier(player, QiElement.WOOD) + SpiritRootBonusHelper.spellElementMultiplier(player, QiElement.FIRE);
        return Math.max(0.1, total / 2.0);
    }

    public static boolean hasRootElement(SpiritRoot root, QiElement element) {
        if (root == null || element == null) {
            return false;
        }
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS || root == SpiritRoot.TRIPLE || root == SpiritRoot.QUADRUPLE || root == SpiritRoot.FIVE_ROOT) {
            return SpiritRootBonusHelper.isFiveElementBasic(element);
        }
        SpiritRootBonus bonus = root.bonus();
        return bonus.primaryElement() == element || bonus.secondaryElement() == element;
    }

    public static boolean hasPureRoot(SpiritRoot root) {
        return root == SpiritRoot.HEAVENLY_HIDDEN || root == SpiritRoot.FIVE_ELEMENT_CHAOS;
    }

    public static double meleeDamageMultiplier(Player player) {
        return PhysiqueBonusHelper.meleeDamageMultiplier(player);
    }

    public static double hpMultiplier(Player player) {
        return PhysiqueBonusHelper.hpMultiplier(player);
    }

    public static double hpBonus(Player player) {
        return PhysiqueBonusHelper.hpBonus(player);
    }

    public static boolean canCultivate(Player player) {
        return PhysiqueBonusHelper.canCultivate(player);
    }

    public static double qiAbsorptionMultiplier(Player player) {
        if (player == null) {
            return 1.0;
        }
        SpiritRoot root = SpiritRootBonusHelper.rootOf(player);
        double mult = switch (root.rarity()) {
            case SSR -> 1.5;
            case SR -> 1.25;
            case R -> root == SpiritRoot.QUADRUPLE ? 0.9 : 1.0;
            case SPECIAL -> root == SpiritRoot.FIVE_ELEMENT_CHAOS ? 0.75 : 1.0;
            case NORMAL -> root == SpiritRoot.FIVE_ROOT ? 0.8 : 1.0;
        };
        if (root == SpiritRoot.MUTANT_ICE && SpiritRootBonusHelper.isColdBiome(player)) {
            mult *= 1.5;
        } else if (root == SpiritRoot.MUTANT_LIGHTNING && SpiritRootBonusHelper.isRainOrThunder(player)) {
            mult *= 1.5;
        }
        return mult;
    }

    public static double spellQiCostMultiplier(Player player, Spell spell) {
        if (player == null || spell == null) {
            return 1.0;
        }
        return SpiritRootBonusHelper.spellQiCostMultiplier(SpiritRootBonusHelper.rootOf(player), spell);
    }

    public static double spellQiCostMultiplier(SpiritRoot root, Spell spell) {
        QiElement element;
        if (spell == null) {
            return 1.0;
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            double wood = SpiritRootBonusHelper.spellQiCostMultiplierForElement(root, QiElement.WOOD);
            double fire = SpiritRootBonusHelper.spellQiCostMultiplierForElement(root, QiElement.FIRE);
            return Math.max(0.1, (wood + fire) / 2.0);
        }
        SpellElement spellElement = spell.element();
        QiElement qiElement = element = spellElement == null || spellElement == SpellElement.NONE ? QiElement.PURE : spellElement.matchingQi();
        if (element == null) {
            element = QiElement.PURE;
        }
        return SpiritRootBonusHelper.spellQiCostMultiplierForElement(root, element);
    }

    private static double spellQiCostMultiplierForElement(SpiritRoot root, QiElement element) {
        if (root == null) {
            root = SpiritRoot.NONE;
        }
        if (element == QiElement.PURE) {
            return root == SpiritRoot.HEAVENLY_HIDDEN ? 0.5 : 1.0;
        }
        SpiritRootBonus bonus = root.bonus();
        if (bonus.counterElement() == element) {
            return 2.0;
        }
        if (bonus.primaryElement() == element) {
            return root.rarity() == SpiritRoot.Rarity.SSR ? 0.7 : 0.9;
        }
        if (bonus.secondaryElement() == element) {
            return 0.9;
        }
        if (root == SpiritRoot.MUTANT_ICE && element == QiElement.WATER) {
            return 0.8;
        }
        if (root == SpiritRoot.MUTANT_LIGHTNING && element == QiElement.LIGHTNING) {
            return 0.8;
        }
        return 1.0;
    }

    public static boolean hasChaosCombo(Player player) {
        if (player == null) {
            return false;
        }
        return CultivationCapability.get(player).map(data -> {
            if (data.getSpiritRoot() != SpiritRoot.FIVE_ELEMENT_CHAOS) {
                return false;
            }
            if (!data.getLearnedTechniques().contains(Technique.FIVE_ELEMENT_CHAOS_ART.id())) {
                return false;
            }
            if (!Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(data.getEquippedTechniqueId())) {
                return false;
            }
            return data.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal();
        }).orElse(false);
    }

    private static boolean isFiveElementBasic(QiElement element) {
        return element == QiElement.METAL || element == QiElement.WOOD || element == QiElement.WATER || element == QiElement.FIRE || element == QiElement.EARTH;
    }

    private static boolean isColdBiome(Player player) {
        if (player == null || player.level() == null) {
            return false;
        }
        Holder<Biome> biome = player.level().getBiome(player.blockPosition());
        return biome.value().getBaseTemperature() < 0.2f || biome.is(BiomeTags.IS_TAIGA);
    }

    private static boolean isRainOrThunder(Player player) {
        return player != null && player.level() != null && (player.level().isRaining() || player.level().isThundering());
    }

    public static boolean canLearnTechnique(Player player, Technique technique) {
        if (player == null || technique == null) {
            return true;
        }
        if (technique == Technique.QINGDI_LONGEVITY) {
            return SpiritRootBonusHelper.hasWoodRootOrBodyIntegration(player);
        }
        if (technique != Technique.FIVE_ELEMENT_CHAOS_ART) {
            return true;
        }
        return CultivationCapability.get(player).map(data -> {
            boolean hasChaosAptitude;
            boolean bl = hasChaosAptitude = data.getSpiritRoot() == SpiritRoot.FIVE_ELEMENT_CHAOS || data.getPhysique() == Physique.FIVE_ELEMENT_CHAOS_BODY;
            if (!hasChaosAptitude) {
                return false;
            }
            return data.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal();
        }).orElse(false);
    }

    public static boolean canEquipTechnique(Player player, Technique technique) {
        return SpiritRootBonusHelper.canLearnTechnique(player, technique);
    }

    private static boolean hasWoodRootOrBodyIntegration(Player player) {
        return CultivationCapability.get(player).map(data -> {
            if (data.getRealm().ordinal() >= Realm.BODY_INTEGRATION.ordinal()) {
                return true;
            }
            SpiritRoot root = data.getSpiritRoot();
            if (root == SpiritRoot.HEAVENLY_WOOD || root == SpiritRoot.FIVE_ELEMENT_CHAOS || root == SpiritRoot.TRIPLE || root == SpiritRoot.QUADRUPLE) {
                return true;
            }
            SpiritRootBonus bonus = root.bonus();
            return bonus.primaryElement() == QiElement.WOOD || bonus.secondaryElement() == QiElement.WOOD;
        }).orElse(false);
    }
}

