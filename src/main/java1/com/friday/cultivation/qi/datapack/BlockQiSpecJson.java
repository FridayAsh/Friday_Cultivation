package com.friday.cultivation.qi.datapack;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.qi.BlockDegradeRule;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockUpgradeRule;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

/**
 * 方块灵气规格 JSON 解析（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.datapack.BlockQiSpecJson）。
 * <p>用于 {@link BlockQiSpecLoader} 在 datapack 重载时把 <code>data/&lt;modid&gt;/qi_specs/*.json</code>
 * 解析为 {@link BlockQiSpec}。支持的字段：element、max_qi、regen_per_sec、emit_rate、degrade、upgrade。</p>
 */
public final class BlockQiSpecJson {
    private BlockQiSpecJson() {
    }

    public static BlockQiSpec parse(JsonElement element, ResourceLocation specId) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] root must be an object");
        }
        JsonObject obj = element.getAsJsonObject();
        QiElement el = parseElement(reqString(obj, "element", specId), specId);
        int maxQi = reqInt(obj, "max_qi", specId);
        double regen = reqDouble(obj, "regen_per_sec", specId);
        double emit = reqDouble(obj, "emit_rate", specId);
        if (emit < 0.0 || emit > 1.0) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] emit_rate must be 0~1");
        }
        BlockDegradeRule degrade = obj.has("degrade") ? parseDegrade(obj.getAsJsonObject("degrade"), specId) : null;
        BlockUpgradeRule upgrade = obj.has("upgrade") ? parseUpgrade(obj.getAsJsonObject("upgrade"), specId) : null;
        return new BlockQiSpec(el, maxQi, regen, emit, degrade, upgrade);
    }

    private static BlockDegradeRule parseDegrade(JsonObject obj, ResourceLocation specId) {
        int threshold = reqInt(obj, "drain_threshold", specId);
        Block target = parseBlock(reqString(obj, "to", specId), specId);
        double chance = reqDouble(obj, "chance", specId);
        return new BlockDegradeRule(threshold, target, chance);
    }

    private static BlockUpgradeRule parseUpgrade(JsonObject obj, ResourceLocation specId) {
        int idle = reqInt(obj, "idle_ticks", specId);
        Block target = parseBlock(reqString(obj, "to", specId), specId);
        double chance = reqDouble(obj, "chance", specId);
        return new BlockUpgradeRule(idle, target, chance);
    }

    @Nullable
    private static Block parseBlock(String id, ResourceLocation specId) {
        ResourceLocation rl = ResourceLocation.tryParse(id);
        if (rl == null) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] invalid block id: " + id);
        }
        Block block = BuiltInRegistries.BLOCK.getOptional(rl).orElse(Blocks.AIR);
        ResourceLocation airLoc = BuiltInRegistries.BLOCK.getKey(Blocks.AIR);
        if (block == Blocks.AIR && !rl.equals(airLoc)) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] unknown block: " + id);
        }
        return block;
    }

    private static QiElement parseElement(String s, ResourceLocation specId) {
        try {
            return QiElement.valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] invalid element: " + s
                    + " (valid: METAL/WOOD/WATER/FIRE/EARTH/ICE/LIGHTNING/PURE)");
        }
    }

    private static String reqString(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] missing string field: " + key);
        }
        return obj.get(key).getAsString();
    }

    private static int reqInt(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] missing int field: " + key);
        }
        return obj.get(key).getAsInt();
    }

    private static double reqDouble(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + specId + "] missing double field: " + key);
        }
        return obj.get(key).getAsDouble();
    }
}
