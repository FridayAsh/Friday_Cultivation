/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.animal.Animal
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  net.minecraftforge.event.AttachCapabilitiesEvent
 *  net.minecraftforge.event.entity.living.LivingEvent$LivingTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.BiomeQiProfile;
import com.friday.cultivation.cultivation.beast.BeastCapability;
import com.friday.cultivation.cultivation.beast.BeastCultivationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class BeastCultivationHandler {
    private static final int CHECK_INTERVAL = 100;
    private static final long QI_PER_CHECK_BASE = 1L;

    private BeastCultivationHandler() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = (Entity)event.getObject();
        if (entity instanceof Animal && !(entity instanceof Player)) {
            event.addCapability(BeastCapability.ID, BeastCapability.createProvider());
        }
    }

    @SubscribeEvent
    public static void onLivingTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity instanceof Animal)) {
            return;
        }
        if (entity.tickCount % 100 != 0) {
            return;
        }
        Level level = entity.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        BeastCapability.get((Entity)entity).ifPresent(data -> BeastCultivationHandler.updateBeast(level2, entity, data));
    }

    private static void updateBeast(ServerLevel level, LivingEntity entity, BeastCultivationData data) {
        BlockPos pos = entity.blockPosition();
        BiomeQiProfile profile = BiomeQiProfile.of((Holder<Biome>)level.getBiome(pos));
        if (profile.density() < 0.4) {
            return;
        }
        long gained = Math.max(1L, Math.round(profile.density() * 5.0));
        data.addQi(gained);
        if (data.canAdvance()) {
            data.advance();
            BeastCultivationHandler.announceAdvancement(level, entity, data);
        }
    }

    private static void announceAdvancement(ServerLevel level, LivingEntity entity, BeastCultivationData data) {
        Component name = entity.getName();
        MutableComponent msg = Component.translatable((String)"message.friday_cultivation.beast.advance", (Object[])new Object[]{name, data.getRealm().displayName()});
        level.players().forEach(arg_0 -> BeastCultivationHandler.lambda$announceAdvancement$1(entity, (Component)msg, arg_0));
    }

    private static /* synthetic */ void lambda$announceAdvancement$1(LivingEntity entity, Component msg, ServerPlayer p) {
        if (p.distanceToSqr((Entity)entity) < 1024.0) {
            p.displayClientMessage(msg, false);
        }
    }
}

