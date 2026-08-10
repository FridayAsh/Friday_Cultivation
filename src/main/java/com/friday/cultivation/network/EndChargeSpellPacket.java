/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.ChargeableSpellHandler;
import com.friday.cultivation.event.SoulHookHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class EndChargeSpellPacket {
    private final int buddhaFireLotusTargetId;

    public EndChargeSpellPacket() {
        this(-1);
    }

    public EndChargeSpellPacket(int buddhaFireLotusTargetId) {
        this.buddhaFireLotusTargetId = buddhaFireLotusTargetId;
    }

    public static void encode(EndChargeSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.buddhaFireLotusTargetId);
    }

    public static EndChargeSpellPacket decode(FriendlyByteBuf buf) {
        return new EndChargeSpellPacket(buf.readVarInt());
    }

    public static void handle(EndChargeSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.isCharging()) {
                    return;
                }
                if (SoulHookHandler.isActionLocked((Entity)player)) {
                    data.clearCharging();
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_hook_spell.action_locked"), true);
                    CapabilityEvents.syncToClient(player);
                    return;
                }
                ChargeableSpellHandler.fireChargedSpell(player, msg.buddhaFireLotusTargetId);
            });
        });
        ctx.setPacketHandled(true);
    }
}

