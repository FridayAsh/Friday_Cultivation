/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class SetSpellTerrainDestructionPacket {
    private final boolean enabled;

    public SetSpellTerrainDestructionPacket(boolean enabled) {
        this.enabled = enabled;
    }

    public static void encode(SetSpellTerrainDestructionPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.enabled);
    }

    public static SetSpellTerrainDestructionPacket decode(FriendlyByteBuf buf) {
        return new SetSpellTerrainDestructionPacket(buf.readBoolean());
    }

    public static void handle(SetSpellTerrainDestructionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                CapabilityEvents.applySpellTerrainRuleSnapshot(data);
                if (ModCommonConfig.spellTerrainDestructionForceDisabled()) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.spell_terrain.locked").withStyle(ChatFormatting.RED), true);
                    CapabilityEvents.syncToClient(player);
                    return;
                }
                data.setSpellTerrainDestructionEnabled(msg.enabled);
                if (player.server.isSingleplayer()) {
                    ModCommonConfig.setSpellTerrainDestructionDefaultEnabled(msg.enabled);
                }
                player.displayClientMessage((Component)Component.translatable((String)(msg.enabled ? "message.friday_cultivation.spell_terrain.enabled" : "message.friday_cultivation.spell_terrain.disabled")).withStyle(msg.enabled ? ChatFormatting.GREEN : ChatFormatting.GOLD), true);
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}

