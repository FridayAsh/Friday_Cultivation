package com.friday.cultivation.entity.npc;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.registry.ModDimensions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * 牛头马面实体 — 完整复刻原模组 SoulReaperEntity
 * 追踪目标灵魂→铁链牵引→护送到地府，无敌不可推，3600tick后消失
 */
public class SoulReaperEntity extends Mob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(SoulReaperEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHAINING = SynchedEntityData.defineId(SoulReaperEntity.class, EntityDataSerializers.BOOLEAN);
    private static final int CHAIN_DISTANCE = 3;
    private static final int MAX_LIFE_TICKS = 3600;
    private static final double MOVE_SPEED = 0.35;

    private Realm realm = Realm.QI_REFINING;
    private UUID targetSoulUuid;
    private int escortTicks = 0;
    private int lifeTicks = 0;

    public SoulReaperEntity(EntityType<? extends SoulReaperEntity> type, Level level) {
        super(type, level);
        this.setPersistenceRequired();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000.0)
                .add(Attributes.MOVEMENT_SPEED, MOVE_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.ARMOR, 100.0)
                .add(Attributes.ARMOR_TOUGHNESS, 50.0);
    }

    public static double healthForRealm(Realm r) {
        if (r == null) return 1000.0;
        return switch (r) {
            case QI_REFINING -> 1000.0;
            case FOUNDATION_BUILDING -> 5000.0;
            case GOLDEN_CORE -> 25000.0;
            case NASCENT_SOUL -> 100000.0;
            case SOUL_FORMATION -> 500000.0;
            case BODY_INTEGRATION -> 2000000.0;
            case MAHAYANA -> 10000000.0;
            case LOOSE_IMMORTAL -> 50000000.0;
            case TRUE_IMMORTAL -> 100000000.0;
            default -> 1000.0;
        };
    }

    public static Realm realmForKills(int kills) {
        if (kills <= 0) return Realm.QI_REFINING;
        if (kills < 3) return Realm.FOUNDATION_BUILDING;
        if (kills < 8) return Realm.GOLDEN_CORE;
        if (kills < 20) return Realm.NASCENT_SOUL;
        if (kills < 50) return Realm.SOUL_FORMATION;
        if (kills < 100) return Realm.BODY_INTEGRATION;
        if (kills < 200) return Realm.MAHAYANA;
        if (kills < 500) return Realm.LOOSE_IMMORTAL;
        return Realm.TRUE_IMMORTAL;
    }

    public Realm getRealm() { return this.realm; }

    public void setRealm(Realm r) {
        this.realm = r == null ? Realm.QI_REFINING : r;
        double hp = healthForRealm(this.realm);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
            this.setHealth((float) hp);
        }
    }

    public void assignTargetSoul(LivingEntity soul) {
        if (soul == null) return;
        this.targetSoulUuid = soul.getUUID();
        this.entityData.set(DATA_TARGET_ID, soul.getId());
    }

    public int getTargetId() { return this.entityData.get(DATA_TARGET_ID); }
    public boolean isChaining() { return this.entityData.get(DATA_CHAINING); }

    public boolean isAssignedTo(Entity entity) {
        return entity != null && this.targetSoulUuid != null && this.targetSoulUuid.equals(entity.getUUID());
    }

    @Override
    public boolean isPushable() { return false; }

    @Override
    public boolean shouldShowName() { return true; }

    @Override
    public boolean canBeLeashed(Player player) { return false; }

    @Override
    public boolean hurt(@NotNull DamageSource source, float amount) { return false; }

    @Override
    public void die(@NotNull DamageSource source) { /* 不会死亡 */ }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide) {
            if (this.isChaining()) {
                this.level().addParticle(ParticleTypes.SOUL, this.getX() + (this.random.nextDouble() - 0.5) * 0.6, this.getY() + this.random.nextDouble() * 2.0, this.getZ() + (this.random.nextDouble() - 0.5) * 0.6, 0.0, 0.05, 0.0);
            }
            return;
        }
        this.lifeTicks++;
        if (this.lifeTicks > MAX_LIFE_TICKS) { this.discard(); return; }
        LivingEntity target = resolveTargetSoul();
        if (target == null) { this.discard(); return; }
        Vec3 targetPos = target.position();
        Vec3 myPos = this.position();
        double dist = myPos.distanceTo(targetPos);
        if (dist > CHAIN_DISTANCE) {
            Vec3 dir = targetPos.subtract(myPos).normalize();
            this.setDeltaMovement(dir.scale(MOVE_SPEED));
            this.faceToward(targetPos);
        } else {
            if (!this.isChaining()) this.entityData.set(DATA_CHAINING, true);
            this.escortTicks++;
            if (this.escortTicks % 20 == 0 && target instanceof ServerPlayer sp) {
                Vec3 pullDir = myPos.subtract(targetPos).normalize();
                target.setDeltaMovement(pullDir.scale(0.3));
                target.hurtMarked = true;
            }
            if (this.escortTicks > 200) {
                this.teleportTargetToDifu(target);
                this.discard();
                return;
            }
        }
        if (this.tickCount % 10 == 0 && this.level() instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.SOUL_FIRE_FLAME, this.getX(), this.getY() + 1.0, this.getZ(), 2, 0.3, 0.5, 0.3, 0.0);
        }
    }

    private LivingEntity resolveTargetSoul() {
        if (this.targetSoulUuid == null) return null;
        if (!(this.level() instanceof ServerLevel sl)) return null;
        Entity entity = sl.getEntity(this.targetSoulUuid);
        if (entity instanceof LivingEntity le && le.isAlive()) return le;
        Entity byId = sl.getEntity(this.getTargetId());
        if (byId instanceof LivingEntity le2 && le2.isAlive()) { this.targetSoulUuid = le2.getUUID(); return le2; }
        return null;
    }

    private void faceToward(Vec3 target) {
        Vec3 dir = target.subtract(this.position());
        double yaw = Math.toDegrees(Math.atan2(-dir.x, dir.z));
        this.setYRot((float) yaw);
        this.yBodyRot = (float) yaw;
    }

    private void teleportTargetToDifu(LivingEntity target) {
        if (!(target instanceof ServerPlayer sp)) return;
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic == null) return;
        ServerLevel difuLevel = sp.server.getLevel(ModDimensions.DIFU);
        if (difuLevel != null) {
            sp.teleportTo(difuLevel, sp.getX(), 64, sp.getZ(), sp.getYRot(), sp.getXRot());
            sp.level().playSound(null, sp.getX(), sp.getY(), sp.getZ(), SoundEvents.PORTAL_TRIGGER, SoundSource.PLAYERS, 1.0f, 0.5f);
            sp.displayClientMessage(net.minecraft.network.chat.Component.translatable("message.friday_cultivation.soul_reaper.escorted_to_difu"), true);
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_CHAINING, false);
    }

    @Override
    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("targetSoulUuid")) this.targetSoulUuid = tag.getUUID("targetSoulUuid");
        if (tag.contains("realm")) this.realm = Realm.valueOf(tag.getString("realm"));
        if (tag.contains("escortTicks")) this.escortTicks = tag.getInt("escortTicks");
        if (tag.contains("lifeTicks")) this.lifeTicks = tag.getInt("lifeTicks");
        this.setRealm(this.realm);
    }

    @Override
    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetSoulUuid != null) tag.putUUID("targetSoulUuid", this.targetSoulUuid);
        tag.putString("realm", this.realm.name());
        tag.putInt("escortTicks", this.escortTicks);
        tag.putInt("lifeTicks", this.lifeTicks);
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
