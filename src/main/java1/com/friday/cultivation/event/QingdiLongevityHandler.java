package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.technique.TechniqueBonusHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

/**
 * 青帝长生功处理器 - 装备此功法时持续回血+吸收盾+濒死替身。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.QingdiLongevityHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class QingdiLongevityHandler {
    private static final float HEAL_PER_SECOND = 5.0f;
    private static final float QI_PER_HEALTH = 10.0f;
    private static final long REBIRTH_COOLDOWN_TICKS = 6000L;
    private static final long ABSORPTION_DURATION_TICKS = 600L;
    private static final String REBIRTH_COOLDOWN_KEY = "friday_cultivation.qingdi_rebirth_next_tick";
    private static final String ABSORPTION_AMOUNT_KEY = "friday_cultivation.qingdi_absorption_amount";
    private static final String ABSORPTION_EXPIRES_KEY = "friday_cultivation.qingdi_absorption_expires_tick";

    private QingdiLongevityHandler() {
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        reconcileTrackedShield(serverPlayer, serverPlayer.level().getGameTime());
        if (serverPlayer.tickCount % 20 != 0) return;
        if (!isEquipped(serverPlayer)) return;
        if (SpiritLockHandler.isEntityLocked((LivingEntity) serverPlayer)) return;
        CultivationData data = CultivationCapability.get((Player) serverPlayer).orElse(null);
        if (data == null || data.getCurrentQi() <= 0L) return;
        float missingHealth = Math.max(0.0f, serverPlayer.getMaxHealth() - serverPlayer.getHealth());
        if (missingHealth <= 0.01f) return;
        float potential = Math.min(HEAL_PER_SECOND, (float) data.getCurrentQi() / QI_PER_HEALTH);
        if (potential <= 0.0f) return;
        long baseQiCost = (long) Math.ceil(potential * QI_PER_HEALTH);
        long qiCost = Math.min(data.getCurrentQi(), TechniqueBonusHelper.applyQiCostMultiplier((Player) serverPlayer, baseQiCost));
        if (qiCost <= 0L) return;
        float actualPotential = (float) baseQiCost / QI_PER_HEALTH;
        float heal = Math.min(missingHealth, actualPotential);
        if (heal > 0.0f) serverPlayer.heal(heal);
        float overflow = Math.max(0.0f, actualPotential - heal);
        if (overflow > 0.0f) addTrackedShield(serverPlayer, overflow);
        data.setCurrentQi(data.getCurrentQi() - qiCost);
        com.friday.cultivation.event.CapabilityEvents.syncToClient(serverPlayer);
        spawnSustainingFx(serverPlayer, heal, overflow);
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) return;
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (event.getAmount() < player.getHealth()) return;
        if (!tryRebirth(player)) return;
        event.setCanceled(true);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) return;
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (!tryRebirthFromDeath(player, event.getSource())) return;
        event.setCanceled(true);
    }

    public static boolean tryRebirthFromDeath(ServerPlayer player, DamageSource source) {
        if (player == null) return false;
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) return false;
        return tryRebirth(player);
    }

    public static boolean isEquipped(ServerPlayer player) {
        return Technique.QINGDI_LONGEVITY.id().equals(TechniqueBonusHelper.equippedOf((Player) player));
    }

    public static float consumeAbsorptionShield(LivingEntity entity, float amount) {
        if (!(entity instanceof ServerPlayer player) || amount <= 0.0f) return 0.0f;
        long now = player.level().getGameTime();
        float tracked = reconcileTrackedShield(player, now);
        float currentAbsorption = Math.max(0.0f, player.getAbsorptionAmount());
        float consumed = Math.min(amount, Math.min(tracked, currentAbsorption));
        if (consumed <= 0.0f) return 0.0f;
        player.setAbsorptionAmount(Math.max(0.0f, currentAbsorption - consumed));
        CompoundTag tag = player.getPersistentData();
        float remainingTracked = Math.max(0.0f, tracked - consumed);
        if (remainingTracked > 0.0f) {
            tag.putFloat(ABSORPTION_AMOUNT_KEY, remainingTracked);
        } else {
            clearTrackedShield(tag);
        }
        return consumed;
    }

    private static boolean tryRebirth(ServerPlayer player) {
        if (!isEquipped(player)) return false;
        if (SpiritLockHandler.isEntityLocked((LivingEntity) player)) return false;
        if (!(player.level() instanceof ServerLevel level)) return false;
        long now = level.getGameTime();
        CompoundTag tag = player.getPersistentData();
        if (tag.getLong(REBIRTH_COOLDOWN_KEY) > now) return false;
        tag.putLong(REBIRTH_COOLDOWN_KEY, now + REBIRTH_COOLDOWN_TICKS);
        BlockPos original = player.blockPosition();
        placeSubstituteLogs(level, original);
        Optional<Vec3> safe = findSafeTeleport(player, original);
        player.setHealth(Math.max(1.0f, player.getMaxHealth() * 0.5f));
        player.setRemainingFireTicks(0);
        player.invulnerableTime = 20;
        safe.ifPresent(pos -> player.connection.teleport(pos.x, pos.y, pos.z, player.getYRot(), player.getXRot()));
        level.playSound(null, original, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.75f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.1f, 0.55f);
        level.sendParticles((ParticleOptions) ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.7, 1.0, 0.7, 0.12);
        level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 50, 0.55, 0.7, 0.55, 0.08);
        player.displayClientMessage((Component) Component.literal("message.friday_cultivation.qingdi_longevity.rebirth"), false);
        return true;
    }

    private static void addTrackedShield(ServerPlayer player, float amount) {
        if (amount <= 0.0f) return;
        long now = player.level().getGameTime();
        float tracked = reconcileTrackedShield(player, now);
        player.setAbsorptionAmount(Math.max(0.0f, player.getAbsorptionAmount()) + amount);
        CompoundTag tag = player.getPersistentData();
        tag.putFloat(ABSORPTION_AMOUNT_KEY, tracked + amount);
        tag.putLong(ABSORPTION_EXPIRES_KEY, now + ABSORPTION_DURATION_TICKS);
    }

    private static float reconcileTrackedShield(ServerPlayer player, long now) {
        CompoundTag tag = player.getPersistentData();
        float tracked = Math.max(0.0f, tag.getFloat(ABSORPTION_AMOUNT_KEY));
        if (tracked <= 0.0f) {
            clearTrackedShield(tag);
            return 0.0f;
        }
        float currentAbsorption = Math.max(0.0f, player.getAbsorptionAmount());
        if (currentAbsorption <= 0.0f) {
            clearTrackedShield(tag);
            return 0.0f;
        }
        tracked = Math.min(tracked, currentAbsorption);
        long expiresAt = tag.getLong(ABSORPTION_EXPIRES_KEY);
        if (expiresAt > 0L && now >= expiresAt) {
            player.setAbsorptionAmount(Math.max(0.0f, currentAbsorption - tracked));
            clearTrackedShield(tag);
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
        if (!(player.level() instanceof ServerLevel level)) return;
        if (heal > 0.0f) {
            level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + (double) player.getBbHeight() * 0.65, player.getZ(), 8, 0.35, 0.35, 0.35, 0.03);
        }
        if (overflow > 0.0f) {
            level.sendParticles((ParticleOptions) ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + (double) player.getBbHeight() * 0.65, player.getZ(), 10, 0.4, 0.45, 0.4, 0.04);
        }
    }

    private static void placeSubstituteLogs(ServerLevel level, BlockPos feet) {
        BlockState log = Blocks.OAK_LOG.defaultBlockState();
        placeIfReplaceable(level, feet, log);
        placeIfReplaceable(level, feet.above(), log);
    }

    private static void placeIfReplaceable(ServerLevel level, BlockPos pos, BlockState state) {
        BlockState old = level.getBlockState(pos);
        if (old.isAir() || old.getCollisionShape(level, pos).isEmpty()) {
            level.setBlock(pos, state, 3);
        }
    }

    private static Optional<Vec3> findSafeTeleport(ServerPlayer player, BlockPos origin) {
        if (!(player.level() instanceof ServerLevel level)) return Optional.empty();
        RandomSource random = player.getRandom();
        for (int i = 0; i < 48; ++i) {
            int x = origin.getX() + Mth.nextInt(random, -10, 10);
            int z = origin.getZ() + Mth.nextInt(random, -10, 10);
            int yTop = Math.min(level.getMaxBuildHeight() - 2, origin.getY() + 6);
            int yBottom = Math.max(level.getMinBuildHeight() + 1, origin.getY() - 6);
            for (int y = yTop; y >= yBottom; --y) {
                BlockPos feet = new BlockPos(x, y, z);
                if (!isSafe(level, feet)) continue;
                return Optional.of(Vec3.atBottomCenterOf(feet));
            }
        }
        return isSafe(level, origin) ? Optional.of(Vec3.atBottomCenterOf(origin)) : Optional.empty();
    }

    private static boolean isSafe(ServerLevel level, BlockPos feet) {
        BlockPos floor = feet.below();
        return level.getBlockState(floor).isFaceSturdy(level, floor, Direction.UP)
                && level.getBlockState(feet).getCollisionShape(level, feet).isEmpty()
                && level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty();
    }
}
