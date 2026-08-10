/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SetCultivationNamePacket {
    private static final int MAX_LEN = 16;
    private final String name;

    public SetCultivationNamePacket(String name) {
        this.name = name;
    }

    public static void encode(SetCultivationNamePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 64);
    }

    public static SetCultivationNamePacket decode(FriendlyByteBuf buf) {
        return new SetCultivationNamePacket(buf.readUtf(64));
    }

    public static void handle(SetCultivationNamePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            String s = msg.name == null ? "" : msg.name;
            if ((s = s.replaceAll("[\\u00a7\\u0000-\\u001f]", "").trim()).length() > 16) {
                s = s.substring(0, 16);
            }
            String fin = s;
            CultivationCapability.get((Player)player).ifPresent(data -> {
                data.setCustomName(fin);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.name.set", (Object[])new Object[]{fin.isEmpty() ? player.getGameProfile().getName() : fin}), true);
            });
        });
        ctx.setPacketHandled(true);
    }
}

