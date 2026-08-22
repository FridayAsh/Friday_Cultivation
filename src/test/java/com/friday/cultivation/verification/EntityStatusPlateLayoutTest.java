package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.friday.cultivation.client.EntityStatusPlateLayout;
import org.junit.jupiter.api.Test;

/** 生物状态牌恒定逻辑尺寸公式的纯数学回归测试。 */
class EntityStatusPlateLayoutTest {
    @Test
    void projectedLogicalSizeRemainsConstantAtDifferentDepths() {
        for (double depth : new double[]{2.0D, 4.0D, 8.0D, 16.0D, 24.0D}) {
            EntityStatusPlateLayout.Layout layout = EntityStatusPlateLayout.compute(depth, 1.73205F, 240);
            double projectedWidth = (double) layout.barWidth() * 1.73205D * 240.0D / (2.0D * depth);
            double projectedHeight = (double) layout.barHeight() * 1.73205D * 240.0D / (2.0D * depth);

            assertEquals(EntityStatusPlateLayout.BAR_WIDTH_PIXELS, projectedWidth, 0.0001D);
            assertEquals(EntityStatusPlateLayout.BAR_HEIGHT_PIXELS, projectedHeight, 0.0001D);
        }
    }

    @Test
    void entityWidthDoesNotParticipateInLayout() {
        EntityStatusPlateLayout.Layout small = EntityStatusPlateLayout.compute(8.0D, 1.2F, 240);
        EntityStatusPlateLayout.Layout large = EntityStatusPlateLayout.compute(8.0D, 1.2F, 240);

        assertEquals(small.barWidth(), large.barWidth(), 0.0F);
        assertEquals(small.barHeight(), large.barHeight(), 0.0F);
    }

    @Test
    void invalidProjectionIsNotDrawable() {
        assertNull(EntityStatusPlateLayout.compute(-1.0D, 1.2F, 240));
        assertNull(EntityStatusPlateLayout.compute(8.0D, 0.0F, 240));
        assertNull(EntityStatusPlateLayout.compute(8.0D, 1.2F, 0));
    }

    @Test
    void ratiosAreClampedWithoutChangingLayout() {
        assertEquals(0.0F, EntityStatusPlateLayout.clampRatio(-1.0D));
        assertEquals(0.5F, EntityStatusPlateLayout.clampRatio(0.5D));
        assertEquals(1.0F, EntityStatusPlateLayout.clampRatio(2.0D));
    }
}
