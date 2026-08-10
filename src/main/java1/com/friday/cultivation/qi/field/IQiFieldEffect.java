package com.friday.cultivation.qi.field;

import com.friday.cultivation.qi.BlockQiSpec;
import net.minecraft.core.BlockPos;

/**
 * 灵气场效果接口 - 在某中心点一定半径内对灵气池产生修饰。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.field.IQiFieldEffect
 */
public interface IQiFieldEffect {
    BlockPos origin();

    int radius();

    boolean isActive();

    QiModifier modifyAt(BlockPos pos, BlockQiSpec spec);
}
