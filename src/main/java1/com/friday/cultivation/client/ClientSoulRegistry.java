package com.friday.cultivation.client;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

/**
 * 客户端灵魂注册表 — 完整复刻原模组 ClientSoulRegistry。
 * 接收服务端 SoulStatePacket 同步的所有灵魂UUID列表，供客户端查询本地玩家是否为灵魂、其他玩家是否为灵魂。
 */
public final class ClientSoulRegistry {
    private static final Set<UUID> SOULS = ConcurrentHashMap.newKeySet();

    private ClientSoulRegistry() {
    }

    public static void replaceAll(Collection<UUID> uuids) {
        SOULS.clear();
        if (uuids != null) {
            SOULS.addAll(uuids);
        }
    }

    public static boolean isSoul(UUID id) {
        return id != null && SOULS.contains(id);
    }

    public static boolean localIsSoul() {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && SOULS.contains(mc.player.getUUID());
    }

    public static Set<UUID> all() {
        return SOULS;
    }

    public static void clear() {
        SOULS.clear();
    }
}
