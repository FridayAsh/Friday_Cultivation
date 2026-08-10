package com.friday.cultivation.physique;

import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.spell.SpellElement;

/**
 * 体质加成数据 — 完整复刻原模组 PhysiqueBonus record
 * 字段：HP倍率/固定HP加成/近战/剑法/金木水火土/纯系法伤/灵气吸收范围/吸收倍率/消耗倍率/受伤倍率/最大灵气倍率/抗性回血/不可修炼
 */
public record PhysiqueBonus(
        double hpMult,
        int maxHpBonus,
        double meleeDmgMult,
        double swordSpellMult,
        double metalSpellMult,
        double woodSpellMult,
        double waterSpellMult,
        double fireSpellMult,
        double earthSpellMult,
        double pureSpellMult,
        int qiAbsorbRange,
        double qiAbsorbMult,
        double qiCostMult,
        double damageTakenMult,
        double maxQiMult,
        boolean resistanceRegen,
        boolean cannotCultivate
) {
    /** 单位加成（无任何加成），所有倍率默认1.0 */
    public static final PhysiqueBonus IDENTITY = builder().build();

    /**
     * 根据法术返回体质对该法术的伤害倍率
     * - 按法术元素取对应五行/纯系倍率
     * - 剑类法术额外乘 swordSpellMult
     */
    public double spellMultiplier(Spell spell) {
        if (spell == null) return 1.0;
        double elementMult = 1.0;
        SpellElement element = spell.element();
        QiElement qi = (element == null || element == SpellElement.NONE) ? QiElement.PURE : element.matchingQi();
        if (qi == null) qi = QiElement.PURE;
        switch (qi) {
            case METAL -> elementMult = this.metalSpellMult;
            case WOOD -> elementMult = this.woodSpellMult;
            case WATER, ICE -> elementMult = this.waterSpellMult;
            case FIRE -> elementMult = this.fireSpellMult;
            case EARTH -> elementMult = this.earthSpellMult;
            case PURE, LIGHTNING -> elementMult = this.pureSpellMult;
            default -> { /* 1.0 */ }
        }
        double swordMult = spell.isSwordSpell() ? this.swordSpellMult : 1.0;
        return Math.max(0.0, elementMult * swordMult);
    }

    public static Builder builder() {
        return new Builder();
    }

    /** 体质加成 Builder — 链式构造，未设置字段默认 1.0/0/false */
    public static final class Builder {
        private double hpMult = 1.0;
        private int maxHpBonus = 0;
        private double meleeDmgMult = 1.0;
        private double swordSpellMult = 1.0;
        private double metalSpellMult = 1.0;
        private double woodSpellMult = 1.0;
        private double waterSpellMult = 1.0;
        private double fireSpellMult = 1.0;
        private double earthSpellMult = 1.0;
        private double pureSpellMult = 1.0;
        private int qiAbsorbRange = 0;
        private double qiAbsorbMult = 1.0;
        private double qiCostMult = 1.0;
        private double damageTakenMult = 1.0;
        private double maxQiMult = 1.0;
        private boolean resistanceRegen = false;
        private boolean cannotCultivate = false;

        public Builder hpMult(double v) { this.hpMult = v; return this; }
        public Builder maxHpBonus(int v) { this.maxHpBonus = v; return this; }
        public Builder meleeDmgMult(double v) { this.meleeDmgMult = v; return this; }
        public Builder swordSpellMult(double v) { this.swordSpellMult = v; return this; }
        public Builder metalSpellMult(double v) { this.metalSpellMult = v; return this; }
        public Builder woodSpellMult(double v) { this.woodSpellMult = v; return this; }
        public Builder waterSpellMult(double v) { this.waterSpellMult = v; return this; }
        public Builder fireSpellMult(double v) { this.fireSpellMult = v; return this; }
        public Builder earthSpellMult(double v) { this.earthSpellMult = v; return this; }
        public Builder pureSpellMult(double v) { this.pureSpellMult = v; return this; }
        public Builder qiAbsorbRange(int v) { this.qiAbsorbRange = v; return this; }
        public Builder qiAbsorbMult(double v) { this.qiAbsorbMult = v; return this; }
        public Builder qiCostMult(double v) { this.qiCostMult = v; return this; }
        public Builder damageTakenMult(double v) { this.damageTakenMult = v; return this; }
        public Builder maxQiMult(double v) { this.maxQiMult = v; return this; }
        public Builder resistanceRegen(boolean v) { this.resistanceRegen = v; return this; }
        public Builder cannotCultivate(boolean v) { this.cannotCultivate = v; return this; }

        public PhysiqueBonus build() {
            return new PhysiqueBonus(
                    hpMult, maxHpBonus, meleeDmgMult, swordSpellMult,
                    metalSpellMult, woodSpellMult, waterSpellMult, fireSpellMult, earthSpellMult, pureSpellMult,
                    qiAbsorbRange, qiAbsorbMult, qiCostMult, damageTakenMult, maxQiMult,
                    resistanceRegen, cannotCultivate
            );
        }
    }
}
