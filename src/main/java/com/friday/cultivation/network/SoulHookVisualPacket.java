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

import com.friday.cultivation.client.SoulHookVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class SoulHookVisualPacket {
    private final int casterId;
    private final int targetId;
    private final int durationTicks;
    private final int chainCount;
    private final boolean vortexPhase;
    private final boolean active;
    private final int elapsedTicks;
    private final boolean hasVortexAnchor;
    private final double vortexX;
    private final double vortexY;
    private final double vortexZ;

    public SoulHookVisualPacket(int casterId, int targetId, int durationTicks) {
        this(casterId, targetId, durationTicks, 1, false, true, 0);
    }

    public SoulHookVisualPacket(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active) {
        this(casterId, targetId, durationTicks, chainCount, vortexPhase, active, 0);
    }

    public SoulHookVisualPacket(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active, int elapsedTicks) {
        this(casterId, targetId, durationTicks, chainCount, vortexPhase, active, elapsedTicks, false, 0.0, 0.0, 0.0);
    }

    public SoulHookVisualPacket(int casterId, int targetId, int durationTicks, int chainCount, boolean vortexPhase, boolean active, int elapsedTicks, boolean hasVortexAnchor, double vortexX, double vortexY, double vortexZ) {
        this.casterId = casterId;
        this.targetId = targetId;
        this.durationTicks = durationTicks;
        this.chainCount = chainCount;
        this.vortexPhase = vortexPhase;
        this.active = active;
        this.elapsedTicks = elapsedTicks;
        this.hasVortexAnchor = hasVortexAnchor;
        this.vortexX = vortexX;
        this.vortexY = vortexY;
        this.vortexZ = vortexZ;
    }

    public static void encode(SoulHookVisualPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.casterId);
        buf.writeVarInt(msg.targetId);
        buf.writeVarInt(msg.durationTicks);
        buf.writeVarInt(msg.chainCount);
        buf.writeBoolean(msg.vortexPhase);
        buf.writeBoolean(msg.active);
        buf.writeVarInt(msg.elapsedTicks);
        buf.writeBoolean(msg.hasVortexAnchor);
        if (msg.hasVortexAnchor) {
            buf.writeDouble(msg.vortexX);
            buf.writeDouble(msg.vortexY);
            buf.writeDouble(msg.vortexZ);
        }
    }

    public static SoulHookVisualPacket decode(FriendlyByteBuf buf) {
        int casterId = buf.readVarInt();
        int targetId = buf.readVarInt();
        int durationTicks = buf.readVarInt();
        int chainCount = buf.readVarInt();
        boolean vortexPhase = buf.readBoolean();
        boolean active = buf.readBoolean();
        int elapsedTicks = buf.readVarInt();
        boolean hasVortexAnchor = buf.readBoolean();
        double vortexX = 0.0;
        double vortexY = 0.0;
        double vortexZ = 0.0;
        if (hasVortexAnchor) {
            vortexX = buf.readDouble();
            vortexY = buf.readDouble();
            vortexZ = buf.readDouble();
        }
        return new SoulHookVisualPacket(casterId, targetId, durationTicks, chainCount, vortexPhase, active, elapsedTicks, hasVortexAnchor, vortexX, vortexY, vortexZ);
    }

    public static void handle(SoulHookVisualPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> SoulHookVisualHandler.onSync(msg.casterId, msg.targetId, msg.durationTicks, msg.chainCount, msg.vortexPhase, msg.active, msg.elapsedTicks, msg.hasVortexAnchor, msg.vortexX, msg.vortexY, msg.vortexZ)));
        ctx.setPacketHandled(true);
    }
}

