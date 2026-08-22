package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.realm.Realm;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RealmTransitionTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void tokenTransitionUsesOneCommonPostcondition() {
        CultivationData data = new CultivationData();

        RealmTransition.Result result = RealmTransition.apply(data,
                RealmTransition.Request.realmToken(Realm.FOUNDATION_BUILDING,
                        Realm.FOUNDATION_BUILDING.firstSubStage(), 0, 100L));

        assertTrue(result.changed());
        assertEquals(Realm.FOUNDATION_BUILDING, data.getRealm());
        assertEquals(data.getMaxQi() / 2L, data.getCurrentQi());
        assertEquals(0L, data.getCultivationProgress());
        assertTrue(result.zhenyuan().automaticPerAttribute() > 0);
    }

    @Test
    void breakthroughTransitionAppliesRewardBeforeFillingResources() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.lastSubStage()));
        data.setCultivationProgress(data.getMaxCultivation());

        data.advanceOnSuccess();

        assertEquals(Realm.GOLDEN_CORE, data.getRealm());
        assertTrue(data.getBreakthroughHpBonus() > 0L);
        assertEquals(data.getMaxQi(), data.getCurrentQi());
        assertEquals(0L, data.getCultivationProgress());
    }

    @Test
    void demotionThroughTheSeamDisablesBelowThresholdReward() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        data.recordTribulationBonus(data.captureTribulationBonus(null, 0.50,
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        assertTrue(data.getTribulationHealthBonus() > 0.0);

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.QI_REFINING, Realm.QI_REFINING.firstSubStage()));

        assertEquals(0.0, data.getTribulationHealthBonus(), 0.000001);

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage()));
        assertTrue(data.getTribulationHealthBonus() > 0.0);
    }

    @Test
    void demotionToMortalDisablesTribulationAndOrdinaryBreakthroughBonuses() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage()));
        data.applyBreakthroughBonus(true);
        data.recordTribulationBonus(new com.friday.cultivation.cultivation.TribulationBonusSnapshot(
                "realm:golden_core:early", Realm.GOLDEN_CORE.id(),
                0.50, 100.0, 50L, 5, 5, 5, 5, 5));

        assertTrue(data.getBreakthroughHpBonus() > 0L);
        assertTrue(data.getTribulationHealthBonus() > 0.0);

        RealmTransition.apply(data, RealmTransition.Request.realmToken(
                Realm.MORTAL, Realm.MORTAL.firstSubStage(), 0, 200L));

        assertEquals(0L, data.getBreakthroughHpBonus());
        assertEquals(0L, data.getBreakthroughQiBonus());
        assertEquals(0.0, data.getTribulationHealthBonus(), 0.000001);
        assertEquals(0L, data.getMaxQi());
        assertEquals(0L, data.getCurrentQi());

        RealmTransition.apply(data, RealmTransition.Request.realmToken(
                Realm.GOLDEN_CORE, Realm.GOLDEN_CORE.firstSubStage(), 0, 300L));
        assertTrue(data.getBreakthroughHpBonus() > 0L);
        assertTrue(data.getBreakthroughQiBonus() > 0L);
        assertTrue(data.getTribulationHealthBonus() > 0.0);
    }

    @Test
    void sameRealmSubstageDemotionDisablesLatestOrdinaryBreakthroughBonus() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.subStageAt(1)));
        data.applyBreakthroughBonus(false);
        assertTrue(data.getBreakthroughHpBonus() > 0L);

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        assertEquals(0L, data.getBreakthroughHpBonus());
        assertEquals(0L, data.getBreakthroughQiBonus());

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.subStageAt(1)));
        assertTrue(data.getBreakthroughHpBonus() > 0L);
        assertTrue(data.getBreakthroughQiBonus() > 0L);
    }
}
