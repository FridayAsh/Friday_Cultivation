package com.friday.cultivation.util;

import com.friday.cultivation.CultivationBonusCategory;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.dao.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.dao.LooseImmortalBonusHelper;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.DharmaBodyManifestationHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.item.BloodBurnPillItem;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.spirit.SpiritRootBonus;
import com.friday.cultivation.spirit.SpiritRootBonusHelper;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.technique.WeaponBonusHelper;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;

/**
 * 法术缩放助手 - 严格 1:1 复刻原模组 SpellScalingHelper。
 */
public final class SpellScalingHelper {
    private SpellScalingHelper() {
    }

    public static double damageMultiplier(LivingEntity caster, Spell spell) {
        if (caster == null || spell == null) {
            return 1.0;
        }
        if (caster instanceof Player) {
            Player player = (Player) caster;
            return SpellScalingHelper.playerDamageMultiplier(player, spell);
        }
        if (caster instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity) caster;
            return SpellScalingHelper.npcDamageMultiplier(npc, spell);
        }
        return 1.0;
    }

    public static double playerDamageMultiplier(Player player, Spell spell) {
        if (player == null || spell == null) {
            return 1.0;
        }
        if (CultivationCapability.get(player).map(data -> !data.isBonusCategoryEnabled(CultivationBonusCategory.SPELL_DAMAGE)).orElse(false).booleanValue()) {
            return 1.0;
        }
        double multiplier = 1.0;
        multiplier *= SpiritRootBonusHelper.overallSpellMultiplier(player, spell);
        multiplier *= TechniqueBonusHelper.spellDamageMultiplier(player, spell);
        multiplier *= ZhenyuanBonusHelper.spellPowerMultiplier(player);
        multiplier *= FoundationDaoBonusHelper.spellDamageMultiplier(player, spell);
        multiplier *= GoldenCoreDaoBonusHelper.spellDamageMultiplier(player, spell);
        multiplier *= LooseImmortalBonusHelper.spellDamageMultiplier(player, spell);
        multiplier *= WeaponBonusHelper.spellDamageMultiplier(player, spell);
        multiplier *= BloodBurnPillItem.spellDamageMultiplier(player);
        multiplier *= SpellScalingHelper.identitySpellDamageMultiplier(player, spell);
        multiplier *= SpellScalingHelper.formationSpellDamageMultiplier(player, spell);
        if (player instanceof ServerPlayer) {
            ServerPlayer serverPlayer = (ServerPlayer) player;
            multiplier *= DharmaBodyManifestationHandler.spellDamageMultiplier(serverPlayer);
        }
        return SpellScalingHelper.sanitizeMultiplier(multiplier *= RealmPressureHandler.outgoingDamageMultiplier(player));
    }

    public static double npcDamageMultiplier(WanderingCultivatorEntity npc, Spell spell) {
        if (npc == null || spell == null) {
            return 1.0;
        }
        double multiplier = 1.0;
        multiplier *= SpellScalingHelper.npcSpiritRootMultiplier(npc, spell);
        multiplier *= SpellScalingHelper.npcTechniqueMultiplier(npc, spell);
        multiplier *= npc.getZhenyuanSpellPowerMult();
        multiplier *= WeaponBonusHelper.spellDamageMultiplier(npc, spell);
        multiplier *= SpellScalingHelper.formationSpellDamageMultiplier(npc, spell);
        multiplier *= DharmaBodyManifestationHandler.spellDamageMultiplier(npc);
        if (npc.isNpcSoulState()) {
            multiplier *= 0.1;
        }
        return SpellScalingHelper.sanitizeMultiplier(multiplier *= RealmPressureHandler.outgoingDamageMultiplier(npc));
    }

    public static int scaledDamage(LivingEntity caster, Spell spell) {
        return SpellScalingHelper.scaledDamage(caster, spell, spell != null ? (double) spell.damage() : 0.0);
    }

    public static int scaledDamage(LivingEntity caster, Spell spell, double baseDamage) {
        return Math.max(0, Math.round(SpellScalingHelper.scaledDamageFloat(caster, spell, (float) baseDamage)));
    }

    public static float scaledDamageFloat(LivingEntity caster, Spell spell, float baseDamage) {
        if (baseDamage <= 0.0f) {
            return 0.0f;
        }
        double scaled = (double) baseDamage * SpellScalingHelper.damageMultiplier(caster, spell);
        return (float) Math.min(3.4028234663852886E38, Math.max(0.0, scaled));
    }

    public static double scaledDamageDouble(LivingEntity caster, Spell spell, double baseDamage) {
        if (baseDamage <= 0.0) {
            return 0.0;
        }
        return Math.max(0.0, baseDamage * SpellScalingHelper.damageMultiplier(caster, spell));
    }

    public static int damageBonus(Player player, Spell spell) {
        if (spell == null) {
            return 0;
        }
        return SpellScalingHelper.scaledDamage(player, spell) - spell.damage();
    }

    public static int powerBonusPercent(LivingEntity caster, Spell spell) {
        return (int) Math.round((SpellScalingHelper.damageMultiplier(caster, spell) - 1.0) * 100.0);
    }

    private static double identitySpellDamageMultiplier(Player player, Spell spell) {
        return 1.0;
    }

    private static double formationSpellDamageMultiplier(LivingEntity caster, Spell spell) {
        return 1.0;
    }

    private static double npcTechniqueMultiplier(WanderingCultivatorEntity npc, Spell spell) {
        Technique technique = Technique.byId(npc.getTechniqueId());
        if (technique == null || spell == null) {
            return 1.0;
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            return Math.max(0.1, (technique.bonus().spellMultFor(QiElement.WOOD) + technique.bonus().spellMultFor(QiElement.FIRE)) / 2.0);
        }
        SpellElement element = spell.element();
        QiElement qiElement = element == SpellElement.NONE ? QiElement.PURE : element.matchingQi();
        if (qiElement == null) {
            qiElement = QiElement.PURE;
        }
        return technique.bonus().spellMultFor(qiElement);
    }

    private static double npcSpiritRootMultiplier(WanderingCultivatorEntity npc, Spell spell) {
        QiElement qi;
        SpiritRoot root = npc.getSpiritRoot();
        if (root == null) {
            root = SpiritRoot.NONE;
        }
        if (spell == Spell.BUDDHA_FIRE_LOTUS) {
            return SpellScalingHelper.npcBuddhaFireLotusMultiplier(npc, root);
        }
        SpellElement spellElement = spell.element();
        double base = spellElement != null && spellElement != SpellElement.NONE ? ((qi = spellElement.matchingQi()) != null ? SpellScalingHelper.npcSpellElementMultiplier(npc, root, qi) : root.bonus().nonElementSpellMult()) : root.bonus().nonElementSpellMult();
        return base * PhysiqueBonusHelper.spellDamageMultiplier(npc, spell);
    }

    private static double npcBuddhaFireLotusMultiplier(WanderingCultivatorEntity npc, SpiritRoot root) {
        double total = SpellScalingHelper.npcSpellElementMultiplier(npc, root, QiElement.WOOD) + SpellScalingHelper.npcSpellElementMultiplier(npc, root, QiElement.FIRE);
        return Math.max(0.1, total / 2.0) * PhysiqueBonusHelper.spellDamageMultiplier(npc, Spell.BUDDHA_FIRE_LOTUS);
    }

    private static double npcSpellElementMultiplier(WanderingCultivatorEntity npc, SpiritRoot root, QiElement element) {
        if (element == null || element == QiElement.PURE) {
            return root.bonus().nonElementSpellMult();
        }
        if (root == SpiritRoot.FIVE_ELEMENT_CHAOS && SpellScalingHelper.npcHasChaosCombo(npc) && SpellScalingHelper.isFiveElementBasic(element)) {
            return 2.0;
        }
        SpiritRootBonus bonus = root.bonus();
        double multiplier = bonus.primaryElement() == element ? bonus.primaryElementMult() : (bonus.secondaryElement() == element ? bonus.secondaryElementMult() : (bonus.counterElement() == element ? bonus.counterElementMult() : bonus.offElementMult()));
        if (bonus.environmentBuff() && bonus.primaryElement() == element) {
            multiplier = SpellScalingHelper.applyEnvironmentBuff(npc, element, multiplier);
        }
        return multiplier;
    }

    private static boolean npcHasChaosCombo(WanderingCultivatorEntity npc) {
        return npc.getSpiritRoot() == SpiritRoot.FIVE_ELEMENT_CHAOS && Technique.FIVE_ELEMENT_CHAOS_ART.id().equals(npc.getTechniqueId()) && npc.getRealm().ordinal() >= Realm.NASCENT_SOUL.ordinal();
    }

    private static boolean isFiveElementBasic(QiElement element) {
        return element == QiElement.METAL || element == QiElement.WOOD || element == QiElement.WATER || element == QiElement.FIRE || element == QiElement.EARTH;
    }

    private static double applyEnvironmentBuff(LivingEntity entity, QiElement element, double base) {
        Level level = entity.level();
        if (level == null) {
            return base;
        }
        Holder<Biome> biome = level.getBiome(entity.blockPosition());
        if (element == QiElement.ICE ? biome.value().getBaseTemperature() < 0.2f || biome.is(BiomeTags.IS_TAIGA) : element == QiElement.LIGHTNING && level.isThundering()) {
            return base * 1.2;
        }
        return base;
    }

    private static double sanitizeMultiplier(double multiplier) {
        if (!Double.isFinite(multiplier)) {
            return 1.0;
        }
        return Math.max(0.0, multiplier);
    }
}
