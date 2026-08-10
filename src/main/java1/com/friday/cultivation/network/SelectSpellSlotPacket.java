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
 * 选择法术槽位包（严格照搬原模组 com.xiaoxiang.cultivation.network.SelectSpellSlotPacket）。
 * <p>客户端 → 服务端，设定 {@code CultivationData.setSelectedSpellSlot(slot)} 后同步。</p>
 */
public class SelectSpellSlotPacket {
    private final int slot;

    public SelectSpellSlotPacket(int slot) {
        this.slot = slot;
    }

    public static void encode(SelectSpellSlotPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.slot);
    }

    public static SelectSpellSlotPacket decode(FriendlyByteBuf buf) {
        return new SelectSpellSlotPacket(buf.readVarInt());
    }

    public static void handle(SelectSpellSlotPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationData ic = CultivationCapability.get((Player) player).orElse(null);
            if (ic == null) {
                return;
            }
            ic.setSelectedSpellSlot(msg.slot);
            CapabilityEvents.syncToClient((ServerPlayer) player);
        });
        ctx.setPacketHandled(true);
    }
}
