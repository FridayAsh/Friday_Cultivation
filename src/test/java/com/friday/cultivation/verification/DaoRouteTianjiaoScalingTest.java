package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.GoldenCoreDao;
import com.friday.cultivation.event.tribulation.TribulationScalingHelper;
import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.event.tribulation.TribulationTier;
import org.junit.jupiter.api.Test;

/** 筑基道和金丹道必须与普通渡劫共用同一套天骄缩放算法。 */
class DaoRouteTianjiaoScalingTest {

    @Test
    void everyTribulatingFoundationDaoUsesEveryTianjiaoTier() {
        for (FoundationDao dao : new FoundationDao[]{FoundationDao.EARTH, FoundationDao.HEAVEN}) {
            TribulationSpec base = TribulationSpec.of(dao.tribulationWaves(), 1, 40);
            for (TribulationTier tier : TribulationTier.values()) {
                assertScaled(base, tier, "筑基道=" + dao + ", 天骄=" + tier);
            }
        }
    }

    @Test
    void everyGoldenCoreDaoUsesEveryTianjiaoTier() {
        for (GoldenCoreDao dao : new GoldenCoreDao[]{
                GoldenCoreDao.HUMAN, GoldenCoreDao.BLOOD,
                GoldenCoreDao.EARTH, GoldenCoreDao.HEAVEN}) {
            TribulationSpec base = TribulationSpec.of(
                    dao.tribulationStrikes(), 1, dao.tribulationDamage());
            for (TribulationTier tier : TribulationTier.values()) {
                assertScaled(base, tier, "金丹道=" + dao + ", 天骄=" + tier);
            }
        }
    }

    private static void assertScaled(TribulationSpec base, TribulationTier tier, String context) {
        TribulationSpec actual = TribulationScalingHelper.scaleSpec(tier, base);
        int expectedBolts = Math.max(1,
                (int)Math.round((double)base.totalBolts() * tier.difficultyMult()));
        int expectedWaves = Math.max(1,
                (int)Math.ceil((double)expectedBolts / Math.max(1, base.boltsPerWave())));
        int expectedDamage = Math.max(1,
                (int)Math.round((double)base.strikeDamage() * tier.difficultyMult()));

        assertEquals(expectedWaves, actual.waves(), context + " 的波数错误");
        assertEquals(base.boltsPerWave(), actual.boltsPerWave(), context + " 的每波雷数错误");
        assertEquals(expectedDamage, actual.strikeDamage(), context + " 的伤害错误");
    }
}
