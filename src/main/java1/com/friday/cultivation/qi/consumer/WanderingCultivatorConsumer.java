package com.friday.cultivation.qi.consumer;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.qi.IQiConsumer;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.Technique;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * 散修灵气消费方 - WanderingCultivatorEntity 作为 IQiConsumer 接收灵气。
 * 严格 1:1 复刻原 mod: xiaoxiang.cultivation.cultivation.qi.consumer.WanderingCultivatorConsumer
 */
public final class WanderingCultivatorConsumer implements IQiConsumer {
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
        if (SpiritLockHandler.isEntityLocked(npc)) {
            return null;
        }
        if (npc.hasEffect(ModEffects.MERIDIAN_FROZEN.get())) {
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
        return this.npc.position().add(0.0, this.npc.getEyeHeight() * 0.6, 0.0);
    }

    @Override
    public double attractRadius() {
        return ATTRACT_RADIUS + (double) this.immortalAbsorbRangeBonus();
    }

    @Override
    public boolean wantsMore() {
        return this.npc.getCurrentQi() < this.npc.getMaxQi();
    }

    @Override
    public int receiveQi(QiElement element, int baseAmount) {
        double efficiency = ABSORB_EFFICIENCY * this.immortalAbsorbMultiplier();
        efficiency *= PhysiqueBonusHelper.qiAbsorbElementMultiplier(this.npc.getPhysique(), element);
        long actual = Math.max(1L, (long) Math.ceil((double) baseAmount * efficiency));
        long gained = this.npc.addQi(actual);
        return (int) Math.min(gained, Integer.MAX_VALUE);
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
