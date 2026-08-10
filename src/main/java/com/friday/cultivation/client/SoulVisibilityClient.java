/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.client;

import com.friday.cultivation.client.ClientSoulRegistry;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public final class SoulVisibilityClient {
    private SoulVisibilityClient() {
    }

    public static boolean localCanSeeSouls() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null) {
            return false;
        }
        return CultivationCapability.get((Player)mc.player).map(data -> data.isSpellEnabled(Spell.YIN_YANG_EYE)).orElse(ClientSoulRegistry.localIsSoul());
    }
}

