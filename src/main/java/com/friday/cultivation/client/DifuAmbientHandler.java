/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.client;

import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModParticles;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class DifuAmbientHandler {
    private static final Random RNG = new Random();
    private static final int PARTICLES_PER_TICK = 6;
    private static final double RADIUS_XZ = 12.0;
    private static final double RADIUS_Y = 7.0;

    private DifuAmbientHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null || mc.isPaused()) {
            return;
        }
        if (!mc.level.dimension().equals(ModDimensions.DIFU)) {
            return;
        }
        double px = mc.player.getX();
        double py = mc.player.getY();
        double pz = mc.player.getZ();
        for (int i = 0; i < 6; ++i) {
            double x = px + (RNG.nextDouble() - 0.5) * 2.0 * 12.0;
            double y = py + (RNG.nextDouble() - 0.3) * 7.0;
            double z = pz + (RNG.nextDouble() - 0.5) * 2.0 * 12.0;
            mc.level.addParticle((ParticleOptions)ModParticles.YIN_QI.get(), x, y, z, 0.0, 0.012, 0.0);
        }
    }
}

