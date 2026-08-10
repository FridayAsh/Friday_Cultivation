/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingJumpEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.event.SoulHookHandler;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class ZhenyuanJumpHandler {
    private static final double VANILLA_BASE_JUMP_VELOCITY = 0.42;
    private static final double MIN_MOMENTUM_TO_PRESERVE = 0.025;
    private static final double MOMENTUM_RESTORE_THRESHOLD_SQ = 0.8464;
    private static final int GROUND_MOMENTUM_MAX_AGE_TICKS = 4;
    private static final int POST_JUMP_PROTECT_TICKS = 6;
    private static final Map<MomentumKey, HorizontalMomentum> LAST_GROUND_MOMENTUM = new HashMap<MomentumKey, HorizontalMomentum>();
    private static final Map<MomentumKey, JumpMomentumFloor> ACTIVE_JUMP_MOMENTUM = new HashMap<MomentumKey, JumpMomentumFloor>();

    private ZhenyuanJumpHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        MomentumKey key = ZhenyuanJumpHandler.key(player);
        if (event.phase == TickEvent.Phase.START) {
            ZhenyuanJumpHandler.applyActiveMomentumFloor(player, key);
            return;
        }
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!player.onGround()) {
            ZhenyuanJumpHandler.applyActiveMomentumFloor(player, key);
            return;
        }
        Vec3 v = player.getDeltaMovement();
        double horizontalSq = ZhenyuanJumpHandler.horizontalLengthSq(v);
        if (horizontalSq >= 6.250000000000001E-4) {
            LAST_GROUND_MOMENTUM.put(key, new HorizontalMomentum(v.x, v.z, player.tickCount, player.isSprinting()));
        } else {
            LAST_GROUND_MOMENTUM.remove(key);
        }
        ACTIVE_JUMP_MOMENTUM.remove(key);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player)) {
            return;
        }
        Player player = (Player)livingEntity;
        if (!player.level().isClientSide && SoulHookHandler.isActionLocked((Entity)player)) {
            return;
        }
        double mult = ZhenyuanBonusHelper.agilityJumpVelocityMult(player);
        if (mult <= 0.0) {
            return;
        }
        Vec3 v = player.getDeltaMovement();
        MomentumKey key = ZhenyuanJumpHandler.key(player);
        HorizontalMomentum previous = ZhenyuanJumpHandler.validGroundMomentum(player, key);
        Vec3 horizontal = ZhenyuanJumpHandler.preservedHorizontalVelocity(previous, v);
        player.setDeltaMovement(horizontal.x, v.y + 0.42 * mult, horizontal.z);
        player.hasImpulse = true;
        if (previous != null && previous.sprinting && ZhenyuanJumpHandler.horizontalLengthSq(previous.x, previous.z) >= 6.250000000000001E-4) {
            ACTIVE_JUMP_MOMENTUM.put(key, new JumpMomentumFloor(previous.x, previous.z, player.tickCount + 6));
        }
    }

    private static Vec3 preservedHorizontalVelocity(HorizontalMomentum previous, Vec3 current) {
        if (previous == null) {
            return current;
        }
        double previousSq = previous.x * previous.x + previous.z * previous.z;
        double currentSq = ZhenyuanJumpHandler.horizontalLengthSq(current);
        if (previousSq <= currentSq || previousSq < 6.250000000000001E-4) {
            return current;
        }
        return new Vec3(previous.x, current.y, previous.z);
    }

    private static HorizontalMomentum validGroundMomentum(Player player, MomentumKey key) {
        HorizontalMomentum previous = LAST_GROUND_MOMENTUM.get(key);
        if (previous == null || player.tickCount - previous.tick > 4) {
            return null;
        }
        return previous;
    }

    private static void applyActiveMomentumFloor(Player player, MomentumKey key) {
        JumpMomentumFloor floor = ACTIVE_JUMP_MOMENTUM.get(key);
        if (floor == null) {
            return;
        }
        if (player.onGround() || player.tickCount > floor.expireTick) {
            ACTIVE_JUMP_MOMENTUM.remove(key);
            return;
        }
        Vec3 current = player.getDeltaMovement();
        double targetSq = floor.x * floor.x + floor.z * floor.z;
        if (targetSq < 6.250000000000001E-4) {
            return;
        }
        double currentSq = ZhenyuanJumpHandler.horizontalLengthSq(current);
        if (currentSq >= targetSq * 0.8464) {
            return;
        }
        Vec3 restored = ZhenyuanJumpHandler.scaleHorizontalTo(current, floor.x, floor.z, targetSq);
        player.setDeltaMovement(restored.x, current.y, restored.z);
        player.hasImpulse = true;
    }

    private static Vec3 scaleHorizontalTo(Vec3 current, double fallbackX, double fallbackZ, double targetSq) {
        double currentSq = ZhenyuanJumpHandler.horizontalLengthSq(current);
        if (currentSq <= 6.250000000000001E-4) {
            return new Vec3(fallbackX, current.y, fallbackZ);
        }
        double scale = Math.sqrt(targetSq / currentSq);
        return new Vec3(current.x * scale, current.y, current.z * scale);
    }

    private static double horizontalLengthSq(Vec3 v) {
        return v.x * v.x + v.z * v.z;
    }

    private static double horizontalLengthSq(double x, double z) {
        return x * x + z * z;
    }

    private static MomentumKey key(Player player) {
        return new MomentumKey(player.getUUID(), player.level().isClientSide);
    }

    private record MomentumKey(UUID playerId, boolean clientSide) {
    }

    private record HorizontalMomentum(double x, double z, int tick, boolean sprinting) {
    }

    private record JumpMomentumFloor(double x, double z, int expireTick) {
    }
}

