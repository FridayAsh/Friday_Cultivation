package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.entity.SeatEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityMountEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class CushionMountHandler {
    private CushionMountHandler() {
    }

    @SubscribeEvent
    public static void onMount(EntityMountEvent event) {
        Entity entity = event.getEntityMounting();
        if (!(entity instanceof ServerPlayer player)) {
            return;
        }
        if (!(event.getEntityBeingMounted() instanceof SeatEntity)) {
            return;
        }
        boolean meditating = event.isMounting();
        CultivationData cap = CultivationCapability.get(player).orElse(null);
        if (cap != null) {
            if (meditating && !cap.hasEquippedTechnique()) {
                player.displayClientMessage(
                        Component.translatable("message.xiaoxiang_cultivation.meditation.no_technique"), true);
                return;
            }
            if (cap.isMeditating() == meditating) {
                return;
            }
            cap.setMeditating(meditating);
            CapabilityEvents.syncToClient(player);
            String key = meditating ? "message.xiaoxiang_cultivation.meditation.start"
                    : "message.xiaoxiang_cultivation.meditation.stop";
            player.displayClientMessage(Component.translatable(key), true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        if (sp.tickCount % 10 != 0) {
            return;
        }
        CultivationData cap = CultivationCapability.get(sp).orElse(null);
        if (cap != null) {
            if (cap.isMeditating() && !(sp.getVehicle() instanceof SeatEntity)) {
                cap.setMeditating(false);
                CapabilityEvents.syncToClient(sp);
            }
        }
    }
}
