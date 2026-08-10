/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.SwordItem
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickEmpty
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
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

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class SwordAuraHandler {
    private SwordAuraHandler() {
    }

    @SubscribeEvent
    public static void onLeftClickEmpty(PlayerInteractEvent.LeftClickEmpty event) {
        SwordAuraHandler.tryFireAura();
    }

    @SubscribeEvent
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (!event.getLevel().isClientSide) {
            return;
        }
        SwordAuraHandler.tryFireAura();
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
        boolean ok = CultivationCapability.get((Player)player).map(d -> d.hasSpell(Spell.SWORD_AURA) && d.isSpellEnabled(Spell.SWORD_AURA) && d.getCurrentQi() >= 50L).orElse(false);
        if (!ok) {
            return;
        }
        ModNetwork.CHANNEL.sendToServer((Object)new FireSwordAuraPacket());
    }
}

