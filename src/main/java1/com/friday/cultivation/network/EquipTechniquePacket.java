package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
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
            if (player == null) return;
            CultivationData cap = CultivationCapability.get(player).orElse(null);
            if (cap != null) {
                cap.setEquippedTechniqueId(msg.techniqueId);
                CapabilityEvents.syncToClient(player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
