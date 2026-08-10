package com.friday.cultivation.spirit;

import net.minecraft.network.chat.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * 灵根系统 — 25种灵根，完整复刻原模组 SpiritRoot 枚举
 * 含完整 SpiritRootBonus 加成数据（主/次/克/生五行 + 剑伤 + 非元素法术 + HP + 每子境界真元 + 环境增益）
 */
public enum SpiritRoot {
    NONE("none", Rarity.NORMAL, SpiritRootBonus.IDENTITY),
    HEAVENLY_SWORD("heavenly_sword", Rarity.SPECIAL, SpiritRootBonus.builder().swordDmgMult(2.0).nonElementSpellMult(0.5).build()),
    HEAVENLY_METAL("heavenly_metal", Rarity.SSR, heavenlyRoot(QiElement.METAL, QiElement.WOOD)),
    HEAVENLY_WOOD("heavenly_wood", Rarity.SSR, heavenlyRoot(QiElement.WOOD, QiElement.EARTH)),
    HEAVENLY_WATER("heavenly_water", Rarity.SSR, heavenlyRoot(QiElement.WATER, QiElement.FIRE)),
    HEAVENLY_FIRE("heavenly_fire", Rarity.SSR, heavenlyRoot(QiElement.FIRE, QiElement.WATER)),
    HEAVENLY_EARTH("heavenly_earth", Rarity.SSR, heavenlyRoot(QiElement.EARTH, QiElement.WOOD)),
    MUTANT_ICE("mutant_ice", Rarity.SSR, SpiritRootBonus.builder().primaryElement(QiElement.ICE).primaryElementMult(1.6).environmentBuff(true).build()),
    MUTANT_LIGHTNING("mutant_lightning", Rarity.SSR, SpiritRootBonus.builder().primaryElement(QiElement.LIGHTNING).primaryElementMult(1.6).environmentBuff(true).build()),
    HEAVENLY_HIDDEN("heavenly_hidden", Rarity.SSR, SpiritRootBonus.builder().nonElementSpellMult(1.5).build()),
    DUAL_METAL_WOOD("dual_metal_wood", Rarity.SR, dualRoot(QiElement.METAL, QiElement.WOOD)),
    DUAL_METAL_WATER("dual_metal_water", Rarity.SR, dualRoot(QiElement.METAL, QiElement.WATER)),
    DUAL_METAL_FIRE("dual_metal_fire", Rarity.SR, dualRoot(QiElement.METAL, QiElement.FIRE)),
    DUAL_METAL_EARTH("dual_metal_earth", Rarity.SR, dualRoot(QiElement.METAL, QiElement.EARTH)),
    DUAL_WOOD_WATER("dual_wood_water", Rarity.SR, dualRoot(QiElement.WOOD, QiElement.WATER)),
    DUAL_WOOD_FIRE("dual_wood_fire", Rarity.SR, dualRoot(QiElement.WOOD, QiElement.FIRE)),
    DUAL_WOOD_EARTH("dual_wood_earth", Rarity.SR, dualRoot(QiElement.WOOD, QiElement.EARTH)),
    DUAL_WATER_FIRE("dual_water_fire", Rarity.SR, dualRoot(QiElement.WATER, QiElement.FIRE)),
    DUAL_WATER_EARTH("dual_water_earth", Rarity.SR, dualRoot(QiElement.WATER, QiElement.EARTH)),
    DUAL_FIRE_EARTH("dual_fire_earth", Rarity.SR, dualRoot(QiElement.FIRE, QiElement.EARTH)),
    TRIPLE("triple", Rarity.R, SpiritRootBonus.IDENTITY),
    QUADRUPLE("quadruple", Rarity.R, SpiritRootBonus.IDENTITY),
    FIVE_ROOT("five_root", Rarity.NORMAL, SpiritRootBonus.IDENTITY),
    FIVE_ELEMENT_CHAOS("five_element_chaos", Rarity.SPECIAL, SpiritRootBonus.builder().primaryElementMult(0.5).offElementMult(0.5).build()),
    BROKEN_VEIN_BODY("broken_vein_body", Rarity.SPECIAL, SpiritRootBonus.builder().meleeDmgMult(2.0).hpMult(2.0).cannotCultivate(true).build());

    private final String id;
    private final Rarity rarity;
    private final SpiritRootBonus bonus;

    SpiritRoot(String id, Rarity rarity, SpiritRootBonus bonus) {
        this.id = id;
        this.rarity = rarity;
        this.bonus = bonus;
    }

    public String id() { return id; }
    public Rarity rarity() { return rarity; }
    public SpiritRootBonus bonus() { return bonus; }

    public String translationKey() { return "spirit_root.friday_cultivation." + id; }
    public String tooltipKey() { return translationKey() + ".tooltip"; }

    public Component displayName() {
        return Component.translatableWithFallback(translationKey(), id);
    }

    /** 可被抽取选中的灵根（NONE/天剑/五行混沌/废脉不可选） */
    public boolean isSelectableRoot() {
        return this != NONE && this != HEAVENLY_SWORD && this != FIVE_ELEMENT_CHAOS && this != BROKEN_VEIN_BODY;
    }

    /** 可被抽取的灵根列表 */
    public static List<SpiritRoot> selectableValues() {
        List<SpiritRoot> roots = new ArrayList<>();
        for (SpiritRoot root : values()) {
            if (root.isSelectableRoot()) roots.add(root);
        }
        return List.copyOf(roots);
    }

    public static SpiritRoot byId(String id) {
        if (id == null || id.isEmpty()) return NONE;
        for (SpiritRoot r : values()) {
            if (r.id.equals(id)) return r;
        }
        return NONE;
    }

    /** 天灵根工厂：主元素1.5倍，克元素0.5倍，每子境界+1真元 */
    private static SpiritRootBonus heavenlyRoot(QiElement primary, QiElement counter) {
        return SpiritRootBonus.builder()
                .primaryElement(primary).primaryElementMult(1.5)
                .counterElement(counter).counterElementMult(0.5)
                .extraZhenyuanPerSubLevel(1)
                .build();
    }

    /** 双灵根工厂：主次各1.2倍，其他0.9倍 */
    private static SpiritRootBonus dualRoot(QiElement e1, QiElement e2) {
        return SpiritRootBonus.builder()
                .primaryElement(e1).secondaryElement(e2)
                .primaryElementMult(1.2).secondaryElementMult(1.2)
                .offElementMult(0.9)
                .build();
    }

    /** 灵根稀有度 — 复刻原模组 Rarity：NORMAL/SPECIAL/SSR/SR/R */
    public enum Rarity {
        NORMAL, SPECIAL, SSR, SR, R;

        public String translationKey() { return "spirit_root_rarity.friday_cultivation." + name().toLowerCase(); }
        public Component displayName() { return Component.translatableWithFallback(translationKey(), name()); }
    }
}
