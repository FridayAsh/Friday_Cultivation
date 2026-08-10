package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.network.BuddhaFireLotusFlashPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.registry.ModEffects;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import com.friday.cultivation.util.SpellTerrainDestructionHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 佛怒火莲爆炸处理器 — 完全照搬原 mod: xiaoxiang.cultivation.event.BuddhaFireLotusExplosionHandler
 * 核心爆炸 + 范围伤害 + 五行根效果 + 火域持续伤害 + 冲击波扫荡 + 闪电地形破坏 + 表面点火 + 粒子效果
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class BuddhaFireLotusExplosionHandler {
    private static final int ROOT_EFFECT_TICKS = 100;
    private static final int LONG_EFFECT_TICKS = 1200;
    private static final int FIRE_DOMAIN_TICKS = 480;
    private static final double TERRAIN_HORIZONTAL_WIDTH_MULTIPLIER = 1.5;
    private static final int MAX_CORE_TERRAIN_CHANGES = 297000;
    private static final int MAX_SHOCKWAVE_TERRAIN_CHANGES = 126000;
    private static final int MAX_SHOCKWAVE_CHANGES_PER_TICK = 3488;
    private static final int MAX_LIGHTNING_TERRAIN_CHANGES = 9450;
    private static final int SHOCKWAVE_SWEEP_TICKS = 42;
    private static final int MAX_SURFACE_FIRES = 900;
    private static final double TWO_PI = Math.PI * 2;
    private static final double GOLDEN_ANGLE = Math.PI * (3.0 - Math.sqrt(5.0));
    private static final DustParticleOptions CYAN_DUST = new DustParticleOptions(new Vector3f(0.08f, 1.0f, 0.78f), 1.8f);
    private static final DustParticleOptions WHITE_DUST = new DustParticleOptions(new Vector3f(0.95f, 0.98f, 1.0f), 1.55f);
    private static final DustParticleOptions GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.78f, 0.16f), 1.35f);
    private static final Map<LevelKey, FireDomain> FIRE_DOMAINS = new ConcurrentHashMap<LevelKey, FireDomain>();
    private static final Map<LevelKey, ShockwaveSweep> SHOCKWAVE_SWEEPS = new ConcurrentHashMap<LevelKey, ShockwaveSweep>();

    private BuddhaFireLotusExplosionHandler() {
    }

    public static void explode(ServerLevel level, Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius, int chargedQi, int rootFlags) {
        BuddhaFireLotusExplosionHandler.explode(level, source, owner, center, damage, radius, chargedQi, rootFlags, true);
    }

    public static void explode(ServerLevel level, Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius, int chargedQi, int rootFlags, boolean touchBarrierArea) {
        float safeDamage = Math.max(1.0f, damage);
        float safeRadius = Math.max(8.0f, radius);
        float horizontalTerrainRadius = (float) ((double) safeRadius * 1.5);
        BuddhaFireLotusExplosionHandler.prePullForEarth(level, owner, center, rootFlags);
        BuddhaFireLotusExplosionHandler.spawnVisuals(level, center, safeRadius, chargedQi);
        if (touchBarrierArea) {
            SectProtectionDomeHandler.onSpellAreaTouchedBarrier(level, center, safeRadius, owner != null ? owner : source, safeDamage);
        }
        BuddhaFireLotusExplosionHandler.affectEntities(level, source, owner, center, safeDamage, safeRadius, rootFlags);
        BuddhaFireLotusExplosionHandler.applyShockwave(level, source, owner, center, safeDamage, safeRadius);
        if (SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            BuddhaFireLotusExplosionHandler.damageCoreTerrain(level, center, horizontalTerrainRadius, safeRadius, owner);
            BuddhaFireLotusExplosionHandler.scheduleShockwaveTerrain(level, center, horizontalTerrainRadius, safeRadius, owner);
            BuddhaFireLotusExplosionHandler.damageLightningTerrain(level, center, horizontalTerrainRadius, safeRadius, owner);
            BuddhaFireLotusExplosionHandler.igniteSurfaceArea(level, center, safeRadius, owner);
        }
        BuddhaFireLotusExplosionHandler.addFireDomain(level, owner, center, Math.min(42.0f, safeRadius * 0.72f));
        BuddhaFireLotusExplosionHandler.playLotusExplosionSounds(level, center, safeRadius);
        ModNetwork.CHANNEL.send(PacketDistributor.NEAR.with(() -> new PacketDistributor.TargetPoint(center.x, center.y, center.z, Math.max(160.0, (double) safeRadius * 6.0), level.dimension())), new BuddhaFireLotusFlashPacket(center.x, center.y, center.z, safeRadius, 480, chargedQi, rootFlags));
    }

    private static void playLotusExplosionSounds(ServerLevel level, Vec3 center, float radius) {
        float scale = Math.min(1.0f, Math.max(0.0f, radius / 64.0f));
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 12.0f + scale * 10.0f, 0.62f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 120.0f, 0.36f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.HOSTILE, 52.0f, 0.56f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.HOSTILE, 120.0f, 0.48f);
        level.playSound(null, center.x, center.y, center.z, SoundEvents.WITHER_SPAWN, SoundSource.HOSTILE, 4.5f, 0.62f);
    }

    private static void prePullForEarth(ServerLevel level, @Nullable LivingEntity owner, Vec3 center, int rootFlags) {
        if (!BuddhaFireLotusExplosionHandler.has(rootFlags, 16)) {
            return;
        }
        AABB box = new AABB(center, center).inflate(30.0);
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive)) {
            if (living == owner || SectProtectionDomeHandler.isEntityProtectedByOwnDome(living) || !SoulStateHandler.canOrdinaryAffect(owner, living) || !SectCombatHandler.canApplyOffensiveEffect(owner, living)) continue;
            Vec3 toCenter = center.subtract(living.position());
            if (toCenter.lengthSqr() < 1.0E-4) continue;
            Vec3 pull = toCenter.normalize().scale(Math.min(3.2, 0.35 + toCenter.length() * 0.12));
            living.setDeltaMovement(living.getDeltaMovement().add(pull.x, Math.max(0.05, pull.y), pull.z));
            living.hurtMarked = true;
        }
    }

    private static void applyShockwave(ServerLevel level, Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius) {
        double waveRadius = Math.max(28.0, (double) radius * 1.22);
        AABB box = new AABB(center, center).inflate(waveRadius);
        float baseDamage = Math.min(60.0f, Math.max(4.0f, damage * 0.015f));
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive)) {
            if (SectProtectionDomeHandler.isEntityProtectedByOwnDome(target) || target != owner && (!SoulStateHandler.canOrdinaryAffect(owner, target) || !SectCombatHandler.canApplyOffensiveEffect(owner, target))) continue;
            Vec3 targetPos = target.position();
            double dist = targetPos.distanceTo(center);
            if (dist > waveRadius) continue;
            double falloff = 1.0 - dist / Math.max(1.0, waveRadius);
            Vec3 away = targetPos.subtract(center);
            if (away.lengthSqr() < 1.0E-4) {
                away = new Vec3(level.random.nextDouble() - 0.5, 0.0, level.random.nextDouble() - 0.5);
            }
            Vec3 horizontalAway = new Vec3(away.x, 0.0, away.z);
            if (horizontalAway.lengthSqr() < 1.0E-4) {
                horizontalAway = away;
            }
            double force = Math.max(0.45, falloff * 4.1);
            Vec3 push = horizontalAway.normalize().scale(force);
            target.setDeltaMovement(target.getDeltaMovement().add(push.x, force * 1.5, push.z));
            target.hurtMarked = true;
            target.hasImpulse = true;
            if (target == owner) continue;
            float applied = baseDamage * (float) (0.25 + falloff * 0.75);
            target.hurt(SpellDamageSourceHelper.indirectSpell(source, owner), applied);
        }
    }

    private static void affectEntities(ServerLevel level, Entity source, @Nullable LivingEntity owner, Vec3 center, float damage, float radius, int rootFlags) {
        AABB box = new AABB(center, center).inflate((double) radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, living -> living.isAlive() && living != owner);
        float totalLifeReturned = 0.0f;
        for (LivingEntity target : targets) {
            if (SectProtectionDomeHandler.isEntityProtectedByOwnDome(target) || !SoulStateHandler.canOrdinaryAffect(owner, target) || !SectCombatHandler.canApplyOffensiveEffect(owner, target)) continue;
            double dist = target.position().add(0.0, (double) target.getBbHeight() * 0.5, 0.0).distanceTo(center);
            if (dist > (double) radius) continue;
            float falloff = BuddhaFireLotusExplosionHandler.has(rootFlags, 16) ? 1.0f : (float) Math.max(0.35, 1.0 - dist / Math.max(1.0, (double) radius));
            float applied = damage * falloff;
            if (BuddhaFireLotusExplosionHandler.has(rootFlags, 32)) {
                BuddhaFireLotusExplosionHandler.eraseBeneficialState(target);
            }
            target.hurt(SpellDamageSourceHelper.indirectSpell(source, owner), applied);
            if (BuddhaFireLotusExplosionHandler.has(rootFlags, 1)) {
                target.addEffect(new MobEffectInstance(ModEffects.SHATTER_ARMOR.get(), 1200, 0, false, true, true));
            }
            if (BuddhaFireLotusExplosionHandler.has(rootFlags, 2)) {
                target.addEffect(new MobEffectInstance(ModEffects.ROOTED.get(), 100, 0, false, true, true));
                target.addEffect(new MobEffectInstance(MobEffects.POISON, 100, 1));
                totalLifeReturned += applied * 0.1f;
            }
            if (BuddhaFireLotusExplosionHandler.has(rootFlags, 4)) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 254));
                target.addEffect(new MobEffectInstance(ModEffects.MERIDIAN_FROZEN.get(), 100, 0, false, true, true));
            }
            if (BuddhaFireLotusExplosionHandler.has(rootFlags, 8)) {
                target.setSecondsOnFire(12);
            }
            if (!BuddhaFireLotusExplosionHandler.has(rootFlags, 16)) continue;
            target.addEffect(new MobEffectInstance(ModEffects.GRAVITY_SUPPRESSION.get(), 100, 0, false, true, true));
        }
        if (owner != null && totalLifeReturned > 0.0f) {
            owner.heal(totalLifeReturned);
            level.sendParticles(ParticleTypes.HEART, owner.getX(), owner.getY() + (double) owner.getBbHeight() * 0.75, owner.getZ(), Math.min(20, Math.max(4, (int) (totalLifeReturned / 10.0f))), 0.45, 0.45, 0.45, 0.04);
        }
    }

    private static void eraseBeneficialState(LivingEntity target) {
        for (MobEffectInstance instance : List.copyOf(target.getActiveEffects())) {
            MobEffect effect = instance.getEffect();
            if (effect.getCategory() != MobEffectCategory.BENEFICIAL) continue;
            target.removeEffect(effect);
        }
        target.setAbsorptionAmount(0.0f);
    }

    private static void damageCoreTerrain(ServerLevel level, Vec3 center, float horizontalRadius, float verticalBasisRadius, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            return;
        }
        double coreRadius = Math.min(49.5, Math.max(17.0, (double) horizontalRadius * 0.68));
        double verticalCoreRadius = Math.min(33.0, Math.max(17.0, (double) verticalBasisRadius * 0.68));
        double verticalRadius = Math.max(10.0, verticalCoreRadius * 0.82);
        double outerRadius = coreRadius * 1.34;
        double innerRadius = coreRadius * 0.34;
        double phase = BuddhaFireLotusExplosionHandler.lotusCraterPhase(center);
        int changed = 0;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = BuddhaFireLotusExplosionHandler.MthFloor(center.x - outerRadius);
        int maxX = BuddhaFireLotusExplosionHandler.MthFloor(center.x + outerRadius);
        int minY = Math.max(level.getMinBuildHeight(), BuddhaFireLotusExplosionHandler.MthFloor(center.y - verticalRadius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, BuddhaFireLotusExplosionHandler.MthFloor(center.y + verticalRadius));
        int minZ = BuddhaFireLotusExplosionHandler.MthFloor(center.z - outerRadius);
        int maxZ = BuddhaFireLotusExplosionHandler.MthFloor(center.z + outerRadius);
        for (int x = minX; x <= maxX && changed < 297000; ++x) {
            double dx = (double) x + 0.5 - center.x;
            for (int y = minY; y <= maxY && changed < 297000; ++y) {
                double dy = (double) y + 0.5 - center.y;
                for (int z = minZ; z <= maxZ && changed < 297000; ++z) {
                    double dz = (double) z + 0.5 - center.z;
                    double horizontalDistance = Math.sqrt(dx * dx + dz * dz);
                    if (horizontalDistance > outerRadius) continue;
                    double angle = Math.atan2(dz, dx);
                    double distanceT = horizontalDistance / outerRadius;
                    double petalStrength = horizontalDistance <= innerRadius ? 1.0 : BuddhaFireLotusExplosionHandler.lotusStarPetalStrength(angle, distanceT, phase);
                    double scatterStrength = horizontalDistance <= innerRadius ? 0.0 : BuddhaFireLotusExplosionHandler.lotusScatterStrength(angle, distanceT, phase);
                    if (horizontalDistance > innerRadius) {
                        double centerBridge = BuddhaFireLotusExplosionHandler.clamp((0.46 - horizontalDistance / coreRadius) / 0.2, 0.0, 1.0);
                        double petalReach = innerRadius + (outerRadius - innerRadius) * BuddhaFireLotusExplosionHandler.clamp(petalStrength * 1.14 + scatterStrength * 0.42 + centerBridge * 0.36, 0.0, 1.0);
                        if (horizontalDistance > petalReach) continue;
                    }
                    double radialFade = 1.0 - BuddhaFireLotusExplosionHandler.clamp((horizontalDistance - innerRadius) / Math.max(1.0, outerRadius - innerRadius), 0.0, 1.0);
                    double profile = horizontalDistance <= innerRadius ? 1.0 : BuddhaFireLotusExplosionHandler.clamp(petalStrength * 0.84 + scatterStrength * 0.28 + radialFade * 0.28, 0.18, 1.0);
                    double localVerticalRadius = verticalRadius * (0.42 + profile * 0.46 + radialFade * 0.18);
                    double normalized = dy * dy / Math.max(0.1, localVerticalRadius * localVerticalRadius);
                    if (normalized > 1.0) continue;
                    pos.set(x, y, z);
                    if (!BuddhaFireLotusExplosionHandler.canDestroy(level, pos, owner) || !SpellTerrainDestructionHelper.setBlock(level, pos, Blocks.AIR.defaultBlockState(), 2, owner)) continue;
                    ++changed;
                }
            }
        }
    }

    private static double lotusStarPetalStrength(double angle, double distanceT, double phase) {
        double t = BuddhaFireLotusExplosionHandler.clamp(distanceT, 0.0, 1.25);
        double primary = BuddhaFireLotusExplosionHandler.lotusPetalSetStrength(angle, phase, 8, Math.max(0.12, 0.38 - t * 0.18));
        double secondary = BuddhaFireLotusExplosionHandler.lotusPetalSetStrength(angle, phase + 0.39269908169872414, 8, Math.max(0.08, 0.24 - t * 0.1)) * (0.48 + t * 0.18);
        double facet = 0.92 + 0.08 * Math.cos(angle * 16.0 + phase * 0.35);
        return BuddhaFireLotusExplosionHandler.clamp(Math.max(primary, secondary) * facet, 0.0, 1.0);
    }

    private static double lotusPetalSetStrength(double angle, double phase, int petals, double width) {
        double best = 0.0;
        for (int i = 0; i < petals; ++i) {
            double target = phase + (double) i * (Math.PI * 2) / (double) petals;
            double diff = BuddhaFireLotusExplosionHandler.angleDistance(angle, target);
            double strength = BuddhaFireLotusExplosionHandler.clamp(1.0 - diff / width, 0.0, 1.0);
            best = Math.max(best, strength * strength * (3.0 - 2.0 * strength));
        }
        return best;
    }

    private static double lotusScatterStrength(double angle, double distanceT, double phase) {
        double t = BuddhaFireLotusExplosionHandler.clamp(distanceT, 0.0, 1.15);
        double outer = BuddhaFireLotusExplosionHandler.smoothstep(0.4, 1.0, t);
        double shard = Math.sin(angle * 13.0 + t * 27.0 + phase * 1.7) * Math.sin(angle * 7.0 - t * 19.0 + phase * 0.9);
        return shard <= 0.42 ? 0.0 : BuddhaFireLotusExplosionHandler.clamp((shard - 0.42) / 0.58, 0.0, 1.0) * outer;
    }

    private static double smoothstep(double edge0, double edge1, double value) {
        double t = BuddhaFireLotusExplosionHandler.clamp((value - edge0) / Math.max(1.0E-6, edge1 - edge0), 0.0, 1.0);
        return t * t * (3.0 - 2.0 * t);
    }

    private static double lotusCraterPhase(Vec3 center) {
        long hash = (long) BuddhaFireLotusExplosionHandler.MthFloor(center.x) * 341873128712L ^ (long) BuddhaFireLotusExplosionHandler.MthFloor(center.y) * 132897987541L ^ (long) BuddhaFireLotusExplosionHandler.MthFloor(center.z) * 42317861L;
        hash ^= hash >>> 33;
        hash *= -49064778989728563L;
        hash ^= hash >>> 33;
        return (double) Math.floorMod(hash, 1000000L) / 1000000.0 * (Math.PI * 2);
    }

    private static double angleDistance(double a, double b) {
        return Math.abs(Math.atan2(Math.sin(a - b), Math.cos(a - b)));
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static void scheduleShockwaveTerrain(ServerLevel level, Vec3 center, float horizontalRadius, float verticalBasisRadius, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            return;
        }
        double startRadius = Math.max(5.0, (double) horizontalRadius * 0.16);
        double endRadius = (double) horizontalRadius * 1.92;
        int rays = Math.min(390, Math.max(136, (int) (horizontalRadius * 5.0f)));
        double verticalScale = BuddhaFireLotusExplosionHandler.clamp((double) verticalBasisRadius / Math.max(1.0, (double) horizontalRadius), 0.45, 1.0);
        ShockwaveSweep sweep = new ShockwaveSweep(level.dimension().location().toString(), center, startRadius, endRadius, verticalScale, rays, level.getGameTime(), level.random.nextDouble() * Math.PI * 2.0, owner == null ? null : owner.getUUID());
        SHOCKWAVE_SWEEPS.put(new LevelKey(sweep.dimension, UUID.randomUUID()), sweep);
    }

    private static void damageLightningTerrain(ServerLevel level, Vec3 center, float horizontalRadius, float verticalBasisRadius, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            return;
        }
        int changed = 0;
        int arcs = Math.min(16, Math.max(6, (int) (horizontalRadius / 7.5f)));
        double maxRadius = Math.min(78.0, (double) horizontalRadius * 0.74);
        for (int i = 0; i < arcs && changed < 9450; ++i) {
            double baseAngle = Math.PI * 2 * (double) i / (double) arcs + level.random.nextDouble() * 0.42;
            Vec3 previous = center.add(Math.cos(baseAngle) * (double) horizontalRadius * 0.12, level.random.nextDouble() * 2.0, Math.sin(baseAngle) * (double) horizontalRadius * 0.12);
            int segments = 5 + level.random.nextInt(3);
            for (int s = 1; s <= segments && changed < 9450; ++s) {
                double t = (double) s / (double) segments;
                double r = maxRadius * (0.18 + t * (0.56 + level.random.nextDouble() * 0.22));
                double angle = baseAngle + Math.sin((double) i * 1.7 + (double) s * 2.2) * 0.55 + (level.random.nextDouble() - 0.5) * 0.36;
                Vec3 next = center.add(Math.cos(angle) * r, (level.random.nextDouble() - 0.5) * (double) verticalBasisRadius * 0.3, Math.sin(angle) * r);
                changed = BuddhaFireLotusExplosionHandler.carveLine(level, previous, next, 0.72 + t * 0.42, 9450, changed, owner);
                previous = next;
            }
        }
    }

    private static void igniteSurfaceArea(ServerLevel level, Vec3 center, float radius, @Nullable LivingEntity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            return;
        }
        int samples = Math.min(5000, Math.max(800, (int) (radius * radius * 2.2f)));
        int changed = 0;
        BlockPos.MutableBlockPos surface = new BlockPos.MutableBlockPos();
        BlockPos.MutableBlockPos firePos = new BlockPos.MutableBlockPos();
        int top = Math.min(level.getMaxBuildHeight() - 2, BuddhaFireLotusExplosionHandler.MthFloor(center.y + (double) radius * 0.65));
        int bottom = Math.max(level.getMinBuildHeight(), BuddhaFireLotusExplosionHandler.MthFloor(center.y - (double) radius * 0.75));
        for (int i = 0; i < samples && changed < 900; ++i) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(level.random.nextDouble()) * (double) radius * 0.86;
            int x = BuddhaFireLotusExplosionHandler.MthFloor(center.x + Math.cos(angle) * r);
            int z = BuddhaFireLotusExplosionHandler.MthFloor(center.z + Math.sin(angle) * r);
            for (int y = top; y >= bottom; --y) {
                surface.set(x, y, z);
                if (!level.isLoaded(surface)) break;
                BlockState state = level.getBlockState(surface);
                if (state.isAir()) continue;
                firePos.set(x, y + 1, z);
                if (SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(level, firePos) || SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(level, surface) || !BaseFireBlock.canBePlacedAt(level, firePos, Direction.UP) || !SpellTerrainDestructionHelper.setBlock(level, firePos, BaseFireBlock.getState(level, firePos), 2, owner)) break;
                ++changed;
                break;
            }
        }
    }

    private static boolean canDestroy(ServerLevel level, BlockPos pos, @Nullable Entity owner) {
        if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
            return false;
        }
        if (!level.isLoaded(pos)) {
            return false;
        }
        if (SectProtectionDomeHandler.isProtectedByAnySectProtectionDome(level, pos)) {
            return false;
        }
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        if (state.is(BlockTags.WITHER_IMMUNE)) {
            return false;
        }
        return !state.is(Blocks.BEDROCK) && !state.is(Blocks.END_PORTAL_FRAME) && !state.is(Blocks.REINFORCED_DEEPSLATE) && !state.is(Blocks.COMMAND_BLOCK) && !state.is(Blocks.CHAIN_COMMAND_BLOCK) && !state.is(Blocks.REPEATING_COMMAND_BLOCK);
    }

    private static int carveLine(ServerLevel level, Vec3 from, Vec3 to, double radius, int cap, int changed, @Nullable Entity owner) {
        Vec3 delta = to.subtract(from);
        double length = delta.length();
        if (length < 1.0E-4) {
            return changed;
        }
        int steps = Math.max(1, (int) Math.ceil(length / 1.35));
        for (int i = 0; i <= steps && changed < cap; ++i) {
            double t = (double) i / (double) steps;
            Vec3 pos = from.add(delta.scale(t));
            changed = BuddhaFireLotusExplosionHandler.carveSphere(level, pos.x, pos.y, pos.z, radius, radius * 0.82, cap, changed, owner);
        }
        return changed;
    }

    private static int carveSphere(ServerLevel level, double cx, double cy, double cz, double radius, double verticalRadius, int cap, int changed, @Nullable Entity owner) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int minX = BuddhaFireLotusExplosionHandler.MthFloor(cx - radius);
        int maxX = BuddhaFireLotusExplosionHandler.MthFloor(cx + radius);
        int minY = Math.max(level.getMinBuildHeight(), BuddhaFireLotusExplosionHandler.MthFloor(cy - verticalRadius));
        int maxY = Math.min(level.getMaxBuildHeight() - 1, BuddhaFireLotusExplosionHandler.MthFloor(cy + verticalRadius));
        int minZ = BuddhaFireLotusExplosionHandler.MthFloor(cz - radius);
        int maxZ = BuddhaFireLotusExplosionHandler.MthFloor(cz + radius);
        double r2 = Math.max(0.1, radius * radius);
        double y2 = Math.max(0.1, verticalRadius * verticalRadius);
        for (int x = minX; x <= maxX && changed < cap; ++x) {
            double dx = (double) x + 0.5 - cx;
            for (int y = minY; y <= maxY && changed < cap; ++y) {
                double dy = (double) y + 0.5 - cy;
                for (int z = minZ; z <= maxZ && changed < cap; ++z) {
                    double dz = (double) z + 0.5 - cz;
                    double normalized = dx * dx / r2 + dy * dy / y2 + dz * dz / r2;
                    if (normalized > 1.0 || level.random.nextDouble() > 0.88 - Math.sqrt(normalized) * 0.18) continue;
                    pos.set(x, y, z);
                    if (!BuddhaFireLotusExplosionHandler.canDestroy(level, pos, owner) || !SpellTerrainDestructionHelper.setBlock(level, pos, Blocks.AIR.defaultBlockState(), 2, owner)) continue;
                    ++changed;
                }
            }
        }
        return changed;
    }

    private static void spawnVisuals(ServerLevel level, Vec3 center, float radius, int chargedQi) {
        BuddhaFireLotusExplosionHandler.spawnCyanShockwave(level, center, radius);
        BuddhaFireLotusExplosionHandler.spawnFireSplashes(level, center, radius, chargedQi);
        BuddhaFireLotusExplosionHandler.spawnFireRain(level, center, radius, chargedQi);
        int coreCount = Math.min(600, 120 + chargedQi / 120);
        for (int i = 0; i < coreCount; ++i) {
            double angle = level.random.nextDouble() * Math.PI * 2.0;
            double r = level.random.nextDouble() * Math.min(12.0, (double) radius * 0.35);
            double y = level.random.nextDouble() * Math.min(18.0, (double) radius * 0.45);
            double x = center.x + Math.cos(angle) * r;
            double z = center.z + Math.sin(angle) * r;
            level.sendParticles((i & 1) == 0 ? CYAN_DUST : WHITE_DUST, x, center.y + y, z, 1, 0.05, 0.05, 0.05, 0.0);
        }
        level.sendParticles(ParticleTypes.FLAME, center.x, center.y + 0.2, center.z, 80, (double) radius * 0.22, 0.55, (double) radius * 0.22, 0.05);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, center.x, center.y + 0.4, center.z, 36, (double) radius * 0.18, 0.45, (double) radius * 0.18, 0.025);
    }

    private static void spawnCyanShockwave(ServerLevel level, Vec3 center, float radius) {
        int count = Math.min(220, Math.max(72, (int) (radius * 4.0f)));
        double speed = Math.min(2.8, 0.75 + (double) radius * 0.035);
        for (int i = 0; i < count; ++i) {
            double angle = Math.PI * 2 * (double) i / (double) count;
            double wave = Math.sin(angle * 4.0) * 0.08;
            double vx = Math.cos(angle) * speed;
            double vz = Math.sin(angle) * speed;
            level.sendParticles(CYAN_DUST, center.x + Math.cos(angle) * 1.4, center.y + 0.28 + wave, center.z + Math.sin(angle) * 1.4, 0, vx, wave * 0.35, vz, 1.0);
        }
    }

    private static void spawnFireSplashes(ServerLevel level, Vec3 center, float radius, int chargedQi) {
        int count = Math.min(320, 96 + chargedQi / 160);
        double speed = Math.min(2.4, 0.55 + (double) radius * 0.028);
        for (int i = 0; i < count; ++i) {
            double theta = level.random.nextDouble() * Math.PI * 2.0;
            double lift = 0.1 + level.random.nextDouble() * 0.58;
            double horizontal = speed * (0.35 + level.random.nextDouble() * 0.85);
            double vx = Math.cos(theta) * horizontal;
            double vz = Math.sin(theta) * horizontal;
            double vy = lift * speed;
            double spawnR = 0.5 + level.random.nextDouble() * Math.min(3.0, (double) radius * 0.08);
            boolean cyan = (i & 1) == 0;
            level.sendParticles(cyan ? CYAN_DUST : WHITE_DUST, center.x + Math.cos(theta) * spawnR, center.y + 0.45 + level.random.nextDouble() * 0.75, center.z + Math.sin(theta) * spawnR, 0, vx, vy, vz, 1.0);
            level.sendParticles(cyan ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, center.x + Math.cos(theta) * spawnR, center.y + 0.35 + level.random.nextDouble() * 0.55, center.z + Math.sin(theta) * spawnR, 0, vx * 0.42, vy * 0.35, vz * 0.42, 1.0);
        }
    }

    private static void spawnFireRain(ServerLevel level, Vec3 center, float radius, int chargedQi) {
        int count = Math.min(520, 140 + chargedQi / 100);
        double rainRadius = Math.min(70.0, (double) radius * 1.05);
        double minHeight = Math.max(12.0, (double) radius * 0.38);
        double heightSpread = Math.min(34.0, Math.max(12.0, (double) radius * 0.62));
        for (int i = 0; i < count; ++i) {
            double theta = level.random.nextDouble() * Math.PI * 2.0;
            double r = Math.sqrt(level.random.nextDouble()) * rainRadius;
            double x = center.x + Math.cos(theta) * r;
            double z = center.z + Math.sin(theta) * r;
            double y = center.y + minHeight + level.random.nextDouble() * heightSpread;
            double inwardX = (center.x - x) * 0.004;
            double inwardZ = (center.z - z) * 0.004;
            double fall = -(0.85 + level.random.nextDouble() * 1.35);
            boolean cyan = (i & 1) == 0;
            level.sendParticles(cyan ? CYAN_DUST : WHITE_DUST, x, y, z, 0, inwardX, fall, inwardZ, 1.0);
            level.sendParticles(cyan ? ParticleTypes.SOUL_FIRE_FLAME : ParticleTypes.SMALL_FLAME, x, y - 0.15, z, 0, inwardX * 0.48, fall * 0.62, inwardZ * 0.48, 1.0);
        }
    }

    private static void addFireDomain(ServerLevel level, @Nullable LivingEntity owner, Vec3 center, float radius) {
        UUID ownerId = owner == null ? null : owner.getUUID();
        float burn = 1.0f;
        if (owner != null) {
            burn = Math.max(owner.getMaxHealth() * 0.01f, 1.0f);
            if (owner instanceof WanderingCultivatorEntity) {
                WanderingCultivatorEntity npc = (WanderingCultivatorEntity) owner;
                burn = Math.max(burn, (float) ((double) npc.getMaxQi() * 0.01));
            } else if (owner instanceof Player) {
                Player player = (Player) owner;
                burn = Math.max(burn, CultivationCapability.get(player).map(data -> (float) ((double) data.getMaxQi() * 0.01)).orElse(0.0f));
            }
        }
        FIRE_DOMAINS.put(new LevelKey(level.dimension().location().toString(), UUID.randomUUID()), new FireDomain(level.dimension().location().toString(), center, radius, level.getGameTime() + 480L, ownerId, burn));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Level level = event.level;
        if (!(level instanceof ServerLevel level2)) {
            return;
        }
        if (FIRE_DOMAINS.isEmpty() && SHOCKWAVE_SWEEPS.isEmpty()) {
            return;
        }
        long now = level2.getGameTime();
        BuddhaFireLotusExplosionHandler.tickShockwaveSweeps(level2, now);
        if (FIRE_DOMAINS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<LevelKey, FireDomain>> it = FIRE_DOMAINS.entrySet().iterator();
        while (it.hasNext()) {
            FireDomain domain = it.next().getValue();
            if (!domain.dimension().equals(level2.dimension().location().toString()) || now >= domain.endTick()) {
                if (now < domain.endTick()) continue;
                it.remove();
                continue;
            }
            BuddhaFireLotusExplosionHandler.tickFireDomain(level2, domain, now);
        }
    }

    private static void tickShockwaveSweeps(ServerLevel level, long now) {
        if (SHOCKWAVE_SWEEPS.isEmpty()) {
            return;
        }
        Iterator<Map.Entry<LevelKey, ShockwaveSweep>> it = SHOCKWAVE_SWEEPS.entrySet().iterator();
        while (it.hasNext()) {
            ShockwaveSweep sweep = it.next().getValue();
            if (!sweep.dimension.equals(level.dimension().location().toString())) continue;
            Entity owner = sweep.owner(level);
            if (!SpellTerrainDestructionHelper.canModifyBlocks(level, owner)) {
                it.remove();
                continue;
            }
            int elapsed = (int) (now - sweep.startTick);
            if (elapsed > 42 || sweep.changed >= 126000) {
                it.remove();
                continue;
            }
            if (elapsed < 0) continue;
            BuddhaFireLotusExplosionHandler.tickShockwaveSweep(level, sweep, elapsed);
        }
    }

    private static void tickShockwaveSweep(ServerLevel level, ShockwaveSweep sweep, int elapsed) {
        double progress = Math.min(1.0, Math.max(0.0, (double) elapsed / 42.0));
        double previousProgress = Math.min(1.0, Math.max(0.0, (double) (elapsed - 1) / 42.0));
        double previousEased = BuddhaFireLotusExplosionHandler.shockwaveEase(previousProgress);
        double eased = BuddhaFireLotusExplosionHandler.shockwaveEase(progress);
        double previousRadius = sweep.startRadius + (sweep.endRadius - sweep.startRadius) * previousEased;
        double currentRadius = sweep.startRadius + (sweep.endRadius - sweep.startRadius) * eased;
        double stepSpan = Math.max(1.6, (sweep.endRadius - sweep.startRadius) / 42.0 * 1.18);
        double segmentStart = Math.max(sweep.startRadius, previousRadius - stepSpan * 0.3);
        double segmentEnd = Math.min(sweep.endRadius, currentRadius + stepSpan * 0.55);
        if (segmentEnd <= segmentStart) {
            return;
        }
        int tickStartChanged = sweep.changed;
        for (int i = 0; i < sweep.rays && sweep.changed - tickStartChanged < 3488 && sweep.changed < 126000; ++i) {
            Vec3 direction = BuddhaFireLotusExplosionHandler.shockwaveDirection(i, sweep.rays, sweep.phase);
            BuddhaFireLotusExplosionHandler.carveSeaUrchinCraterSegment(level, sweep, direction, segmentStart, segmentEnd, tickStartChanged, i);
        }
    }

    private static double shockwaveEase(double progress) {
        return 1.0 - Math.pow(1.0 - progress, 1.36);
    }

    private static Vec3 shockwaveDirection(int index, int count, double phase) {
        double y = 1.0 - 2.0 * ((double) index + 0.5) / (double) Math.max(1, count);
        double horizontal = Math.sqrt(Math.max(0.0, 1.0 - y * y));
        double angle = (double) index * GOLDEN_ANGLE + phase;
        return new Vec3(Math.cos(angle) * horizontal, y, Math.sin(angle) * horizontal).normalize();
    }

    private static void carveSeaUrchinCraterSegment(ServerLevel level, ShockwaveSweep sweep, Vec3 direction, double fromRadius, double toRadius, int tickStartChanged, int directionIndex) {
        double length = toRadius - fromRadius;
        if (length <= 0.0 || direction.lengthSqr() < 1.0E-5) {
            return;
        }
        double startT = (fromRadius - sweep.startRadius) / Math.max(1.0, sweep.endRadius - sweep.startRadius);
        double baseSpacing = 0.72 + Math.max(0.0, startT) * 3.25;
        double r = fromRadius + level.random.nextDouble() * baseSpacing * 0.65;
        int step = 0;
        while (r <= toRadius && sweep.changed - tickStartChanged < 3488 && sweep.changed < 126000) {
            double radialT = (r - sweep.startRadius) / Math.max(1.0, sweep.endRadius - sweep.startRadius);
            double severity = 1.0 - radialT;
            double density = 0.98 - radialT * 0.58;
            if (level.random.nextDouble() <= density) {
                double width = 0.42 + severity * 2.15;
                double verticalWidth = width * (0.58 + severity * 0.22);
                Vec3 pos = BuddhaFireLotusExplosionHandler.seaUrchinPocketCenter(level, sweep, direction, directionIndex, step, r, severity);
                int perTickCap = Math.min(126000, tickStartChanged + 3488);
                sweep.changed = BuddhaFireLotusExplosionHandler.carveSphere(level, pos.x, pos.y, pos.z, width, verticalWidth, perTickCap, sweep.changed, sweep.owner(level));
            }
            double spacing = 0.82 + radialT * 4.15;
            r += spacing * (0.76 + level.random.nextDouble() * 0.78);
            ++step;
        }
    }

    private static Vec3 seaUrchinPocketCenter(ServerLevel level, ShockwaveSweep sweep, Vec3 direction, int directionIndex, int step, double radius, double severity) {
        Vec3 up = Math.abs(direction.y) > 0.92 ? new Vec3(1.0, 0.0, 0.0) : new Vec3(0.0, 1.0, 0.0);
        Vec3 tangent = direction.cross(up).normalize();
        Vec3 bitangent = direction.cross(tangent).normalize();
        double angle = (double) directionIndex * 1.731 + (double) step * 2.113 + sweep.phase;
        double spread = (0.22 + (1.0 - severity) * 1.15) * (0.35 + level.random.nextDouble() * 0.85);
        Vec3 lateral = tangent.scale(Math.cos(angle) * spread).add(bitangent.scale(Math.sin(angle) * spread));
        double radialJitter = (level.random.nextDouble() - 0.5) * (0.34 + (1.0 - severity) * 0.72);
        Vec3 local = direction.scale(radius + radialJitter).add(lateral);
        return sweep.center.add(local.x, local.y * sweep.verticalScale, local.z);
    }

    private static void tickFireDomain(ServerLevel level, FireDomain domain, long now) {
        if (now % 5L == 0L) {
            level.sendParticles(ParticleTypes.FLAME, domain.center().x, domain.center().y + 0.25, domain.center().z, 24, (double) domain.radius() * 0.45, 0.35, (double) domain.radius() * 0.45, 0.025);
        }
        if (now % 20L != 0L) {
            return;
        }
        LivingEntity owner = domain.ownerId() == null ? null : (level.getEntity(domain.ownerId()) instanceof LivingEntity le ? le : null);
        AABB box = new AABB(domain.center(), domain.center()).inflate((double) domain.radius());
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, box, Entity::isAlive)) {
            if (target == owner || SectProtectionDomeHandler.isEntityProtectedByOwnDome(target) || !SoulStateHandler.canOrdinaryAffect(owner, target) || !SectCombatHandler.canApplyOffensiveEffect(owner, target) || target.position().distanceToSqr(domain.center()) > (double) (domain.radius() * domain.radius())) continue;
            target.setSecondsOnFire(4);
            target.hurt(owner != null ? SpellDamageSourceHelper.directSpell(owner) : level.damageSources().magic(), domain.burnDamage());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (entity.level().isClientSide) {
            return;
        }
        if (entity.hasEffect(ModEffects.ROOTED.get())) {
            Vec3 v = entity.getDeltaMovement();
            entity.setDeltaMovement(0.0, Math.min(v.y, 0.0), 0.0);
            if (entity instanceof Mob) {
                Mob mob = (Mob) entity;
                mob.getNavigation().stop();
            }
            entity.hurtMarked = true;
        }
        if (entity.hasEffect(ModEffects.GRAVITY_SUPPRESSION.get())) {
            Vec3 v = entity.getDeltaMovement();
            entity.setDeltaMovement(v.x * 0.35, Math.min(v.y - 0.18, -0.16), v.z * 0.35);
            entity.fallDistance = 0.0f;
            if (entity instanceof Player) {
                Player player = (Player) entity;
                player.getAbilities().flying = false;
            }
            entity.hurtMarked = true;
        }
    }

    private static boolean has(int flags, int flag) {
        return (flags & flag) != 0;
    }

    private static int MthFloor(double value) {
        return (int) Math.floor(value);
    }

    private static final class ShockwaveSweep {
        private final String dimension;
        private final Vec3 center;
        private final double startRadius;
        private final double endRadius;
        private final double verticalScale;
        private final int rays;
        private final long startTick;
        private final double phase;
        @Nullable
        private final UUID ownerUuid;
        private int changed;

        private ShockwaveSweep(String dimension, Vec3 center, double startRadius, double endRadius, double verticalScale, int rays, long startTick, double phase, @Nullable UUID ownerUuid) {
            this.dimension = dimension;
            this.center = center;
            this.startRadius = startRadius;
            this.endRadius = endRadius;
            this.verticalScale = verticalScale;
            this.rays = rays;
            this.startTick = startTick;
            this.phase = phase;
            this.ownerUuid = ownerUuid;
        }

        @Nullable
        private Entity owner(ServerLevel level) {
            return this.ownerUuid == null ? null : level.getEntity(this.ownerUuid);
        }
    }

    private record LevelKey(String dimension, UUID id) {
    }

    private record FireDomain(String dimension, Vec3 center, float radius, long endTick, @Nullable UUID ownerId, float burnDamage) {
    }
}
