/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
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
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class MushroomCloudEntity
extends Entity {
    private static final int LIFETIME = 600;
    private static final int ERUPTION_TICKS = 80;
    private static final int DISSIPATION_START = 400;
    private static final double STEM_RADIUS = 10.0;
    private static final double STEM_HEIGHT = 45.0;
    private static final double CAP_INNER_RADIUS = 14.0;
    private static final double CAP_OUTER_RADIUS = 45.0;
    private static final double CAP_BASE_Y = 40.0;
    private static final double CAP_TOP_Y = 80.0;
    private static final double CORE_RADIUS = 6.0;
    private static final double CORE_HEIGHT = 15.0;
    private int age = 0;

    public MushroomCloudEntity(EntityType<? extends MushroomCloudEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public MushroomCloudEntity(ServerLevel level, Vec3 center) {
        this((EntityType<? extends MushroomCloudEntity>)((EntityType)ModEntities.MUSHROOM_CLOUD.get()), (Level)level);
        this.setPos(center.x, center.y, center.z);
    }

    protected void defineSynchedData() {
    }

    public void tick() {
        double pz;
        double px;
        double y;
        double r;
        double angle;
        int i;
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age > 600) {
            this.discard();
            return;
        }
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel server = (ServerLevel)level;
        double cx = this.getX();
        double cy = this.getY();
        double cz = this.getZ();
        float intensity = this.age < 80 ? 2.5f : (this.age < 400 ? 1.0f : Math.max(0.0f, 1.0f - (float)(this.age - 400) / 200.0f));
        if (intensity <= 0.0f) {
            return;
        }
        if (this.age < 400) {
            int coreCount = (int)(12.0f * intensity);
            for (i = 0; i < coreCount; ++i) {
                angle = this.random.nextDouble() * Math.PI * 2.0;
                r = this.random.nextDouble() * 6.0;
                y = this.random.nextDouble() * 15.0;
                px = cx + Math.cos(angle) * r;
                pz = cz + Math.sin(angle) * r;
                server.sendParticles((ParticleOptions)ParticleTypes.FLAME, px, cy + y, pz, 0, 0.0, 0.2, 0.0, 0.3);
                if (i % 2 != 0) continue;
                server.sendParticles((ParticleOptions)ParticleTypes.LAVA, px, cy + y * 0.5, pz, 0, 0.0, 0.1, 0.0, 0.0);
            }
        }
        int stemCount = (int)(35.0f * intensity);
        for (i = 0; i < stemCount; ++i) {
            angle = this.random.nextDouble() * Math.PI * 2.0;
            r = this.random.nextDouble() * 10.0;
            y = this.random.nextDouble() * 45.0;
            px = cx + Math.cos(angle) * r;
            pz = cz + Math.sin(angle) * r;
            server.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, px, cy + y, pz, 0, Math.cos(angle) * 0.02, 0.15 + this.random.nextDouble() * 0.1, Math.sin(angle) * 0.02, 1.0);
        }
        int capExplosionCount = (int)(8.0f * intensity);
        for (int i2 = 0; i2 < capExplosionCount; ++i2) {
            double angle2 = this.random.nextDouble() * Math.PI * 2.0;
            double r2 = 14.0 + this.random.nextDouble() * 31.0;
            double y2 = 40.0 + this.random.nextDouble() * 40.0;
            double px2 = cx + Math.cos(angle2) * r2;
            double pz2 = cz + Math.sin(angle2) * r2;
            server.sendParticles((ParticleOptions)ParticleTypes.EXPLOSION, px2, cy + y2, pz2, 0, 0.0, 0.0, 0.0, 0.0);
        }
        int capOuterCount = (int)(70.0f * intensity);
        for (int i3 = 0; i3 < capOuterCount; ++i3) {
            double angle3 = this.random.nextDouble() * Math.PI * 2.0;
            double r3 = 14.0 + this.random.nextDouble() * 31.0;
            double y3 = 40.0 + this.random.nextDouble() * 40.0;
            double px3 = cx + Math.cos(angle3) * r3;
            double pz3 = cz + Math.sin(angle3) * r3;
            server.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, px3, cy + y3, pz3, 0, Math.cos(angle3) * 0.08, 0.05, Math.sin(angle3) * 0.08, 1.0);
        }
        int capInnerCount = (int)(40.0f * intensity);
        for (int i4 = 0; i4 < capInnerCount; ++i4) {
            double angle4 = this.random.nextDouble() * Math.PI * 2.0;
            double r4 = 14.0 + this.random.nextDouble() * 12.0;
            double y4 = 40.0 + this.random.nextDouble() * 40.0;
            double px4 = cx + Math.cos(angle4) * r4;
            double pz4 = cz + Math.sin(angle4) * r4;
            server.sendParticles((ParticleOptions)ParticleTypes.POOF, px4, cy + y4, pz4, 0, Math.cos(angle4) * 0.08, 0.05, Math.sin(angle4) * 0.08, 0.8);
        }
        int capSkirtCount = (int)(30.0f * intensity);
        for (int i5 = 0; i5 < capSkirtCount; ++i5) {
            double angle5 = this.random.nextDouble() * Math.PI * 2.0;
            double r5 = 11.200000000000001 + this.random.nextDouble() * 31.0 * 1.1;
            double y5 = 40.0 - this.random.nextDouble() * 6.0;
            double px5 = cx + Math.cos(angle5) * r5;
            double pz5 = cz + Math.sin(angle5) * r5;
            server.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, px5, cy + y5, pz5, 0, Math.cos(angle5) * 0.1, -0.02, Math.sin(angle5) * 0.1, 1.0);
        }
        if (this.age < 400) {
            int dripCount = (int)(10.0f * intensity);
            for (int i6 = 0; i6 < dripCount; ++i6) {
                double angle6 = this.random.nextDouble() * Math.PI * 2.0;
                double r6 = 14.0 + this.random.nextDouble() * 31.0 * 0.7;
                double yStart = 40.0 - this.random.nextDouble() * 8.0;
                double px6 = cx + Math.cos(angle6) * r6;
                double pz6 = cz + Math.sin(angle6) * r6;
                server.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, px6, cy + yStart, pz6, 0, 0.0, -0.08, 0.0, 1.0);
            }
        }
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }

    protected AABB makeBoundingBox() {
        return new AABB(this.getX() - 1.0, this.getY() - 1.0, this.getZ() - 1.0, this.getX() + 1.0, this.getY() + 1.0, this.getZ() + 1.0);
    }
}

