/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.advancements.Advancement
 *  net.minecraft.advancements.AdvancementProgress
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket
 *  net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingDeathEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.TimeAccelerationHandler;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenLooseImmortalChoicePacket;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class LooseImmortalHandler {
    private LooseImmortalHandler() {
    }

    public static boolean tryOpenTribulationDeathChoice(LivingDeathEvent event, ServerPlayer player, CultivationData data) {
        if (event == null || player == null || data == null) {
            return false;
        }
        if (data.getRealm() != Realm.TRIBULATION_TRANSCENDENCE) {
            return false;
        }
        if (!data.isInTribulation()) {
            return false;
        }
        if (!TribulationHandler.isTribulationDamage(event.getSource())) {
            return false;
        }
        event.setCanceled(true);
        player.setHealth(Math.max(1.0f, player.getMaxHealth()));
        player.setRemainingFireTicks(0);
        player.invulnerableTime = 20;
        data.setLooseImmortalChoicePending(true);
        TribulationHandler.abortForLooseImmortalChoice(player, data);
        LooseImmortalHandler.openChoice(player);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.choice_open"), false);
        return true;
    }

    @SubscribeEvent(priority=EventPriority.HIGH)
    public static void onLivingDeath(LivingDeathEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player = (ServerPlayer)livingEntity;
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return;
        }
        LooseImmortalHandler.tryOpenTribulationDeathChoice(event, player, data);
    }

    public static void resolveChoice(ServerPlayer player, boolean becomeLooseImmortal) {
        if (player == null) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (!data.isLooseImmortalChoicePending() || data.getRealm() != Realm.TRIBULATION_TRANSCENDENCE) {
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.choice_invalid"), true);
                return;
            }
            data.setLooseImmortalChoicePending(false);
            if (becomeLooseImmortal) {
                LooseImmortalHandler.becomeLooseImmortal(player, data);
            } else {
                data.demoteOnFailure();
                TechniqueEffectHandler.refreshMaxHealth(player);
                SoulStateHandler.enterSoulState(player, false, false);
                SoulStateHandler.beginVoluntaryDifuTransfer(player);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.direct_death"), false);
            }
        });
    }

    public static void becomeLooseImmortal(ServerPlayer player, CultivationData data) {
        long next = player.serverLevel().getGameTime() + 12000000L;
        data.becomeLooseImmortal(next);
        TechniqueEffectHandler.refreshMaxHealth(player);
        player.setHealth(Math.max(1.0f, Math.min(player.getHealth(), player.getMaxHealth())));
        LooseImmortalHandler.grantLooseImmortalAdvancement(player);
        CapabilityEvents.syncToClient(player);
        LooseImmortalHandler.sendTitle(player, (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.became_title"), (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.became_subtitle"), 10, 70, 20);
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 1.0f, 0.55f);
            level2.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.8f, 0.75f);
        }
    }

    public static void completeTribulationSuccess(ServerPlayer player, CultivationData data) {
        int before = data.getLooseImmortalTribulations();
        long next = player.serverLevel().getGameTime() + 12000000L;
        CultivationData.LooseImmortalPromotionResult promotion = data.promoteLooseImmortal(next);
        TechniqueEffectHandler.refreshMaxHealth(player);
        CapabilityEvents.syncToClient(player);
        if (!promotion.promoted()) {
            return;
        }
        int after = promotion.toLevel();
        player.setHealth(player.getMaxHealth());
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_success", (Object[])new Object[]{before, after}), false);
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_reward", (Object[])new Object[]{promotion.freeZhenyuan(), promotion.automaticAttributePerStat(), promotion.maxQiBonus()}), false);
        LooseImmortalHandler.sendTitle(player, (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_success_title", (Object[])new Object[]{after}), (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_success_subtitle"), 10, 60, 20);
        Level level = player.level();
        if (level instanceof ServerLevel) {
            ServerLevel level2 = (ServerLevel)level;
            level2.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0f, 0.82f);
        }
    }

    public static void completeTribulationFailure(ServerPlayer player, CultivationData data) {
        if (data.isLooseImmortal()) {
            data.setNextLooseImmortalTribulationTick(player.serverLevel().getGameTime() + 12000000L);
        }
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_failure"), false);
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationCapability.get((Player)player2).ifPresent(data -> LooseImmortalHandler.tick(player2, data));
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        CultivationCapability.get((Player)player2).ifPresent(data -> {
            if (data.isLooseImmortalChoicePending()) {
                LooseImmortalHandler.openChoice(player2);
            }
            if (data.isLooseImmortal() && data.getLooseImmortalTribulations() < 9 && data.getNextLooseImmortalTribulationTick() < 0L) {
                data.setNextLooseImmortalTribulationTick(player2.serverLevel().getGameTime() + 12000000L);
                CapabilityEvents.syncToClient(player2);
            }
        });
    }

    private static void tick(ServerPlayer player, CultivationData data) {
        if (data.isLooseImmortalChoicePending()) {
            player.setHealth(Math.max(1.0f, player.getMaxHealth()));
            if (player.tickCount % 40 == 0) {
                LooseImmortalHandler.openChoice(player);
            }
            return;
        }
        if (!data.isLooseImmortal()) {
            return;
        }
        if (data.getLooseImmortalTribulations() >= 9) {
            return;
        }
        if (data.isInTribulation()) {
            return;
        }
        long now = player.serverLevel().getGameTime();
        long next = data.getNextLooseImmortalTribulationTick();
        if (next < 0L) {
            data.setNextLooseImmortalTribulationTick(now + 12000000L);
            CapabilityEvents.syncToClient(player);
            return;
        }
        long remaining = data.getLooseImmortalTribulationRemainingTicks(now);
        if (remaining <= 200L && remaining > 0L && player.tickCount % 20 == 0) {
            int seconds = (int)Math.max(1L, (remaining + 19L) / 20L);
            LooseImmortalHandler.sendTitle(player, (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.countdown_title", (Object[])new Object[]{seconds}), (Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.countdown_subtitle"), 0, 25, 5);
        }
        if (remaining <= 0L) {
            LooseImmortalHandler.beginLooseImmortalTribulation(player, data);
        }
    }

    private static void beginLooseImmortalTribulation(ServerPlayer player, CultivationData data) {
        int level = data.getLooseImmortalTribulations();
        int waves = LooseImmortalBonusHelper.wavesForCurrentLevel(level);
        int bolts = LooseImmortalBonusHelper.boltsPerWaveForCurrentLevel(level);
        int damage = LooseImmortalBonusHelper.strikeDamageForCurrentLevel(level);
        if (waves <= 0) {
            return;
        }
        if (data.isTimeAccelerationActive()) {
            TimeAccelerationHandler.stop(player, data, true);
        }
        TribulationHandler.beginLooseImmortalTribulation(player, data,
                new TribulationSpec(waves, bolts, damage, 0.0, 0, data.getTribulationType()));
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.loose_immortal.tribulation_start", (Object[])new Object[]{level, Realm.formatTribulationCount(waves, bolts), damage}), false);
    }

    private static void openChoice(ServerPlayer player) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenLooseImmortalChoicePacket());
    }

    private static void sendTitle(ServerPlayer player, Component title, Component subtitle, int fadeIn, int stay, int fadeOut) {
        player.connection.send((Packet)new ClientboundSetTitlesAnimationPacket(fadeIn, stay, fadeOut));
        player.connection.send((Packet)new ClientboundSetTitleTextPacket(title));
        player.connection.send((Packet)new ClientboundSetSubtitleTextPacket(subtitle));
    }

    private static void grantLooseImmortalAdvancement(ServerPlayer player) {
        if (player == null || player.getServer() == null) {
            return;
        }
        ResourceLocation id = new ResourceLocation("friday_cultivation", "realms/loose_immortal");
        Advancement advancement = player.getServer().getAdvancements().getAdvancement(id);
        if (advancement == null) {
            return;
        }
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (progress.isDone()) {
            return;
        }
        for (String criterion : progress.getRemainingCriteria()) {
            player.getAdvancements().award(advancement, criterion);
        }
    }
}

