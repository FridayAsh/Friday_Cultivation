package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.client.EntityStatusPlateLayout;
import org.junit.jupiter.api.Test;

/** 生物状态牌恒定逻辑尺寸公式的纯数学回归测试。 */
class EntityStatusPlateLayoutTest {
    @Test
    void worldDimensionsRemainConstantAtDifferentDepths() {
        for (double depth : new double[]{2.0D, 4.0D, 8.0D, 16.0D, 24.0D}) {
            EntityStatusPlateLayout.Layout layout = EntityStatusPlateLayout.compute(depth);

            assertEquals(1.2F, layout.barWidth(), 0.0001F);
            assertEquals(0.144F, layout.barHeight(), 0.0001F);
            assertEquals(0.144F, layout.iconSize(), 0.0001F,
                    "护甲与韧性图标应使用原生 9×9 排版尺寸，经过 0.016 世界比例后为 0.144 格");
        }
    }

    @Test
    void worldPlaneScaleDoesNotGrowWithCameraDistance() {
        EntityStatusPlateLayout.Layout near = EntityStatusPlateLayout.compute(2.0D);
        EntityStatusPlateLayout.Layout far = EntityStatusPlateLayout.compute(24.0D);

        assertEquals(near.worldUnitsPerLogicalPixel(), far.worldUnitsPerLogicalPixel(), 0.000001F,
                "状态牌必须保持固定世界尺寸，让它与生物一起自然缩小，不能在远处放大世界平面");
    }

    @Test
    void entityWidthDoesNotParticipateInLayout() {
        EntityStatusPlateLayout.Layout small = EntityStatusPlateLayout.compute(8.0D);
        EntityStatusPlateLayout.Layout large = EntityStatusPlateLayout.compute(8.0D);

        assertEquals(small.barWidth(), large.barWidth(), 0.0F);
        assertEquals(small.barHeight(), large.barHeight(), 0.0F);
    }

    @Test
    void invalidProjectionIsNotDrawable() {
        assertNull(EntityStatusPlateLayout.compute(-1.0D));
        assertNull(EntityStatusPlateLayout.compute(Double.NaN));
    }

    @Test
    void ratiosAreClampedWithoutChangingLayout() {
        assertEquals(0.0F, EntityStatusPlateLayout.clampRatio(-1.0D));
        assertEquals(0.5F, EntityStatusPlateLayout.clampRatio(0.5D));
        assertEquals(1.0F, EntityStatusPlateLayout.clampRatio(2.0D));
    }

    @Test
    void healthFillKeepsTextureScaleWhenRatioIsTiny() {
        EntityStatusPlateLayout.FillSlice zero =
                EntityStatusPlateLayout.healthFillSlice(100.0F, 96.0F, 0.0D);
        EntityStatusPlateLayout.FillSlice tiny =
                EntityStatusPlateLayout.healthFillSlice(100.0F, 96.0F, 0.01D);
        EntityStatusPlateLayout.FillSlice full =
                EntityStatusPlateLayout.healthFillSlice(100.0F, 96.0F, 1.0D);

        assertEquals(zero.left(), zero.right(), 0.0001F, "零血量不能绘制填充");
        assertEquals(0.0F, zero.sourceRight(), 0.0001F, "零血量不能采样填充纹理");
        assertTrue(tiny.right() > tiny.left(), "极低血量仍应保留对应宽度");
        assertTrue(tiny.right() <= 196.0F, "极低血量目标范围不能越过右边框");
        assertEquals(0.01F,
                (tiny.sourceRight() - tiny.sourceLeft()) / EntityStatusPlateLayout.TEXTURE_WIDTH,
                0.0001F,
                "极低血量只能裁取纹理左侧同比例切片，不能把96像素整张贴图压缩成红色竖条");
        assertEquals(96.0F, full.sourceRight(), 0.0001F);
        assertEquals(196.0F, full.right(), 0.0001F);
    }
}
