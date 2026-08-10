package com.friday.cultivation.alchemy.datapack;

import com.friday.cultivation.alchemy.PillEffectSpec;
import com.friday.cultivation.alchemy.PillEffectSpecs;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PillEffectSpecLoader extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "pill_effects";
    private static final Gson GSON = new Gson();
    private static final Logger LOGGER = LoggerFactory.getLogger("PillEffectSpecLoader");

    public PillEffectSpecLoader() { super(GSON, DIRECTORY); }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager manager, @NotNull ProfilerFiller profiler) {
        PillEffectSpecs.reset();
        int loaded = 0, failed = 0;
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation itemId = entry.getKey();
            try {
                Item item = ForgeRegistries.ITEMS.getValue(itemId);
                if (item == null || item == Items.AIR) { LOGGER.warn("[pill_effects] skipped {}: no such item", itemId); ++failed; continue; }
                PillEffectSpec spec = PillEffectSpec.fromJson(GsonHelper.convertToJsonObject(entry.getValue(), "pill effect spec"));
                PillEffectSpecs.override(item, spec); ++loaded;
            } catch (Exception e) { LOGGER.warn("[pill_effects] skipped {}: {}", itemId, e.getMessage()); ++failed; }
        }
        LOGGER.info("[pill_effects] loaded {} specs ({} failed)", loaded, failed);
    }
}
