package com.friday.cultivation.client;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
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
 * 灵气飞行客户端处理器 - 严格 1:1 复刻原模组
 * 混淆名映射: m_91087_=getInstance, f_91074_=player, f_91066_=options,
 *             f_92091_=keyJump, m_90857_=isDown, m_6858_=setJumping,
 *             m_150110_=getAbilities, m_35942_=getFlyingSpeed,
 *             m_35943_=setFlyingSpeed, f_35936_=mayBuild, f_35935_=mayFly,
 *             m_7500_=isSpectator, m_5833_=isPassenger, m_108577_=up,
 *             f_108566_=moveForward, f_108572_=jumping, f_108573_=shifting
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = {Dist.CLIENT})
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
        if (mc.options.keyJump.isDown() && QiFlightClientHandler.hasFlightMovementInput(input)) {
            player2.setJumping(true);
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
        if (player == null || player.isSpectator() || player.isPassenger()) {
            return false;
        }
        if (!player.getAbilities().mayBuild || !player.getAbilities().mayfly) {
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
        return input != null && (input.up || input.forwardImpulse != 0.0f || input.jumping || input.shiftKeyDown);
    }
}
