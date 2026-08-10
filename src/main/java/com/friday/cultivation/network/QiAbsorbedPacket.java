/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientCultivationHooks;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

public class QiAbsorbedPacket {
    private final double x;
    private final double y;
    private final double z;
    private final int elementOrdinal;

    public QiAbsorbedPacket(double x, double y, double z, int elementOrdinal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.elementOrdinal = elementOrdinal;
    }

    public static void encode(QiAbsorbedPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeVarInt(msg.elementOrdinal);
    }

    public static QiAbsorbedPacket decode(FriendlyByteBuf buf) {
        return new QiAbsorbedPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt());
    }

    public static void handle(QiAbsorbedPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> ClientCultivationHooks.onQiAbsorbed(msg.x, msg.y, msg.z, msg.elementOrdinal));
        ctx.setPacketHandled(true);
    }
}

