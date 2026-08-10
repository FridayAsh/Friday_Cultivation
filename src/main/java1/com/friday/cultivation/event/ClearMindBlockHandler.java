package com.friday.cultivation.event;

import com.friday.cultivation.registry.ModEffects;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class ClearMindBlockHandler {
    private ClearMindBlockHandler() {
    }

    @SubscribeEvent
    public static void onMobEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        if (!player.hasEffect((MobEffect) ModEffects.CLEAR_MIND.get())) {
            return;
        }
        if (event.getEffectInstance().getEffect().getCategory() == MobEffectCategory.HARMFUL) {
            event.setResult(Event.Result.DENY);
        }
    }
}
