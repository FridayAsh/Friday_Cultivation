package com.friday.cultivation.spell;

/**
 * 法术轮盘布局（严格照搬原模组 com.xiaoxiang.cultivation.cultivation.spell.SpellWheelLayout）
 * 8个槽位 + 中心槽位，3x3网格布局
 */
public final class SpellWheelLayout {
    public static final int[] OCT_X = new int[]{0, 1, 2, 0, 2, 0, 1, 2};
    public static final int[] OCT_Y = new int[]{0, 0, 0, 1, 1, 2, 2, 2};
    public static final int GRID_CENTER = 1;
    public static final int SLOT_COUNT = 8;

    private SpellWheelLayout() {}

    public static int offsetX(int slot) {
        return OCT_X[slot] - 1;
    }

    public static int offsetY(int slot) {
        return OCT_Y[slot] - 1;
    }

    public static int closestSlot(double dx, double dy, double deadZone) {
        double dist = Math.sqrt(dx * dx + dy * dy);
        if (dist < deadZone) {
            return -1;
        }
        double mouseAng = Math.atan2(dy, dx);
        int best = 0;
        double bestDiff = Double.MAX_VALUE;
        for (int i = 0; i < 8; ++i) {
            double slotAng = Math.atan2(SpellWheelLayout.offsetY(i), SpellWheelLayout.offsetX(i));
            double diff = Math.abs(SpellWheelLayout.angleDiff(mouseAng, slotAng));
            if (!(diff < bestDiff)) continue;
            bestDiff = diff;
            best = i;
        }
        return best;
    }

    public static double angleDiff(double a, double b) {
        double d = a - b;
        while (d > Math.PI) d -= Math.PI * 2;
        while (d < -Math.PI) d += Math.PI * 2;
        return d;
    }
}