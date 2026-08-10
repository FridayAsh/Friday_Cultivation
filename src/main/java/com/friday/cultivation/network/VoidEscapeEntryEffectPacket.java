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

import com.friday.cultivation.client.VoidEscapeClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class VoidEscapeEntryEffectPacket {
    private final int entityId;

    public VoidEscapeEntryEffectPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(VoidEscapeEntryEffectPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
    }

    public static VoidEscapeEntryEffectPacket decode(FriendlyByteBuf buf) {
        return new VoidEscapeEntryEffectPacket(buf.readVarInt());
    }

    public static void handle(VoidEscapeEntryEffectPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> VoidEscapeClientEffects.startEntryEffect(msg.entityId)));
        ctx.setPacketHandled(true);
    }
}

