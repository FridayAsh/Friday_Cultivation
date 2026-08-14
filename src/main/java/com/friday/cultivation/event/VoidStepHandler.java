/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$ServerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.NascentSoulOutOfBodyHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.network.VoidStepPacket;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class VoidStepHandler {
    public static final double SLOW_FALL_VELOCITY_CAP = -0.1;
    private static final double HELD_INPUT_BOOST_PER_TICK = 0.06;
    private static final double HELD_INPUT_RETENTION = 0.9;
    private static final double HELD_INPUT_MAX_LATERAL_SPEED = 0.42;
    private static final int HELD_INPUT_KEEPALIVE_TICKS = 4;
    private static final int HORIZONTAL_DIR_MASK = 15;
    private static final double DASH_BURST_SPEED = 1.2;
    private static final int DASH_DECAY_TICKS = 18;
    public static final double SLOW_FALL_TRIGGER_HEIGHT = 3.5;
    public static final int SLOW_FALL_FULL_AIR_BLOCKS = 3;
    private static final double JUMP_3_BLOCKS_VY = 0.85;
    private static final int JUMP_3_QI_COST = 15;
    private static final Map<UUID, DashState> DASH_PLAYERS = new HashMap<UUID, DashState>();
    private static final Map<UUID, HeldInputState> HELD_INPUT_PLAYERS = new HashMap<UUID, HeldInputState>();

    private VoidStepHandler() {
    }

    public static void handlePacket(ServerPlayer player, VoidStepPacket.Op op, int dirBits, float yaw) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        if (!VoidStepHandler.canUseVoidStep(data)) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            DASH_PLAYERS.remove(player.getUUID());
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)player)) {
            DASH_PLAYERS.remove(player.getUUID());
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        switch (op) {
            case JUMP_3_BLOCKS: {
                VoidStepHandler.handleJump3Blocks(player, data);
                break;
            }
            case DASH: {
                VoidStepHandler.handleDash(player, data, dirBits, yaw);
                break;
            }
            case HELD_INPUT: {
                VoidStepHandler.handleHeldInput(player, data, dirBits, yaw);
            }
        }
    }

    private static void handleHeldInput(ServerPlayer player, CultivationData data, int dirBits, float yaw) {
        if (SoulHookHandler.isActionLocked((Entity)player)) {
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        if (!VoidStepHandler.canReceiveHeldInputBoost(player, data)) {
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        Vec3 dir = VoidStepHandler.horizontalDirectionFromBits(dirBits, yaw);
        if (dir.lengthSqr() < 1.0E-6) {
            HELD_INPUT_PLAYERS.remove(player.getUUID());
            return;
        }
        HeldInputState state = new HeldInputState(4, dir.x, dir.z);
        HELD_INPUT_PLAYERS.put(player.getUUID(), state);
        VoidStepHandler.applyHeldInputBoost(player, state);
    }

    private static boolean canUseVoidStep(CultivationData data) {
        if (data.getRealm().ordinal() < Realm.VOID_REFINING.ordinal()) {
            return false;
        }
        return data.isSpellEnabled(Spell.VOID_STEP);
    }

    private static void handleJump3Blocks(ServerPlayer player, CultivationData data) {
        if (!player.onGround() && player.fallDistance >= 0.5f) {
            return;
        }
        if (data.getCurrentQi() < 15L) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)player)) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - 15L);
        Vec3 v = player.getDeltaMovement();
        player.setDeltaMovement(v.x, 0.85, v.z);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        ServerLevel level = player.serverLevel();
        level.sendParticles((ParticleOptions)ParticleTypes.CLOUD, player.getX(), player.getY() + 0.05, player.getZ(), 14, 0.4, 0.05, 0.4, 0.05);
        CapabilityEvents.syncToClient(player);
    }

    private static void handleDash(ServerPlayer player, CultivationData data, int dirBits, float yaw) {
        double len;
        if (data.getCurrentQi() < 60L) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)player)) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            return;
        }
        if (dirBits == 0) {
            return;
        }
        if (!VoidStepHandler.isFarEnoughAboveGround(player)) {
            return;
        }
        Vec3 horizontal = VoidStepHandler.horizontalDirectionFromBits(dirBits, yaw);
        double dx = horizontal.x;
        double dy = 0.0;
        double dz = horizontal.z;
        if ((dirBits & 0x10) != 0) {
            dy += -1.0;
        }
        if ((dirBits & 0x20) != 0) {
            dy += 1.0;
        }
        if ((len = Math.sqrt(dx * dx + dy * dy + dz * dz)) < 1.0E-6) {
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - 60L);
        player.setDeltaMovement((dx /= len) * 1.2, (dy /= len) * 1.2, (dz /= len) * 1.2);
        player.hurtMarked = true;
        player.fallDistance = 0.0f;
        DASH_PLAYERS.put(player.getUUID(), new DashState(18, dx, dy, dz));
        CapabilityEvents.syncToClient(player);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (event.getServer() == null) {
            return;
        }
        for (ServerPlayer player : event.getServer().getPlayerList().getPlayers()) {
            VoidStepHandler.applyAutoSlowFall(player);
        }
        Iterator<Map.Entry<UUID, HeldInputState>> heldIt = HELD_INPUT_PLAYERS.entrySet().iterator();
        while (heldIt.hasNext()) {
            CultivationData data;
            Map.Entry<UUID, HeldInputState> entry = heldIt.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            HeldInputState state = entry.getValue();
            CultivationData cultivationData = data = player == null ? null : (CultivationData)CultivationCapability.get((Player)player).orElse(null);
            if (player != null && RealmPressureHandler.isSuppressed((LivingEntity)player)) {
                heldIt.remove();
                continue;
            }
            if (player != null && SoulHookHandler.isActionLocked((Entity)player)) {
                heldIt.remove();
                continue;
            }
            if (player == null || state.ticksRemaining <= 0 || data == null || !VoidStepHandler.canUseVoidStep(data) || !VoidStepHandler.canReceiveHeldInputBoost(player, data)) {
                heldIt.remove();
                continue;
            }
            if (!DASH_PLAYERS.containsKey(player.getUUID())) {
                VoidStepHandler.applyHeldInputBoost(player, state);
            }
            --state.ticksRemaining;
            if (state.ticksRemaining > 0) continue;
            heldIt.remove();
        }
        Iterator<Map.Entry<UUID, DashState>> dashIt = DASH_PLAYERS.entrySet().iterator();
        while (dashIt.hasNext()) {
            Map.Entry<UUID, DashState> entry = dashIt.next();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(entry.getKey());
            DashState state = entry.getValue();
            if (player != null && RealmPressureHandler.isSuppressed((LivingEntity)player)) {
                dashIt.remove();
                continue;
            }
            if (player != null && SoulHookHandler.isActionLocked((Entity)player)) {
                dashIt.remove();
                continue;
            }
            if (player == null || state.ticksRemaining <= 0) {
                dashIt.remove();
                continue;
            }
            float t = (float)state.ticksRemaining / 18.0f;
            double speed = 1.2 * (double)t;
            Vec3 v = player.getDeltaMovement();
            double vy = state.dirY != 0.0 ? state.dirY * speed : v.y;
            player.setDeltaMovement(state.dirX * speed, vy, state.dirZ * speed);
            player.hurtMarked = true;
            player.fallDistance = 0.0f;
            --state.ticksRemaining;
            if (state.ticksRemaining > 0) continue;
            dashIt.remove();
        }
    }

    private static void applyAutoSlowFall(ServerPlayer player) {
        if (SoulHookHandler.isActionLocked((Entity)player)) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            return;
        }
        if (player.onGround()) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        if (player.getAbilities().flying) {
            return;
        }
        if (NascentSoulOutOfBodyHandler.isActive(player)) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !VoidStepHandler.canUseVoidStep(data)) {
            return;
        }
        if (!VoidStepHandler.isFarEnoughAboveGround(player)) {
            return;
        }
        Vec3 v = player.getDeltaMovement();
        if (v.y < -0.1) {
            player.setDeltaMovement(v.x, -0.1, v.z);
            player.hurtMarked = true;
        }
        player.fallDistance = 0.0f;
        if ((player.tickCount & 7) == 0) {
            player.serverLevel().sendParticles((ParticleOptions)ParticleTypes.CLOUD, player.getX(), player.getY() - 0.1, player.getZ(), 2, 0.3, 0.05, 0.3, 0.0);
        }
    }

    private static boolean canReceiveHeldInputBoost(ServerPlayer player, CultivationData data) {
        if (player.onGround()) {
            return false;
        }
        if (player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)player)) {
            return false;
        }
        if (player.getAbilities().flying) {
            return false;
        }
        if (NascentSoulOutOfBodyHandler.isActive(player)) {
            return false;
        }
        return VoidStepHandler.isFarEnoughAboveGround(player);
    }

    private static void applyHeldInputBoost(ServerPlayer player, HeldInputState state) {
        Vec3 v = player.getDeltaMovement();
        double newVx = v.x * 0.9 + state.dirX * 0.06;
        double newVz = v.z * 0.9 + state.dirZ * 0.06;
        double newMag = Math.sqrt(newVx * newVx + newVz * newVz);
        if (newMag > 0.42) {
            double scale = 0.42 / newMag;
            newVx *= scale;
            newVz *= scale;
        }
        player.setDeltaMovement(newVx, v.y, newVz);
        player.hurtMarked = true;
    }

    private static Vec3 horizontalDirectionFromBits(int dirBits, float yaw) {
        double len;
        int horizontalBits = dirBits & 0xF;
        if (horizontalBits == 0) {
            return Vec3.ZERO;
        }
        double yawRad = Math.toRadians(yaw);
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);
        double dx = 0.0;
        double dz = 0.0;
        if ((horizontalBits & 1) != 0) {
            dx += -sinY;
            dz += cosY;
        }
        if ((horizontalBits & 2) != 0) {
            dx += sinY;
            dz += -cosY;
        }
        if ((horizontalBits & 4) != 0) {
            dx += cosY;
            dz += sinY;
        }
        if ((horizontalBits & 8) != 0) {
            dx += -cosY;
            dz += -sinY;
        }
        return (len = Math.sqrt(dx * dx + dz * dz)) < 1.0E-6 ? Vec3.ZERO : new Vec3(dx / len, 0.0, dz / len);
    }

    public static boolean hasSlowFallClearance(Level level, BlockPos feet, double feetY) {
        for (int dy = 1; dy <= 3; ++dy) {
            if (level.getBlockState(feet.below(dy)).isAir()) continue;
            return false;
        }
        double fraction = feetY - Math.floor(feetY);
        if (fraction + 1.0E-6 < 0.5) {
            return level.getBlockState(feet.below(4)).isAir();
        }
        return true;
    }

    private static boolean isFarEnoughAboveGround(ServerPlayer player) {
        return VoidStepHandler.hasSlowFallClearance(player.level(), player.blockPosition(), player.getY());
    }

    private static final class HeldInputState {
        int ticksRemaining;
        final double dirX;
        final double dirZ;

        HeldInputState(int ticks, double dirX, double dirZ) {
            this.ticksRemaining = ticks;
            this.dirX = dirX;
            this.dirZ = dirZ;
        }
    }

    private static final class DashState {
        int ticksRemaining;
        final double dirX;
        final double dirY;
        final double dirZ;

        DashState(int ticks, double dirX, double dirY, double dirZ) {
            this.ticksRemaining = ticks;
            this.dirX = dirX;
            this.dirY = dirY;
            this.dirZ = dirZ;
        }
    }
}

