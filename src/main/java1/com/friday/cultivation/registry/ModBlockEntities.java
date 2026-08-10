package com.friday.cultivation.registry;

import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationRuneBlockEntity;
import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, "friday_cultivation");

    public static final RegistryObject<BlockEntityType<AlchemyCoreBlockEntity>> ALCHEMY_CORE =
            BLOCK_ENTITIES.register("alchemy_core", () -> BlockEntityType.Builder.of(AlchemyCoreBlockEntity::new, ModBlocks.ALCHEMY_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<RefiningCoreBlockEntity>> REFINING_CORE =
            BLOCK_ENTITIES.register("refining_core", () -> BlockEntityType.Builder.of(RefiningCoreBlockEntity::new, ModBlocks.REFINING_CORE.get()).build(null));

    public static final RegistryObject<BlockEntityType<FormationCorePlateBlockEntity>> FORMATION_CORE_PLATE =
            BLOCK_ENTITIES.register("formation_core_plate", () -> BlockEntityType.Builder.of(FormationCorePlateBlockEntity::new,
                    ModBlocks.FORMATION_CORE_PLATE_LOW.get(),
                    ModBlocks.FORMATION_CORE_PLATE_MID.get(),
                    ModBlocks.FORMATION_CORE_PLATE_HIGH.get(),
                    ModBlocks.FORMATION_CORE_PLATE_SUPREME.get(),
                    ModBlocks.FORMATION_CORE_PLATE_IMMORTAL.get()).build(null));

    public static final RegistryObject<BlockEntityType<FormationRuneBlockEntity>> FORMATION_RUNE =
            BLOCK_ENTITIES.register("formation_rune", () -> BlockEntityType.Builder.of(FormationRuneBlockEntity::new, ModBlocks.FORMATION_RUNE.get()).build(null));

    public static final RegistryObject<BlockEntityType<SpiritVeinCoreBlockEntity>> SPIRIT_VEIN_CORE =
            BLOCK_ENTITIES.register("spirit_vein_core", () -> BlockEntityType.Builder.of(SpiritVeinCoreBlockEntity::new, ModBlocks.SPIRIT_VEIN_CORE.get()).build(null));

    private ModBlockEntities() {}
    public static void register(IEventBus bus) { BLOCK_ENTITIES.register(bus); }
}
