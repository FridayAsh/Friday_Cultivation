/*
 * Decompiled with CFR 0.152.
 */
package com.friday.cultivation.cultivation.draw;

import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.draw.DrawCard;
import com.friday.cultivation.cultivation.draw.IdentityDrawDeck;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public final class IdentityDrawSampler {
    private IdentityDrawSampler() {
    }

    public static IdentityDrawDeck sampleNew(Random rng) {
        return new IdentityDrawDeck(IdentityDrawSampler.sampleCards(rng), 0);
    }

    public static IdentityDrawDeck roll(IdentityDrawDeck old, Random rng) {
        return old.withRoll(IdentityDrawSampler.sampleCards(rng));
    }

    private static List<DrawCard> sampleCards(Random rng) {
        List<String> ids = IdentityDrawSampler.sampleIdentityIds(rng, 3);
        List<String> roots = IdentityDrawSampler.sampleSpiritRootIds(rng, 3);
        ArrayList<DrawCard> cards = new ArrayList<DrawCard>(3);
        int n = Math.min(ids.size(), roots.size());
        for (int i = 0; i < n; ++i) {
            cards.add(new DrawCard(ids.get(i), roots.get(i)));
        }
        return cards;
    }

    private static List<String> sampleIdentityIds(Random rng, int count) {
        ArrayList<Identity> pool = new ArrayList<Identity>(Identity.selectableOrigins());
        Collections.shuffle(pool, rng);
        ArrayList<String> picked = new ArrayList<String>(count);
        for (int i = 0; i < count && i < pool.size(); ++i) {
            picked.add(((Identity)((Object)pool.get(i))).id());
        }
        return picked;
    }

    private static int weightOf(SpiritRoot.Rarity r) {
        return switch (r) {
            default -> throw new IncompatibleClassChangeError();
            case SSR -> 3;
            case SR -> 8;
            case R -> 15;
            case SPECIAL -> 1;
            case NORMAL -> 0;
        };
    }

    private static List<String> sampleSpiritRootIds(Random rng, int count) {
        ArrayList<SpiritRoot> candidates = new ArrayList<SpiritRoot>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int totalWeight = 0;
        for (SpiritRoot r : SpiritRoot.values()) {
            int w;
            if (!r.isSelectableRoot() || (w = IdentityDrawSampler.weightOf(r.rarity())) <= 0) continue;
            candidates.add(r);
            weights.add(w);
            totalWeight += w;
        }
        ArrayList<String> picked = new ArrayList<String>(count);
        for (int draw = 0; draw < count && !candidates.isEmpty(); ++draw) {
            int target = rng.nextInt(totalWeight);
            int idx = -1;
            int acc = 0;
            for (int i = 0; i < weights.size(); ++i) {
                if ((acc += ((Integer)weights.get(i)).intValue()) <= target) continue;
                idx = i;
                break;
            }
            if (idx < 0) {
                idx = candidates.size() - 1;
            }
            picked.add(((SpiritRoot)((Object)candidates.get(idx))).id());
            totalWeight -= ((Integer)weights.get(idx)).intValue();
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
        List<String> ids = IdentityDrawSampler.sampleSpiritRootIds(rng, 1);
        return ids.isEmpty() ? SpiritRoot.HEAVENLY_HIDDEN : SpiritRoot.byId(ids.get(0));
    }

    public static Physique randomPhysique(Random rng) {
        List<Physique> pool = Physique.weightedPool();
        if (pool.isEmpty()) {
            return Physique.MORTAL_BODY;
        }
        return pool.get(rng.nextInt(pool.size()));
    }
}

