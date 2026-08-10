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

import com.friday.cultivation.client.BuddhaFireLotusClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class BuddhaFireLotusFlashPacket {
    private final double x;
    private final double y;
    private final double z;
    private final double radius;
    private final int durationTicks;
    private final int chargedQi;
    private final int rootFlags;

    public BuddhaFireLotusFlashPacket(double x, double y, double z, double radius, int durationTicks, int chargedQi, int rootFlags) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.durationTicks = durationTicks;
        this.chargedQi = chargedQi;
        this.rootFlags = rootFlags;
    }

    public static void encode(BuddhaFireLotusFlashPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeDouble(msg.radius);
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.chargedQi);
        buf.writeVarInt(msg.rootFlags);
    }

    public static BuddhaFireLotusFlashPacket decode(FriendlyByteBuf buf) {
        return new BuddhaFireLotusFlashPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(BuddhaFireLotusFlashPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> BuddhaFireLotusClientEffects.onExplosion(msg.x, msg.y, msg.z, msg.radius, msg.durationTicks, msg.chargedQi, msg.rootFlags)));
        ctx.setPacketHandled(true);
    }
}

