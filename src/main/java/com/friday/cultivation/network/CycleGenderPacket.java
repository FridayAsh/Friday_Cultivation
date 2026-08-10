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

public class CycleGenderPacket {
    public static void encode(CycleGenderPacket msg, FriendlyByteBuf buf) {
    }

    public static CycleGenderPacket decode(FriendlyByteBuf buf) {
        return new CycleGenderPacket();
    }

    public static void handle(CycleGenderPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (data.getGenderEditsLeft() <= 0) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.gender.no_edits"), true);
                    return;
                }
                data.setGender(data.getGender() == 2 ? 1 : 2);
                data.setGenderEditsLeft(data.getGenderEditsLeft() - 1);
                CapabilityEvents.syncToClient(player);
                String key = data.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male";
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.gender.set", (Object[])new Object[]{Component.translatable((String)key), data.getGenderEditsLeft()}), true);
            });
        });
        ctx.setPacketHandled(true);
    }
}

