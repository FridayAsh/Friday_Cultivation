/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 */
package com.friday.cultivation.client;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;

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

