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

import com.friday.cultivation.client.TimeStasisClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class TimeStasisTargetPacket {
    private final int entityId;
    private final int durationTicks;
    private final boolean frozen;

    public TimeStasisTargetPacket(int entityId, int durationTicks, boolean frozen) {
        this.entityId = entityId;
        this.durationTicks = durationTicks;
        this.frozen = frozen;
    }

    public static void encode(TimeStasisTargetPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeBoolean(msg.frozen);
    }

    public static TimeStasisTargetPacket decode(FriendlyByteBuf buf) {
        return new TimeStasisTargetPacket(buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(TimeStasisTargetPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> TimeStasisClientEffects.onTargetStasis(msg.entityId, msg.durationTicks, msg.frozen)));
        ctx.setPacketHandled(true);
    }
}

