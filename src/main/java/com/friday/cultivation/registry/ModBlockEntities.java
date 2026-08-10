/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.entity.BlockEntityType$Builder
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import com.friday.cultivation.registry.ModBlocks;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCK_ENTITY_TYPES, (String)"friday_cultivation");
    public static final RegistryObject<BlockEntityType<AlchemyCoreBlockEntity>> ALCHEMY_CORE = BLOCK_ENTITIES.register("alchemy_core", () -> BlockEntityType.Builder.of(AlchemyCoreBlockEntity::new, new Block[]{ModBlocks.ALCHEMY_CORE.get()}).build(null));
    public static final RegistryObject<BlockEntityType<RefiningCoreBlockEntity>> REFINING_CORE = BLOCK_ENTITIES.register("refining_core", () -> BlockEntityType.Builder.of(RefiningCoreBlockEntity::new, new Block[]{ModBlocks.REFINING_CORE.get()}).build(null));
    public static final RegistryObject<BlockEntityType<FormationCorePlateBlockEntity>> FORMATION_CORE_PLATE = BLOCK_ENTITIES.register("formation_core_plate", () -> BlockEntityType.Builder.of(FormationCorePlateBlockEntity::new, new Block[]{ModBlocks.LOW_FORMATION_CORE_PLATE.get(), (Block)ModBlocks.MID_FORMATION_CORE_PLATE.get(), (Block)ModBlocks.HIGH_FORMATION_CORE_PLATE.get(), (Block)ModBlocks.SUPREME_FORMATION_CORE_PLATE.get(), (Block)ModBlocks.IMMORTAL_FORMATION_CORE_PLATE.get()}).build(null));
    public static final RegistryObject<BlockEntityType<SpiritVeinCoreBlockEntity>> SPIRIT_VEIN_CORE = BLOCK_ENTITIES.register("spirit_vein_core", () -> BlockEntityType.Builder.of(SpiritVeinCoreBlockEntity::new, new Block[]{ModBlocks.LOW_SPIRIT_VEIN_CORE.get(), (Block)ModBlocks.MID_SPIRIT_VEIN_CORE.get(), (Block)ModBlocks.HIGH_SPIRIT_VEIN_CORE.get(), (Block)ModBlocks.SUPREME_SPIRIT_VEIN_CORE.get(), (Block)ModBlocks.IMMORTAL_SPIRIT_VEIN_CORE.get()}).build(null));
    public static final RegistryObject<BlockEntityType<FormationRuneBlockEntity>> FORMATION_RUNE = BLOCK_ENTITIES.register("formation_rune", () -> BlockEntityType.Builder.of(FormationRuneBlockEntity::new, new Block[]{ModBlocks.FORMATION_RUNE.get()}).build(null));

    private ModBlockEntities() {
    }

    public static void register(IEventBus bus) {
        BLOCK_ENTITIES.register(bus);
    }
}

