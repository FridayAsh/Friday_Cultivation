package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 法术启用/禁用开关包 — 客户端 → 服务端（复刻原模组 ToggleSpellPacket）
 * 切换被动法术的启用状态。
 */
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
            if (player == null) return;
            Spell spell = Spell.byId(msg.spellId);
            if (spell == null) return;
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                if (!ic.hasSpell(spell)) return;
                ic.setSpellEnabled(spell, msg.enable);
                CapabilityEvents.syncToClient(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
