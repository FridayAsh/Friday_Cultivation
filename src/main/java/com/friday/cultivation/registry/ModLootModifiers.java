/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.serialization.Codec
 *  net.minecraft.resources.ResourceKey
 *  net.minecraftforge.common.loot.IGlobalLootModifier
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries$Keys
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.mojang.serialization.Codec;
import com.friday.cultivation.loot.AddItemLootModifier;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM = DeferredRegister.create((ResourceKey)ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, (String)"friday_cultivation");
    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM = GLM.register("add_item", AddItemLootModifier.CODEC);

    private ModLootModifiers() {
    }

    public static void register(IEventBus bus) {
        GLM.register(bus);
    }
}

