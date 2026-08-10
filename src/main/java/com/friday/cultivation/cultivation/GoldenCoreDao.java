/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

public enum GoldenCoreDao {
    NONE("none", 0, 1.0, 1.0, 1.0, 1.0, 1.0, 0, 0, 0, 0, 0, 0),
    HUMAN("human", 300, 1.0, 1.0, 1.0, 1.0, 1.0, 1, 0, 0, 0, 3, 40),
    BLOOD("blood", 500, 1.0, 1.0, 2.0, 2.0, 0.3, 4, 1, 1, 5, 3, 40),
    EARTH("earth", 800, 1.5, 0.5, 1.0, 1.0, 1.0, 3, 2, 2, 0, 6, 40),
    HEAVEN("heaven", 1000, 2.0, 0.2, 1.0, 1.0, 1.0, 5, 3, 3, 0, 9, 40);

    private final String id;
    private final int lifespanBonus;
    private final double spellDamageMult;
    private final double spellQiCostMult;
    private final double hpMult;
    private final double bloodSpellDamageMult;
    private final double bloodSpellQiCostMult;
    private final int bodyDefenseBonus;
    private final int cultivationEfficiencyBonus;
    private final int qiRecoveryPerSecondBonus;
    private final int meleeDamageBonus;
    private final int tribulationStrikes;
    private final int tribulationDamage;

    private GoldenCoreDao(String id, int lifespanBonus, double spellDamageMult, double spellQiCostMult, double hpMult, double bloodSpellDamageMult, double bloodSpellQiCostMult, int bodyDefenseBonus, int cultivationEfficiencyBonus, int qiRecoveryPerSecondBonus, int meleeDamageBonus, int tribulationStrikes, int tribulationDamage) {
        this.id = id;
        this.lifespanBonus = lifespanBonus;
        this.spellDamageMult = spellDamageMult;
        this.spellQiCostMult = spellQiCostMult;
        this.hpMult = hpMult;
        this.bloodSpellDamageMult = bloodSpellDamageMult;
        this.bloodSpellQiCostMult = bloodSpellQiCostMult;
        this.bodyDefenseBonus = bodyDefenseBonus;
        this.cultivationEfficiencyBonus = cultivationEfficiencyBonus;
        this.qiRecoveryPerSecondBonus = qiRecoveryPerSecondBonus;
        this.meleeDamageBonus = meleeDamageBonus;
        this.tribulationStrikes = tribulationStrikes;
        this.tribulationDamage = tribulationDamage;
    }

    public String id() {
        return this.id;
    }

    public int lifespanBonus() {
        return this.lifespanBonus;
    }

    public double spellDamageMult() {
        return this.spellDamageMult;
    }

    public double spellQiCostMult() {
        return this.spellQiCostMult;
    }

    public double hpMult() {
        return this.hpMult;
    }

    public double bloodSpellDamageMult() {
        return this.bloodSpellDamageMult;
    }

    public double bloodSpellQiCostMult() {
        return this.bloodSpellQiCostMult;
    }

    public int bodyDefenseBonus() {
        return this.bodyDefenseBonus;
    }

    public int cultivationEfficiencyBonus() {
        return this.cultivationEfficiencyBonus;
    }

    public int qiRecoveryPerSecondBonus() {
        return this.qiRecoveryPerSecondBonus;
    }

    public int meleeDamageBonus() {
        return this.meleeDamageBonus;
    }

    public int tribulationStrikes() {
        return this.tribulationStrikes;
    }

    public int tribulationDamage() {
        return this.tribulationDamage;
    }

    public String translationKey() {
        return "golden_core_dao.friday_cultivation." + this.id;
    }

    public String tooltipKey() {
        return "tooltip.friday_cultivation.golden_core." + this.id;
    }

    public String requirementKey() {
        return "screen.friday_cultivation.breakthrough.golden_core." + this.id + ".requirement";
    }

    public static GoldenCoreDao byId(String id) {
        if (id == null || id.isEmpty()) {
            return NONE;
        }
        for (GoldenCoreDao d : GoldenCoreDao.values()) {
            if (!d.id.equals(id)) continue;
            return d;
        }
        return NONE;
    }
}

