package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 请求加入宗门对话框包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.RequestSectJoinDialoguePacket。
 * 客户端请求打开与宗门 NPC 的入宗对话，服务端校验目标后回传 OpenSectJoinDialoguePacket。
 */
public class RequestSectJoinDialoguePacket {
    private final int targetEntityId;

    public RequestSectJoinDialoguePacket(int targetEntityId) {
        this.targetEntityId = targetEntityId;
    }

    public static void encode(RequestSectJoinDialoguePacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.targetEntityId);
    }

    public static RequestSectJoinDialoguePacket decode(FriendlyByteBuf buf) {
        return new RequestSectJoinDialoguePacket(buf.readVarInt());
    }

    public static void handle(RequestSectJoinDialoguePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            WanderingCultivatorMenu menu;
            WanderingCultivatorEntity target;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Entity entity = player.serverLevel().getEntity(msg.targetEntityId);
            if (!(entity instanceof WanderingCultivatorEntity) || !(target = (WanderingCultivatorEntity) entity).isAlive() || player.distanceToSqr(target) > 100.0) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.sect.dialogue.unavailable"), true);
                return;
            }
            AbstractContainerMenu patt1941$temp = player.containerMenu;
            if (patt1941$temp instanceof WanderingCultivatorMenu && (menu = (WanderingCultivatorMenu) patt1941$temp).getNpcEntityId() == target.getId()) {
                player.closeContainer();
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            if (target.getSectId().isBlank()) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.sect.dialogue.no_sect"), true);
                return;
            }
            CompoundTag snapshot = data.snapshot(player, target);
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSectJoinDialoguePacket(target.getId(), target.getSectName(), target.getCultivatorName().getString(), snapshot));
        });
        ctx.setPacketHandled(true);
    }
}
