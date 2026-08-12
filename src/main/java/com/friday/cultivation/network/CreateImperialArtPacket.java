package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.Random;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

/**
 * 自创帝法：仅真仙九重天且已击杀过大帝时可用。
 * 服务端随机生成帝法名称，学习并自动装备帝法功法（imperial_art）。
 */
public class CreateImperialArtPacket {
    public CreateImperialArtPacket() {
    }

    public static void encode(CreateImperialArtPacket msg, FriendlyByteBuf buf) {
    }

    public static CreateImperialArtPacket decode(FriendlyByteBuf buf) {
        return new CreateImperialArtPacket();
    }

    public static void handle(CreateImperialArtPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!CreateImperialArtPacket.canCreate(data)) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.imperial_art.locked"), true);
                    return;
                }
                String name = CreateImperialArtPacket.rollName();
                data.setImperialArtName(name);
                data.learnTechnique(Technique.IMPERIAL_ART.id());
                data.setEquippedTechniqueId(Technique.IMPERIAL_ART.id());
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.imperial_art.created", (Object[])new Object[]{Component.literal(name).withStyle(ChatFormatting.GOLD)}), false);
            });
        });
        ctx.setPacketHandled(true);
    }

    public static boolean canCreate(CultivationData data) {
        if (data == null) {
            return false;
        }
        if (data.hasCreatedImperialArt()) {
            return false;
        }
        if (data.getRealm() != Realm.TRUE_IMMORTAL || !data.getSubStage().isPeakFor(Realm.TRUE_IMMORTAL)) {
            return false;
        }
        return data.hasKilledGreatEmperor();
    }

    private static final String[] NAME_PREFIX = new String[]{"天", "帝", "万", "玄", "太", "九", "混", "乾", "坤", "圣"};
    private static final String[] NAME_CORE = new String[]{"皇", "极", "道", "尊", "古", "初", "灵", "真", "冥", "虚"};
    private static final String[] NAME_SUFFIX = new String[]{"决", "经", "典", "录", "章", "篇", "书", "谱", "法", "诀"};

    private static String rollName() {
        Random rnd = new Random();
        return NAME_PREFIX[rnd.nextInt(NAME_PREFIX.length)] + NAME_CORE[rnd.nextInt(NAME_CORE.length)] + NAME_SUFFIX[rnd.nextInt(NAME_SUFFIX.length)];
    }
}
