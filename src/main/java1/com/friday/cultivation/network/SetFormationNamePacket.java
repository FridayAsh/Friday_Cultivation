package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.inventory.FormationMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 设置阵法名称包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.SetFormationNamePacket。
 * 玩家为阵法核心盘命名，服务端写入方块实体并反馈。
 */
public class SetFormationNamePacket {
    private final String name;

    public SetFormationNamePacket(String name) {
        this.name = name == null ? "" : name;
    }

    public static void encode(SetFormationNamePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.name, 32);
    }

    public static SetFormationNamePacket decode(FriendlyByteBuf buf) {
        return new SetFormationNamePacket(buf.readUtf(32));
    }

    public static void handle(SetFormationNamePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1453$temp = player.containerMenu;
            if (!(patt1453$temp instanceof FormationMenu)) {
                return;
            }
            FormationMenu menu = (FormationMenu) patt1453$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            be.setCustomName(msg.name);
            String saved = be.getCustomName();
            if (saved.isEmpty()) {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.formation.name_cleared").withStyle(ChatFormatting.GRAY), true);
            } else {
                player.displayClientMessage(Component.translatable("message.friday_cultivation.formation.name_saved", saved).withStyle(ChatFormatting.GOLD), true);
            }
        });
        ctx.setPacketHandled(true);
    }
}
