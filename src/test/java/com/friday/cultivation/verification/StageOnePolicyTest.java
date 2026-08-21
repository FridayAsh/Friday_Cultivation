package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

/** 阶段 1 的结构回归测试：通过公开 Seam 约束止血规则，避免旧写法重新出现。 */
class StageOnePolicyTest {
    private static final Path SOURCE_ROOT = Path.of("src", "main", "java");

    @Test
    void flightEventsHaveOneRegistrationMechanism() throws IOException {
        String events = read("com/friday/cultivation/flight/CultivationFlightEvents.java");
        String mod = read("com/friday/cultivation/FridayCultivationMod.java");

        assertEquals(1, count(events, "@Mod.EventBusSubscriber"));
        assertEquals(0, count(mod, "CultivationFlightEvents.class"));
    }

    @Test
    void developmentPacketsCrossServerAuthorizationSeam() throws IOException {
        String realm = read("com/friday/cultivation/network/RealmSelectionPacket.java");
        String stats = read("com/friday/cultivation/network/EditPlayerStatsPacket.java");

        assertTrue(realm.contains("ServerAuthorization.canSelectRealm(player)"));
        assertTrue(stats.contains("ServerAuthorization.canEditPlayerStats(player)"));
    }

    @Test
    void loginRestoresFlightFromCapabilityInsteadOfClearingIt() throws IOException {
        String capabilityEvents = read("com/friday/cultivation/event/CapabilityEvents.java");
        String flight = read("com/friday/cultivation/flight/CultivationFlightHandler.java");

        assertTrue(capabilityEvents.contains("CultivationFlightHandler.restoreAfterLogin(player2)"));
        assertTrue(!capabilityEvents.contains("data.clearSwordFlight()"));
        assertTrue(flight.contains("cd.getSwordFlightStack().copy()"));
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(SOURCE_ROOT.resolve(relativePath), StandardCharsets.UTF_8);
    }

    private static int count(String text, String needle) {
        int count = 0;
        int from = 0;
        while ((from = text.indexOf(needle, from)) >= 0) {
            count++;
            from += needle.length();
        }
        return count;
    }
}
