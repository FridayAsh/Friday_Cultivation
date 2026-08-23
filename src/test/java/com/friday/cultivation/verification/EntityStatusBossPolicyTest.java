package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.client.EntityStatusBossPolicy;
import java.util.List;
import org.junit.jupiter.api.Test;

class EntityStatusBossPolicyTest {
    @Test
    void standardForgeTagAndVanillaBossTypesAlwaysQualify() {
        assertTrue(EntityStatusBossPolicy.isBossCandidate(true, false, false, 20.0D));
        assertTrue(EntityStatusBossPolicy.isBossCandidate(false, true, false, 20.0D));
    }

    @Test
    void untaggedHighHealthHostileModEntityUsesFallbackClassification() {
        assertTrue(EntityStatusBossPolicy.isBossCandidate(false, false, true, 248.0D));
        assertFalse(EntityStatusBossPolicy.isBossCandidate(false, false, true, 99.99D));
        assertFalse(EntityStatusBossPolicy.isBossCandidate(false, false, false, 248.0D));
    }

    @Test
    void matchingStandardBossOverlaySuppressesFridayFallback() {
        List<EntityStatusBossPolicy.ExistingBossBar> bars = List.of(
                new EntityStatusBossPolicy.ExistingBossBar("Ice and Fire Troll", 0.77D, 12, 19));

        assertTrue(EntityStatusBossPolicy.hasCorrespondingBossBar(
                "  ICE   AND FIRE TROLL  ", 0.20D, bars));
        assertTrue(EntityStatusBossPolicy.hasCorrespondingBossBar(
                "Localized title differs", 0.75D, bars));
    }

    @Test
    void unrelatedBossOverlayDoesNotHideMissingModBossBar() {
        List<EntityStatusBossPolicy.ExistingBossBar> bars = List.of(
                new EntityStatusBossPolicy.ExistingBossBar("Unrelated Boss", 0.25D, 12, 19));

        assertFalse(EntityStatusBossPolicy.hasCorrespondingBossBar(
                "Ice and Fire Troll", 0.90D, bars));
        assertFalse(EntityStatusBossPolicy.hasCorrespondingBossBar(
                "Ice and Fire Troll", 0.90D, List.of()));
    }

    @Test
    void fallbackBarStartsAfterAnyExistingVanillaBars() {
        assertTrue(EntityStatusBossPolicy.nextFreeBossBarY(List.of()) == 12);
        assertTrue(EntityStatusBossPolicy.nextFreeBossBarY(List.of(
                new EntityStatusBossPolicy.ExistingBossBar("A", 1.0D, 12, 19),
                new EntityStatusBossPolicy.ExistingBossBar("B", 1.0D, 31, 19))) == 50);
    }
}
