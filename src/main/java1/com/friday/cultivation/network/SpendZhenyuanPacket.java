package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 消耗真元包（严格照搬原模组 com.xiaoxiang.cultivation.network.SpendZhenyuanPacket）。
 * <p>客户端 → 服务端，每次最多花费 64 点真元到 {@link CultivationData.ZhenyuanAttr} 对应属性。</p>
 */
public class SpendZhenyuanPacket {
    private static final int MAX_AMOUNT_PER_PACKET = 64;
    private final int attrOrdinal;
    private final int amount;

    public SpendZhenyuanPacket(CultivationData.ZhenyuanAttr attr) {
        this(attr, 1);
    }

    public SpendZhenyuanPacket(CultivationData.ZhenyuanAttr attr, int amount) {
        this.attrOrdinal = attr.ordinal();
        this.amount = amount;
    }

    private SpendZhenyuanPacket(int ord, int amount) {
        this.attrOrdinal = ord;
        this.amount = amount;
    }

    public static void encode(SpendZhenyuanPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.attrOrdinal);
        buf.writeVarInt(msg.amount);
    }

    public static SpendZhenyuanPacket decode(FriendlyByteBuf buf) {
        return new SpendZhenyuanPacket(buf.readVarInt(), buf.readVarInt());
    }

    public static void handle(SpendZhenyuanPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData.ZhenyuanAttr[] values = CultivationData.ZhenyuanAttr.values();
            if (msg.attrOrdinal < 0 || msg.attrOrdinal >= values.length) {
                return;
            }
            CultivationData.ZhenyuanAttr attr = values[msg.attrOrdinal];
            CultivationData ic = CultivationCapability.get((Player) player).orElse(null);
            if (ic == null) {
                return;
            }
            int amount = Math.min(msg.amount, MAX_AMOUNT_PER_PACKET);
            if (amount <= 0) {
                return;
            }
            int spent = 0;
            for (int i = 0; i < amount && ic.spendZhenyuanOn(attr); ++i) {
                ++spent;
            }
            if (spent > 0) {
                CapabilityEvents.syncToClient((ServerPlayer) player);
            }
        });
        ctx.setPacketHandled(true);
    }
}
