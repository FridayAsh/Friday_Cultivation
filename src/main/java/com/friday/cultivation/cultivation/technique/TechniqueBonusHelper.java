/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 */
package com.friday.cultivation.cultivation.technique;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.WeaponBonusHelper;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public final class TechniqueBonusHelper {
    private TechniqueBonusHelper() {
    }

    public static Technique equippedOf(Player player) {
        if (player == null) {
            return null;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null || !data.hasEquippedTechnique()) {
            return null;
        }
        return Technique.byId(data.getEquippedTechniqueId());
    }

    public static Technique.Bonus bonusOf(Player player) {
        Technique t = TechniqueBonusHelper.equippedOf(player);
        return t == null ? Technique.Bonus.NONE : t.bonus();
    }

    public static int attackBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).attack;
    }

    public static int defenseBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).defense;
    }

    public static int critRateBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).critRate;
    }

    public static int maxHpBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).maxHp;
    }

    public static int qiAbsorbRangeBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).qiAbsorbRange + PhysiqueBonusHelper.qiAbsorbRangeBonus(player);
    }

    public static int refiningBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).refining;
    }

    public static int alchemyBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).alchemy;
    }

    public static int autoRegenPerMinute(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).autoRegenPerMinute;
    }

    public static int undeadBonusDamage(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).undeadBonusDamage;
    }

    public static double qiAbsorbMultiplier(Player player) {
        double t = TechniqueBonusHelper.bonusOf((Player)player).qiAbsorbMult * PhysiqueBonusHelper.qiAbsorbMultiplier(player);
        if (TechniqueBonusHelper.photosynthesisActive(player)) {
            t *= 1.5;
        }
        return t;
    }

    public static double moveSpeedBonus(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).moveSpeed;
    }

    public static double fallDamageReduce(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).fallDamageReduce;
    }

    public static double spellElementMultiplier(Player player, QiElement el) {
        return TechniqueBonusHelper.bonusOf(player).spellMultFor(el);
    }

    public static double spellDamageMultiplier(Player player, Spell spell) {
        QiElement qiElement;
        if (spell == null) {
            return 1.0;
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            return Math.max(0.1, (TechniqueBonusHelper.spellElementMultiplier(player, QiElement.WOOD) + TechniqueBonusHelper.spellElementMultiplier(player, QiElement.FIRE)) / 2.0);
        }
        SpellElement element = spell.element();
        QiElement qiElement2 = qiElement = element == SpellElement.NONE ? QiElement.PURE : element.matchingQi();
        if (qiElement == null) {
            qiElement = QiElement.PURE;
        }
        return TechniqueBonusHelper.spellElementMultiplier(player, qiElement);
    }

    public static double alchemyTierUpChance(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).alchemyTierUpChance;
    }

    public static double refiningTierUpChance(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).refiningTierUpChance;
    }

    public static boolean nightVision(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).nightVision;
    }

    public static boolean waterBreathing(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).waterBreathing;
    }

    public static boolean fireResistance(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).fireResistance;
    }

    public static boolean knockbackResist(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).knockbackResist || TechniqueBonusHelper.hasImmortalCombo(player);
    }

    public static boolean blockAllPotions(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).blockAllPotions || TechniqueBonusHelper.hasImmortalCombo(player);
    }

    public static boolean fireImmune(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).fireImmune || TechniqueBonusHelper.hasImmortalCombo(player);
    }

    public static boolean qiCostHalve(Player player) {
        return TechniqueBonusHelper.generalQiCostMultiplier(player) < 1.0;
    }

    public static boolean woodSpellCostHalve(Player player) {
        return TechniqueBonusHelper.bonusOf((Player)player).woodSpellCostHalve;
    }

    public static double woodSpellCostMultiplier(Player player) {
        Technique.Bonus bonus = TechniqueBonusHelper.bonusOf(player);
        double mult = bonus.woodSpellCostMult;
        if (bonus.woodSpellCostHalve) {
            mult *= 0.5;
        }
        return mult;
    }

    public static boolean photosynthesisActive(Player player) {
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            return false;
        }
        Technique.Bonus bonus = TechniqueBonusHelper.bonusOf(player);
        if (!bonus.photosynthesis) {
            return false;
        }
        Level level = player.level();
        if (level == null || !level.dimensionType().hasSkyLight() || !level.isDay()) {
            return false;
        }
        BlockPos head = player.blockPosition().above();
        return level.canSeeSky(head) && !level.isRainingAt(head);
    }

    public static boolean forestShelter(Player player) {
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            return false;
        }
        return TechniqueBonusHelper.bonusOf((Player)player).forestShelter;
    }

    public static boolean damageHalve(Player player) {
        return TechniqueBonusHelper.damageTakenMultiplier(player) < 1.0;
    }

    public static double damageTakenMultiplier(Player player) {
        double mult;
        double d = mult = TechniqueBonusHelper.bonusOf((Player)player).damageHalve ? 0.5 : 1.0;
        if (!Double.isFinite(mult *= PhysiqueBonusHelper.damageTakenMultiplier(player))) {
            return 1.0;
        }
        return Math.max(0.0, mult);
    }

    public static boolean hasImmortalCombo(Player player) {
        if (player == null) {
            return false;
        }
        return TechniqueBonusHelper.equippedOf(player) == Technique.IMMORTAL_INCANTATION && PhysiqueBonusHelper.physiqueOf(player) == Physique.IMMORTAL_BODY;
    }

    public static boolean isImmortalIncantationActive(Player player) {
        return TechniqueBonusHelper.equippedOf(player) == Technique.IMMORTAL_INCANTATION;
    }

    public static long applyQiCostMultiplier(Player player, long baseCost) {
        if (baseCost <= 0L) {
            return 0L;
        }
        double multiplier = TechniqueBonusHelper.generalQiCostMultiplier(player);
        if (multiplier == 1.0) {
            return baseCost;
        }
        return Math.max(1L, (long)Math.ceil((double)baseCost * multiplier));
    }

    public static long applySpellQiCostMultiplier(Player player, Spell spell, long baseCost) {
        double weaponCostMult;
        ServerPlayer serverPlayer;
        double dharmaBodyCostMult;
        double woodCostMult;
        long cost = TechniqueBonusHelper.applyQiCostMultiplier(player, baseCost);
        if (spell != null) {
            double looseImmortalCostMult;
            double goldenCoreCostMult;
            double foundationCostMult;
            double physiqueSpellCostMult;
            double rootCostMult = SpiritRootBonusHelper.spellQiCostMultiplier(player, spell);
            if (rootCostMult != 1.0) {
                cost = Math.max(1L, (long)Math.ceil((double)cost * rootCostMult));
            }
            if ((physiqueSpellCostMult = PhysiqueBonusHelper.spellQiCostMultiplier(player, spell)) != 1.0) {
                cost = Math.max(1L, (long)Math.ceil((double)cost * physiqueSpellCostMult));
            }
            if ((foundationCostMult = FoundationDaoBonusHelper.spellQiCostMultiplier(player, spell)) != 1.0) {
                cost = Math.max(1L, (long)Math.ceil((double)cost * foundationCostMult));
            }
            if ((goldenCoreCostMult = GoldenCoreDaoBonusHelper.spellQiCostMultiplier(player, spell)) != 1.0) {
                cost = Math.max(1L, (long)Math.ceil((double)cost * goldenCoreCostMult));
            }
            if ((looseImmortalCostMult = LooseImmortalBonusHelper.spellQiCostMultiplier(player, spell)) != 1.0) {
                cost = Math.max(1L, (long)Math.ceil((double)cost * looseImmortalCostMult));
            }
        }
        if (spell != null && (spell.element() == SpellElement.WOOD || spell == Spell.BUDDHA_FIRE_LOTUS) && (woodCostMult = TechniqueBonusHelper.woodSpellCostMultiplier(player)) != 1.0) {
            cost = Math.max(1L, (long)Math.ceil((double)cost * woodCostMult));
        }
        if (player instanceof ServerPlayer && (dharmaBodyCostMult = DharmaBodyManifestationHandler.spellQiCostMultiplier(serverPlayer = (ServerPlayer)player)) != 1.0) {
            cost = Math.max(1L, (long)Math.ceil((double)cost * dharmaBodyCostMult));
        }
        if (spell != null && cost > 0L && (weaponCostMult = WeaponBonusHelper.spellQiCostMultiplier((LivingEntity)player)) != 1.0) {
            cost = Math.max(1L, (long)Math.ceil((double)cost * weaponCostMult));
        }
        return cost;
    }

    public static double generalQiCostMultiplier(Player player) {
        double mult;
        double d = mult = TechniqueBonusHelper.bonusOf((Player)player).qiCostHalve ? 0.5 : 1.0;
        if (!Double.isFinite(mult *= PhysiqueBonusHelper.generalQiCostMultiplier(player))) {
            return 1.0;
        }
        return Math.max(0.0, mult);
    }
}

