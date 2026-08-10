package com.friday.cultivation;

import com.friday.cultivation.qi.BlockQiSpecs;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 方块灵气映射便捷类（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.BlockQiMapping）。
 * <p>仅一行转发至 {@link BlockQiSpecs#elementOf}，为兼容旧调用点保留。</p>
 *
 * @deprecated 改用 {@link BlockQiSpecs#elementOf}。
 */
@Deprecated(forRemoval = false)
public final class BlockQiMapping {
    private BlockQiMapping() {
    }

    @Nullable
    public static QiElement of(BlockState state) {
        return null;
    }
}
