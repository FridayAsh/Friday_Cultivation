package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 打坐修炼切换包 — 客户端 → 服务端
 */
public class SetMeditatingPacket {

    private final boolean meditating;

    public SetMeditatingPacket(boolean meditating) {
        this.meditating = meditating;
    }

    public static void encode(SetMeditatingPacket packet, FriendlyByteBuf buf) {
        buf.writeBoolean(packet.meditating);
    }

    public static SetMeditatingPacket decode(FriendlyByteBuf buf) {
        return new SetMeditatingPacket(buf.readBoolean());
    }

    public static void handle(SetMeditatingPacket packet, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ServerPlayer player = ctx.get().getSender();
            if (player != null) {
                CultivationData data = CultivationCapability.get(player).orElse(null);
                if (data != null) {
                    data.setMeditating(packet.meditating);
                    if (packet.meditating) {
                        player.displayClientMessage(
                                Component.translatable("message.cultivation.meditate_start"), false);
                    } else {
                        player.displayClientMessage(
                                Component.translatable("message.cultivation.meditate_stop"), false);
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
