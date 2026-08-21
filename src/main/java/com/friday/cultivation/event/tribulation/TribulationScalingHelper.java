package com.friday.cultivation.event.tribulation;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.world.entity.player.Player;

import java.util.Map;

/**
 * 渡劫综合评判：灵根 / 体质 / 功法品质越好 → 渡劫越猛、奖励越高。
 *
 * 可扩展设计：
 * - 功法 ItemTier / 体质 Physique.Rarity 实现 TribulationQuality 接口，
 *   新增枚举值按 ordinal 自动归一接入，无需改本类。
 * - 灵根 SpiritRoot 无连续品阶，用分组映射表；新增灵根只需加一行表项。
 */
public final class TribulationScalingHelper {
    private TribulationScalingHelper() {
    }

    /** 灵根品阶分组表（新增灵根在此加一行即可接入） */
    private static final Map<SpiritRoot, Double> SPIRIT_ROOT_SCALE = Map.ofEntries(
            Map.entry(SpiritRoot.NONE, 0.0),
            Map.entry(SpiritRoot.TRIPLE, 0.2),
            Map.entry(SpiritRoot.QUADRUPLE, 0.25),
            Map.entry(SpiritRoot.DUAL_METAL_WOOD, 0.35),
            Map.entry(SpiritRoot.DUAL_METAL_WATER, 0.35),
            Map.entry(SpiritRoot.DUAL_METAL_FIRE, 0.35),
            Map.entry(SpiritRoot.DUAL_METAL_EARTH, 0.35),
            Map.entry(SpiritRoot.DUAL_WOOD_WATER, 0.35),
            Map.entry(SpiritRoot.DUAL_WOOD_FIRE, 0.35),
            Map.entry(SpiritRoot.DUAL_WOOD_EARTH, 0.35),
            Map.entry(SpiritRoot.DUAL_WATER_FIRE, 0.35),
            Map.entry(SpiritRoot.DUAL_WATER_EARTH, 0.35),
            Map.entry(SpiritRoot.DUAL_FIRE_EARTH, 0.35),
            Map.entry(SpiritRoot.HEAVENLY_SWORD, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_METAL, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_WOOD, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_WATER, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_FIRE, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_EARTH, 0.55),
            Map.entry(SpiritRoot.HEAVENLY_HIDDEN, 0.6),
            Map.entry(SpiritRoot.MUTANT_ICE, 0.75),
            Map.entry(SpiritRoot.MUTANT_LIGHTNING, 0.75),
            Map.entry(SpiritRoot.FIVE_ROOT, 0.85),
            Map.entry(SpiritRoot.BROKEN_VEIN_BODY, 0.95),
            Map.entry(SpiritRoot.FIVE_ELEMENT_CHAOS, 1.0)
    );

    /** 综合系数（0~2.0）：灵根 + 体质 + 功法 */
    public static double compositeScore(Player player, CultivationData data) {
        return spiritRootScore(data) + physiqueScore(data) + techniqueScore(player);
    }

    /** 档位判定 */
    public static TribulationTier tier(Player player, CultivationData data) {
        return TribulationTier.of(compositeScore(player, data));
    }

    /** 档位名称（本地化键） */
    public static String tierTranslationKey(Player player, CultivationData data) {
        return tier(player, data).translationKey();
    }

    /** 渡劫道数/伤害倍率 */
    public static double difficultyMult(Player player, CultivationData data) {
        return tier(player, data).difficultyMult();
    }

    /** 渡劫成功奖励百分比（作用于当前五维点数） */
    public static double rewardPercent(Player player, CultivationData data) {
        return tier(player, data).rewardPercent();
    }

    /**
     * 将基础劫谱按当前天骄档位转换成一次性运行劫谱。
     * 该转换只在启动前执行一次，Session 建立后不再重新缩放。
     */
    public static TribulationSpec scaleSpec(Player player, CultivationData data, TribulationSpec base) {
        if (base == null) {
            return TribulationSpec.of(0, 1, 0);
        }
        double mult = difficultyMult(player, data);
        int bolts = Math.max(1, base.boltsPerWave());
        int totalBolts = Math.max(1, (int)Math.round((double)base.waves() * bolts * mult));
        int waves = Math.max(1, (int)Math.ceil((double)totalBolts / (double)bolts));
        int damage = base.strikeDamage() <= 0
                ? 0
                : Math.max(1, (int)Math.round((double)base.strikeDamage() * mult));
        return new TribulationSpec(waves, bolts, damage, base.damageRatio(),
                base.boltIntervalTicks(), base.type());
    }

    /** 灵根系数（0~1.2） */
    private static double spiritRootScore(CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        SpiritRoot root = data.getSpiritRoot();
        if (root == null) {
            return 0.0;
        }
        Double scale = SPIRIT_ROOT_SCALE.get(root);
        if (scale == null) {
            // 未在表中的灵根：默认按枚举顺序比例（新增自动接入）
            return TribulationConstants.SPIRIT_ROOT_WEIGHT
                    * ((double) root.ordinal() / (double) SpiritRoot.values().length);
        }
        return TribulationConstants.SPIRIT_ROOT_WEIGHT * scale;
    }

    /** 体质系数（0~0.5）：Physique.Rarity 实现 TribulationQuality 自动归一 */
    private static double physiqueScore(CultivationData data) {
        if (data == null) {
            return 0.0;
        }
        Physique physique = data.getPhysique();
        if (physique == null) {
            return 0.0;
        }
        return TribulationConstants.PHYSIQUE_WEIGHT * qualityOf(physique.rarity());
    }

    /** 功法系数（0~0.3）：ItemTier 实现 TribulationQuality 自动归一 */
    private static double techniqueScore(Player player) {
        Technique technique = TechniqueBonusHelper.equippedOf(player);
        if (technique == null) {
            return 0.0;
        }
        ItemTier tier = technique.tier();
        if (tier == null) {
            return 0.0;
        }
        return TribulationConstants.TECHNIQUE_WEIGHT * qualityOf(tier);
    }

    /** 品阶归一化：实现 TribulationQuality 的枚举按 ordinal 比例（0~1） */
    private static double qualityOf(Enum<?> e) {
        Object[] values = e.getClass().getEnumConstants();
        if (values.length <= 1) {
            return 0.0;
        }
        return (double) e.ordinal() / (double) (values.length - 1);
    }
}
