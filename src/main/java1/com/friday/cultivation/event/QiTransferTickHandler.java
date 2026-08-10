package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.QiElement;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class QiTransferTickHandler {
    private static final double RANGE = 16.0;
    private static final long BASE_DRAIN = 5L;
    private static final int SOUND_INTERVAL = 8;

    private QiTransferTickHandler() {
    }

    private static long computeDrain(int chargingTicks) {
        long t = Math.min(20L, (long) chargingTicks / 20L);
        return 5L * (1L << (int) t);
    }

    public static void tick(ServerPlayer player, CultivationData data) {
        if (SpiritLockHandler.isEntityLocked(player)) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        long drain = QiTransferTickHandler.computeDrain(data.getChargingTicks());
        long curQi = data.getCurrentQi();
        if (curQi <= 0L) {
            data.clearCharging();
            CapabilityEvents.syncToClient(player);
            return;
        }
        long actualDrain = Math.min(drain, curQi);
        Vec3 from = player.getEyePosition();
        Vec3 view = player.getViewVector(1.0f);
        Vec3 to = from.add(view.scale(16.0));
        BlockHitResult blockHit = player.level().clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 blockHitPos = blockHit.getType() == HitResult.Type.MISS ? to : blockHit.getLocation();
        AABB sweepBox = player.getBoundingBox().expandTowards(view.scale(16.0)).inflate(1.0);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(player.level(), player, from, blockHitPos, sweepBox, e -> e != player && e.isAlive() && SoulStateHandler.canOrdinaryAffect(player, e) && QiTransferTickHandler.isQiAbsorbingEntity(e), 0.5f);
        long transferred = 0L;
        Vec3 hitPos = blockHitPos;
        if (entityHit != null) {
            Entity target = entityHit.getEntity();
            transferred = QiTransferTickHandler.depositQiToEntity(target, actualDrain);
            hitPos = entityHit.getLocation();
        } else if (blockHit.getType() == HitResult.Type.BLOCK) {
            BlockEntity be;
            BlockPos bp = blockHit.getBlockPos();
            BlockState bs = player.level().getBlockState(bp);
            if (bs.is(ModBlocks.ALCHEMY_CORE.get())) {
                BlockEntity be2 = player.level().getBlockEntity(bp);
                if (be2 instanceof AlchemyCoreBlockEntity) {
                    AlchemyCoreBlockEntity ace = (AlchemyCoreBlockEntity) be2;
                    transferred = ace.addQi(actualDrain);
                    hitPos = Vec3.atBottomCenterOf(bp);
                }
            } else if (bs.is(ModBlocks.REFINING_CORE.get())) {
                BlockEntity be3 = player.level().getBlockEntity(bp);
                if (be3 instanceof RefiningCoreBlockEntity) {
                    RefiningCoreBlockEntity rce = (RefiningCoreBlockEntity) be3;
                    transferred = rce.addQi(actualDrain);
                    hitPos = Vec3.atBottomCenterOf(bp);
                }
            } else if (bs.getBlock() instanceof FormationCorePlateBlock && (be = player.level().getBlockEntity(bp)) instanceof FormationCorePlateBlockEntity) {
                FormationCorePlateBlockEntity fce = (FormationCorePlateBlockEntity) be;
                transferred = fce.addQi(actualDrain);
                hitPos = Vec3.atBottomCenterOf(bp);
            }
        }
        data.setCurrentQi(curQi - actualDrain);
        data.incrementChargingTicks();
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel) level;
            QiTransferTickHandler.renderQiBeam(sl, from, hitPos, transferred > 0L);
        }
        if (player.tickCount % 8 == 0) {
            player.serverLevel().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.3f, 1.5f + (float) data.getChargingTicks() / 100.0f);
        }
    }

    private static boolean isQiAbsorbingEntity(Entity e) {
        if (SpiritLockHandler.isEntityLocked(e)) {
            return false;
        }
        if (e instanceof WanderingCultivatorEntity) {
            return true;
        }
        if (e instanceof Player) {
            Player p = (Player) e;
            return CultivationCapability.get(p).map(d -> d.getCurrentQi() < d.getMaxQi()).orElse(false);
        }
        return false;
    }

    private static long depositQiToEntity(Entity target, long amount) {
        if (SpiritLockHandler.isEntityLocked(target)) {
            return 0L;
        }
        if (target instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity) target;
            return npc.addQi(amount);
        }
        if (target instanceof Player) {
            Player p = (Player) target;
            int gained = CultivationCapability.get(p).map(d -> d.absorbQi((int) Math.min(Integer.MAX_VALUE, amount), QiElement.PURE)).orElse(0);
            if (p instanceof ServerPlayer) {
                ServerPlayer sp2 = (ServerPlayer) p;
                CapabilityEvents.syncToClient(sp2);
            }
            return gained;
        }
        return 0L;
    }

    private static void renderQiBeam(ServerLevel sl, Vec3 from, Vec3 to, boolean hitTarget) {
        Vec3 dir = to.subtract(from);
        double dist = dir.length();
        if (dist < 0.01) {
            return;
        }
        SimpleParticleType qiParticle = ModParticles.AMBIENT_QI.get();
        Vec3 step = dir.normalize().scale(0.5);
        int n = (int) (dist / 0.5);
        for (int i = 1; i <= n; ++i) {
            Vec3 p = from.add(step.scale((double) i));
            sl.sendParticles(qiParticle, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        if (hitTarget) {
            sl.sendParticles(qiParticle, to.x, to.y, to.z, 8, 0.3, 0.3, 0.3, 0.02);
            sl.sendParticles(ParticleTypes.END_ROD, to.x, to.y, to.z, 3, 0.2, 0.2, 0.2, 0.05);
        } else {
            sl.sendParticles(qiParticle, to.x, to.y, to.z, 3, 0.2, 0.2, 0.2, 0.02);
        }
    }
}
