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

import com.friday.cultivation.client.NascentSoulBodyVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class NascentSoulBodyPacket {
    private final boolean active;
    private final int playerId;
    private final double x;
    private final double y;
    private final double z;
    private final float yRot;
    private final float xRot;
    private final int durationTicks;

    public NascentSoulBodyPacket(boolean active, int playerId, double x, double y, double z, float yRot, float xRot, int durationTicks) {
        this.active = active;
        this.playerId = playerId;
        this.x = x;
        this.y = y;
        this.z = z;
        this.yRot = yRot;
        this.xRot = xRot;
        this.durationTicks = durationTicks;
    }

    public static void encode(NascentSoulBodyPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.playerId);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.yRot);
        buf.writeFloat(msg.xRot);
        buf.writeVarInt(msg.durationTicks);
    }

    public static NascentSoulBodyPacket decode(FriendlyByteBuf buf) {
        return new NascentSoulBodyPacket(buf.readBoolean(), buf.readVarInt(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readVarInt());
    }

    public static void handle(NascentSoulBodyPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> NascentSoulBodyVisualHandler.onBodySync(msg.active, msg.playerId, msg.x, msg.y, msg.z, msg.yRot, msg.xRot, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}

