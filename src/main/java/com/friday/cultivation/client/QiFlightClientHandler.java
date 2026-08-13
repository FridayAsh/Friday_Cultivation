package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.FlightInputPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiFlightTogglePacket;
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
 * 灵气飞行通过双击空格显式激活/取消（类似创造模式）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    /** 上次空格按下 tick（双击检测） */
    private static long lastJumpPressTick = -100L;
    /** 上一 tick 空格是否按住（按下边缘检测） */
    private static boolean prevJumpDown = false;

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
        if (mc.options.keySprint.isDown()) {
            player2.setSprinting(true);
        }
        // 飞行时禁用原生水平/垂直移动输入：运动完全由服务端 FlightInputPacket 控制，
        // 避免客户端本地移动与服务端强制同步冲突导致的卡顿
        input.forwardImpulse = 0.0f;
        input.leftImpulse = 0.0f;
        input.jumping = false;
        input.shiftKeyDown = false;
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
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        // 双击空格切换灵气飞行（类似创造模式）：
        // 用按下边缘检测（上一 tick 未按住→本 tick 按住），避免 consumeClick 与 MC 跳跃冲突
        boolean jumpDown = mc.options.keyJump.isDown();
        boolean jumpPressed = jumpDown && !QiFlightClientHandler.prevJumpDown;
        QiFlightClientHandler.prevJumpDown = jumpDown;
        if (jumpPressed) {
            long now = player.tickCount;
            boolean swordActive = data.isSwordFlightActive();
            boolean isDoubleJump = now - QiFlightClientHandler.lastJumpPressTick <= 8L;
            if (data.hasSpell(Spell.QI_FLIGHT) && !swordActive) {
                if (isDoubleJump) {
                    ModNetwork.CHANNEL.sendToServer((Object)new QiFlightTogglePacket());
                    // 本地立即切换（不等服务端 sync），保证双击后即刻发包控制运动
                    data.setQiFlightActive(!data.isQiFlightActive());
                    QiFlightClientHandler.lastJumpPressTick = -100L;
                } else {
                    QiFlightClientHandler.lastJumpPressTick = now;
                }
            }
        }
        if (!QiFlightClientHandler.isFlightActive(player)) {
            return;
        }
        // 每 tick 发送输入状态（原版创造飞行每 tick 更新，保证响应及时）
        boolean jump = mc.options.keyJump.isDown();
        boolean sprint = mc.options.keySprint.isDown();
        boolean sneak = mc.options.keyShift.isDown();
        // WASD 轴向：前/后与左/右（-1 ~ 1）
        float forward = 0.0f;
        float strafe = 0.0f;
        if (mc.options.keyUp.isDown()) {
            forward += 1.0f;
        }
        if (mc.options.keyDown.isDown()) {
            forward -= 1.0f;
        }
        if (mc.options.keyRight.isDown()) {
            strafe += 1.0f;
        }
        if (mc.options.keyLeft.isDown()) {
            strafe -= 1.0f;
        }
        ModNetwork.CHANNEL.sendToServer((Object)new FlightInputPacket(jump, sprint, sneak, forward, strafe));
    }

    /** 御剑飞行激活，或灵气飞行已显式激活（客户端数据判断；灵气由服务端校验） */
    private static boolean isFlightActive(LocalPlayer player) {
        if (player == null) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        return data.isSwordFlightActive() || data.isQiFlightActive();
    }
}
