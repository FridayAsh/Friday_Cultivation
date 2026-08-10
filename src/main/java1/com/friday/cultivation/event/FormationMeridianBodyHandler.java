package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class FormationMeridianBodyHandler {
    private static final int INTERVAL_TICKS = 20;
    private static final int RANGE_BLOCKS = 100;
    private static final long QI_PER_SECOND = 100L;

    private FormationMeridianBodyHandler() {
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
        ServerPlayer sp = (ServerPlayer) player;
        if (sp.tickCount % 20 != 0) {
            return;
        }
        CultivationCapability.get(player).ifPresent(data -> {
            if (data.getPhysique() != Physique.FORMATION_MERIDIAN_BODY) {
                return;
            }
            FormationCorePlateBlockEntity target = FormationMeridianBodyHandler.findNeediestPlate(sp.serverLevel(), sp.blockPosition());
            if (target == null) {
                return;
            }
            long added = target.addQi(100L);
            if (added > 0L) {
                FormationMeridianBodyHandler.sendQiTrail(sp.serverLevel(), sp, target.getBlockPos());
            }
        });
    }

    private static FormationCorePlateBlockEntity findNeediestPlate(ServerLevel level, BlockPos center) {
        FormationCorePlateBlockEntity best = null;
        double bestFill = Double.MAX_VALUE;
        long bestQi = Long.MAX_VALUE;
        int minChunkX = center.getX() - 100 >> 4;
        int maxChunkX = center.getX() + 100 >> 4;
        int minChunkZ = center.getZ() - 100 >> 4;
        int maxChunkZ = center.getZ() + 100 >> 4;
        int rangeSq = 10000;
        for (int cx = minChunkX; cx <= maxChunkX; ++cx) {
            for (int cz = minChunkZ; cz <= maxChunkZ; ++cz) {
                if (!level.getChunkSource().hasChunk(cx, cz)) continue;
                LevelChunk chunk = level.getChunk(cx, cz);
                for (BlockEntity be : chunk.getBlockEntities().values()) {
                    double fill;
                    FormationCorePlateBlockEntity plate;
                    if (!(be instanceof FormationCorePlateBlockEntity) || (plate = (FormationCorePlateBlockEntity) be).getBlockPos().distSqr((Vec3i) center) > (double) rangeSq) continue;
                    long maxQi = plate.getMaxQi();
                    long currentQi = plate.getCurrentQi();
                    if (currentQi >= maxQi) continue;
                    double d = fill = maxQi <= 0L ? 1.0 : (double) currentQi / (double) maxQi;
                    if (!(fill < bestFill) && (fill != bestFill || currentQi >= bestQi)) continue;
                    best = plate;
                    bestFill = fill;
                    bestQi = currentQi;
                }
            }
        }
        return best;
    }

    private static void sendQiTrail(ServerLevel level, ServerPlayer player, BlockPos platePos) {
        Vec3 from = player.position().add(0.0, (double) player.getBbHeight() * 0.68, 0.0);
        Vec3 to = Vec3.atCenterOf((Vec3i) platePos);
        Vec3 delta = to.subtract(from);
        int samples = Math.min(28, Math.max(8, (int) (delta.length() / 4.0)));
        for (int i = 0; i <= samples; ++i) {
            double t = (double) i / (double) samples;
            Vec3 p = from.add(delta.scale(t));
            level.sendParticles((ParticleOptions) ((SimpleParticleType) ModParticles.QI_ABSORB.get()), p.x, p.y, p.z, 1, 0.025, 0.025, 0.025, 0.0);
        }
        level.sendParticles((ParticleOptions) ((SimpleParticleType) ModParticles.AMBIENT_QI.get()), to.x, to.y + 0.25, to.z, 8, 0.18, 0.18, 0.18, 0.01);
    }
}
