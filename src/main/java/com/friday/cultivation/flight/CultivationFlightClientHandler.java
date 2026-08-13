package com.friday.cultivation.flight;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 修仙飞行客户端：飞行中（mayfly+flying）每 2 tick 发送输入状态包
 * （跳跃/疾跑/潜行），服务端据此控制运动。
 * 判定：御剑激活 或 灵气飞行可用（已启用且灵气>0）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class CultivationFlightClientHandler {
    private static int tickCounter = 0;

    private CultivationFlightClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
            return;
        }
        if (!CultivationFlightHandler.isSwordFlightActive(player) && !CultivationFlightHandler.canQiFlight(player)) {
            return;
        }
        // 每 2 tick 发送输入状态，降低网络流量
        ++CultivationFlightClientHandler.tickCounter;
        if (CultivationFlightClientHandler.tickCounter % 2 != 0) {
            return;
        }
        if (mc.options == null || mc.screen != null) {
            return;
        }
        boolean jump = mc.options.keyJump.isDown();
        boolean sprint = mc.options.keySprint.isDown();
        boolean sneak = mc.options.keyShift.isDown();
        if (jump || sprint || sneak) {
            com.friday.cultivation.network.ModNetwork.CHANNEL.sendToServer(new CultivationFlightInputPacket(jump, sprint, sneak));
        }
    }
}
