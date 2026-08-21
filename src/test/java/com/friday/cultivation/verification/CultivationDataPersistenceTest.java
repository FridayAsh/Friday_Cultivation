package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.event.tribulation.TribulationType;
import net.minecraft.nbt.CompoundTag;
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
        original.incrementDaoFruitTotalEaten();
        original.incrementDaoFruitTotalEaten();
        original.startTribulation(TribulationSpec.ratio(3, 2, 0.25));
        original.addTribulationBonus(0.50, Realm.FOUNDATION_BUILDING);

        CompoundTag encoded = original.serializeNBT();
        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(encoded);

        assertEquals(CultivationData.CURRENT_DATA_VERSION, loaded.getDataVersion());
        assertEquals(original.getRealm(), loaded.getRealm());
        assertEquals(original.getBreakthroughHpBonus(), loaded.getBreakthroughHpBonus());
        assertEquals(original.getBreakthroughQiBonus(), loaded.getBreakthroughQiBonus());
        assertEquals(original.getDaoFruitTotalEaten(), loaded.getDaoFruitTotalEaten());
        assertEquals(original.getTribulationDamageRatio(), loaded.getTribulationDamageRatio(), 0.000001);
        assertEquals(original.getTribulationType().id(), loaded.getTribulationType().id());
        assertEquals(original.activeTribulationMultiplier(), loaded.activeTribulationMultiplier(), 0.000001);
    }

    @Test
    void cloneCopiesTheSameFieldsAsThePersistenceContract() {
        CultivationData original = new CultivationData();
        RealmTransition.apply(original, RealmTransition.Request.adminEdit(
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage()));
        original.applyBreakthroughBonus(false);
        original.incrementDaoFruitTotalEaten();
        original.startTribulation(TribulationSpec.ratio(2, 1, 0.4));
        original.addTribulationBonus(0.25, Realm.GOLDEN_CORE);

        CultivationData clone = new CultivationData();
        clone.copyFrom(original);

        assertEquals(original.getDataVersion(), clone.getDataVersion());
        assertEquals(original.getRealm(), clone.getRealm());
        assertEquals(original.getBreakthroughHpBonus(), clone.getBreakthroughHpBonus());
        assertEquals(original.getBreakthroughQiBonus(), clone.getBreakthroughQiBonus());
        assertEquals(original.getDaoFruitTotalEaten(), clone.getDaoFruitTotalEaten());
        assertEquals(original.getTribulationDamageRatio(), clone.getTribulationDamageRatio(), 0.000001);
        assertEquals(original.getTribulationType().id(), clone.getTribulationType().id());
        assertEquals(original.activeTribulationMultiplier(), clone.activeTribulationMultiplier(), 0.000001);
    }

    @Test
    void unknownTribulationIdUsesExplicitSafeFallback() {
        CompoundTag tag = new CultivationData().serializeNBT();
        tag.putString("tribulationType", "future_unknown_tribulation");

        CultivationData loaded = new CultivationData();
        loaded.deserializeNBT(tag);

        assertSame(TribulationType.LIGHTNING, loaded.getTribulationType());
    }
}
