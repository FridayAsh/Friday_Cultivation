package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.friday.cultivation.client.EntityStatusScreenProjection;
import org.junit.jupiter.api.Test;

/** 光影合成后状态牌仍使用真实裁剪空间锚点和正常世界透视的纯数学回归测试。 */
class EntityStatusScreenProjectionTest {
    @Test
    void clipSpaceCenterMapsToGuiCenter() {
        EntityStatusScreenProjection.Projected projected =
                EntityStatusScreenProjection.project(0.0F, 0.0F, 4.0F, 1.5F, 320, 180);

        assertEquals(160.0F, projected.screenX(), 0.0001F);
        assertEquals(90.0F, projected.screenY(), 0.0001F);
    }

    @Test
    void fixedWorldPlateKeepsNaturalPerspective() {
        EntityStatusScreenProjection.Projected near =
                EntityStatusScreenProjection.project(0.0F, 0.0F, 4.0F, 1.5F, 320, 180);
        EntityStatusScreenProjection.Projected far =
                EntityStatusScreenProjection.project(0.0F, 0.0F, 8.0F, 1.5F, 320, 180);

        assertEquals(near.localScale() * 0.5F, far.localScale(), 0.000001F,
                "光影后绘制只能改变着色阶段，不能恢复恒定屏幕像素或破坏正常透视");
    }

    @Test
    void invalidOrBehindCameraProjectionIsRejected() {
        assertNull(EntityStatusScreenProjection.project(0.0F, 0.0F, -1.0F, 1.5F, 320, 180));
        assertNull(EntityStatusScreenProjection.project(Float.NaN, 0.0F, 4.0F, 1.5F, 320, 180));
        assertNull(EntityStatusScreenProjection.project(0.0F, 0.0F, 4.0F, 0.0F, 320, 180));
    }
}
