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

import com.friday.cultivation.client.SoulHookProgressHud;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoulHookProgressPacket {
    private final boolean active;
    private final int remainingTicks;
    private final int totalTicks;
    private final boolean locked;

    public SoulHookProgressPacket(boolean active, int remainingTicks, int totalTicks, boolean locked) {
        this.active = active;
        this.remainingTicks = remainingTicks;
        this.totalTicks = totalTicks;
        this.locked = locked;
    }

    public static void encode(SoulHookProgressPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.remainingTicks);
        buf.writeVarInt(msg.totalTicks);
        buf.writeBoolean(msg.locked);
    }

    public static SoulHookProgressPacket decode(FriendlyByteBuf buf) {
        return new SoulHookProgressPacket(buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean());
    }

    public static void handle(SoulHookProgressPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> SoulHookProgressHud.onSync(msg.active, msg.remainingTicks, msg.totalTicks, msg.locked)));
        ctx.setPacketHandled(true);
    }
}

