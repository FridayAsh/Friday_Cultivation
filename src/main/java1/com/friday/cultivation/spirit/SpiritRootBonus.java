package com.friday.cultivation.spirit;

import com.friday.cultivation.spirit.QiElement;

/**
 * 灵根加成数据 — 完整复刻原模组 SpiritRootBonus record
 * 包含主/次/克/生五行元素倍率 + 剑伤倍率 + 非元素法术倍率 + HP倍率 + 每子境界额外真元 + 环境增益 + 不可修炼标记
 */
public record SpiritRootBonus(
        QiElement primaryElement,
        QiElement secondaryElement,
        QiElement counterElement,
        double primaryElementMult,
        double secondaryElementMult,
        double counterElementMult,
        double offElementMult,
        double swordDmgMult,
        double meleeDmgMult,
        double nonElementSpellMult,
        double hpMult,
        int extraZhenyuanPerSubLevel,
        boolean environmentBuff,
        boolean cannotCultivate
) {
    /** 单位加成（无任何加成） */
    public static final SpiritRootBonus IDENTITY = new SpiritRootBonus(
            null, null, null,
            1.0, 1.0, 1.0, 1.0,
            1.0, 1.0, 1.0, 1.0,
            0, false, false
    );

    public static Builder builder() {
        return new Builder();
    }

    /**
     * 灵根加成 Builder — 链式构造，未设置的字段用默认值（1.0 / 0 / false）
     */
    public static final class Builder {
        private QiElement primaryElement = null;
        private QiElement secondaryElement = null;
        private QiElement counterElement = null;
        private double primaryElementMult = 1.0;
        private double secondaryElementMult = 1.0;
        private double counterElementMult = 1.0;
        private double offElementMult = 1.0;
        private double swordDmgMult = 1.0;
        private double meleeDmgMult = 1.0;
        private double nonElementSpellMult = 1.0;
        private double hpMult = 1.0;
        private int extraZhenyuanPerSubLevel = 0;
        private boolean environmentBuff = false;
        private boolean cannotCultivate = false;

        public Builder primaryElement(QiElement v) { this.primaryElement = v; return this; }
        public Builder secondaryElement(QiElement v) { this.secondaryElement = v; return this; }
        public Builder counterElement(QiElement v) { this.counterElement = v; return this; }
        public Builder primaryElementMult(double v) { this.primaryElementMult = v; return this; }
        public Builder secondaryElementMult(double v) { this.secondaryElementMult = v; return this; }
        public Builder counterElementMult(double v) { this.counterElementMult = v; return this; }
        public Builder offElementMult(double v) { this.offElementMult = v; return this; }
        public Builder swordDmgMult(double v) { this.swordDmgMult = v; return this; }
        public Builder meleeDmgMult(double v) { this.meleeDmgMult = v; return this; }
        public Builder nonElementSpellMult(double v) { this.nonElementSpellMult = v; return this; }
        public Builder hpMult(double v) { this.hpMult = v; return this; }
        public Builder extraZhenyuanPerSubLevel(int v) { this.extraZhenyuanPerSubLevel = v; return this; }
        public Builder environmentBuff(boolean v) { this.environmentBuff = v; return this; }
        public Builder cannotCultivate(boolean v) { this.cannotCultivate = v; return this; }

        public SpiritRootBonus build() {
            return new SpiritRootBonus(
                    primaryElement, secondaryElement, counterElement,
                    primaryElementMult, secondaryElementMult, counterElementMult, offElementMult,
                    swordDmgMult, meleeDmgMult, nonElementSpellMult, hpMult,
                    extraZhenyuanPerSubLevel, environmentBuff, cannotCultivate
            );
        }
    }
}
