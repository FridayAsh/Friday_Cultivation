package com.friday.cultivation.client;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * HUD 属性条的统一客户端动画控制器。
 *
 * <p>这个模块只负责把真实数值转换为可绘制状态，永远不会修改玩家能力或服务端数据。
 * 绘制层只需要读取 {@link Visual} 的主填充、延迟拖影和闪光强度即可。</p>
 */
public final class HudBarAnimator {
    private static final long SNAP_AFTER_IDLE_MS = 650L;
    private static final long STATE_PRUNE_INTERVAL_MS = 1_000L;
    private static final long STATE_EXPIRE_AFTER_MS = 5_000L;
    private static final double EPSILON = 0.0001D;

    private static final Profile HEALTH = new Profile(220L, 90L, 120L, 320L, false, 0L, 0L, 0L);
    private static final Profile CULTIVATION = new Profile(420L, 240L, 90L, 260L, true, 140L, 110L, 220L);
    private static final Profile QI = new Profile(280L, 100L, 60L, 200L, false, 0L, 0L, 0L);
    private static final Profile WUDAO = new Profile(360L, 240L, 90L, 240L, false, 0L, 0L, 0L);
    private static final Profile EXPERIENCE = new Profile(260L, 160L, 0L, 220L, true, 120L, 100L, 210L);

    public enum BarId {
        HEALTH,
        CULTIVATION,
        QI,
        WUDAO,
        EXPERIENCE
    }

    /**
     * 绘制层使用的动画结果。textCurrent/textMax 只在经验跨级动画期间使用旧阶段数值，
     * 其他属性条仍由 HUD 立即显示真实数值。
     */
    public record Visual(double primaryRatio, double trailingRatio, float pulseStrength,
                         long displayCycleKey, double textCurrent, double textMax,
                         boolean cycleTransition) {
        public Visual {
            primaryRatio = clamp(primaryRatio);
            trailingRatio = clamp(Math.max(primaryRatio, trailingRatio));
            pulseStrength = (float)clamp(pulseStrength);
            textMax = Math.max(1.0D, finite(textMax, 1.0D));
            textCurrent = Math.max(0.0D, Math.min(textMax, finite(textCurrent, 0.0D)));
        }
    }

    /** 拥有者 UUID + 属性条类型，保证多只生物不会互相覆盖动画状态。 */
    private record AnimationKey(UUID ownerId, BarId barId) {
    }

    private final Map<AnimationKey, State> states = new HashMap<>();
    private long nextPruneMillis;

    /** 清空指定拥有者的所有属性条状态。 */
    public void reset(UUID ownerId) {
        if (ownerId == null) {
            return;
        }
        states.keySet().removeIf(key -> key.ownerId().equals(ownerId));
    }

    /** 清空全部视觉状态；客户端换世界或退出时调用。 */
    public void reset() {
        states.clear();
        nextPruneMillis = 0L;
    }

    /**
     * 提交一个属性条的真实值并取得当前应绘制的视觉状态。
     * cycleKey 只用于经验等级、修为境界等“阶段切换”属性；普通属性传 0 即可。
     */
    public Visual sample(UUID ownerId, BarId id, double current, double max, long cycleKey, long nowMillis) {
        Objects.requireNonNull(ownerId, "HUD animation owner id cannot be null");
        if (id == null) {
            throw new IllegalArgumentException("HUD bar id cannot be null");
        }
        pruneIfDue(nowMillis);
        AnimationKey key = new AnimationKey(ownerId, id);
        State state = states.computeIfAbsent(key, ignored -> new State());
        return state.sample(current, max, cycleKey, nowMillis, profile(id));
    }

    private void pruneIfDue(long nowMillis) {
        if (nowMillis < nextPruneMillis) {
            return;
        }
        nextPruneMillis = nowMillis + STATE_PRUNE_INTERVAL_MS;
        Iterator<Map.Entry<AnimationKey, State>> iterator = states.entrySet().iterator();
        while (iterator.hasNext()) {
            State state = iterator.next().getValue();
            if (nowMillis - state.lastSampleMillis > STATE_EXPIRE_AFTER_MS) {
                iterator.remove();
            }
        }
    }

    private static Profile profile(BarId id) {
        return switch (id) {
            case HEALTH -> HEALTH;
            case CULTIVATION -> CULTIVATION;
            case QI -> QI;
            case WUDAO -> WUDAO;
            case EXPERIENCE -> EXPERIENCE;
        };
    }

    private static final class State {
        private boolean initialized;
        private double ratio;
        private double trailRatio;
        private double targetRatio;
        private double targetCurrent;
        private double targetMax;
        private double displayMax = 1.0D;
        private long displayCycleKey;
        private long lastSampleMillis;

