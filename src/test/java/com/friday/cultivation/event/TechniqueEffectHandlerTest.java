package com.friday.cultivation.event;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

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
        TechniqueEffectHandler.clearLegacyTribulationHealthModifier(maxHealth);
        assertNull(maxHealth.getModifier(legacyId));
    }
}
