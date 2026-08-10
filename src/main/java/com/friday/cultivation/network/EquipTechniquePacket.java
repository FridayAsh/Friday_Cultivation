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
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class EquipTechniquePacket {
    private final String techniqueId;

    public EquipTechniquePacket(String techniqueId) {
        this.techniqueId = techniqueId == null ? "" : techniqueId;
    }

    public static void encode(EquipTechniquePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.techniqueId, 64);
    }

    public static EquipTechniquePacket decode(FriendlyByteBuf buf) {
        return new EquipTechniquePacket(buf.readUtf(64));
    }

    public static void handle(EquipTechniquePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (msg.techniqueId.isEmpty()) {
                    data.setEquippedTechniqueId("");
                    CapabilityEvents.syncToClient(player);
                    return;
                }
                Technique t = Technique.byId(msg.techniqueId);
                if (t == null) {
                    return;
                }
                if (!data.getLearnedTechniques().contains(t.id())) {
                    return;
                }
                if (!TechniqueLoadoutHelper.canEquipForCurrentState(data, t)) {
                    String key = t.isGhostDao() ? "message.friday_cultivation.technique.requires_soul" : "message.friday_cultivation.technique.requires_living";
                    player.displayClientMessage((Component)Component.translatable((String)key, (Object[])new Object[]{t.displayName()}), true);
                    return;
                }
                if (!SpiritRootBonusHelper.canEquipTechnique((Player)player, t)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.technique_book.requirement_not_met", (Object[])new Object[]{t.displayName()}), true);
                    return;
                }
                data.setEquippedTechniqueId(t.id());
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}

