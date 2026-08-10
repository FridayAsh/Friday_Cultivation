package com.friday.cultivation.sect;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.RandomSource;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 宗门名生成器 — 从 data/friday_cultivation/sect/name_pool.json 加载
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.sect.SectNameGenerator
 */
public final class SectNameGenerator {

    private static final ResourceLocation RESOURCE_PATH =
            new ResourceLocation("friday_cultivation", "sect/name_pool");

    private static List<String> fullNames = List.of();
    private static List<String> prefixes = List.of();
    private static List<String> suffixes = List.of();

    private SectNameGenerator() {}

    /** 由服务端数据包管理器加载名池（可在服务端启动时调用） */
    public static void reload(MinecraftServer server) {
        if (server == null) return;
        ResourceManager rm = server.getResourceManager();
        java.util.Optional<Resource> opt = rm.getResource(RESOURCE_PATH);
        if (opt.isEmpty()) return;
        try (InputStream is = opt.get().open();
             InputStreamReader reader = new InputStreamReader(is, StandardCharsets.UTF_8)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (!root.isJsonObject()) return;
            JsonObject obj = root.getAsJsonObject();
            fullNames = toStringList(obj, "full_names");
            prefixes = toStringList(obj, "prefixes");
            suffixes = toStringList(obj, "suffixes");
        } catch (Exception e) {
            // 加载失败则保持默认
        }
    }

    private static List<String> toStringList(JsonObject obj, String key) {
        List<String> out = new ArrayList<>();
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonArray()) {
            for (JsonElement e : el.getAsJsonArray()) {
                if (e.isJsonPrimitive()) out.add(e.getAsString());
            }
        }
        return out;
    }

    /** 生成随机宗门名 */
    public static String randomName(RandomSource random) {
        if (!fullNames.isEmpty() && random.nextInt(4) == 0) {
            return fullNames.get(random.nextInt(fullNames.size()));
        }
        String prefix = prefixes.isEmpty() ? "山" : prefixes.get(random.nextInt(prefixes.size()));
        String suffix = suffixes.isEmpty() ? "宗" : suffixes.get(random.nextInt(suffixes.size()));
        return prefix + suffix;
    }
}
