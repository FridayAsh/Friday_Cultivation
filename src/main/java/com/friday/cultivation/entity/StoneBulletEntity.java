/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.BlockParticleOption
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ThrowableItemProjectile
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity;

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

public class StoneBulletEntity
extends ThrowableItemProjectile {
    private static final int MAX_LIFETIME = 100;
    private static final double BARRIER_REACH_RADIUS = 0.55;
    private float damage = 20.0f;
    private int lifetime;

    public StoneBulletEntity(EntityType<? extends StoneBulletEntity> type, Level level) {
        super(type, level);
    }

    public StoneBulletEntity(Level level, LivingEntity owner) {
        super((EntityType)ModEntities.STONE_BULLET.get(), owner, level);
    }

    public void setDamage(float damage) {
        this.damage = Math.max(0.0f, damage);
    }

    public float getDamage() {
        return this.damage;
    }

    @NotNull
    protected Item getDefaultItem() {
        return Items.POINTED_DRIPSTONE;
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime((Entity)this)) {
            return;
        }
        Vec3 oldPos = this.position();
        super.tick();
        if (this.level().isClientSide) {
            this.spawnTrailParticles();
            return;
        }
        if (this.isRemoved()) {
            return;
        }
        ++this.lifetime;
        if (this.lifetime > 100) {
            this.discard();
            return;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            LivingEntity living;
            ServerLevel server = (ServerLevel)level;
            Entity ownerEntity = this.getOwner();
            LivingEntity owner = ownerEntity instanceof LivingEntity ? (living = (LivingEntity)ownerEntity) : null;
            SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(server, (Entity)this, oldPos, this.position(), owner, this.damage, 0.55);
            if (barrierHit != null) {
                this.discard();
            }
        }
    }

    protected void recreateFromPacket(@NotNull EntityHitResult result) {
        Level level;
        LivingEntity living;
        LivingEntity owner;
        LivingEntity target;
        super.onHit(result);
        if (this.level().isClientSide) {
            return;
        }
        Entity entity = result.getEntity();
        if (!(entity instanceof LivingEntity) || !(target = (LivingEntity)entity).isAlive()) {
            this.discard();
            return;
        }
        Entity ownerEntity = this.getOwner();
        if (ownerEntity != null && ownerEntity.getUUID().equals(target.getUUID())) {
            this.discard();
            return;
        }
        LivingEntity livingEntity = owner = ownerEntity instanceof LivingEntity ? (living = (LivingEntity)ownerEntity) : null;
        if (SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)target) && SectCombatHandler.canApplyOffensiveEffect(owner, target) && !SectProtectionDomeHandler.isEntityProtectedByOwnDome((Entity)target)) {
            target.hurt(SpellDamageSourceHelper.indirectSpell((Entity)this, owner), this.damage);
        }
        if ((level = this.level()) instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            this.spawnImpactFx(server, result.getLocation());
        }
        this.discard();
    }

    protected void onHitBlock(@NotNull BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel)level;
            this.spawnImpactFx(server, result.getLocation());
        }
        this.discard();
    }

    protected float getGravity() {
        return 0.02f;
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.lifetime = tag.getInt("lifetime");
        if (tag.contains("damage")) {
            this.damage = tag.getFloat("damage");
        }
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    private void spawnTrailParticles() {
        Vec3 velocity = this.getDeltaMovement();
        double speed = velocity.length();
        if (speed < 0.01) {
            return;
        }
        Vec3 dir = velocity.scale(1.0 / speed);
        Vec3 tail = this.position().multiply(dir.scale(0.25));
        this.level().addParticle((ParticleOptions)new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.POINTED_DRIPSTONE.defaultBlockState()), tail.x, tail.y, tail.z, 0.0, 0.0, 0.0);
        this.level().addParticle((ParticleOptions)ParticleTypes.CRIT, tail.x + (this.random.nextDouble() - 0.5) * 0.12, tail.y + (this.random.nextDouble() - 0.5) * 0.12, tail.z + (this.random.nextDouble() - 0.5) * 0.12, -dir.x * 0.04, -dir.y * 0.04, -dir.z * 0.04);
    }

    private void spawnImpactFx(ServerLevel server, Vec3 pos) {
        server.sendParticles((ParticleOptions)new BlockParticleOption(ParticleTypes.BLOCK, Blocks.POINTED_DRIPSTONE.defaultBlockState()), pos.x, pos.y, pos.z, 14, 0.18, 0.18, 0.18, 0.08);
        server.sendParticles((ParticleOptions)ParticleTypes.CRIT, pos.x, pos.y, pos.z, 8, 0.18, 0.18, 0.18, 0.05);
        server.playSound(null, pos.x, pos.y, pos.z, SoundEvents.STONE_BREAK, SoundSource.PLAYERS, 0.55f, 1.35f);
    }
}

