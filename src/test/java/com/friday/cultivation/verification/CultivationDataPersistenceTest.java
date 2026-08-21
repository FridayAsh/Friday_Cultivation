package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.event.tribulation.TribulationType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.server.Bootstrap;
import net.minecraft.SharedConstants;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Stage 3 contract tests for the player progress aggregate.
 *
 * <p>The test intentionally exercises the fields that previously drifted
 * between NBT and PlayerEvent.Clone.  SyncCultivationDataPacket also uses
 * CultivationData#serializeNBT, so this round-trip is the single codec seam
 * shared by persistence, clone verification, and network synchronization.</p>
 */
class CultivationDataPersistenceTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void nbtRoundTripPreservesProgressAndTribulationState() {
        CultivationData original = new CultivationData();
        RealmTransition.apply(original, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        original.applyBreakthroughBonus(true);
        original.startTribulation(TribulationSpec.ratio(3, 2, 0.25), false,
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage(), "DAO_BONE");
        original.recordTribulationBonus(original.captureTribulationBonus(null, 0.50,
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));

        CompoundTag encoded = original.serializeNBT();
        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(encoded);

        assertEquals(CultivationData.CURRENT_DATA_VERSION, loaded.getDataVersion());
        assertEquals(original.getRealm(), loaded.getRealm());
        assertEquals(original.getBreakthroughHpBonus(), loaded.getBreakthroughHpBonus());
        assertEquals(original.getBreakthroughQiBonus(), loaded.getBreakthroughQiBonus());
        assertEquals(original.getTribulationDamageRatio(), loaded.getTribulationDamageRatio(), 0.000001);
        assertEquals(original.getTribulationType().id(), loaded.getTribulationType().id());
        assertEquals(original.getTribulationSession().spec(), loaded.getTribulationSession().spec());
        assertEquals(original.getTribulationSession().sourceRealmId(), loaded.getTribulationSession().sourceRealmId());
        assertEquals(original.getTribulationSession().targetRealmId(), loaded.getTribulationSession().targetRealmId());
        assertEquals(original.getTribulationSession().tierId(), loaded.getTribulationSession().tierId());
        assertEquals(original.getTribulationBonusSnapshots().size(), loaded.getTribulationBonusSnapshots().size());
    }

    @Test
    void cloneCopiesTheSameFieldsAsThePersistenceContract() {
        CultivationData original = new CultivationData();
        RealmTransition.apply(original, RealmTransition.Request.adminEdit(
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage()));
        original.applyBreakthroughBonus(false);
        original.startTribulation(TribulationSpec.ratio(2, 1, 0.4), false,
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage(), "DAO_BONE");
        original.recordTribulationBonus(original.captureTribulationBonus(null, 0.25,
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage()));

        CultivationData clone = new CultivationData();
        clone.copyFrom(original);

        assertEquals(original.getDataVersion(), clone.getDataVersion());
        assertEquals(original.getRealm(), clone.getRealm());
        assertEquals(original.getBreakthroughHpBonus(), clone.getBreakthroughHpBonus());
        assertEquals(original.getBreakthroughQiBonus(), clone.getBreakthroughQiBonus());
        assertEquals(original.getTribulationDamageRatio(), clone.getTribulationDamageRatio(), 0.000001);
        assertEquals(original.getTribulationType().id(), clone.getTribulationType().id());
        assertEquals(original.getTribulationSession().spec(), clone.getTribulationSession().spec());
        assertEquals(original.getTribulationBonusSnapshots().size(), clone.getTribulationBonusSnapshots().size());
    }

    @Test
    void unknownTribulationIdUsesExplicitSafeFallback() {
        CompoundTag tag = new CultivationData().serializeNBT();
        tag.putString("tribulationType", "future_unknown_tribulation");

        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(tag);

        assertSame(TribulationType.LIGHTNING, loaded.getTribulationType());
    }

    @Test
    void clearTribulationRemovesTheWholeSessionAndLegacyMirrors() {
        CultivationData data = new CultivationData();
        data.startTribulation(TribulationSpec.ratio(2, 3, 0.4), false,
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage(), "DAO_BONE");

        data.clearTribulation();

        assertEquals(null, data.getTribulationSession());
        assertEquals(0.0, data.getTribulationDamageRatio(), 0.000001);
        assertSame(TribulationType.LIGHTNING, data.getTribulationType());
    }

    @Test
    void legacyMortalDataCannotRevivePermanentBreakthroughBonus() {
        CompoundTag legacy = new CultivationData().serializeNBT();
        legacy.putString("realm", Realm.MORTAL.id());
        legacy.remove("breakthroughBonusTargetRealm");
        legacy.putLong("breakthroughHpBonus", 999L);
        legacy.putLong("breakthroughQiBonus", 9999L);

        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(legacy);

        assertEquals(0L, loaded.getBreakthroughHpBonus());
        assertEquals(0L, loaded.getBreakthroughQiBonus());
        assertEquals(0L, loaded.getMaxQi());
    }

    @Test
    void versionOneLedgerMigratesSubstageAndDisablesItBelowMilestone() {
        CompoundTag legacyPlayer = new CultivationData().serializeNBT();
        legacyPlayer.putString("realm", Realm.VOID_REFINING.id());
        legacyPlayer.putString("subStage", Realm.VOID_REFINING.firstSubStage().id());
        legacyPlayer.putLong("breakthroughHpBonus", 124L);
        legacyPlayer.putLong("breakthroughQiBonus", 620L);
        legacyPlayer.putString("breakthroughBonusTargetRealm", Realm.VOID_REFINING.id());
        legacyPlayer.remove("breakthroughBonusTargetSubStage");
        CompoundTag legacySnapshot = new CompoundTag();
        legacySnapshot.putInt("version", 1);
        legacySnapshot.putString("rewardKey", "realm:void_refining:middle");
        legacySnapshot.putString("targetRealmId", Realm.VOID_REFINING.id());
        legacySnapshot.putDouble("sourcePercent", 0.30);
        legacySnapshot.putDouble("healthBonus", 1314.0);
        ListTag ledger = new ListTag();
        ledger.add(legacySnapshot);
        legacyPlayer.put("tribulationBonusLedger", ledger);

        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(legacyPlayer);

        assertEquals(0.0, loaded.getTribulationHealthBonus(), 0.000001);
        assertEquals(0L, loaded.getBreakthroughHpBonus());
        CompoundTag migratedPlayer = loaded.serializeNBT();
        assertEquals("middle", migratedPlayer.getString("breakthroughBonusTargetSubStage"));
        CompoundTag migratedSnapshot = migratedPlayer
                .getList("tribulationBonusLedger", 10).getCompound(0);
        assertEquals(2, migratedSnapshot.getInt("version"));
        assertEquals("middle", migratedSnapshot.getString("targetSubStageId"));
    }
}
