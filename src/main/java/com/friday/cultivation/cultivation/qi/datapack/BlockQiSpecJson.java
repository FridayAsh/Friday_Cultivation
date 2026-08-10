/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  net.minecraft.core.registries.BuiltInRegistries
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.level.block.Block
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi.datapack;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockDegradeRule;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.BlockUpgradeRule;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

public final class BlockQiSpecJson {
    private BlockQiSpecJson() {
    }

    public static BlockQiSpec parse(JsonElement element, ResourceLocation specId) {
        if (!element.isJsonObject()) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] root must be an object");
        }
        JsonObject obj = element.getAsJsonObject();
        QiElement el = BlockQiSpecJson.parseElement(BlockQiSpecJson.reqString(obj, "element", specId), specId);
        int maxQi = BlockQiSpecJson.reqInt(obj, "max_qi", specId);
        double regen = BlockQiSpecJson.reqDouble(obj, "regen_per_sec", specId);
        double emit = BlockQiSpecJson.reqDouble(obj, "emit_rate", specId);
        if (emit < 0.0 || emit > 1.0) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] emit_rate must be 0~1");
        }
        BlockDegradeRule degrade = obj.has("degrade") ? BlockQiSpecJson.parseDegrade(obj.getAsJsonObject("degrade"), specId) : null;
        BlockUpgradeRule upgrade = obj.has("upgrade") ? BlockQiSpecJson.parseUpgrade(obj.getAsJsonObject("upgrade"), specId) : null;
        return new BlockQiSpec(el, maxQi, regen, emit, degrade, upgrade);
    }

    private static BlockDegradeRule parseDegrade(JsonObject obj, ResourceLocation specId) {
        int threshold = BlockQiSpecJson.reqInt(obj, "drain_threshold", specId);
        Block target = BlockQiSpecJson.parseBlock(BlockQiSpecJson.reqString(obj, "to", specId), specId);
        double chance = BlockQiSpecJson.reqDouble(obj, "chance", specId);
        return new BlockDegradeRule(threshold, target, chance);
    }

    private static BlockUpgradeRule parseUpgrade(JsonObject obj, ResourceLocation specId) {
        int idle = BlockQiSpecJson.reqInt(obj, "idle_ticks", specId);
        Block target = BlockQiSpecJson.parseBlock(BlockQiSpecJson.reqString(obj, "to", specId), specId);
        double chance = BlockQiSpecJson.reqDouble(obj, "chance", specId);
        return new BlockUpgradeRule(idle, target, chance);
    }

    @Nullable
    private static Block parseBlock(String id, ResourceLocation specId) {
        ResourceLocation rl = ResourceLocation.tryParse((String)id);
        if (rl == null) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] invalid block id: " + id);
        }
        Block block = (Block)BuiltInRegistries.BLOCK.get(rl);
        if (block == BuiltInRegistries.BLOCK.get(BuiltInRegistries.BLOCK.getDefaultKey()) && !rl.equals((Object)BuiltInRegistries.BLOCK.getDefaultKey())) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] unknown block: " + id);
        }
        return block;
    }

    private static QiElement parseElement(String s, ResourceLocation specId) {
        try {
            return QiElement.valueOf(s.toUpperCase());
        }
        catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] invalid element: " + s + " (valid: METAL/WOOD/WATER/FIRE/EARTH/ICE/LIGHTNING/PURE)");
        }
    }

    private static String reqString(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] missing string field: " + key);
        }
        return obj.get(key).getAsString();
    }

    private static int reqInt(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] missing int field: " + key);
        }
        return obj.get(key).getAsInt();
    }

    private static double reqDouble(JsonObject obj, String key, ResourceLocation specId) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            throw new IllegalArgumentException("[qi_specs/" + String.valueOf(specId) + "] missing double field: " + key);
        }
        return obj.get(key).getAsDouble();
    }
}

