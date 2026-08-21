package com.friday.cultivation.cultivation;

import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.realm.SubStage;

/**
 * The single write seam for player realm transitions.
 *
 * <p>Adapters (tokens, packets, breakthrough code, and later tribulation
 * controllers) describe intent with a {@link Request}; this module owns the
 * state mutation and the common postconditions.  Realm ordering remains in
 * RealmTopology, while this module owns the transition transaction.</p>
 */
public final class RealmTransition {
    private RealmTransition() {
    }

    public enum Reason {
        REALM_TOKEN,
        REALM_SELECTION,
        ADMIN_EDIT,
        BREAKTHROUGH,
        TRIBULATION_SUCCESS,
        TRIBULATION_FAILURE,
        LOOSE_IMMORTAL_CHOICE,
        REINCARNATION
    }

    public enum ResourcePolicy {
        PRESERVE,
        HALF,
        HALF_IF_CHANGED,
        FULL
    }

    public enum RewardPolicy {
        NONE,
        MINOR_BREAKTHROUGH,
        MAJOR_BREAKTHROUGH
    }

    public record Request(
            Realm targetRealm,
            SubStage targetSubStage,
            Reason reason,
            ResourcePolicy resourcePolicy,
            RewardPolicy rewardPolicy,
            boolean rebuildZhenyuan,
            boolean resetProgress,
            boolean resetWuDao,
            int looseImmortalLevel,
            long gameTime
    ) {
        public Request {
            reason = reason == null ? Reason.REALM_SELECTION : reason;
            resourcePolicy = resourcePolicy == null ? ResourcePolicy.PRESERVE : resourcePolicy;
            rewardPolicy = rewardPolicy == null ? RewardPolicy.NONE : rewardPolicy;
            looseImmortalLevel = Math.max(0, looseImmortalLevel);
            gameTime = Math.max(0L, gameTime);
        }

        public static Request realmSelection(Realm realm, SubStage subStage, int looseLevel, long gameTime) {
            return new Request(realm, subStage, Reason.REALM_SELECTION, ResourcePolicy.HALF,
                    RewardPolicy.NONE, true, true, true, looseLevel, gameTime);
        }

        public static Request realmToken(Realm realm, SubStage subStage, int looseLevel, long gameTime) {
            return new Request(realm, subStage, Reason.REALM_TOKEN, ResourcePolicy.HALF,
                    RewardPolicy.NONE, true, true, true, looseLevel, gameTime);
        }

        public static Request adminEdit(Realm realm, SubStage subStage) {
            return new Request(realm, subStage, Reason.ADMIN_EDIT, ResourcePolicy.HALF_IF_CHANGED,
                    RewardPolicy.NONE, false, true, false, 0, 0L);
        }

        public static Request breakthrough(Realm realm, SubStage subStage, RewardPolicy rewardPolicy) {
            return new Request(realm, subStage, Reason.BREAKTHROUGH, ResourcePolicy.FULL,
                    rewardPolicy, false, true, true, 0, 0L);
        }
    }

    public record Result(
            boolean changed,
            boolean realmChanged,
            Realm realm,
            SubStage subStage,
            CultivationData.ZhenyuanBaselineResult zhenyuan
    ) {
        public static Result unchanged(CultivationData data) {
            return new Result(false, false, data.getRealm(), data.getSubStage(),
                    new CultivationData.ZhenyuanBaselineResult(
                            data.getUnallocatedZhenyuan(),
                            CultivationData.computeAutomaticZhenyuanAttrPerStat(data.getRealm(), data.getSubStage())));
        }
    }

