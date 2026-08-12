/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiFlightInputPacket;
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
 * 灵气飞行客户端（参考 DivineArsenal 盔甲套装飞行方案）：
 * - 服务端每 tick 授权 mayfly（灵气飞行启用且灵气>0），玩家按住空格
 *   即自然起飞上升（MC 原生飞行行为）；
 * - 客户端在飞行中每 2 tick 发送输入状态包（跳跃/疾跑/潜行），
 *   服务端据此施加垂直上升/水平加速/减速，实现如创造模式的自由飞行。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    private static final float VANILLA_FLYING_SPEED = 0.05f;
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
        if (player2 != mc.player || mc.options == null || !QiFlightClientHandler.isQiFlightFlying(player2)) {
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
        if (player == null) {
            return;
        }
        if (!QiFlightClientHandler.isQiFlightFlying(player)) {
            return;
        }
        if (Math.abs(player.getAbilities().getFlyingSpeed() - 0.05f) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(0.05f);
        }
        if (mc.options == null || mc.screen != null) {
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
            ModNetwork.CHANNEL.sendToServer((Object)new QiFlightInputPacket(jump, sprint, sneak));
        }
    }

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
        boolean ghostFlight = data.isSoulState() && data.isSpellEnabled(Spell.GHOST_FLIGHT);
        return ghostFlight || data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }

    private static boolean hasFlightMovementInput(Input input) {
        return input != null && (input.hasForwardImpulse() || input.leftImpulse != 0.0f || input.jumping || input.shiftKeyDown);
    }
}
