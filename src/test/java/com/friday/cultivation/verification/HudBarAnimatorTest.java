package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.client.HudBarAnimator;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** HUD 属性条动画的纯逻辑回归测试，不依赖 Minecraft 渲染环境。 */
class HudBarAnimatorTest {
    @Test
    void gainUsesSmoothPrimaryTransition() {
        HudBarAnimator animator = new HudBarAnimator();
        animator.beginFrame(UUID.randomUUID());

        HudBarAnimator.Visual initial = animator.sample(HudBarAnimator.BarId.HEALTH, 50.0, 100.0, 0L, 1_000L);
        HudBarAnimator.Visual started = animator.sample(HudBarAnimator.BarId.HEALTH, 80.0, 100.0, 0L, 1_001L);
        HudBarAnimator.Visual middle = animator.sample(HudBarAnimator.BarId.HEALTH, 80.0, 100.0, 0L, 1_100L);
        HudBarAnimator.Visual finished = animator.sample(HudBarAnimator.BarId.HEALTH, 80.0, 100.0, 0L, 1_400L);

        assertEquals(0.50, initial.primaryRatio(), 0.0001);
        assertEquals(0.50, started.primaryRatio(), 0.0001);
        assertTrue(middle.primaryRatio() > 0.50 && middle.primaryRatio() < 0.80);
        assertEquals(0.80, finished.primaryRatio(), 0.0001);
    }

    @Test
    void damageKeepsDelayedTrailingFill() {
        HudBarAnimator animator = new HudBarAnimator();
        animator.beginFrame(UUID.randomUUID());
        animator.sample(HudBarAnimator.BarId.HEALTH, 100.0, 100.0, 0L, 2_000L);

        HudBarAnimator.Visual damaged = animator.sample(HudBarAnimator.BarId.HEALTH, 30.0, 100.0, 0L, 2_001L);
        HudBarAnimator.Visual afterPrimary = animator.sample(HudBarAnimator.BarId.HEALTH, 30.0, 100.0, 0L, 2_100L);

        assertEquals(1.0, damaged.trailingRatio(), 0.0001);
        assertTrue(afterPrimary.primaryRatio() < 1.0);
        assertTrue(afterPrimary.trailingRatio() > afterPrimary.primaryRatio());
    }

    @Test
    void experienceLevelUpFillsOldStageThenResetsToNewStage() {
        HudBarAnimator animator = new HudBarAnimator();
        animator.beginFrame(UUID.randomUUID());
        animator.sample(HudBarAnimator.BarId.EXPERIENCE, 84.0, 87.0, 25L, 3_000L);

        HudBarAnimator.Visual started = animator.sample(HudBarAnimator.BarId.EXPERIENCE, 0.0, 90.0, 26L, 3_001L);
        HudBarAnimator.Visual full = animator.sample(HudBarAnimator.BarId.EXPERIENCE, 0.0, 90.0, 26L, 3_150L);
        HudBarAnimator.Visual reset = animator.sample(HudBarAnimator.BarId.EXPERIENCE, 10.0, 90.0, 26L, 3_250L);
        HudBarAnimator.Visual finished = animator.sample(HudBarAnimator.BarId.EXPERIENCE, 10.0, 90.0, 26L, 3_500L);

        assertEquals(25L, started.displayCycleKey());
        assertTrue(full.primaryRatio() > started.primaryRatio());
        assertTrue(full.pulseStrength() > 0.0F);
        assertEquals(26L, reset.displayCycleKey());
        assertTrue(reset.cycleTransition());
        assertEquals(26L, finished.displayCycleKey());
        assertEquals(10.0 / 90.0, finished.primaryRatio(), 0.0001);
    }

    @Test
    void levelDowngradeDoesNotPlayUpgradeRollover() {
        HudBarAnimator animator = new HudBarAnimator();
        animator.beginFrame(UUID.randomUUID());
        animator.sample(HudBarAnimator.BarId.EXPERIENCE, 20.0, 90.0, 25L, 4_000L);

        HudBarAnimator.Visual downgraded = animator.sample(HudBarAnimator.BarId.EXPERIENCE, 80.0, 87.0, 24L, 4_001L);

        assertEquals(24L, downgraded.displayCycleKey());
        assertTrue(!downgraded.cycleTransition());
    }
}
