package com.friday.cultivation.client;

import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 地府环境粒子处理器（严格照搬原模组 com.xiaoxiang.cultivation.client.DifuAmbientHandler）
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class DifuAmbientHandler {
    private static final RandomSource RNG = RandomSource.create();
    private static final int PARTICLES_PER_TICK = 6;
    private static final double RADIUS_XZ = 12.0;
    private static final double RADIUS_Y = 7.0;

    private DifuAmbientHandler() {}

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            return;
        }
        if (mc.level.dimension() != ModDimensions.DIFU) {
            return;
        }
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        for (int i = 0; i < PARTICLES_PER_TICK; ++i) {
            double x = px + (RNG.nextDouble() - 0.5) * 2.0 * RADIUS_XZ;
            double y = py + (RNG.nextDouble() - 0.3) * RADIUS_Y;
            double z = pz + (RNG.nextDouble() - 0.5) * 2.0 * RADIUS_XZ;
            mc.level.addParticle((ParticleOptions)ModParticles.YIN_QI.get(), x, y, z, 0.0, 0.012, 0.0);
        }
    }
}