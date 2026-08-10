/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.event.ReincarnationManager;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class RequestReincarnationScreenPacket {
    public static void encode(RequestReincarnationScreenPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestReincarnationScreenPacket decode(FriendlyByteBuf buf) {
        return new RequestReincarnationScreenPacket();
    }

    public static void handle(RequestReincarnationScreenPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData data = CultivationCapability.get((Player)player).orElse(null);
            if (data == null) {
                return;
            }
            if (!data.isSoulState() || !data.isReincarnationReady()) {
                return;
            }
            ReincarnationManager.prompt(player);
        });
        ctx.setPacketHandled(true);
    }
}

