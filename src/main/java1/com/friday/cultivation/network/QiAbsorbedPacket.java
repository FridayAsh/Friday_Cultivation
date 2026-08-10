package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientCultivationHooks;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 灵气被吸收反馈包（服务端→客户端）— 严格复刻自原 mod
 * com.xiaoxiang.cultivation.network.QiAbsorbedPacket
 * <p>
 * 字段：x/y/z + elementOrdinal（QiElement 索引，0=PURE..7=LIGHTNING）
 */
public class QiAbsorbedPacket {
    private final double x;
    private final double y;
    private final double z;
    private final int elementOrdinal;

    public QiAbsorbedPacket(double x, double y, double z, int elementOrdinal) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.elementOrdinal = elementOrdinal;
    }

    public static void encode(QiAbsorbedPacket msg, FriendlyByteBuf buf) {
        buf.writeDouble(msg.x);
        buf.writeDouble(msg.y);
        buf.writeDouble(msg.z);
        buf.writeVarInt(msg.elementOrdinal);
    }

    public static QiAbsorbedPacket decode(FriendlyByteBuf buf) {
        return new QiAbsorbedPacket(buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readVarInt());
    }

    public static void handle(QiAbsorbedPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ClientCultivationHooks.onQiAbsorbed(msg.x, msg.y, msg.z, msg.elementOrdinal)));
        ctx.setPacketHandled(true);
    }
}
