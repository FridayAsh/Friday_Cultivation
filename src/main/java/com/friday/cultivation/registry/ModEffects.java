/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.cultivation.effect.BloodBurnEffect;
import com.friday.cultivation.cultivation.effect.ClearMindEffect;
import com.friday.cultivation.cultivation.effect.DivineStrideEffect;
import com.friday.cultivation.cultivation.effect.GravitySuppressionEffect;
import com.friday.cultivation.cultivation.effect.InverseFiveElementsEffect;
import com.friday.cultivation.cultivation.effect.MeridianFrozenEffect;
import com.friday.cultivation.cultivation.effect.PalmThunderStunEffect;
import com.friday.cultivation.cultivation.effect.RootedEffect;
import com.friday.cultivation.cultivation.effect.ShadowStepEffect;
import com.friday.cultivation.cultivation.effect.ShatterArmorEffect;
import com.friday.cultivation.cultivation.effect.TimeStasisEffect;
import com.friday.cultivation.cultivation.effect.TimeStasisFlowEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.MOB_EFFECTS, (String)"friday_cultivation");
    public static final RegistryObject<MobEffect> BLOOD_BURN = EFFECTS.register("blood_burn", BloodBurnEffect::new);
    public static final RegistryObject<MobEffect> CLEAR_MIND = EFFECTS.register("clear_mind", ClearMindEffect::new);
    public static final RegistryObject<MobEffect> DIVINE_STRIDE = EFFECTS.register("divine_stride", DivineStrideEffect::new);
    public static final RegistryObject<MobEffect> SHADOW_STEP = EFFECTS.register("shadow_step", ShadowStepEffect::new);
    public static final RegistryObject<MobEffect> TIME_STASIS = EFFECTS.register("time_stasis", TimeStasisEffect::new);
    public static final RegistryObject<MobEffect> TIME_STASIS_FLOW = EFFECTS.register("time_stasis_flow", TimeStasisFlowEffect::new);
    public static final RegistryObject<MobEffect> SHATTER_ARMOR = EFFECTS.register("shatter_armor", ShatterArmorEffect::new);
    public static final RegistryObject<MobEffect> MERIDIAN_FROZEN = EFFECTS.register("meridian_frozen", MeridianFrozenEffect::new);
    public static final RegistryObject<MobEffect> ROOTED = EFFECTS.register("rooted", RootedEffect::new);
    public static final RegistryObject<MobEffect> GRAVITY_SUPPRESSION = EFFECTS.register("gravity_suppression", GravitySuppressionEffect::new);
    public static final RegistryObject<MobEffect> INVERSE_FIVE_ELEMENTS = EFFECTS.register("inverse_five_elements", InverseFiveElementsEffect::new);
    public static final RegistryObject<MobEffect> PALM_THUNDER_STUN = EFFECTS.register("palm_thunder_stun", PalmThunderStunEffect::new);

    private ModEffects() {
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}

