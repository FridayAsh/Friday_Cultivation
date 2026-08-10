package com.friday.cultivation.registry;

import com.friday.cultivation.loot.AddItemLootModifier;
import com.mojang.serialization.Codec;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/**
 * 战利品修改器注册表（严格照搬原模组 com.xiaoxiang.cultivation.registry.ModLootModifiers）
 */
public final class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> GLM =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, "friday_cultivation");
    public static final RegistryObject<Codec<AddItemLootModifier>> ADD_ITEM =
            GLM.register("add_item", AddItemLootModifier.CODEC);

    private ModLootModifiers() {}

    public static void register(IEventBus bus) {
        GLM.register(bus);
    }
}