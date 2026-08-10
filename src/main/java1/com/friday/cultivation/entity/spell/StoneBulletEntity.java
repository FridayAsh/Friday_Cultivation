package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.SectCombatHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import com.friday.cultivation.util.SpellDamageSourceHelper;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * 石弹术实体 — 完整复刻原模组 StoneBulletEntity
 */
public class StoneBulletEntity extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME = 100;
    private static final double BARRIER_REACH_RADIUS = 0.55;
    private float damage = 20.0f;
    private int lifetime;

    public StoneBulletEntity(EntityType<? extends StoneBulletEntity> type, Level level) {
        super(type, level);
    }

    public StoneBulletEntity(Level level, LivingEntity owner) {
        super(ModEntities.STONE_BULLET.get(), owner, level);
    }

    public void setDamage(float damage) { this.damage = Math.max(0.0f, damage); }
    public float getDamage() { return this.damage; }

    @NotNull
    @Override
    protected Item getDefaultItem() { return Items.POINTED_DRIPSTONE; }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) return;
        Vec3 oldPos = this.position();
        super.tick();
        if (this.level().isClientSide) {
            this.spawnTrailParticles();
            return;
        }
        if (this.isRemoved()) return;
        this.lifetime++;
        if (this.lifetime > MAX_LIFETIME) {
            this.discard();
            return;
        }
        if (this.level() instanceof ServerLevel server) {
            Entity ownerEntity = this.getOwner();
            LivingEntity owner = ownerEntity instanceof LivingEntity living ? living : null;
            SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(
                    server, this, oldPos, this.position(), owner, this.damage, BARRIER_REACH_RADIUS);
            if (barrierHit != null) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (this.level().isClientSide) return;
        Entity entity = result.getEntity();
        if (!(entity instanceof LivingEntity target) || !target.isAlive()) {
            this.discard();
            return;
        }
        Entity ownerEntity = this.getOwner();
        if (ownerEntity != null && ownerEntity.getUUID().equals(target.getUUID())) {
            this.discard();
            return;
        }
        LivingEntity owner = ownerEntity instanceof LivingEntity living ? living : null;
        if (SoulStateHandler.canOrdinaryAffect(owner, target)
                && SectCombatHandler.canApplyOffensiveEffect(owner, target)
                && !SectProtectionDomeHandler.isEntityProtectedByOwnDome(target)) {
            target.hurt(SpellDamageSourceHelper.indirectSpell(this, owner), this.damage);
        }
        if (this.level() instanceof ServerLevel server) {
            this.spawnImpactFx(server, result.getLocation());
        }
        this.discard();
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) return;
        if (this.level() instanceof ServerLevel server) {
            this.spawnImpactFx(server, result.getLocation());
        }
        this.discard();
    }

    @Override
    protected float getGravity() { return 0.02f; }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("damage")) {
            this.damage = tag.getFloat("damage");
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private void spawnTrailParticles() {
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 0.01) return;
        Vec3 dir = velocity.normalize();
        Vec3 tail = this.position().subtract(dir.scale(0.25));
        this.level().addParticle(new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.POINTED_DRIPSTONE.defaultBlockState()), tail.x, tail.y, tail.z, 0.0, 0.0, 0.0);
        this.level().addParticle(ParticleTypes.CRIT, tail.x + (this.random.nextDouble() - 0.5) * 0.12, tail.y + (this.random.nextDouble() - 0.5) * 0.12, tail.z + (this.random.nextDouble() - 0.5) * 0.12, -dir.x * 0.04, -dir.y * 0.04, -dir.z * 0.04);
    }

    private void spawnImpactFx(ServerLevel server, Vec3 pos) {
        server.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()), pos.x, pos.y, pos.z, 14, 0.18, 0.18, 0.18, 0.08);
        server.sendParticles(ParticleTypes.CRIT, pos.x, pos.y, pos.z, 8, 0.18, 0.18, 0.18, 0.05);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.55f, 1.35f);
    }
}
