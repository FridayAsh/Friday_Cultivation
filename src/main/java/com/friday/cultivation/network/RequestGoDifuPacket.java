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
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.DeathChoicePacket;
import com.friday.cultivation.registry.ModDimensions;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class RequestGoDifuPacket {
    public static void encode(RequestGoDifuPacket msg, FriendlyByteBuf buf) {
    }

    public static RequestGoDifuPacket decode(FriendlyByteBuf buf) {
        return new RequestGoDifuPacket();
    }

    public static void handle(RequestGoDifuPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData data = CultivationCapability.get((Player)player).orElse(null);
            if (data == null || !data.isSoulState()) {
                return;
            }
            if (player.level().dimension() == ModDimensions.DIFU) {
                return;
            }
            if (data.isSoulDeathChoicePending()) {
                SoulStateHandler.resolveDeathChoice(player, DeathChoicePacket.Choice.GO_DIFU);
                return;
            }
            SoulStateHandler.beginVoluntaryDifuTransfer(player);
        });
        ctx.setPacketHandled(true);
    }
}

