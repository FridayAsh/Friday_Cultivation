/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

public enum FoundationDao {
    NONE("none", 0, 1.0, 1.0, 1.0, false, 0, 0, 0, 0),
    HUMAN("human", 100, 1.0, 1.0, 1.0, false, 1, 0, 0, 0),
    BLOOD("blood", 200, 1.0, 1.0, 1.5, true, 4, 1, 1, 5),
    EARTH("earth", 300, 1.25, 0.75, 1.0, false, 3, 2, 2, 0),
    HEAVEN("heaven", 500, 1.5, 0.5, 1.0, false, 5, 3, 3, 0);

    private final String id;
    private final int lifespanBonus;
    private final double spellDamageMult;
    private final double spellQiCostMult;
    private final double hpMult;
    private final boolean bloodMastery;
    private final int bodyDefenseBonus;
    private final int cultivationEfficiencyBonus;
    private final int qiRecoveryPerSecondBonus;
    private final int meleeDamageBonus;

    private FoundationDao(String id, int lifespanBonus, double spellDamageMult, double spellQiCostMult, double hpMult, boolean bloodMastery, int bodyDefenseBonus, int cultivationEfficiencyBonus, int qiRecoveryPerSecondBonus, int meleeDamageBonus) {
        this.id = id;
        this.lifespanBonus = lifespanBonus;
        this.spellDamageMult = spellDamageMult;
        this.spellQiCostMult = spellQiCostMult;
        this.hpMult = hpMult;
        this.bloodMastery = bloodMastery;
        this.bodyDefenseBonus = bodyDefenseBonus;
        this.cultivationEfficiencyBonus = cultivationEfficiencyBonus;
        this.qiRecoveryPerSecondBonus = qiRecoveryPerSecondBonus;
        this.meleeDamageBonus = meleeDamageBonus;
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

    public boolean bloodMastery() {
        return this.bloodMastery;
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

    public String translationKey() {
        return "foundation_dao.friday_cultivation." + this.id;
    }

    /** 筑基之道渡劫波数：人/血道不渡劫（0），地/天道渡劫（4 波） */
    public int tribulationWaves() {
        return switch (this) {
            case EARTH, HEAVEN -> 4;
            default -> 0;
        };
    }

    public static FoundationDao byId(String id) {
        if (id == null || id.isEmpty()) {
            return NONE;
        }
        for (FoundationDao d : FoundationDao.values()) {
            if (!d.id.equals(id)) continue;
            return d;
        }
        return NONE;
    }
}

