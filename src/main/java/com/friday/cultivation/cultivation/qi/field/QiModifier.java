/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.qi.field;

import com.friday.cultivation.cultivation.qi.BlockQiSpec;

public record QiModifier(double maxMult, double regenMult, double emitMult, double drainMult) {
    public static final QiModifier IDENTITY = new QiModifier(1.0, 1.0, 1.0, 1.0);
    public static final QiModifier QI_GATHERING = new QiModifier(1.5, 2.0, 2.0, 1.0);

    public QiModifier compose(QiModifier other) {
        if (this == IDENTITY) {
            return other;
        }
        if (other == IDENTITY) {
            return this;
        }
        return new QiModifier(this.maxMult * other.maxMult, this.regenMult * other.regenMult, this.emitMult * other.emitMult, this.drainMult * other.drainMult);
    }

    public BlockQiSpec applyTo(BlockQiSpec base) {
        if (this == IDENTITY || base == null) {
            return base;
        }
        return new BlockQiSpec(base.element(), Math.max(1, (int)Math.round((double)base.baseMaxQi() * this.maxMult)), base.baseRegenPerSec() * this.regenMult, Math.min(1.0, base.baseEmitRate() * this.emitMult), base.degradeRule(), base.upgradeRule());
    }
}

