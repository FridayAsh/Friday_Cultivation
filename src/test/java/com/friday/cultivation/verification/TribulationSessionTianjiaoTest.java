package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.TribulationBonusSnapshot;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.event.tribulation.TribulationScalingHelper;
import com.friday.cultivation.event.tribulation.TribulationSession;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import org.junit.jupiter.api.Test;

/** 天骄来源必须固定在渡劫 Session，而不是成功时重新读取玩家当前资质。 */
class TribulationSessionTianjiaoTest {

    @Test
    void sessionTierIsTheRewardSourceAndIsPersistedOnTheSnapshot() {
        CultivationData data = new CultivationData();
        TribulationSession session = TribulationSession.create(
                TribulationSpec.of(4, 1, 40), false,
                Realm.QI_REFINING, Realm.QI_REFINING.firstSubStage(),
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage(),
                "SOVEREIGN_OF_DAOS");
        data.startTribulation(session.spec(), false, Realm.FOUNDATION_BUILDING,
                Realm.FOUNDATION_BUILDING.firstSubStage(), session.tierId());

        assertEquals(0.50, TribulationScalingHelper.rewardPercent(data.getTribulationSession()), 0.000001);
        TribulationBonusSnapshot snapshot = data.captureTribulationBonus(null, 0.50,
                Realm.FOUNDATION_BUILDING, Realm.FOUNDATION_BUILDING.firstSubStage(), session);

        assertEquals("SOVEREIGN_OF_DAOS", snapshot.sourceTierId());
        assertEquals("realm", snapshot.sourceRouteId());
        assertEquals(0.0, snapshot.sourceCompositeScore(), 0.000001);
    }
}
