package com.friday.cultivation.qi.datapack;

import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.datapack.BlockQiSpecJson;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.mojang.logging.LogUtils;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

/**
 * 方块灵气规格 datapack 加载器（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.datapack.BlockQiSpecLoader）。
 * <p>扫描 <code>data/&lt;modid&gt;/qi_specs/*.json</code>，解析后调用 {@link BlockQiSpecs#override}。</p>
 */
public class BlockQiSpecLoader extends SimpleJsonResourceReloadListener {
    public static final String DIRECTORY = "qi_specs";
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Gson GSON = new Gson();

    public BlockQiSpecLoader() {
        super(GSON, DIRECTORY);
    }

    @Override
    protected void apply(@NotNull Map<ResourceLocation, JsonElement> jsons, @NotNull ResourceManager rm, @NotNull ProfilerFiller profiler) {
        BlockQiSpecs.resetToDefaults();
        HashMap<Block, BlockQiSpec> overrides = new HashMap<>();
        int failed = 0;
        ResourceLocation airKey = BuiltInRegistries.BLOCK.getKey(Blocks.AIR);
        for (Map.Entry<ResourceLocation, JsonElement> entry : jsons.entrySet()) {
            ResourceLocation specId = entry.getKey();
            try {
                Block block = BuiltInRegistries.BLOCK.getOptional(specId).orElse(Blocks.AIR);
                if (block == Blocks.AIR && !specId.equals(airKey)) {
                    LOGGER.warn("[qi_specs] skipped {}: no such block", specId);
                    failed++;
                    continue;
                }
                BlockQiSpec spec = BlockQiSpecJson.parse(entry.getValue(), specId);
                overrides.put(block, spec);
            } catch (Exception e) {
                LOGGER.warn("[qi_specs] skipped {}: {}", specId, e.getMessage());
                failed++;
            }
        }
        for (Map.Entry<Block, BlockQiSpec> entry : overrides.entrySet()) {
            BlockQiSpecs.override(entry.getKey(), entry.getValue());
        }
        LOGGER.info("[qi_specs] loaded {} datapack overrides ({} failed)", overrides.size(), failed);
    }
}
