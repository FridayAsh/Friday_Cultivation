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
        if (player == null || !QiFlightClientHandler.isQiFlightFlying(player)) {
            return;
        }
        if (Math.abs(player.getAbilities().getFlyingSpeed() - 0.05f) > 1.0E-4f) {
            player.getAbilities().setFlyingSpeed(0.05f);
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

