package com.friday.cultivation.client;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Jade 可选兼容层。
 *
 * <p>Friday 在最高优先级取消标准 Boss 条事件后，Jade 无法再从该事件记录需要避让的
 * 高度。本适配器只补回 Jade 的 Boss 区域输入，不修改其配置，也不建立硬依赖。</p>
 */
public final class JadeOverlayCompat {
    private static final int JADE_VANILLA_BOSS_BAR_Y = 12;
    private static final JadeFields JADE_FIELDS = JadeFields.create();
    private static final JadeTooltipLayout JADE_TOOLTIP_LAYOUT = JadeTooltipLayout.create();

    private JadeOverlayCompat() {
    }

    public static int reservedBottom(int firstBarY, int barHeight,
                                     int stackIncrement, int visibleBossCount) {
        if (visibleBossCount <= 0 || barHeight <= 0) {
            return 0;
        }
        return firstBarY + barHeight + (visibleBossCount - 1) * stackIncrement;
    }

    /** 向已安装的 Jade 登记项目 Boss 条区域；未安装或版本不兼容时安全跳过。 */
    public static void reserveBossBarArea(int bottom) {
        if (bottom > 0) {
            JADE_FIELDS.reserve(bottom);
        }
    }

    /** Jade 11.13.2 把 Boss 矩形高度当作 Tooltip Y，需补回其固定起点 12。 */
    public static int jadeHeightFieldValue(int projectBottom) {
        return projectBottom <= 0 ? 0 : projectBottom + JADE_VANILLA_BOSS_BAR_Y;
    }

    /** 在 Jade 正式绘制前强制重算当前 Tooltip 位置。 */
    public static void recalculateTooltipLayout() {
        JADE_TOOLTIP_LAYOUT.recalculate();
    }

    private record JadeFields(Field shown, Field height) {
        private static JadeFields create() {
            try {
                Class<?> clientProxy = Class.forName("snownee.jade.util.ClientProxy", false,
                        JadeOverlayCompat.class.getClassLoader());
                Field shown = clientProxy.getDeclaredField("bossbarShown");
                Field height = clientProxy.getDeclaredField("bossbarHeight");
                if (!shown.trySetAccessible() || !height.trySetAccessible()) {
                    return new JadeFields(null, null);
                }
                return new JadeFields(shown, height);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
                return new JadeFields(null, null);
            }
        }

        private void reserve(int bottom) {
            if (shown == null || height == null) {
                return;
            }
            try {
                shown.setBoolean(null, true);
                height.setInt(null, jadeHeightFieldValue(bottom));
            } catch (IllegalAccessException | RuntimeException ignored) {
                // Jade 为可选模组，兼容失败不能影响项目 HUD。
            }
        }
    }

    private record JadeTooltipLayout(Method instance, Field tooltipRenderer,
                                     Method recalculateRealRect) {
        private static JadeTooltipLayout create() {
            try {
                ClassLoader loader = JadeOverlayCompat.class.getClassLoader();
                Class<?> handlerClass = Class.forName(
                        "snownee.jade.overlay.WailaTickHandler", false, loader);
                Class<?> rendererClass = Class.forName(
                        "snownee.jade.overlay.TooltipRenderer", false, loader);
                Method instance = handlerClass.getMethod("instance");
                Field tooltipRenderer = handlerClass.getField("tooltipRenderer");
                Method recalculateRealRect = rendererClass.getMethod("recalculateRealRect");
                return new JadeTooltipLayout(instance, tooltipRenderer, recalculateRealRect);
            } catch (ReflectiveOperationException | LinkageError | RuntimeException ignored) {
                return new JadeTooltipLayout(null, null, null);
            }
        }

        private void recalculate() {
            if (instance == null || tooltipRenderer == null || recalculateRealRect == null) {
                return;
            }
            try {
                Object handler = instance.invoke(null);
                Object renderer = tooltipRenderer.get(handler);
                if (renderer != null) {
                    recalculateRealRect.invoke(renderer);
                }
            } catch (ReflectiveOperationException | RuntimeException ignored) {
                // Jade 为可选模组，兼容失败不能影响项目 HUD。
            }
        }
    }
}
