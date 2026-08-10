package com.friday.cultivation.qi.source;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.qi.BlockQiSpecs;
import com.friday.cultivation.qi.IQiSource;
import com.friday.cultivation.qi.QiEcosystem;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * 方块灵气源视图（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.qi.source.BlockQiSourceView）。
 * <p>把 {@link QiEcosystem} 包装为 {@link IQiSource} 适配器，仅取静态灵气属性与即时读取/抽取。</p>
 */
public final class BlockQiSourceView implements IQiSource {
    private final ServerLevel level;
    private final BlockPos pos;
    private final QiElement element;

    private BlockQiSourceView(ServerLevel level, BlockPos pos, QiElement element) {
        this.level = level;
        this.pos = pos;
        this.element = element;
    }

    @Nullable
    public static BlockQiSourceView create(ServerLevel level, BlockPos pos) {
        BlockQiSpec spec = BlockQiSpecs.of(level.getBlockState(pos));
        if (spec == null) {
            return null;
        }
        return new BlockQiSourceView(level, pos.immutable(), spec.element());
    }

    public QiElement element() {
        return this.element;
    }

    public int peek() {
        return QiEcosystem.peekBlock(this.level, this.pos);
    }

    public int extract(int amount) {
        return QiEcosystem.tryDrainBlock(this.level, this.pos, amount);
    }

    public BlockPos pos() {
        return this.pos;
    }
}
