/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SpiritLockHandler;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class QingdiLongevityHandler {
    private static final long QI_PER_SECOND = 50L;
    private static final float HEAL_PER_SECOND = 5.0f;
    private static final float QI_PER_HEALTH = 10.0f;
    private static final float HEALTH_FULL_EPSILON = 0.01f;
    private static final long REBIRTH_COOLDOWN_TICKS = 6000L;
    private static final long ABSORPTION_DURATION_TICKS = 600L;
    private static final String REBIRTH_COOLDOWN_KEY = "friday_cultivation.qingdi_rebirth_next_tick";
    private static final String ABSORPTION_AMOUNT_KEY = "friday_cultivation.qingdi_absorption_amount";
    private static final String ABSORPTION_EXPIRES_KEY = "friday_cultivation.qingdi_absorption_expires_tick";

    private QingdiLongevityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        float overflow;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        QingdiLongevityHandler.reconcileTrackedShield(player2, player2.serverLevel().getGameTime());
        if (player2.tickCount % 20 != 0) {
            return;
        }
        if (!QingdiLongevityHandler.isEquipped(player2)) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player2)) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null || data.getCurrentQi() <= 0L) {
            return;
        }
        float missingHealth = Math.max(0.0f, player2.getMaxHealth() - player2.getHealth());
        if (missingHealth <= 0.01f) {
            return;
        }
        float potential = Math.min(5.0f, (float)data.getCurrentQi() / 10.0f);
        if (potential <= 0.0f) {
            return;
        }
        long baseQiCost = (long)Math.ceil(potential * 10.0f);
        long qiCost = Math.min(data.getCurrentQi(), TechniqueBonusHelper.applyQiCostMultiplier((Player)player2, baseQiCost));
        if (qiCost <= 0L) {
            return;
        }
        float actualPotential = (float)baseQiCost / 10.0f;
        float heal = Math.min(missingHealth, actualPotential);
        if (heal > 0.0f) {
            player2.heal(heal);
        }
        if ((overflow = Math.max(0.0f, actualPotential - heal)) > 0.0f) {
            QingdiLongevityHandler.addTrackedShield(player2, overflow);
        }
        data.setCurrentQi(data.getCurrentQi() - qiCost);
        CapabilityEvents.syncToClient(player2);
        QingdiLongevityHandler.spawnSustainingFx(player2, heal, overflow);
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (event.getAmount() < player.getHealth()) {
            return;
        }
        if (!QingdiLongevityHandler.tryRebirth(player)) {
            return;
        }
        event.setCanceled(true);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (!QingdiLongevityHandler.tryRebirthFromDeath(player, event.getSource())) {
            return;
        }
        event.setCanceled(true);
    }

    public static boolean tryRebirthFromDeath(ServerPlayer player, DamageSource source) {
        if (player == null) {
            return false;
        }
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        return QingdiLongevityHandler.tryRebirth(player);
    }

    public static boolean isEquipped(ServerPlayer player) {
        return TechniqueBonusHelper.equippedOf((Player)player) == Technique.QINGDI_LONGEVITY;
    }

    public static float consumeAbsorptionShield(LivingEntity entity, float amount) {
        float currentAbsorption;
        ServerPlayer player;
        block7: {
            block6: {
                if (!(entity instanceof ServerPlayer)) break block6;
                player = (ServerPlayer)entity;
                if (!(amount <= 0.0f)) break block7;
            }
            return 0.0f;
        }
        long now = player.serverLevel().getGameTime();
        float tracked = QingdiLongevityHandler.reconcileTrackedShield(player, now);
        float consumed = Math.min(amount, Math.min(tracked, currentAbsorption = Math.max(0.0f, player.getAbsorptionAmount())));
        if (consumed <= 0.0f) {
            return 0.0f;
        }
        player.setAbsorptionAmount(Math.max(0.0f, currentAbsorption - consumed));
        CompoundTag tag = player.getPersistentData();
        float remainingTracked = Math.max(0.0f, tracked - consumed);
        if (remainingTracked > 0.0f) {
            tag.putFloat(ABSORPTION_AMOUNT_KEY, remainingTracked);
        } else {
            QingdiLongevityHandler.clearTrackedShield(tag);
        }
        return consumed;
    }

    private static boolean tryRebirth(ServerPlayer player) {
        if (!QingdiLongevityHandler.isEquipped(player)) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)player)) {
            return false;
        }
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (tag.getLong(REBIRTH_COOLDOWN_KEY) > now) {
            return false;
        }
        tag.putLong(REBIRTH_COOLDOWN_KEY, now + 6000L);
        BlockPos original = player.blockPosition();
        QingdiLongevityHandler.placeSubstituteLogs(level, original);
        Optional<Vec3> safe = QingdiLongevityHandler.findSafeTeleport(player, original);
        player.setHealth(Math.max(1.0f, player.getMaxHealth() * 0.5f));
        player.setRemainingFireTicks(0);
        player.fallDistance = 0.0f;
        player.invulnerableTime = 20;
        safe.ifPresent(pos -> player.connection.teleport(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot()));
        level.playSound(null, original, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.75f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.GRASS_PLACE, SoundSource.PLAYERS, 1.1f, 0.55f);
        level.sendParticles((ParticleOptions)ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.7, 1.0, 0.7, 0.12);
        level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.55, 0.7, 0.55, 0.08);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.qingdi_longevity.rebirth"), false);
        return true;
    }

    private static void addTrackedShield(ServerPlayer player, float amount) {
        if (amount <= 0.0f) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        float tracked = QingdiLongevityHandler.reconcileTrackedShield(player, now);
        player.setAbsorptionAmount(Math.max(0.0f, player.getAbsorptionAmount()) + amount);
        CompoundTag tag = player.getPersistentData();
        tag.putFloat(ABSORPTION_AMOUNT_KEY, tracked + amount);
        tag.putLong(ABSORPTION_EXPIRES_KEY, now + 600L);
    }

    private static float reconcileTrackedShield(ServerPlayer player, long now) {
        CompoundTag tag = player.getPersistentData();
        float tracked = Math.max(0.0f, tag.getFloat(ABSORPTION_AMOUNT_KEY));
        if (tracked <= 0.0f) {
            QingdiLongevityHandler.clearTrackedShield(tag);
            return 0.0f;
        }
        float currentAbsorption = Math.max(0.0f, player.getAbsorptionAmount());
        if (currentAbsorption <= 0.0f) {
            QingdiLongevityHandler.clearTrackedShield(tag);
            return 0.0f;
        }
        tracked = Math.min(tracked, currentAbsorption);
        long expiresAt = tag.getLong(ABSORPTION_EXPIRES_KEY);
        if (expiresAt > 0L && now >= expiresAt) {
            player.setAbsorptionAmount(Math.max(0.0f, currentAbsorption - tracked));
            QingdiLongevityHandler.clearTrackedShield(tag);
            return 0.0f;
        }
        tag.putFloat(ABSORPTION_AMOUNT_KEY, tracked);
        return tracked;
    }

    private static void clearTrackedShield(CompoundTag tag) {
        tag.remove(ABSORPTION_AMOUNT_KEY);
        tag.remove(ABSORPTION_EXPIRES_KEY);
    }

    private static void spawnSustainingFx(ServerPlayer player, float heal, float overflow) {
        ServerLevel level = player.serverLevel();
        if (heal > 0.0f) {
            level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + (double)player.getBbHeight() * 0.65, player.getZ(), 8, 0.35, 0.35, 0.35, 0.03);
        }
        if (overflow > 0.0f) {
            level.sendParticles((ParticleOptions)ParticleTypes.WAX_ON, player.getX(), player.getY() + (double)player.getBbHeight() * 0.65, player.getZ(), 10, 0.4, 0.45, 0.4, 0.04);
        }
    }

    private static void placeSubstituteLogs(ServerLevel level, BlockPos feet) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        QingdiLongevityHandler.placeIfReplaceable(level, feet, log);
        QingdiLongevityHandler.placeIfReplaceable(level, feet.above(), log);
    }

    private static void placeIfReplaceable(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState old = level.getBlockState(pos);
        if (old.isAir() || old.getCollisionShape(level, pos).isEmpty()) {
            level.setBlock(pos, state, 3);
        }
    }

    private static Optional<Vec3> findSafeTeleport(ServerPlayer player, BlockPos origin) {
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < 48; ++i) {
            int x = origin.getX() + Mth.nextInt((RandomSource)player.getRandom(), (int)-10, (int)10);
            int z = origin.getZ() + Mth.nextInt((RandomSource)player.getRandom(), (int)-10, (int)10);
            int yTop = Math.min(level.getMaxBuildHeight() - 2, origin.getY() + 6);
            int yBottom = Math.max(level.getMinBuildHeight() + 1, origin.getY() - 6);
            for (int y = yTop; y >= yBottom; --y) {
                BlockPos feet = new BlockPos(x, y, z);
                if (!QingdiLongevityHandler.isSafe(level, feet)) continue;
                return Optional.of(Vec3.atBottomCenterOf((Vec3i)feet));
            }
        }
        return QingdiLongevityHandler.isSafe(level, origin) ? Optional.of(Vec3.atBottomCenterOf((Vec3i)origin)) : Optional.empty();
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP) && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty() && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }
}

