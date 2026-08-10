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

import com.friday.cultivation.client.ClientSoulRegistry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoulStatePacket {
    private final List<UUID> souls;

    public SoulStatePacket(Collection<UUID> souls) {
        this.souls = new ArrayList<UUID>(souls);
    }

    public static void encode(SoulStatePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.souls.size());
        for (UUID id : msg.souls) {
            buf.writeUUID(id);
        }
    }

    public static SoulStatePacket decode(FriendlyByteBuf buf) {
        int n = buf.readVarInt();
        ArrayList<UUID> list = new ArrayList<UUID>(n);
        for (int i = 0; i < n; ++i) {
            list.add(buf.readUUID());
        }
        return new SoulStatePacket(list);
    }

    public static void handle(SoulStatePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ClientSoulRegistry.replaceAll(msg.souls)));
        ctx.setPacketHandled(true);
    }
}

