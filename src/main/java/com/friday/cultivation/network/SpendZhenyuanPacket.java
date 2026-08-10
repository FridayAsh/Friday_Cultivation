/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

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
            CultivationCapability.get((Player)player).ifPresent(data -> {
                int amount = Math.min(msg.amount, 64);
                if (amount <= 0) {
                    return;
                }
                int spent = 0;
                for (int i = 0; i < amount && data.spendZhenyuanOn(attr); ++i) {
                    ++spent;
                }
                if (spent > 0) {
                    CapabilityEvents.syncToClient(player);
                }
            });
        });
        ctx.setPacketHandled(true);
    }
}

