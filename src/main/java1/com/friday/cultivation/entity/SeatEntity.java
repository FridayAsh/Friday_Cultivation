package com.friday.cultivation.entity;

import com.friday.cultivation.block.CushionBlock;
import com.friday.cultivation.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;

/**
 * 坐垫实体 - 供玩家骑乘的坐垫，检测下方坐垫方块。
 * 严格 1:1 复刻原 mod: com.xiaoxiang.cultivation.entity.SeatEntity
 */
public class SeatEntity extends Entity {
    private static final double SEAT_Y_OFFSET = -0.5;

    public SeatEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    public SeatEntity(Level level, BlockPos pos) {
        this(ModEntities.SEAT.get(), level);
        this.setPos((double) pos.getX() + 0.5, (double) pos.getY() + -0.5, (double) pos.getZ() + 0.5);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide()) {
            return;
        }
        if (this.getPassengers().isEmpty()) {
            this.discard();
            return;
        }
        BlockPos cushionPos = BlockPos.containing((double) this.getX(), (double) (this.getY() - -0.5), (double) this.getZ());
        if (!(this.level().getBlockState(cushionPos).getBlock() instanceof CushionBlock)) {
            this.ejectPassengers();
            this.discard();
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.0;
    }

    @Override
    protected void positionRider(@NotNull Entity passenger, @NotNull Entity.MoveFunction callback) {
        callback.accept(passenger, this.getX(), this.getY() - -0.5, this.getZ());
    }

    @NotNull
    @Override
    public Vec3 getDismountLocationForPassenger(@NotNull LivingEntity passenger) {
        Vec3 base = super.getDismountLocationForPassenger(passenger);
        return new Vec3(base.x, this.getY() - -0.5 + 0.05, base.z);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
    }

    @NotNull
    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isControlledByLocalInstance() {
        return false;
    }

    public static boolean isSeat(Entity e) {
        return e instanceof SeatEntity;
    }
}
