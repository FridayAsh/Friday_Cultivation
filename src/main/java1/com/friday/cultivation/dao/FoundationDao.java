package com.friday.cultivation.dao;

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

    FoundationDao(String id, int lifespanBonus, double spellDamageMult, double spellQiCostMult,
                  double hpMult, boolean bloodMastery, int bodyDefenseBonus,
                  int cultivationEfficiencyBonus, int qiRecoveryPerSecondBonus, int meleeDamageBonus) {
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

    public String id() { return id; }
    public int lifespanBonus() { return lifespanBonus; }
    public double spellDamageMult() { return spellDamageMult; }
    public double spellQiCostMult() { return spellQiCostMult; }
    public double hpMult() { return hpMult; }
    public boolean bloodMastery() { return bloodMastery; }
    public int bodyDefenseBonus() { return bodyDefenseBonus; }
    public int cultivationEfficiencyBonus() { return cultivationEfficiencyBonus; }
    public int qiRecoveryPerSecondBonus() { return qiRecoveryPerSecondBonus; }
    public int meleeDamageBonus() { return meleeDamageBonus; }

    public String translationKey() { return "foundation_dao.friday_cultivation." + id; }

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

    public static FoundationDao byId(String id) {
        if (id == null || id.isEmpty()) return NONE;
        for (FoundationDao d : values()) { if (d.id.equals(id)) return d; }
        return NONE;
    }
}
