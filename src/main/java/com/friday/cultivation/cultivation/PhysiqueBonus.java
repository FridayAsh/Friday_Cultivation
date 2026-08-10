/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.spell.SpellElement;

public record PhysiqueBonus(double hpMult, int maxHpBonus, double meleeDmgMult, double swordSpellMult, double metalSpellMult, double woodSpellMult, double waterSpellMult, double fireSpellMult, double earthSpellMult, double pureSpellMult, int qiAbsorbRange, double qiAbsorbMult, double qiCostMult, double damageTakenMult, double maxQiMult, boolean resistanceRegen, boolean cannotCultivate) {
    public static final PhysiqueBonus IDENTITY = PhysiqueBonus.builder().build();

    public double spellMultiplier(Spell spell) {
        QiElement qi;
        if (spell == null) {
            return 1.0;
        }
        double elementMult = 1.0;
        SpellElement element = spell.element();
        QiElement qiElement = qi = element == null || element == SpellElement.NONE ? QiElement.PURE : element.matchingQi();
        if (qi == QiElement.METAL) {
            elementMult = this.metalSpellMult;
        } else if (qi == QiElement.WOOD) {
            elementMult = this.woodSpellMult;
        } else if (qi == QiElement.WATER || qi == QiElement.ICE) {
            elementMult = this.waterSpellMult;
        } else if (qi == QiElement.FIRE) {
            elementMult = this.fireSpellMult;
        } else if (qi == QiElement.EARTH) {
            elementMult = this.earthSpellMult;
        } else if (qi == QiElement.PURE || qi == QiElement.LIGHTNING) {
            elementMult = this.pureSpellMult;
        }
        double swordMult = spell.isSwordSpell() ? this.swordSpellMult : 1.0;
        return Math.max(0.0, elementMult * swordMult);
    }

    public static Builder builder() {
        return new Builder();
    }

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

        public Builder hpMult(double value) {
            this.hpMult = value;
            return this;
        }

        public Builder maxHpBonus(int value) {
            this.maxHpBonus = value;
            return this;
        }

        public Builder meleeDmgMult(double value) {
            this.meleeDmgMult = value;
            return this;
        }

        public Builder swordSpellMult(double value) {
            this.swordSpellMult = value;
            return this;
        }

        public Builder metalSpellMult(double value) {
            this.metalSpellMult = value;
            return this;
        }

        public Builder woodSpellMult(double value) {
            this.woodSpellMult = value;
            return this;
        }

        public Builder waterSpellMult(double value) {
            this.waterSpellMult = value;
            return this;
        }

        public Builder fireSpellMult(double value) {
            this.fireSpellMult = value;
            return this;
        }

        public Builder earthSpellMult(double value) {
            this.earthSpellMult = value;
            return this;
        }

        public Builder pureSpellMult(double value) {
            this.pureSpellMult = value;
            return this;
        }

        public Builder qiAbsorbRange(int value) {
            this.qiAbsorbRange = value;
            return this;
        }

        public Builder qiAbsorbMult(double value) {
            this.qiAbsorbMult = value;
            return this;
        }

        public Builder qiCostMult(double value) {
            this.qiCostMult = value;
            return this;
        }

        public Builder damageTakenMult(double value) {
            this.damageTakenMult = value;
            return this;
        }

        public Builder maxQiMult(double value) {
            this.maxQiMult = value;
            return this;
        }

        public Builder resistanceRegen(boolean value) {
            this.resistanceRegen = value;
            return this;
        }

        public Builder cannotCultivate(boolean value) {
            this.cannotCultivate = value;
            return this;
        }

        public PhysiqueBonus build() {
            return new PhysiqueBonus(this.hpMult, this.maxHpBonus, this.meleeDmgMult, this.swordSpellMult, this.metalSpellMult, this.woodSpellMult, this.waterSpellMult, this.fireSpellMult, this.earthSpellMult, this.pureSpellMult, this.qiAbsorbRange, this.qiAbsorbMult, this.qiCostMult, this.damageTakenMult, this.maxQiMult, this.resistanceRegen, this.cannotCultivate);
        }
    }
}