    public static Result apply(CultivationData data, Request request) {
        if (data == null) {
            throw new IllegalArgumentException("CultivationData is required");
        }
        if (request == null) {
            throw new IllegalArgumentException("RealmTransition.Request is required");
        }

        Realm targetRealm = request.targetRealm() == null ? Realm.MORTAL : request.targetRealm();
        SubStage targetSub = normalizeSubStage(targetRealm, request.targetSubStage());
        Realm oldRealm = data.getRealm();
        SubStage oldSub = data.getSubStage();
        boolean realmChanged = oldRealm != targetRealm || oldSub != targetSub;

        // CultivationData owns the demotion/reward-ledger invalidation invariant.
        data.setRealm(targetRealm);
        data.setSubStage(targetSub);

        if (RealmTopology.isBefore(targetRealm, oldRealm)) {
            data.syncAutomaticZhenyuanAfterRealmDemotion(oldRealm, oldSub, targetRealm, targetSub);
        }

        if (targetRealm == Realm.LOOSE_IMMORTAL) {
            int level = request.looseImmortalLevel() > 0 ? request.looseImmortalLevel() : 1;
            data.setSoulState(false);
            data.setGhostCultivator(false);
            data.setReincarnationPending(false);
            data.setReincarnationReady(false);
            data.setLooseImmortalTribulations(level);
            data.setNextLooseImmortalTribulationTick(level >= 9
                    ? -1L : request.gameTime() + 12000000L);
            data.setSubStage(Realm.LOOSE_IMMORTAL.firstSubStage());
            targetSub = data.getSubStage();
        }

        CultivationData.ZhenyuanBaselineResult baseline = new CultivationData.ZhenyuanBaselineResult(
                data.getUnallocatedZhenyuan(),
                CultivationData.computeAutomaticZhenyuanAttrPerStat(targetRealm, targetSub));
        if (request.rebuildZhenyuan()) {
            baseline = data.syncZhenyuanToRealmBaseline(targetRealm, targetSub);
        }

        applyBreakthroughReward(data, targetRealm, request.rewardPolicy());

        if (request.resetProgress() && (request.resourcePolicy() != ResourcePolicy.HALF_IF_CHANGED || realmChanged)) {
            data.setCultivationProgress(0L);
        }
        if (request.resetWuDao() && (request.resourcePolicy() != ResourcePolicy.HALF_IF_CHANGED || realmChanged)) {
            data.setWuDaoProgress(0L);
        }

        switch (request.resourcePolicy()) {
            case HALF -> data.setCurrentQi(data.getMaxQi() / 2L);
            case HALF_IF_CHANGED -> {
                if (realmChanged) {
                    data.setCurrentQi(data.getMaxQi() / 2L);
                } else {
                    data.setCurrentQi(data.getCurrentQi());
                }
            }
            case FULL -> data.setCurrentQi(data.getMaxQi());
            case PRESERVE -> data.setCurrentQi(data.getCurrentQi());
        }

        if (targetRealm == Realm.BODY_TEMPERING) {
            data.refreshBodyTemperingInheritedHp();
        }
        return new Result(true, realmChanged, targetRealm, targetSub, baseline);
    }

    /** Applies the common failure-side demotion postcondition. */
    public static Result applyFailure(CultivationData data, Realm targetRealm, SubStage targetSub) {
        Result result = apply(data, new Request(targetRealm, targetSub, Reason.TRIBULATION_FAILURE,
                ResourcePolicy.PRESERVE, RewardPolicy.NONE, false, true, true, 0, 0L));
        data.clearTribulationBonus();
        data.setCurrentQi(0L);
        return result;
    }

    private static SubStage normalizeSubStage(Realm realm, SubStage requested) {
        if (requested == null) {
            return realm.firstSubStage();
        }
        SubStage canonical = realm.subStageAt(requested.level());
        return canonical == null ? realm.firstSubStage() : canonical;
    }

    private static void applyBreakthroughReward(CultivationData data, Realm targetRealm, RewardPolicy policy) {
        if (policy == RewardPolicy.MAJOR_BREAKTHROUGH) {
            data.addUnallocatedZhenyuan(5);
            data.addAllZhenyuanAttributes(5);
            data.applyBreakthroughBonus(true);
        } else if (policy == RewardPolicy.MINOR_BREAKTHROUGH
                && targetRealm != Realm.MORTAL
                && targetRealm != Realm.BODY_TEMPERING
                && targetRealm != Realm.LOOSE_IMMORTAL) {
            int reward = 1 + data.getSpiritRoot().bonus().extraZhenyuanPerSubLevel()
                    + PhysiqueBonusHelper.extraZhenyuanPerMinor(data.getPhysique());
            data.addUnallocatedZhenyuan(reward);
            data.addAllZhenyuanAttributes(1);
            data.applyBreakthroughBonus(false);
        }
    }
}
