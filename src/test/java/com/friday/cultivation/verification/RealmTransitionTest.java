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
}
