package com.friday.cultivation.client;

/**
 * 生物头顶状态牌的纯布局模块。
 *
 * <p>所有尺寸先以统一的本地排版单位定义，再换算成固定世界尺寸；屏幕投影由
 * {@link EntityStatusScreenProjection} 单独负责。该类不接触实体、OpenGL 或光影状态。</p>
 */
public final class EntityStatusPlateLayout {
    /**
     * 固定世界缩放：75 个本地布局像素对应 1.2 格宽，与早期 48×0.025 的世界状态牌相同。
     * 距离只影响正常透视，不再反向放大远处状态牌。
     */
    public static final float WORLD_UNITS_PER_LOGICAL_PIXEL = 0.016F;
    public static final float BAR_WIDTH_PIXELS = 75.0F;
    public static final float BAR_HEIGHT_PIXELS = 9.0F;
    public static final float BAR_HEAD_GAP_PIXELS = 3.0F;
    public static final float ATTRIBUTE_ICON_SIZE_PIXELS = 9.0F;
    public static final float ATTRIBUTE_TEXT_SCALE = 0.72F;
    public static final float ATTRIBUTE_ICON_TEXT_GAP_PIXELS = 1.0F;
    public static final float ICON_GAP_PIXELS = 3.0F;
    public static final float TEXT_SCALE = 0.78F;
    public static final float TEXT_SHADOW_Z = 0.001F;
    public static final float CLIP_TEXTURE_PIXELS = 3.0F;
    public static final float TEXTURE_WIDTH = 96.0F;
    public static final float TEXTURE_HEIGHT = 6.0F;

    private EntityStatusPlateLayout() {
    }

    /**
     * 计算固定世界尺寸布局。
     *
     * @param depth 相对于相机前方向的正向深度，仅用于剔除相机后方实体
     * @return 可绘制布局；输入无效或位于相机后方时返回 {@code null}
     */
    public static Layout compute(double depth) {
        if (!Double.isFinite(depth) || depth <= 0.0001D) {
            return null;
        }
        float scale = WORLD_UNITS_PER_LOGICAL_PIXEL;
        return new Layout(scale, BAR_WIDTH_PIXELS * scale, BAR_HEIGHT_PIXELS * scale,
                ATTRIBUTE_ICON_SIZE_PIXELS * scale, BAR_HEAD_GAP_PIXELS * scale);
    }

    public static float clampRatio(double ratio) {
        if (!Double.isFinite(ratio)) {
            return 0.0F;
        }
        return (float) Math.max(0.0D, Math.min(1.0D, ratio));
    }

    public record Layout(float worldUnitsPerLogicalPixel,
                         float barWidth,
                         float barHeight,
                         float iconSize,
                         float headGap) {
    }
}
