package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 请求宗门界面包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.RequestSectScreenPacket。
 * 客户端请求打开宗门界面；若正对游历修士则以其为目标快照，否则为玩家自身宗门快照。
 */
public class RequestSectScreenPacket {
    private final int entityId;

    public RequestSectScreenPacket(int entityId) {
        this.entityId = entityId;
    }

    public static void encode(RequestSectScreenPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.entityId);
    }

    public static RequestSectScreenPacket decode(FriendlyByteBuf buf) {
        return new RequestSectScreenPacket(buf.readVarInt());
    }

    public static void handle(RequestSectScreenPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            WanderingCultivatorEntity npc;
            Entity entity;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            WanderingCultivatorEntity target = null;
            if (msg.entityId >= 0 && (entity = player.serverLevel().getEntity(msg.entityId)) instanceof WanderingCultivatorEntity && player.distanceToSqr(npc = (WanderingCultivatorEntity) entity) <= 100.0) {
                WanderingCultivatorMenu menu;
                target = npc;
                AbstractContainerMenu patt1585$temp = player.containerMenu;
                if (patt1585$temp instanceof WanderingCultivatorMenu && (menu = (WanderingCultivatorMenu) patt1585$temp).getNpcEntityId() == target.getId()) {
                    player.closeContainer();
                }
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSectScreenPacket(data.snapshot(player, target)));
        });
        ctx.setPacketHandled(true);
    }
}
