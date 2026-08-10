package com.friday.cultivation.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

import java.util.UUID;

/**
 * 灵魂收割者目标条目（严格照搬原模组 com.xiaoxiang.cultivation.network.SoulReaperTargetEntry）。
 * <p>作为 {@link SoulReaperTargetsPacket} 的单一目标数据载体，包含 UUID / 玩家标记 / 5 个 Component。</p>
 */
public record SoulReaperTargetEntry(UUID targetId, boolean playerTarget, Component name,
                                    Component gender, Component identity, Component realm,
                                    Component location) {
    public void encode(FriendlyByteBuf buf) {
        buf.writeUUID(this.targetId);
        buf.writeBoolean(this.playerTarget);
        buf.writeComponent(this.name);
        buf.writeComponent(this.gender);
        buf.writeComponent(this.identity);
        buf.writeComponent(this.realm);
        buf.writeComponent(this.location);
    }

    public static SoulReaperTargetEntry decode(FriendlyByteBuf buf) {
        return new SoulReaperTargetEntry(
                buf.readUUID(),
                buf.readBoolean(),
                buf.readComponent(),
                buf.readComponent(),
                buf.readComponent(),
                buf.readComponent(),
                buf.readComponent());
    }
}
