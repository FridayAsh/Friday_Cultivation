/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.DamageTypeTags
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.entity.living.LivingDamageEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class LifeCreationPillHandler {
    private LifeCreationPillHandler() {
    }

    @SubscribeEvent(priority=EventPriority.LOW)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (livingEntity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)livingEntity;
            LifeCreationPillHandler.tryReviveNpc(event, npc);
            return;
        }
        livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (event.getAmount() < player.getHealth()) {
            return;
        }
        Inventory inv = player.getInventory();
        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack s = inv.getItem(i);
            if (!s.is((Item)ModItems.PILL_REJUVENATION_IMMORTAL.get())) continue;
            slot = i;
            break;
        }
        if (slot == -1) {
            return;
        }
        inv.getItem(slot).shrink(1);
        event.setCanceled(true);
        player.setHealth(player.getMaxHealth());
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        CultivationCapability.get((Player)player).ifPresent(data -> {
            long restore = data.getMaxQi() / 3L;
            long newQi = Math.min(data.getMaxQi(), data.getCurrentQi() + restore);
            data.setCurrentQi(newQi);
            CapabilityEvents.syncToClient(player);
        });
        ServerLevel sl = (ServerLevel)player.level();
        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
        sl.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.7f);
        sl.sendParticles((ParticleOptions)ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.7, 0.5, 0.05);
        sl.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.6, 1.0, 0.6, 0.1);
        sl.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 100, 0.8, 1.2, 0.8, 0.5);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.pill.life_creation.activated").withStyle(ChatFormatting.GOLD), false);
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        if (!LifeCreationPillHandler.tryRevivePlayerFromDeath(player, event.getSource())) {
            return;
        }
        event.setCanceled(true);
    }

    public static boolean tryRevivePlayerFromDeath(ServerPlayer player, DamageSource source) {
        if (player == null) {
            return false;
        }
        if (source != null && source.is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return false;
        }
        Inventory inv = player.getInventory();
        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!stack.is((Item)ModItems.PILL_REJUVENATION_IMMORTAL.get())) continue;
            slot = i;
            break;
        }
        if (slot == -1) {
            return false;
        }
        inv.getItem(slot).shrink(1);
        player.setHealth(player.getMaxHealth());
        player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
        player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        CultivationCapability.get((Player)player).ifPresent(data -> {
            long restore = data.getMaxQi() / 3L;
            long newQi = Math.min(data.getMaxQi(), data.getCurrentQi() + restore);
            data.setCurrentQi(newQi);
            CapabilityEvents.syncToClient(player);
        });
        ServerLevel level = player.serverLevel();
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.5f, 1.2f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.7f);
        level.sendParticles((ParticleOptions)ParticleTypes.HEART, player.getX(), player.getY() + 1.0, player.getZ(), 15, 0.5, 0.7, 0.5, 0.05);
        level.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, player.getX(), player.getY() + 1.0, player.getZ(), 80, 0.6, 1.0, 0.6, 0.1);
        level.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, player.getX(), player.getY() + 0.5, player.getZ(), 100, 0.8, 1.2, 0.8, 0.5);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.pill.life_creation.activated").withStyle(ChatFormatting.GOLD), false);
        return true;
    }

    private static void tryReviveNpc(LivingDamageEvent event, WanderingCultivatorEntity npc) {
        if (event.getAmount() < npc.getHealth()) {
            return;
        }
        SimpleContainer inv = npc.getInventory();
        int slot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack s = inv.getItem(i);
            if (!s.is((Item)ModItems.PILL_REJUVENATION_IMMORTAL.get())) continue;
            slot = i;
            break;
        }
        if (slot == -1) {
            return;
        }
        inv.getItem(slot).shrink(1);
        event.setCanceled(true);
        npc.setHealth(npc.getMaxHealth());
        npc.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 200, 1));
        npc.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 1200, 1));
        npc.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
        long restore = npc.getMaxQi() / 3L;
        npc.addQi(restore);
        Level level = npc.level();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 1.5f, 1.2f);
            sl.playSound(null, npc.getX(), npc.getY(), npc.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.0f, 0.7f);
            sl.sendParticles((ParticleOptions)ParticleTypes.HEART, npc.getX(), npc.getY() + 1.0, npc.getZ(), 15, 0.5, 0.7, 0.5, 0.05);
            sl.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, npc.getX(), npc.getY() + 1.0, npc.getZ(), 80, 0.6, 1.0, 0.6, 0.1);
            sl.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, npc.getX(), npc.getY() + 0.5, npc.getZ(), 100, 0.8, 1.2, 0.8, 0.5);
        }
    }
}

