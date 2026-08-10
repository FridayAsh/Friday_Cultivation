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
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity;

import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.IQiConsumer;
import com.friday.cultivation.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.cultivation.qi.consumer.WanderingCultivatorConsumer;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class QiOrbEntity
extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ELEMENT = SynchedEntityData.defineId(QiOrbEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final int MAX_AGE_TICKS = 600;
    private static final int SPAWN_FLOAT_TICKS = 30;
    private static final double SPAWN_FLOAT_RISE_SPEED = 0.04;
    private static final double ABSORB_DISTANCE = 1.0;
    private static final double ATTRACTION_SPEED = 0.35;
    private int age = 0;
    @Nullable
    private UUID cachedPlayerUuid = null;

    public QiOrbEntity(EntityType<? extends QiOrbEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public QiOrbEntity(Level level, double x, double y, double z, QiElement element) {
        this((EntityType<? extends QiOrbEntity>)((EntityType)ModEntities.QI_ORB.get()), level);
        this.setPos(x, y, z);
        this.setElement(element);
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_ELEMENT, QiElement.PURE.ordinal());
    }

    public QiElement getElement() {
        int idx = (Integer)this.entityData.get(DATA_ELEMENT);
        QiElement[] vals = QiElement.values();
        return vals[Math.floorMod(idx, vals.length)];
    }

    public void setElement(QiElement element) {
        this.entityData.set(DATA_ELEMENT, element.ordinal());
    }

    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age >= 600) {
            this.discard();
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        if (this.age < 30) {
            Vec3 v = this.getDeltaMovement();
            double vx = v.x * 0.92;
            double vy = 0.04;
            double vz = v.z * 0.92;
            this.setDeltaMovement(vx, vy, vz);
            this.move(MoverType.SELF, this.getDeltaMovement());
            return;
        }
        IQiConsumer target = this.findAttractingConsumer();
        if (target != null) {
            Vec3 targetPos = target.position();
            Vec3 toTarget = new Vec3(targetPos.x - this.getX(), targetPos.y - this.getY(), targetPos.z - this.getZ());
            double dist = toTarget.length();
            if (dist < 1.0) {
                target.receiveQi(this.getElement(), 1);
                this.discard();
                return;
            }
            Vec3 pull = toTarget.normalize().scale(0.35);
            this.setDeltaMovement(pull);
        } else {
            Vec3 v = this.getDeltaMovement();
            double vx = v.x * 0.92;
            double vy = 0.04;
            double vz = v.z * 0.92;
            this.setDeltaMovement(vx, vy, vz);
        }
        Vec3 dm = this.getDeltaMovement();
        if (dm.lengthSqr() > 1.0E-8) {
            this.move(MoverType.SELF, dm);
        }
    }

    private IQiConsumer findAttractingConsumer() {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel server = (ServerLevel)level;
        IQiConsumer best = null;
        double bestDist = Double.MAX_VALUE;
        if (this.cachedPlayerUuid != null) {
            PlayerQiConsumer c;
            ServerPlayer p = server.getServer().getPlayerList().getPlayer(this.cachedPlayerUuid);
            if (p != null && p.isAlive() && p.level() == server && (c = PlayerQiConsumer.wrap(p)) != null && c.wantsMore()) {
                double radius = c.attractRadius();
                double distSq = c.position().distanceToSqr(this.position());
                if (distSq <= radius * radius) {
                    bestDist = distSq;
                    best = c;
                }
            }
            if (best == null) {
                this.cachedPlayerUuid = null;
            }
        }
        if (best == null) {
            for (ServerPlayer p : server.players()) {
                PlayerQiConsumer c = PlayerQiConsumer.wrap(p);
                if (c == null || !c.wantsMore()) continue;
                double radius = c.attractRadius();
                double distSq = c.position().distanceToSqr(this.position());
                if (distSq > radius * radius || !(distSq < bestDist)) continue;
                bestDist = distSq;
                best = c;
                this.cachedPlayerUuid = p.getUUID();
            }
        }
        AABB searchBox = this.getBoundingBox().inflate(8.0);
        for (WanderingCultivatorEntity npc : server.getEntitiesOfClass(WanderingCultivatorEntity.class, searchBox)) {
            WanderingCultivatorConsumer c = WanderingCultivatorConsumer.wrap(npc);
            if (c == null || !c.wantsMore()) continue;
            double radius = c.attractRadius();
            double distSq = c.position().distanceToSqr(this.position());
            if (distSq > radius * radius || !(distSq < bestDist)) continue;
            bestDist = distSq;
            best = c;
        }
        IQiConsumer spiritVeinCore = SpiritVeinCoreBlockEntity.findNearestConsumer(server, this.position(), bestDist);
        if (spiritVeinCore != null) {
            best = spiritVeinCore;
        }
        return best;
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        if (tag.contains("element")) {
            this.setElement(QiElement.byId(tag.getString("element")));
        }
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        tag.putString("element", this.getElement().id());
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

