/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.SpiritLockVisualHandler;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
        return new SpiritLockVisualPacket(true, locked, 0, pos.east(), durationTicks);
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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> SpiritLockVisualHandler.onVisualSync(msg.blockTarget, msg.locked, msg.entityId, msg.blockPos, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}

