package com.friday.cultivation.verification;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import java.util.List;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class RealmTopologyTest {
    @Test
    void mainChainHasExplicitTwentyRealmsAndNoLooseImmortal() {
        List<Realm> expected = List.of(
                Realm.MORTAL, Realm.BODY_TEMPERING, Realm.QI_REFINING,
                Realm.FOUNDATION_BUILDING, Realm.GOLDEN_CORE, Realm.NASCENT_SOUL,
                Realm.SOUL_FORMATION, Realm.VOID_REFINING, Realm.BODY_INTEGRATION,
                Realm.MAHAYANA, Realm.TRIBULATION_TRANSCENDENCE, Realm.TRUE_IMMORTAL,
                Realm.MYSTIC_IMMORTAL, Realm.IMMORTAL_LORD, Realm.IMMORTAL_VENERABLE,
                Realm.IMMORTAL_KING, Realm.HALF_SAGE, Realm.SAGE,
                Realm.HALF_EMPEROR, Realm.GREAT_EMPEROR);

        assertEquals(expected, RealmTopology.mainChain());
        assertEquals(21, RealmTopology.selectionOrder().size());
        assertFalse(RealmTopology.mainChain().contains(Realm.LOOSE_IMMORTAL));
        assertEquals(Realm.LOOSE_IMMORTAL, RealmTopology.selectionOrder().get(11));
    }

    @Test
    void stableIdsAndMainTransitionsDoNotUseEnumDeclarationOrder() {
        assertSame(Realm.MYSTIC_IMMORTAL, RealmTopology.require("mystic_immortal"));
        assertSame(Realm.TRUE_IMMORTAL, RealmTopology.nextMain(Realm.TRIBULATION_TRANSCENDENCE).orElseThrow());
        assertSame(Realm.HALF_SAGE, RealmTopology.nextMain(Realm.IMMORTAL_KING).orElseThrow());
        assertTrue(RealmTopology.previousMain(Realm.MYSTIC_IMMORTAL).orElseThrow() == Realm.TRUE_IMMORTAL);
        assertSame(Realm.GREAT_EMPEROR, RealmTopology.mainChain().get(19));
    }

    @Test
    void looseImmortalIsASeparateBranchForComparisons() {
        assertEquals(RealmTopology.RealmRelation.LOOSE_IMMORTAL_BRANCH,
                RealmTopology.relationOf(Realm.LOOSE_IMMORTAL));
        assertFalse(RealmTopology.isAtLeast(Realm.LOOSE_IMMORTAL, Realm.TRUE_IMMORTAL));
        assertTrue(RealmTopology.isAtLeast(Realm.TRUE_IMMORTAL, Realm.LOOSE_IMMORTAL));
        assertTrue(RealmTopology.isAtLeast(Realm.LOOSE_IMMORTAL, Realm.TRIBULATION_TRANSCENDENCE));
    }

    @Test
    void fullRealmAndSubstageProgressIsStrictlyOrdered() {
        List<Realm> chain = RealmTopology.mainChain();
        for (int i = 0; i < chain.size(); i++) {
            Realm realm = chain.get(i);
            assertTrue(RealmTopology.isAtLeast(realm, realm.lastSubStage(), realm, realm.firstSubStage()));
            if (realm.subStageCount() > 1) {
                assertFalse(RealmTopology.isAtLeast(realm, realm.firstSubStage(), realm, realm.lastSubStage()));
            }
            if (i + 1 < chain.size()) {
                Realm next = chain.get(i + 1);
                assertTrue(RealmTopology.isAtLeast(next, next.firstSubStage(), realm, realm.lastSubStage()));
                assertFalse(RealmTopology.isAtLeast(realm, realm.lastSubStage(), next, next.firstSubStage()));
            }
        }
    }

    @Test
    void businessSourcesDoNotUseRealmOrdinal() throws Exception {
        long forbidden = Files.walk(Path.of("src", "main", "java"))
                .filter(path -> path.toString().endsWith(".java"))
                .flatMap(path -> {
                    try {
                        return Files.readAllLines(path).stream();
                    } catch (Exception exception) {
                        throw new IllegalStateException(exception);
                    }
                })
                .filter(line -> (line.contains("Realm.") || line.contains("getRealm()")
                        || line.contains("realm.ordinal()") || line.contains("minRealm.ordinal()"))
                        && line.contains("ordinal()"))
                .count();
        assertEquals(0, forbidden);
    }
}
