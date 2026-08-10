package com.friday.cultivation.network;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.sect.SectSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.PacketDistributor;

import java.util.function.Supplier;

/**
 * 宗门友伤开关包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.SetSectFriendlyFirePacket。
 * 玩家在宗门界面切换同宗免伤；校验目标宗门与玩家一致后写入并刷新快照。
 */
public class SetSectFriendlyFirePacket {
    private final boolean sameSectImmunity;
    private final int targetEntityId;

    public SetSectFriendlyFirePacket(boolean sameSectImmunity) {
        this(sameSectImmunity, -1);
    }

    public SetSectFriendlyFirePacket(boolean sameSectImmunity, int targetEntityId) {
        this.sameSectImmunity = sameSectImmunity;
        this.targetEntityId = targetEntityId;
    }

    public static void encode(SetSectFriendlyFirePacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.sameSectImmunity);
        buf.writeVarInt(msg.targetEntityId);
    }

    public static SetSectFriendlyFirePacket decode(FriendlyByteBuf buf) {
        return new SetSectFriendlyFirePacket(buf.readBoolean(), buf.readVarInt());
    }

    public static void handle(SetSectFriendlyFirePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            boolean sameSectScreen;
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            SectSavedData data = SectSavedData.get(player.serverLevel());
            WanderingCultivatorEntity target = SetSectFriendlyFirePacket.resolveTarget(player, msg.targetEntityId);
            String playerSectId = data.sectIdOf(player);
            boolean bl = sameSectScreen = target == null || playerSectId != null && !playerSectId.isBlank() && playerSectId.equals(target.getSectId());
            if (sameSectScreen && data.isRegisteredMember(player)) {
                data.setSameSectImmunity(player, msg.sameSectImmunity);
                player.displayClientMessage(Component.translatable(msg.sameSectImmunity ? "message.friday_cultivation.sect.same_immunity_on" : "message.friday_cultivation.sect.same_immunity_off"), true);
            }
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new OpenSectScreenPacket(data.snapshot(player, target)));
        });
        ctx.setPacketHandled(true);
    }

    private static WanderingCultivatorEntity resolveTarget(ServerPlayer player, int targetEntityId) {
        WanderingCultivatorEntity npc;
        if (targetEntityId < 0) {
            return null;
        }
        Entity entity = player.serverLevel().getEntity(targetEntityId);
        if (entity instanceof WanderingCultivatorEntity && (npc = (WanderingCultivatorEntity) entity).isAlive() && player.distanceToSqr(npc) <= 100.0) {
            return npc;
        }
        return null;
    }
}
