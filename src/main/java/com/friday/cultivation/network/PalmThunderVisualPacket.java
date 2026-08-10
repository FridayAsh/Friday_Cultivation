/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.fml.DistExecutor
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.PalmThunderVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class PalmThunderVisualPacket {
    public static final int MODE_CHANNEL = 0;
    public static final int MODE_BURST = 1;
    private final int mode;
    private final int entityId;
    private final int durationTicks;
    private final boolean active;
    private final double x;
    private final double y;
    private final double z;
    private final float radius;
    private final float progress;
    private final boolean armed;

    private PalmThunderVisualPacket(int mode, int entityId, int durationTicks, boolean active, double x, double y, double z, float radius, float progress, boolean armed) {
        this.mode = mode;
        this.entityId = entityId;
        this.durationTicks = durationTicks;
        this.active = active;
        this.x = x;
        this.y = y;
        this.z = z;
        this.radius = radius;
        this.progress = progress;
        this.armed = armed;
    }

    public static PalmThunderVisualPacket channel(int casterId, int durationTicks, boolean active) {
        return PalmThunderVisualPacket.channel(casterId, durationTicks, active, active ? 1.0f : 0.0f, active);
    }

    public static PalmThunderVisualPacket channel(int casterId, int durationTicks, boolean active, float progress, boolean armed) {
        return new PalmThunderVisualPacket(0, casterId, durationTicks, active, 0.0, 0.0, 0.0, 0.0f, Math.max(0.0f, Math.min(1.0f, progress)), armed);
    }

    public static PalmThunderVisualPacket burst(Vec3 center, float radius, int durationTicks) {
        return new PalmThunderVisualPacket(1, 0, durationTicks, true, center.x, center.y, center.z, radius, 1.0f, false);
    }

    public static void encode(PalmThunderVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.mode);
        buf.writeVarInt(msg.entityId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeBoolean(msg.active);
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeFloat(msg.radius);
        buf.writeFloat(msg.progress);
        buf.writeBoolean(msg.armed);
    }

    public static PalmThunderVisualPacket decode(FriendlyByteBuf buf) {
        return new PalmThunderVisualPacket(buf.readVarInt(), buf.readVarInt(), buf.readVarInt(), buf.readBoolean(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readFloat(), buf.readFloat(), buf.readBoolean());
    }

    public static void handle(PalmThunderVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> PalmThunderVisualHandler.onSync(msg.mode, msg.entityId, msg.durationTicks, msg.active, msg.x, msg.y, msg.z, msg.radius, msg.progress, msg.armed)));
        ctx.setPacketHandled(true);
    }
}

