package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class EndChargeSpellPacket {
    public EndChargeSpellPacket() {
    }

    public static void encode(EndChargeSpellPacket msg, FriendlyByteBuf buf) {
    }

    public static EndChargeSpellPacket decode(FriendlyByteBuf buf) {
        return new EndChargeSpellPacket();
    }

    public static void handle(EndChargeSpellPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            CultivationData cap = CultivationCapability.get(player).orElse(null);
            if (cap != null) {
                if (cap.isCharging()) {
                    cap.clearCharging();
                    CapabilityEvents.syncToClient(player);
                }
            }
        });
        ctx.setPacketHandled(true);
    }
}
