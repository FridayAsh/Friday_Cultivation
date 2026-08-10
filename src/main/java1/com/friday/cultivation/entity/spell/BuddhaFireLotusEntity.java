package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.BuddhaFireLotusExplosionHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

/**
 * 佛怒火莲实体 — 完整复刻原模组 BuddhaFireLotusEntity
 * 四阶段充能+追踪制导+命中爆炸（委托BuddhaFireLotusExplosionHandler）
 */
public class BuddhaFireLotusEntity extends Entity implements IEntityAdditionalSpawnData {
    public static final int ROOT_METAL = 1;
    public static final int ROOT_WOOD = 2;
    public static final int ROOT_WATER = 4;
    public static final int ROOT_FIRE = 8;
    public static final int ROOT_EARTH = 16;
    public static final int ROOT_PURE = 32;
    private static final EntityDataAccessor<Integer> DATA_CHARGE = SynchedEntityData.defineId(BuddhaFireLotusEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_ROOT_FLAGS = SynchedEntityData.defineId(BuddhaFireLotusEntity.class, EntityDataSerializers.INT);
    private static final int MAX_LIFETIME = 360;
    private static final double BASE_SPEED = 0.62;
    private static final double MAX_SPEED = 1.15;
    private static final double HOMING_STRENGTH = 0.075;
    private static final double BARRIER_REACH_RADIUS = 1.2;

    private UUID ownerUuid;
    private UUID targetUuid;
    private float damage = 5000.0f;
    private float radius = 32.0f;
    private int lifetime;

    public BuddhaFireLotusEntity(EntityType<? extends BuddhaFireLotusEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public BuddhaFireLotusEntity(Level level, LivingEntity owner) {
        this((EntityType<? extends BuddhaFireLotusEntity>) ModEntities.BUDDHA_FIRE_LOTUS.get(), level);
        this.ownerUuid = owner.getUUID();
    }

    public void configure(float damage, float radius, int chargedQi, int rootFlags, @Nullable LivingEntity target) {
        this.damage = Math.max(1.0f, damage);
        this.radius = Math.max(8.0f, radius);
        this.entityData.set(DATA_CHARGE, Math.max(0, chargedQi));
        this.entityData.set(DATA_ROOT_FLAGS, rootFlags);
        this.targetUuid = target == null ? null : target.getUUID();
    }

    public int chargedQi() {
        return this.entityData.get(DATA_CHARGE);
    }

    public int rootFlags() {
        return this.entityData.get(DATA_ROOT_FLAGS);
    }

    public float damage() {
        return this.damage;
    }

    public float explosionRadius() {
        return this.radius;
    }

    public float visualScale() {
        float charge = Math.max(1.0f, (float) this.chargedQi() / 10000.0f);
        return Mth.clamp(0.75f + (float) Math.sqrt(charge) * 0.25f, 0.8f, 1.8f);
    }

    @Nullable
    public LivingEntity getOwnerEntity(ServerLevel level) {
        if (this.ownerUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.ownerUuid);
        return entity instanceof LivingEntity le ? le : null;
    }

    @Nullable
    private LivingEntity getTargetEntity(ServerLevel level) {
        if (this.targetUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.targetUuid);
        return entity instanceof LivingEntity living && living.isAlive() ? living : null;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_CHARGE, 0);
        this.entityData.define(DATA_ROOT_FLAGS, 0);
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel server)) {
            return;
        }
        ++this.lifetime;
        if (this.lifetime > MAX_LIFETIME) {
            this.explode(server, this.position());
            return;
        }
        LivingEntity owner = this.getOwnerEntity(server);
        LivingEntity target = this.getTargetEntity(server);
        Vec3 movement = this.guidedMovement(target);
        if (movement.lengthSqr() < 1.0E-6) {
            this.explode(server, this.position());
            return;
        }
        Vec3 oldPos = this.position();
        Vec3 newPos = oldPos.add(movement);
        SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(server, this, oldPos, newPos, owner, this.damage, BARRIER_REACH_RADIUS);
        if (barrierHit != null) {
            this.impactOnBarrier(barrierHit.hitPos());
            return;
        }
        HitResult hit = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);
        if (hit.getType() != HitResult.Type.MISS) {
            this.explode(server, hit.getLocation());
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
        if (target != null && target.distanceToSqr(this) <= 2.25) {
            this.explode(server, target.position().add(0.0, (double) target.getBbHeight() * 0.45, 0.0));
        }
    }

    private Vec3 guidedMovement(@Nullable LivingEntity target) {
        Vec3 current = this.getDeltaMovement();
        if (current.lengthSqr() < 1.0E-6) {
            current = this.getLookAngle().lengthSqr() < 1.0E-6 ? new Vec3(0.0, 0.0, 1.0) : this.getLookAngle();
        }
        Vec3 direction = current.normalize();
        if (target != null) {
            Vec3 wanted = target.getEyePosition().subtract(this.position());
            if (wanted.lengthSqr() > 1.0E-6) {
                direction = direction.lerp(wanted.normalize(), HOMING_STRENGTH).normalize();
            }
        }
        double charge = Math.max(1.0, (double) this.chargedQi() / 10000.0);
        double speed = Math.min(MAX_SPEED, BASE_SPEED + Math.sqrt(charge) * 0.11);
        Vec3 movement = direction.scale(speed);
        this.setDeltaMovement(movement);
        return movement;
    }

    private boolean canHitEntity(Entity entity) {
        if (entity == this) {
            return false;
        }
        if (!entity.isAlive()) {
            return false;
        }
        if (this.ownerUuid != null && this.ownerUuid.equals(entity.getUUID())) {
            return false;
        }
        return entity.isPickable();
    }

    private void explode(ServerLevel server, Vec3 center) {
        this.explode(server, center, true);
    }

    private void explode(ServerLevel server, Vec3 center, boolean touchBarrierArea) {
        LivingEntity owner = this.getOwnerEntity(server);
        BuddhaFireLotusExplosionHandler.explode(server, this, owner, center, this.damage, this.radius, this.chargedQi(), this.rootFlags(), touchBarrierArea);
        this.discard();
    }

    public void impactOnBarrier(Vec3 hitPos) {
        if (!(this.level() instanceof ServerLevel server)) {
            return;
        }
        this.explode(server, hitPos, false);
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        this.damage = tag.getFloat("damage");
        this.radius = tag.getFloat("radius");
        if (tag.hasUUID("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
        if (tag.hasUUID("target")) {
            this.targetUuid = tag.getUUID("target");
        }
        this.entityData.set(DATA_CHARGE, tag.getInt("chargedQi"));
        this.entityData.set(DATA_ROOT_FLAGS, tag.getInt("rootFlags"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
        tag.putFloat("radius", this.radius);
        tag.putInt("chargedQi", this.chargedQi());
        tag.putInt("rootFlags", this.rootFlags());
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
        if (this.targetUuid != null) {
            tag.putUUID("target", this.targetUuid);
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.damage);
        buffer.writeFloat(this.radius);
        buffer.writeVarInt(this.chargedQi());
        buffer.writeVarInt(this.rootFlags());
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.damage = additionalData.readFloat();
        this.radius = additionalData.readFloat();
        this.entityData.set(DATA_CHARGE, additionalData.readVarInt());
        this.entityData.set(DATA_ROOT_FLAGS, additionalData.readVarInt());
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
