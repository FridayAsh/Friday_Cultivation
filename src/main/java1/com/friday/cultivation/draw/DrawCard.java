package com.friday.cultivation.draw;

import net.minecraft.network.FriendlyByteBuf;

/**
 * 抽卡结果（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.draw.DrawCard）
 */
public record DrawCard(String identityId, String spiritRootId) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(this.identityId);
        buf.writeUtf(this.spiritRootId);
    }

    public static DrawCard decode(FriendlyByteBuf buf) {
        String id = buf.readUtf();
        String root = buf.readUtf();
        return new DrawCard(id, root);
    }
}