package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.qi.BlockQiSpec;
import com.friday.cultivation.cultivation.qi.state.ChunkQiPool;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import org.junit.jupiter.api.Test;

class ChunkQiPoolTest {
    @Test
    void peekDoesNotConsumeTimeAndFractionalRegenIsRetained() {
        ChunkQiPool pool = new ChunkQiPool();
        BlockPos pos = new BlockPos(1, 64, 1);
        BlockQiSpec spec = BlockQiSpec.of(QiElement.PURE, 100, 1.0, 0.0);
        RandomSource random = RandomSource.create(1L);

        pool.tryDrain(pos, spec, 10, 0L, random);
        assertEquals(90, pool.peek(pos, spec, 10L));
        assertEquals(90, pool.peek(pos, spec, 10L));

        pool.tryDrain(pos, spec, 0, 10L, random);
        assertEquals(91, pool.peek(pos, spec, 20L));
        pool.tryDrain(pos, spec, 0, 20L, random);
        assertEquals(91, pool.peek(pos, spec, 20L));
    }
}
