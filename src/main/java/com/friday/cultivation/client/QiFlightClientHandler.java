package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.FlightInputPacket;
import com.friday.cultivation.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 修仙飞行客户端（自写实现，绕过 Caelus 飞行管理）：
 * 御剑飞行 / 灵气飞行激活时，玩家悬浮（服务端 setNoGravity），
 * 客户端发送输入状态包（跳跃/疾跑/潜行），服务端据此控制运动。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    private static int inputTickCounter = 0;

    private QiFlightClientHandler() {
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        LocalPlayer player2 = (LocalPlayer)player;
        Minecraft mc = Minecraft.getInstance();
        if (player2 != mc.player || mc.options == null || !QiFlightClientHandler.isFlightActive(player2)) {
            return;
        }
        Input input = event.getInput();
        if (mc.options.keySprint.isDown() && QiFlightClientHandler.hasFlightMovementInput(input)) {
            player2.setSprinting(true);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options == null || mc.screen != null) {
            return;
        }
        if (!QiFlightClientHandler.isFlightActive(player)) {
            return;
        }
        // 每 2 tick 发送输入状态，降低网络开销
        if (++QiFlightClientHandler.inputTickCounter % 2 != 0) {
            return;
        }
        boolean jump = mc.options.keyJump.isDown();
        boolean sprint = mc.options.keySprint.isDown();
        boolean sneak = mc.options.keyShift.isDown();
        if (jump || sprint || sneak) {
            ModNetwork.CHANNEL.sendToServer((Object)new FlightInputPacket(jump, sprint, sneak));
        }
    }

    /** 御剑飞行或灵气飞行激活（客户端数据判断） */
    private static boolean isFlightActive(LocalPlayer player) {
        if (player == null) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        return data.isSwordFlightActive() || data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }

    private static boolean hasFlightMovementInput(Input input) {
        return input != null && (input.hasForwardImpulse() || input.leftImpulse != 0.0f || input.jumping || input.shiftKeyDown);
    }
}
