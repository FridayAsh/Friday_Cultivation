/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.SoundType
 *  net.minecraft.world.level.block.state.BlockBehaviour
 *  net.minecraft.world.level.block.state.BlockBehaviour$Properties
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.material.MapColor
 *  net.minecraft.world.level.material.PushReaction
 *  net.minecraftforge.eventbus.api.IEventBus
 *  net.minecraftforge.registries.DeferredRegister
 *  net.minecraftforge.registries.ForgeRegistries
 *  net.minecraftforge.registries.IForgeRegistry
 *  net.minecraftforge.registries.RegistryObject
 */
package com.friday.cultivation.registry;

import com.friday.cultivation.block.BoneBlock;
import com.friday.cultivation.block.BoneRemainsBlock;
import com.friday.cultivation.block.CushionBlock;
import com.friday.cultivation.block.HerbBlock;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlock;
import com.friday.cultivation.block.formation.FarmHarvestFlagBlock;
import com.friday.cultivation.block.formation.FlightBanFlagBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationRuneBlock;
import com.friday.cultivation.block.formation.MazeFlagBlock;
import com.friday.cultivation.block.formation.QiGatheringFlagBlock;
import com.friday.cultivation.block.formation.RejuvenationFlagBlock;
import com.friday.cultivation.block.formation.SectProtectionBarrierBlock;
import com.friday.cultivation.block.formation.SectProtectionFlagBlock;
import com.friday.cultivation.block.formation.WitherGrowthFlagBlock;
import com.friday.cultivation.block.refining.RefiningCoreBlock;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlock;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.cultivation.qi.formation.CoreTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create((IForgeRegistry)ForgeRegistries.BLOCKS, (String)"friday_cultivation");
    public static final RegistryObject<Block> CUSHION = BLOCKS.register("cushion", () -> new CushionBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW).strength(0.5f).sound(SoundType.WOOL).noCollission()));
    public static final RegistryObject<Block> LOW_SPIRIT_STONE_ORE = BLOCKS.register("low_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.0f, 3.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> MID_SPIRIT_STONE_ORE = BLOCKS.register("mid_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f, 4.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> HIGH_SPIRIT_STONE_ORE = BLOCKS.register("high_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0f, 6.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> SUPREME_SPIRIT_STONE_ORE = BLOCKS.register("supreme_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(6.0f, 8.0f).sound(SoundType.STONE).noOcclusion()));
    public static final RegistryObject<Block> SPIRIT_VEIN_SPRING = BLOCKS.register("spirit_vein_spring", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(4.0f, 8.0f).lightLevel(state -> 5).sound(SoundType.AMETHYST).noOcclusion()));
    public static final RegistryObject<Block> HERB = BLOCKS.register("herb", () -> new HerbBlock(BlockBehaviour.Properties.copy((BlockBehaviour)Blocks.POPPY).lightLevel(s -> 4)));
    public static final RegistryObject<Block> ALCHEMY_CORE = BLOCKS.register("alchemy_core", () -> new AlchemyCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f).sound(SoundType.COPPER).noOcclusion()));
    public static final RegistryObject<Block> REFINING_CORE = BLOCKS.register("refining_core", () -> new RefiningCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_ORANGE).strength(3.0f, 6.0f).sound(SoundType.COPPER).noOcclusion()));
    public static final RegistryObject<Block> LOW_FORMATION_CORE_PLATE = BLOCKS.register("low_formation_core_plate", () -> new FormationCorePlateBlock(ModBlocks.corePlateProps(), CoreTier.LOW));
    public static final RegistryObject<Block> MID_FORMATION_CORE_PLATE = BLOCKS.register("mid_formation_core_plate", () -> new FormationCorePlateBlock(ModBlocks.corePlateProps(), CoreTier.MID));
    public static final RegistryObject<Block> HIGH_FORMATION_CORE_PLATE = BLOCKS.register("high_formation_core_plate", () -> new FormationCorePlateBlock(ModBlocks.corePlateProps(), CoreTier.HIGH));
    public static final RegistryObject<Block> SUPREME_FORMATION_CORE_PLATE = BLOCKS.register("supreme_formation_core_plate", () -> new FormationCorePlateBlock(ModBlocks.corePlateProps(), CoreTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_FORMATION_CORE_PLATE = BLOCKS.register("immortal_formation_core_plate", () -> new FormationCorePlateBlock(ModBlocks.corePlateProps(), CoreTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_QI_GATHERING_FLAG = BLOCKS.register("low_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_QI_GATHERING_FLAG = BLOCKS.register("mid_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_QI_GATHERING_FLAG = BLOCKS.register("high_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_QI_GATHERING_FLAG = BLOCKS.register("supreme_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_QI_GATHERING_FLAG = BLOCKS.register("immortal_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_SECT_PROTECTION_FLAG = BLOCKS.register("low_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_SECT_PROTECTION_FLAG = BLOCKS.register("mid_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_SECT_PROTECTION_FLAG = BLOCKS.register("high_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_SECT_PROTECTION_FLAG = BLOCKS.register("supreme_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_SECT_PROTECTION_FLAG = BLOCKS.register("immortal_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_WITHER_GROWTH_FLAG = BLOCKS.register("low_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_WITHER_GROWTH_FLAG = BLOCKS.register("mid_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_WITHER_GROWTH_FLAG = BLOCKS.register("high_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_WITHER_GROWTH_FLAG = BLOCKS.register("supreme_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_WITHER_GROWTH_FLAG = BLOCKS.register("immortal_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_REJUVENATION_FLAG = BLOCKS.register("low_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_REJUVENATION_FLAG = BLOCKS.register("mid_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_REJUVENATION_FLAG = BLOCKS.register("high_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_REJUVENATION_FLAG = BLOCKS.register("supreme_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_REJUVENATION_FLAG = BLOCKS.register("immortal_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_FLIGHT_BAN_FLAG = BLOCKS.register("low_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_FLIGHT_BAN_FLAG = BLOCKS.register("mid_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_FLIGHT_BAN_FLAG = BLOCKS.register("high_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_FLIGHT_BAN_FLAG = BLOCKS.register("supreme_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_FLIGHT_BAN_FLAG = BLOCKS.register("immortal_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_MAZE_FLAG = BLOCKS.register("low_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_MAZE_FLAG = BLOCKS.register("mid_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_MAZE_FLAG = BLOCKS.register("high_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_MAZE_FLAG = BLOCKS.register("supreme_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_MAZE_FLAG = BLOCKS.register("immortal_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> LOW_FARM_HARVEST_FLAG = BLOCKS.register("low_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_FARM_HARVEST_FLAG = BLOCKS.register("mid_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_FARM_HARVEST_FLAG = BLOCKS.register("high_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_FARM_HARVEST_FLAG = BLOCKS.register("supreme_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_FARM_HARVEST_FLAG = BLOCKS.register("immortal_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));
    public static final RegistryObject<Block> SECT_PROTECTION_BARRIER = BLOCKS.register("sect_protection_barrier", () -> new SectProtectionBarrierBlock(BlockBehaviour.Properties.of().strength(-1.0f, 3600000.0f).noLootTable().noCollission().isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false).isValidSpawn((s, l, p, type) -> false).pushReaction(PushReaction.BLOCK)));
    public static final RegistryObject<Block> FORMATION_RUNE = BLOCKS.register("formation_rune", () -> new FormationRuneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.05f, 0.05f).noOcclusion().noCollission().noLootTable().lightLevel(state -> (Boolean)state.getValue(FormationRuneBlock.LIT) ? 8 : 0).sound(SoundType.STONE).isViewBlocking((s, l, p) -> false).isSuffocating((s, l, p) -> false).isRedstoneConductor((s, l, p) -> false).isValidSpawn((s, l, p, type) -> false).pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> LOW_SPIRIT_VEIN_CORE = BLOCKS.register("low_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.LOW), SpiritVeinCoreTier.LOW));
    public static final RegistryObject<Block> MID_SPIRIT_VEIN_CORE = BLOCKS.register("mid_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.MID), SpiritVeinCoreTier.MID));
    public static final RegistryObject<Block> HIGH_SPIRIT_VEIN_CORE = BLOCKS.register("high_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.HIGH), SpiritVeinCoreTier.HIGH));
    public static final RegistryObject<Block> SUPREME_SPIRIT_VEIN_CORE = BLOCKS.register("supreme_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.SUPREME), SpiritVeinCoreTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_SPIRIT_VEIN_CORE = BLOCKS.register("immortal_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.IMMORTAL), SpiritVeinCoreTier.IMMORTAL));
    public static final RegistryObject<Block> BONE_REMAINS = BLOCKS.register("bone_remains", () -> new BoneRemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.6f).sound(SoundType.BONE_BLOCK).noCollission().noOcclusion().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> BONE_BLOCK = BLOCKS.register("bone_block", () -> new BoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.5f).sound(SoundType.BONE_BLOCK).noOcclusion().noCollission()));

    private static BlockBehaviour.Properties corePlateProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_PURPLE).strength(3.0f, 6.0f).sound(SoundType.METAL).noCollission().noOcclusion();
    }

    private static BlockBehaviour.Properties flagProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_GREEN).strength(0.5f, 1.0f).sound(SoundType.WOOL).noCollission();
    }

    private static BlockBehaviour.Properties spiritVeinCoreProps(SpiritVeinCoreTier tier) {
        int light = switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> 8;
            case MID -> 10;
            case HIGH -> 12;
            case SUPREME -> 14;
            case IMMORTAL -> 15;
        };
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(4.0f, 8.0f).lightLevel(state -> light).sound(SoundType.AMETHYST).noOcclusion();
    }

    private ModBlocks() {
    }

    public static void register(IEventBus bus) {
        BLOCKS.register(bus);
    }
}

