package com.friday.cultivation.registry;

import com.friday.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.refining.RefiningRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, "friday_cultivation");
    public static final DeferredRegister<RecipeType<?>> TYPES =
            DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, "friday_cultivation");

    public static final RegistryObject<AlchemyRecipe.Serializer> ALCHEMY_SERIALIZER =
            SERIALIZERS.register("alchemy", AlchemyRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<AlchemyRecipe>> ALCHEMY_TYPE =
            TYPES.register("alchemy", () -> new RecipeType<AlchemyRecipe>() { @Override public String toString() { return "friday_cultivation:alchemy"; } });

    // 炼器 (Phase 13 续) — 注册炼器配方类型与序列化器
    public static final RegistryObject<RefiningRecipe.Serializer> REFINING_SERIALIZER =
            SERIALIZERS.register("refining", RefiningRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<RefiningRecipe>> REFINING_TYPE =
            TYPES.register("refining", () -> new RecipeType<RefiningRecipe>() { @Override public String toString() { return "friday_cultivation:refining"; } });

    private ModRecipes() {}
    public static void register(IEventBus bus) { SERIALIZERS.register(bus); TYPES.register(bus); }
}
