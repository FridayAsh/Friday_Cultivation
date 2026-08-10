/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.event.IdentityDrawHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class ChooseOriginPacket {
    private final boolean random;
    private final String identityId;
    private final String spiritRootId;
    private final String physiqueId;
    private final boolean reconfigureMode;

    public ChooseOriginPacket(boolean random, String identityId, String spiritRootId, String physiqueId) {
        this(random, identityId, spiritRootId, physiqueId, false);
    }

    public ChooseOriginPacket(boolean random, String identityId, String spiritRootId, String physiqueId, boolean reconfigureMode) {
        this.random = random;
        this.identityId = identityId == null ? "" : identityId;
        this.spiritRootId = spiritRootId == null ? "" : spiritRootId;
        this.physiqueId = physiqueId == null ? "" : physiqueId;
        this.reconfigureMode = reconfigureMode;
    }

    public static void encode(ChooseOriginPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.random);
        buf.writeUtf(msg.identityId);
        buf.writeUtf(msg.spiritRootId);
        buf.writeUtf(msg.physiqueId);
        buf.writeBoolean(msg.reconfigureMode);
    }

    public static ChooseOriginPacket decode(FriendlyByteBuf buf) {
        return new ChooseOriginPacket(buf.readBoolean(), buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readBoolean());
    }

    public static void handle(ChooseOriginPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            IdentityDrawHandler.handleChooseOrigin(player, msg.random, msg.identityId, msg.spiritRootId, msg.physiqueId, msg.reconfigureMode);
        });
        ctx.setPacketHandled(true);
    }
}

