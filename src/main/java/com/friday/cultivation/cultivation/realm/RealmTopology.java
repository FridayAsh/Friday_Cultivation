package com.friday.cultivation.cultivation.realm;

import java.util.Collections;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * 境界顺序的唯一解释 Module。
 *
 * <p>Interface 使用稳定 id、显式主链和旁支关系；枚举声明顺序不是等级、持久化或协议的一部分。
 * {@link Realm} 只保留领域数据和兼容查询，所有调用者应通过本 Module 判断顺序。</p>
 */
public final class RealmTopology {
    public enum RealmRelation {
        MAIN,
        LOOSE_IMMORTAL_BRANCH
    }

    private static final List<Realm> MAIN_CHAIN = List.of(
            Realm.MORTAL,
            Realm.BODY_TEMPERING,
            Realm.QI_REFINING,
            Realm.FOUNDATION_BUILDING,
            Realm.GOLDEN_CORE,
            Realm.NASCENT_SOUL,
            Realm.SOUL_FORMATION,
            Realm.VOID_REFINING,
            Realm.BODY_INTEGRATION,
            Realm.MAHAYANA,
            Realm.TRIBULATION_TRANSCENDENCE,
            Realm.TRUE_IMMORTAL,
            Realm.MYSTIC_IMMORTAL,
            Realm.IMMORTAL_LORD,
            Realm.IMMORTAL_VENERABLE,
            Realm.IMMORTAL_KING,
            Realm.HALF_SAGE,
            Realm.SAGE,
            Realm.HALF_EMPEROR,
            Realm.GREAT_EMPEROR
    );

    /** UI/选择顺序：旁支显示在渡劫后，但不成为主链 nextMain 的一步。 */
    private static final List<Realm> SELECTION_ORDER = List.of(
            Realm.MORTAL,
            Realm.BODY_TEMPERING,
            Realm.QI_REFINING,
            Realm.FOUNDATION_BUILDING,
            Realm.GOLDEN_CORE,
            Realm.NASCENT_SOUL,
            Realm.SOUL_FORMATION,
            Realm.VOID_REFINING,
            Realm.BODY_INTEGRATION,
            Realm.MAHAYANA,
            Realm.TRIBULATION_TRANSCENDENCE,
            Realm.LOOSE_IMMORTAL,
            Realm.TRUE_IMMORTAL,
            Realm.MYSTIC_IMMORTAL,
            Realm.IMMORTAL_LORD,
            Realm.IMMORTAL_VENERABLE,
            Realm.IMMORTAL_KING,
            Realm.HALF_SAGE,
            Realm.SAGE,
            Realm.HALF_EMPEROR,
            Realm.GREAT_EMPEROR
    );

    private static final Map<String, Realm> BY_ID;
    private static final Map<Realm, Integer> MAIN_INDEX;

    static {
        Map<String, Realm> byId = new HashMap<>();
        EnumMap<Realm, Integer> mainIndex = new EnumMap<>(Realm.class);
        for (int i = 0; i < MAIN_CHAIN.size(); i++) {
            Realm realm = MAIN_CHAIN.get(i);
            byId.put(realm.id(), realm);
            mainIndex.put(realm, i);
        }
        byId.put(Realm.LOOSE_IMMORTAL.id(), Realm.LOOSE_IMMORTAL);
        BY_ID = Collections.unmodifiableMap(byId);
        MAIN_INDEX = Collections.unmodifiableMap(mainIndex);
    }

    private RealmTopology() {
    }

    public static Realm require(String stableId) {
        return find(stableId).orElseThrow(() -> new IllegalArgumentException("Unknown realm id: " + stableId));
    }

    public static Optional<Realm> find(String stableId) {
        return Optional.ofNullable(BY_ID.get(stableId));
    }

