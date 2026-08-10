package com.friday.cultivation.client;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.client.ClientCultivationHooks;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = {Dist.CLIENT})
public final class AmbientQiHandler {
    private static final Random RNG = new Random();
    private static final int SAMPLE_RADIUS_XZ = 7;
    private static final int SAMPLE_RADIUS_Y = 4;
    private static final int SAMPLE_PER_TICK = 25;
    private static final int RAIN_LIGHTNING_TRIES = 1;
    private static final int THUNDER_LIGHTNING_TRIES = 3;

    private AmbientQiHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
    }

    private static void trySpawnAt(Level level, BlockPos.MutableBlockPos pos, BlockState state, QiElement element) {
        double jy;
        SimpleParticleType type = ClientCultivationHooks.pickAmbientParticle(element);
        if (type == null) {
            return;
        }
        double jx = (double) pos.getX() + RNG.nextDouble();
        double jz = (double) pos.getZ() + RNG.nextDouble();
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        if (!aboveState.canOcclude()) {
            jy = (double) pos.getY() + 1.0 + RNG.nextDouble();
        } else if (!state.canOcclude()) {
            jy = (double) pos.getY() + RNG.nextDouble();
        } else {
            return;
        }
        double vx = (RNG.nextDouble() - 0.5) * 0.005;
        double vy = 0.005 + RNG.nextDouble() * 0.005;
        double vz = (RNG.nextDouble() - 0.5) * 0.005;
        level.addParticle((ParticleOptions) type, jx, jy, jz, vx, vy, vz);
    }

    private static void spawnLightningAt(Level level, BlockPos.MutableBlockPos pos) {
        SimpleParticleType type = ClientCultivationHooks.pickAmbientParticle(QiElement.LIGHTNING);
        if (type == null) {
            return;
        }
        double jx = (double) pos.getX() + RNG.nextDouble();
        double jy = (double) pos.getY() + RNG.nextDouble();
        double jz = (double) pos.getZ() + RNG.nextDouble();
        double vx = (RNG.nextDouble() - 0.5) * 0.005;
        double vy = 0.005 + RNG.nextDouble() * 0.005;
        double vz = (RNG.nextDouble() - 0.5) * 0.005;
        level.addParticle((ParticleOptions) type, jx, jy, jz, vx, vy, vz);
    }
}
