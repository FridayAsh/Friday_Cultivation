package com.friday.cultivation.network;

import com.friday.cultivation.CultivationBonusCategory;
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
 * 切换加成分类包（严格照搬原模组 com.xiaoxiang.cultivation.network.ToggleBonusCategoryPacket）。
 * <p>客户端 → 服务端，按 {@link CultivationBonusCategory} 启用/禁用某项真元加成。</p>
 */
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
            CultivationData ic = CultivationCapability.get((Player) player).orElse(null);
            if (ic == null) {
                return;
            }
            if (ic.isBonusCategoryEnabled(category) == msg.enable) {
                return;
            }
            ic.setBonusCategoryEnabled(category, msg.enable);
            if (category == CultivationBonusCategory.MAX_QI) {
                ic.setCurrentQi(ic.getCurrentQi());
            }
            CapabilityEvents.syncToClient((ServerPlayer) player);
            player.displayClientMessage(
                    Component.translatable(
                            msg.enable
                                    ? "message.friday_cultivation.bonus_category.enabled"
                                    : "message.friday_cultivation.bonus_category.disabled",
                            Component.translatable(category.labelKey())),
                    false);
        });
        ctx.setPacketHandled(true);
    }
}
