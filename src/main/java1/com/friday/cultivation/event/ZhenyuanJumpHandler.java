package com.friday.cultivation.event;

import com.friday.cultivation.ZhenyuanBonusHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.Map;

/**
 * 真元跳跃 handler - 监听 LivingJumpEvent，应用 ZhenyuanBonusHelper 的跳跃加速倍率，
 * 在玩家身上保持横向动量。严格 1:1 复刻原 mod ZhenyuanJumpHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class ZhenyuanJumpHandler {
    private static final double VANILLA_BASE_JUMP_VELOCITY = 0.42;
    private static final double MIN_MOMENTUM_TO_PRESERVE = 0.025;
    private static final double MOMENTUM_RESTORE_THRESHOLD_SQ = 0.8464;
    private static final int GROUND_MOMENTUM_MAX_AGE_TICKS = 4;
    private static final int POST_JUMP_PROTECT_TICKS = 6;
    private static final Map<MomentumKey, HorizontalMomentum> LAST_GROUND_MOMENTUM = new HashMap<>();
    private static final Map<MomentumKey, JumpMomentumFloor> ACTIVE_JUMP_MOMENTUM = new HashMap<>();

    private ZhenyuanJumpHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        MomentumKey key = key(player);
        if (event.phase == TickEvent.Phase.START) {
            applyActiveMomentumFloor(player, key);
            return;
        }
        if (event.phase != TickEvent.Phase.END) return;
        if (!player.onGround()) {
            applyActiveMomentumFloor(player, key);
            return;
        }
        Vec3 v = player.getDeltaMovement();
        double horizontalSq = horizontalLengthSq(v);
        if (horizontalSq >= 6.25e-4) {
            LAST_GROUND_MOMENTUM.put(key, new HorizontalMomentum(v.x, v.z, player.tickCount, player.isSprinting()));
        } else {
            LAST_GROUND_MOMENTUM.remove(key);
        }
        ACTIVE_JUMP_MOMENTUM.remove(key);
    }

    @SubscribeEvent
    public static void onLivingJump(LivingEvent.LivingJumpEvent event) {
        LivingEntity living = event.getEntity();
        if (!(living instanceof Player player)) return;
        Level level = player.level();
        if (!level.isClientSide() && SoulHookHandler.isActionLocked((Entity) player)) return;
        double mult = ZhenyuanBonusHelper.agilityJumpVelocityMult(player);
        if (mult <= 0.0) return;
        Vec3 v = player.getDeltaMovement();
        MomentumKey key = key(player);
        HorizontalMomentum previous = validGroundMomentum(player, key);
        Vec3 horizontal = preservedHorizontalVelocity(previous, v);
        player.setDeltaMovement(horizontal.x, v.y + VANILLA_BASE_JUMP_VELOCITY * mult, horizontal.z);
        player.hasImpulse = true;
        if (previous != null && previous.sprinting && horizontalLengthSq(previous.x, previous.z) >= 6.25e-4) {
            ACTIVE_JUMP_MOMENTUM.put(key, new JumpMomentumFloor(previous.x, previous.z, player.tickCount + POST_JUMP_PROTECT_TICKS));
        }
    }

    private static Vec3 preservedHorizontalVelocity(HorizontalMomentum previous, Vec3 current) {
        if (previous == null) {
            return new Vec3(current.x, 0.0, current.z);
        }
        double curSq = horizontalLengthSq(current.x, current.z);
        if (curSq < MOMENTUM_RESTORE_THRESHOLD_SQ) {
            return new Vec3(previous.x, current.y, previous.z);
        }
        return current;
    }

    private static void applyActiveMomentumFloor(Player player, MomentumKey key) {
        JumpMomentumFloor floor = ACTIVE_JUMP_MOMENTUM.get(key);
        if (floor == null) return;
        if (player.tickCount > floor.expiresAt) {
            ACTIVE_JUMP_MOMENTUM.remove(key);
            return;
        }
        Vec3 v = player.getDeltaMovement();
        double curSq = horizontalLengthSq(v.x, v.z);
        if (curSq < horizontalLengthSq(floor.x, floor.z)) {
            player.setDeltaMovement(floor.x * 0.6 + v.x * 0.4, v.y, floor.z * 0.6 + v.z * 0.4);
        }
    }

    private static HorizontalMomentum validGroundMomentum(Player player, MomentumKey key) {
        HorizontalMomentum last = LAST_GROUND_MOMENTUM.get(key);
        if (last == null) return null;
        if (player.tickCount - last.capturedAtTick > GROUND_MOMENTUM_MAX_AGE_TICKS) return null;
        return last;
    }

    private static MomentumKey key(Player player) {
        return new MomentumKey(player.getUUID());
    }

    private static double horizontalLengthSq(Vec3 v) {
        return horizontalLengthSq(v.x, v.z);
    }

    private static double horizontalLengthSq(double x, double z) {
        return x * x + z * z;
    }

    private record MomentumKey(java.util.UUID uuid) {
    }

    private record HorizontalMomentum(double x, double z, int capturedAtTick, boolean sprinting) {
    }

    private record JumpMomentumFloor(double x, double z, int expiresAt) {
    }
}
