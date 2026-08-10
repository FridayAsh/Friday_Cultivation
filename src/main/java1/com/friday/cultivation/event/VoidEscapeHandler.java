/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.friday.cultivation.event.CapabilityEvents
 *  com.friday.cultivation.event.PassiveSpellHandler
 *  com.friday.cultivation.event.VoidEscapeHandler$1
 *  com.friday.cultivation.event.VoidEscapeHandler$ExitReason
 *  com.friday.cultivation.network.ModNetwork
 *  com.friday.cultivation.network.VoidEscapeEntryEffectPacket
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.util.Mth
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.phys.AABB
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$Clone
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerChangedDimensionEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.PassiveSpellHandler;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.event.VoidEscapeHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.VoidEscapeEntryEffectPacket;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class VoidEscapeHandler {
    private static final int INVISIBILITY_DELAY_TICKS = 12;
    private static final Map<UUID, Long> PENDING_INVISIBILITY = new HashMap<UUID, Long>();
    private static final String PHASE_APPLIED = "xxcVoidEscapePhaseApplied";
    private static final String PREV_MAYFLY = "xxcVoidEscapePrevMayfly";
    private static final String PREV_FLYING = "xxcVoidEscapePrevFlying";
    private static final String PREV_NO_GRAVITY = "xxcVoidEscapePrevNoGravity";
    private static final String PREV_NO_PHYSICS = "xxcVoidEscapePrevNoPhysics";

    private VoidEscapeHandler() {
    }

    public static void tickCharge(ServerPlayer player, CultivationData data) {
        if (data.isVoidEscapeActive()) {
            data.clearCharging();
            CapabilityEvents.syncToClient((ServerPlayer)player);
            return;
        }
        if (data.isInTribulation()) {
            data.clearCharging();
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.void_escape.blocked_tribulation"));
            CapabilityEvents.syncToClient((ServerPlayer)player);
            return;
        }
        long need = 10L;
        if (data.getCurrentQi() < need) {
            data.clearCharging();
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.void_escape.charge_failed_qi"));
            CapabilityEvents.syncToClient((ServerPlayer)player);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - need);
        data.incrementChargingTicks();
        VoidEscapeHandler.spawnChargeVisuals(player, data);
        if (data.getChargingTicks() >= 100) {
            data.clearCharging();
            VoidEscapeHandler.enter(player, data);
        } else {
            CapabilityEvents.syncToClient((ServerPlayer)player);
        }
    }

    private static void spawnChargeVisuals(ServerPlayer player, CultivationData data) {
        int ticks = data.getChargingTicks();
        if (ticks <= 0) {
            return;
        }
        if (ticks == 1 || ticks % 20 == 0) {
            float progress = Mth.clamp((float)((float)ticks / 100.0f), (float)0.0f, (float)1.0f);
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.1f + progress * 0.1f, 0.7f + progress * 0.25f);
        }
    }

    public static void enter(ServerPlayer player, CultivationData data) {
        if (data.isInTribulation()) {
            data.clearCharging();
            data.clearVoidEscape();
            PENDING_INVISIBILITY.remove(player.getUUID());
            player.setInvisible(false);
            VoidEscapeHandler.restoreVoidMovement(player);
            player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.void_escape.blocked_tribulation"));
            CapabilityEvents.syncToClient((ServerPlayer)player);
            return;
        }
        data.startVoidEscape(10);
        VoidEscapeHandler.applyVoidMovement(player);
        player.setInvisible(false);
        ModNetwork.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), (Object)new VoidEscapeEntryEffectPacket(player.getId()));
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.7f, 0.72f);
        player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 0.32f, 1.35f);
        PENDING_INVISIBILITY.put(player.getUUID(), player.serverLevel().getGameTime() + 12L);
        CapabilityEvents.syncToClient((ServerPlayer)player);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.void_escape.entered"));
    }

    public static void exit(ServerPlayer player, CultivationData data, ExitReason reason) {
        if (!data.isVoidEscapeActive()) {
            return;
        }
        data.clearVoidEscape();
        PENDING_INVISIBILITY.remove(player.getUUID());
        player.setInvisible(false);
        VoidEscapeHandler.moveToNearestSafeExit(player);
        VoidEscapeHandler.restoreVoidMovement(player);
        CapabilityEvents.syncToClient((ServerPlayer)player);
        String msgKey = switch (reason) {
            default -> throw new IncompatibleClassChangeError();
            case STABILITY_DEPLETED -> "message.friday_cultivation.void_escape.exit_stability";
            case QI_DEPLETED -> "message.friday_cultivation.void_escape.exit_qi";
            case MANUAL -> "message.friday_cultivation.void_escape.exit_manual";
            case DIMENSION_CHANGE -> "message.friday_cultivation.void_escape.exit_dimension";
            case TRIBULATION -> "message.friday_cultivation.void_escape.exit_tribulation";
        };
        player.sendSystemMessage((Component)Component.translatable((String)msgKey));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            if (VoidEscapeHandler.hasVoidMovementSnapshot(player2)) {
                VoidEscapeHandler.moveToNearestSafeExit(player2);
            }
            VoidEscapeHandler.restoreVoidMovement(player2);
            return;
        }
        if (data.isInTribulation()) {
            VoidEscapeHandler.exit(player2, data, ExitReason.TRIBULATION);
            return;
        }
        VoidEscapeHandler.applyVoidMovement(player2);
        VoidEscapeHandler.applyDelayedInvisibility(player2);
        long cost = 5L;
        if (data.getCurrentQi() < cost) {
            VoidEscapeHandler.exit(player2, data, ExitReason.QI_DEPLETED);
            return;
        }
        data.setCurrentQi(data.getCurrentQi() - cost);
        if (data.getVoidEscapeStability() <= 0) {
            VoidEscapeHandler.exit(player2, data, ExitReason.STABILITY_DEPLETED);
            return;
        }
        if (!PENDING_INVISIBILITY.containsKey(player2.getUUID()) && player2.tickCount % 40 == 0) {
            player2.setInvisible(true);
        }
        if (player2.tickCount % 5 == 0) {
            CapabilityEvents.syncToClient((ServerPlayer)player2);
        }
    }

    private static void applyDelayedInvisibility(ServerPlayer player) {
        Long dueTick = PENDING_INVISIBILITY.get(player.getUUID());
        if (dueTick == null) {
            return;
        }
        if (player.serverLevel().getGameTime() < dueTick) {
            return;
        }
        player.setInvisible(true);
        PENDING_INVISIBILITY.remove(player.getUUID());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            return;
        }
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
        int remaining = data.decrementVoidEscapeStability();
        CapabilityEvents.syncToClient((ServerPlayer)player);
        if (remaining <= 0) {
            // empty if block
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            return;
        }
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        event.setCanceled(true);
    }

    public static boolean tryManualExitIfActive(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            return false;
        }
        VoidEscapeHandler.exit(player, data, ExitReason.MANUAL);
        return true;
    }

    @SubscribeEvent
    public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            VoidEscapeHandler.restoreVoidMovement(player2);
            return;
        }
        VoidEscapeHandler.exit(player2, data, ExitReason.DIMENSION_CHANGE);
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            boolean hadVoidEscape;
            boolean bl = hadVoidEscape = data.isVoidEscapeActive() || VoidEscapeHandler.hasVoidMovementSnapshot(player2);
            if (data.isVoidEscapeActive()) {
                data.clearVoidEscape();
            }
            PENDING_INVISIBILITY.remove(player2.getUUID());
            player2.setInvisible(false);
            if (hadVoidEscape) {
                VoidEscapeHandler.moveToNearestSafeExit(player2);
            }
            VoidEscapeHandler.restoreVoidMovement(player2);
            CapabilityEvents.syncToClient((ServerPlayer)player2);
        });
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            boolean hadVoidEscape;
            boolean bl = hadVoidEscape = data.isVoidEscapeActive() || VoidEscapeHandler.hasVoidMovementSnapshot(player2);
            if (data.isVoidEscapeActive()) {
                data.clearVoidEscape();
            }
            PENDING_INVISIBILITY.remove(player2.getUUID());
            player2.setInvisible(false);
            if (hadVoidEscape) {
                VoidEscapeHandler.moveToNearestSafeExit(player2);
            }
            VoidEscapeHandler.restoreVoidMovement(player2);
        });
    }

    @SubscribeEvent
    public static void onPlayerClone(PlayerEvent.Clone event) {
        Player player = event.getOriginal();
        if (player instanceof ServerPlayer) {
            ServerPlayer original = (ServerPlayer)player;
            PENDING_INVISIBILITY.remove(original.getUUID());
            original.setInvisible(false);
            VoidEscapeHandler.restoreVoidMovement(original);
        }
        if ((player = event.getEntity()) instanceof ServerPlayer) {
            ServerPlayer player2 = (ServerPlayer)player;
            CultivationCapability.get((Player)player2).ifPresent(data -> {
                if (data.isVoidEscapeActive()) {
                    data.clearVoidEscape();
                }
                PENDING_INVISIBILITY.remove(player2.getUUID());
                player2.setInvisible(false);
                VoidEscapeHandler.restoreVoidMovement(player2);
                CapabilityEvents.syncToClient((ServerPlayer)player2);
            });
        }
    }

    private static void applyVoidMovement(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(PHASE_APPLIED)) {
            tag.putBoolean(PHASE_APPLIED, true);
            tag.putBoolean(PREV_MAYFLY, player.getAbilities().mayBuild);
            tag.putBoolean(PREV_FLYING, player.getAbilities().mayfly);
            tag.putBoolean(PREV_NO_GRAVITY, player.isNoGravity());
            tag.putBoolean(PREV_NO_PHYSICS, player.noPhysics);
        }
        boolean dirty = false;
        player.noPhysics = true;
        if (!player.isNoGravity()) {
            player.setNoGravity(true);
        }
        if (!player.getAbilities().mayBuild) {
            player.getAbilities().mayBuild = true;
            dirty = true;
        }
        if (!player.getAbilities().mayfly) {
            player.getAbilities().mayfly = true;
            dirty = true;
        }
        player.fallDistance = 0.0f;
        if (dirty) {
            player.onUpdateAbilities();
        }
    }

    private static void restoreVoidMovement(ServerPlayer player) {
        CompoundTag tag = player.getPersistentData();
        if (!tag.getBoolean(PHASE_APPLIED)) {
            return;
        }
        boolean prevMayfly = tag.getBoolean(PREV_MAYFLY);
        boolean prevFlying = tag.getBoolean(PREV_FLYING);
        boolean prevNoGravity = tag.getBoolean(PREV_NO_GRAVITY);
        boolean prevNoPhysics = tag.getBoolean(PREV_NO_PHYSICS);
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        boolean passiveFlightAllowed = PassiveSpellHandler.hasEnabledPassiveFlight((CultivationData)data);
        boolean restoredMayfly = prevMayfly || passiveFlightAllowed;
        boolean restoredFlying = prevFlying && restoredMayfly;
        VoidEscapeHandler.removeVoidMovementSnapshot(tag);
        player.fallDistance = 0.0f;
        if (player.isPassenger()) {
            player.noPhysics = true;
            return;
        }
        player.noPhysics = prevNoPhysics;
        player.setNoGravity(prevNoGravity);
        boolean dirty = false;
        if (player.isSpectator()) {
            if (!player.getAbilities().mayBuild) {
                player.getAbilities().mayBuild = true;
                dirty = true;
            }
            if (player.getAbilities().mayfly != restoredFlying) {
                player.getAbilities().mayfly = restoredFlying;
                dirty = true;
            }
        } else {
            if (player.getAbilities().mayBuild != restoredMayfly) {
                player.getAbilities().mayBuild = restoredMayfly;
                dirty = true;
            }
            if (player.getAbilities().mayfly != restoredFlying) {
                player.getAbilities().mayfly = restoredFlying;
                dirty = true;
            }
        }
        if (dirty) {
            player.onUpdateAbilities();
        }
    }

    private static boolean hasVoidMovementSnapshot(ServerPlayer player) {
        return player.getPersistentData().getBoolean(PHASE_APPLIED);
    }

    private static void removeVoidMovementSnapshot(CompoundTag tag) {
        tag.remove(PHASE_APPLIED);
        tag.remove(PREV_MAYFLY);
        tag.remove(PREV_FLYING);
        tag.remove(PREV_NO_GRAVITY);
        tag.remove(PREV_NO_PHYSICS);
    }

    private static void moveToNearestSafeExit(ServerPlayer player) {
        if (player.level().noCollision((Entity)player, player.getBoundingBox())) {
            return;
        }
        BlockPos origin = player.blockPosition();
        for (int radius = 0; radius <= 4; ++radius) {
            for (int dy = -1; dy <= 4; ++dy) {
                for (int dx = -radius; dx <= radius; ++dx) {
                    for (int dz = -radius; dz <= radius; ++dz) {
                        double z;
                        double y;
                        double x;
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != radius || !VoidEscapeHandler.canExitAt(player, x = (double)(origin.getX() + dx) + 0.5, y = (double)(origin.getY() + dy), z = (double)(origin.getZ() + dz) + 0.5)) continue;
                        player.teleportTo(x, y, z);
                        return;
                    }
                }
            }
        }
        int surfaceY = player.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, origin.getX(), origin.getZ());
        double fallbackY = Mth.clamp((double)((double)surfaceY + 0.1), (double)((double)player.level().getMinBuildHeight() + 1.0), (double)((double)player.level().getMaxBuildHeight() - 2.0));
        player.teleportTo(player.getX(), fallbackY, player.getZ());
    }

    private static boolean canExitAt(ServerPlayer player, double x, double y, double z) {
        if (y < (double)player.level().getMinBuildHeight() + 1.0 || y > (double)player.level().getMaxBuildHeight() - 2.0) {
            return false;
        }
        AABB moved = player.getBoundingBox().move(x - player.getX(), y - player.getY(), z - player.getZ());
        return player.level().noCollision((Entity)player, moved);
    }

    public static boolean isInVoidEscape(Player player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        return data != null && data.isVoidEscapeActive();
    }

    /** 退出原因（照搬原模组） */
    public enum ExitReason {
        STABILITY_DEPLETED,
        QI_DEPLETED,
        MANUAL,
        DIMENSION_CHANGE,
        TRIBULATION
    }
}
