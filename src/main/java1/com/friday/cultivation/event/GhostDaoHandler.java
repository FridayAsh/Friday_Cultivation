package com.friday.cultivation.event;

import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 楝奸亾淇偧澶勭悊鍣紙涓ユ牸鐓ф惉鍘熸ā缁?com.xiaoxiang.cultivation.event.GhostDaoHandler锛?
 * 鍦ㄥ湴搴滅淮搴︼紝姣忕鑷姩鍚告敹 10 鐐归槾姘?
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class GhostDaoHandler {
    public static final int YIN_QI_PER_SECOND = 10;

    private GhostDaoHandler() {}

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (!(event.player instanceof ServerPlayer player2)) {
            return;
        }
        if (player2.tickCount % 20 != 0) {
            return;
        }
        if (player2.level().dimension() != ModDimensions.DIFU) {
            return;
        }
        CultivationData ref = CultivationCapability.get(player2).orElse(null);
        if (ref == null) {
            return;
        }
        if (!ref.isSoulState()) {
            return;
        }
        if (!TechniqueLoadoutHelper.equippedTechniqueIsGhostDao(ref)) {
            return;
        }
        if (ref.getCurrentQi() >= ref.getMaxQi() && ref.getCultivationProgress() >= ref.getMaxCultivation()) {
            return;
        }
        long beforeCultivation = ref.getCultivationProgress();
        int qiGained = ref.absorbQi(10);
        long cultivationGained = ref.getCultivationProgress() - beforeCultivation;
        if (qiGained > 0 || cultivationGained > 0L) {
            CapabilityEvents.syncToClient(player2);
            Level level = player2.level();
            if (level instanceof ServerLevel sl) {
                sl.sendParticles((ParticleOptions)((SimpleParticleType)ModParticles.YIN_QI.get()),
                        player2.getX(), player2.getY() + 1.0, player2.getZ(),
                        5, 0.4, 0.5, 0.4, 0.02);
            }
        }
    }
}