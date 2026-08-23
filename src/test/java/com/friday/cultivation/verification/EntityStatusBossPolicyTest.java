package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.client.EntityStatusBossPolicy;
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
}
