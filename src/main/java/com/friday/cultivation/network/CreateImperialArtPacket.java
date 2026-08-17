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
        if (data.getRealm() != Realm.HALF_EMPEROR || !data.getSubStage().isPeakFor(Realm.HALF_EMPEROR)) {
            return false;
        }
        return data.hasKilledGreatEmperor();
    }

    private static final String[] NAME_PREFIX = new String[]{
        "天", "帝", "万", "玄", "太", "九", "混", "乾", "坤", "圣",
        "鸿", "元", "苍", "荒", "宇", "宙", "神", "仙", "灵", "真",
        "冥", "虚", "无", "极", "至", "大", "道", "龙", "凤", "星",
        "月", "日", "天", "紫", "青", "金", "玉", "赤", "墨", "幽",
        "雷", "电", "风", "云", "冰", "炎", "镇", "破", "灭", "斩"
    };
    private static final String[] NAME_CORE = new String[]{
        "皇", "尊", "古", "初", "道", "圣", "神", "龙", "凤", "冥",
        "虚", "极", "无", "真", "元", "玄", "灵", "命", "魂", "心",
        "乾", "坤", "荒", "宇", "宙", "星", "月", "日", "雷", "炎",
        "河", "山", "海", "渊", "界", "天", "地", "人", "神", "魔",
        "凤", "凰", "麒", "麟", "蟒", "蛟", "鲲", "鹏", "麟", "刹"
    };
    private static final String[] NAME_SUFFIX = new String[]{"诀", "卷", "法", "功", "典", "经", "录", "章", "谱", "术"};

    private static String rollName() {
        Random rnd = new Random();
        int length = 3 + rnd.nextInt(3); // 3~5 字（含结尾字）
        StringBuilder sb = new StringBuilder();
        int remaining = length - 1; // 结尾字占 1 字，其余为前置修饰
        while (remaining > 0) {
            int take = Math.min(2, remaining);
            if (take == 2) {
                sb.append(NAME_PREFIX[rnd.nextInt(NAME_PREFIX.length)]);
                sb.append(NAME_CORE[rnd.nextInt(NAME_CORE.length)]);
            } else {
                sb.append(rnd.nextBoolean() ? NAME_PREFIX[rnd.nextInt(NAME_PREFIX.length)] : NAME_CORE[rnd.nextInt(NAME_CORE.length)]);
            }
            remaining -= take;
        }
        sb.append(NAME_SUFFIX[rnd.nextInt(NAME_SUFFIX.length)]);
        return sb.toString();
    }
}
