package com.friday.cultivation.client;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.event.VoidStepHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.VoidStepPacket;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 虚步加速客户端处理器 - 完全照搬原 mod: xiaoxiang.cultivation.client.VoidStepClientBoostHandler。
 * 慢落候选判定、客户端虚空踏步能力校验、方向位掩码采集、按住输入包节流发送、横向速度增强。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class VoidStepClientBoostHandler {
    private static final double BOOST_PER_TICK = 0.06;
    private static final double INPUT_RETENTION = 0.9;
    private static final double MAX_LATERAL_SPEED = 0.42;
    private static final double INPUT_THRESHOLD = 0.05;
    private static final int HELD_INPUT_PACKET_INTERVAL_TICKS = 2;
    private static int heldInputPacketCooldown;

    private VoidStepClientBoostHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        double dz;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (mc.screen != null) {
            heldInputPacketCooldown = 0;
            return;
        }
        if (!VoidStepClientBoostHandler.isSlowFallCandidate(player)) {
            heldInputPacketCooldown = 0;
            return;
        }
        boolean localCanUseVoidStep = VoidStepClientBoostHandler.clientCanUseVoidStep(player);
        if (localCanUseVoidStep) {
            VoidStepClientBoostHandler.capLocalSlowFallVelocity(player);
        }
        Options options = mc.options;
        double fwd = 0.0;
        double strafe = 0.0;
        if (options.keyUp.isDown()) {
            fwd += 1.0;
        }
        if (options.keyDown.isDown()) {
            fwd -= 1.0;
        }
        if (options.keyLeft.isDown()) {
            strafe += 1.0;
        }
        if (options.keyRight.isDown()) {
            strafe -= 1.0;
        }
        if (Math.abs(fwd) < INPUT_THRESHOLD && Math.abs(strafe) < INPUT_THRESHOLD) {
            heldInputPacketCooldown = 0;
            return;
        }
        int dirBits = VoidStepClientBoostHandler.collectHorizontalDirBits(options);
        VoidStepClientBoostHandler.maybeSendHeldInputPacket(player, dirBits);
        if (!localCanUseVoidStep) {
            return;
        }
        double yawRad = Math.toRadians(player.getYRot());
        double sinY = Math.sin(yawRad);
        double cosY = Math.cos(yawRad);
        double dx = strafe * cosY - fwd * sinY;
        double mag = Math.sqrt(dx * dx + (dz = fwd * cosY + strafe * sinY) * dz);
        if (mag < 1.0E-4) {
            return;
        }
        dx /= mag;
        dz /= mag;
        Vec3 v = player.getDeltaMovement();
        double newVx = v.x * INPUT_RETENTION + dx * BOOST_PER_TICK;
        double newVz = v.z * INPUT_RETENTION + dz * BOOST_PER_TICK;
        double newMag = Math.sqrt(newVx * newVx + newVz * newVz);
        if (newMag > MAX_LATERAL_SPEED) {
            double scale = MAX_LATERAL_SPEED / newMag;
            newVx *= scale;
            newVz *= scale;
        }
        player.setDeltaMovement(newVx, v.y, newVz);
        player.hasImpulse = true;
    }

    private static void capLocalSlowFallVelocity(LocalPlayer player) {
        Vec3 v = player.getDeltaMovement();
        if (v.y < -0.1) {
            player.setDeltaMovement(v.x, -0.1, v.z);
            player.hasImpulse = true;
        }
        player.fallDistance = 0.0f;
    }

    private static boolean isSlowFallCandidate(LocalPlayer player) {
        if (player.onGround()) {
            return false;
        }
        if (player.isSpectator()) {
            return false;
        }
        if (player.isCreative()) {
            return false;
        }
        if (player.getAbilities().flying) {
            return false;
        }
        return VoidStepClientBoostHandler.isFarEnoughAboveGround(player);
    }

    private static boolean clientCanUseVoidStep(LocalPlayer player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null) {
            return false;
        }
        if (data.getRealm().ordinal() < Realm.VOID_REFINING.ordinal()) {
            return false;
        }
        return data.isSpellEnabled(Spell.VOID_STEP);
    }

    private static int collectHorizontalDirBits(Options options) {
        int bits = 0;
        if (options.keyUp.isDown()) {
            bits |= 1;
        }
        if (options.keyDown.isDown()) {
            bits |= 2;
        }
        if (options.keyLeft.isDown()) {
            bits |= 4;
        }
        if (options.keyRight.isDown()) {
            bits |= 8;
        }
        return bits;
    }

    private static void maybeSendHeldInputPacket(LocalPlayer player, int dirBits) {
        if (heldInputPacketCooldown > 0) {
            --heldInputPacketCooldown;
            return;
        }
        heldInputPacketCooldown = 1;
        ModNetwork.CHANNEL.sendToServer(new VoidStepPacket(VoidStepPacket.Op.HELD_INPUT, dirBits, player.getYRot()));
    }

    private static boolean isFarEnoughAboveGround(LocalPlayer player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        return VoidStepHandler.hasSlowFallClearance(level, feet, player.getY());
    }
}
