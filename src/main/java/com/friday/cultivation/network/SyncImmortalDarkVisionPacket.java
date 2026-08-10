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

import com.friday.cultivation.client.ImmortalNightVisionHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SyncImmortalDarkVisionPacket {
    private final boolean enabled;

    public SyncImmortalDarkVisionPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(SyncImmortalDarkVisionPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
    }

    public static SyncImmortalDarkVisionPacket decode(FriendlyByteBuf buf) {
        return new SyncImmortalDarkVisionPacket(buf.readBoolean());
    }

    public static void handle(SyncImmortalDarkVisionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> ImmortalNightVisionHandler.setServerDarkVision(msg.enabled)));
        ctx.setPacketHandled(true);
    }
}

