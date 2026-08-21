/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.registry.ModItems;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

public class SoulReaperEntity
extends PathfinderMob {
    private static final EntityDataAccessor<Integer> DATA_TARGET_ID = SynchedEntityData.defineId(SoulReaperEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_CHAINING = SynchedEntityData.defineId(SoulReaperEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final int CHAIN_DISTANCE = 3;
    private static final int MAX_LIFE_TICKS = 3600;
    private static final double MOVE_SPEED = 0.35;
    private Realm realm = Realm.QI_REFINING;
    private UUID targetSoulUuid;
    private int escortTicks = 0;
    private int lifeTicks = 0;

    public SoulReaperEntity(EntityType<? extends SoulReaperEntity> type, Level level) {
        super(type, level);
        this.setNoGravity(true);
        this.noPhysics = true;
        this.setPersistenceRequired();
        this.setSilent(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, SoulReaperEntity.healthForRealm(Realm.QI_REFINING)).add(Attributes.ATTACK_DAMAGE, 4.0).add(Attributes.MOVEMENT_SPEED, 0.3).add(Attributes.FOLLOW_RANGE, 64.0);
    }

    public static double healthForRealm(Realm r) {
        return r.baseHealthForNpc();
    }

    /** 勾魂使者境界随击杀数提升；上限为主链路的半帝（不生成大帝级勾魂使者） */
    public static Realm realmForKills(int kills) {
        int start = RealmTopology.progressionIndex(Realm.QI_REFINING);
        int end = RealmTopology.progressionIndex(Realm.HALF_EMPEROR);
        int idx = Math.min(end, start + Math.max(0, kills));
        Realm r = RealmTopology.mainChain().get(Math.max(0, idx));
        return r == Realm.BODY_TEMPERING ? Realm.QI_REFINING : r;
    }

    public Realm getRealm() {
        return this.realm;
    }

    public void setRealm(Realm r) {
        this.realm = r;
        double hp = SoulReaperEntity.healthForRealm(r);
        if (this.getAttribute(Attributes.MAX_HEALTH) != null) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(hp);
        }
        this.setHealth((float)hp);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_TARGET_ID, -1);
        this.entityData.define(DATA_CHAINING, false);
    }

    protected void registerGoals() {
    }

    public void assignTargetSoul(LivingEntity soul) {
        this.targetSoulUuid = soul.getUUID();
        this.entityData.set(DATA_TARGET_ID, soul.getId());
    }

    public int getTargetId() {
        return (Integer)this.entityData.get(DATA_TARGET_ID);
    }

    public boolean isChaining() {
        return (Boolean)this.entityData.get(DATA_CHAINING);
    }

    public boolean isAssignedTo(Entity entity) {
        return entity != null && entity.getUUID().equals(this.targetSoulUuid);
    }

    public boolean isPushable() {
        return false;
    }

    protected void doPush(@NotNull Entity entity) {
    }

    public boolean canScare(double distanceToClosestPlayer) {
        return false;
    }

    public boolean isPersistenceRequired() {
        return true;
    }

    public boolean byFraction(@NotNull DamageSource source, float amount) {
        ServerPlayer sp;
        boolean bySoul;
        Entity attacker = source.getEntity();
        boolean bl = bySoul = attacker instanceof ServerPlayer && CultivationCapability.get((Player)(sp = (ServerPlayer)attacker)).map(CultivationData::isSoulState).orElse(false) != false;
        if (!bySoul && !this.level().isClientSide) {
            return false;
        }
        return super.hurt(source, amount);
    }

    public void die(@NotNull DamageSource source) {
        Player soul;
        if (!this.level().isClientSide && this.targetSoulUuid != null && (soul = this.level().getPlayerByUUID(this.targetSoulUuid)) instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)soul;
            CultivationCapability.get((Player)sp).ifPresent(data -> {
                if (data.isSoulState()) {
                    data.setSoulReaperKills(data.getSoulReaperKills() + 1);
                    data.setSoulReaperPursuitEnabled(true);
                    data.setNextReaperTick(data.getSoulTicks() + 24000);
                    CapabilityEvents.syncToClient(sp);
                    int minutes = SoulStateHandler.reaperWaitMinutes(24000);
                    int count = SoulStateHandler.upcomingReaperCount(data);
                    Realm nextRealm = SoulStateHandler.upcomingReaperRealm(data);
                    sp.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(10, 96, 24));
                    sp.connection.send((Packet)new ClientboundSetTitleTextPacket((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper.slain_title").withStyle(new ChatFormatting[]{ChatFormatting.DARK_RED, ChatFormatting.BOLD})));
                    sp.connection.send((Packet)new ClientboundSetSubtitleTextPacket((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper.slain_subtitle", (Object[])new Object[]{minutes, count, nextRealm.displayName()}).withStyle(ChatFormatting.GOLD)));
                    sp.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper.slain", (Object[])new Object[]{minutes, count, nextRealm.displayName()}).withStyle(ChatFormatting.DARK_RED));
                }
            });
            this.spawnAtLocation(new ItemStack((ItemLike)ModItems.SOUL_REAPER_TOKEN.get()));
        }
        super.die(source);
    }

    public void tick() {
        boolean soulValid;
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        ++this.lifeTicks;
        LivingEntity soulTarget = this.resolveTargetSoul();
        boolean bl = soulValid = soulTarget != null && SoulStateHandler.canSoulHookTarget((Entity)soulTarget) && soulTarget.level() == this.level();
        if (!soulValid || this.lifeTicks > 3600) {
            this.discard();
            return;
        }
        if (((Integer)this.entityData.get(DATA_TARGET_ID)).intValue() != soulTarget.getId()) {
            this.entityData.set(DATA_TARGET_ID, soulTarget.getId());
        }
        Vec3 myPos = this.position();
        Vec3 soulPos = soulTarget.position().add(0.0, (double)soulTarget.getBbHeight() * 0.5, 0.0);
        double dist = myPos.distanceTo(soulPos);
        boolean hookActive = SoulHookHandler.hasActive((LivingEntity)this, soulTarget);
        if (!hookActive && SoulHookHandler.hasActiveTarget((Entity)soulTarget)) {
            this.entityData.set(DATA_CHAINING, false);
            this.setDeltaMovement(Vec3.ZERO);
            this.faceToward(soulPos);
            return;
        }
        if (!hookActive && dist > 3.0) {
            this.entityData.set(DATA_CHAINING, false);
            Vec3 step = soulPos.multiply(myPos).normalize().scale(Math.min(0.35, dist));
            this.setPos(myPos.x + step.x, myPos.y + step.y, myPos.z + step.z);
            this.faceToward(soulPos);
        } else {
            this.entityData.set(DATA_CHAINING, true);
            this.faceToward(soulPos);
            if (!hookActive) {
                this.escortTicks = 0;
                SoulHookHandler.startEscort((LivingEntity)this, soulTarget);
            }
        }
    }

    private LivingEntity resolveTargetSoul() {
        ServerPlayer sp;
        ServerLevel sl;
        Entity entity;
        if (this.targetSoulUuid == null) {
            return null;
        }
        Level level = this.level();
        if (level instanceof ServerLevel && (entity = (sl = (ServerLevel)level).getEntity(this.targetSoulUuid)) instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)entity;
            return living;
        }
        Player player = this.level().getPlayerByUUID(this.targetSoulUuid);
        return player instanceof ServerPlayer ? (sp = (ServerPlayer)player) : null;
    }

    private void faceToward(Vec3 target) {
        Vec3 d = target.multiply(this.position());
        float yaw = (float)Math.toDegrees(Math.atan2(-d.x, d.z));
        this.setYRot(yaw);
        this.yBodyRot = yaw;
        this.yHeadRot = yaw;
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.targetSoulUuid != null) {
            tag.putUUID("targetSoul", this.targetSoulUuid);
        }
        tag.putInt("escortTicks", this.escortTicks);
        tag.putInt("lifeTicks", this.lifeTicks);
        tag.putString("realm", this.realm.id());
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("targetSoul")) {
            this.targetSoulUuid = tag.getUUID("targetSoul");
        }
        this.escortTicks = tag.getInt("escortTicks");
        this.lifeTicks = tag.getInt("lifeTicks");
        if (tag.contains("realm")) {
            this.realm = Realm.byId(tag.getString("realm"));
        }
    }
}

