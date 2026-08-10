/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.PhysiqueBonus;
import com.friday.cultivation.cultivation.SpiritRoot;
import java.util.ArrayList;
import java.util.List;

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

    private Physique(String id, Rarity rarity, PhysiqueBonus bonus) {
        this.id = id;
        this.rarity = rarity;
        this.bonus = bonus;
    }

    public String id() {
        return this.id;
    }

    public Rarity rarity() {
        return this.rarity;
    }

    public PhysiqueBonus bonus() {
        return this.bonus;
    }

    public String translationKey() {
        return "physique.friday_cultivation." + this.id;
    }

    public String tooltipKey() {
        return "physique.friday_cultivation." + this.id + ".tooltip";
    }

    public String introKey() {
        return "physique.friday_cultivation." + this.id + ".intro";
    }

    public String effectsKey() {
        return "physique.friday_cultivation." + this.id + ".effects";
    }

    public static Physique byId(String id) {
        if (id == null || id.isEmpty()) {
            return MORTAL_BODY;
        }
        for (Physique physique : Physique.values()) {
            if (!physique.id.equals(id)) continue;
            return physique;
        }
        return MORTAL_BODY;
    }

    public static Physique fromLegacySpiritRootId(String id) {
        if (SpiritRoot.HEAVENLY_SWORD.id().equals(id)) {
            return INNATE_SWORD_BODY;
        }
        if (SpiritRoot.FIVE_ELEMENT_CHAOS.id().equals(id)) {
            return FIVE_ELEMENT_CHAOS_BODY;
        }
        if (SpiritRoot.BROKEN_VEIN_BODY.id().equals(id)) {
            return BROKEN_VEIN_BODY;
        }
        return null;
    }

    public static List<Physique> selectableValues() {
        return List.of(Physique.values());
    }

    public static List<Physique> weightedPool() {
        ArrayList<Physique> pool = new ArrayList<Physique>();
        for (Physique physique : Physique.values()) {
            int weight = Physique.weightOf(physique.rarity);
            for (int i = 0; i < weight; ++i) {
                pool.add(physique);
            }
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

    public static enum Rarity {
        LOW,
        MID,
        HIGH,
        SUPREME,
        IMMORTAL,
        SPECIAL;


        public String translationKey() {
            return "physique_rarity.friday_cultivation." + this.name().toLowerCase();
        }
    }
}

