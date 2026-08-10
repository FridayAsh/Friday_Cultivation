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

import com.friday.cultivation.client.RealmPressureClientEffects;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class RealmPressureVisualPacket {
    private static final int MODE_TARGET = 0;
    private static final int MODE_CASTER = 1;
    private static final int MODE_EXPANSION = 2;
    private final int mode;
    private final boolean active;
    private final int entityId;
    private final int casterId;
    private final int durationTicks;
    private final float radius;

    private RealmPressureVisualPacket(int mode, boolean active, int entityId, int casterId, int durationTicks, float radius) {
        this.mode = mode;
        this.active = active;
        this.entityId = entityId;
        this.casterId = casterId;
        this.durationTicks = durationTicks;
        this.radius = radius;
    }

    public static RealmPressureVisualPacket target(int entityId, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(0, active, entityId, 0, durationTicks, 0.0f);
    }

    public static RealmPressureVisualPacket caster(int entityId, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(1, active, entityId, 0, durationTicks, 0.0f);
    }

    public static RealmPressureVisualPacket expansion(int casterId, float radius, int durationTicks, boolean active) {
        return new RealmPressureVisualPacket(2, active, 0, casterId, durationTicks, radius);
    }

    public static void encode(RealmPressureVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mode);
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.casterId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeFloat(msg.radius);
    }

    public static RealmPressureVisualPacket decode(FriendlyByteBuf buf) {
        int mode = buf.readVarInt();
        boolean active = buf.readBoolean();
        int entityId = buf.readVarInt();
        int casterId = buf.readVarInt();
        int durationTicks = buf.readVarInt();
        float radius = buf.readFloat();
        return new RealmPressureVisualPacket(mode, active, entityId, casterId, durationTicks, radius);
    }

    public static void handle(RealmPressureVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> RealmPressureClientEffects.onSync(msg.mode, msg.active, msg.entityId, msg.casterId, msg.durationTicks, msg.radius)));
        ctx.setPacketHandled(true);
    }
}

