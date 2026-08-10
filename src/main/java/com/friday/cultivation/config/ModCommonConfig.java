/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraftforge.common.ForgeConfigSpec
 *  net.minecraftforge.common.ForgeConfigSpec$BooleanValue
 *  net.minecraftforge.common.ForgeConfigSpec$Builder
 *  net.minecraftforge.common.ForgeConfigSpec$DoubleValue
 *  net.minecraftforge.common.ForgeConfigSpec$IntValue
 */
package com.friday.cultivation.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModCommonConfig {
    private static final double DEFAULT_SECT_SETTLEMENT_CELL_SPAWN_CHANCE = 0.34;
    private static final double DEFAULT_SECT_TREED_BIOME_SPAWN_CHANCE = 0.6;
    private static final double CONFIG_DOUBLE_EPSILON = 1.0E-6;
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.BooleanValue ALLOW_CUSTOM_START_SCREEN;
    public static final ForgeConfigSpec.BooleanValue SPELL_TERRAIN_DESTRUCTION_ENABLED;
    public static final ForgeConfigSpec.BooleanValue SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED;
    public static final ForgeConfigSpec.BooleanValue OFFLINE_AUTH_ENABLED;
    public static final ForgeConfigSpec.IntValue OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS;
    public static final ForgeConfigSpec.DoubleValue SECT_SETTLEMENT_CELL_SPAWN_CHANCE;
    public static final ForgeConfigSpec.DoubleValue SECT_TREED_BIOME_SPAWN_CHANCE;
    public static final ForgeConfigSpec.IntValue SECT_WORLDGEN_SLOW_LOG_MS;
    public static final ForgeConfigSpec.IntValue NPC_CORPSE_DECAY_DAYS;

    private ModCommonConfig() {
    }

    public static boolean allowCustomStartScreen() {
        return (Boolean)ALLOW_CUSTOM_START_SCREEN.get();
    }

    public static void setAllowCustomStartScreen(boolean value) {
        ALLOW_CUSTOM_START_SCREEN.set(value);
        ALLOW_CUSTOM_START_SCREEN.save();
    }

    public static boolean spellTerrainDestructionEnabled() {
        return (Boolean)SPELL_TERRAIN_DESTRUCTION_ENABLED.get();
    }

    public static boolean spellTerrainDestructionDefaultEnabled() {
        return (Boolean)SPELL_TERRAIN_DESTRUCTION_ENABLED.get();
    }

    public static void setSpellTerrainDestructionEnabled(boolean value) {
        SPELL_TERRAIN_DESTRUCTION_ENABLED.set(value);
        SPELL_TERRAIN_DESTRUCTION_ENABLED.save();
    }

    public static void setSpellTerrainDestructionDefaultEnabled(boolean value) {
        ModCommonConfig.setSpellTerrainDestructionEnabled(value);
    }

    public static boolean spellTerrainDestructionForceDisabled() {
        return (Boolean)SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.get();
    }

    public static void setSpellTerrainDestructionForceDisabled(boolean value) {
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.set(value);
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED.save();
    }

    public static boolean offlineAuthEnabled() {
        return (Boolean)OFFLINE_AUTH_ENABLED.get();
    }

    public static int offlineAuthLoginTimeoutSeconds() {
        return (Integer)OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS.get();
    }

    public static double sectSettlementCellSpawnChance() {
        return (Double)SECT_SETTLEMENT_CELL_SPAWN_CHANCE.get();
    }

    public static double sectTreedBiomeSpawnChance() {
        return (Double)SECT_TREED_BIOME_SPAWN_CHANCE.get();
    }

    public static boolean migrateLegacySectSpawnDensityDefaults() {
        double treedChance;
        boolean changed = false;
        double cellChance = (Double)SECT_SETTLEMENT_CELL_SPAWN_CHANCE.get();
        if (ModCommonConfig.isLegacySectCellChance(cellChance)) {
            SECT_SETTLEMENT_CELL_SPAWN_CHANCE.set(0.34);
            SECT_SETTLEMENT_CELL_SPAWN_CHANCE.save();
            changed = true;
        }
        if (ModCommonConfig.isLegacySectTreedChance(treedChance = ((Double)SECT_TREED_BIOME_SPAWN_CHANCE.get()).doubleValue())) {
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
        return (Integer)SECT_WORLDGEN_SLOW_LOG_MS.get();
    }

    public static int npcCorpseDecayTicks() {
        return (Integer)NPC_CORPSE_DECAY_DAYS.get() * 24000;
    }

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        builder.push("startup");
        ALLOW_CUSTOM_START_SCREEN = builder.comment(new String[]{"Controls the first-time origin flow for players who do not yet have an identity.", "true = open the custom origin screen after the server allows normal gameplay.", "false = automatically roll a random identity, spirit root, and physique instead of opening the screen.", "When offlineAuth.enabled is true, both the screen and automatic random start wait until /register or /login succeeds."}).translation("config.friday_cultivation.allow_custom_start_screen").define("allowCustomStartScreen", true);
        builder.pop();
        builder.push("spells");
        SPELL_TERRAIN_DESTRUCTION_ENABLED = builder.comment(new String[]{"Default spell terrain destruction preference for players who have not changed it yet.", "true = new/uninitialized players start with spell block destruction enabled.", "false = new/uninitialized players start with spell block destruction disabled.", "Players can still toggle their own preference unless spellTerrainDestructionForceDisabled is true."}).translation("config.friday_cultivation.spell_terrain_destruction_enabled").define("spellTerrainDestructionEnabled", true);
        SPELL_TERRAIN_DESTRUCTION_FORCE_DISABLED = builder.comment(new String[]{"Server-side hard lock for spell-caused terrain changes.", "true = all Xiaoxiang spell block destruction/fire/crater changes are always disabled, and player GUI toggles are locked.", "false = each player uses the default above until they manually toggle their own preference.", "This is enforced on the server side; client-side config changes cannot bypass it."}).translation("config.friday_cultivation.spell_terrain_destruction_force_disabled").define("spellTerrainDestructionForceDisabled", false);
        builder.pop();
        builder.push("offlineAuth");
        OFFLINE_AUTH_ENABLED = builder.comment(new String[]{"Enables the built-in /register and /login gate for offline-mode servers.", "Keep this false for single-player, LAN, and online-mode servers.", "When enabled, players must authenticate before moving, chatting, using commands, or interacting."}).translation("config.friday_cultivation.offline_auth_enabled").define("enabled", false);
        OFFLINE_AUTH_LOGIN_TIMEOUT_SECONDS = builder.comment(new String[]{"Seconds before an unauthenticated player is kicked. Set to 0 to disable the timeout.", "The timeout only applies when offlineAuth.enabled is true."}).translation("config.friday_cultivation.offline_auth_login_timeout_seconds").defineInRange("loginTimeoutSeconds", 180, 0, 3600);
        builder.pop();
        builder.push("worldgen");
        SECT_SETTLEMENT_CELL_SPAWN_CHANCE = builder.comment(new String[]{"Chance for each 32x32-chunk sect generation cell to host one sect settlement.", "Lower values make new terrain load faster and greatly reduce sect density.", "1.0 means every valid sect cell generates a sect candidate."}).translation("config.friday_cultivation.sect_settlement_cell_spawn_chance").defineInRange("sectSettlementCellSpawnChance", 0.34, 0.0, 1.0);
        SECT_TREED_BIOME_SPAWN_CHANCE = builder.comment(new String[]{"Relative chance for a sect to still generate when its site is NOT an open/treeless", "land biome -- i.e. treed biomes (forest, jungle, taiga, swamp, etc.) AND water", "biomes (ocean, river).", "Sects are now allowed in EVERY biome (including the open sea); open/treeless sites", "use the full chance, fully-treed/water sites use this fraction, and mixed sites scale", "in between.", "0.60 keeps forest/ocean sects less common than open land while making them more discoverable.", "1.0 = every biome is equally likely; 0.0 = only open/treeless biomes spawn sects."}).translation("config.friday_cultivation.sect_treed_biome_spawn_chance").defineInRange("sectTreedBiomeSpawnChance", 0.6, 0.0, 1.0);
        SECT_WORLDGEN_SLOW_LOG_MS = builder.comment(new String[]{"Logs a warning when one sect settlement feature placement takes at least this many milliseconds.", "Set to 0 to disable slow sect worldgen logging."}).translation("config.friday_cultivation.sect_worldgen_slow_log_ms").defineInRange("sectWorldgenSlowLogMs", 80, 0, 10000);
        builder.pop();
        builder.push("entities");
        NPC_CORPSE_DECAY_DAYS = builder.comment(new String[]{"In-game days an NPC corpse stays before it decays into a skeleton skull + bone block.", "Corpses are entities; keeping them around very long (with many deaths) costs more", "performance than the cheap remains blocks, so lower this if corpse piles cause lag.", "Meditation time-acceleration also speeds up decay (x10 meditation -> 0.3 days).", "1 day = 24000 ticks. Default 3 days."}).translation("config.friday_cultivation.npc_corpse_decay_days").defineInRange("npcCorpseDecayDays", 3, 1, 3650);
        builder.pop();
        SPEC = builder.build();
    }
}

