package com.friday.cultivation.entity;

import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.registry.ModEntities;
import java.util.UUID;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

public class IceShellEntity extends Entity {
    private static final int FREEZE_TICKS = 100;
    private static final int LINGER_TICKS = 100;
    private int age = 0;
    private UUID targetUuid;

    public IceShellEntity(EntityType<? extends IceShellEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public IceShellEntity(ServerLevel level, LivingEntity target) {
        this(ModEntities.ICE_SHELL.get(), level);
        this.targetUuid = target.getUUID();
        this.setPos(target.getX(), target.getY(), target.getZ());
        this.applyFreezeEffects(target);
        level.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.4f, 0.6f);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    public void tick() {
        if (TimeStasisHandler.pauseEntityTickInStoppedTime(this)) {
            return;
        }
        super.tick();
        ++this.age;
        if (this.age > LINGER_TICKS) {
            this.discard();
            return;
        }
        LivingEntity target = this.findTarget();
        if (target == null || !target.isAlive()) {
            this.discard();
            return;
        }
        this.setPos(target.getX(), target.getY(), target.getZ());
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel) level;
            this.applyFreezeEffects(target);
            if (this.age % 5 == 0) {
                this.spawnFreezeParticles(server, target);
            }
            if (this.age == FREEZE_TICKS - 1) {
                this.releaseTarget(target);
            }
        } else if (this.age % 3 == 0) {
            this.level().addParticle(ParticleTypes.SNOWFLAKE,
                    target.getX() + (this.random.nextDouble() - 0.5),
                    target.getY() + this.random.nextDouble() * target.getBbHeight(),
                    target.getZ() + (this.random.nextDouble() - 0.5),
                    0.0, 0.05, 0.0);
        }
    }

    private void applyFreezeEffects(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 5, 6, false, false));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 5, 0, false, false));
        target.setDeltaMovement(0.0, target.getDeltaMovement().y * 0.1, 0.0);
        target.hurtMarked = true;
    }

    private void releaseTarget(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1));
        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0));
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel) level;
            server.playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.GLASS_BREAK, SoundSource.PLAYERS, 0.5f, 1.4f);
            for (int i = 0; i < 30; ++i) {
                double dx = (this.random.nextDouble() - 0.5) * 1.5;
                double dy = this.random.nextDouble() * target.getBbHeight();
                double dz = (this.random.nextDouble() - 0.5) * 1.5;
                server.sendParticles(ParticleTypes.SNOWFLAKE,
                        target.getX() + dx, target.getY() + dy, target.getZ() + dz,
                        1, dx * 0.2, dy * 0.05, dz * 0.2, 0.1);
            }
        }
    }

    private void spawnFreezeParticles(ServerLevel server, LivingEntity target) {
        for (int i = 0; i < 6; ++i) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double y = this.random.nextDouble() * target.getBbHeight();
            double r = target.getBbWidth() * 0.7;
            double px = target.getX() + Math.cos(angle) * r;
            double pz = target.getZ() + Math.sin(angle) * r;
            server.sendParticles(ParticleTypes.SNOWFLAKE,
                    px, target.getY() + y, pz,
                    1, 0.0, 0.02, 0.0, 0.05);
        }
    }

    private LivingEntity findTarget() {
        if (this.targetUuid == null) {
            return null;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel server = (ServerLevel) level;
            Entity e = server.getEntity(this.targetUuid);
            return e instanceof LivingEntity ? (LivingEntity) e : null;
        }
        for (Entity e : this.level().getEntities(this, this.getBoundingBox().inflate(2.0))) {
            if (!e.getUUID().equals(this.targetUuid) || !(e instanceof LivingEntity)) continue;
            return (LivingEntity) e;
        }
        return null;
    }

    public UUID targetUuid() {
        return this.targetUuid;
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        this.age = tag.getInt("age");
        if (tag.hasUUID("target")) {
            this.targetUuid = tag.getUUID("target");
        }
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putInt("age", this.age);
        if (this.targetUuid != null) {
            tag.putUUID("target", this.targetUuid);
        }
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    protected AABB makeBoundingBox() {
        return new AABB(
                this.getX() - 1.5, this.getY() - 0.5, this.getZ() - 1.5,
                this.getX() + 1.5, this.getY() + 2.5, this.getZ() + 1.5
        );
    }
}
