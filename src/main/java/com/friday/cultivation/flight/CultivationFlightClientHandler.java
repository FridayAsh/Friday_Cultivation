package com.friday.cultivation.flight;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 修仙飞行客户端：
 * - 双击空格切换灵气飞行开关（发 QiFlightTogglePacket，服务端权威）；
 * - 灵气飞行激活（isQiFlightToggled）或御剑激活时，补设 mayfly+flying
 *   防止 Caelus 覆盖，运动交给 MC 原生创造飞行（空格上升/Shift 下降）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class CultivationFlightClientHandler {
    private static long lastJumpPressTick = -100L;
    private static boolean prevJumpDown = false;

    private CultivationFlightClientHandler() {
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
        // 双击空格切换灵气飞行
        boolean jumpDown = mc.options.keyJump.isDown();
        boolean jumpPressed = jumpDown && !CultivationFlightClientHandler.prevJumpDown;
        CultivationFlightClientHandler.prevJumpDown = jumpDown;
        if (jumpPressed) {
            long now = player.tickCount;
            boolean isDoubleJump = now - CultivationFlightClientHandler.lastJumpPressTick <= 8L;
            if (isDoubleJump && !CultivationFlightHandler.isSwordFlightActive(player)) {
                com.friday.cultivation.network.ModNetwork.CHANNEL.sendToServer(new QiFlightTogglePacket());
                CultivationFlightClientHandler.lastJumpPressTick = -100L;
            } else {
                CultivationFlightClientHandler.lastJumpPressTick = now;
            }
        }
        // 补设飞行状态（防 Caelus 覆盖）
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        boolean sword = CultivationFlightHandler.isSwordFlightActive(player);
        boolean qi = CultivationFlightHandler.canQiFlight(player);
        if (sword || qi) {
            if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
                player.getAbilities().mayfly = true;
                player.getAbilities().flying = true;
            }
        }
    }

    @SubscribeEvent
    public static void onMovementInput(MovementInputUpdateEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof LocalPlayer)) {
            return;
        }
        LocalPlayer lp = (LocalPlayer)player;
        Minecraft mc = Minecraft.getInstance();
        if (lp != mc.player || mc.options == null) {
            return;
        }
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        boolean sword = CultivationFlightHandler.isSwordFlightActive(lp);
        boolean qi = CultivationFlightHandler.canQiFlight(lp);
        if (!sword && !qi) {
            return;
        }
        if (!lp.getAbilities().mayfly || !lp.getAbilities().flying) {
            lp.getAbilities().mayfly = true;
            lp.getAbilities().flying = true;
        }
        if (mc.options.keySprint.isDown()) {
            lp.setSprinting(true);
        }
    }
}
