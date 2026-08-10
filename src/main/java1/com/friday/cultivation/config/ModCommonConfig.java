package com.friday.cultivation.config;

import net.minecraftforge.common.ForgeConfigSpec;

/**
 * 通用配置（服务端）
 */
public class ModCommonConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue CULTIVATION_SPEED_MULTIPLIER;
    public static final ForgeConfigSpec.BooleanValue ENABLE_REALM_PRESSURE;
    public static final ForgeConfigSpec.BooleanValue ENABLE_TRIBULATION;
    public static final ForgeConfigSpec.BooleanValue OFFLINE_AUTH_ENABLED;
    public static final ForgeConfigSpec.IntValue OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS;

    // ── 完整复刻原模组 ModCommonConfig 的配置项 ──
    public static final ForgeConfigSpec.BooleanValue ALLOW_CUSTOM_START_SCREEN;
    public static final ForgeConfigSpec.BooleanValue SPELL_TERRAIN_DESTRUCTION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED;
    public static final ForgeConfigSpec.DoubleValue SECT_SETTLEMENT_CELL_SPAWN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue SECT_TREED_BIOME_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue SECT_WORLDGEN_SLOW_LOG_MS;
    public static final ForgeConfigSpec.IntValue NPC_CORPSE_DECAY_DAYS;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("修仙系统通用配置");
        builder.push("general");

        CULTIVATION_SPEED_MULTIPLIER = builder
                .comment("修炼速度倍率（百分比，100 = 正常速度）")
                .defineInRange("cultivationSpeedMultiplier", 100, 1, 1000);

        ENABLE_REALM_PRESSURE = builder
                .comment("是否启用境界压制")
                .define("enableRealmPressure", true);

        ENABLE_TRIBULATION = builder
                .comment("是否启用天劫系统")
                .define("enableTribulation", true);

        OFFLINE_AUTH_ENABLED = builder
                .comment("是否启用离线登录验证（玩家上线须 register / login 才能行动）")
                .define("offlineAuthEnabled", false);

        OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS = builder
                .comment("离线登录验证超时秒数（0 = 不限时）")
                .defineInRange("offlineAuthLoginTimeoutSeconds", 120, 0, 3600);

        builder.pop();

        builder.push("startup");
        ALLOW_CUSTOM_START_SCREEN = builder
                .comment("Controls the first-time origin flow for players who do not yet have an identity.",
                        "true = open the custom origin screen after the server allows normal gameplay.",
                        "false = automatically roll a random identity, spirit root, and physique instead of opening the screen.",
                        "When offlineAuth.enabled is true, both the screen and automatic random start wait until /register or /login succeeds.")
                .define("allowCustomStartScreen", true);
        builder.pop();

        builder.push("spells");
        SPELL_TERRAIN_DESTRUCTION_ENABLED = builder
                .comment("Default spell terrain destruction preference for players who have not changed it yet.",
                        "true = new/uninitialized players start with spell block destruction enabled.",
                        "false = new/uninitialized players start with spell block destruction disabled.",
                        "Players can still toggle their own preference unless spellTerrainDestructionForceDisabled is true.")
                .define("spellTerrainDestructionEnabled", true);
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED = builder
                .comment("Server-side hard lock for spell-caused terrain changes.",
                        "true = all spell block destruction/fire/crater changes are always disabled, and player GUI toggles are locked.",
                        "false = each player uses the default above until they manually toggle their own preference.",
                        "This is enforced on the server side; client-side config changes cannot bypass it.")
                .define("spellTerrainDestructionForceDisabled", false);
        builder.pop();

        builder.push("worldgen");
        SECT_SETTLEMENT_CELL_SPAWN_CHANCE = builder
                .comment("Chance for each 32x32-chunk sect generation cell to host one sect settlement.",
                        "Lower values make new terrain load faster and greatly reduce sect density.",
                        "1.0 means every valid sect cell generates a sect candidate.")
                .defineInRange("sectSettlementCellSpawnChance", 0.34, 0.0, 1.0);
        SECT_TREED_BIOME_SPAWN_CHANCE = builder
                .comment("Relative chance for a sect to still generate when its site is NOT an open/treeless",
                        "land biome -- i.e. treed biomes (forest, jungle, taiga, swamp, etc.) AND water",
                        "biomes (ocean, river).",
                        "Sects are now allowed in EVERY biome (including the open sea); open/treeless sites",
                        "use the full chance, fully-treed/water sites use this fraction, and mixed sites scale",
                        "in between.",
                        "0.60 keeps forest/ocean sects less common than open land while making them more discoverable.",
                        "1.0 = every biome is equally likely; 0.0 = only open/treeless biomes spawn sects.")
                .defineInRange("sectTreedBiomeSpawnChance", 0.6, 0.0, 1.0);
        SECT_WORLDGEN_SLOW_LOG_MS = builder
                .comment("Logs a warning when one sect settlement feature placement takes at least this many milliseconds.",
                        "Set to 0 to disable slow sect worldgen logging.")
                .defineInRange("sectWorldgenSlowLogMs", 80, 0, 10000);
        builder.pop();

        builder.push("entities");
        NPC_CORPSE_DECAY_DAYS = builder
                .comment("In-game days an NPC corpse stays before it decays into a skeleton skull + bone block.",
                        "Corpses are entities; keeping them around very long (with many deaths) costs more",
                        "performance than the cheap remains blocks, so lower this if corpse piles cause lag.",
                        "Meditation time-acceleration also speeds up decay (x10 meditation -> 0.3 days).",
                        "1 day = 24000 ticks. Default 3 days.")
                .defineInRange("npcCorpseDecayDays", 3, 1, 3650);
        builder.pop();

        SPEC = builder.build();
    }

    public static boolean offlineAuthEnabled() {
        return OFFLINE_AUTH_ENABLED.get();
    }

    public static int offlineAuthLoginTimeoutSeconds() {
        return OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS.get();
    }

    public static boolean allowCustomStartScreen() {
        return ALLOW_CUSTOM_START_SCREEN.get();
    }

    public static void setAllowCustomStartScreen(boolean value) {
        ALLOW_CUSTOM_START_SCREEN.set(value);
        ALLOW_CUSTOM_START_SCREEN.save();
    }

    public static boolean spellTerrainDestructionEnabled() {
        return SPELL_TERRAIN_DESTRUCTION_ENABLED.get();
    }

    public static boolean spellTerrainDestructionDefaultEnabled() {
        return SPELL_TERRAIN_DESTRUCTION_ENABLED.get();
    }

    public static void setSpellTerrainDestructionEnabled(boolean value) {
        SPELL_TERRAIN_DESTRUCTION_ENABLED.set(value);
        SPELL_TERRAIN_DESTRUCTION_ENABLED.save();
    }

    public static void setSpellTerrainDestructionDefaultEnabled(boolean value) {
        ModCommonConfig.setSpellTerrainDestructionEnabled(value);
    }

    public static boolean spellTerrainDestructionForceDisabled() {
        return SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.get();
    }

    public static void setSpellTerrainDestructionForceDisabled(boolean value) {
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.set(value);
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.save();
    }

    public static double sectSettlementCellSpawnChance() {
        return SECT_SETTLEMENT_CELL_SPAWN_CHANCE.get();
    }

    public static double sectTreedBiomeSpawnChance() {
        return SECT_TREED_BIOME_SPAWN_CHANCE.get();
    }

    public static boolean migrateLegacySectSpawnDensityDefaults() {
        boolean changed = false;
        double cellChance = SECT_SETTLEMENT_CELL_SPAWN_CHANCE.get();
        if (ModCommonConfig.isLegacySectCellChance(cellChance)) {
            SECT_SETTLEMENT_CELL_SPAWN_CHANCE.set(0.34);
            SECT_SETTLEMENT_CELL_SPAWN_CHANCE.save();
            changed = true;
        }
        double treedChance = SECT_TREED_BIOME_SPAWN_CHANCE.get();
        if (ModCommonConfig.isLegacySectTreedChance(treedChance)) {
            SECT_TREED_BIOME_SPAWN_CHANCE.set(0.6);
            SECT_TREED_BIOME_SPAWN_CHANCE.save();
            changed = true;
        }
        return changed;
    }

    private static boolean isLegacySectCellChance(double value) {
        return ModCommonConfig.nearlyEquals(value, 0.18) || ModCommonConfig.nearlyEquals(value, 0.24) || ModCommonConfig.nearlyEquals(value, 0.3);
    }

    private static boolean isLegacySectTreedChance(double value) {
        return ModCommonConfig.nearlyEquals(value, 0.35) || ModCommonConfig.nearlyEquals(value, 0.45) || ModCommonConfig.nearlyEquals(value, 0.55);
    }

    private static boolean nearlyEquals(double a, double b) {
        return Math.abs(a - b) < 1.0E-6;
    }

    public static int sectWorldgenSlowLogMs() {
        return SECT_WORLDGEN_SLOW_LOG_MS.get();
    }

    public static int npcCorpseDecayTicks() {
        return NPC_CORPSE_DECAY_DAYS.get() * 24000;
    }
}
