/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class SkyTrailEntity
extends Entity {
    private static final EntityDataAccessor<Float> DATA_END_X = SynchedEntityData.defineId(SkyTrailEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y = SynchedEntityData.defineId(SkyTrailEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z = SynchedEntityData.defineId(SkyTrailEntity.class, (EntityDataSerializer)EntityDataSerializers.FLOAT);
    private static final int LIFETIME = 600;
    private static final int FADE_START = 500;
    private int age = 0;

    public SkyTrailEntity(EntityType<? extends SkyTrailEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SkyTrailEntity(ServerLevel level, Vec3 start, Vec3 end) {
        this((EntityType<? extends SkyTrailEntity>)((EntityType)ModEntities.SKY_TRAIL.get()), (Level)level);
        this.setPos(start.x, start.y, start.z);
        this.entityData.set(DATA_END_X, Float.valueOf((float)end.x));
        this.entityData.set(DATA_END_Y, Float.valueOf((float)end.y));
        this.entityData.set(DATA_END_Z, Float.valueOf((float)end.z));
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_END_X, Float.valueOf(0.0f));
        this.entityData.define(DATA_END_Y, Float.valueOf(0.0f));
        this.entityData.define(DATA_END_Z, Float.valueOf(0.0f));
    }

    public Vec3 endPos() {
        return new Vec3((double)((Float)this.entityData.get(DATA_END_X)).floatValue(), (double)((Float)this.entityData.get(DATA_END_Y)).floatValue(), (double)((Float)this.entityData.get(DATA_END_Z)).floatValue());
    }

    public float alphaMultiplier() {
        if (this.age < 500) {
            return 1.0f;
        }
        return Math.max(0.0f, 1.0f - (float)(this.age - 500) / 100.0f);
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age > 600) {
            this.discard();
        }
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        this.entityData.set(DATA_END_X, Float.valueOf(tag.getFloat("ex")));
        this.entityData.set(DATA_END_Y, Float.valueOf(tag.getFloat("ey")));
        this.entityData.set(DATA_END_Z, Float.valueOf(tag.getFloat("ez")));
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        Vec3 e = this.endPos();
        tag.putFloat("ex", (float)e.x);
        tag.putFloat("ey", (float)e.y);
        tag.putFloat("ez", (float)e.z);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        Vec3 e = this.endPos();
        return new AABB(Math.min(this.getX(), e.x), Math.min(this.getY(), e.y), Math.min(this.getZ(), e.z), Math.max(this.getX(), e.x), Math.max(this.getY(), e.y), Math.max(this.getZ(), e.z)).inflate(2.0);
    }
}

