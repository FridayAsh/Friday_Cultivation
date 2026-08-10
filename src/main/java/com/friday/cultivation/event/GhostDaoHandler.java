/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class GhostDaoHandler {
    public static final int YIN_QI_PER_SECOND = 10;

    private GhostDaoHandler() {
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
        if (player2.tickCount % 20 != 0) {
            return;
        }
        if (player2.level().dimension() != ModDimensions.DIFU) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player)player2).orElse(null);
        if (data == null) {
            return;
        }
        if (!data.isSoulState()) {
            return;
        }
        if (!TechniqueLoadoutHelper.equippedTechniqueIsGhostDao(data)) {
            return;
        }
        if (data.getCurrentQi() >= data.getMaxQi() && data.getCultivationProgress() >= data.getMaxCultivation()) {
            return;
        }
        long beforeCultivation = data.getCultivationProgress();
        int qiGained = data.absorbQi(10);
        long cultivationGained = data.getCultivationProgress() - beforeCultivation;
        if (qiGained > 0 || cultivationGained > 0L) {
            CapabilityEvents.syncToClient(player2);
            Level level = player2.level();
            if (level instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.YIN_QI.get()), player2.getX(), player2.getY() + 1.0, player2.getZ(), 5, 0.4, 0.5, 0.4, 0.02);
            }
        }
    }
}

