package com.friday.cultivation.event.tribulation;

/**
 * 渡劫配置（数据驱动劫谱）。
 * 波数 / 每波道数 / 单次伤害 / 伤害比例 / 雷击间隔 / 劫种。
 *
 * 由 Realm 提供各境界的 spec；所有数值为代码常量，配置接口留待项目完成后实现。
 */
public record TribulationSpec(
        int waves,
        int boltsPerWave,
        int strikeDamage,
        double damageRatio,
        int boltIntervalTicks,
        TribulationType type
) {
    /** 便捷构造：固定伤害、自动间隔、雷劫 */
    public static TribulationSpec of(int waves, int boltsPerWave, int strikeDamage) {
        return new TribulationSpec(waves, boltsPerWave, strikeDamage, 0.0, 0, TribulationType.LIGHTNING);
    }

    /** 便捷构造：比例伤害（=标准生命×ratio）、自动间隔、雷劫 */
    public static TribulationSpec ratio(int waves, int boltsPerWave, double damageRatio) {
        return new TribulationSpec(waves, boltsPerWave, 0, damageRatio, 0, TribulationType.LIGHTNING);
    }

    /** 总道数 */
    public int totalBolts() {
        return Math.max(0, waves) * Math.max(1, boltsPerWave);
    }

    /** 每波实际间隔（0 = 自动按道数计算） */
    public int effectiveBoltInterval() {
        if (boltIntervalTicks > 0) {
            return boltIntervalTicks;
        }
        int bolts = Math.max(1, boltsPerWave);
        if (bolts <= 1) {
            return 0;
        }
        int interval = Math.round((float) TribulationConstants.SUB_WAVE_DURATION_BASE_TICKS / (float) (bolts - 1));
        return Math.max(TribulationConstants.MIN_SUB_BOLT_INTERVAL_TICKS,
                Math.min(TribulationConstants.MAX_SUB_BOLT_INTERVAL_TICKS, interval));
    }
}
