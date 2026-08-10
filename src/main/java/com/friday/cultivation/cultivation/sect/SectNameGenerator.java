/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.google.gson.JsonArray
 *  com.google.gson.JsonElement
 *  com.google.gson.JsonObject
 *  com.google.gson.JsonParser
 *  net.minecraft.util.RandomSource
 */
package com.friday.cultivation.cultivation.sect;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

public final class SectNameGenerator {
    private static final String RESOURCE_PATH = "/data/friday_cultivation/sect/name_pool.json";
    private static final NamePool POOL = SectNameGenerator.loadPool();

    private SectNameGenerator() {
    }

    public static String randomName(long seed) {
        RandomSource random = RandomSource.create((long)(seed ^ 0x5EC7A11B71L));
        if (!SectNameGenerator.POOL.fullNames.isEmpty() && random.nextInt(4) == 0) {
            return SectNameGenerator.POOL.fullNames.get(random.nextInt(SectNameGenerator.POOL.fullNames.size()));
        }
        if (!SectNameGenerator.POOL.prefixes.isEmpty() && !SectNameGenerator.POOL.suffixes.isEmpty()) {
            String prefix = SectNameGenerator.POOL.prefixes.get(random.nextInt(SectNameGenerator.POOL.prefixes.size()));
            String suffix = SectNameGenerator.POOL.suffixes.get(random.nextInt(SectNameGenerator.POOL.suffixes.size()));
            return prefix + suffix;
        }
        return "sect_" + Long.toUnsignedString(seed, 36);
    }

    /*
     * Enabled aggressive block sorting
     * Enabled unnecessary exception pruning
     * Enabled aggressive exception aggregation
     */
    private static NamePool loadPool() {
        try (InputStream in = SectNameGenerator.class.getResourceAsStream(RESOURCE_PATH);){
            if (in == null) {
                NamePool namePool2 = NamePool.EMPTY;
                return namePool2;
            }
            JsonObject root = JsonParser.parseReader((Reader)new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            NamePool namePool = new NamePool(SectNameGenerator.readStringArray(root, "full_names"), SectNameGenerator.readStringArray(root, "prefixes"), SectNameGenerator.readStringArray(root, "suffixes"));
            return namePool;
        }
        catch (IOException | RuntimeException ignored) {
            return NamePool.EMPTY;
        }
    }

    private static List<String> readStringArray(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonArray()) {
            return List.of();
        }
        JsonArray array = root.getAsJsonArray(key);
        ArrayList<String> values = new ArrayList<String>();
        for (JsonElement element : array) {
            String value;
            if (!element.isJsonPrimitive() || (value = element.getAsString().trim()).isEmpty()) continue;
            values.add(value);
        }
        return List.copyOf(values);
    }

    private record NamePool(List<String> fullNames, List<String> prefixes, List<String> suffixes) {
        private static final NamePool EMPTY = new NamePool(List.of(), List.of(), List.of());
    }
}

