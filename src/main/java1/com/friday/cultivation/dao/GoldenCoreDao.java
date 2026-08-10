package com.friday.cultivation.dao;

public enum GoldenCoreDao {
    NONE("none", 0, 1.0, 1.0, 1.0, 1.0, 1.0, 0, 0, 0, 0, 0, 0),
    HUMAN("human", 300, 1.0, 1.0, 1.0, 1.0, 1.0, 1, 0, 0, 0, 3, 40),
    BLOOD("blood", 500, 1.0, 1.0, 2.0, 2.0, 0.3, 4, 1, 1, 5, 3, 40),
    EARTH("earth", 800, 1.5, 0.5, 1.0, 1.0, 1.0, 3, 2, 2, 0, 6, 40),
    HEAVEN("heaven", 1000, 2.0, 0.2, 1.0, 1.0, 1.0, 5, 3, 3, 0, 9, 40);

    private final String id;
    private final int lifespanBonus;
    private final double spellDamageMult, spellQiCostMult, hpMult, bloodSpellDamageMult, bloodSpellQiCostMult;
    private final int bodyDefenseBonus, cultivationEfficiencyBonus, qiRecoveryPerSecondBonus, meleeDamageBonus, tribulationStrikes, tribulationDamage;

    GoldenCoreDao(String id, int lifespanBonus, double spellDamageMult, double spellQiCostMult, double hpMult,
                  double bloodSpellDamageMult, double bloodSpellQiCostMult, int bodyDefenseBonus,
                  int cultivationEfficiencyBonus, int qiRecoveryPerSecondBonus, int meleeDamageBonus,
                  int tribulationStrikes, int tribulationDamage) {
        this.id = id; this.lifespanBonus = lifespanBonus;
        this.spellDamageMult = spellDamageMult; this.spellQiCostMult = spellQiCostMult; this.hpMult = hpMult;
        this.bloodSpellDamageMult = bloodSpellDamageMult; this.bloodSpellQiCostMult = bloodSpellQiCostMult;
        this.bodyDefenseBonus = bodyDefenseBonus; this.cultivationEfficiencyBonus = cultivationEfficiencyBonus;
        this.qiRecoveryPerSecondBonus = qiRecoveryPerSecondBonus; this.meleeDamageBonus = meleeDamageBonus;
        this.tribulationStrikes = tribulationStrikes; this.tribulationDamage = tribulationDamage;
    }

    public String id() { return id; }
    public int lifespanBonus() { return lifespanBonus; }
    public double spellDamageMult() { return spellDamageMult; }
    public double spellQiCostMult() { return spellQiCostMult; }
    public double hpMult() { return hpMult; }
    public double bloodSpellDamageMult() { return bloodSpellDamageMult; }
    public double bloodSpellQiCostMult() { return bloodSpellQiCostMult; }
    public int bodyDefenseBonus() { return bodyDefenseBonus; }
    public int cultivationEfficiencyBonus() { return cultivationEfficiencyBonus; }
    public int qiRecoveryPerSecondBonus() { return qiRecoveryPerSecondBonus; }
    public int meleeDamageBonus() { return meleeDamageBonus; }
    public int tribulationStrikes() { return tribulationStrikes; }
    public int tribulationDamage() { return tribulationDamage; }
    public String translationKey() { return "golden_core_dao.friday_cultivation." + id; }
    public String tooltipKey() { return "tooltip.friday_cultivation.golden_core." + id; }
    public String requirementKey() { return "screen.friday_cultivation.breakthrough.golden_core." + id + ".requirement"; }

    /** 显示名（中文名） */
    public net.minecraft.network.chat.Component displayName() {
        return net.minecraft.network.chat.Component.translatableWithFallback(translationKey(),
                switch (this) {
                    case HUMAN -> "人道";
                    case BLOOD -> "血道";
                    case EARTH -> "地道";
                    case HEAVEN -> "天道";
                    default -> "未定";
                });
    }

    public static GoldenCoreDao byId(String id) {
        if (id == null || id.isEmpty()) return NONE;
        for (GoldenCoreDao d : values()) { if (d.id.equals(id)) return d; }
        return NONE;
    }
}
