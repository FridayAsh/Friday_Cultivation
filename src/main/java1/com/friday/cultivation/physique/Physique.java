package com.friday.cultivation.physique;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 体质系统 — 13种体质，完整复刻原模组 Physique 枚举
 * 含完整 PhysiqueBonus 加成数据（分元素法伤 + 剑法 + HP + 灵气吸收 + 受伤倍率 + 修炼限制）
 */
public enum Physique {
    MORTAL_BODY("mortal_body", Rarity.LOW, PhysiqueBonus.IDENTITY),
    BROKEN_VEIN_BODY("broken_vein_body", Rarity.SPECIAL, PhysiqueBonus.builder().hpMult(2.0).meleeDmgMult(2.0).maxQiMult(0.1).resistanceRegen(true).build()),
    INNATE_SWORD_BODY("innate_sword_body", Rarity.IMMORTAL, PhysiqueBonus.builder().swordSpellMult(2.0).build()),
    IMMORTAL_BODY("immortal_body", Rarity.IMMORTAL, PhysiqueBonus.builder().maxHpBonus(10).qiAbsorbMult(10.0).qiAbsorbRange(10).qiCostMult(0.5).damageTakenMult(0.5).build()),
    FIVE_ELEMENT_CHAOS_BODY("five_element_chaos_body", Rarity.SPECIAL, PhysiqueBonus.builder().metalSpellMult(0.5).woodSpellMult(0.5).waterSpellMult(0.5).fireSpellMult(0.5).earthSpellMult(0.5).build()),
    INVERSE_FIVE_ELEMENTS_BODY("inverse_five_elements_body", Rarity.SPECIAL, PhysiqueBonus.IDENTITY),
    HEAVENLY_FIRE_BODY("heavenly_fire_body", Rarity.SUPREME, PhysiqueBonus.builder().fireSpellMult(1.2).build()),
    MYSTIC_ICE_BODY("mystic_ice_body", Rarity.SUPREME, PhysiqueBonus.builder().waterSpellMult(1.2).build()),
    SWORD_BONE("sword_bone", Rarity.SUPREME, PhysiqueBonus.builder().swordSpellMult(1.2).build()),
    ALCHEMY_HEART_BODY("alchemy_heart_body", Rarity.SUPREME, PhysiqueBonus.IDENTITY),
    FORMATION_MERIDIAN_BODY("formation_meridian_body", Rarity.SUPREME, PhysiqueBonus.IDENTITY),
    BLOOD_FIEND_BODY("blood_fiend_body", Rarity.SUPREME, PhysiqueBonus.builder().pureSpellMult(1.0).build()),
    CHAOS_BODY("chaos_body", Rarity.IMMORTAL, PhysiqueBonus.IDENTITY);

    private final String id;
    private final Rarity rarity;
    private final PhysiqueBonus bonus;

    Physique(String id, Rarity rarity, PhysiqueBonus bonus) {
        this.id = id;
        this.rarity = rarity;
        this.bonus = bonus;
    }

    public String id() { return id; }
    public Rarity rarity() { return rarity; }
    public PhysiqueBonus bonus() { return bonus; }

    public String translationKey() { return "physique.friday_cultivation." + id; }
    public String tooltipKey() { return translationKey() + ".tooltip"; }
    public String introKey() { return translationKey() + ".intro"; }
    public String effectsKey() { return translationKey() + ".effects"; }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), id);
    }

    public static Physique byId(String id) {
        if (id == null || id.isEmpty()) return MORTAL_BODY;
        for (Physique p : values()) {
            if (p.id.equals(id)) return p;
        }
        return MORTAL_BODY;
    }

    /** 旧灵根ID映射（兼容存档） */
    public static Physique fromLegacySpiritRootId(String id) {
        if (id == null) return null;
        if (id.equals("heavenly_sword")) return INNATE_SWORD_BODY;
        if (id.equals("five_element_chaos")) return FIVE_ELEMENT_CHAOS_BODY;
        if (id.equals("broken_vein_body")) return BROKEN_VEIN_BODY;
        return null;
    }

    public static List<Physique> selectableValues() {
        return List.of(values());
    }

    /** 按稀有度权重生成抽取池（LOW 70, SUPREME 20, IMMORTAL 8, SPECIAL 3, 其他1） */
    public static List<Physique> weightedPool() {
        List<Physique> pool = new ArrayList<>();
        for (Physique p : values()) {
            int weight = weightOf(p.rarity);
            for (int i = 0; i < weight; i++) pool.add(p);
        }
        return pool;
    }

    public static int weightOf(Rarity rarity) {
        return switch (rarity) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 70;
            case MID -> 20;
            case HIGH -> 8;
            case SUPREME -> 3;
            case IMMORTAL -> 1;
            case SPECIAL -> 1;
        };
    }

    /** 体质稀有度 — 复刻原模组 Rarity：LOW/MID/HIGH/SUPREME/IMMORTAL/SPECIAL */
    public enum Rarity {
        LOW, MID, HIGH, SUPREME, IMMORTAL, SPECIAL;

        public String translationKey() { return "physique_rarity.friday_cultivation." + name().toLowerCase(); }
        public Component displayName() { return Component.translatableWithFallback(translationKey(), name()); }
    }
}
