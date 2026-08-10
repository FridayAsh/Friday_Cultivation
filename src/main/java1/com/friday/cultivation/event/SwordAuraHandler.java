package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.network.FireSwordAuraPacket;
import com.friday.cultivation.network.ModNetwork;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.SwordItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * SwordAura 触发事件 - 仅 Client。当玩家持剑按下左键时，
 * 若已习得 SwordAura 法术且启用且 qi 足够 50，向服务端发送 FireSwordAuraPacket。
 * 严格 1:1 复刻原 mod SwordAuraHandler。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class SwordAuraHandler {
    private SwordAuraHandler() {
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        tryFireAura();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide()) {
            return;
        }
        tryFireAura();
    }

    private static void tryFireAura() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        if (!(player.getMainHandItem().getItem() instanceof SwordItem)) {
            return;
        }
        boolean ok;
        CultivationData iData = CultivationCapability.get((Player) player).orElse(null);
        if (iData == null) {
            return;
        }
        ok = iData.hasSpell(Spell.SWORD_AURA) && iData.isSpellEnabled(Spell.SWORD_AURA) && iData.getCurrentQi() >= 50L;
        if (!ok) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer(new FireSwordAuraPacket());
    }
}
