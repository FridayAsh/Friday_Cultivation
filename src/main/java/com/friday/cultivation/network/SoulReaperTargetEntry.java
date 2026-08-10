/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 */
package com.friday.cultivation.network;

import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;

public record SoulReaperTargetEntry(UUID targetId, boolean playerTarget, Component name, Component gender, Component identity, Component realm, Component location) {
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
        return new SoulReaperTargetEntry(buf.readUUID(), buf.readBoolean(), buf.readComponent(), buf.readComponent(), buf.readComponent(), buf.readComponent(), buf.readComponent());
    }
}

