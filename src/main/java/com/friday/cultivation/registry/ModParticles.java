/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.particles.ParticleType
 *  net.minecraft.core.particles.SimpleParticleType
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.PARTICLE_TYPES, (String)"friday_cultivation");
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI = PARTICLES.register("ambient_qi", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_WOOD = PARTICLES.register("ambient_qi_wood", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_FIRE = PARTICLES.register("ambient_qi_fire", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_WATER = PARTICLES.register("ambient_qi_water", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_EARTH = PARTICLES.register("ambient_qi_earth", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_METAL = PARTICLES.register("ambient_qi_metal", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_ICE = PARTICLES.register("ambient_qi_ice", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_LIGHTNING = PARTICLES.register("ambient_qi_lightning", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> AMBIENT_QI_LOTUS = PARTICLES.register("ambient_qi_lotus", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> YIN_QI = PARTICLES.register("yin_qi", () -> new SimpleParticleType(false));
    public static final RegistryObject<SimpleParticleType> BREAKTHROUGH = PARTICLES.register("breakthrough", () -> new SimpleParticleType(true));
    public static final RegistryObject<SimpleParticleType> QI_ABSORB = PARTICLES.register("qi_absorb", () -> new SimpleParticleType(true));

    private ModParticles() {
    }

    public static void register(IEventBus bus) {
        PARTICLES.register(bus);
    }
}

