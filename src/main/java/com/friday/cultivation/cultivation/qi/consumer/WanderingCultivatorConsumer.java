/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi.consumer;

import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.IQiConsumer;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public final class WanderingCultivatorConsumer
implements IQiConsumer {
    public static final double ATTRACT_RADIUS = 6.0;
    public static final double ABSORB_EFFICIENCY = 0.5;
    private final WanderingCultivatorEntity npc;

    private WanderingCultivatorConsumer(WanderingCultivatorEntity npc) {
        this.npc = npc;
    }

    @Nullable
    public static WanderingCultivatorConsumer wrap(WanderingCultivatorEntity npc) {
        if (npc == null || !npc.isAlive()) {
            return null;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)npc)) {
            return null;
        }
        if (npc.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
            return null;
        }
        if (npc.getMaxQi() <= 0L) {
            return null;
        }
        return new WanderingCultivatorConsumer(npc);
    }

    public WanderingCultivatorEntity npc() {
        return this.npc;
    }

    @Override
    public Vec3 position() {
        return this.npc.position().add(0.0, (double)this.npc.getEyeHeight() * 0.6, 0.0);
    }

    @Override
    public double attractRadius() {
        return 6.0 + (double)this.immortalAbsorbRangeBonus();
    }

    @Override
    public boolean wantsMore() {
        return this.npc.getCurrentQi() < this.npc.getMaxQi();
    }

    @Override
    public int receiveQi(QiElement element, int baseAmount) {
        double efficiency = 0.5 * this.immortalAbsorbMultiplier();
        long actual = Math.max(1L, (long)Math.ceil((double)baseAmount * (efficiency *= PhysiqueBonusHelper.qiAbsorbElementMultiplier(this.npc.getPhysique(), element))));
        long gained = this.npc.addQi(actual);
        return (int)Math.min(gained, Integer.MAX_VALUE);
    }

    private int immortalAbsorbRangeBonus() {
        int bonus = 0;
        if (this.hasImmortalTechniqueOrLegacySpell()) {
            bonus += 10;
        }
        return bonus += this.npc.getPhysique().bonus().qiAbsorbRange();
    }

    private double immortalAbsorbMultiplier() {
        double multiplier = this.hasImmortalTechniqueOrLegacySpell() ? 10.0 : 1.0;
        return Double.isFinite(multiplier *= this.npc.getPhysique().bonus().qiAbsorbMult()) ? Math.max(0.0, multiplier) : 1.0;
    }

    private boolean hasImmortalTechniqueOrLegacySpell() {
        return Technique.IMMORTAL_INCANTATION.id().equals(this.npc.getTechniqueId()) || this.npc.getSpellIds().contains(Spell.IMMORTAL_INCANTATION.id());
    }
}

