/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 */
package com.friday.cultivation.cultivation.draw;

import net.minecraft.network.FriendlyByteBuf;

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

