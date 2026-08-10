package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 设定仙号包（严格照搬原模组 com.xiaoxiang.cultivation.network.SetCultivationNamePacket）。
 * <p>客户端 → 服务端，限制 16 字符、过滤 § 颜色符与控制字符后写入
 * {@link CultivationData#setCustomName(String)}。</p>
 */
public class SetCultivationNamePacket {
    private static final int MAX_LEN = 16;
    private final String name;

    public SetCultivationNamePacket(String name) {
        this.name = name;
    }

    public static void encode(SetCultivationNamePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 64);
    }

    public static SetCultivationNamePacket decode(FriendlyByteBuf buf) {
        return new SetCultivationNamePacket(buf.readUtf(64));
    }

    public static void handle(SetCultivationNamePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            String s = msg.name == null ? "" : msg.name;
            s = s.replaceAll("[§\\u0000-\\u001f]", "").trim();
            if (s.length() > MAX_LEN) {
                s = s.substring(0, MAX_LEN);
            }
            String fin = s;
            CultivationData ic = CultivationCapability.get((Player) player).orElse(null);
            if (ic != null) {
                ic.setCustomName(fin);
                CapabilityEvents.syncToClient((ServerPlayer) player);
                player.displayClientMessage(
                        Component.translatable("message.friday_cultivation.name.set",
                                fin.isEmpty() ? player.getGameProfile().getName() : fin),
                        true);
            }
        });
        ctx.setPacketHandled(true);
    }
}
