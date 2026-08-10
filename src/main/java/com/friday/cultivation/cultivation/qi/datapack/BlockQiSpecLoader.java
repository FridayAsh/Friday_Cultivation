/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
 *  net.minecraft.util.profiling.ProfilerFiller
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.cultivation.qi.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.friday.cultivation.FridayCultivationMod;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.cultivation.qi.datapack.BlockQiSpecJson;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;

public class BlockQiSpecLoader
extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "qi_specs";
    private static final Gson GSON = new Gson();

    public BlockQiSpecLoader() {
        super(GSON, DIRECTORY);
    }

    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager rm, @NotNull ProfilerFiller profiler) {
        BlockQiSpecs.resetToDefaults();
        HashMap<Block, BlockQiSpec> overrides = new HashMap<Block, BlockQiSpec>();
        int failed = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation specId = entry.getKey();
            try {
                Block block = (Block)BuiltInRegistries.BLOCK.get(specId);
                if (block == Blocks.AIR && !specId.equals(BuiltInRegistries.BLOCK.getKey(Blocks.AIR))) {
                    FridayCultivationMod.LOGGER.warn("[qi_specs] skipped {}: no such block", (Object)specId);
                    ++failed;
                    continue;
                }
                BlockQiSpec spec = BlockQiSpecJson.parse(entry.getValue(), specId);
                overrides.put(block, spec);
            }
            catch (Exception e) {
                FridayCultivationMod.LOGGER.warn("[qi_specs] skipped {}: {}", (Object)specId, (Object)e.getMessage());
                ++failed;
            }
        }
        for (Map.Entry<Block, BlockQiSpec> entry : overrides.entrySet()) {
            BlockQiSpecs.override(entry.getKey(), entry.getValue());
        }
        FridayCultivationMod.LOGGER.info("[qi_specs] loaded {} datapack overrides ({} failed)", (Object)overrides.size(), (Object)failed);
    }
}

