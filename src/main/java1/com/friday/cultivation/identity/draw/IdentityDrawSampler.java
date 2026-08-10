package com.friday.cultivation.identity.draw;

import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.physique.Physique;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * 身份抽卡 — 随机抽样器
 * 复刻自原模组 com.xiaoxiang.cultivation.cultivation.draw.IdentityDrawSampler
 */
public final class IdentityDrawSampler {

    private IdentityDrawSampler() {
    }

    public static IdentityDrawDeck sampleNew(Random rng) {
        return new IdentityDrawDeck(sampleCards(rng), 0);
    }

    public static IdentityDrawDeck roll(IdentityDrawDeck old, Random rng) {
        return old.withRoll(sampleCards(rng));
    }

    private static List<DrawCard> sampleCards(Random rng) {
        List<String> ids = sampleIdentityIds(rng, 3);
        List<String> roots = sampleSpiritRootIds(rng, 3);
        ArrayList<DrawCard> cards = new ArrayList<>(3);
        int n = Math.min(ids.size(), roots.size());
        for (int i = 0; i < n; ++i) {
            cards.add(new DrawCard(ids.get(i), roots.get(i)));
        }
        return cards;
    }

    private static List<String> sampleIdentityIds(Random rng, int count) {
        ArrayList<Identity> pool = new ArrayList<>(Identity.selectableOrigins());
        Collections.shuffle(pool, rng);
        ArrayList<String> picked = new ArrayList<>(count);
        for (int i = 0; i < count && i < pool.size(); ++i) {
            picked.add(pool.get(i).id());
        }
        return picked;
    }

    /**
     * 灵根权重:
     * LEGENDARY=3, EPIC=8, RARE=15, UNCOMMON=1, COMMON=0
     */
    private static int weightOf(SpiritRoot.Rarity r) {
        return switch (r) {
            case SPECIAL -> 3;
            case SSR -> 8;
            case SR -> 15;
            case R -> 1;
            case NORMAL -> 0;
        };
    }

    private static List<String> sampleSpiritRootIds(Random rng, int count) {
        ArrayList<SpiritRoot> candidates = new ArrayList<>();
        ArrayList<Integer> weights = new ArrayList<>();
        int totalWeight = 0;
        for (SpiritRoot r : SpiritRoot.values()) {
            int w;
            if (!r.isSelectableRoot() || (w = weightOf(r.rarity())) <= 0) continue;
            candidates.add(r);
            weights.add(w);
            totalWeight += w;
        }
        ArrayList<String> picked = new ArrayList<>(count);
        for (int draw = 0; draw < count && !candidates.isEmpty(); ++draw) {
            int target = rng.nextInt(totalWeight);
            int idx = -1;
            int acc = 0;
            for (int i = 0; i < weights.size(); ++i) {
                if ((acc += weights.get(i)) <= target) continue;
                idx = i;
                break;
            }
            if (idx < 0) {
                idx = candidates.size() - 1;
            }
            picked.add(candidates.get(idx).id());
            totalWeight -= weights.get(idx);
            candidates.remove(idx);
            weights.remove(idx);
        }
        return picked;
    }

    public static Identity randomIdentity(Random rng) {
        List<Identity> origins = Identity.selectableOrigins();
        if (origins.isEmpty()) {
            return Identity.LONE_CULTIVATOR;
        }
        return origins.get(rng.nextInt(origins.size()));
    }

    public static SpiritRoot randomSpiritRoot(Random rng) {
        List<String> ids = sampleSpiritRootIds(rng, 1);
        return ids.isEmpty() ? SpiritRoot.HEAVENLY_HIDDEN : SpiritRoot.byId(ids.get(0));
    }

    public static Physique randomPhysique(Random rng) {
        List<Physique> all = Physique.selectableValues();
        if (all.isEmpty()) {
            return Physique.MORTAL_BODY;
        }
        return all.get(rng.nextInt(all.size()));
    }
}
