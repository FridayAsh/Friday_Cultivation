/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber$Bus
 */
package com.friday.cultivation.client;

import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.registry.ModParticles;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation", bus=Mod.EventBusSubscriber.Bus.FORGE, value={Dist.CLIENT})
public final class PillItemEntityGlowHandler {
    private static final Random RNG = new Random();

    private PillItemEntityGlowHandler() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        for (Entity e : mc.level.entitiesForRendering()) {
            ItemEntity ie;
            ItemStack stack;
            Item item;
            if (!(e instanceof ItemEntity) || !((item = (stack = (ie = (ItemEntity)e).getItem()).getItem()) instanceof PillItem)) continue;
            PillItem pill = (PillItem)item;
            if (RNG.nextInt(15) != 0) continue;
            ParticleOptions particle = PillItemEntityGlowHandler.particleForTier(pill.tier());
            double dx = (RNG.nextDouble() - 0.5) * 0.6;
            double dy = 0.1 + RNG.nextDouble() * 0.3;
            double dz = (RNG.nextDouble() - 0.5) * 0.6;
            mc.level.addParticle(particle, ie.getX() + dx, ie.getY() + dy, ie.getZ() + dz, 0.0, 0.02, 0.0);
        }
    }

    private static ParticleOptions particleForTier(PillTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> (SimpleParticleType)ModParticles.AMBIENT_QI.get();
            case MID -> (SimpleParticleType)ModParticles.AMBIENT_QI_EARTH.get();
            case HIGH -> (SimpleParticleType)ModParticles.AMBIENT_QI_ICE.get();
            case SUPREME -> (SimpleParticleType)ModParticles.AMBIENT_QI_LIGHTNING.get();
            case IMMORTAL -> (SimpleParticleType)ModParticles.AMBIENT_QI_FIRE.get();
        };
    }
}

