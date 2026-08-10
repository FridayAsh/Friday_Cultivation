package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationBonusCategory;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
            if (player == null) return;
            CultivationData cap = CultivationCapability.get(player).orElse(null);
            if (cap != null) {
                if (cap.isBodyDefenseEnabled() == msg.enable) return;
                cap.setBonusCategoryEnabled(CultivationBonusCategory.BODY_DEFENSE, msg.enable);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage(Component.translatable(
                        msg.enable ? "message.xiaoxiang_cultivation.body_defense.enabled"
                                : "message.xiaoxiang_cultivation.body_defense.disabled"), false);
            }
        });
        ctx.setPacketHandled(true);
    }
}
