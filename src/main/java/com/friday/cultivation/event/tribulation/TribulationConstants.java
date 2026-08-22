package com.friday.cultivation.event.tribulation;

/**
 * 渡劫系统常量集中管理。
 * 所有硬编码数值统一在此修改；配置接口（JSON/配置文件）留待项目完成后实现。
 */
public final class TribulationConstants {
    private TribulationConstants() {
    }

    // ---- 通用 ----
    /** 波间冷却（tick） */
    public static final int WAVE_COOLDOWN_TICKS = 20;
    /** 雷击随机半径（格） */
    public static final double BOLT_RANDOM_RADIUS = 1.0;
    /** 子波基准时长（tick） */
    public static final int SUB_WAVE_DURATION_BASE_TICKS = 30;
    /** 最小雷击间隔（tick） */
    public static final int MIN_SUB_BOLT_INTERVAL_TICKS = 4;
    /** 最大雷击间隔（tick） */
    public static final int MAX_SUB_BOLT_INTERVAL_TICKS = 20;

    // ---- 渡劫云特效 ----
    public static final float CLOUD_SOUND_VOLUME = 0.8f;
    public static final float CLOUD_SOUND_PITCH = 0.42f;
    public static final float THUNDER_SOUND_VOLUME = 1.3f;
    public static final float THUNDER_SOUND_PITCH = 0.55f;
    public static final int CLOUD_PARTICLE_COUNT = 34;
    public static final double CLOUD_PARTICLE_RADIUS = 4.8;
    public static final double CLOUD_PARTICLE_HEIGHT = 0.28;
    public static final double CLOUD_PARTICLE_SPREAD = 3.4;
    public static final double CLOUD_PARTICLE_SPEED = 0.012;
    public static final int CLOUD_CLOUD_COUNT_COOLDOWN = 8;
    public static final int CLOUD_CLOUD_COUNT_NORMAL = 5;
    public static final double CLOUD_RADIUS = 5.6;
    public static final double CLOUD_Y_OFFSET = 8.0;
    /** 云时长基准（tick） */
    public static final int CLOUD_DURATION_BASE_TICKS = 60;
    public static final int CLOUD_DURATION_WAVE_GAP_TICKS = 20;
    public static final int CLOUD_DURATION_TAIL_TICKS = 80;

    // ---- 雷灵根减免 ----
    /** 变异雷灵根渡劫伤害除数 */
    public static final int LIGHTNING_ROOT_DIVISOR = 2;

    // ---- 综合评判权重（灵根/体质/功法品质）----
    // 这是唯一权重来源；TribulationScalingHelper 只负责读取，不再复制一套常量。
    public static final double SPIRIT_ROOT_WEIGHT = 1.2;
    public static final double PHYSIQUE_WEIGHT = 0.5;
    public static final double TECHNIQUE_WEIGHT = 0.3;
}
