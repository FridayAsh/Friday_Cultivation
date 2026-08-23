package com.friday.cultivation.client;

/**
 * 生物状态 HUD 的 Boss 分类规则。
 *
 * <p>该类保持为无 Minecraft 渲染依赖的纯逻辑模块，便于对模组兼容规则做稳定回归测试。</p>
 */
public final class EntityStatusBossPolicy {
    public static final double UNTAGGED_HOSTILE_BOSS_HEALTH = 100.0D;
    /** 为顶部 FPS 文本保留一行高度；Boss 标题位于血条上方 9 像素。 */
    public static final int PROJECT_FIRST_BOSS_BAR_Y = 24;

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

}
