package com.friday.cultivation.client;

import java.util.List;
import java.util.Locale;

/**
 * 生物状态 HUD 的 Boss 分类与标准 Boss 条去重规则。
 *
 * <p>该类保持为无 Minecraft 渲染依赖的纯逻辑模块，便于对模组兼容规则做稳定回归测试。</p>
 */
public final class EntityStatusBossPolicy {
    public static final double UNTAGGED_HOSTILE_BOSS_HEALTH = 100.0D;
    public static final double PROGRESS_MATCH_TOLERANCE = 0.08D;
    public static final int VANILLA_FIRST_BOSS_BAR_Y = 12;

    private EntityStatusBossPolicy() {
    }

    /**
     * Forge 通用 Boss 标签和原版 Boss 类型具有最高优先级；未正确打标签的模组敌对生物
     * 以 100 最大生命作为兼容兜底，避免把高生命友方 NPC 或坐骑误判为 Boss。
     */
    public static boolean isBossCandidate(boolean forgeBossTagged, boolean vanillaBoss,
                                          boolean hostile, double maxHealth) {
        if (forgeBossTagged || vanillaBoss) {
            return true;
        }
        return hostile && Double.isFinite(maxHealth)
                && maxHealth >= UNTAGGED_HOSTILE_BOSS_HEALTH;
    }

    /**
     * 标准 Boss 条名称与实体名相同，或其显示进度与实体实时生命比例足够接近时，视为
     * 已有对应条。后者用于兼容使用自定义标题而非实体显示名的模组。
     */
    public static boolean hasCorrespondingBossBar(String entityName, double healthRatio,
                                                   List<ExistingBossBar> existingBars) {
        String normalizedEntityName = normalizeName(entityName);
        double clampedRatio = clampRatio(healthRatio);
        for (ExistingBossBar bar : existingBars) {
            if (!normalizedEntityName.isEmpty()
                    && normalizedEntityName.equals(normalizeName(bar.name()))) {
                return true;
            }
            if (Double.isFinite(healthRatio) && Double.isFinite(bar.progress())
                    && Math.abs(clampedRatio - clampRatio(bar.progress()))
                    <= PROGRESS_MATCH_TOLERANCE) {
                return true;
            }
        }
        return false;
    }

    /** 把 Friday 兜底条排在所有已由原版/模组绘制的标准 Boss 条之后。 */
    public static int nextFreeBossBarY(List<ExistingBossBar> existingBars) {
        int nextY = VANILLA_FIRST_BOSS_BAR_Y;
        for (ExistingBossBar bar : existingBars) {
            nextY = Math.max(nextY, bar.y() + Math.max(0, bar.increment()));
        }
        return nextY;
    }

    private static String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            return "";
        }
        return name.replaceAll("(?i)\\u00a7[0-9A-FK-OR]", "")
                .trim()
                .replaceAll("\\s+", " ")
                .toLowerCase(Locale.ROOT);
    }

    private static double clampRatio(double ratio) {
        return Math.max(0.0D, Math.min(1.0D, ratio));
    }

    public record ExistingBossBar(String name, double progress, int y, int increment) {
    }
}
