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

import com.friday.cultivation.client.DharmaBodyClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DharmaBodyManifestationPacket {
    private final boolean active;
    private final int entityId;
    private final int durationTicks;

    public DharmaBodyManifestationPacket(boolean active, int entityId, int durationTicks) {
        this.active = active;
        this.entityId = entityId;
        this.durationTicks = durationTicks;
    }

    public static void encode(DharmaBodyManifestationPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
    }

    public static DharmaBodyManifestationPacket decode(FriendlyByteBuf buf) {
        return new DharmaBodyManifestationPacket(buf.readBoolean(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(DharmaBodyManifestationPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> DharmaBodyClientEffects.onSync(msg.active, msg.entityId, msg.durationTicks)));
        ctx.setPacketHandled(true);
    }
}

