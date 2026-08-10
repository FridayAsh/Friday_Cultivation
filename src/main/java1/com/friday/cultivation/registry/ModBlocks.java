package com.friday.cultivation.registry;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.block.BoneBlock;
import com.friday.cultivation.block.BoneRemainsBlock;
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
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlock;
import com.friday.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.qi.formation.CoreTier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, "friday_cultivation");

    public static final RegistryObject<AlchemyCoreBlock> ALCHEMY_CORE =
            BLOCKS.register("alchemy_core", () -> new AlchemyCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    public static final RegistryObject<com.friday.cultivation.block.refining.RefiningCoreBlock> REFINING_CORE =
            BLOCKS.register("refining_core", () -> new com.friday.cultivation.block.refining.RefiningCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion()));

    // ── 坐垫方块 ──
    public static final RegistryObject<com.friday.cultivation.block.CushionBlock> CUSHION =
            BLOCKS.register("cushion", () -> new com.friday.cultivation.block.CushionBlock(BlockBehaviour.Properties.of().mapColor(MapColor.WOOL).strength(0.5f).noOcclusion()));

    // ── 灵石矿脉 → 灵泉 ──
    public static final RegistryObject<Block> SPIRIT_VEIN_SPRING =
            BLOCKS.register("spirit_vein_spring", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.WATER).strength(3.0f).requiresCorrectToolForDrops().randomTicks()));

    // ── 灵石矿矿石（4级） ──
    public static final RegistryObject<Block> SUPREME_SPIRIT_STONE_ORE =
            BLOCKS.register("supreme_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.5f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> HIGH_SPIRIT_STONE_ORE =
            BLOCKS.register("high_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> MID_SPIRIT_STONE_ORE =
            BLOCKS.register("mid_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.5f).requiresCorrectToolForDrops()));
    public static final RegistryObject<Block> LOW_SPIRIT_STONE_ORE =
            BLOCKS.register("low_spirit_stone_ore", () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(1.5f).requiresCorrectToolForDrops()));

    // ── 阵法核心盘（4级） ──
    public static final RegistryObject<FormationCorePlateBlock> FORMATION_CORE_PLATE_LOW =
            BLOCKS.register("formation_core_plate_low", () -> new FormationCorePlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(3.5f).requiresCorrectToolForDrops().noOcclusion(), CoreTier.LOW));
    public static final RegistryObject<FormationCorePlateBlock> FORMATION_CORE_PLATE_MID =
            BLOCKS.register("formation_core_plate_mid", () -> new FormationCorePlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0f).requiresCorrectToolForDrops().noOcclusion(), CoreTier.MID));
    public static final RegistryObject<FormationCorePlateBlock> FORMATION_CORE_PLATE_HIGH =
            BLOCKS.register("formation_core_plate_high", () -> new FormationCorePlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.5f).requiresCorrectToolForDrops().noOcclusion(), CoreTier.HIGH));
    public static final RegistryObject<FormationCorePlateBlock> FORMATION_CORE_PLATE_SUPREME =
            BLOCKS.register("formation_core_plate_supreme", () -> new FormationCorePlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(5.5f).requiresCorrectToolForDrops().noOcclusion(), CoreTier.SUPREME));
    public static final RegistryObject<FormationCorePlateBlock> FORMATION_CORE_PLATE_IMMORTAL =
            BLOCKS.register("formation_core_plate_immortal", () -> new FormationCorePlateBlock(BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(7.0f).requiresCorrectToolForDrops().noOcclusion(), CoreTier.IMMORTAL));

    // ── 阵法雾符 ──
    public static final RegistryObject<FormationRuneBlock> FORMATION_RUNE =
            BLOCKS.register("formation_rune", () -> new FormationRuneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(2.0f).noOcclusion()));

    // ── 灵草 ──
    public static final RegistryObject<Block> HERB =
            BLOCKS.register("herb", () -> new HerbBlock(BlockBehaviour.Properties.copy(Blocks.GRASS).noCollission()));

    // ── 宗门护盾屏障（隐形） ──
    public static final RegistryObject<SectProtectionBarrierBlock> SECT_PROTECTION_BARRIER =
            BLOCKS.register("sect_protection_barrier", () -> new SectProtectionBarrierBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(-1.0f, 3600000.0f).noOcclusion().noLootTable()));

    // ── 灵脉核心 ──
    public static final RegistryObject<SpiritVeinCoreBlock> SPIRIT_VEIN_CORE =
            BLOCKS.register("spirit_vein_core", () -> new SpiritVeinCoreBlock(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_CYAN).strength(3.0f).requiresCorrectToolForDrops().noOcclusion()));

    // ── 聚灵阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_QI_GATHERING_FLAG = BLOCKS.register("low_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_QI_GATHERING_FLAG = BLOCKS.register("mid_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_QI_GATHERING_FLAG = BLOCKS.register("high_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_QI_GATHERING_FLAG = BLOCKS.register("supreme_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_QI_GATHERING_FLAG = BLOCKS.register("immortal_qi_gathering_flag", () -> new QiGatheringFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 宗门守护阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_SECT_PROTECTION_FLAG = BLOCKS.register("low_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_SECT_PROTECTION_FLAG = BLOCKS.register("mid_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_SECT_PROTECTION_FLAG = BLOCKS.register("high_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_SECT_PROTECTION_FLAG = BLOCKS.register("supreme_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_SECT_PROTECTION_FLAG = BLOCKS.register("immortal_sect_protection_flag", () -> new SectProtectionFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 枯萎生长阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_WITHER_GROWTH_FLAG = BLOCKS.register("low_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_WITHER_GROWTH_FLAG = BLOCKS.register("mid_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_WITHER_GROWTH_FLAG = BLOCKS.register("high_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_WITHER_GROWTH_FLAG = BLOCKS.register("supreme_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_WITHER_GROWTH_FLAG = BLOCKS.register("immortal_wither_growth_flag", () -> new WitherGrowthFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 复苏阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_REJUVENATION_FLAG = BLOCKS.register("low_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_REJUVENATION_FLAG = BLOCKS.register("mid_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_REJUVENATION_FLAG = BLOCKS.register("high_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_REJUVENATION_FLAG = BLOCKS.register("supreme_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_REJUVENATION_FLAG = BLOCKS.register("immortal_rejuvenation_flag", () -> new RejuvenationFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 禁飞阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_FLIGHT_BAN_FLAG = BLOCKS.register("low_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_FLIGHT_BAN_FLAG = BLOCKS.register("mid_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_FLIGHT_BAN_FLAG = BLOCKS.register("high_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_FLIGHT_BAN_FLAG = BLOCKS.register("supreme_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_FLIGHT_BAN_FLAG = BLOCKS.register("immortal_flight_ban_flag", () -> new FlightBanFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 迷宫阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_MAZE_FLAG = BLOCKS.register("low_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_MAZE_FLAG = BLOCKS.register("mid_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_MAZE_FLAG = BLOCKS.register("high_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_MAZE_FLAG = BLOCKS.register("supreme_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_MAZE_FLAG = BLOCKS.register("immortal_maze_flag", () -> new MazeFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 灵田收割阵旗（5 品级） ──
    public static final RegistryObject<Block> LOW_FARM_HARVEST_FLAG = BLOCKS.register("low_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.LOW));
    public static final RegistryObject<Block> MID_FARM_HARVEST_FLAG = BLOCKS.register("mid_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.MID));
    public static final RegistryObject<Block> HIGH_FARM_HARVEST_FLAG = BLOCKS.register("high_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.HIGH));
    public static final RegistryObject<Block> SUPREME_FARM_HARVEST_FLAG = BLOCKS.register("supreme_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_FARM_HARVEST_FLAG = BLOCKS.register("immortal_farm_harvest_flag", () -> new FarmHarvestFlagBlock(ModBlocks.flagProps(), ItemTier.IMMORTAL));

    // ── 灵脉核心（5 品级） ──
    public static final RegistryObject<Block> LOW_SPIRIT_VEIN_CORE = BLOCKS.register("low_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.LOW), SpiritVeinCoreTier.LOW));
    public static final RegistryObject<Block> MID_SPIRIT_VEIN_CORE = BLOCKS.register("mid_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.MID), SpiritVeinCoreTier.MID));
    public static final RegistryObject<Block> HIGH_SPIRIT_VEIN_CORE = BLOCKS.register("high_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.HIGH), SpiritVeinCoreTier.HIGH));
    public static final RegistryObject<Block> SUPREME_SPIRIT_VEIN_CORE = BLOCKS.register("supreme_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.SUPREME), SpiritVeinCoreTier.SUPREME));
    public static final RegistryObject<Block> IMMORTAL_SPIRIT_VEIN_CORE = BLOCKS.register("immortal_spirit_vein_core", () -> new SpiritVeinCoreBlock(ModBlocks.spiritVeinCoreProps(SpiritVeinCoreTier.IMMORTAL), SpiritVeinCoreTier.IMMORTAL));

    // ── 骸骨残骸 / 骸骨方块 ──
    public static final RegistryObject<Block> BONE_REMAINS = BLOCKS.register("bone_remains", () -> new BoneRemainsBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(0.6f).sound(SoundType.WET_GRASS).noOcclusion().randomTicks().pushReaction(PushReaction.DESTROY)));
    public static final RegistryObject<Block> BONE_BLOCK = BLOCKS.register("bone_block", () -> new BoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.SAND).strength(1.5f).sound(SoundType.WET_GRASS).requiresCorrectToolForDrops().noOcclusion()));

    private static BlockBehaviour.Properties flagProps() {
        return BlockBehaviour.Properties.of().mapColor(MapColor.GRASS).strength(0.5f, 1.0f).sound(SoundType.WOOL).noOcclusion();
    }

    private static BlockBehaviour.Properties spiritVeinCoreProps(SpiritVeinCoreTier tier) {
        int light = switch (tier) {
            case LOW -> 8;
            case MID -> 10;
            case HIGH -> 12;
            case SUPREME -> 14;
            case IMMORTAL -> 15;
        };
        return BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_LIGHT_BLUE).strength(4.0f, 8.0f).lightLevel(state -> light).sound(SoundType.VINE).requiresCorrectToolForDrops();
    }

    private ModBlocks() {}
    public static void register(IEventBus bus) { BLOCKS.register(bus); }
}