        private boolean transitionActive;
        private double transitionFrom;
        private double transitionTo;
        private long transitionStartedAt;
        private long transitionDuration;

        private boolean trailActive;
        private double trailFrom;
        private double trailTo;
        private long trailStartedAt;
        private long trailDelay;
        private long trailDuration;

        private boolean cycleActive;
        private long cycleTargetKey;
        private double cycleOldMax;
        private double cycleTargetCurrent;
        private double cycleTargetMax;
        private double cycleTargetRatio;
        private double cycleStartRatio;
        private long cycleStartedAt;
        private long cycleFillDuration;
        private long cycleFlashDuration;
        private long cycleResetDuration;
        private long pulseStartedAt = Long.MIN_VALUE;
        private long pulseDuration;

        private Visual sample(double current, double max, long cycleKey, long now, Profile profile) {
            current = Math.max(0.0D, finite(current, 0.0D));
            max = Math.max(1.0D, finite(max, 1.0D));
            double target = clamp(current / max);

            if (!initialized) {
                initialized = true;
                ratio = target;
                trailRatio = target;
                targetRatio = target;
                targetCurrent = current;
                targetMax = max;
                displayMax = max;
                displayCycleKey = cycleKey;
                lastSampleMillis = now;
                return normalVisual(current, max, now);
            }

            if (now < lastSampleMillis || now - lastSampleMillis > SNAP_AFTER_IDLE_MS) {
                snap(current, max, cycleKey, target, now);
                return normalVisual(current, max, now);
            }
            lastSampleMillis = now;

            double previousTargetRatio = targetRatio;
            double previousTargetMax = targetMax;
            boolean cycleAdvanced = profile.cycleAware && cycleKey > displayCycleKey;
            boolean cycleChanged = profile.cycleAware && cycleKey != displayCycleKey;
            targetCurrent = current;
            targetMax = max;
            targetRatio = target;

            if (cycleActive) {
                if (cycleKey < displayCycleKey) {
                    // 指令降级、死亡或数据回滚不能伪装成一次升级动画。
                    cycleActive = false;
                    displayCycleKey = cycleKey;
                    ratio = target;
                    trailRatio = target;
                    return normalVisual(current, max, now);
                }
                cycleTargetKey = cycleKey;
                cycleTargetCurrent = current;
                cycleTargetMax = max;
                cycleTargetRatio = target;
                return updateCycle(now);
            }

            advancePrimary(now);
            advanceTrail(now);

            if (cycleAdvanced) {
                startCycle(cycleKey, current, max, target, now, profile);
                return updateCycle(now);
            }
            if (cycleChanged) {
                displayCycleKey = cycleKey;
            }

            if (Math.abs(target - previousTargetRatio) > EPSILON
                    || Math.abs(max - previousTargetMax) > EPSILON) {
                startTransition(target, now, profile);
            }
            displayMax = max;
            return normalVisual(current, max, now);
        }

        private void startTransition(double target, long now, Profile profile) {
            double from = ratio;
            boolean increasing = target >= from;
            transitionActive = Math.abs(target - from) > EPSILON;
            transitionFrom = from;
            transitionTo = target;
            transitionStartedAt = now;
            transitionDuration = increasing ? profile.gainDuration : profile.lossDuration;

            if (!increasing) {
                trailFrom = Math.max(trailRatio, from);
                trailTo = target;
                trailStartedAt = now;
                trailDelay = profile.trailDelay;
                trailDuration = profile.trailDuration;
                trailActive = true;
                trailRatio = trailFrom;
            } else {
                trailActive = false;
                trailRatio = from;
            }
        }

        private void advancePrimary(long now) {
            if (!transitionActive) {
                return;
            }
            double progress = normalizedTime(now - transitionStartedAt, transitionDuration);
            ratio = lerp(transitionFrom, transitionTo, easeOutCubic(progress));
            if (progress >= 1.0D) {
                ratio = transitionTo;
                transitionActive = false;
                if (transitionTo > transitionFrom + EPSILON) {
                    pulseStartedAt = now;
                    pulseDuration = 120L;
                }
            }
        }

        private void advanceTrail(long now) {
            if (!trailActive || now < trailStartedAt + trailDelay) {
                return;
            }
            double progress = normalizedTime(now - trailStartedAt - trailDelay, trailDuration);
            trailRatio = lerp(trailFrom, trailTo, easeOutCubic(progress));
            if (progress >= 1.0D) {
                trailRatio = trailTo;
                trailActive = false;
            }
        }

