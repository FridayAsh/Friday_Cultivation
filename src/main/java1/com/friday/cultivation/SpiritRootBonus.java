package com.friday.cultivation;

/**
 * 灵根加成 Record — 完整复刻原模组 SpiritRootBonus（含 Builder 内部类）。
 * <p>
 * 14 字段：primaryElement / secondaryElement / counterElement (QiElement)、
 * 5 元素倍率 (primary/secondary/counter/off) + swordDmgMelee/nonElement/hpMult、
 * extraZhenyuanPerSubLevel (int) + environmentBuff/cannotCultivate (boolean)。
 * </p>
 * <p>
 * 默认值（IDENTITY）：所有倍率为 1.0，元素=null，cannotCultivate=false。
 * </p>
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
     * 灵根加成 Builder — 严格复刻自原 mod SpiritRootBonus$Builder。
     * 14 字段 + 14 setter + build()。
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

        public Builder primaryElement(QiElement v) {
            this.primaryElement = v;
            return this;
        }

        public Builder secondaryElement(QiElement v) {
            this.secondaryElement = v;
            return this;
        }

        public Builder counterElement(QiElement v) {
            this.counterElement = v;
            return this;
        }

        public Builder primaryElementMult(double v) {
            this.primaryElementMult = v;
            return this;
        }

        public Builder secondaryElementMult(double v) {
            this.secondaryElementMult = v;
            return this;
        }

        public Builder counterElementMult(double v) {
            this.counterElementMult = v;
            return this;
        }

        public Builder offElementMult(double v) {
            this.offElementMult = v;
            return this;
        }

        public Builder swordDmgMult(double v) {
            this.swordDmgMult = v;
            return this;
        }

        public Builder meleeDmgMult(double v) {
            this.meleeDmgMult = v;
            return this;
        }

        public Builder nonElementSpellMult(double v) {
            this.nonElementSpellMult = v;
            return this;
        }

        public Builder hpMult(double v) {
            this.hpMult = v;
            return this;
        }

        public Builder extraZhenyuanPerSubLevel(int v) {
            this.extraZhenyuanPerSubLevel = v;
            return this;
        }

        public Builder environmentBuff(boolean v) {
            this.environmentBuff = v;
            return this;
        }

        public Builder cannotCultivate(boolean v) {
            this.cannotCultivate = v;
            return this;
        }

        public SpiritRootBonus build() {
            return new SpiritRootBonus(
                    this.primaryElement, this.secondaryElement, this.counterElement,
                    this.primaryElementMult, this.secondaryElementMult, this.counterElementMult, this.offElementMult,
                    this.swordDmgMult, this.meleeDmgMult, this.nonElementSpellMult, this.hpMult,
                    this.extraZhenyuanPerSubLevel, this.environmentBuff, this.cannotCultivate
            );
        }
    }
}
