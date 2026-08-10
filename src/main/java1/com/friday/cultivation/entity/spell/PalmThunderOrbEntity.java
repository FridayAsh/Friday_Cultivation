package com.friday.cultivation.entity.spell;

import com.friday.cultivation.event.PalmThunderHandler;
import com.friday.cultivation.event.SectProtectionDomeHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
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

import java.util.UUID;

/**
 * 掌心雷抛射物实体 — 完整复刻原模组 PalmThunderOrbEntity
 */
public class PalmThunderOrbEntity extends Entity implements IEntityAdditionalSpawnData {
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
        this((EntityType<? extends PalmThunderOrbEntity>) ModEntities.PALM_THUNDER_ORB.get(), level);
        this.ownerUuid = owner.getUUID();
        this.damage = Math.max(1.0f, damage);
    }

    @Nullable
    public LivingEntity getOwnerEntity(ServerLevel level) {
        if (this.ownerUuid == null) return null;
        Entity entity = level.getEntity(this.ownerUuid);
        return entity instanceof LivingEntity ? (LivingEntity) entity : null;
    }

    public float damage() { return this.damage; }
    public float explosionRadius() { return this.radius; }

    @Override
    protected void defineSynchedData() {}

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) return;
        super.tick();
        if (this.level().isClientSide) return;
        if (!(this.level() instanceof ServerLevel server)) return;
        this.lifetime++;
        Vec3 oldPos = this.position();
        Vec3 movement = this.getDeltaMovement();
        if (movement.lengthSqr() < 1.0E-6 || this.lifetime > MAX_LIFETIME) {
            this.explode(server, oldPos);
            return;
        }
        LivingEntity owner = this.getOwnerEntity(server);
        Vec3 newPos = oldPos.add(movement);
        SectProtectionDomeHandler.BarrierHit barrierHit = SectProtectionDomeHandler.touchProjectileBarrier(
                server, this, oldPos, newPos, owner, this.damage, BARRIER_REACH_RADIUS);
        if (barrierHit != null) {
            this.explode(server, barrierHit.hitPos(), false);
            return;
        }
        BlockHitResult blockHit = this.level().clip(new ClipContext(oldPos, newPos, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockHit.getType() != HitResult.Type.MISS) {
            this.explode(server, blockHit.getLocation());
            return;
        }
        AABB scan = this.getBoundingBox().expandTowards(movement).inflate(0.55);
        EntityHitResult entityHit = ProjectileUtil.getEntityHitResult(this.level(), this, oldPos, newPos, scan, this::canHitEntity);
        if (entityHit != null) {
            this.explode(server, entityHit.getLocation());
            return;
        }
        this.setPos(newPos.x, newPos.y, newPos.z);
    }

    private boolean canHitEntity(Entity entity) {
        if (!(this.level() instanceof ServerLevel server)) return false;
        if (!(entity instanceof LivingEntity living)) return false;
        if (!living.isAlive() || !living.isPickable()) return false;
        if (this.ownerUuid != null && this.ownerUuid.equals(living.getUUID())) return false;
        LivingEntity owner = this.getOwnerEntity(server);
        return SoulStateHandler.canOrdinaryAffect(owner, living);
    }

    private void explode(ServerLevel server, Vec3 center) {
        this.explode(server, center, true);
    }

    private void explode(ServerLevel server, Vec3 center, boolean touchBarrierArea) {
        PalmThunderHandler.explode(server, this, this.getOwnerEntity(server), center, this.damage, this.radius, touchBarrierArea);
        this.discard();
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.lifetime = tag.getInt("lifetime");
        this.damage = tag.getFloat("damage");
        this.radius = tag.getFloat("radius");
        if (tag.hasUUID("owner")) {
            this.ownerUuid = tag.getUUID("owner");
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("lifetime", this.lifetime);
        tag.putFloat("damage", this.damage);
        tag.putFloat("radius", this.radius);
        if (this.ownerUuid != null) {
            tag.putUUID("owner", this.ownerUuid);
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeFloat(this.damage);
        buffer.writeFloat(this.radius);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.damage = additionalData.readFloat();
        this.radius = additionalData.readFloat();
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