        private void startCycle(long cycleKey, double current, double max, double target, long now, Profile profile) {
            cycleActive = true;
            cycleTargetKey = cycleKey;
            cycleOldMax = Math.max(1.0D, displayMax);
            cycleTargetCurrent = current;
            cycleTargetMax = max;
            cycleTargetRatio = target;
            cycleStartRatio = ratio;
            cycleStartedAt = now;
            long distance = Math.abs(cycleKey - displayCycleKey);
            boolean compressed = distance > 1L;
            cycleFillDuration = compressed ? 90L : profile.cycleFillDuration;
            cycleFlashDuration = compressed ? 70L : profile.cycleFlashDuration;
            cycleResetDuration = compressed ? 100L : profile.cycleResetDuration;
            transitionActive = false;
            trailActive = false;
            trailRatio = ratio;
        }

        private Visual updateCycle(long now) {
            long elapsed = Math.max(0L, now - cycleStartedAt);
            long fillEnd = cycleFillDuration;
            long flashEnd = fillEnd + cycleFlashDuration;
            long resetEnd = flashEnd + cycleResetDuration;

            if (elapsed < fillEnd) {
                double progress = normalizedTime(elapsed, cycleFillDuration);
                ratio = lerp(cycleStartRatio, 1.0D, easeOutCubic(progress));
                return new Visual(ratio, ratio, 0.0F, displayCycleKey,
                        ratio * cycleOldMax, cycleOldMax, true);
            }
            if (elapsed < flashEnd) {
                ratio = 1.0D;
                double flashProgress = normalizedTime(elapsed - fillEnd, cycleFlashDuration);
                float pulse = (float)(1.0D - Math.abs(flashProgress * 2.0D - 1.0D));
                return new Visual(1.0D, 1.0D, pulse, displayCycleKey,
                        cycleOldMax, cycleOldMax, true);
            }
            if (elapsed < resetEnd) {
                displayCycleKey = cycleTargetKey;
                double progress = normalizedTime(elapsed - flashEnd, cycleResetDuration);
                ratio = lerp(0.0D, cycleTargetRatio, easeOutCubic(progress));
                return new Visual(ratio, ratio, 0.0F, displayCycleKey,
                        ratio * cycleTargetMax, cycleTargetMax, true);
            }

            cycleActive = false;
            displayCycleKey = cycleTargetKey;
            ratio = cycleTargetRatio;
            trailRatio = ratio;
            displayMax = cycleTargetMax;
            targetRatio = cycleTargetRatio;
            targetCurrent = cycleTargetCurrent;
            targetMax = cycleTargetMax;
            return normalVisual(cycleTargetCurrent, cycleTargetMax, now);
        }

        private Visual normalVisual(double current, double max, long now) {
            float pulse = pulse(now);
            return new Visual(ratio, Math.max(ratio, trailRatio), pulse,
                    displayCycleKey, current, max, false);
        }

        private float pulse(long now) {
            if (pulseStartedAt == Long.MIN_VALUE || pulseDuration <= 0L) {
                return 0.0F;
            }
            double progress = normalizedTime(now - pulseStartedAt, pulseDuration);
            if (progress >= 1.0D) {
                pulseStartedAt = Long.MIN_VALUE;
                return 0.0F;
            }
            return (float)(1.0D - progress);
        }

        private void snap(double current, double max, long cycleKey, double target, long now) {
            ratio = target;
            trailRatio = target;
            targetRatio = target;
            targetCurrent = current;
            targetMax = max;
            displayMax = max;
            displayCycleKey = cycleKey;
            lastSampleMillis = now;
            transitionActive = false;
            trailActive = false;
            cycleActive = false;
            pulseStartedAt = Long.MIN_VALUE;
        }

        private void clear() {
            initialized = false;
            ratio = 0.0D;
            trailRatio = 0.0D;
            targetRatio = 0.0D;
            targetCurrent = 0.0D;
            targetMax = 1.0D;
            displayMax = 1.0D;
            displayCycleKey = 0L;
            lastSampleMillis = 0L;
            transitionActive = false;
            trailActive = false;
            cycleActive = false;
            pulseStartedAt = Long.MIN_VALUE;
        }
    }

    private record Profile(long gainDuration, long lossDuration, long trailDelay,
                           long trailDuration, boolean cycleAware, long cycleFillDuration,
                           long cycleFlashDuration, long cycleResetDuration) {
    }

    private static double finite(double value, double fallback) {
        return Double.isFinite(value) ? value : fallback;
    }

    private static double clamp(double value) {
        return Math.max(0.0D, Math.min(1.0D, finite(value, 0.0D)));
    }

    private static double normalizedTime(long elapsed, long duration) {
        if (duration <= 0L) {
            return 1.0D;
        }
        return clamp((double)Math.max(0L, elapsed) / (double)duration);
    }

    private static double easeOutCubic(double progress) {
        double inverse = 1.0D - clamp(progress);
        return 1.0D - inverse * inverse * inverse;
    }

    private static double lerp(double from, double to, double progress) {
        return from + (to - from) * clamp(progress);
    }
}
