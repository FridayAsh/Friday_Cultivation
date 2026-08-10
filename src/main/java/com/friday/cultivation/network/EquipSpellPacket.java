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
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class EquipSpellPacket {
    private final int slot;
    private final String spellId;

    public EquipSpellPacket(int slot, String spellId) {
        this.slot = slot;
        this.spellId = spellId == null ? "" : spellId;
    }

    public static void encode(EquipSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
        buf.writeUtf(msg.spellId, 64);
    }

    public static EquipSpellPacket decode(FriendlyByteBuf buf) {
        return new EquipSpellPacket(buf.readVarInt(), buf.readUtf(64));
    }

    public static void handle(EquipSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (msg.slot < 0 || msg.slot >= 8) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                Spell sp;
                if (!(msg.spellId.isEmpty() || (sp = Spell.byId(msg.spellId)) != null && data.hasSpell(sp))) {
                    return;
                }
                data.setEquippedSpellAt(msg.slot, msg.spellId);
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}

