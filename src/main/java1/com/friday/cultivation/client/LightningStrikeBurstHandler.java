package com.friday.cultivation.client;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 闪电粒子爆发处理器（严格照搬原模组 com.xiaoxiang.cultivation.client.LightningStrikeBurstHandler）
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class LightningStrikeBurstHandler {
    private static final RandomSource RNG = RandomSource.create();
    private static final double TRIGGER_RANGE = 64.0;
    private static final int BURST_COUNT = 30;
    private static final double BURST_RADIUS = 5.0;

    private LightningStrikeBurstHandler() {}

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide() && event.getEntity() instanceof LightningBolt bolt) {
            Minecraft mc = Minecraft.getInstance();
            Player player = mc.player;
            if (player == null) {
                return;
            }
            CultivationData ic = CultivationCapability.get(player).orElse(null);
            boolean canSee = false;
            if (ic != null) {
                canSee = ic.isSpellEnabled(Spell.SPIRIT_VISION);
            }
            if (!canSee) {
                return;
            }
            if (player.distanceToSqr((Entity)bolt) > 4096.0) {
                return;
            }
            spawnLightningBurst(bolt.getX(), bolt.getY(), bolt.getZ());
        }
    }

    private static void spawnLightningBurst(double cx, double cy, double cz) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (int i = 0; i < BURST_COUNT; ++i) {
            double dx = (RNG.nextDouble() - 0.5) * 2.0 * BURST_RADIUS;
            double dy = RNG.nextDouble() * BURST_RADIUS;
            double dz = (RNG.nextDouble() - 0.5) * 2.0 * BURST_RADIUS;
            double vx = (RNG.nextDouble() - 0.5) * 0.05;
            double vy = 0.05 + RNG.nextDouble() * 0.1;
            double vz = (RNG.nextDouble() - 0.5) * 0.05;
            mc.level.addParticle((ParticleOptions)ModParticles.AMBIENT_QI_LIGHTNING.get(), cx + dx, cy + dy, cz + dz, vx, vy, vz);
        }
    }
}