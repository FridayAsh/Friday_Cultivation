package com.friday.cultivation.qi.state;

/**
 * 方块灵气状态 - 单方块灵气池条目（当前值/总消耗/末次触摸时间）。
 * 完全照搬原 mod: xiaoxiang.cultivation.cultivation.qi.state.BlockQiState
 */
public final class BlockQiState {
    public int currentQi;
    public int totalDrained;
    public long lastTouchTime;

    public BlockQiState() {
    }

    public BlockQiState(int currentQi, int totalDrained, long lastTouchTime) {
        this.currentQi = currentQi;
        this.totalDrained = totalDrained;
        this.lastTouchTime = lastTouchTime;
    }
}
