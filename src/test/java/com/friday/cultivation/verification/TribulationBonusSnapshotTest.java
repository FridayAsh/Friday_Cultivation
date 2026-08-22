package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.TribulationBonusSnapshot;
import com.friday.cultivation.cultivation.realm.Realm;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

class TribulationBonusSnapshotTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void capturesFixedValuesAndDisablesOnlyBelowTargetRealm() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        long qiBefore = data.getMaxQi();
        data.recordTribulationBonus(new TribulationBonusSnapshot(
                "realm:foundation_building:early", Realm.FOUNDATION_BUILDING.id(),
                0.50, 50.0, 25L, 3, 4, 5, 6, 7));

        assertEquals(50.0, data.getTribulationHealthBonus(), 0.000001);
        assertEquals(qiBefore + 25L, data.getMaxQi());
        assertEquals(3, data.getTribulationConstitutionBonus());

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.QI_REFINING, Realm.QI_REFINING.firstSubStage()));
        assertEquals(0.0, data.getTribulationHealthBonus(), 0.000001);
        assertEquals(0L, data.getTribulationMaxQiBonus());

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        assertEquals(50.0, data.getTribulationHealthBonus(), 0.000001);
    }

    @Test
    void sameMilestoneReplacesInsteadOfStacking() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        data.recordTribulationBonus(new TribulationBonusSnapshot(
                "realm:foundation_building:early", Realm.FOUNDATION_BUILDING.id(),
                0.25, 25.0, 10L, 1, 1, 1, 1, 1));
        data.recordTribulationBonus(new TribulationBonusSnapshot(
                "realm:foundation_building:early", Realm.FOUNDATION_BUILDING.id(),
                0.50, 50.0, 20L, 2, 2, 2, 2, 2));

        assertEquals(1, data.getTribulationBonusSnapshots().size());
        assertEquals(50.0, data.getTribulationHealthBonus(), 0.000001);
        assertTrue(data.getTribulationConstitutionBonus() == 2);
    }

    @Test
    void sameRealmLowerSubstageDisablesFutureTribulationSnapshot() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        data.recordTribulationBonus(new TribulationBonusSnapshot(
                "realm:void_refining:middle", Realm.VOID_REFINING.id(),
                0.30, 1314.0, 20676L, 17, 17, 17, 17, 17));

        assertEquals(0.0, data.getTribulationHealthBonus(), 0.000001);
        assertEquals(0L, data.getTribulationMaxQiBonus());
        assertEquals(0, data.getTribulationConstitutionBonus());

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.subStageAt(1)));
        assertEquals(1314.0, data.getTribulationHealthBonus(), 0.000001);

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        assertEquals(0.0, data.getTribulationHealthBonus(), 0.000001);
    }

    @Test
    void versionOneSnapshotMigratesTargetSubstageFromRewardKey() {
        CompoundTag legacy = new CompoundTag();
        legacy.putInt("version", 1);
        legacy.putString("rewardKey", "realm:void_refining:middle");
        legacy.putString("targetRealmId", Realm.VOID_REFINING.id());
        legacy.putDouble("sourcePercent", 0.30);
        legacy.putDouble("healthBonus", 1314.0);

        TribulationBonusSnapshot migrated = TribulationBonusSnapshot.fromTag(legacy);

        assertEquals("middle", migrated.targetSubStageId());
        assertFalse(migrated.isActive(Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        assertTrue(migrated.isActive(Realm.VOID_REFINING, Realm.VOID_REFINING.subStageAt(1)));
    }

    @Test
    void legacyMultiplierAndArrayLedgerAreGoneFromPlayerData() throws Exception {
        String source = Files.readString(Path.of("src", "main", "java", "com", "friday", "cultivation", "cultivation", "CultivationData.java"));

        assertFalse(source.contains("tribulationBonusEntries"));
        assertFalse(source.contains("activeTribulationMultiplier"));
        assertFalse(source.contains("List<double[]>"));
    }
}
