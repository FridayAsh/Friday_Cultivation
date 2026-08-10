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

import com.friday.cultivation.client.TribulationCloudClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class TribulationCloudPacket {
    private final int entityId;
    private final int durationTicks;

    public TribulationCloudPacket(int entityId, int durationTicks) {
        this.entityId = entityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(TribulationCloudPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
    }

    public static TribulationCloudPacket decode(FriendlyByteBuf buf) {
        return new TribulationCloudPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(TribulationCloudPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> TribulationCloudClientEffects.onCloud(msg.entityId, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}

