package com.friday.cultivation.network;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换性别数据包（严格照搬原模组 com.xiaoxiang.cultivation.network.CycleGenderPacket）
 */
public class CycleGenderPacket {
    public CycleGenderPacket() {}

    public static void encode(CycleGenderPacket msg, FriendlyByteBuf buf) {}

    public static CycleGenderPacket decode(FriendlyByteBuf buf) {
        return new CycleGenderPacket();
    }

    public static void handle(CycleGenderPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            if (ic != null) {
                if (ic.getGenderEditsLeft() <= 0) {
                    player.sendSystemMessage(Component.translatable("message.friday_cultivation.gender.no_edits"));
                    return;
                }
                ic.setGender(ic.getGender() == 2 ? 1 : 2);
                ic.setGenderEditsLeft(ic.getGenderEditsLeft() - 1);
                CapabilityEvents.syncToClient(player);
                String key = ic.getGender() == 2 ? "screen.friday_cultivation.gender.female" : "screen.friday_cultivation.gender.male";
                player.sendSystemMessage(Component.translatable("message.friday_cultivation.gender.set",
                        Component.translatable(key), ic.getGenderEditsLeft()));
            }
        });
        ctx.setPacketHandled(true);
    }
}