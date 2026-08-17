package com.friday.cultivation.event.tribulation;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.world.entity.player.Player;

/**
 * 渡劫综合评判：灵根 / 体质 / 功法品质越好 → 渡劫越多（天骄遭天妒）。
 * 修正系数 = 1.0 + 灵根系数 + 体质系数 + 功法系数。
 */
public final class TribulationScalingHelper {
    private TribulationScalingHelper() {
    }

    /**
     * 综合修正系数：最终道数 = 基准道数 × 此系数。
     */
    public static double scalingMultiplier(Player player, CultivationData data) {
        double spiritRoot = spiritRootQuality(data);
        double physique = physiqueQuality(data);
        double technique = techniqueQuality(player);
        return 1.0 + spiritRoot + physique + technique;
    }

    /** 灵根品质系数（0 ~ SPIRIT_ROOT_WEIGHT） */
    private static double spiritRootQuality(CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        SpiritRoot root = data.getSpiritRoot();
        if (root == null) {
            return 0.0;
        }
        // 简化分级：混沌五灵根/变异/天灵根高；双灵根中；杂灵根低；无灵根最低
        double quality = switch (root) {
            case NONE -> 0.0;
            case TRIPLE, QUADRUPLE -> 0.15;
            case DUAL_METAL_WOOD, DUAL_METAL_WATER, DUAL_METAL_FIRE, DUAL_METAL_EARTH,
                 DUAL_WOOD_WATER, DUAL_WOOD_FIRE, DUAL_WOOD_EARTH, DUAL_WATER_FIRE,
                 DUAL_WATER_EARTH, DUAL_FIRE_EARTH -> 0.3;
            case HEAVENLY_SWORD, HEAVENLY_METAL, HEAVENLY_WOOD, HEAVENLY_WATER,
                 HEAVENLY_FIRE, HEAVENLY_EARTH, HEAVENLY_HIDDEN -> 0.5;
            case MUTANT_ICE, MUTANT_LIGHTNING -> 0.6;
            case FIVE_ROOT -> 0.7;
            case BROKEN_VEIN_BODY, FIVE_ELEMENT_CHAOS -> 0.8;
            default -> 0.2;
        };
        return quality * TribulationConstants.SPIRIT_ROOT_WEIGHT;
    }

    /** 体质品质系数（0 ~ PHYSIQUE_WEIGHT） */
    private static double physiqueQuality(CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        Physique physique = data.getPhysique();
        if (physique == null) {
            return 0.0;
        }
        double quality = switch (physique.rarity()) {
            case LOW -> 0.1;
            case MID -> 0.2;
            case HIGH -> 0.4;
            case SUPREME -> 0.7;
            case IMMORTAL -> 0.85;
            case SPECIAL -> 1.0;
        };
        return quality * TribulationConstants.PHYSIQUE_WEIGHT;
    }

    /** 功法品质系数（0 ~ TECHNIQUE_WEIGHT） */
    private static double techniqueQuality(Player player) {
        Technique technique = TechniqueBonusHelper.equippedOf(player);
        if (technique == null) {
            return 0.0;
        }
        ItemTier tier = technique.tier();
        if (tier == null) {
            return 0.0;
        }
        double quality = switch (tier) {
            case LOW -> 0.1;
            case MID -> 0.2;
            case HIGH -> 0.4;
            case SUPREME -> 0.7;
            case IMMORTAL -> 0.85;
            case SAGE -> 0.95;
            case GREAT_EMPEROR -> 1.0;
        };
        return quality * TribulationConstants.TECHNIQUE_WEIGHT;
    }
}
