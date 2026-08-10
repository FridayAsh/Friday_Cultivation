/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.projectile.ProjectileUtil
 *  net.minecraft.world.level.ClipContext
 *  net.minecraft.world.level.ClipContext$Block
 *  net.minecraft.world.level.ClipContext$Fluid
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.BlockHitResult
 *  net.minecraft.world.phys.EntityHitResult
 *  net.minecraft.world.phys.HitResult$Type
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.entity.IEntityAdditionalSpawnData
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PalmThunderOrbEntity
extends Entity
implements IEntityAdditionalSpawnData {
    private static final int MAX_LIFETIME = 80;
    private static final double BARRIER_REACH_RADIUS = 0.65;
    private UUID ownerUuid;
    private float damage = 30.0f;
    private float radius = 4.25f;
    private int lifetime;

    public PalmThunderOrbEntity(EntityType<? extends PalmThunderOrbEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public PalmThunderOrbEntity(Level level, LivingEntity owner, float damage) {
        this((EntityType<? extends PalmThunderOrbEntity>)((EntityType)ModEntities.PALM_THUNDER_ORB.get()), level);
        this.ownerUuid = owner.getUUID();
        this.damage = Math.max(1.0f, damage);
    }

    @Nullable
    public LivingEntity getOwnerEntity(ServerLevel level) {
        LivingEntity living;
        if (this.ownerUuid == null) {
            return null;
        }
        Entity entity = level.getEntity(this.ownerUuid);
        return entity instanceof LivingEntity ? (living = (LivingEntity)entity) : null;
    }

    public float damage() {
        return this.damage;
    }

    public float explosionRadius() {
        return this.radius;
    }

    protected void defineSynchedData() {
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        ++this.lifetime;
        Vec3 oldPos = this.position();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6 || this.lifetime > 80) {
            this.explode(server, oldPos);
            return;
        }
        LivingEntity owner = this.getOwnerEntity(server);
        Vec3 newPos = oldPos.add(movement);
        SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(server, this, oldPos, newPos, owner, this.damage, 0.65);
        if (barrierHit != null) {
            this.explode(server, barrierHit.hitPos(), false);
            return;
        }
        BlockHitResult blockHit = this.level().clip(new ClipContext(oldPos, newPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity)this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.explode(server, blockHit.getLocation());
            return;
        }
        AABB scan = this.getBoundingBox().expandTowards(movement).inflate(0.55);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult((Level)this.level(), (Entity)this, (Vec3)oldPos, (Vec3)newPos, (AABB)scan, this::canHitEntity);
        if (entityHit != null) {
            this.explode(server, entityHit.getLocation());
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private boolean canHitEntity(Entity entity) {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return false;
        }
        ServerLevel server = (ServerLevel)level;
        if (!(entity instanceof LivingEntity)) {
            return false;
        }
        LivingEntity living = (LivingEntity)entity;
        if (!living.isAlive() || !living.isPickable()) {
            return false;
        }
        if (this.ownerUuid != null && this.ownerUuid.equals(living.getUUID())) {
            return false;
        }
        LivingEntity owner = this.getOwnerEntity(server);
        return SoulStateHandler.canOrdinaryAffect((Entity)owner, (Entity)living);
    }

    private void explode(ServerLevel server, Vec3 center) {
        this.explode(server, center, true);
    }

    private void explode(ServerLevel server, Vec3 center, boolean touchBarrierArea) {
        PalmThunderHandler.explode(server, this, this.getOwnerEntity(server), center, this.damage, this.radius, touchBarrierArea);
        this.discard();
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        this.damage = tag.getFloat("damage");
        this.radius = tag.getFloat("radius");
        if (tag.contains("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
        tag.putFloat("radius", this.radius);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
    }

    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.damage);
        buffer.writeFloat(this.radius);
    }

    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.damage = additionalData.readFloat();
        this.radius = additionalData.readFloat();
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

