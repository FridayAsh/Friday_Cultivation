/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.qi.state;

public final class BlockQiState {
    public int currentQi;
    public int totalDrained;
    public long lastTouchTime;
    /** 小数再生余量，避免高频查询/低速再生永久丢失进度。 */
    public double regenRemainder;

    public BlockQiState() {
    }

    public BlockQiState(int currentQi, int totalDrained, long lastTouchTime) {
        this.currentQi = currentQi;
        this.totalDrained = totalDrained;
        this.lastTouchTime = lastTouchTime;
        this.regenRemainder = 0.0;
    }
}
