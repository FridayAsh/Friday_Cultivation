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

import com.friday.cultivation.client.DeathSequenceClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class DeathSequencePacket {
    private final int titleTicks;
    private final int countdownTicks;

    public DeathSequencePacket(int titleTicks, int countdownTicks) {
        this.titleTicks = titleTicks;
        this.countdownTicks = countdownTicks;
    }

    public static void encode(DeathSequencePacket m, FriendlyByteBuf b) {
        b.writeVarInt(m.titleTicks);
        b.writeVarInt(m.countdownTicks);
    }

    public static DeathSequencePacket decode(FriendlyByteBuf b) {
        return new DeathSequencePacket(b.readVarInt(), b.readVarInt());
    }

    public static void handle(DeathSequencePacket m, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> DeathSequenceClientEffects.start(m.titleTicks, m.countdownTicks)));
        ctx.setPacketHandled(true);
    }
}

