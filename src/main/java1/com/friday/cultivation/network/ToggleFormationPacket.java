package com.friday.cultivation.network;

import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.inventory.FormationMenu;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 切换阵法激活包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.ToggleFormationPacket。
 * 玩家在阵法控制台启用/停用阵法，服务端尝试激活并反馈结果。
 */
public class ToggleFormationPacket {
    private final boolean activate;

    public ToggleFormationPacket(boolean activate) {
        this.activate = activate;
    }

    public static void encode(ToggleFormationPacket msg, FriendlyByteBuf buf) {
        buf.writeBoolean(msg.activate);
    }

    public static ToggleFormationPacket decode(FriendlyByteBuf buf) {
        return new ToggleFormationPacket(buf.readBoolean());
    }

    public static void handle(ToggleFormationPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1421$temp = player.containerMenu;
            if (!(patt1421$temp instanceof FormationMenu)) {
                return;
            }
            FormationMenu menu = (FormationMenu) patt1421$temp;
            FormationCorePlateBlockEntity be = menu.getBlockEntity();
            if (be == null) {
                return;
            }
            if (msg.activate) {
                if (be.isActivated()) {
                    return;
                }
                FormationCorePlateBlockEntity.ActivationResult result = be.tryActivate();
                MutableComponent formationName = result.formationType() == null ? Component.translatable("formation.friday_cultivation.multiple") : Component.translatable(result.formationType().translationKey());
                MutableComponent reply = switch (result.kind()) {
                    default -> throw new IncompatibleClassChangeError();
                    case SUCCESS -> Component.translatable("formation.friday_cultivation.activated", formationName, result.flagCount()).withStyle(ChatFormatting.GREEN);
                    case NO_FLAGS -> Component.translatable("formation.friday_cultivation.fail.no_flags").withStyle(ChatFormatting.RED);
                    case TOO_FEW_FLAGS -> Component.translatable("formation.friday_cultivation.fail.too_few_flags", result.detected(), result.required()).withStyle(ChatFormatting.RED);
                    case NO_QI -> Component.translatable("formation.friday_cultivation.fail.no_qi").withStyle(ChatFormatting.RED);
                };
                player.displayClientMessage(reply, false);
                if (result.kind() == FormationCorePlateBlockEntity.ActivationResultKind.SUCCESS) {
                    if (result.sourcesInRange() <= 0) {
                        player.displayClientMessage(Component.translatable("formation.friday_cultivation.warn.no_sources").withStyle(ChatFormatting.RED), false);
                    } else {
                        player.displayClientMessage(Component.translatable("formation.friday_cultivation.info.sources", result.sourcesInRange()).withStyle(ChatFormatting.AQUA), false);
                    }
                }
            } else {
                if (!be.isActivated()) {
                    return;
                }
                be.deactivate();
                player.displayClientMessage(Component.translatable("formation.friday_cultivation.deactivated").withStyle(ChatFormatting.GRAY), false);
            }
            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }
}
