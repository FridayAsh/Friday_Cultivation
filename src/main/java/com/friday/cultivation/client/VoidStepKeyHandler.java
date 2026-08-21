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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class VoidStepKeyHandler {
    private static final long DOUBLE_TAP_WINDOW_MS = 400L;
    private static final int K_FORWARD = 0;
    private static final int K_BACK = 1;
    private static final int K_LEFT = 2;
    private static final int K_RIGHT = 3;
    private static final int K_SNEAK = 4;
    private static final int K_JUMP = 5;
    private static final int NUM_KEYS = 6;
    private static final int[] DIR_BIT_FOR_KEY = new int[]{1, 2, 4, 8, 16, 32};
    private static final boolean[] wasDown = new boolean[6];
    private static final long[] lastPressTime = new long[6];

    private VoidStepKeyHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            VoidStepKeyHandler.resetState();
            return;
        }
        if (mc.screen != null) {
            return;
        }
        if (!VoidStepKeyHandler.canUseVoidStep(player)) {
            VoidStepKeyHandler.resetState();
            return;
        }
        Options opts = mc.options;
        boolean[] curDown = new boolean[]{opts.keyUp.isDown(), opts.keyDown.isDown(), opts.keyLeft.isDown(), opts.keyRight.isDown(), opts.keyShift.isDown(), opts.keyJump.isDown()};
        long now = System.currentTimeMillis();
        boolean onGround = player.onGround();
        boolean canTriggerJump3 = onGround || player.fallDistance < 0.5f;
        boolean inSlowFall = !onGround && VoidStepKeyHandler.isFarEnoughAboveGround(player);
        for (int i = 0; i < 6; ++i) {
            boolean pressEdge;
            boolean bl = pressEdge = curDown[i] && !wasDown[i];
            if (pressEdge) {
                boolean doubleTap;
                long lastPress = lastPressTime[i];
                boolean bl2 = doubleTap = lastPress > 0L && now - lastPress <= 400L;
                if (doubleTap) {
                    VoidStepKeyHandler.handleDoubleTap(player, i, canTriggerJump3, inSlowFall, VoidStepKeyHandler.collectDirBits(opts));
                    VoidStepKeyHandler.lastPressTime[i] = 0L;
                } else {
                    VoidStepKeyHandler.lastPressTime[i] = now;
                }
            }
            VoidStepKeyHandler.wasDown[i] = curDown[i];
        }
    }

    private static void handleDoubleTap(LocalPlayer player, int keyIdx, boolean canTriggerJump3, boolean inSlowFall, int currentDirBits) {
        if (keyIdx == 5 && canTriggerJump3) {
            ModNetwork.CHANNEL.sendToServer((Object)new VoidStepPacket(VoidStepPacket.Op.JUMP_3_BLOCKS));
            return;
        }
        if (inSlowFall) {
            int dirBits = currentDirBits | DIR_BIT_FOR_KEY[keyIdx];
            ModNetwork.CHANNEL.sendToServer((Object)new VoidStepPacket(VoidStepPacket.Op.DASH, dirBits, player.getYRot()));
        }
    }

    private static int collectDirBits(Options opts) {
        int bits = 0;
        if (opts.keyUp.isDown()) {
            bits |= 1;
        }
        if (opts.keyDown.isDown()) {
            bits |= 2;
        }
        if (opts.keyLeft.isDown()) {
            bits |= 4;
        }
        if (opts.keyRight.isDown()) {
            bits |= 8;
        }
        if (opts.keyShift.isDown()) {
            bits |= 0x10;
        }
        if (opts.keyJump.isDown()) {
            bits |= 0x20;
        }
        return bits;
    }

    private static boolean isFarEnoughAboveGround(LocalPlayer player) {
        Level level = player.level();
        BlockPos feet = player.blockPosition();
        return VoidStepHandler.hasSlowFallClearance(level, feet, player.getY());
    }

    private static boolean canUseVoidStep(LocalPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        if (RealmTopology.isBefore(data.getRealm(), Realm.VOID_REFINING)) {
            return false;
        }
        return data.isSpellEnabled(Spell.VOID_STEP);
    }

    private static void resetState() {
        for (int i = 0; i < 6; ++i) {
            VoidStepKeyHandler.wasDown[i] = false;
            VoidStepKeyHandler.lastPressTime[i] = 0L;
        }
    }
}

