package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.FlightInputPacket;
import com.friday.cultivation.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 灵气飞行客户端（对照 DivineArsenal DivineFlightHandler.ClientHandler 完全一致，
 * 套装穿戴检查替换为灵气飞行判定）。
 * 服务端每 tick 授权 mayfly（灵气飞行启用且灵气>0），玩家按空格即自然起飞上升
 * （MC 原生飞行行为）；客户端在飞行中每 2 tick 发送输入状态包。
 */
@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class QiFlightClientHandler {
    private static int tickCounter = 0;

    private QiFlightClientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null) {
            return;
        }
        if (!QiFlightClientHandler.canQiFlight(player)) {
            return;
        }
        if (!player.getAbilities().flying) {
            return;
        }
        // 每 2 tick 发送输入状态，降低网络流量
        ++QiFlightClientHandler.tickCounter;
        if (QiFlightClientHandler.tickCounter % 2 != 0) {
            return;
        }
        if (mc.options == null) {
            return;
        }
        boolean jump = mc.options.keyJump.isDown();
        boolean sprint = mc.options.keySprint.isDown();
        boolean sneak = mc.options.keyShift.isDown();
        if (jump || sprint || sneak) {
            ModNetwork.CHANNEL.sendToServer((Object)new FlightInputPacket(jump, sprint, sneak));
        }
    }

    /** 灵气飞行可用（已启用且灵气>0）——替代 DivineArsenal 的套装穿戴检查 */
    private static boolean canQiFlight(Player player) {
        if (player == null || player.isCreative() || player.isSpectator()) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        return data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }
}
