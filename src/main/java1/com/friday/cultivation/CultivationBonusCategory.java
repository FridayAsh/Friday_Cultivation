package com.friday.cultivation;

/**
 * 真元加成分类（完全基于原模组 CFR 反编译）
 */
public enum CultivationBonusCategory {
    BODY_DEFENSE("body_defense"),
    MOVEMENT_SPEED("movement_speed"),
    JUMP_HEIGHT("jump_height"),
    MELEE_DAMAGE("melee_damage"),
    MINING_SPEED("mining_speed"),
    SPELL_DAMAGE("spell_damage"),
    MAX_QI("max_qi"),
    QI_RECOVERY("qi_recovery"),
    CULTIVATION_EFFICIENCY("cultivation_efficiency");

    private final String id;

    CultivationBonusCategory(String id) { this.id = id; }

    public String id() { return id; }
    public String labelKey() { return "screen.friday_cultivation.bonus_category." + id; }
    public String descriptionKey() { return "screen.friday_cultivation.bonus_category." + id + ".desc"; }

    public static CultivationBonusCategory byId(String id) {
        if (id == null) return null;
        for (var c : values()) { if (c.id.equals(id)) return c; }
        return null;
    }
}
