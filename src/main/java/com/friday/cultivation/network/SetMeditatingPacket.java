/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivationDataPacket;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

public class SetMeditatingPacket {
    private final boolean meditating;

    public SetMeditatingPacket(boolean meditating) {
        this.meditating = meditating;
    }

    public static void encode(SetMeditatingPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.meditating);
    }

    public static SetMeditatingPacket decode(FriendlyByteBuf buf) {
        return new SetMeditatingPacket(buf.readBoolean());
    }

    public static void handle(SetMeditatingPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                boolean was = data.isMeditating();
                if (was == msg.meditating) {
                    return;
                }
                data.setMeditating(msg.meditating);
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new SyncCultivationDataPacket((CultivationData)data));
                String key = msg.meditating ? "message.friday_cultivation.meditation.start" : "message.friday_cultivation.meditation.stop";
                player.displayClientMessage((Component)Component.translatable((String)key), true);
            });
        });
        ctx.setPacketHandled(true);
    }
}

