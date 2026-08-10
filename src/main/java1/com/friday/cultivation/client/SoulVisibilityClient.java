package com.friday.cultivation.client;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

/**
 * 灵魂可见性客户端 - 严格 1:1 复刻原模组
 * 混淆名映射: m_91087_=getInstance, f_91074_=player
 * 完全照搬原 mod: xiaoxiang.cultivation.client.SoulVisibilityClient
 */
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
