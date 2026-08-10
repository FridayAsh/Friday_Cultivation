/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.world.entity.player.Player
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.registry.ModParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;

public final class ClientCultivationHooks {
    private ClientCultivationHooks() {
    }

    public static void applySync(CompoundTag data) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || data == null) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(d -> d.deserializeNBT(data));
    }

    public static void onQiAbsorbed(double x, double y, double z, int elementOrdinal) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        QiElement element = QiElement.values()[Math.floorMod(elementOrdinal, QiElement.values().length)];
        SimpleParticleType elementParticle = ClientCultivationHooks.pickAmbientParticle(element);
        SimpleParticleType absorb = elementParticle != null ? elementParticle : (SimpleParticleType)ModParticles.QI_ABSORB.get();
        for (int i = 0; i < 8; ++i) {
            mc.level.addParticle((ParticleOptions)absorb, x, y, z, (mc.level.random.nextDouble() - 0.5) * 0.8, (mc.level.random.nextDouble() - 0.3) * 0.4, (mc.level.random.nextDouble() - 0.5) * 0.8);
        }
    }

    public static SimpleParticleType pickAmbientParticle(QiElement element) {
        return switch (element) {
            default -> throw new IncompatibleClassChangeError();
            case PURE -> (SimpleParticleType)ModParticles.AMBIENT_QI.get();
            case METAL -> (SimpleParticleType)ModParticles.AMBIENT_QI_METAL.get();
            case WOOD -> (SimpleParticleType)ModParticles.AMBIENT_QI_WOOD.get();
            case WATER -> (SimpleParticleType)ModParticles.AMBIENT_QI_WATER.get();
            case FIRE -> (SimpleParticleType)ModParticles.AMBIENT_QI_FIRE.get();
            case EARTH -> (SimpleParticleType)ModParticles.AMBIENT_QI_EARTH.get();
            case ICE -> (SimpleParticleType)ModParticles.AMBIENT_QI_ICE.get();
            case LIGHTNING -> (SimpleParticleType)ModParticles.AMBIENT_QI_LIGHTNING.get();
        };
    }
}

