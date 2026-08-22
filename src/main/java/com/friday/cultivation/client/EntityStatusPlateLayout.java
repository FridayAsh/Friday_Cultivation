package com.friday.cultivation.client;

/**
 * 生物头顶状态牌的纯布局模块。
 *
 * <p>所有尺寸以 Minecraft GUI 逻辑像素定义，再通过当前投影矩阵换算为世界尺寸。
 * 该类不接触实体、OpenGL 或光影状态，因此可以直接通过单元测试验证恒定尺寸公式。</p>
 */
public final class EntityStatusPlateLayout {
    public static final float BAR_WIDTH_PIXELS = 75.0F;
    public static final float BAR_HEIGHT_PIXELS = 9.0F;
    public static final float BAR_HEAD_GAP_PIXELS = 3.0F;
    public static final float ICON_SIZE_PIXELS = 13.0F;
    public static final float ICON_GAP_PIXELS = 3.0F;
    public static final float TEXT_SCALE = 0.78F;
    public static final float TEXT_SHADOW_Z = 0.001F;
    public static final float CLIP_TEXTURE_PIXELS = 3.0F;
    public static final float TEXTURE_WIDTH = 96.0F;
    public static final float TEXTURE_HEIGHT = 6.0F;

    private EntityStatusPlateLayout() {
    }

    /**
     * 计算一个逻辑像素在当前相机深度对应的世界尺寸。
     *
     * @param depth 相对于相机前方向的正向深度
     * @param projectionY 当前投影矩阵的 m11 项绝对值
     * @param guiScaledHeight Minecraft GUI 逻辑高度
     * @return 可绘制布局；输入无效或位于相机后方时返回 {@code null}
     */
    public static Layout compute(double depth, float projectionY, int guiScaledHeight) {
        if (!Double.isFinite(depth) || depth <= 0.0001D
                || !Float.isFinite(projectionY) || projectionY <= 0.0001F
                || guiScaledHeight <= 0) {
            return null;
        }
        double unitsPerPixel = 2.0D * depth / ((double) projectionY * (double) guiScaledHeight);
        if (!Double.isFinite(unitsPerPixel) || unitsPerPixel <= 0.0D) {
            return null;
        }
        float scale = (float) unitsPerPixel;
        if (!Float.isFinite(scale) || scale <= 0.0F) {
            return null;
        }
        return new Layout(scale, BAR_WIDTH_PIXELS * scale, BAR_HEIGHT_PIXELS * scale,
                ICON_SIZE_PIXELS * scale, BAR_HEAD_GAP_PIXELS * scale);
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
