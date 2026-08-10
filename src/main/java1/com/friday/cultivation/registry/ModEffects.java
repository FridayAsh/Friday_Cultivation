package com.friday.cultivation.registry;

import com.friday.cultivation.effect.BloodBurnEffect;
import com.friday.cultivation.effect.ClearMindEffect;
import com.friday.cultivation.effect.DivineStrideEffect;
import com.friday.cultivation.effect.InverseFiveElementsEffect;
import com.friday.cultivation.effect.GravitySuppressionEffect;
import com.friday.cultivation.effect.MeridianFrozenEffect;
import com.friday.cultivation.effect.PalmThunderStunEffect;
import com.friday.cultivation.effect.RootedEffect;
import com.friday.cultivation.effect.ShadowStepEffect;
import com.friday.cultivation.effect.TimeStasisEffect;
import com.friday.cultivation.effect.TimeStasisFlowEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 修仙模组药水效果注册 — 复刻原模组 ModEffects
 */
public final class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, "friday_cultivation");

    public static final RegistryObject<MobEffect> BLOOD_BURN =
            EFFECTS.register("blood_burn", BloodBurnEffect::new);
    public static final RegistryObject<MobEffect> CLEAR_MIND =
            EFFECTS.register("clear_mind", ClearMindEffect::new);
    public static final RegistryObject<MobEffect> DIVINE_STRIDE =
            EFFECTS.register("divine_stride", DivineStrideEffect::new);
    public static final RegistryObject<MobEffect> SHADOW_STEP =
            EFFECTS.register("shadow_step", ShadowStepEffect::new);

    /** 逆五行连锁标记效果 */
    public static final RegistryObject<MobEffect> INVERSE_FIVE_ELEMENTS =
            EFFECTS.register("inverse_five_elements", InverseFiveElementsEffect::new);

    /** 经脉冻结效果 */
    public static final RegistryObject<MobEffect> MERIDIAN_FROZEN =
            EFFECTS.register("meridian_frozen", MeridianFrozenEffect::new);

    public static final RegistryObject<MobEffect> PALM_THUNDER_STUN =
            EFFECTS.register("palm_thunder_stun", PalmThunderStunEffect::new);

    /** 定身效果（照搬原模组） */
    public static final RegistryObject<MobEffect> ROOTED =
            EFFECTS.register("rooted", RootedEffect::new);

    /** 重力压制效果（照搬原模组） */
    public static final RegistryObject<MobEffect> GRAVITY_SUPPRESSION =
            EFFECTS.register("gravity_suppression", GravitySuppressionEffect::new);

    /** 时间凝滞效果（照搬原模组） */
    public static final RegistryObject<MobEffect> TIME_STASIS =
            EFFECTS.register("time_stasis", TimeStasisEffect::new);

    /** 时间凝滞流逝效果（照搬原模组） */
    public static final RegistryObject<MobEffect> TIME_STASIS_FLOW =
            EFFECTS.register("time_stasis_flow", TimeStasisFlowEffect::new);

    /** 碎甲效果（照搬原模组） */
    public static final RegistryObject<MobEffect> SHATTER_ARMOR =
            EFFECTS.register("shatter_armor", com.friday.cultivation.effect.ShatterArmorEffect::new);

    private ModEffects() {}

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
