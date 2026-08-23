package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.friday.cultivation.client.JadeOverlayCompat;
import org.junit.jupiter.api.Test;

class JadeOverlayCompatTest {
    @Test
    void reservedBottomTracksMovedAndStackedBossBars() {
        assertEquals(0, JadeOverlayCompat.reservedBottom(24, 9, 19, 0));
        assertEquals(33, JadeOverlayCompat.reservedBottom(24, 9, 19, 1));
        assertEquals(52, JadeOverlayCompat.reservedBottom(24, 9, 19, 2));
        assertEquals(71, JadeOverlayCompat.reservedBottom(24, 9, 19, 3));
    }

    @Test
    void jadeHeightFieldCompensatesForItsVanillaOrigin() {
        assertEquals(45, JadeOverlayCompat.jadeHeightFieldValue(33),
                "Jade 11.13.2 会用 bossbarHeight-12 作为 Tooltip Y，必须补回原版起点12");
    }
}
