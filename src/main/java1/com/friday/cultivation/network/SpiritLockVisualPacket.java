package com.friday.cultivation.network;

import com.friday.cultivation.client.SpiritLockVisualHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 锁灵阵视觉同步包（照搬原模组 SpiritLockVisualPacket）
 * 服务端 → 客户端：实体/方块锁定视觉状态。
 */
public class SpiritLockVisualPacket {
    private final boolean blockTarget;
    private final boolean locked;
    private final int entityId;
    private final BlockPos blockPos;
    private final int durationTicks;

    private SpiritLockVisualPacket(boolean blockTarget, boolean locked, int entityId, BlockPos blockPos, int durationTicks) {
        this.blockTarget = blockTarget;
        this.locked = locked;
        this.entityId = entityId;
        this.blockPos = blockPos;
        this.durationTicks = durationTicks;
    }

    public static SpiritLockVisualPacket entity(int entityId, int durationTicks, boolean locked) {
        return new SpiritLockVisualPacket(false, locked, entityId, BlockPos.ZERO, durationTicks);
    }

    public static SpiritLockVisualPacket block(BlockPos pos, int durationTicks, boolean locked) {
        return new SpiritLockVisualPacket(true, locked, 0, pos.immutable(), durationTicks);
    }

    public static void encode(SpiritLockVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.blockTarget);
        buf.writeBoolean(msg.locked);
        if (msg.blockTarget) {
            buf.writeBlockPos(msg.blockPos);
        } else {
            buf.writeVarInt(msg.entityId);
        }
        buf.writeVarInt(msg.durationTicks);
    }

    public static SpiritLockVisualPacket decode(FriendlyByteBuf buf) {
        boolean blockTarget = buf.readBoolean();
        boolean locked = buf.readBoolean();
        if (blockTarget) {
            return SpiritLockVisualPacket.block(buf.readBlockPos(), buf.readVarInt(), locked);
        }
        return SpiritLockVisualPacket.entity(buf.readVarInt(), buf.readVarInt(), locked);
    }

    public static void handle(SpiritLockVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> SpiritLockVisualHandler.onVisualSync(msg.blockTarget, msg.locked, msg.entityId, msg.blockPos, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}
