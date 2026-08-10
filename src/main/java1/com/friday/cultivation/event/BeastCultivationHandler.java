package com.friday.cultivation.event;

import com.friday.cultivation.BiomeQiProfile;
import com.friday.cultivation.beast.BeastCapability;
import com.friday.cultivation.beast.BeastCultivationData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
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

/**
 * 妖兽修炼处理器 - 动物自动根据群系灵气密度获得灵气并晋升境界。
 * 完全照搬原 mod: xiaoxiang.cultivation.event.BeastCultivationHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class BeastCultivationHandler {
    private static final int CHECK_INTERVAL = 100;

    private BeastCultivationHandler() {
    }

    @SubscribeEvent
    public static void onAttachCapabilities(AttachCapabilitiesEvent<Entity> event) {
        Entity entity = event.getObject();
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
        if (entity.tickCount % CHECK_INTERVAL != 0) {
            return;
        }
        Level level = entity.level();
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BeastCapability.get(entity).ifPresent(data -> updateBeast(serverLevel, entity, data));
    }

    private static void updateBeast(ServerLevel level, LivingEntity entity, BeastCultivationData data) {
        BlockPos pos = entity.blockPosition();
        Holder<Biome> biome = level.getBiome(pos);
        BiomeQiProfile profile = BiomeQiProfile.of(biome);
        if (profile.density() < 0.4) {
            return;
        }
        long gained = Math.max(1L, Math.round(profile.density() * 5.0));
        data.addQi(gained);
        if (data.canAdvance()) {
            data.advance();
            announceAdvancement(level, entity, data);
        }
    }

    private static void announceAdvancement(ServerLevel level, LivingEntity entity, BeastCultivationData data) {
        Component name = entity.getName();
        Component msg = Component.translatable("message.friday_cultivation.beast.advance", name, data.getRealm().displayName());
        for (ServerPlayer p : level.players()) {
            if (p.distanceTo(entity) < 32.0) {
                p.displayClientMessage(msg, false);
            }
        }
    }
}
