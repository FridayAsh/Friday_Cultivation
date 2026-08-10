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

public class ToggleSpellPacket {
    private final String spellId;
    private final boolean enable;

    public ToggleSpellPacket(String spellId, boolean enable) {
        this.spellId = spellId;
        this.enable = enable;
    }

    public static void encode(ToggleSpellPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.spellId, 64);
        buf.writeBoolean(msg.enable);
    }

    public static ToggleSpellPacket decode(FriendlyByteBuf buf) {
        return new ToggleSpellPacket(buf.readUtf(64), buf.readBoolean());
    }

    public static void handle(ToggleSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Spell spell = Spell.byId(msg.spellId);
            if (spell == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.hasSpell(spell)) {
                    return;
                }
                data.setSpellEnabled(spell, msg.enable);
                CapabilityEvents.syncToClient(player);
            });
        });
        ctx.setPacketHandled(true);
    }
}

