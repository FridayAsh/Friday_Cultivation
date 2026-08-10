/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.Goal$Flag
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.Vec3
 */
package com.friday.cultivation.entity.npc.ai;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import java.util.EnumSet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;

public class CultivatorFlightCombatGoal
extends Goal {
    private static final double ORBIT_RADIUS = 18.0;
    private static final double ANGULAR_VELOCITY = 0.04;
    private static final double HEIGHT_ABOVE_TARGET = 6.0;
    private static final double FLYING_TARGET_HEIGHT_ABOVE_GROUND = 10.0;
    private static final double MIN_COMBAT_HEIGHT_ABOVE_GROUND = 4.0;
    private static final double MAX_COMBAT_HEIGHT_ABOVE_GROUND = 18.0;
    private static final double MAX_MOVE_PER_TICK = 0.45;
    private static final double MAX_VERTICAL_MOVE_PER_TICK = 0.24;
    private static final double INITIAL_ANGLE_MAX = Math.PI * 2;
    private final WanderingCultivatorEntity npc;
    private double orbitAngle = 0.0;
    private int dodgeTicksRemaining = 0;
    private Vec3 dodgeOffset = Vec3.ZERO;

    public CultivatorFlightCombatGoal(WanderingCultivatorEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        if (this.npc.isTradingFreeze()) {
            return false;
        }
        if (!this.npc.canUseCombatFlight()) {
            return false;
        }
        if (this.npc.isUsingItem()) {
            return false;
        }
        if (!this.npc.isFallFlying()) {
            return false;
        }
        LivingEntity target = this.npc.getTarget();
        return target != null && target.isAlive();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void start() {
        this.orbitAngle = this.npc.getRandom().nextDouble() * (Math.PI * 2);
    }

    @Override
    public void stop() {
        this.dodgeTicksRemaining = 0;
        this.dodgeOffset = Vec3.ZERO;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    @Override
    public void tick() {
        Vec3 motion;
        double dist;
        double dz;
        double dy;
        double dx;
        LivingEntity target = this.npc.getTarget();
        if (target == null) {
            return;
        }
        this.orbitAngle += 0.04;
        if (this.orbitAngle > Math.PI * 2) {
            this.orbitAngle -= Math.PI * 2;
        }
        double cx = target.getX() + Math.cos(this.orbitAngle) * 18.0;
        double cz = target.getZ() + Math.sin(this.orbitAngle) * 18.0;
        double cy = this.computeCombatY(target);
        if (this.dodgeTicksRemaining > 0) {
            --this.dodgeTicksRemaining;
        } else if (this.npc.getRandom().nextInt(40) == 0) {
            dx = (this.npc.getRandom().nextDouble() - 0.5) * 1.0;
            dy = (this.npc.getRandom().nextDouble() - 0.5) * 0.8;
            dz = (this.npc.getRandom().nextDouble() - 0.5) * 1.0;
            this.dodgeOffset = new Vec3(dx, dy, dz);
            this.dodgeTicksRemaining = 5;
        }
        if (this.dodgeTicksRemaining > 0) {
            cx += this.dodgeOffset.x;
            cy += this.dodgeOffset.y;
            cz += this.dodgeOffset.z;
            cy = this.clampCombatY(target, cy);
        }
        if ((dist = Math.sqrt((dx = cx - this.npc.getX()) * dx + (dy = cy - this.npc.getY()) * dy + (dz = cz - this.npc.getZ()) * dz)) < 0.15) {
            double yWave = Math.sin((double)this.npc.tickCount / 10.0) * 0.02;
            motion = new Vec3(0.0, yWave, 0.0);
        } else {
            double speed = Math.min(0.45, dist * 0.18);
            double scale = speed / dist;
            double vertical = CultivatorFlightCombatGoal.clamp(dy * scale, -0.24, 0.24);
            motion = new Vec3(dx * scale, vertical, dz * scale);
        }
        this.npc.setDeltaMovement(motion);
        this.npc.getLookControl().setLookAt((Entity)target, 30.0f, 30.0f);
    }

    /*
     * Unable to fully structure code
     */
    private double computeCombatY(LivingEntity target) {
        int groundY = this.npc.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getBlockX(), target.getBlockZ());
        boolean targetIsFlying = target.isFallFlying() || target.isInWater()
                || (target instanceof Player player && player.getAbilities().flying);
        double desiredY = targetIsFlying ? (double) groundY + 10.0 : target.getEyeY() + 6.0;
        return this.clampCombatY(target, desiredY);
    }

    private double clampCombatY(LivingEntity target, double y) {
        int groundY = this.npc.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, target.getBlockX(), target.getBlockZ());
        double minY = Math.max((double)this.npc.level().getMinBuildHeight() + 1.0, (double)groundY + 4.0);
        double maxY = Math.min((double)this.npc.level().getMaxBuildHeight() - 2.0, (double)groundY + 18.0);
        return CultivatorFlightCombatGoal.clamp(y, minY, maxY);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}

