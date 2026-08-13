package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.FlightInputPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiFlightTogglePacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵气飞行客户端：
 * - 双击空格显式激活/取消灵气飞行（类似创造模式开关）；
 * - 激活后服务端每 tick 授权 mayfly，玩家按空格即自然起飞上升；
 * - 飞行中每 2 tick 发送输入状态包（跳跃/疾跑/潜行），
 *   服务端据此施加垂直上升/水平加速/减速（对照 DivineArsenal FlightInputPacket）。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    private static int tickCounter = 0;
    /** 双击检测：上次空格按下 tick 与按下边缘 */
    private static long lastJumpPressTick = -100L;
    private static boolean prevJumpDown = false;
    /** 双击激活后抑制空格输入直到松开，避免激活瞬间残留上升输入 */
    private static boolean suppressJumpUntilRelease = false;

    private QiFlightClientHandler() {
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
        // 双击空格切换灵气飞行（按下边缘检测）
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
                    data.setQiFlightActive(!data.isQiFlightActive());
                    QiFlightClientHandler.suppressJumpUntilRelease = true;
                    QiFlightClientHandler.lastJumpPressTick = -100L;
                } else {
                    QiFlightClientHandler.lastJumpPressTick = now;
                }
            }
        }
        if (QiFlightClientHandler.suppressJumpUntilRelease && !jumpDown) {
            QiFlightClientHandler.suppressJumpUntilRelease = false;
        }
        if (!QiFlightClientHandler.isQiFlightFlying(player)) {
            return;
        }
        // 每 2 tick 发送输入状态，降低网络流量
        ++QiFlightClientHandler.tickCounter;
        if (QiFlightClientHandler.tickCounter % 2 != 0) {
            return;
        }
        boolean jump = QiFlightClientHandler.suppressJumpUntilRelease ? false : mc.options.keyJump.isDown();
        boolean sprint = mc.options.keySprint.isDown();
        boolean sneak = mc.options.keyShift.isDown();
        if (jump || sprint || sneak) {
            ModNetwork.CHANNEL.sendToServer((Object)new FlightInputPacket(jump, sprint, sneak));
        }
    }

    /** 灵气飞行激活（mayfly 已由服务端授权、且玩家正在飞行） */
    private static boolean isQiFlightFlying(LocalPlayer player) {
        if (player == null || player.isCreative() || player.isSpectator()) {
            return false;
        }
        if (!player.getAbilities().mayfly || !player.getAbilities().flying) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        return data.isQiFlightActive() && data.getCurrentQi() > 0L;
    }
}
