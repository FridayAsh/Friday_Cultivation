package com.friday.cultivation.network;

import com.friday.cultivation.client.QiShieldVisualHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 灵气护盾受击同步包（服务端→客户端）— 严格复刻自原模组
 * com.xiaoxiang.cultivation.network.QiShieldHitPacket
 * <p>
 * 字段：playerId（受击玩家实体ID）+ dirX/Y/Z（攻击方向向量）+ intensity（强度）
 */
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
        buf.writeInt(msg.playerId);
        buf.writeFloat(msg.dirX);
        buf.writeFloat(msg.dirY);
        buf.writeFloat(msg.dirZ);
        buf.writeFloat(msg.intensity);
    }

    public static QiShieldHitPacket decode(FriendlyByteBuf buf) {
        return new QiShieldHitPacket(buf.readInt(), buf.readFloat(), buf.readFloat(), buf.readFloat(), buf.readFloat());
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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> QiShieldVisualHandler.onShieldHit(msg)));
        ctx.setPacketHandled(true);
    }
}
