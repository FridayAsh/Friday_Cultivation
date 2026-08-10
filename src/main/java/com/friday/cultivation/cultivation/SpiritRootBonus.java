/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.QiElement;

public record SpiritRootBonus(QiElement primaryElement, QiElement secondaryElement, QiElement counterElement, double primaryElementMult, double secondaryElementMult, double counterElementMult, double offElementMult, double swordDmgMult, double meleeDmgMult, double nonElementSpellMult, double hpMult, int extraZhenyuanPerSubLevel, boolean environmentBuff, boolean cannotCultivate) {
    public static final SpiritRootBonus IDENTITY = new SpiritRootBonus(null, null, null, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 1.0, 0, false, false);

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder primaryElement(QiElement e) {
            this.primaryElement = e;
            return this;
        }

        public Builder secondaryElement(QiElement e) {
            this.secondaryElement = e;
            return this;
        }

        public Builder counterElement(QiElement e) {
            this.counterElement = e;
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
            return new SpiritRootBonus(this.primaryElement, this.secondaryElement, this.counterElement, this.primaryElementMult, this.secondaryElementMult, this.counterElementMult, this.offElementMult, this.swordDmgMult, this.meleeDmgMult, this.nonElementSpellMult, this.hpMult, this.extraZhenyuanPerSubLevel, this.environmentBuff, this.cannotCultivate);
        }
    }
}

