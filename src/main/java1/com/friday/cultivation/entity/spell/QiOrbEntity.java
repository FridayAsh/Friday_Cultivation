package com.friday.cultivation.entity.spell;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.qi.IQiConsumer;
import com.friday.cultivation.qi.consumer.PlayerQiConsumer;
import com.friday.cultivation.qi.consumer.WanderingCultivatorConsumer;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
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

import java.util.UUID;

/**
 * 灵气球 - 由灵脉/灵草等产生，吸引附近的灵气消费方（玩家/NPC/灵脉核心）回收。
 * 严格 1:1 复刻原 mod QiOrbEntity。
 */
public class QiOrbEntity extends Entity {
    private static final EntityDataAccessor<Integer> DATA_ELEMENT =
            SynchedEntityData.defineId(QiOrbEntity.class, EntityDataSerializers.INT);
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
        this((EntityType<? extends QiOrbEntity>) (EntityType<?>) ModEntities.QI_ORB.get(), level);
        this.setPos(x, y, z);
        this.setElement(element);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.set(DATA_ELEMENT, QiElement.PURE.ordinal());
    }

    public QiElement getElement() {
        int idx = this.entityData.get(DATA_ELEMENT);
        QiElement[] vals = QiElement.values();
        return vals[Math.floorMod(idx, vals.length)];
    }

    public void setElement(QiElement element) {
        this.entityData.set(DATA_ELEMENT, element.ordinal());
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age >= MAX_AGE_TICKS) {
            this.discard();
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        if (this.age < SPAWN_FLOAT_TICKS) {
            Vec3 v = this.getDeltaMovement();
            double vx = v.x * 0.92;
            double vy = SPAWN_FLOAT_RISE_SPEED;
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
            if (dist < ABSORB_DISTANCE) {
                target.receiveQi(this.getElement(), 1);
                this.discard();
                return;
            }
            Vec3 pull = toTarget.normalize().scale(ATTRACTION_SPEED);
            this.setDeltaMovement(pull);
        } else {
            Vec3 v = this.getDeltaMovement();
            double vx = v.x * 0.92;
            double vy = SPAWN_FLOAT_RISE_SPEED;
            double vz = v.z * 0.92;
            this.setDeltaMovement(vx, vy, vz);
        }
        Vec3 dm = this.getDeltaMovement();
        if (dm.lengthSqr() > 1.0E-8) {
            this.move(MoverType.SELF, dm);
        }
    }

    @Nullable
    private IQiConsumer findAttractingConsumer() {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return null;
        }
        ServerLevel server = (ServerLevel) level;
        IQiConsumer best = null;
        double bestDist = Double.MAX_VALUE;
        if (this.cachedPlayerUuid != null) {
            ServerPlayer p = server.getServer().getPlayerList().getPlayer(this.cachedPlayerUuid);
            if (p != null && p.isAlive() && p.level() == server) {
                PlayerQiConsumer c = PlayerQiConsumer.wrap(p);
                if (c != null && c.wantsMore()) {
                    double radius = c.attractRadius();
                    double distSq = c.position().distanceToSqr(this.position());
                    if (distSq <= radius * radius) {
                        bestDist = distSq;
                        best = c;
                    }
                }
            }
            if (best == null) {
                this.cachedPlayerUuid = null;
            }
        }
        if (best == null) {
            for (Object p : server.getPlayers(p -> true)) {
                PlayerQiConsumer c = PlayerQiConsumer.wrap((ServerPlayer) p);
                if (c == null || !c.wantsMore()) continue;
                double radius = c.attractRadius();
                double distSq = c.position().distanceToSqr(this.position());
                if (distSq > radius * radius || !(distSq < bestDist)) continue;
                bestDist = distSq;
                best = c;
                this.cachedPlayerUuid = ((ServerPlayer) p).getUUID();
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

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        if (tag.contains("element")) {
            this.setElement(QiElement.byId(tag.getString("element")));
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        tag.putString("element", this.getElement().id());
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
