package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
            if (player == null) return;
            CultivationData cap = CultivationCapability.get(player).orElse(null);
            if (cap != null) {
                cap.setEquippedSpellAt(msg.slot, msg.spellId);
                CapabilityEvents.syncToClient(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
