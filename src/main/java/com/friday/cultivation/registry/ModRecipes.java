/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.item.crafting.Recipe
 *  net.minecraft.world.item.crafting.RecipeSerializer
 *  net.minecraft.world.item.crafting.RecipeType
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.cultivation.refining.RefiningRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.RECIPE_SERIALIZERS, (String)"friday_cultivation");
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create((ResourceKey)Registries.RECIPE_TYPE, (String)"friday_cultivation");
    public static final RegistryObject<RecipeSerializer<AlchemyRecipe>> ALCHEMY_SERIALIZER = RECIPE_SERIALIZERS.register("alchemy", AlchemyRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<AlchemyRecipe>> ALCHEMY_TYPE = RECIPE_TYPES.register("alchemy", () -> ModRecipes.simpleType("alchemy"));
    public static final RegistryObject<RecipeSerializer<RefiningRecipe>> REFINING_SERIALIZER = RECIPE_SERIALIZERS.register("refining", RefiningRecipe.Serializer::new);
    public static final RegistryObject<RecipeType<RefiningRecipe>> REFINING_TYPE = RECIPE_TYPES.register("refining", () -> ModRecipes.simpleType("refining"));

    private ModRecipes() {
    }

    public static void register(IEventBus bus) {
        RECIPE_SERIALIZERS.register(bus);
        RECIPE_TYPES.register(bus);
    }

    private static <T extends Recipe<?>> RecipeType<T> simpleType(String path) {
        final ResourceLocation id = new ResourceLocation("friday_cultivation", path);
        return new RecipeType<T>(){

            public String toString() {
                return id.toString();
            }
        };
    }
}

