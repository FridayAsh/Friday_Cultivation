package com.friday.cultivation;

import java.util.ArrayList;
import java.util.List;

/**
 * 灵根枚举 — 完整复刻原模组 SpiritRoot（含 Rarity 内部枚举）。
 * <p>
 * 25 种灵根（NONE / HEAVENLY_SWORD / HEAVENLY_METAL-WOOD-WATER-FIRE-EARTH / MUTANT_ICE-LIGHTNING
 * / HEAVENLY_HIDDEN / DUAL_*(10 种) / TRIPLE / QUADRUPLE / FIVE_ROOT / FIVE_ELEMENT_CHAOS
 * / BROKEN_VEIN_BODY），每种带 Rarity 和 SpiritRootBonus。
 * </p>
 * <p>
 * Rarity 内部枚举：NORMAL / R / SR / SSR / SPECIAL。
 * </p>
 */
public enum SpiritRoot {
    NONE("none", Rarity.NORMAL, SpiritRootBonus.IDENTITY),
    HEAVENLY_SWORD("heavenly_sword", Rarity.SPECIAL, SpiritRootBonus.builder().swordDmgMult(2.0).nonElementSpellMult(0.5).build()),
    HEAVENLY_METAL("heavenly_metal", Rarity.SSR, SpiritRoot.heavenlyRoot(QiElement.METAL, QiElement.WOOD)),
    HEAVENLY_WOOD("heavenly_wood", Rarity.SSR, SpiritRoot.heavenlyRoot(QiElement.WOOD, QiElement.EARTH)),
    HEAVENLY_WATER("heavenly_water", Rarity.SSR, SpiritRoot.heavenlyRoot(QiElement.WATER, QiElement.FIRE)),
    HEAVENLY_FIRE("heavenly_fire", Rarity.SSR, SpiritRoot.heavenlyRoot(QiElement.FIRE, QiElement.WATER)),
    HEAVENLY_EARTH("heavenly_earth", Rarity.SSR, SpiritRoot.heavenlyRoot(QiElement.EARTH, QiElement.WOOD)),
    MUTANT_ICE("mutant_ice", Rarity.SSR, SpiritRootBonus.builder().primaryElement(QiElement.ICE).primaryElementMult(1.6).environmentBuff(true).build()),
    MUTANT_LIGHTNING("mutant_lightning", Rarity.SSR, SpiritRootBonus.builder().primaryElement(QiElement.LIGHTNING).primaryElementMult(1.6).environmentBuff(true).build()),
    HEAVENLY_HIDDEN("heavenly_hidden", Rarity.SSR, SpiritRootBonus.builder().nonElementSpellMult(1.5).build()),
    DUAL_METAL_WOOD("dual_metal_wood", Rarity.SR, SpiritRoot.dualRoot(QiElement.METAL, QiElement.WOOD)),
    DUAL_METAL_WATER("dual_metal_water", Rarity.SR, SpiritRoot.dualRoot(QiElement.METAL, QiElement.WATER)),
    DUAL_METAL_FIRE("dual_metal_fire", Rarity.SR, SpiritRoot.dualRoot(QiElement.METAL, QiElement.FIRE)),
    DUAL_METAL_EARTH("dual_metal_earth", Rarity.SR, SpiritRoot.dualRoot(QiElement.METAL, QiElement.EARTH)),
    DUAL_WOOD_WATER("dual_wood_water", Rarity.SR, SpiritRoot.dualRoot(QiElement.WOOD, QiElement.WATER)),
    DUAL_WOOD_FIRE("dual_wood_fire", Rarity.SR, SpiritRoot.dualRoot(QiElement.WOOD, QiElement.FIRE)),
    DUAL_WOOD_EARTH("dual_wood_earth", Rarity.SR, SpiritRoot.dualRoot(QiElement.WOOD, QiElement.EARTH)),
    DUAL_WATER_FIRE("dual_water_fire", Rarity.SR, SpiritRoot.dualRoot(QiElement.WATER, QiElement.FIRE)),
    DUAL_WATER_EARTH("dual_water_earth", Rarity.SR, SpiritRoot.dualRoot(QiElement.WATER, QiElement.EARTH)),
    DUAL_FIRE_EARTH("dual_fire_earth", Rarity.SR, SpiritRoot.dualRoot(QiElement.FIRE, QiElement.EARTH)),
    TRIPLE("triple", Rarity.R, SpiritRootBonus.IDENTITY),
    QUADRUPLE("quadruple", Rarity.R, SpiritRootBonus.IDENTITY),
    FIVE_ROOT("five_root", Rarity.NORMAL, SpiritRootBonus.IDENTITY),
    FIVE_ELEMENT_CHAOS("five_element_chaos", Rarity.SPECIAL, SpiritRootBonus.builder().primaryElementMult(0.5).offElementMult(0.5).build()),
    BROKEN_VEIN_BODY("broken_vein_body", Rarity.SPECIAL, SpiritRootBonus.builder().meleeDmgMult(2.0).hpMult(2.0).cannotCultivate(true).build());

    private final String id;
    private final Rarity rarity;
    private final SpiritRootBonus bonus;

    private SpiritRoot(String id, Rarity rarity, SpiritRootBonus bonus) {
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

    public SpiritRootBonus bonus() {
        return this.bonus;
    }

    public boolean isSelectableRoot() {
        return this != NONE && this != HEAVENLY_SWORD && this != FIVE_ELEMENT_CHAOS && this != BROKEN_VEIN_BODY;
    }

    public static List<SpiritRoot> selectableValues() {
        ArrayList<SpiritRoot> roots = new ArrayList<SpiritRoot>();
        for (SpiritRoot root : SpiritRoot.values()) {
            if (!root.isSelectableRoot()) continue;
            roots.add(root);
        }
        return List.copyOf(roots);
    }

    public String translationKey() {
        return "spirit_root.friday_cultivation." + this.id;
    }

    public String tooltipKey() {
        return "spirit_root.friday_cultivation." + this.id + ".tooltip";
    }

    public static SpiritRoot byId(String id) {
        for (SpiritRoot r : SpiritRoot.values()) {
            if (!r.id.equals(id)) continue;
            return r;
        }
        return NONE;
    }

    private static SpiritRootBonus heavenlyRoot(QiElement primary, QiElement counter) {
        return SpiritRootBonus.builder().primaryElement(primary).primaryElementMult(1.5).counterElement(counter).counterElementMult(0.5).extraZhenyuanPerSubLevel(1).build();
    }

    private static SpiritRootBonus dualRoot(QiElement e1, QiElement e2) {
        return SpiritRootBonus.builder().primaryElement(e1).secondaryElement(e2).primaryElementMult(1.2).secondaryElementMult(1.2).offElementMult(0.9).build();
    }

    /**
     * 灵根品级枚举 — 严格复刻自原 mod SpiritRoot$Rarity。
     */
    public enum Rarity {
        NORMAL,
        R,
        SR,
        SSR,
        SPECIAL;

        public String translationKey() {
            return "spirit_root_rarity.friday_cultivation." + this.name().toLowerCase();
        }
    }
}
