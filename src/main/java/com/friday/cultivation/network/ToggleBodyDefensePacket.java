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

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ToggleBodyDefensePacket {
    private final boolean enable;

    public ToggleBodyDefensePacket(boolean enable) {
        this.enable = enable;
    }

    public static void encode(ToggleBodyDefensePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enable);
    }

    public static ToggleBodyDefensePacket decode(FriendlyByteBuf buf) {
        return new ToggleBodyDefensePacket(buf.readBoolean());
    }

    public static void handle(ToggleBodyDefensePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (data.isBodyDefenseEnabled() == msg.enable) {
                    return;
                }
                data.setBonusCategoryEnabled(CultivationBonusCategory.BODY_DEFENSE, msg.enable);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)(msg.enable ? "message.friday_cultivation.body_defense.enabled" : "message.friday_cultivation.body_defense.disabled")), false);
            });
        });
        ctx.setPacketHandled(true);
    }
}

