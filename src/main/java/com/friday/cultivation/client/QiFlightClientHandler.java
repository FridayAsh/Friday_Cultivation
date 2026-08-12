/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.Input
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.MovementInputUpdateEvent
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiFlightLaunchPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    private static final float VANILLA_FLYING_SPEED = 0.05f;
    private static long lastJumpPressTime = 0L;
    private static boolean wasJumpDown = false;

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
        if (QiFlightClientHandler.isQiFlightFlying(player)) {
            if (Math.abs(player.getAbilities().getFlyingSpeed() - 0.05f) > 1.0E-4f) {
                player.getAbilities().setFlyingSpeed(0.05f);
            }
            return;
        }
        QiFlightClientHandler.tickDoubleJumpLaunch(mc, player);
    }

    /**
     * 双击空格起飞：灵气飞行被动开启且未在飞行时，检测两次空格按下
     * （间隔 ≤ 400ms），立即进入飞行状态（mayfly 由服务端已授权）。
     */
    private static void tickDoubleJumpLaunch(Minecraft mc, LocalPlayer player) {
        if (mc.options == null || mc.screen != null) {
            QiFlightClientHandler.wasJumpDown = mc.options != null && mc.options.keyJump.isDown();
            return;
        }
        boolean jumpDown = mc.options.keyJump.isDown();
        if (jumpDown && !QiFlightClientHandler.wasJumpDown) {
            long now = System.currentTimeMillis();
            long last = QiFlightClientHandler.lastJumpPressTime;
            if (last > 0L && now - last <= 400L) {
                QiFlightClientHandler.tryLaunch(player);
                QiFlightClientHandler.lastJumpPressTime = 0L;
            } else {
                QiFlightClientHandler.lastJumpPressTime = now;
            }
        }
        QiFlightClientHandler.wasJumpDown = jumpDown;
    }

    private static void tryLaunch(LocalPlayer player) {
        if (player.isCreative() || player.isSpectator()) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        if (!data.isSpellEnabled(Spell.QI_FLIGHT) || data.getCurrentQi() <= 0L) {
            return;
        }
        if (!player.getAbilities().mayfly) {
            return;
        }
        // 客户端立即起飞（视觉响应），服务端同步确认
        player.getAbilities().flying = true;
        player.getAbilities().setFlyingSpeed(0.05f);
        player.onUpdateAbilities();
        ModNetwork.CHANNEL.sendToServer((Object)new QiFlightLaunchPacket());
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

