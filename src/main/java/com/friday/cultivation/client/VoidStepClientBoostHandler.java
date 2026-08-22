/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.BlockPos
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.event.VoidStepHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.VoidStepPacket;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
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
        if (Math.abs(fwd) < 0.05 && Math.abs(strafe) < 0.05) {
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
        double newVx = v.x * 0.9 + dx * 0.06;
        double newVz = v.z * 0.9 + dz * 0.06;
        double newMag = Math.sqrt(newVx * newVx + newVz * newVz);
        if (newMag > 0.42) {
            double scale = 0.42 / newMag;
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
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        if (RealmTopology.isBefore(data.getRealm(), Realm.VOID_REFINING)) {
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
        ModNetwork.CHANNEL.sendToServer((Object)new VoidStepPacket(VoidStepPacket.Op.HELD_INPUT, dirBits, player.getYRot()));
    }

    private static boolean isFarEnoughAboveGround(LocalPlayer player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        return VoidStepHandler.hasSlowFallClearance(level, feet, player.getY());
    }
}

