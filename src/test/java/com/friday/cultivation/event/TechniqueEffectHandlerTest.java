package com.friday.cultivation.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.TribulationBonusSnapshot;
import com.friday.cultivation.cultivation.realm.Realm;
import java.util.UUID;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class TechniqueEffectHandlerTest {
    @BeforeAll
    static void bootstrapMinecraftRegistries() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void clearsLegacyTribulationHealthModifierFromPlayerAttributes() {
        AttributeInstance maxHealth = new AttributeInstance(Attributes.MAX_HEALTH, ignored -> { });
        UUID legacyId = UUID.nameUUIDFromBytes("xiaoxiang.tribulation.hpMult".getBytes());
        maxHealth.addPermanentModifier(new AttributeModifier(legacyId,
                "legacy_tribulation_hp_mult", 0.50, AttributeModifier.Operation.MULTIPLY_TOTAL));

        assertNotNull(maxHealth.getModifier(legacyId));
        TechniqueEffectHandler.syncTribulationHealthModifier(maxHealth, null);
        assertNull(maxHealth.getModifier(legacyId));
    }

    @Test
    void syncRemovesSnapshotWhenCurrentSubstageFallsBelowItsMilestone() {
        CultivationData data = new CultivationData();
        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        data.recordTribulationBonus(new TribulationBonusSnapshot(
                "realm:void_refining:middle", Realm.VOID_REFINING.id(),
                0.30, 1314.0, 20676L, 17, 17, 17, 17, 17));

        AttributeInstance maxHealth = new AttributeInstance(Attributes.MAX_HEALTH, ignored -> { });
        UUID snapshotId = UUID.nameUUIDFromBytes("xiaoxiang.tribulation.hpSnapshot".getBytes());

        TechniqueEffectHandler.syncTribulationHealthModifier(maxHealth, data);
        assertNull(maxHealth.getModifier(snapshotId));

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.subStageAt(1)));
        TechniqueEffectHandler.syncTribulationHealthModifier(maxHealth, data);
        assertNotNull(maxHealth.getModifier(snapshotId));
        assertEquals(1314.0, maxHealth.getModifier(snapshotId).getAmount(), 0.000001);

        RealmTransition.apply(data, RealmTransition.Request.adminEdit(
                Realm.VOID_REFINING, Realm.VOID_REFINING.firstSubStage()));
        TechniqueEffectHandler.syncTribulationHealthModifier(maxHealth, data);
        assertNull(maxHealth.getModifier(snapshotId));
    }
}
