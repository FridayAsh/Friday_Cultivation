package com.friday.cultivation.event.tribulation;

/**
 * 渡劫天骄档位：综合评判系数（灵根+体质+功法）决定档位，
 * 每档对应道数/伤害倍率与奖励倍率。最高档「君临万道」奖励 ×10。
 */
public enum TribulationTier {
    MORTAL_DUST("tribulation_tier.friday_cultivation.mortal_dust", 0.0, 0.2, 1.0, 1.0),        // 凡尘
    SPIRIT_DAWN("tribulation_tier.friday_cultivation.spirit_dawn", 0.2, 0.4, 1.2, 1.5),      // 灵慧初开
    DAO_BONE("tribulation_tier.friday_cultivation.dao_bone", 0.4, 0.6, 1.5, 2.0),            // 道骨初成
    INNER_LIGHT("tribulation_tier.friday_cultivation.inner_light", 0.6, 0.8, 1.8, 2.5),      // 玄光内蕴
    PHOENIX_GIFT("tribulation_tier.friday_cultivation.phoenix_gift", 0.8, 1.0, 2.2, 3.0),     // 凤骨天资
    IMMORTAL_GRACE("tribulation_tier.friday_cultivation.immortal_grace", 1.0, 1.2, 2.6, 4.0), // 仙姿玉骨
    HEAVEN_ENVY("tribulation_tier.friday_cultivation.heaven_envy", 1.2, 1.4, 3.0, 5.0),       // 天妒之才
    THUNDER_FAVOR("tribulation_tier.friday_cultivation.thunder_favor", 1.4, 1.6, 3.5, 6.5),   // 天雷眷顾
    FATE_DEFIER("tribulation_tier.friday_cultivation.fate_defier", 1.6, 1.8, 4.0, 8.0),       // 逆天改命
    SOVEREIGN_OF_DAOS("tribulation_tier.friday_cultivation.sovereign_of_daos", 1.8, 2.0, 4.5, 10.0); // 君临万道

    private final String translationKey;
    private final double min;
    private final double maxExclusive;
    private final double difficultyMult;
    private final double rewardMult;

    TribulationTier(String translationKey, double min, double maxExclusive, double difficultyMult, double rewardMult) {
        this.translationKey = translationKey;
        this.min = min;
        this.maxExclusive = maxExclusive;
        this.difficultyMult = difficultyMult;
        this.rewardMult = rewardMult;
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

    /** 奖励倍率 */
    public double rewardMult() {
        return rewardMult;
    }

    /** 该档位综合系数范围文本 */
    public String rangeText() {
        return String.format("%.1f~%.1f", min, maxExclusive);
    }
}
