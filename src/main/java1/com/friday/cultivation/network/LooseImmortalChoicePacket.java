package com.friday.cultivation.network;

import com.friday.cultivation.event.LooseImmortalHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 散仙选择包 - 严格 1:1 复刻原模组
 * 混淆名映射: 无混淆名（基础 boolean）
 */
public class LooseImmortalChoicePacket {
    private final boolean becomeLooseImmortal;

    public LooseImmortalChoicePacket(boolean becomeLooseImmortal) {
        this.becomeLooseImmortal = becomeLooseImmortal;
    }

    public static void encode(LooseImmortalChoicePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.becomeLooseImmortal);
    }

    public static LooseImmortalChoicePacket decode(FriendlyByteBuf buf) {
        return new LooseImmortalChoicePacket(buf.readBoolean());
    }

    public static void handle(LooseImmortalChoicePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player != null) {
                LooseImmortalHandler.resolveChoice(player, msg.becomeLooseImmortal);
            }
        });
        ctx.setPacketHandled(true);
    }
}
