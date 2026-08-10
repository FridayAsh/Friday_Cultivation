/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.Gson
 *  com.google.gson.JsonElement
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.ResourceManager
 *  net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener
 *  net.minecraft.util.GsonHelper
 *  net.minecraft.util.profiling.ProfilerFiller
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Items
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.cultivation.alchemy.datapack;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.friday.cultivation.FridayCultivationMod;
import com.friday.cultivation.cultivation.alchemy.PillEffectSpec;
import com.friday.cultivation.cultivation.alchemy.PillEffectSpecs;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

public class PillEffectSpecLoader
extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "pill_effects";
    private static final Gson GSON = new Gson();

    public PillEffectSpecLoader() {
        super(GSON, DIRECTORY);
    }

    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        PillEffectSpecs.reset();
        int loaded = 0;
        int failed = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation itemId = entry.getKey();
            try {
                Item item = (Item)ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null || item == Items.AIR) {
                    FridayCultivationMod.LOGGER.warn("[pill_effects] skipped {}: no such item", (Object)itemId);
                    ++failed;
                    continue;
                }
                PillEffectSpec spec = PillEffectSpec.fromJson(GsonHelper.convertToJsonObject((JsonElement)entry.getValue(), (String)"pill effect spec"));
                PillEffectSpecs.override(item, spec);
                ++loaded;
            }
            catch (Exception e) {
                FridayCultivationMod.LOGGER.warn("[pill_effects] skipped {}: {}", (Object)itemId, (Object)e.getMessage());
                ++failed;
            }
        }
        FridayCultivationMod.LOGGER.info("[pill_effects] loaded {} specs ({} failed)", (Object)loaded, (Object)failed);
    }
}

