package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.SectJoinDialogueScreen;
import com.friday.cultivation.client.screen.SectScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;

/**
 * 宗门客户端钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.SectClientHooks）。
 */
public final class SectClientHooks {
    private SectClientHooks() {
    }

    public static void open(CompoundTag snapshot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.setScreen(new SectScreen(snapshot));
    }

    public static void openJoinDialogue(int targetEntityId, String sectName, String npcName, CompoundTag snapshot) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.getConnection() == null) {
            return;
        }
        mc.setScreen(new SectJoinDialogueScreen(targetEntityId, sectName, npcName, snapshot));
    }
}
