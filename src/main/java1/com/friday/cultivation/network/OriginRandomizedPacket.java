package com.friday.cultivation.network;

import com.friday.cultivation.client.OriginRandomStartAnimation;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 服务端→客户端: 随机结果通知，触发客户端动画
 * 复刻自原模组 com.xiaoxiang.cultivation.network.OriginRandomizedPacket
 */
public class OriginRandomizedPacket {
    private final String identityId;
    private final String spiritRootId;
    private final String physiqueId;
    private final boolean grantStarterItems;

    public OriginRandomizedPacket(String identityId, String spiritRootId, String physiqueId, boolean grantStarterItems) {
        this.identityId = identityId == null ? "" : identityId;
        this.spiritRootId = spiritRootId == null ? "" : spiritRootId;
        this.physiqueId = physiqueId == null ? "" : physiqueId;
        this.grantStarterItems = grantStarterItems;
    }

    public static void encode(OriginRandomizedPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.identityId);
        buf.writeUtf(msg.spiritRootId);
        buf.writeUtf(msg.physiqueId);
        buf.writeBoolean(msg.grantStarterItems);
    }

    public static OriginRandomizedPacket decode(FriendlyByteBuf buf) {
        return new OriginRandomizedPacket(
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean()
        );
    }

    public static void handle(OriginRandomizedPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> OriginRandomStartAnimation.start(
                msg.identityId, msg.spiritRootId, msg.physiqueId, msg.grantStarterItems));
        ctx.setPacketHandled(true);
    }
}
