package com.friday.cultivation.event.tribulation;

/**
 * 渡劫天骄档位：综合评判系数（灵根+体质+功法）决定档位。
 * 每档对应渡劫难度倍率（道数/伤害）与奖励百分比（作用于当前五维点数）。
 * 最高档「君临万道」奖励 50%。
 */
public enum TribulationTier {
    MORTAL_DUST("tribulation_tier.friday_cultivation.mortal_dust", 0.0, 0.2, 1.0, 0.00, 0xA0A0A0),                    // 凡尘 灰
    SPIRIT_DAWN("tribulation_tier.friday_cultivation.spirit_dawn", 0.2, 0.4, 1.2, 0.05, 0x55FF55),                  // 灵慧初开 绿
    DAO_BONE("tribulation_tier.friday_cultivation.dao_bone", 0.4, 0.6, 1.5, 0.10, 0x55AAFF),                       // 道骨初成 蓝
    INNER_LIGHT("tribulation_tier.friday_cultivation.inner_light", 0.6, 0.8, 1.8, 0.15, 0x55FFFF),                 // 玄光内蕴 青
    PHOENIX_GIFT("tribulation_tier.friday_cultivation.phoenix_gift", 0.8, 1.0, 2.2, 0.20, 0xFFAA00),               // 凤骨天资 橙
    IMMORTAL_GRACE("tribulation_tier.friday_cultivation.immortal_grace", 1.0, 1.2, 2.6, 0.25, 0xFF55FF),           // 仙姿玉骨 紫
    HEAVEN_ENVY("tribulation_tier.friday_cultivation.heaven_envy", 1.2, 1.4, 3.0, 0.30, 0xFF3355),                  // 天妒之才 绯红
    THUNDER_FAVOR("tribulation_tier.friday_cultivation.thunder_favor", 1.4, 1.6, 3.5, 0.35, 0xFF8844),             // 天雷眷顾 金橙
    FATE_DEFIER("tribulation_tier.friday_cultivation.fate_defier", 1.6, 1.8, 4.0, 0.40, 0xFFD700),                  // 逆天改命 金
    SOVEREIGN_OF_DAOS("tribulation_tier.friday_cultivation.sovereign_of_daos", 1.8, 2.0, 4.5, 0.50, 0xFFA500);     // 君临万道 鎏金

    private final String translationKey;
    private final double min;
    private final double maxExclusive;
    private final double difficultyMult;
    private final double rewardPercent;
    private final int color;

    TribulationTier(String translationKey, double min, double maxExclusive, double difficultyMult, double rewardPercent, int color) {
        this.translationKey = translationKey;
        this.min = min;
        this.maxExclusive = maxExclusive;
        this.difficultyMult = difficultyMult;
        this.rewardPercent = rewardPercent;
        this.color = color;
    }

    /** 综合系数（0~2.0）对应的档位 */
    public static TribulationTier of(double composite) {
        double c = Math.max(0.0, Math.min(2.0, composite));
        for (TribulationTier t : values()) {
            if (c < t.maxExclusive) {
                return t;
            }
        }
        return SOVEREIGN_OF_DAOS;
    }

    public String translationKey() {
        return translationKey;
    }

    /** 道数/伤害倍率 */
    public double difficultyMult() {
        return difficultyMult;
    }

    /** 奖励百分比（作用于当前五维点数，0.00~0.50） */
    public double rewardPercent() {
        return rewardPercent;
    }

    /** 档位颜色（尊贵感递增） */
    public int color() {
        return color;
    }

    /** 该档位综合系数范围文本 */
    public String rangeText() {
        return String.format("%.1f~%.1f", min, maxExclusive);
    }
}
