/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.LifeBalanceVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class LifeBalanceVisualPacket {
    private static final int MODE_MARK = 0;
    private static final int MODE_LINK = 1;
    private final int mode;
    private final int entityId;
    private final int casterId;
    private final int targetId;
    private final int durationTicks;
    private final boolean active;

    private LifeBalanceVisualPacket(int mode, int entityId, int casterId, int targetId, int durationTicks, boolean active) {
        this.mode = mode;
        this.entityId = entityId;
        this.casterId = casterId;
        this.targetId = targetId;
        this.durationTicks = durationTicks;
        this.active = active;
    }

    public static LifeBalanceVisualPacket mark(int entityId, int durationTicks, boolean active) {
        return new LifeBalanceVisualPacket(0, entityId, 0, 0, durationTicks, active);
    }

    public static LifeBalanceVisualPacket link(int casterId, int targetId, int durationTicks, boolean active) {
        return new LifeBalanceVisualPacket(1, 0, casterId, targetId, durationTicks, active);
    }

    public static void encode(LifeBalanceVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mode);
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.casterId);
        buf.writeVarInt(msg.targetId);
        buf.writeVarInt(msg.durationTicks);
    }

    public static LifeBalanceVisualPacket decode(FriendlyByteBuf buf) {
        int mode = buf.readVarInt();
        boolean active = buf.readBoolean();
        int entityId = buf.readVarInt();
        int casterId = buf.readVarInt();
        int targetId = buf.readVarInt();
        int durationTicks = buf.readVarInt();
        return new LifeBalanceVisualPacket(mode, entityId, casterId, targetId, durationTicks, active);
    }

    public static void handle(LifeBalanceVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            if (msg.mode == 1) {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> LifeBalanceVisualHandler.onLinkSync(msg.casterId, msg.targetId, msg.durationTicks, msg.active));
            } else {
                DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> LifeBalanceVisualHandler.onMarkSync(msg.entityId, msg.durationTicks, msg.active));
            }
        });
        ctx.setPacketHandled(true);
    }
}

