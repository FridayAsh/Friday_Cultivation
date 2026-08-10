package com.friday.cultivation.entity;

import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
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

public class SkyTrailEntity extends Entity {
    private static final EntityDataAccessor<Float> DATA_END_X = SynchedEntityData.defineId(SkyTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Y = SynchedEntityData.defineId(SkyTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_END_Z = SynchedEntityData.defineId(SkyTrailEntity.class, EntityDataSerializers.FLOAT);
    private static final int LIFETIME = 600;
    private static final int FADE_START = 500;
    private int age = 0;

    public SkyTrailEntity(EntityType<? extends SkyTrailEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SkyTrailEntity(ServerLevel level, Vec3 start, Vec3 end) {
        this(ModEntities.SKY_TRAIL.get(), level);
        this.setPos(start.x, start.y, start.z);
        this.entityData.set(DATA_END_X, (float) end.x);
        this.entityData.set(DATA_END_Y, (float) end.y);
        this.entityData.set(DATA_END_Z, (float) end.z);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_END_X, 0.0f);
        this.entityData.define(DATA_END_Y, 0.0f);
        this.entityData.define(DATA_END_Z, 0.0f);
    }

    public Vec3 endPos() {
        return new Vec3(this.entityData.get(DATA_END_X), this.entityData.get(DATA_END_Y), this.entityData.get(DATA_END_Z));
    }

    public float alphaMultiplier() {
        if (this.age < FADE_START) {
            return 1.0f;
        }
        return Math.max(0.0f, 1.0f - (float) (this.age - FADE_START) / 100.0f);
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age > LIFETIME) {
            this.discard();
        }
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        this.entityData.set(DATA_END_X, tag.getFloat("ex"));
        this.entityData.set(DATA_END_Y, tag.getFloat("ey"));
        this.entityData.set(DATA_END_Z, tag.getFloat("ez"));
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        Vec3 e = this.endPos();
        tag.putFloat("ex", (float) e.x);
        tag.putFloat("ey", (float) e.y);
        tag.putFloat("ez", (float) e.z);
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected AABB makeBoundingBox() {
        Vec3 e = this.endPos();
        return new AABB(
                Math.min(this.getX(), e.x), Math.min(this.getY(), e.y), Math.min(this.getZ(), e.z),
                Math.max(this.getX(), e.x), Math.max(this.getY(), e.y), Math.max(this.getZ(), e.z)
        ).inflate(2.0);
    }
}
