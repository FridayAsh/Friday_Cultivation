package com.friday.cultivation.client;

import com.friday.cultivation.client.screen.FormationScreen;
import com.friday.cultivation.network.SyncFormationFlagsPacket;
import net.minecraft.client.Minecraft;

/** 仅客户端处理阵旗同步包。 */
public final class ClientFormationSyncHooks {
    private ClientFormationSyncHooks() {
    }

    public static void applyFlags(SyncFormationFlagsPacket message) {
        if (Minecraft.getInstance().screen instanceof FormationScreen screen) {
            screen.setFlagEntries(message.corePos(), message.entries());
        }
    }
}
