/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.phys.Vec3
 */
package com.friday.cultivation.entity.npc.ai;

import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

public class CultivatorRangedKitingGoal
extends Goal {
    private static final double MIN_RANGE = 12.0;
    private static final double PREFERRED_RANGE = 18.0;
    private static final double MAX_RANGE = 28.0;
    private static final double GIVE_UP_RANGE = 42.0;
    private static final double GROUND_WALK_SPEED_MODIFIER = 1.0;
    private final WanderingCultivatorEntity npc;
    private int repathDelay = 0;

    public CultivatorRangedKitingGoal(WanderingCultivatorEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.npc.isTradingFreeze()) {
            return false;
        }
        if (this.npc.canUseCombatFlightSoon() || this.npc.isUsingItem()) {
            return false;
        }
        if (!NpcSpellCaster.hasCombatSpell(this.npc)) {
            return false;
        }
        LivingEntity target = this.npc.getTarget();
        return target != null && target.isAlive() && (double)this.npc.distanceTo((Entity)target) <= 42.0;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        LivingEntity target = this.npc.getTarget();
        if (target == null) {
            return;
        }
        this.npc.getLookControl().setLookAt((Entity)target, 30.0f, 30.0f);
        if (this.repathDelay > 0) {
            --this.repathDelay;
            return;
        }
        this.repathDelay = 8 + this.npc.getRandom().nextInt(8);
        double distSqr = this.npc.distanceToSqr((Entity)target);
        if (distSqr < 144.0) {
            this.moveAwayFrom(target);
        } else if (distSqr > 784.0) {
            this.moveTowardPreferredRange(target);
        } else {
            this.npc.getNavigation().stop();
        }
    }

    private void moveAwayFrom(LivingEntity target) {
        Vec3 away = this.npc.position().multiply(target.position());
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(this.npc.getRandom().nextDouble() - 0.5, 0.0, this.npc.getRandom().nextDouble() - 0.5);
        }
        Vec3 dir = new Vec3(away.x, 0.0, away.z).normalize();
        Vec3 dest = this.npc.position().add(dir.scale(8.0));
        this.npc.getNavigation().moveTo(dest.x, this.npc.getY(), dest.z, 1.0);
    }

    private void moveTowardPreferredRange(LivingEntity target) {
        Vec3 fromTarget = this.npc.position().multiply(target.position());
        if (fromTarget.lengthSqr() < 1.0E-4) {
            return;
        }
        Vec3 dir = new Vec3(fromTarget.x, 0.0, fromTarget.z).normalize();
        Vec3 dest = target.position().add(dir.scale(18.0));
        this.npc.getNavigation().moveTo(dest.x, target.getY(), dest.z, 1.0);
    }
}

