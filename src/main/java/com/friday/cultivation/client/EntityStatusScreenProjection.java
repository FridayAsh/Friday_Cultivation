package com.friday.cultivation.client;

/**
 * 把实体渲染阶段捕获的真实裁剪空间锚点换算成最终 GUI 坐标。
 *
 * <p>这里只做纯数学换算，不读取 FOV、不重建相机，也不接触光影状态。状态牌仍按固定
 * 世界尺寸参与透视；最终像素在光影合成后绘制，以隔离昼夜色温和自动曝光。</p>
 */
public final class EntityStatusScreenProjection {
    private EntityStatusScreenProjection() {
    }

    public static Projected project(float clipX, float clipY, float clipW,
                                    float projectionY, int guiWidth, int guiHeight) {
        if (!Float.isFinite(clipX) || !Float.isFinite(clipY)
                || !Float.isFinite(clipW) || clipW <= 0.0001F
                || !Float.isFinite(projectionY) || projectionY <= 0.0001F
                || guiWidth <= 0 || guiHeight <= 0) {
            return null;
        }

        float ndcX = clipX / clipW;
        float ndcY = clipY / clipW;
        float localScale = EntityStatusPlateLayout.WORLD_UNITS_PER_LOGICAL_PIXEL
                * projectionY * guiHeight / (2.0F * clipW);
        if (!Float.isFinite(ndcX) || !Float.isFinite(ndcY)
                || !Float.isFinite(localScale) || localScale <= 0.0F) {
            return null;
        }

        float screenX = (ndcX * 0.5F + 0.5F) * guiWidth;
        float screenY = (0.5F - ndcY * 0.5F) * guiHeight;
        return new Projected(screenX, screenY, localScale, clipW);
    }

    public record Projected(float screenX, float screenY, float localScale, float depth) {
    }
}
