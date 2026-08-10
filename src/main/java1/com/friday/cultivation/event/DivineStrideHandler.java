package com.friday.cultivation.event;

import com.friday.cultivation.registry.ModEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class DivineStrideHandler {
    public static final String NBT_AIR_JUMPS_USED = "xc_air_jumps_used";
    public static final int MAX_AIR_JUMPS = 3;

    private DivineStrideHandler() {
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onLivingFall(LivingFallEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof Player player)) {
            return;
        }
        MobEffectInstance e = player.getEffect((MobEffect) ModEffects.DIVINE_STRIDE.get());
        if (e == null) {
            return;
        }
        if (e.getAmplifier() >= 1) {
            event.setDistance(0.0f);
            event.setDamageMultiplier(0.0f);
            event.setCanceled(true);
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
        MobEffectInstance e = sp.getEffect((MobEffect) ModEffects.DIVINE_STRIDE.get());
        if (e == null) {
            if (sp.getPersistentData().contains(NBT_AIR_JUMPS_USED)) {
                sp.getPersistentData().remove(NBT_AIR_JUMPS_USED);
            }
            return;
        }
        int amp = e.getAmplifier();
        if (amp >= 2) {
            BlockPos feet = sp.blockPosition();
            FrostWalkerEnchantment.onEntityMoved(sp, sp.level(), feet, 1);
        }
        if (amp >= 3 && sp.onGround() && sp.getPersistentData().getInt(NBT_AIR_JUMPS_USED) != 0) {
            sp.getPersistentData().putInt(NBT_AIR_JUMPS_USED, 0);
        }
    }
}
