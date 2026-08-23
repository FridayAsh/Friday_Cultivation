package com.friday.cultivation.client;

import java.lang.reflect.Field;

/**
 * Jade 可选兼容层。
 *
 * <p>Friday 在最高优先级取消标准 Boss 条事件后，Jade 无法再从该事件记录需要避让的
 * 高度。本适配器只补回 Jade 的 Boss 区域输入，不修改其配置，也不建立硬依赖。</p>
 */
public final class JadeOverlayCompat {
    private static final JadeFields JADE_FIELDS = JadeFields.create();

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
                height.setInt(null, bottom);
            } catch (IllegalAccessException | RuntimeException ignored) {
                // Jade 为可选模组，兼容失败不能影响项目 HUD。
            }
        }
    }
}
