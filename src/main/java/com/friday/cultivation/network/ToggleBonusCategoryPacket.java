/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class ToggleBonusCategoryPacket {
    private final String categoryId;
    private final boolean enable;

    public ToggleBonusCategoryPacket(String categoryId, boolean enable) {
        this.categoryId = categoryId == null ? "" : categoryId;
        this.enable = enable;
    }

    public static void encode(ToggleBonusCategoryPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.categoryId, 64);
        buf.writeBoolean(msg.enable);
    }

    public static ToggleBonusCategoryPacket decode(FriendlyByteBuf buf) {
        return new ToggleBonusCategoryPacket(buf.readUtf(64), buf.readBoolean());
    }

    public static void handle(ToggleBonusCategoryPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationBonusCategory category = CultivationBonusCategory.byId(msg.categoryId);
            if (category == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (data.isBonusCategoryEnabled(category) == msg.enable) {
                    return;
                }
                data.setBonusCategoryEnabled(category, msg.enable);
                if (category == CultivationBonusCategory.MAX_QI) {
                    data.setCurrentQi(data.getCurrentQi());
                }
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)(msg.enable ? "message.friday_cultivation.bonus_category.enabled" : "message.friday_cultivation.bonus_category.disabled"), (Object[])new Object[]{Component.translatable((String)category.labelKey())}), false);
            });
        });
        ctx.setPacketHandled(true);
    }
}