    /** 仅供旧存档迁移 Adapter 使用；新数据禁止保存枚举序号。 */
    public static Optional<Realm> fromLegacyEnumOrdinal(int ordinal) {
        Realm[] values = Realm.values();
        return ordinal >= 0 && ordinal < values.length
                ? Optional.of(values[ordinal])
                : Optional.empty();
    }

    /** 仅供旧渡劫账本迁移 Adapter 读取/写回过渡格式；新账本使用稳定 id。 */
    public static int legacyEnumOrdinal(Realm realm) {
        if (realm == null) {
            return -1;
        }
        Realm[] values = Realm.values();
        for (int i = 0; i < values.length; i++) {
            if (values[i] == realm) {
                return i;
            }
        }
        return -1;
    }

    public static RealmRelation relationOf(Realm realm) {
        return realm == Realm.LOOSE_IMMORTAL
                ? RealmRelation.LOOSE_IMMORTAL_BRANCH
                : RealmRelation.MAIN;
    }

    public static List<Realm> mainChain() {
        return MAIN_CHAIN;
    }

    public static List<Realm> selectionOrder() {
        return SELECTION_ORDER;
    }

    public static Optional<Realm> nextMain(Realm realm) {
        if (realm == null || relationOf(realm) != RealmRelation.MAIN) {
            return Optional.empty();
        }
        int index = MAIN_INDEX.get(realm);
        return index + 1 < MAIN_CHAIN.size()
                ? Optional.of(MAIN_CHAIN.get(index + 1))
                : Optional.empty();
    }

    public static Optional<Realm> previousMain(Realm realm) {
        if (realm == null || relationOf(realm) != RealmRelation.MAIN) {
            return Optional.empty();
        }
        int index = MAIN_INDEX.get(realm);
        return index > 0 ? Optional.of(MAIN_CHAIN.get(index - 1)) : Optional.empty();
    }

    /** 返回稳定的主链位置；旁支散仙使用真仙位置，且不改变 nextMain。 */
    public static int progressionIndex(Realm realm) {
        if (realm == null) {
            return -1;
        }
        if (realm == Realm.LOOSE_IMMORTAL) {
            return MAIN_INDEX.get(Realm.TRUE_IMMORTAL);
        }
        return MAIN_INDEX.get(realm);
    }

    /** 返回包含子阶段的稳定进度位置，数字层按 1-based 转成连续位置。 */
    public static int progressionIndex(Realm realm, SubStage subStage) {
        if (realm == null) {
            return -1;
        }
        int offset = 0;
        for (Realm item : MAIN_CHAIN) {
            if (item == realm) {
                break;
            }
            offset += item.subStageCount();
        }
        if (realm == Realm.LOOSE_IMMORTAL) {
            offset = 0;
            for (Realm item : MAIN_CHAIN) {
                if (item == Realm.TRUE_IMMORTAL) {
                    break;
                }
                offset += item.subStageCount();
            }
        }
        int stage = subStage == null ? 0 : subStage.level();
        if (realm.usesNumericLevels()) {
            stage = Math.max(0, stage - 1);
        }
        return offset + stage;
    }

    public static boolean isAtLeast(Realm actual, Realm required) {
        if (actual == null || required == null) {
            return false;
        }
        if (actual == Realm.LOOSE_IMMORTAL) {
            return required == Realm.LOOSE_IMMORTAL
                    || (relationOf(required) == RealmRelation.MAIN
                    && progressionIndex(required) < progressionIndex(actual));
        }
        if (required == Realm.LOOSE_IMMORTAL) {
            return progressionIndex(actual) >= progressionIndex(required);
        }
        return progressionIndex(actual) >= progressionIndex(required);
    }

    public static boolean isBefore(Realm actual, Realm required) {
        return actual != null && required != null && !isAtLeast(actual, required);
    }

    public static boolean isKnown(Realm realm) {
        return realm != null && (MAIN_INDEX.containsKey(realm) || realm == Realm.LOOSE_IMMORTAL);
    }
}
