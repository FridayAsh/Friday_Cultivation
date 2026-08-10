package com.friday.cultivation.identity.draw;

import net.minecraft.network.FriendlyByteBuf;

/**
 * 身份抽卡 — 单张卡牌（身份+灵根组合）
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.draw.DrawCard
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
