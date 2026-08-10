package com.friday.cultivation.client;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.QiElement;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

/**
 * 客户端修养同步钩子（严格照搬原模组 com.xiaoxiang.cultivation.client.ClientCultivationHooks）。
 * <p>由服务端派发 SyncCultivationDataPacket 时调用 <code>applySync</code>，再用
 * {@link QiElement} 决定 ambient 粒子。</p>
 */
public final class ClientCultivationHooks {
    private ClientCultivationHooks() {
    }

    public static void applySync(CompoundTag data) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || data == null) {
            return;
        }
        CultivationData d = CultivationCapability.get((Player) player).orElse(null);
        if (d != null) {
            d.deserializeNBT(data);
        }
    }

    public static void onQiAbsorbed(double x, double y, double z, int elementOrdinal) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        if (level == null) {
            return;
        }
        QiElement[] values = QiElement.values();
        QiElement element = values[Math.floorMod(elementOrdinal, values.length)];
        SimpleParticleType elementParticle = pickAmbientParticle(element);
        SimpleParticleType absorb = elementParticle != null ? elementParticle
                : ModParticles.QI_ABSORB.get();
        for (int i = 0; i < 8; ++i) {
            double xj = (level.random.nextDouble() - 0.5) * 0.8;
            double yj = (level.random.nextDouble() - 0.3) * 0.4;
            double zj = (level.random.nextDouble() - 0.5) * 0.8;
            level.addParticle((ParticleOptions) absorb, x, y, z, xj, yj, zj);
        }
    }

    public static SimpleParticleType pickAmbientParticle(QiElement element) {
        if (element == null) {
            return ModParticles.AMBIENT_QI.get();
        }
        switch (element) {
            case METAL: return ModParticles.AMBIENT_QI_METAL.get();
            case WOOD: return ModParticles.AMBIENT_QI_WOOD.get();
            case WATER: return ModParticles.AMBIENT_QI_WATER.get();
            case FIRE: return ModParticles.AMBIENT_QI_FIRE.get();
            case EARTH: return ModParticles.AMBIENT_QI_EARTH.get();
            case ICE: return ModParticles.AMBIENT_QI_ICE.get();
            case LIGHTNING: return ModParticles.AMBIENT_QI_LIGHTNING.get();
            default: return ModParticles.AMBIENT_QI.get();
        }
    }
}
