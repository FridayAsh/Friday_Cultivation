/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LightningBolt
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.entity.EntityJoinLevelEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.registry.ModParticles;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class LightningStrikeBurstHandler {
    private static final Random RNG = new Random();
    private static final double TRIGGER_RANGE = 64.0;
    private static final int BURST_COUNT = 30;
    private static final double BURST_RADIUS = 5.0;

    private LightningStrikeBurstHandler() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        Entity entity;
        if (event.getLevel().isClientSide() && (entity = event.getEntity()) instanceof LightningBolt) {
            LightningBolt bolt = (LightningBolt)entity;
            Minecraft mc = Minecraft.getInstance();
            LocalPlayer player = mc.player;
            if (player == null) {
                return;
            }
            boolean canSee = CultivationCapability.get((Player)player).map(d -> d.isSpellEnabled(Spell.SPIRIT_VISION)).orElse(false);
            if (!canSee) {
                return;
            }
            if (player.distanceToSqr((Entity)bolt) > 4096.0) {
                return;
            }
            LightningStrikeBurstHandler.spawnLightningBurst(bolt.getX(), bolt.getY(), bolt.getZ());
        }
    }

    private static void spawnLightningBurst(double cx, double cy, double cz) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (int i = 0; i < 30; ++i) {
            double dx = (RNG.nextDouble() - 0.5) * 2.0 * 5.0;
            double dy = RNG.nextDouble() * 5.0;
            double dz = (RNG.nextDouble() - 0.5) * 2.0 * 5.0;
            double vx = (RNG.nextDouble() - 0.5) * 0.05;
            double vy = 0.05 + RNG.nextDouble() * 0.1;
            double vz = (RNG.nextDouble() - 0.5) * 0.05;
            mc.level.addParticle((ParticleOptions)ModParticles.AMBIENT_QI_LIGHTNING.get(), cx + dx, cy + dy, cz + dz, vx, vy, vz);
        }
    }
}

