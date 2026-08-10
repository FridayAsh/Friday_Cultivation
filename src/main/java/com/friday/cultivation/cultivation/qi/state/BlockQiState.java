/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.qi.state;

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

