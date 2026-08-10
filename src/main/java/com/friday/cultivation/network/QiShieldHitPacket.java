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

import com.friday.cultivation.client.QiShieldVisualHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

public class QiShieldHitPacket {
    private final int playerId;
    private final float dirX;
    private final float dirY;
    private final float dirZ;
    private final float intensity;

    public QiShieldHitPacket(int playerId, float dirX, float dirY, float dirZ, float intensity) {
        this.playerId = playerId;
        this.dirX = dirX;
        this.dirY = dirY;
        this.dirZ = dirZ;
        this.intensity = intensity;
    }

    public static void encode(QiShieldHitPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.playerId);
        buf.writeFloat(msg.dirX);
        buf.writeFloat(msg.dirY);
        buf.writeFloat(msg.dirZ);
        buf.writeFloat(msg.intensity);
    }

    public static QiShieldHitPacket decode(FriendlyByteBuf buf) {
        return new QiShieldHitPacket(buf.readVarInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
    }

    public int playerId() {
        return this.playerId;
    }

    public float dirX() {
        return this.dirX;
    }

    public float dirY() {
        return this.dirY;
    }

    public float dirZ() {
        return this.dirZ;
    }

    public float intensity() {
        return this.intensity;
    }

    public static void handle(QiShieldHitPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn((Dist)Dist.CLIENT, () -> () -> QiShieldVisualHandler.onShieldHit(msg)));
        ctx.setPacketHandled(true);
    }
}

