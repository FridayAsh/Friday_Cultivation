/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.common.util.INBTSerializable
 */
package com.friday.cultivation.cultivation;

import com.friday.cultivation.event.tribulation.TribulationSpec;
import com.friday.cultivation.event.tribulation.TribulationSession;
import com.friday.cultivation.event.tribulation.TribulationType;
import java.util.List;

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.GoldenCoreDao;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;

public class CultivationData
implements INBTSerializable<CompoundTag> {
    public static final int CURRENT_DATA_VERSION = 4;
    public static final int MAX_TIME_ACCELERATION_MULTIPLIER = 10000;
    private static final int[] TIME_ACCELERATION_MULTIPLIERS = new int[]{2, 5, 10, 100, 1000, 10000};
    private int dataVersion = CURRENT_DATA_VERSION;
    private Realm realm = Realm.MORTAL;
    private SubStage subStage = SubStage.EARLY;
    private long currentQi = 0L;
    private long cultivationProgress = 0L;
    private long totalQiAbsorbed = 0L;
    private SpiritRoot spiritRoot = SpiritRoot.NONE;
    private Physique physique = Physique.MORTAL_BODY;
    private boolean meditating = false;
    private int timeAccelerationMultiplier = 1;
    private long timeAccelerationElapsedTicks = 0L;
    private String customName = "";
    private int gender = 1;
    private int genderEditsLeft = 5;
    private int tribulationStrikesRemaining = 0;
    private int tribulationBoltsPerWave = 1;
    private int tribulationBoltsRemainingInWave = 0;
    private int tribulationBoltCooldown = 0;
    private int tribulationBoltIndexInWave = 0;
    private int tribulationCooldown = 0;
    public static final int TRIBULATION_INTERVAL_TICKS = 20;
    public static final int TRIBULATION_CHARGE_TICKS = 60;
    private int attack = 0;
    private int defense = 0;
    private int critRate = 0;
    private int qiAbsorbRange = 0;
    private int refining = 0;
    private int refiningXp = 0;
    private int alchemy = 0;
    private int alchemyXp = 0;
    private String sectId = "";
    private String sectName = "";
    private String sectRoleId = "";
    private final Set<String> learnedSpells = new LinkedHashSet<String>();
    private final Set<String> disabledSpells = new LinkedHashSet<String>();
    private final Set<String> learnedTechniques = new LinkedHashSet<String>();
    private String equippedTechniqueId = "";
    public static final int EQUIPPED_SLOT_COUNT = 8;
    private final String[] equippedSpells = new String[8];
    private int selectedSpellSlot = -1;
    private String chargingSpellId = "";
    private long chargedQi = 0L;
    private int chargingTicks = 0;
    private int chargingEntityId = -1;
    private ItemStack swordFlightStack = ItemStack.EMPTY;
    /** 灵气飞行显式开关（双击空格激活/取消） */
    private boolean qiFlightToggled = false;
    /** 锻体加成固定生命值（锻体境界时按标准值×锻体百分比计算一次，永久继承到后续境界） */
    private double bodyTemperingHpInherited = 0.0;
    /** 悟道进度（半圣/半帝境界专用：悟道条，满后突破下一境界） */
    private long wuDaoProgress = 0L;
    /** 悟道随机源（服务端浮动/额外获得） */
    private final java.util.Random wuDaoRandom = new java.util.Random();
    /** 待播报的悟道额外事件（档位名/数值，由调用方读取后清除） */
    private String pendingWuDaoBonusName = null;
    private long pendingWuDaoBonusValue = 0L;
    private int swordFlightOriginalSlot = -1;
    /** 服务端飞行消耗计时，随 FlightState 一起持久化，避免第二套静态权威状态。 */
    private int flightTicks = 0;
    private boolean voidEscapeActive = false;
    private int voidEscapeStability = 0;
    private QiElement inverseFiveElementMark = QiElement.PURE;
    private long inverseFiveElementMarkExpiresAt = 0L;
    private int inverseFiveElementStacks = 0;
    private long inverseFiveElementStacksExpiresAt = 0L;
    private String identityId = "";
    private int unallocatedZhenyuan = 0;
    private int attrConstitution = 0;
    private int attrPhysique = 0;
    private int attrAgility = 0;
    private int attrSpellPower = 0;
    private int attrQiSea = 0;
    /** 突破累计生命加成原始值（按最新突破里程碑统一启停）。 */
    private long breakthroughHpBonus = 0L;
    /** 突破累计灵气加成原始值（按最新突破里程碑统一启停）。 */
    private long breakthroughQiBonus = 0L;
    /** 普通突破加成的最新目标境界；低于该境界时原始值保留但不生效。 */
    private String breakthroughBonusTargetRealmId = "";
    /** 普通突破加成的最新目标子阶段；与目标境界共同组成完整启用阈值。 */
    private String breakthroughBonusTargetSubStageId = "";
    private boolean zhenyuanMajorAutoRebalanceApplied = false;
    private boolean bodyDefenseEnabled = true;
    private final Set<String> disabledBonusCategories = new LinkedHashSet<String>();
    private boolean spellTerrainDestructionEnabled = true;
    private boolean spellTerrainDestructionPreferenceInitialized = false;
    private boolean spellTerrainDestructionForcedOffByServer = false;
    private double boneAge = 0.0;
    private int mortalLifespan = 0;
    private FoundationDao foundationDao = FoundationDao.NONE;
    private int zhujiDanEaten = 0;
    private int bloodPillEaten = 0;
    private int daoFruitEaten = 0;
    private boolean zhujiSecretUsed = false;
    private FoundationDao pendingFoundationDao = FoundationDao.NONE;
    private GoldenCoreDao goldenCoreDao = GoldenCoreDao.NONE;
    private int jiedanPillUsed = 0;
    private int bloodJiedanPillUsed = 0;
    private int trueBloodUsed = 0;
    private int earthEvilQiUsed = 0;
    private int heavenClearQiUsed = 0;
    private int creationFruitEaten = 0;
    private GoldenCoreDao pendingGoldenCoreDao = GoldenCoreDao.NONE;
    private int tribulationStrikeDamageOverride = 0;

    /** 当前渡劫劫种（默认雷劫） */
    private TribulationType tribulationType = TribulationType.LIGHTNING;
    /** 当前渡劫伤害比例（strikeDamage<=0 时用标准生命×比例） */
    private double tribulationDamageRatio = 0.0;
    /** 当前活动渡劫的固定计划快照；活动期间禁止从 Realm 重新构造。 */
    private TribulationSession tribulationSession;
    /** 固定快照渡劫奖励账本：同一里程碑 rewardKey 只保留一条记录。 */
    private final Map<String, TribulationBonusSnapshot> tribulationBonusLedger = new LinkedHashMap<>();
    private boolean soulState = false;
    private int soulTicks = 0;
    private boolean reincarnationPending = false;
    private boolean reincarnationReady = false;
    private int soulReaperKills = 0;
    /** 是否曾亲手击杀过大帝生灵（突破大帝的前置条件） */
    private boolean killedGreatEmperor = false;
    /** 自创帝法名称（空串=尚未自创）；自创后随机生成并存入 */
    private String imperialArtName = "";
    private int nextReaperTick = -1;
    private boolean soulDeathChoicePending = false;
    private boolean soulReaperPursuitEnabled = false;
    private int difuTicks = 0;
    private int difuReincarnationEntries = 0;
    private boolean ghostCultivator = false;
    private boolean soulReaperIdentity = false;
    private int looseImmortalTribulations = 0;
    private int looseImmortalRewardLevel = 0;
    private long nextLooseImmortalTribulationTick = -1L;
    private boolean looseImmortalChoicePending = false;
    private boolean looseImmortalTribulationActive = false;
    public static final int ZHENYUAN_REWARD_MINOR = 1;
    public static final int ZHENYUAN_REWARD_MAJOR = 5;
    public static final int ZHENYUAN_ATTR_REWARD_MINOR = 1;
    public static final int ZHENYUAN_ATTR_REWARD_MAJOR = 5;

    public static int[] allowedTimeAccelerationMultipliers() {
        return (int[])TIME_ACCELERATION_MULTIPLIERS.clone();
    }

    public static boolean isAllowedTimeAccelerationMultiplier(int multiplier) {
        for (int allowed : TIME_ACCELERATION_MULTIPLIERS) {
            if (allowed != multiplier) continue;
            return true;
        }
        return false;
    }

    public Realm getRealm() {
        return this.realm;
    }

    public int getDataVersion() {
        return this.dataVersion;
    }

    void setRealm(Realm realm) {
        Realm realm2 = this.realm = realm == null ? Realm.MORTAL : realm;
        if (this.realm == Realm.LOOSE_IMMORTAL) {
            if (this.looseImmortalTribulations <= 0) {
                this.looseImmortalTribulations = 1;
            }
            this.looseImmortalTribulations = LooseImmortalBonusHelper.clampLevel(this.looseImmortalTribulations);
            if (this.looseImmortalRewardLevel <= 0) {
                this.looseImmortalRewardLevel = 1;
            }
            this.looseImmortalRewardLevel = Math.min(LooseImmortalBonusHelper.clampLevel(this.looseImmortalRewardLevel), this.looseImmortalTribulations);
        } else {
            this.looseImmortalTribulations = 0;
            this.looseImmortalRewardLevel = 0;
            this.nextLooseImmortalTribulationTick = -1L;
            this.looseImmortalChoicePending = false;
            this.looseImmortalTribulationActive = false;
        }
        this.ensureSpellsForRealm();
    }

    public SubStage getSubStage() {
        return this.subStage;
    }

    void setSubStage(SubStage subStage) {
        this.subStage = subStage != null ? subStage : this.realm.firstSubStage();
    }

    /** 统一层号：数字层境界返回第几层（1-based），4 档境界返回 0-3 */
    public int getLevel() {
        return this.subStage.level();
    }

    /** 当前子阶段是否为该境界最高档 */
    public boolean isLastSubStage() {
        return this.subStage.isPeakFor(this.realm);
    }

    private long applyQiRefiningEnhancements(long baseMax) {
        return baseMax;
    }

    public long getCurrentQi() {
        // 凡人无灵气：强制为 0，凡人不能施放任何法术/消耗灵气
        return this.realm == Realm.MORTAL ? 0L : this.currentQi;
    }

    public long getMaxQi() {
        // 凡人没有可用灵气池；必须在所有加成计算之前短路，避免旧档突破/渡劫加成
        // 让凡人重新获得灵气并触发灵气护盾。
        if (this.realm == Realm.MORTAL) {
            return 0L;
        }
        if (this.realm == Realm.BODY_TEMPERING) {
            return 100L;
        }
        boolean maxQiBonusEnabled;
        long max;
        long base = this.realm.maxQi(this.subStage);
        if (this.spiritRoot == SpiritRoot.HEAVENLY_HIDDEN) {
            base = Math.max(1L, Math.round((double)base * 1.5));
        }
        long l = max = !(maxQiBonusEnabled = this.isBonusCategoryEnabled(CultivationBonusCategory.MAX_QI)) || this.attrQiSea <= 0 ? base : base + ZhenyuanBonusHelper.qiSeaFlatBonus(this);
        if (maxQiBonusEnabled) {
            max = CultivationData.saturatedAddLong(max, LooseImmortalBonusHelper.maxQiBonus(this));
            double physiqueMaxQiMult = PhysiqueBonusHelper.maxQiMultiplier(this.physique);
            if (physiqueMaxQiMult != 1.0) {
                max = Math.max(1L, Math.round((double)max * physiqueMaxQiMult));
            }
        }
        max = this.applyQiRefiningEnhancements(max);
        // 突破累计灵气加成：低于最新突破目标境界时保持快照但不生效。
        long breakthroughQi = this.getBreakthroughQiBonus();
        if (breakthroughQi > 0L) {
            max = CultivationData.saturatedAddLong(max, breakthroughQi);
        }
        max = CultivationData.saturatedAddLong(max, this.getTribulationMaxQiBonus());
        return max;
    }

    public void setCurrentQi(long currentQi) {
        long max = this.getMaxQi();
        this.currentQi = Math.max(0L, Math.min(currentQi, max));
    }

    public int absorbQi(int amount) {
        if (amount <= 0) {
            return 0;
        }
        this.totalQiAbsorbed += (long)amount;
        if (!this.isLooseImmortal()) {
            this.setCultivationProgress(this.cultivationProgress + (long)amount);
        }
        // 半圣/圣人/半帝/大帝：吸收灵气同时累积悟道（浮动获得：±30% 随机 + 5% 顿悟档位）
        // 第九帝界（大帝巅峰）后无下一境界，悟道已无用：不再触发悟道增加
        if (this.getWuDaoMax() > 0L && !(this.realm == Realm.GREAT_EMPEROR && this.isLastSubStage())) {
            this.gainWuDao((long)amount);
        }
        long before = this.currentQi;
        this.setCurrentQi(this.currentQi + (long)amount);
        return (int)(this.currentQi - before);
    }

    @Deprecated
    public int absorbQi(int amount, QiElement element) {
        return this.absorbQi(amount);
    }

    public boolean consumeQi(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (this.currentQi < amount) {
            return false;
        }
        this.currentQi -= amount;
        return true;
    }

    public long getTotalQiAbsorbed() {
        return this.totalQiAbsorbed;
    }

    public void setTotalQiAbsorbed(long t) {
        this.totalQiAbsorbed = t;
    }

    public long getMaxCultivation() {
        long base = CultivationData.deterministicCultivationRequirement(this.realm, this.subStage);
        double multiplier = PhysiqueBonusHelper.cultivationRequirementMultiplier(this.physique);
        return multiplier == 1.0 ? base : Math.max(1L, Math.round((double)base * multiplier));
    }

    /**
     * 确定性伪随机修为上限：同一境界/层数永远生成同一数值（读档、跨端一致），
     * 数值带随机感（非整齐整数），凡人两位数起步、曲线平缓、高境界达百万量级。
     */
    private static long deterministicCultivationRequirement(Realm realm, SubStage sub) {
        long[] bases = new long[]{60L, 150L, 800L, 2500L, 5000L, 10000L, 20000L, 40000L, 80000L, 160000L, 320000L, 700000L, 1000000L};
        int ord = realm == null ? 0 : Math.max(0, RealmTopology.progressionIndex(realm));
        long base = ord < bases.length ? bases[ord] : (long)Math.round(1000000.0 * Math.pow(1.5, (double)(ord - (bases.length - 1))));
        int count = realm == null ? 1 : Math.max(1, realm.subStageCount());
        int level = sub == null ? 0 : Math.max(0, sub.level());
        double frac = 0.0;
        if (realm != null && realm.usesNumericLevels()) {
            if (count > 1) {
                frac = (double)(level - 1) / (double)(count - 1);
            }
        } else if (count > 1) {
            frac = (double)level / (double)(count - 1);
        }
        long span = Math.max(1L, base / 4L);
        long val = Math.max(2L, base + Math.round(frac * (double)span));
        java.util.Random rnd = new java.util.Random((long)ord * 1000003L + (long)level * 7919L + 104729L);
        long jitter = 5L + (long)rnd.nextInt((int)Math.max(1L, val / 20L));
        return Math.max(2L, val - jitter);
    }

    public long getCultivationProgress() {
        return this.cultivationProgress;
    }

    public void setCultivationProgress(long v) {
        this.cultivationProgress = Math.max(0L, Math.min(v, this.getMaxCultivation()));
    }

    public SpiritRoot getSpiritRoot() {
        return this.spiritRoot;
    }

    public void setSpiritRoot(SpiritRoot s) {
        SpiritRoot next = s == null ? SpiritRoot.NONE : s;
        Physique legacy = Physique.fromLegacySpiritRootId(next.id());
        if (legacy != null) {
            this.physique = legacy;
            this.spiritRoot = SpiritRoot.NONE;
            return;
        }
        this.spiritRoot = next;
    }

    public Physique getPhysique() {
        return this.physique;
    }

    public void setPhysique(Physique p) {
        this.physique = p == null ? Physique.MORTAL_BODY : p;
        this.ensureSpellsForRealm();
        this.setCurrentQi(this.currentQi);
        this.setCultivationProgress(this.cultivationProgress);
    }

    public QiElement getInverseFiveElementMark() {
        return this.inverseFiveElementMark == null ? QiElement.PURE : this.inverseFiveElementMark;
    }

    public boolean hasActiveInverseFiveElementMark(long gameTime) {
        return this.getInverseFiveElementMark() != QiElement.PURE && gameTime < this.inverseFiveElementMarkExpiresAt;
    }

    public long getInverseFiveElementMarkExpiresAt() {
        return this.inverseFiveElementMarkExpiresAt;
    }

    public void setInverseFiveElementMark(QiElement element, long expiresAt) {
        this.inverseFiveElementMark = element == null ? QiElement.PURE : element;
        this.inverseFiveElementMarkExpiresAt = Math.max(0L, expiresAt);
    }

    public int getActiveInverseFiveElementStacks(long gameTime) {
        return gameTime < this.inverseFiveElementStacksExpiresAt ? Math.max(0, this.inverseFiveElementStacks) : 0;
    }

    public int getInverseFiveElementStacks() {
        return Math.max(0, this.inverseFiveElementStacks);
    }

    public long getInverseFiveElementStacksExpiresAt() {
        return this.inverseFiveElementStacksExpiresAt;
    }

    public void setInverseFiveElementStacks(int stacks, long expiresAt) {
        this.inverseFiveElementStacks = Math.max(0, stacks);
        this.inverseFiveElementStacksExpiresAt = Math.max(0L, expiresAt);
    }

    public void clearInverseFiveElementState() {
        this.inverseFiveElementMark = QiElement.PURE;
        this.inverseFiveElementMarkExpiresAt = 0L;
        this.inverseFiveElementStacks = 0;
        this.inverseFiveElementStacksExpiresAt = 0L;
    }

    public String getIdentityId() {
        return this.identityId;
    }

    public void setIdentityId(String id) {
        this.identityId = id != null ? id : "";
    }

    public boolean hasChosenIdentity() {
        return !this.identityId.isEmpty();
    }

    public int getUnallocatedZhenyuan() {
        return this.unallocatedZhenyuan;
    }

    public void setUnallocatedZhenyuan(int v) {
        this.unallocatedZhenyuan = Math.max(0, v);
    }

    public void addUnallocatedZhenyuan(int delta) {
        if (delta > 0) {
            this.unallocatedZhenyuan += delta;
        }
    }

    public boolean spendZhenyuanOn(ZhenyuanAttr attr) {
        if (this.unallocatedZhenyuan <= 0 || attr == null) {
            return false;
        }
        --this.unallocatedZhenyuan;
        switch (attr) {
            case CONSTITUTION: {
                ++this.attrConstitution;
                break;
            }
            case PHYSIQUE: {
                ++this.attrPhysique;
                break;
            }
            case AGILITY: {
                ++this.attrAgility;
                break;
            }
            case SPELL_POWER: {
                ++this.attrSpellPower;
                break;
            }
            case QI_SEA: {
                ++this.attrQiSea;
            }
        }
        return true;
    }

    public int getAttrConstitution() {
        return this.attrConstitution;
    }

    public int getAttrPhysique() {
        return this.attrPhysique;
    }

    public int getAttrAgility() {
        return this.attrAgility;
    }

    public int getAttrSpellPower() {
        return this.attrSpellPower;
    }

    public int getAttrQiSea() {
        return this.attrQiSea;
    }

    public void setAttrConstitution(int v) {
        this.attrConstitution = Math.max(0, v);
    }

    public void setAttrPhysique(int v) {
        this.attrPhysique = Math.max(0, v);
    }

    public void setAttrAgility(int v) {
        this.attrAgility = Math.max(0, v);
    }

    public void setAttrSpellPower(int v) {
        this.attrSpellPower = Math.max(0, v);
    }

    public void setAttrQiSea(int v) {
        this.attrQiSea = Math.max(0, v);
    }

    public long getBreakthroughHpBonus() {
        return this.isBreakthroughBonusActive() ? this.breakthroughHpBonus : 0L;
    }

    public long getBreakthroughQiBonus() {
        return this.isBreakthroughBonusActive() ? this.breakthroughQiBonus : 0L;
    }

    /**
     * 突破加成算法（锻体后的所有境界，无论大/小境界突破都累加）：
     * - 百分比化：每次突破 + 标准生命值(当前境界) × 5%，大境界 ×1.0、小境界 ×0.35
     * - 灵气加成约为生命加成的 5 倍
     * - 加成绑定到本次突破后的目标境界和子阶段；降到完整目标进度以下时只停用，
     *   重新达到目标进度后恢复。这样普通突破与渡劫固定快照使用同一条失效语义。
     */
    public void applyBreakthroughBonus(boolean majorBreakthrough) {
        if (this.realm == null || this.realm == Realm.MORTAL || this.realm == Realm.BODY_TEMPERING || this.realm == Realm.LOOSE_IMMORTAL) {
            return;
        }
        double base = this.realm.standardMaxHealth() * 0.05;
        double mult = majorBreakthrough ? 1.0 : 0.35;
        long hpGain = Math.max(1L, Math.round(base * mult));
        long qiGain = hpGain * 5L;
        this.breakthroughHpBonus += hpGain;
        this.breakthroughQiBonus += qiGain;
        this.breakthroughBonusTargetRealmId = this.realm.id();
        this.breakthroughBonusTargetSubStageId = this.subStage.id();
    }

    private boolean isBreakthroughBonusActive() {
        if (this.breakthroughHpBonus <= 0L && this.breakthroughQiBonus <= 0L) {
            return false;
        }
        Realm target = RealmTopology.find(this.breakthroughBonusTargetRealmId).orElse(null);
        if (target == null) {
            return false;
        }
        SubStage targetSub = SubStage.byId(this.breakthroughBonusTargetSubStageId, target);
        return RealmTopology.isAtLeast(this.realm, this.subStage, target, targetSub);
    }

    private SubStage inferLegacyBreakthroughTargetSubStage(Realm target) {
        SubStage inferred = this.realm == target ? this.subStage : target.firstSubStage();
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            if (!target.id().equals(snapshot.targetRealmId())) {
                continue;
            }
            SubStage snapshotSub = SubStage.byId(snapshot.targetSubStageId(), target);
            if (RealmTopology.isAtLeast(target, snapshotSub, target, inferred)) {
                inferred = snapshotSub;
            }
        }
        return inferred;
    }

    public void addAllZhenyuanAttributes(int delta) {
        if (delta <= 0) {
            return;
        }
        this.attrConstitution += delta;
        this.attrPhysique += delta;
        this.attrAgility += delta;
        this.attrSpellPower += delta;
        this.attrQiSea += delta;
    }

    public void ensureMinimumZhenyuanAttributes(int minPerAttribute) {
        int min = Math.max(0, minPerAttribute);
        this.attrConstitution = Math.max(this.attrConstitution, min);
        this.attrPhysique = Math.max(this.attrPhysique, min);
        this.attrAgility = Math.max(this.attrAgility, min);
        this.attrSpellPower = Math.max(this.attrSpellPower, min);
        this.attrQiSea = Math.max(this.attrQiSea, min);
    }

    public ZhenyuanBaselineResult syncZhenyuanToRealmBaseline(Realm targetRealm, SubStage targetSub) {
        Realm safeRealm = targetRealm == null ? Realm.MORTAL : targetRealm;
        SubStage safeSub = targetSub == null ? safeRealm.firstSubStage() : targetSub;
        int extraPerMinor = this.getSpiritRoot().bonus().extraZhenyuanPerSubLevel() + PhysiqueBonusHelper.extraZhenyuanPerMinor(this.getPhysique());
        boolean loose = safeRealm == Realm.LOOSE_IMMORTAL;
        int looseLevel = loose ? Math.max(1, this.getLooseImmortalTribulations()) : 0;
        Realm zhenyuanRealm = loose ? Realm.TRIBULATION_TRANSCENDENCE : safeRealm;
        SubStage zhenyuanSub = loose ? Realm.TRIBULATION_TRANSCENDENCE.lastSubStage() : safeSub;
        int free = CultivationData.computeTotalZhenyuanEarned(zhenyuanRealm, zhenyuanSub, extraPerMinor);
        int autoPerAttr = CultivationData.computeAutomaticZhenyuanAttrPerStat(zhenyuanRealm, zhenyuanSub);
        if (loose) {
            free += LooseImmortalBonusHelper.freeZhenyuanTotalForLevel(looseLevel);
            autoPerAttr += LooseImmortalBonusHelper.automaticZhenyuanAttributesTotalForLevel(looseLevel);
            this.looseImmortalRewardLevel = looseLevel;
        }
        this.unallocatedZhenyuan = Math.max(0, free);
        this.attrConstitution = autoPerAttr;
        this.attrPhysique = autoPerAttr;
        this.attrAgility = autoPerAttr;
        this.attrSpellPower = autoPerAttr;
        this.attrQiSea = autoPerAttr;
        this.zhenyuanMajorAutoRebalanceApplied = true;
        return new ZhenyuanBaselineResult(this.unallocatedZhenyuan, autoPerAttr);
    }

    void syncAutomaticZhenyuanAfterRealmDemotion(Realm oldRealm, SubStage oldSub, Realm targetRealm, SubStage targetSub) {
        Realm safeOldRealm = oldRealm == null ? Realm.MORTAL : oldRealm;
        SubStage safeOldSub = oldSub == null ? safeOldRealm.firstSubStage() : oldSub;
        Realm safeTargetRealm = targetRealm == null ? Realm.MORTAL : targetRealm;
        SubStage safeTargetSub = targetSub == null ? safeTargetRealm.firstSubStage() : targetSub;
        int oldAuto = CultivationData.computeAutomaticZhenyuanAttrPerStat(safeOldRealm, safeOldSub);
        int targetAuto = CultivationData.computeAutomaticZhenyuanAttrPerStat(safeTargetRealm, safeTargetSub);
        this.unallocatedZhenyuan = Math.max(0, this.unallocatedZhenyuan);
        this.attrConstitution = targetAuto + Math.max(0, this.attrConstitution - oldAuto);
        this.attrPhysique = targetAuto + Math.max(0, this.attrPhysique - oldAuto);
        this.attrAgility = targetAuto + Math.max(0, this.attrAgility - oldAuto);
        this.attrSpellPower = targetAuto + Math.max(0, this.attrSpellPower - oldAuto);
        this.attrQiSea = targetAuto + Math.max(0, this.attrQiSea - oldAuto);
        this.zhenyuanMajorAutoRebalanceApplied = true;
    }

    public int getFreelyEarnedZhenyuanApprox() {
        int auto = CultivationData.computeAutomaticZhenyuanAttrPerStat(this.realm, this.subStage);
        if (this.isLooseImmortal()) {
            auto += LooseImmortalBonusHelper.automaticZhenyuanAttributesTotalForLevel(this.getLooseImmortalTribulations());
        }
        return this.unallocatedZhenyuan + Math.max(0, this.attrConstitution - auto) + Math.max(0, this.attrPhysique - auto) + Math.max(0, this.attrAgility - auto) + Math.max(0, this.attrSpellPower - auto) + Math.max(0, this.attrQiSea - auto);
    }

    public int applyZhenyuanMajorAutoRebalanceMigration() {
        if (this.zhenyuanMajorAutoRebalanceApplied) {
            return 0;
        }
        int legacy = CultivationData.computeLegacyAutomaticZhenyuanAttrPerStat(this.realm, this.subStage);
        int current = CultivationData.computeAutomaticZhenyuanAttrPerStat(this.realm, this.subStage);
        int diff = Math.max(0, current - legacy);
        if (diff > 0) {
            this.addAllZhenyuanAttributes(diff);
        }
        this.zhenyuanMajorAutoRebalanceApplied = true;
        return diff;
    }

    public boolean isMeditating() {
        return this.meditating;
    }

    public void setMeditating(boolean m) {
        this.meditating = m;
    }

    public boolean canUseTimeAcceleration() {
        return RealmTopology.isAtLeast(this.realm, Realm.FOUNDATION_BUILDING);
    }

    public boolean isTimeAccelerationActive() {
        return this.timeAccelerationMultiplier > 1;
    }

    public int getTimeAccelerationMultiplier() {
        return Math.max(1, this.timeAccelerationMultiplier);
    }

    public long getTimeAccelerationElapsedTicks() {
        return Math.max(0L, this.timeAccelerationElapsedTicks);
    }

    public void startTimeAcceleration(int multiplier) {
        this.timeAccelerationMultiplier = Math.min(10000, Math.max(2, multiplier));
        this.timeAccelerationElapsedTicks = 0L;
    }

    public void stopTimeAcceleration() {
        this.timeAccelerationMultiplier = 1;
        this.timeAccelerationElapsedTicks = 0L;
    }

    public void tickTimeAccelerationElapsed() {
        this.tickTimeAccelerationElapsed(1L);
    }

    public void tickTimeAccelerationElapsed(long ticks) {
        if (this.isTimeAccelerationActive()) {
            this.timeAccelerationElapsedTicks = CultivationData.saturatedAddLong(this.timeAccelerationElapsedTicks, Math.max(0L, ticks));
        }
    }

    public String getCustomName() {
        return this.customName == null ? "" : this.customName;
    }

    public void setCustomName(String n) {
        this.customName = n == null ? "" : n;
    }

    public int getGender() {
        return this.gender;
    }

    public void setGender(int g) {
        this.gender = Math.max(0, Math.min(2, g));
    }

    public int getGenderEditsLeft() {
        return this.genderEditsLeft;
    }

    public void setGenderEditsLeft(int n) {
        this.genderEditsLeft = Math.max(0, n);
    }

    public int getTribulationStrikesRemaining() {
        return this.tribulationStrikesRemaining;
    }

    public int getTribulationBoltsPerWave() {
        return this.tribulationSession == null
                ? Math.max(1, this.tribulationBoltsPerWave)
                : this.tribulationSession.spec().boltsPerWave();
    }

    public boolean hasPendingTribulationBolts() {
        return this.tribulationBoltsRemainingInWave > 0;
    }

    public int getTribulationBoltsRemainingInWave() {
        return Math.max(0, this.tribulationBoltsRemainingInWave);
    }

    public int getTribulationBoltCooldown() {
        return Math.max(0, this.tribulationBoltCooldown);
    }

    public int getTribulationBoltIndexInWave() {
        return Math.max(0, this.tribulationBoltIndexInWave);
    }

    public int getTribulationCooldown() {
        return this.tribulationCooldown;
    }

    public boolean isInTribulation() {
        return this.tribulationStrikesRemaining > 0;
    }

    /** 唯一活动渡劫启动入口：先固定完整 Spec 与来源/目标上下文。 */
    public void startTribulation(TribulationSpec spec, boolean looseImmortal,
                                 Realm targetRealm, SubStage targetSubStage, String tierId) {
        TribulationSpec safe = spec == null ? TribulationSpec.of(0, 1, 0) : spec;
        this.tribulationSession = TribulationSession.create(safe, looseImmortal,
                this.realm, this.subStage, targetRealm, targetSubStage, tierId);
        this.tribulationStrikesRemaining = this.tribulationSession.spec().waves();
        this.tribulationCooldown = 60;
        this.tribulationStrikeDamageOverride = this.tribulationSession.spec().strikeDamage();
        this.tribulationBoltsPerWave = this.tribulationSession.spec().boltsPerWave();
        this.tribulationType = this.tribulationSession.spec().type();
        this.tribulationDamageRatio = this.tribulationSession.spec().damageRatio();
        this.looseImmortalTribulationActive = looseImmortal;
        this.clearPendingTribulationWave();
    }

    public TribulationSession getTribulationSession() {
        return this.tribulationSession;
    }

    public boolean hasTribulationSession() {
        return this.tribulationSession != null;
    }

    /** 当前渡劫伤害比例（0 = 用固定伤害） */
    public double getTribulationDamageRatio() {
        return this.tribulationSession == null
                ? this.tribulationDamageRatio : this.tribulationSession.spec().damageRatio();
    }

    /** 捕获渡劫前最终有效属性，生成不可重算的固定奖励快照。 */
    public TribulationBonusSnapshot captureTribulationBonus(Player player, double percent, Realm targetRealm, SubStage targetSub) {
        if (percent <= 0.0 || targetRealm == null) {
            return null;
        }
        String subId = targetSub == null ? targetRealm.firstSubStage().id() : targetSub.id();
        String rewardKey = "realm:" + targetRealm.id() + ":" + subId;
        double sourceHealth = player == null ? this.getRealm().standardMaxHealth() : Math.max(0.0, player.getMaxHealth());
        long sourceQi = Math.max(0L, this.getMaxQi());
        return new TribulationBonusSnapshot(
                rewardKey,
                targetRealm.id(),
                subId,
                percent,
                Math.max(0.0, Math.round(sourceHealth * percent)),
                CultivationData.saturatedMultiplyLong(sourceQi, percent),
                CultivationData.saturatedRoundInt((double)this.attrConstitution * percent),
                CultivationData.saturatedRoundInt((double)this.attrPhysique * percent),
                CultivationData.saturatedRoundInt((double)this.attrAgility * percent),
                CultivationData.saturatedRoundInt((double)this.attrSpellPower * percent),
                CultivationData.saturatedRoundInt((double)this.attrQiSea * percent));
    }

    /** 唯一账本写入口；同一里程碑会替换旧记录，不会重复叠加。 */
    public void recordTribulationBonus(TribulationBonusSnapshot snapshot) {
        if (snapshot != null) {
            this.tribulationBonusLedger.put(snapshot.rewardKey(), snapshot);
        }
    }

    public java.util.Collection<TribulationBonusSnapshot> getTribulationBonusSnapshots() {
        return java.util.Collections.unmodifiableCollection(this.tribulationBonusLedger.values());
    }

    private boolean isTribulationBonusActive(TribulationBonusSnapshot snapshot) {
        return snapshot != null && snapshot.isActive(this.realm, this.subStage);
    }

    public double getTribulationHealthBonus() {
        double total = 0.0;
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            if (this.isTribulationBonusActive(snapshot)) total += snapshot.healthBonus();
        }
        return total;
    }

    public long getTribulationMaxQiBonus() {
        long total = 0L;
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            if (this.isTribulationBonusActive(snapshot)) total = CultivationData.saturatedAddLong(total, snapshot.maxQiBonus());
        }
        return total;
    }

    public int getTribulationConstitutionBonus() {
        return this.activeTribulationAttributeBonus(0);
    }

    public int getTribulationPhysiqueBonus() {
        return this.activeTribulationAttributeBonus(1);
    }

    public int getTribulationAgilityBonus() {
        return this.activeTribulationAttributeBonus(2);
    }

    public int getTribulationSpellPowerBonus() {
        return this.activeTribulationAttributeBonus(3);
    }

    public int getTribulationQiSeaBonus() {
        return this.activeTribulationAttributeBonus(4);
    }

    private int activeTribulationAttributeBonus(int dimension) {
        int total = 0;
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            if (!this.isTribulationBonusActive(snapshot)) continue;
            int value = switch (dimension) {
                case 0 -> snapshot.constitutionBonus();
                case 1 -> snapshot.physiqueBonus();
                case 2 -> snapshot.agilityBonus();
                case 3 -> snapshot.spellPowerBonus();
                case 4 -> snapshot.qiSeaBonus();
                default -> 0;
            };
            total = CultivationData.saturatedAddInt(total, value);
        }
        return total;
    }

    /** 显示当前生效快照的奖励比例总和。 */
    public double getTribulationBonusPercent() {
        double sum = 0.0;
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            if (this.isTribulationBonusActive(snapshot)) sum += snapshot.sourcePercent();
        }
        return sum;
    }

    /** 仅用于转世/明确重置；境界跌落只停用，不删除账本。 */
    public void clearTribulationBonus() {
        this.tribulationBonusLedger.clear();
    }

    /** 当前渡劫劫种 */
    public TribulationType getTribulationType() {
        if (this.tribulationSession != null) {
            return this.tribulationSession.spec().type();
        }
        return this.tribulationType == null ? TribulationType.LIGHTNING : this.tribulationType;
    }

    public int getCurrentTribulationStrikeDamage() {
        if (this.tribulationSession != null) {
            return this.tribulationSession.spec().strikeDamage();
        }
        return this.tribulationStrikeDamageOverride > 0 ? this.tribulationStrikeDamageOverride : this.realm.tribulationStrikeDamage();
    }

    public void clearTribulation() {
        this.tribulationSession = null;
        this.tribulationStrikesRemaining = 0;
        this.tribulationCooldown = 0;
        this.tribulationStrikeDamageOverride = 0;
        this.tribulationBoltsPerWave = 1;
        this.tribulationType = TribulationType.LIGHTNING;
        this.tribulationDamageRatio = 0.0;
        this.looseImmortalTribulationActive = false;
        this.clearPendingTribulationWave();
    }

    public void decrementTribulationCooldown() {
        if (this.tribulationCooldown > 0) {
            --this.tribulationCooldown;
        }
    }

    public void beginTribulationWave() {
        this.tribulationBoltsRemainingInWave = this.getTribulationBoltsPerWave();
        this.tribulationBoltCooldown = 0;
        this.tribulationBoltIndexInWave = 0;
    }

    public void decrementTribulationBoltCooldown() {
        if (this.tribulationBoltCooldown > 0) {
            --this.tribulationBoltCooldown;
        }
    }

    public void consumePendingTribulationBolt(int nextDelayTicks) {
        if (this.tribulationBoltsRemainingInWave <= 0) {
            this.clearPendingTribulationWave();
            return;
        }
        --this.tribulationBoltsRemainingInWave;
        ++this.tribulationBoltIndexInWave;
        int n = this.tribulationBoltCooldown = this.tribulationBoltsRemainingInWave > 0 ? Math.max(0, nextDelayTicks) : 0;
        if (this.tribulationBoltsRemainingInWave <= 0) {
            this.tribulationBoltsRemainingInWave = 0;
        }
    }

    public void clearPendingTribulationWave() {
        this.tribulationBoltsRemainingInWave = 0;
        this.tribulationBoltCooldown = 0;
        this.tribulationBoltIndexInWave = 0;
    }

    public void decrementTribulationStrikes() {
        if (this.tribulationStrikesRemaining > 0) {
            --this.tribulationStrikesRemaining;
            this.tribulationCooldown = 20;
        }
    }

    public int getAttack() {
        return this.attack;
    }

    public void setAttack(int a) {
        this.attack = Math.max(0, a);
    }

    public int getDefense() {
        return this.defense;
    }

    public void setDefense(int d) {
        this.defense = Math.max(0, d);
    }

    public boolean isBodyDefenseEnabled() {
        return this.bodyDefenseEnabled;
    }

    public void setBodyDefenseEnabled(boolean enabled) {
        this.bodyDefenseEnabled = enabled;
    }

    public boolean isBonusCategoryEnabled(CultivationBonusCategory category) {
        if (category == null) {
            return true;
        }
        if (category == CultivationBonusCategory.BODY_DEFENSE) {
            return this.bodyDefenseEnabled;
        }
        return !this.disabledBonusCategories.contains(category.id());
    }

    public void setBonusCategoryEnabled(CultivationBonusCategory category, boolean enabled) {
        if (category == null) {
            return;
        }
        if (category == CultivationBonusCategory.BODY_DEFENSE) {
            this.setBodyDefenseEnabled(enabled);
            return;
        }
        if (enabled) {
            this.disabledBonusCategories.remove(category.id());
        } else {
            this.disabledBonusCategories.add(category.id());
        }
    }

    public boolean isSpellTerrainDestructionEnabled() {
        return this.spellTerrainDestructionEnabled;
    }

    public void setSpellTerrainDestructionEnabled(boolean enabled) {
        this.spellTerrainDestructionEnabled = enabled;
        this.spellTerrainDestructionPreferenceInitialized = true;
    }

    public boolean isSpellTerrainDestructionPreferenceInitialized() {
        return this.spellTerrainDestructionPreferenceInitialized;
    }

    public boolean initializeSpellTerrainDestructionPreference(boolean defaultEnabled) {
        if (this.spellTerrainDestructionPreferenceInitialized) {
            return false;
        }
        this.spellTerrainDestructionEnabled = defaultEnabled;
        this.spellTerrainDestructionPreferenceInitialized = true;
        return true;
    }

    public boolean isSpellTerrainDestructionForcedOffByServer() {
        return this.spellTerrainDestructionForcedOffByServer;
    }

    public boolean setSpellTerrainDestructionForcedOffByServer(boolean forcedOff) {
        if (this.spellTerrainDestructionForcedOffByServer == forcedOff) {
            return false;
        }
        this.spellTerrainDestructionForcedOffByServer = forcedOff;
        return true;
    }

    public boolean isSpellTerrainDestructionEffective() {
        return !this.spellTerrainDestructionForcedOffByServer && this.spellTerrainDestructionEnabled;
    }

    public double getBoneAge() {
        return this.boneAge;
    }

    public void setBoneAge(double v) {
        this.boneAge = Math.max(0.0, v);
    }

    public void addBoneAge(double delta) {
        this.boneAge = Math.max(0.0, this.boneAge + delta);
    }

    public int getMortalLifespan() {
        return this.mortalLifespan;
    }

    public void setMortalLifespan(int v) {
        this.mortalLifespan = Math.max(0, v);
    }

    public FoundationDao getFoundationDao() {
        return this.foundationDao;
    }

    public void setFoundationDao(FoundationDao d) {
        this.foundationDao = d == null ? FoundationDao.NONE : d;
    }

    public int getZhujiDanEaten() {
        return this.zhujiDanEaten;
    }

    public void addZhujiDanEaten(int n) {
        this.zhujiDanEaten = Math.max(0, this.zhujiDanEaten + n);
    }

    public int getBloodPillEaten() {
        return this.bloodPillEaten;
    }

    public void addBloodPillEaten(int n) {
        this.bloodPillEaten = Math.max(0, this.bloodPillEaten + n);
    }

    public int getDaoFruitEaten() {
        return this.daoFruitEaten;
    }

    public void addDaoFruitEaten(int n) {
        this.daoFruitEaten = Math.max(0, this.daoFruitEaten + n);
    }

    public boolean isZhujiSecretUsed() {
        return this.zhujiSecretUsed;
    }

    public void setZhujiSecretUsed(boolean v) {
        this.zhujiSecretUsed = v;
    }

    public FoundationDao getPendingFoundationDao() {
        return this.pendingFoundationDao;
    }

    public void setPendingFoundationDao(FoundationDao d) {
        this.pendingFoundationDao = d == null ? FoundationDao.NONE : d;
    }

    public GoldenCoreDao getGoldenCoreDao() {
        return this.goldenCoreDao;
    }

    public void setGoldenCoreDao(GoldenCoreDao d) {
        this.goldenCoreDao = d == null ? GoldenCoreDao.NONE : d;
    }

    public int getJiedanPillUsed() {
        return this.jiedanPillUsed;
    }

    public int addJiedanPillUsed(int n) {
        this.jiedanPillUsed = Math.max(0, this.jiedanPillUsed + n);
        return this.jiedanPillUsed;
    }

    public int getBloodJiedanPillUsed() {
        return this.bloodJiedanPillUsed;
    }

    public int addBloodJiedanPillUsed(int n) {
        this.bloodJiedanPillUsed = Math.max(0, this.bloodJiedanPillUsed + n);
        return this.bloodJiedanPillUsed;
    }

    public int getTrueBloodUsed() {
        return this.trueBloodUsed;
    }

    public int addTrueBloodUsed(int n) {
        this.trueBloodUsed = Math.max(0, this.trueBloodUsed + n);
        return this.trueBloodUsed;
    }

    public int getEarthEvilQiUsed() {
        return this.earthEvilQiUsed;
    }

    public int addEarthEvilQiUsed(int n) {
        this.earthEvilQiUsed = Math.max(0, this.earthEvilQiUsed + n);
        return this.earthEvilQiUsed;
    }

    public int getHeavenClearQiUsed() {
        return this.heavenClearQiUsed;
    }

    public int addHeavenClearQiUsed(int n) {
        this.heavenClearQiUsed = Math.max(0, this.heavenClearQiUsed + n);
        return this.heavenClearQiUsed;
    }

    public int getCreationFruitEaten() {
        return this.creationFruitEaten;
    }

    public int addCreationFruitEaten(int n) {
        this.creationFruitEaten = Math.max(0, this.creationFruitEaten + n);
        return this.creationFruitEaten;
    }

    public GoldenCoreDao getPendingGoldenCoreDao() {
        return this.pendingGoldenCoreDao;
    }

    public void setPendingGoldenCoreDao(GoldenCoreDao d) {
        this.pendingGoldenCoreDao = d == null ? GoldenCoreDao.NONE : d;
    }

    public FoundationDao bestEligibleFoundationDao(int boneAgeYears) {
        if (this.zhujiDanEaten >= 9 && this.daoFruitEaten >= 1 && this.zhujiSecretUsed && boneAgeYears < 21) {
            return FoundationDao.HEAVEN;
        }
        if (this.zhujiDanEaten >= 6 && this.zhujiSecretUsed) {
            return FoundationDao.EARTH;
        }
        if (this.bloodPillEaten >= 3) {
            return FoundationDao.BLOOD;
        }
        if (this.zhujiDanEaten >= 1) {
            return FoundationDao.HUMAN;
        }
        return FoundationDao.NONE;
    }

    public boolean isEligibleFoundationDao(FoundationDao dao, int boneAgeYears) {
        if (dao == null || dao == FoundationDao.NONE) {
            return false;
        }
        return switch (dao) {
            case HUMAN -> {
                if (this.zhujiDanEaten >= 1) {
                    yield true;
                }
                yield false;
            }
            case BLOOD -> {
                if (this.bloodPillEaten >= 3) {
                    yield true;
                }
                yield false;
            }
            case EARTH -> {
                if (this.zhujiDanEaten >= 6 && this.zhujiSecretUsed) {
                    yield true;
                }
                yield false;
            }
            case HEAVEN -> {
                if (this.zhujiDanEaten >= 9 && this.daoFruitEaten >= 1 && this.zhujiSecretUsed && boneAgeYears < 21) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public boolean isFoundationAllowedForGoldenCore(GoldenCoreDao dao, boolean hasBloodTransformTalisman) {
        if (dao == null || dao == GoldenCoreDao.NONE) {
            return false;
        }
        return switch (dao) {
            case HUMAN -> {
                if (this.foundationDao != FoundationDao.NONE) {
                    yield true;
                }
                yield false;
            }
            case BLOOD -> {
                if (this.foundationDao == FoundationDao.BLOOD || hasBloodTransformTalisman) {
                    yield true;
                }
                yield false;
            }
            case EARTH -> {
                if (this.foundationDao == FoundationDao.EARTH || this.foundationDao == FoundationDao.HEAVEN) {
                    yield true;
                }
                yield false;
            }
            case HEAVEN -> {
                if (this.foundationDao == FoundationDao.HEAVEN) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public boolean isEligibleGoldenCoreDao(GoldenCoreDao dao, int boneAgeYears, boolean hasBloodTransformTalisman) {
        if (!this.isFoundationAllowedForGoldenCore(dao, hasBloodTransformTalisman)) {
            return false;
        }
        return switch (dao) {
            case HUMAN -> {
                if (this.jiedanPillUsed >= 1) {
                    yield true;
                }
                yield false;
            }
            case BLOOD -> {
                if (this.bloodJiedanPillUsed >= 3 && this.trueBloodUsed >= 1) {
                    yield true;
                }
                yield false;
            }
            case EARTH -> {
                if (this.jiedanPillUsed >= 6 && this.earthEvilQiUsed >= 1) {
                    yield true;
                }
                yield false;
            }
            case HEAVEN -> {
                if (this.jiedanPillUsed >= 9 && this.heavenClearQiUsed >= 1 && this.creationFruitEaten >= 1 && boneAgeYears < 60) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    public GoldenCoreDao bestEligibleGoldenCoreDao(int boneAgeYears, boolean hasBloodTransformTalisman) {
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.HEAVEN, boneAgeYears, hasBloodTransformTalisman)) {
            return GoldenCoreDao.HEAVEN;
        }
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.EARTH, boneAgeYears, hasBloodTransformTalisman)) {
            return GoldenCoreDao.EARTH;
        }
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.BLOOD, boneAgeYears, hasBloodTransformTalisman)) {
            return GoldenCoreDao.BLOOD;
        }
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.HUMAN, boneAgeYears, hasBloodTransformTalisman)) {
            return GoldenCoreDao.HUMAN;
        }
        return GoldenCoreDao.NONE;
    }

    public void resetFoundationProgress() {
        this.zhujiDanEaten = 0;
        this.bloodPillEaten = 0;
        this.daoFruitEaten = 0;
        this.zhujiSecretUsed = false;
    }

    public void resetGoldenCoreProgress() {
        this.jiedanPillUsed = 0;
        this.bloodJiedanPillUsed = 0;
        this.trueBloodUsed = 0;
        this.earthEvilQiUsed = 0;
        this.heavenClearQiUsed = 0;
        this.creationFruitEaten = 0;
    }

    public boolean isSoulState() {
        return this.soulState;
    }

    public void setSoulState(boolean v) {
        boolean wasSoul = this.soulState;
        this.soulState = v;
        if (v) {
            if (wasSoul) {
                this.ensureSoulRuleSpellsLearned();
            } else {
                this.enableSoulRulesOnSoulEntry();
            }
        } else {
            this.soulTicks = 0;
            this.soulDeathChoicePending = false;
            this.soulReaperPursuitEnabled = false;
            this.nextReaperTick = -1;
            if (wasSoul) {
                this.removeSoulRulesAfterReturn();
            } else {
                this.removeSoulOnlySpell(Spell.GHOST_FLIGHT);
            }
        }
    }

    public int getSoulTicks() {
        return this.soulTicks;
    }

    public void setSoulTicks(int v) {
        this.soulTicks = Math.max(0, v);
    }

    public void incrementSoulTicks() {
        ++this.soulTicks;
    }

    public boolean isReincarnationPending() {
        return this.reincarnationPending;
    }

    public void setReincarnationPending(boolean v) {
        this.reincarnationPending = v;
    }

    public boolean isReincarnationReady() {
        return this.reincarnationReady;
    }

    public void setReincarnationReady(boolean v) {
        this.reincarnationReady = v;
    }

    public int getSoulReaperKills() {
        return this.soulReaperKills;
    }

    public void setSoulReaperKills(int v) {
        this.soulReaperKills = Math.max(0, v);
    }

    public boolean hasKilledGreatEmperor() {
        return this.killedGreatEmperor;
    }

    public void setKilledGreatEmperor(boolean v) {
        this.killedGreatEmperor = v;
    }

    /** 是否满足突破大帝的全部条件（击杀过大帝 + 已装备自创帝法功法） */
    public boolean canBreakthroughToGreatEmperor() {
        if (!this.hasKilledGreatEmperor()) {
            return false;
        }
        if (!this.hasCreatedImperialArt()) {
            return false;
        }
        return Technique.IMPERIAL_ART.id().equals(this.getEquippedTechniqueId());
    }

    /** 是否已自创帝法 */
    public boolean hasCreatedImperialArt() {
        return this.imperialArtName != null && !this.imperialArtName.isEmpty();
    }

    public String getImperialArtName() {
        return this.imperialArtName == null ? "" : this.imperialArtName;
    }

    public void setImperialArtName(String v) {
        this.imperialArtName = v == null ? "" : v;
    }

    public int getNextReaperTick() {
        return this.nextReaperTick;
    }

    public void setNextReaperTick(int v) {
        this.nextReaperTick = v;
    }

    public boolean isSoulDeathChoicePending() {
        return this.soulDeathChoicePending;
    }

    public void setSoulDeathChoicePending(boolean v) {
        this.soulDeathChoicePending = v;
    }

    public boolean isSoulReaperPursuitEnabled() {
        return this.soulReaperPursuitEnabled;
    }

    public void setSoulReaperPursuitEnabled(boolean v) {
        this.soulReaperPursuitEnabled = v;
    }

    public int getDifuTicks() {
        return this.difuTicks;
    }

    public void setDifuTicks(int v) {
        this.difuTicks = Math.max(0, v);
    }

    public void incrementDifuTicks() {
        ++this.difuTicks;
    }

    public void addSoulTicks(int ticks) {
        this.soulTicks = CultivationData.saturatedAddInt(this.soulTicks, ticks);
    }

    public void addDifuTicks(int ticks) {
        this.difuTicks = CultivationData.saturatedAddInt(this.difuTicks, ticks);
    }

    public int getDifuReincarnationEntries() {
        return this.difuReincarnationEntries;
    }

    public void setDifuReincarnationEntries(int v) {
        this.difuReincarnationEntries = Math.max(0, v);
    }

    public int recordDifuReincarnationEntry() {
        this.difuReincarnationEntries = CultivationData.saturatedAddInt(this.difuReincarnationEntries, 1);
        return this.difuReincarnationEntries;
    }

    public boolean isGhostCultivator() {
        return this.ghostCultivator;
    }

    public void setGhostCultivator(boolean v) {
        this.ghostCultivator = v;
    }

    public boolean isSoulReaperIdentity() {
        return this.soulReaperIdentity;
    }

    public void setSoulReaperIdentity(boolean v) {
        this.soulReaperIdentity = v;
        if (v) {
            this.learnSpell(Spell.YIN_YANG_EYE);
        }
    }

    public boolean isLooseImmortal() {
        return !this.soulState && this.realm == Realm.LOOSE_IMMORTAL && this.looseImmortalTribulations > 0;
    }

    public int getLooseImmortalTribulations() {
        return LooseImmortalBonusHelper.clampLevel(this.looseImmortalTribulations);
    }

    public void setLooseImmortalTribulations(int value) {
        this.looseImmortalTribulations = LooseImmortalBonusHelper.clampLevel(value);
        this.looseImmortalRewardLevel = Math.min(LooseImmortalBonusHelper.clampLevel(this.looseImmortalRewardLevel), this.looseImmortalTribulations);
        this.ensureSpellsForRealm();
    }

    public int getLooseImmortalRewardLevel() {
        return LooseImmortalBonusHelper.clampLevel(this.looseImmortalRewardLevel);
    }

    public long getNextLooseImmortalTribulationTick() {
        return this.nextLooseImmortalTribulationTick;
    }

    public void setNextLooseImmortalTribulationTick(long tick) {
        this.nextLooseImmortalTribulationTick = Math.max(-1L, tick);
    }

    public long getLooseImmortalTribulationRemainingTicks(long currentGameTime) {
        if (!this.isLooseImmortal() || this.looseImmortalTribulations >= 9 || this.looseImmortalTribulationActive || this.nextLooseImmortalTribulationTick < 0L) {
            return 0L;
        }
        return Math.max(0L, this.nextLooseImmortalTribulationTick - Math.max(0L, currentGameTime));
    }

    public boolean advanceLooseImmortalTribulationCountdown(long currentGameTime, long ticks) {
        if (!this.isLooseImmortal() || this.looseImmortalTribulations >= 9 || this.looseImmortalTribulationActive || this.nextLooseImmortalTribulationTick < 0L || ticks <= 0L) {
            return false;
        }
        long previous = this.nextLooseImmortalTribulationTick;
        long shifted = previous - ticks;
        if (shifted < currentGameTime) {
            shifted = currentGameTime;
        }
        this.nextLooseImmortalTribulationTick = shifted;
        return previous != shifted;
    }

    public boolean isLooseImmortalChoicePending() {
        return this.looseImmortalChoicePending;
    }

    public void setLooseImmortalChoicePending(boolean value) {
        this.looseImmortalChoicePending = value;
    }

    public boolean isLooseImmortalTribulationActive() {
        return this.looseImmortalTribulationActive;
    }

    public void becomeLooseImmortal(long nextTribulationTick) {
        RealmTransition.apply(this, new RealmTransition.Request(
                Realm.LOOSE_IMMORTAL, Realm.LOOSE_IMMORTAL.firstSubStage(),
                RealmTransition.Reason.TRIBULATION_FAILURE, RealmTransition.ResourcePolicy.HALF,
                RealmTransition.RewardPolicy.NONE, false, true, true, 1, nextTribulationTick - 12000000L));
        this.looseImmortalRewardLevel = 1;
        this.nextLooseImmortalTribulationTick = nextTribulationTick;
        this.looseImmortalChoicePending = false;
        this.looseImmortalTribulationActive = false;
        this.soulTicks = 0;
        this.difuTicks = 0;
        this.currentQi = 0L;
        this.cultivationProgress = 0L;
        this.clearTribulation();
        this.clearCharging();
        LooseImmortalBonusHelper.applyFirstTribulationPenalty(this);
        this.setCurrentQi(this.getMaxQi() / 2L);
        this.ensureSpellsForRealm();
    }

    public LooseImmortalPromotionResult promoteLooseImmortal(long nextTribulationTick) {
        if (!this.isLooseImmortal() || this.looseImmortalTribulations >= 9) {
            return LooseImmortalPromotionResult.EMPTY;
        }
        int before = this.getLooseImmortalTribulations();
        this.looseImmortalTribulations = LooseImmortalBonusHelper.clampLevel(before + 1);
        this.nextLooseImmortalTribulationTick = this.looseImmortalTribulations >= 9 ? -1L : nextTribulationTick;
        this.cultivationProgress = 0L;
        this.clearTribulation();
        LooseImmortalPromotionResult result = this.grantLooseImmortalProgressionRewards(before, this.looseImmortalTribulations);
        this.setCurrentQi(this.getMaxQi());
        this.ensureSpellsForRealm();
        return result;
    }

    public LooseImmortalPromotionResult applyMissingLooseImmortalProgressionRewards() {
        if (!this.isLooseImmortal()) {
            return LooseImmortalPromotionResult.EMPTY;
        }
        int target = this.getLooseImmortalTribulations();
        if (this.looseImmortalRewardLevel <= 0) {
            this.looseImmortalRewardLevel = 1;
        }
        return this.grantLooseImmortalProgressionRewards(this.looseImmortalRewardLevel, target);
    }

    private LooseImmortalPromotionResult grantLooseImmortalProgressionRewards(int fromLevel, int toLevel) {
        int from = LooseImmortalBonusHelper.clampLevel(fromLevel);
        int to = LooseImmortalBonusHelper.clampLevel(toLevel);
        if (to <= from) {
            this.looseImmortalRewardLevel = Math.max(this.looseImmortalRewardLevel, to);
            return new LooseImmortalPromotionResult(false, from, to, 0, 0, 0L);
        }
        int free = LooseImmortalBonusHelper.freeZhenyuanRewardBetween(from, to);
        int autoPerAttr = LooseImmortalBonusHelper.automaticZhenyuanAttributesRewardBetween(from, to);
        long maxQi = LooseImmortalBonusHelper.maxQiBonusRewardBetween(from, to);
        this.addUnallocatedZhenyuan(free);
        this.addAllZhenyuanAttributes(autoPerAttr);
        this.looseImmortalRewardLevel = to;
        return new LooseImmortalPromotionResult(true, from, to, free, autoPerAttr, maxQi);
    }

    public int getCritRate() {
        return this.critRate;
    }

    public void setCritRate(int v) {
        this.critRate = Math.max(0, Math.min(100, v));
    }

    public int getQiAbsorbRange() {
        return this.qiAbsorbRange;
    }

    public void setQiAbsorbRange(int v) {
        this.qiAbsorbRange = Math.max(0, v);
    }

    public int getRefining() {
        return this.refining;
    }

    public void setRefining(int v) {
        this.refining = Math.max(0, v);
    }

    public int getAlchemy() {
        return this.alchemy;
    }

    public void setAlchemy(int v) {
        this.alchemy = Math.max(0, v);
    }

    public RefiningRank getRefiningRank() {
        RefiningRank[] ranks = RefiningRank.values();
        int idx = Math.max(0, Math.min(ranks.length - 1, this.refining));
        return ranks[idx];
    }

    public int getRefiningXp() {
        return this.refiningXp;
    }

    public void setRefiningXp(int v) {
        this.refiningXp = Math.max(0, v);
    }

    public boolean addRefiningXp(int amount) {
        if (amount <= 0) {
            return false;
        }
        RefiningRank rank = this.getRefiningRank();
        if (rank.isMax()) {
            return false;
        }
        this.refiningXp += amount;
        boolean leveledUp = false;
        while (!rank.isMax() && this.refiningXp >= rank.xpToNext()) {
            this.refiningXp -= rank.xpToNext();
            ++this.refining;
            rank = this.getRefiningRank();
            leveledUp = true;
        }
        if (rank.isMax()) {
            this.refiningXp = 0;
        }
        return leveledUp;
    }

    public AlchemyRank getAlchemyRank() {
        AlchemyRank[] ranks = AlchemyRank.values();
        int idx = Math.max(0, Math.min(ranks.length - 1, this.alchemy));
        return ranks[idx];
    }

    public int getAlchemyXp() {
        return this.alchemyXp;
    }

    public void setAlchemyXp(int v) {
        this.alchemyXp = Math.max(0, v);
    }

    public boolean addAlchemyXp(int amount) {
        if (amount <= 0) {
            return false;
        }
        AlchemyRank rank = this.getAlchemyRank();
        if (rank.isMax()) {
            return false;
        }
        this.alchemyXp += amount;
        boolean leveledUp = false;
        while (!rank.isMax() && this.alchemyXp >= rank.xpToNext()) {
            this.alchemyXp -= rank.xpToNext();
            ++this.alchemy;
            rank = this.getAlchemyRank();
            leveledUp = true;
        }
        if (rank.isMax()) {
            this.alchemyXp = 0;
        }
        return leveledUp;
    }

    public String getSectId() {
        return this.sectId;
    }

    public void setSectId(String s) {
        String normalized;
        String string = normalized = s == null ? "" : s;
        if (!normalized.equals(this.sectId)) {
            this.sectName = "";
            this.sectRoleId = "";
        }
        this.sectId = normalized;
        if (this.sectId.isBlank()) {
            this.sectName = "";
            this.sectRoleId = "";
        }
    }

    public String getSectName() {
        return this.sectName == null ? "" : this.sectName;
    }

    public String getSectRoleId() {
        return this.sectRoleId == null ? "" : this.sectRoleId;
    }

    public boolean hasSectDisplay() {
        return !this.getSectId().isBlank() && !this.getSectName().isBlank() && !this.getSectRoleId().isBlank();
    }

    public void setSectDisplay(String sectId, String sectName, String sectRoleId) {
        this.sectId = sectId == null ? "" : sectId;
        this.sectName = sectName == null ? "" : sectName;
        String string = this.sectRoleId = sectRoleId == null ? "" : sectRoleId;
        if (this.sectId.isBlank()) {
            this.sectName = "";
            this.sectRoleId = "";
        }
    }

    public void clearSectDisplay() {
        this.sectId = "";
        this.sectName = "";
        this.sectRoleId = "";
    }

    public boolean isSolo() {
        return this.sectId == null || this.sectId.isEmpty();
    }

    public Set<String> getLearnedSpells() {
        return this.learnedSpells;
    }

    public Set<String> getDisabledSpells() {
        return this.disabledSpells;
    }

    public Set<String> getLearnedTechniques() {
        return this.learnedTechniques;
    }

    public boolean hasSpell(Spell spell) {
        return this.learnedSpells.contains(spell.id());
    }

    public boolean isSpellEnabled(Spell spell) {
        return this.hasSpell(spell) && !this.disabledSpells.contains(spell.id());
    }

    public void setSpellEnabled(Spell spell, boolean enabled) {
        if (!this.hasSpell(spell)) {
            return;
        }
        if (spell == Spell.GHOST_FLIGHT && !this.soulState) {
            this.removeSoulOnlySpell(spell);
            return;
        }
        if (enabled) {
            this.disabledSpells.remove(spell.id());
        } else {
            this.disabledSpells.add(spell.id());
        }
    }

    public void learnSpell(Spell spell) {
        if (spell == Spell.GHOST_FLIGHT && !this.soulState) {
            return;
        }
        this.learnedSpells.add(spell.id());
    }

    private void enableSoulRulesOnSoulEntry() {
        this.learnSpell(Spell.YIN_YANG_EYE);
        this.setSpellEnabled(Spell.YIN_YANG_EYE, true);
        this.learnSpell(Spell.GHOST_FLIGHT);
        this.setSpellEnabled(Spell.GHOST_FLIGHT, true);
    }

    private void ensureSoulRuleSpellsLearned() {
        this.learnSpell(Spell.YIN_YANG_EYE);
        this.learnSpell(Spell.GHOST_FLIGHT);
    }

    private void removeSoulRulesAfterReturn() {
        this.setSpellEnabled(Spell.YIN_YANG_EYE, false);
        this.removeSoulOnlySpell(Spell.GHOST_FLIGHT);
    }

    private void removeSoulOnlySpell(Spell spell) {
        String id = spell.id();
        this.learnedSpells.remove(id);
        this.disabledSpells.remove(id);
        for (int i = 0; i < 8; ++i) {
            if (!id.equals(this.equippedSpells[i])) continue;
            this.equippedSpells[i] = "";
        }
    }

    public void learnTechnique(String techId) {
        String normalized = Technique.normalizeId(techId);
        if (!normalized.isEmpty()) {
            this.learnedTechniques.add(normalized);
        }
    }

    public String getEquippedTechniqueId() {
        return this.equippedTechniqueId == null ? "" : this.equippedTechniqueId;
    }

    public void setEquippedTechniqueId(String id) {
        this.equippedTechniqueId = Technique.normalizeId(id);
    }

    public boolean hasEquippedTechnique() {
        return !this.getEquippedTechniqueId().isEmpty();
    }

    public String[] getEquippedSpells() {
        String[] copy = new String[8];
        for (int i = 0; i < 8; ++i) {
            copy[i] = this.equippedSpells[i] == null ? "" : this.equippedSpells[i];
        }
        return copy;
    }

    public String getEquippedSpellAt(int slot) {
        if (slot < 0 || slot >= 8) {
            return "";
        }
        return this.equippedSpells[slot] == null ? "" : this.equippedSpells[slot];
    }

    public void setEquippedSpellAt(int slot, String spellId) {
        if (slot < 0 || slot >= 8) {
            return;
        }
        this.equippedSpells[slot] = spellId == null ? "" : spellId;
    }

    public boolean isSpellEquipped(String spellId) {
        if (spellId == null || spellId.isEmpty()) {
            return false;
        }
        for (String s : this.equippedSpells) {
            if (!spellId.equals(s)) continue;
            return true;
        }
        return false;
    }

    public String getChargingSpellId() {
        return this.chargingSpellId == null ? "" : this.chargingSpellId;
    }

    public void setChargingSpellId(String id) {
        this.chargingSpellId = id == null ? "" : id;
    }

    public boolean isCharging() {
        return !this.getChargingSpellId().isEmpty();
    }

    public long getChargedQi() {
        return this.chargedQi;
    }

    public void setChargedQi(long v) {
        this.chargedQi = Math.max(0L, v);
    }

    public void addChargedQi(long v) {
        this.chargedQi = Math.max(0L, this.chargedQi + v);
    }

    public int getChargingTicks() {
        return this.chargingTicks;
    }

    public void incrementChargingTicks() {
        ++this.chargingTicks;
    }

    public int getChargingEntityId() {
        return this.chargingEntityId;
    }

    public void setChargingEntityId(int id) {
        this.chargingEntityId = id;
    }

    public void clearCharging() {
        this.chargingSpellId = "";
        this.chargedQi = 0L;
        this.chargingTicks = 0;
        this.chargingEntityId = -1;
    }

    public boolean isSwordFlightActive() {
        return !this.swordFlightStack.isEmpty();
    }

    public boolean isQiFlightToggled() {
        return this.qiFlightToggled;
    }

    public void setQiFlightToggled(boolean v) {
        this.qiFlightToggled = v;
    }

    /** 悟道进度（半圣/半帝专用） */
    public long getWuDaoProgress() {
        return this.wuDaoProgress;
    }

    public void setWuDaoProgress(long v) {
        this.wuDaoProgress = Math.max(0L, Math.min(v, this.getWuDaoMax()));
    }

    /** 悟道上限（半圣/圣人/半帝/大帝）：确定性伪随机，同境界/子阶段恒定，带随机感、随子阶段递增 */
    public long getWuDaoMax() {
        if (this.realm != Realm.HALF_SAGE && this.realm != Realm.SAGE && this.realm != Realm.HALF_EMPEROR && this.realm != Realm.GREAT_EMPEROR) {
            return 0L;
        }
        return CultivationData.deterministicWuDaoRequirement(this.realm, this.subStage);
    }

    /**
     * 确定性伪随机悟道上限：半圣/圣人/半帝/大帝专用，
     * 同一境界/子阶段永远生成同一数值（读档、跨端一致），
     * 数值带随机感（非整齐整数），随子阶段递增、高境界达数十万量级。
     */
    private static long deterministicWuDaoRequirement(Realm realm, SubStage sub) {
        long[] bases = new long[]{40000L, 100000L, 300000L, 900000L};
        int idx = realm == null ? -1 : RealmTopology.progressionIndex(realm)
                - RealmTopology.progressionIndex(Realm.HALF_SAGE);
        if (realm == null || idx < 0 || idx >= bases.length) {
            return 0L;
        }
        long base = bases[idx];
        int count = Math.max(1, realm.subStageCount());
        int level = sub == null ? 0 : Math.max(0, sub.level());
        double frac = 0.0;
        if (realm.usesNumericLevels()) {
            if (count > 1) {
                frac = (double)(level - 1) / (double)(count - 1);
            }
        } else if (count > 1) {
            frac = (double)level / (double)(count - 1);
        }
        long span = Math.max(1L, base / 4L);
        long val = Math.max(2L, base + Math.round(frac * (double)span));
        java.util.Random rnd = new java.util.Random(130000L + (long)idx * 1000003L + (long)level * 7919L + 104729L);
        long jitter = 5L + (long)rnd.nextInt((int)Math.max(1L, val / 20L));
        return Math.max(2L, val - jitter);
    }

    /**
     * 悟道浮动获得（半圣/圣人/半帝/大帝）：
     * 1) 基础值 = 修为获取量 × [0.7, 1.3] 连续浮动（60% 概率落在少得区间 [0.7,1.0)，40% 概率落在多得区间 [1.0,1.3]）
     * 2) 额外 5% 概率触发顿悟档位（权重抽取）：
     *    初悟+1000 / 小悟+5000 / 明悟+10000 / 彻悟+50000 / 顿悟=补满
     * 3) 触发额外档位时记录待播报事件（档位名/数值），由调用方读取后清除
     */
    public long gainWuDao(long baseAmount) {
        if (baseAmount <= 0L || this.getWuDaoMax() <= 0L) {
            return 0L;
        }
        this.pendingWuDaoBonusName = null;
        this.pendingWuDaoBonusValue = 0L;
        // 基础：悟道 = 灵气吸收量 * 20%（约为原速 1/5，避免随灵气倍率暴涨），小幅浮动 0.85~1.15
        double mult = 0.85 + this.wuDaoRandom.nextDouble() * 0.3;
        long gained = Math.max(1L, Math.round((double)baseAmount * 0.2 * mult));
        // 额外 2% 档位：数值按悟道上限的小比例（初悟0.5% 小悟1% 明悟2% 彻悟4%），
        // 避免低上限境界（如半圣 40000）触发大额档位一下子补满
        if (this.wuDaoRandom.nextDouble() < 0.02) {
            long max = this.getWuDaoMax();
            long pct05 = Math.max(1L, max / 200L);
            long pct1 = Math.max(1L, max / 100L);
            long pct2 = Math.max(1L, max / 50L);
            long pct4 = Math.max(1L, max / 25L);
            int roll = this.wuDaoRandom.nextInt(100);
            if (roll < 40) {
                gained += pct05;
                this.pendingWuDaoBonusName = "wudao_bonus.friday_cultivation.initial";
                this.pendingWuDaoBonusValue = pct05;
            } else if (roll < 70) {
                gained += pct1;
                this.pendingWuDaoBonusName = "wudao_bonus.friday_cultivation.small";
                this.pendingWuDaoBonusValue = pct1;
            } else if (roll < 90) {
                gained += pct2;
                this.pendingWuDaoBonusName = "wudao_bonus.friday_cultivation.medium";
                this.pendingWuDaoBonusValue = pct2;
            } else if (roll < 99) {
                gained += pct4;
                this.pendingWuDaoBonusName = "wudao_bonus.friday_cultivation.great";
                this.pendingWuDaoBonusValue = pct4;
            } else {
                // 顿悟：悟道值直接补满
                long remain = this.getWuDaoMax() - this.wuDaoProgress;
                this.pendingWuDaoBonusName = "wudao_bonus.friday_cultivation.enlightenment";
                this.pendingWuDaoBonusValue = Math.max(1L, remain);
                this.wuDaoProgress = this.getWuDaoMax();
                return this.pendingWuDaoBonusValue;
            }
        }
        this.setWuDaoProgress(this.wuDaoProgress + gained);
        return gained;
    }

    /** 读取并清除待播报的悟道额外事件（无则返回 null） */
    @Nullable
    public String pollWuDaoBonusName() {
        String name = this.pendingWuDaoBonusName;
        this.pendingWuDaoBonusName = null;
        return name;
    }

    /** 读取并清除待播报的悟道额外事件数值 */
    public long pollWuDaoBonusValue() {
        long value = this.pendingWuDaoBonusValue;
        this.pendingWuDaoBonusValue = 0L;
        return value;
    }

    /** 悟道是否圆满（半圣/圣人/半帝/大帝悟道满） */
    public boolean isWuDaoComplete() {
        long max = this.getWuDaoMax();
        return max > 0L && this.wuDaoProgress >= max;
    }


    public ItemStack getSwordFlightStack() {
        return this.swordFlightStack;
    }

    public int getSwordFlightOriginalSlot() {
        return this.swordFlightOriginalSlot;
    }

    public void startSwordFlight(ItemStack stack, int originalSlot) {
        this.swordFlightStack = stack == null ? ItemStack.EMPTY : stack.copy();
        this.swordFlightOriginalSlot = originalSlot;
        this.flightTicks = 0;
    }

    public void clearSwordFlight() {
        this.swordFlightStack = ItemStack.EMPTY;
        this.swordFlightOriginalSlot = -1;
        this.flightTicks = 0;
    }

    public int getFlightTicks() {
        return Math.max(0, this.flightTicks);
    }

    public int incrementFlightTicks() {
        this.flightTicks = Math.min(20, this.flightTicks + 1);
        return this.flightTicks;
    }

    public void clearFlightTicks() {
        this.flightTicks = 0;
    }

    public boolean isVoidEscapeActive() {
        return this.voidEscapeActive;
    }

    public int getVoidEscapeStability() {
        return this.voidEscapeStability;
    }

    public void startVoidEscape(int initialStability) {
        this.voidEscapeActive = true;
        this.voidEscapeStability = Math.max(1, initialStability);
    }

    public void clearVoidEscape() {
        this.voidEscapeActive = false;
        this.voidEscapeStability = 0;
    }

    public int decrementVoidEscapeStability() {
        if (!this.voidEscapeActive) {
            return 0;
        }
        this.voidEscapeStability = Math.max(0, this.voidEscapeStability - 1);
        return this.voidEscapeStability;
    }

    public int getSelectedSpellSlot() {
        return this.selectedSpellSlot;
    }

    public void setSelectedSpellSlot(int slot) {
        this.selectedSpellSlot = slot < 0 || slot >= 8 ? -1 : slot;
    }

    public String getSelectedSpellId() {
        if (this.selectedSpellSlot < 0) {
            return "";
        }
        return this.getEquippedSpellAt(this.selectedSpellSlot);
    }

    public void ensureSpellsForRealm() {
        if (RealmTopology.isAtLeast(this.realm, Realm.QI_REFINING)) {
            this.learnedSpells.add(Spell.SPIRIT_VISION.id());
            this.learnedSpells.add(Spell.QI_TRANSFER.id());
            this.learnedSpells.add(Spell.QI_SHIELD.id());
            this.learnedSpells.add(Spell.REALM_PRESSURE.id());
            if (this.physique == Physique.INNATE_SWORD_BODY) {
                this.learnedSpells.add(Spell.SWORD_AURA.id());
            }
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.FOUNDATION_BUILDING)) {
            this.learnedSpells.add(Spell.SWORD_FLIGHT.id());
            this.learnedSpells.add(Spell.BIGU.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.GOLDEN_CORE)) {
            this.learnedSpells.add(Spell.CORE_SELF_DESTRUCT.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.NASCENT_SOUL)) {
            this.learnedSpells.add(Spell.NASCENT_SOUL_OUT_OF_BODY.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.SOUL_FORMATION)) {
            this.learnedSpells.add(Spell.DIVINE_SENSE.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.VOID_REFINING)) {
            this.learnedSpells.add(Spell.VOID_STEP.id());
            this.learnedSpells.add(Spell.VOID_ESCAPE.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.BODY_INTEGRATION)) {
            this.learnedSpells.add(Spell.DHARMA_BODY_MANIFESTATION.id());
        }
        if (RealmTopology.isAtLeast(this.realm, Realm.TRUE_IMMORTAL)
                || this.realm == Realm.LOOSE_IMMORTAL) {
            this.learnedSpells.add(Spell.QI_FLIGHT.id());
        }
    }

    public boolean canBreakthrough() {
        if (this.realm == Realm.LOOSE_IMMORTAL) {
            return false;
        }
        if (this.realm == Realm.GREAT_EMPEROR && this.subStage.isPeakFor(this.realm)) {
            return false;
        }
        if (this.isInTribulation()) {
            return false;
        }
        // 半圣/圣人/半帝/大帝：需同时满足修为值满 + 悟道值满方可突破
        if (this.realm == Realm.HALF_SAGE || this.realm == Realm.SAGE || this.realm == Realm.HALF_EMPEROR || this.realm == Realm.GREAT_EMPEROR) {
            return this.cultivationProgress >= this.getMaxCultivation() && this.isWuDaoComplete();
        }
        return this.cultivationProgress >= this.getMaxCultivation();
    }

    /** 锻体加成固定生命值（锻体境界时计算一次，永久继承到后续境界） */
    public double getBodyTemperingHpInherited() {
        return this.bodyTemperingHpInherited;
    }

    public void setBodyTemperingHpInherited(double v) {
        this.bodyTemperingHpInherited = Math.max(0.0, v);
    }

    /** 锻体百分比（按层数）：1~3层 +10/20/30%，4~6层 +45/60/75%，7~9层 +95/115/135%，10层 +185% */
    public static double bodyTemperingPercent(int level) {
        return switch (level) {
            case 1 -> 0.10;
            case 2 -> 0.20;
            case 3 -> 0.30;
            case 4 -> 0.45;
            case 5 -> 0.60;
            case 6 -> 0.75;
            case 7 -> 0.95;
            case 8 -> 1.15;
            case 9 -> 1.35;
            case 10 -> 1.85;
            default -> 0.0;
        };
    }

    /** 处于锻体境界时，按锻体标准生命值(150)×锻体百分比计算一次固定加成（永久继承） */
    public void refreshBodyTemperingInheritedHp() {
        if (this.realm != Realm.BODY_TEMPERING) {
            return;
        }
        int lvl = this.subStage == null ? 1 : Math.max(1, this.subStage.level());
        this.bodyTemperingHpInherited = 150.0 * CultivationData.bodyTemperingPercent(lvl);
    }

    public void advanceOnSuccess() {
        if (this.isLooseImmortal()) {
            this.cultivationProgress = 0L;
            this.setCurrentQi(this.getMaxQi());
            return;
        }
        if (this.realm == Realm.MORTAL) {
            RealmTransition.apply(this, RealmTransition.Request.breakthrough(
                    Realm.BODY_TEMPERING, Realm.BODY_TEMPERING.firstSubStage(), RealmTransition.RewardPolicy.NONE));
            return;
        }
        if (this.subStage.isPeakFor(this.realm)) {
            if (this.realm == Realm.GREAT_EMPEROR) {
                this.setCurrentQi(this.getMaxQi());
                return;
            }
            // 主链路进阶：真仙→半圣→圣人→半帝→大帝（next() 跳过散仙旁支）
            Realm n = this.realm.next();
            if (n != this.realm) {
                if (n == Realm.FOUNDATION_BUILDING && this.pendingFoundationDao != FoundationDao.NONE) {
                    this.foundationDao = this.pendingFoundationDao;
                    this.resetFoundationProgress();
                    this.pendingFoundationDao = FoundationDao.NONE;
                }
                if (n == Realm.GOLDEN_CORE && this.pendingGoldenCoreDao != GoldenCoreDao.NONE) {
                    this.goldenCoreDao = this.pendingGoldenCoreDao;
                    this.resetGoldenCoreProgress();
                    this.pendingGoldenCoreDao = GoldenCoreDao.NONE;
                }
                RealmTransition.apply(this, RealmTransition.Request.breakthrough(
                        n, n.firstSubStage(), RealmTransition.RewardPolicy.MAJOR_BREAKTHROUGH));
                return;
            }
            this.setCurrentQi(this.getMaxQi());
            return;
        }
        SubStage next = this.subStage.nextFor(this.realm);
        RealmTransition.apply(this, RealmTransition.Request.breakthrough(
                this.realm, next, RealmTransition.RewardPolicy.MINOR_BREAKTHROUGH));
    }

    public int getTotalZhenyuanEarned() {
        return this.unallocatedZhenyuan + this.attrConstitution + this.attrPhysique + this.attrAgility + this.attrSpellPower + this.attrQiSea;
    }

    public static int computeTotalZhenyuanEarned(Realm targetRealm, SubStage targetSub, int extraPerMinor) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == Realm.LOOSE_IMMORTAL) {
            targetRealm = Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = targetRealm.lastSubStage();
        }
        // 大境界突破：每次 +5（凡人→锻体不发，故扣除 1 次）
        int majorCount = Math.max(0, RealmTopology.progressionIndex(targetRealm) - 1);
        int minorCount = 0;
        for (Realm r : RealmTopology.mainChain()) {
            if (r == targetRealm) {
                break;
            }
            if (r == Realm.MORTAL || r == Realm.BODY_TEMPERING) {
                continue;
            }
            minorCount += Math.max(0, r.subStageCount() - 1);
        }
        if (targetRealm != Realm.BODY_TEMPERING) {
            int subIdx = targetSub == null ? 0 : (targetRealm.usesNumericLevels() ? Math.max(0, targetSub.level() - 1) : targetSub.level());
            minorCount += subIdx;
        }
        return majorCount * 5 + Math.max(0, minorCount) * (1 + Math.max(0, extraPerMinor));
    }

    public static int computeAutomaticZhenyuanAttrPerStat(Realm targetRealm, SubStage targetSub) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == Realm.LOOSE_IMMORTAL) {
            targetRealm = Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = targetRealm.lastSubStage();
        }
        int majorCount = Math.max(0, RealmTopology.progressionIndex(targetRealm) - 1);
        int minorCount = 0;
        for (Realm r : RealmTopology.mainChain()) {
            if (r == targetRealm) {
                break;
            }
            if (r == Realm.MORTAL || r == Realm.BODY_TEMPERING) {
                continue;
            }
            minorCount += Math.max(0, r.subStageCount() - 1);
        }
        if (targetRealm != Realm.BODY_TEMPERING) {
            int subIdx = targetSub == null ? 0 : (targetRealm.usesNumericLevels() ? Math.max(0, targetSub.level() - 1) : targetSub.level());
            minorCount += subIdx;
        }
        return majorCount * 5 + Math.max(0, minorCount) * 1;
    }

    private static int computeLegacyAutomaticZhenyuanAttrPerStat(Realm targetRealm, SubStage targetSub) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        int majorCount = Math.max(0, RealmTopology.progressionIndex(targetRealm));
        int minorCount = 0;
        for (Realm r : RealmTopology.mainChain()) {
            if (r == targetRealm) {
                break;
            }
            minorCount += Math.max(1, r.subStageCount());
        }
        int subIdx = targetSub == null ? 0 : (targetRealm.usesNumericLevels() ? Math.max(0, targetSub.level() - 1) : targetSub.level());
        minorCount = minorCount + subIdx - 1;
        return majorCount + Math.max(0, minorCount);
    }

    private static int saturatedAddInt(int value, int delta) {
        if (delta <= 0) {
            return Math.max(0, value);
        }
        return value > Integer.MAX_VALUE - delta ? Integer.MAX_VALUE : value + delta;
    }

    private static long saturatedAddLong(long value, long delta) {
        if (delta <= 0L) {
            return Math.max(0L, value);
        }
        return value > Long.MAX_VALUE - delta ? Long.MAX_VALUE : value + delta;
    }

    private static int saturatedRoundInt(double value) {
        if (!(value > 0.0)) {
            return 0;
        }
        return value >= Integer.MAX_VALUE ? Integer.MAX_VALUE : (int)Math.round(value);
    }

    private static long saturatedMultiplyLong(long value, double multiplier) {
        if (value <= 0L || !(multiplier > 0.0)) {
            return 0L;
        }
        double result = (double)value * multiplier;
        return result >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.max(0L, Math.round(result));
    }

    public void demoteOnFailure() {
        if (this.isLooseImmortal()) {
            this.currentQi = 0L;
            this.cultivationProgress = 0L;
            this.clearTribulation();
            return;
        }
        Realm oldRealm = this.realm;
        SubStage oldSub = this.subStage;
        this.currentQi = 0L;
        this.cultivationProgress = 0L;
        this.pendingFoundationDao = FoundationDao.NONE;
        this.pendingGoldenCoreDao = GoldenCoreDao.NONE;
        this.tribulationStrikeDamageOverride = 0;
        this.tribulationBoltsPerWave = 1;
        this.clearPendingTribulationWave();
        if (this.realm == Realm.MORTAL) {
            RealmTransition.applyFailure(this, Realm.MORTAL, SubStage.EARLY);
            return;
        }
        Realm targetRealm = this.realm;
        SubStage targetSub = this.subStage;
        SubStage first = this.realm.firstSubStage();
        if (this.subStage == first || this.subStage.level() <= (this.realm.usesNumericLevels() ? 1 : 0)) {
            Realm p = this.realm.prev();
            if (p == Realm.MORTAL || p == this.realm) {
                targetRealm = Realm.MORTAL;
                targetSub = SubStage.EARLY;
            } else {
                targetRealm = p;
                targetSub = p.lastSubStage();
            }
        } else {
            targetSub = this.subStage.prevFor(this.realm);
        }
        RealmTransition.applyFailure(this, targetRealm, targetSub);
    }

    public void copyFrom(CultivationData other) {
        boolean wasSoul = this.soulState;
        this.dataVersion = other.dataVersion;
        this.realm = other.realm;
        this.subStage = other.subStage;
        this.bodyTemperingHpInherited = other.bodyTemperingHpInherited;
        this.currentQi = other.currentQi;
        this.cultivationProgress = other.cultivationProgress;
        this.totalQiAbsorbed = other.totalQiAbsorbed;
        this.spiritRoot = other.spiritRoot;
        this.physique = other.physique;
        this.meditating = other.meditating;
        this.timeAccelerationMultiplier = other.timeAccelerationMultiplier;
        this.timeAccelerationElapsedTicks = other.timeAccelerationElapsedTicks;
        this.customName = other.customName;
        this.gender = other.gender;
        this.genderEditsLeft = other.genderEditsLeft;
        this.tribulationStrikesRemaining = other.tribulationStrikesRemaining;
        this.tribulationCooldown = other.tribulationCooldown;
        this.tribulationBoltsPerWave = other.tribulationBoltsPerWave;
        this.tribulationBoltsRemainingInWave = other.tribulationBoltsRemainingInWave;
        this.tribulationBoltCooldown = other.tribulationBoltCooldown;
        this.tribulationBoltIndexInWave = other.tribulationBoltIndexInWave;
        this.tribulationType = other.tribulationType;
        this.tribulationDamageRatio = other.tribulationDamageRatio;
        this.tribulationSession = other.tribulationSession;
        this.attack = other.attack;
        this.defense = other.defense;
        this.critRate = other.critRate;
        this.qiAbsorbRange = other.qiAbsorbRange;
        this.refining = other.refining;
        this.refiningXp = other.refiningXp;
        this.alchemy = other.alchemy;
        this.alchemyXp = other.alchemyXp;
        this.sectId = other.sectId;
        this.sectName = other.sectName;
        this.sectRoleId = other.sectRoleId;
        this.learnedSpells.clear();
        this.learnedSpells.addAll(other.learnedSpells);
        this.disabledSpells.clear();
        this.disabledSpells.addAll(other.disabledSpells);
        this.learnedTechniques.clear();
        this.learnedTechniques.addAll(other.learnedTechniques);
        this.equippedTechniqueId = other.equippedTechniqueId;
        for (int i = 0; i < 8; ++i) {
            this.equippedSpells[i] = other.equippedSpells[i];
        }
        this.selectedSpellSlot = other.selectedSpellSlot;
        this.chargingSpellId = "";
        this.chargedQi = 0L;
        this.chargingTicks = 0;
        this.chargingEntityId = -1;
        this.swordFlightStack = other.swordFlightStack.copy();
        this.swordFlightOriginalSlot = other.swordFlightOriginalSlot;
        this.flightTicks = other.flightTicks;
        this.qiFlightToggled = other.qiFlightToggled;
        this.wuDaoProgress = other.wuDaoProgress;
        this.voidEscapeActive = other.voidEscapeActive;
        this.voidEscapeStability = other.voidEscapeStability;
        this.inverseFiveElementMark = other.inverseFiveElementMark;
        this.inverseFiveElementMarkExpiresAt = other.inverseFiveElementMarkExpiresAt;
        this.inverseFiveElementStacks = other.inverseFiveElementStacks;
        this.inverseFiveElementStacksExpiresAt = other.inverseFiveElementStacksExpiresAt;
        this.identityId = other.identityId;
        this.unallocatedZhenyuan = other.unallocatedZhenyuan;
        this.attrConstitution = other.attrConstitution;
        this.attrPhysique = other.attrPhysique;
        this.attrAgility = other.attrAgility;
        this.attrSpellPower = other.attrSpellPower;
        this.attrQiSea = other.attrQiSea;
        this.breakthroughHpBonus = other.breakthroughHpBonus;
        this.breakthroughQiBonus = other.breakthroughQiBonus;
        this.breakthroughBonusTargetRealmId = other.breakthroughBonusTargetRealmId;
        this.breakthroughBonusTargetSubStageId = other.breakthroughBonusTargetSubStageId;
        this.zhenyuanMajorAutoRebalanceApplied = other.zhenyuanMajorAutoRebalanceApplied;
        this.bodyDefenseEnabled = other.bodyDefenseEnabled;
        this.disabledBonusCategories.clear();
        this.disabledBonusCategories.addAll(other.disabledBonusCategories);
        this.spellTerrainDestructionEnabled = other.spellTerrainDestructionEnabled;
        this.spellTerrainDestructionPreferenceInitialized = other.spellTerrainDestructionPreferenceInitialized;
        this.spellTerrainDestructionForcedOffByServer = other.spellTerrainDestructionForcedOffByServer;
        this.boneAge = other.boneAge;
        this.mortalLifespan = other.mortalLifespan;
        this.foundationDao = other.foundationDao;
        this.zhujiDanEaten = other.zhujiDanEaten;
        this.bloodPillEaten = other.bloodPillEaten;
        this.daoFruitEaten = other.daoFruitEaten;
        this.zhujiSecretUsed = other.zhujiSecretUsed;
        this.pendingFoundationDao = other.pendingFoundationDao;
        this.goldenCoreDao = other.goldenCoreDao;
        this.jiedanPillUsed = other.jiedanPillUsed;
        this.bloodJiedanPillUsed = other.bloodJiedanPillUsed;
        this.trueBloodUsed = other.trueBloodUsed;
        this.earthEvilQiUsed = other.earthEvilQiUsed;
        this.heavenClearQiUsed = other.heavenClearQiUsed;
        this.creationFruitEaten = other.creationFruitEaten;
        this.pendingGoldenCoreDao = other.pendingGoldenCoreDao;
        this.tribulationStrikeDamageOverride = other.tribulationStrikeDamageOverride;
        this.tribulationBonusLedger.clear();
        this.tribulationBonusLedger.putAll(other.tribulationBonusLedger);
        this.soulState = other.soulState;
        this.soulTicks = other.soulTicks;
        this.reincarnationPending = other.reincarnationPending;
        this.reincarnationReady = other.reincarnationReady;
        this.soulReaperKills = other.soulReaperKills;
        this.killedGreatEmperor = other.killedGreatEmperor;
        this.imperialArtName = other.imperialArtName;
        this.nextReaperTick = other.nextReaperTick;
        this.soulDeathChoicePending = other.soulDeathChoicePending;
        this.soulReaperPursuitEnabled = other.soulReaperPursuitEnabled;
        this.difuTicks = other.difuTicks;
        this.difuReincarnationEntries = other.difuReincarnationEntries;
        this.ghostCultivator = other.ghostCultivator;
        this.soulReaperIdentity = other.soulReaperIdentity;
        this.looseImmortalTribulations = other.looseImmortalTribulations;
        this.looseImmortalRewardLevel = other.looseImmortalRewardLevel;
        this.nextLooseImmortalTribulationTick = other.nextLooseImmortalTribulationTick;
        this.looseImmortalChoicePending = other.looseImmortalChoicePending;
        this.looseImmortalTribulationActive = other.looseImmortalTribulationActive;
        if (this.soulReaperIdentity) {
            this.learnSpell(Spell.YIN_YANG_EYE);
        }
        if (this.soulState) {
            this.ensureSoulRuleSpellsLearned();
        } else if (wasSoul) {
            this.removeSoulRulesAfterReturn();
        } else {
            this.removeSoulOnlySpell(Spell.GHOST_FLIGHT);
        }
        this.ensureSpellsForRealm();
    }

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("dataVersion", CURRENT_DATA_VERSION);
        tag.putString("realm", this.realm.id());
        tag.putString("subStage", this.subStage.id());
        tag.putLong("currentQi", this.currentQi);
        tag.putLong("cultivationProgress", this.cultivationProgress);
        tag.putLong("totalQiAbsorbed", this.totalQiAbsorbed);
        tag.putString("spiritRoot", this.spiritRoot.id());
        tag.putString("physique", this.physique.id());
        tag.putBoolean("meditating", this.meditating);
        tag.putInt("timeAccelerationMultiplier", this.getTimeAccelerationMultiplier());
        tag.putLong("timeAccelerationElapsedTicks", this.getTimeAccelerationElapsedTicks());
        tag.putString("customName", this.getCustomName());
        tag.putInt("gender", this.gender);
        tag.putInt("genderEditsLeft", this.genderEditsLeft);
        tag.putInt("tribStrikes", this.tribulationStrikesRemaining);
        tag.putInt("tribCooldown", this.tribulationCooldown);
        tag.putInt("tribBoltsPerWave", this.tribulationBoltsPerWave);
        tag.putInt("tribBoltsRemainingInWave", this.tribulationBoltsRemainingInWave);
        tag.putInt("tribBoltCooldown", this.tribulationBoltCooldown);
        tag.putInt("tribBoltIndexInWave", this.tribulationBoltIndexInWave);
        tag.putInt("attack", this.attack);
        tag.putInt("defense", this.defense);
        tag.putInt("critRate", this.critRate);
        tag.putInt("qiAbsorbRange", this.qiAbsorbRange);
        tag.putInt("refining", this.refining);
        tag.putInt("refiningXp", this.refiningXp);
        tag.putInt("alchemy", this.alchemy);
        tag.putInt("alchemyXp", this.alchemyXp);
        tag.putString("sectId", this.sectId);
        tag.putString("sectName", this.getSectName());
        tag.putString("sectRoleId", this.getSectRoleId());
        ListTag spells = new ListTag();
        for (String string : this.learnedSpells) {
            spells.add(StringTag.valueOf((String)string));
        }
        tag.put("spells", (Tag)spells);
        ListTag disabled = new ListTag();
        for (String string : this.disabledSpells) {
            disabled.add(StringTag.valueOf((String)string));
        }
        tag.put("disabledSpells", (Tag)disabled);
        ListTag listTag = new ListTag();
        for (String s : this.learnedTechniques) {
            listTag.add(StringTag.valueOf((String)s));
        }
        tag.put("techniques", (Tag)listTag);
        tag.putString("equippedTechnique", this.getEquippedTechniqueId());
        tag.putString("chargingSpellId", this.getChargingSpellId());
        tag.putLong("chargedQi", this.chargedQi);
        tag.putInt("chargingTicks", this.chargingTicks);
        tag.putInt("chargingEntityId", this.chargingEntityId);
        if (!this.swordFlightStack.isEmpty()) {
            tag.put("swordFlightStack", (Tag)this.swordFlightStack.save(new CompoundTag()));
        }
        tag.putInt("swordFlightOriginalSlot", this.swordFlightOriginalSlot);
        tag.putInt("flightTicks", this.flightTicks);
        tag.putBoolean("qiFlightToggled", this.qiFlightToggled);
        tag.putLong("wuDaoProgress", this.wuDaoProgress);
        tag.putDouble("bodyTemperingHpInherited", this.bodyTemperingHpInherited);
        tag.putBoolean("voidEscapeActive", this.voidEscapeActive);
        tag.putInt("voidEscapeStability", this.voidEscapeStability);
        tag.putString("inverseFiveElementMark", this.getInverseFiveElementMark().id());
        tag.putLong("inverseFiveElementMarkExpiresAt", this.inverseFiveElementMarkExpiresAt);
        tag.putInt("inverseFiveElementStacks", this.inverseFiveElementStacks);
        tag.putLong("inverseFiveElementStacksExpiresAt", this.inverseFiveElementStacksExpiresAt);
        ListTag listTag2 = new ListTag();
        for (int i = 0; i < 8; ++i) {
            listTag2.add(StringTag.valueOf((String)(this.equippedSpells[i] == null ? "" : this.equippedSpells[i])));
        }
        tag.put("equippedSpells", (Tag)listTag2);
        tag.putInt("selectedSpellSlot", this.selectedSpellSlot);
        tag.putString("identityId", this.identityId);
        tag.putInt("unallocatedZhenyuan", this.unallocatedZhenyuan);
        tag.putInt("attrConstitution", this.attrConstitution);
        tag.putInt("attrPhysique", this.attrPhysique);
        tag.putInt("attrAgility", this.attrAgility);
        tag.putInt("attrSpellPower", this.attrSpellPower);
        tag.putInt("attrQiSea", this.attrQiSea);
        tag.putLong("breakthroughHpBonus", this.breakthroughHpBonus);
        tag.putLong("breakthroughQiBonus", this.breakthroughQiBonus);
        tag.putString("breakthroughBonusTargetRealm", this.breakthroughBonusTargetRealmId == null ? "" : this.breakthroughBonusTargetRealmId);
        tag.putString("breakthroughBonusTargetSubStage", this.breakthroughBonusTargetSubStageId == null ? "" : this.breakthroughBonusTargetSubStageId);
        tag.putBoolean("zhenyuanMajorAutoRebalanceApplied", this.zhenyuanMajorAutoRebalanceApplied);
        tag.putBoolean("bodyDefenseEnabled", this.bodyDefenseEnabled);
        ListTag disabledBonusList = new ListTag();
        for (String id : this.disabledBonusCategories) {
            CultivationBonusCategory category = CultivationBonusCategory.byId(id);
            if (category == null || category == CultivationBonusCategory.BODY_DEFENSE) continue;
            disabledBonusList.add(StringTag.valueOf((String)id));
        }
        tag.put("disabledBonusCategories", (Tag)disabledBonusList);
        tag.putBoolean("spellTerrainDestructionEnabled", this.spellTerrainDestructionEnabled);
        tag.putBoolean("spellTerrainDestructionPreferenceInitialized", this.spellTerrainDestructionPreferenceInitialized);
        tag.putBoolean("spellTerrainDestructionForcedOffByServer", this.spellTerrainDestructionForcedOffByServer);
        tag.putDouble("boneAge", this.boneAge);
        tag.putInt("mortalLifespan", this.mortalLifespan);
        tag.putString("foundationDao", this.foundationDao.id());
        tag.putInt("zhujiDanEaten", this.zhujiDanEaten);
        tag.putInt("bloodPillEaten", this.bloodPillEaten);
        tag.putInt("daoFruitEaten", this.daoFruitEaten);
        tag.putBoolean("zhujiSecretUsed", this.zhujiSecretUsed);
        tag.putString("pendingFoundationDao", this.pendingFoundationDao.id());
        tag.putString("goldenCoreDao", this.goldenCoreDao.id());
        tag.putInt("jiedanPillUsed", this.jiedanPillUsed);
        tag.putInt("bloodJiedanPillUsed", this.bloodJiedanPillUsed);
        tag.putInt("trueBloodUsed", this.trueBloodUsed);
        tag.putInt("earthEvilQiUsed", this.earthEvilQiUsed);
        tag.putInt("heavenClearQiUsed", this.heavenClearQiUsed);
        tag.putInt("creationFruitEaten", this.creationFruitEaten);
        tag.putString("pendingGoldenCoreDao", this.pendingGoldenCoreDao.id());
        tag.putInt("tribulationStrikeDamageOverride", this.tribulationStrikeDamageOverride);
        tag.putString("tribulationType", this.getTribulationType().id());
        tag.putDouble("tribulationDamageRatio", this.tribulationDamageRatio);
        if (this.tribulationSession != null) {
            tag.put("tribulationSession", this.tribulationSession.toTag());
        }
        tag.putBoolean("soulState", this.soulState);
        tag.putInt("soulTicks", this.soulTicks);
        tag.putBoolean("reincarnationPending", this.reincarnationPending);
        tag.putBoolean("reincarnationReady", this.reincarnationReady);
        tag.putInt("soulReaperKills", this.soulReaperKills);
        tag.putBoolean("killedGreatEmperor", this.killedGreatEmperor);
        tag.putString("imperialArtName", this.imperialArtName == null ? "" : this.imperialArtName);
        tag.putInt("nextReaperTick", this.nextReaperTick);
        tag.putBoolean("soulDeathChoicePending", this.soulDeathChoicePending);
        tag.putBoolean("soulReaperPursuitEnabled", this.soulReaperPursuitEnabled);
        tag.putInt("difuTicks", this.difuTicks);
        tag.putInt("difuReincarnationEntries", this.difuReincarnationEntries);
        tag.putBoolean("ghostCultivator", this.ghostCultivator);
        tag.putBoolean("soulReaperIdentity", this.soulReaperIdentity);
        tag.putInt("looseImmortalTribulations", this.looseImmortalTribulations);
        tag.putInt("looseImmortalRewardLevel", this.looseImmortalRewardLevel);
        tag.putLong("nextLooseImmortalTribulationTick", this.nextLooseImmortalTribulationTick);
        tag.putBoolean("looseImmortalChoicePending", this.looseImmortalChoicePending);
        tag.putBoolean("looseImmortalTribulationActive", this.looseImmortalTribulationActive);
        ListTag tribLedger = new ListTag();
        for (TribulationBonusSnapshot snapshot : this.tribulationBonusLedger.values()) {
            tribLedger.add(snapshot.toTag());
        }
        tag.put("tribulationBonusLedger", tribLedger);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        int storedVersion = tag.contains("dataVersion", 3) ? tag.getInt("dataVersion") : 0;
        this.dataVersion = Math.max(0, Math.min(CURRENT_DATA_VERSION, storedVersion));
        this.tribulationSession = null;
        if (tag.contains("realm", 8)) {
            this.realm = Realm.byId(tag.getString("realm"));
        }
        if (tag.contains("subStage", 8)) {
            this.subStage = SubStage.byId(tag.getString("subStage"), this.realm);
        }
        this.tribulationBonusLedger.clear();
        this.currentQi = tag.getLong("currentQi");
        this.cultivationProgress = tag.contains("cultivationProgress", 4) ? tag.getLong("cultivationProgress") : this.currentQi;
        this.totalQiAbsorbed = tag.getLong("totalQiAbsorbed");
        this.physique = tag.contains("physique", 8) ? Physique.byId(tag.getString("physique")) : Physique.MORTAL_BODY;
        if (tag.contains("spiritRoot", 8)) {
            String rootId = tag.getString("spiritRoot");
            Physique legacy = Physique.fromLegacySpiritRootId(rootId);
            if (legacy != null) {
                this.physique = legacy;
                this.spiritRoot = SpiritRoot.NONE;
            } else {
                this.spiritRoot = SpiritRoot.byId(rootId);
            }
        }
        this.meditating = tag.getBoolean("meditating");
        this.timeAccelerationMultiplier = tag.contains("timeAccelerationMultiplier", 3) ? Math.min(10000, Math.max(1, tag.getInt("timeAccelerationMultiplier"))) : 1;
        long l = this.timeAccelerationElapsedTicks = tag.contains("timeAccelerationElapsedTicks", 4) ? Math.max(0L, tag.getLong("timeAccelerationElapsedTicks")) : 0L;
        if (!this.canUseTimeAcceleration() || this.timeAccelerationMultiplier <= 1) {
            this.stopTimeAcceleration();
        }
        this.customName = tag.getString("customName");
        this.gender = tag.getInt("gender");
        this.genderEditsLeft = tag.contains("genderEditsLeft") ? tag.getInt("genderEditsLeft") : 5;
        this.tribulationStrikesRemaining = tag.getInt("tribStrikes");
        this.tribulationCooldown = tag.getInt("tribCooldown");
        this.tribulationBoltsPerWave = tag.contains("tribBoltsPerWave", 3) ? Math.max(1, tag.getInt("tribBoltsPerWave")) : 1;
        this.tribulationBoltsRemainingInWave = tag.contains("tribBoltsRemainingInWave", 3) ? Math.max(0, tag.getInt("tribBoltsRemainingInWave")) : 0;
        this.tribulationBoltCooldown = tag.contains("tribBoltCooldown", 3) ? Math.max(0, tag.getInt("tribBoltCooldown")) : 0;
        int n = this.tribulationBoltIndexInWave = tag.contains("tribBoltIndexInWave", 3) ? Math.max(0, tag.getInt("tribBoltIndexInWave")) : 0;
        if (this.tribulationStrikesRemaining <= 0 || this.tribulationBoltsRemainingInWave <= 0) {
            this.clearPendingTribulationWave();
        } else {
            this.tribulationBoltsRemainingInWave = Math.min(this.tribulationBoltsRemainingInWave, this.getTribulationBoltsPerWave());
            this.tribulationBoltIndexInWave = Math.min(this.tribulationBoltIndexInWave, this.getTribulationBoltsPerWave());
        }
        this.attack = tag.getInt("attack");
        this.defense = tag.getInt("defense");
        this.critRate = tag.getInt("critRate");
        this.qiAbsorbRange = tag.getInt("qiAbsorbRange");
        this.refining = tag.getInt("refining");
        this.refiningXp = tag.getInt("refiningXp");
        this.alchemy = tag.getInt("alchemy");
        this.alchemyXp = tag.getInt("alchemyXp");
        this.sectId = tag.contains("sectId", 8) ? tag.getString("sectId") : "";
        this.sectName = tag.contains("sectName", 8) ? tag.getString("sectName") : "";
        String string = this.sectRoleId = tag.contains("sectRoleId", 8) ? tag.getString("sectRoleId") : "";
        if (this.sectId.isBlank()) {
            this.sectName = "";
            this.sectRoleId = "";
        }
        this.learnedSpells.clear();
        if (tag.contains("spells", 9)) {
            ListTag spells = tag.getList("spells", 8);
            for (int i = 0; i < spells.size(); ++i) {
                this.learnedSpells.add(spells.getString(i));
            }
        }
        this.disabledSpells.clear();
        if (tag.contains("disabledSpells", 9)) {
            ListTag disabled = tag.getList("disabledSpells", 8);
            for (int i = 0; i < disabled.size(); ++i) {
                this.disabledSpells.add(disabled.getString(i));
            }
        }
        this.learnedTechniques.clear();
        if (tag.contains("techniques", 9)) {
            ListTag techs = tag.getList("techniques", 8);
            for (int i = 0; i < techs.size(); ++i) {
                String normalized = Technique.normalizeId(techs.getString(i));
                if (normalized.isEmpty()) continue;
                this.learnedTechniques.add(normalized);
            }
        }
        this.equippedTechniqueId = tag.contains("equippedTechnique", 8) ? Technique.normalizeId(tag.getString("equippedTechnique")) : "";
        this.chargingSpellId = tag.contains("chargingSpellId", 8) ? tag.getString("chargingSpellId") : "";
        this.chargedQi = tag.getInt("chargedQi");
        this.chargingTicks = tag.getInt("chargingTicks");
        this.chargingEntityId = tag.contains("chargingEntityId", 3) ? tag.getInt("chargingEntityId") : -1;
        this.swordFlightStack = tag.contains("swordFlightStack", 10) ? ItemStack.of((CompoundTag)tag.getCompound("swordFlightStack")) : ItemStack.EMPTY;
        this.swordFlightOriginalSlot = tag.contains("swordFlightOriginalSlot", 3) ? tag.getInt("swordFlightOriginalSlot") : -1;
        this.flightTicks = tag.contains("flightTicks", 3) ? Math.min(20, Math.max(0, tag.getInt("flightTicks"))) : 0;
        this.qiFlightToggled = tag.getBoolean("qiFlightToggled");
        this.wuDaoProgress = tag.contains("wuDaoProgress", 4) ? Math.max(0L, tag.getLong("wuDaoProgress")) : 0L;
        this.bodyTemperingHpInherited = tag.contains("bodyTemperingHpInherited", 6) ? tag.getDouble("bodyTemperingHpInherited") : 0.0;
        // 灵气飞行状态随数据同步（syncToClient 传输），存档亦保留；登录时由 CapabilityEvents 重置
        this.voidEscapeActive = tag.getBoolean("voidEscapeActive");
        this.voidEscapeStability = tag.contains("voidEscapeStability", 3) ? tag.getInt("voidEscapeStability") : 0;
        this.inverseFiveElementMark = tag.contains("inverseFiveElementMark", 8) ? QiElement.byId(tag.getString("inverseFiveElementMark")) : QiElement.PURE;
        this.inverseFiveElementMarkExpiresAt = tag.contains("inverseFiveElementMarkExpiresAt", 4) ? tag.getLong("inverseFiveElementMarkExpiresAt") : 0L;
        this.inverseFiveElementStacks = tag.contains("inverseFiveElementStacks", 3) ? Math.max(0, tag.getInt("inverseFiveElementStacks")) : 0;
        this.inverseFiveElementStacksExpiresAt = tag.contains("inverseFiveElementStacksExpiresAt", 4) ? tag.getLong("inverseFiveElementStacksExpiresAt") : 0L;
        for (int i = 0; i < 8; ++i) {
            this.equippedSpells[i] = "";
        }
        if (tag.contains("equippedSpells", 9)) {
            ListTag eq = tag.getList("equippedSpells", 8);
            for (int i = 0; i < eq.size() && i < 8; ++i) {
                this.equippedSpells[i] = eq.getString(i);
            }
        }
        this.selectedSpellSlot = tag.contains("selectedSpellSlot", 3) ? tag.getInt("selectedSpellSlot") : -1;
        this.identityId = tag.contains("identityId", 8) ? tag.getString("identityId") : "";
        this.unallocatedZhenyuan = tag.contains("unallocatedZhenyuan", 3) ? tag.getInt("unallocatedZhenyuan") : 0;
        this.attrConstitution = tag.contains("attrConstitution", 3) ? tag.getInt("attrConstitution") : 0;
        this.attrPhysique = tag.contains("attrPhysique", 3) ? tag.getInt("attrPhysique") : 0;
        this.attrAgility = tag.contains("attrAgility", 3) ? tag.getInt("attrAgility") : 0;
        this.attrSpellPower = tag.contains("attrSpellPower", 3) ? tag.getInt("attrSpellPower") : 0;
        this.attrQiSea = tag.contains("attrQiSea", 3) ? tag.getInt("attrQiSea") : 0;
        if (tag.contains("tribulationBonusLedger", 9)) {
            ListTag ledger = tag.getList("tribulationBonusLedger", 10);
            for (int i = 0; i < ledger.size(); ++i) {
                this.recordTribulationBonus(TribulationBonusSnapshot.fromTag(ledger.getCompound(i)));
            }
        } else if (tag.contains("tribulationBonus", 9)) {
            // 一次性迁移旧 [percent, enum ordinal]：新档只写固定值，不再写旧列表。
            ListTag legacy = tag.getList("tribulationBonus", 10);
            for (int i = 0; i < legacy.size(); ++i) {
                CompoundTag entry = legacy.getCompound(i);
                double percent = Math.max(0.0, entry.getDouble("p"));
                Realm target = RealmTopology.fromLegacyEnumOrdinal(entry.getInt("r")).orElse(null);
                if (target == null || percent <= 0.0) continue;
                String key = "legacy:" + i;
                this.recordTribulationBonus(new TribulationBonusSnapshot(
                        key, target.id(), percent,
                        Math.max(0.0, Math.round(this.realm.standardMaxHealth() * percent)),
                        CultivationData.saturatedMultiplyLong(this.getMaxQi(), percent),
                        CultivationData.saturatedRoundInt((double)this.attrConstitution * percent),
                        CultivationData.saturatedRoundInt((double)this.attrPhysique * percent),
                        CultivationData.saturatedRoundInt((double)this.attrAgility * percent),
                        CultivationData.saturatedRoundInt((double)this.attrSpellPower * percent),
                        CultivationData.saturatedRoundInt((double)this.attrQiSea * percent)));
            }
        }
        this.breakthroughHpBonus = tag.contains("breakthroughHpBonus", 4) ? tag.getLong("breakthroughHpBonus") : 0L;
        this.breakthroughQiBonus = tag.contains("breakthroughQiBonus", 4) ? tag.getLong("breakthroughQiBonus") : 0L;
        if (tag.contains("breakthroughBonusTargetRealm", 8)) {
            this.breakthroughBonusTargetRealmId = tag.getString("breakthroughBonusTargetRealm");
        } else {
            // 旧档没有目标境界字段：把已有累计值绑定到读档时的境界；凡人旧档直接
            // 视为已失效，避免旧的永久生命/灵气加成再次复活。
            this.breakthroughBonusTargetRealmId = this.realm == Realm.MORTAL ? "" : this.realm.id();
        }
        if (this.breakthroughBonusTargetRealmId.isBlank()) {
            this.breakthroughHpBonus = 0L;
            this.breakthroughQiBonus = 0L;
            this.breakthroughBonusTargetSubStageId = "";
        } else {
            Realm breakthroughTarget = RealmTopology.find(this.breakthroughBonusTargetRealmId).orElse(null);
            if (breakthroughTarget == null) {
                this.breakthroughHpBonus = 0L;
                this.breakthroughQiBonus = 0L;
                this.breakthroughBonusTargetRealmId = "";
                this.breakthroughBonusTargetSubStageId = "";
            } else if (tag.contains("breakthroughBonusTargetSubStage", 8)) {
                this.breakthroughBonusTargetSubStageId = SubStage.byId(
                        tag.getString("breakthroughBonusTargetSubStage"), breakthroughTarget).id();
            } else {
                // v3 只有目标境界：优先结合当前进度与同境界渡劫 rewardKey 恢复最新子阶段。
                this.breakthroughBonusTargetSubStageId = this.inferLegacyBreakthroughTargetSubStage(breakthroughTarget).id();
            }
        }
        this.zhenyuanMajorAutoRebalanceApplied = tag.contains("zhenyuanMajorAutoRebalanceApplied", 1) && tag.getBoolean("zhenyuanMajorAutoRebalanceApplied");
        this.bodyDefenseEnabled = !tag.contains("bodyDefenseEnabled", 1) || tag.getBoolean("bodyDefenseEnabled");
        this.disabledBonusCategories.clear();
        if (tag.contains("disabledBonusCategories", 9)) {
            ListTag disabledBonusList = tag.getList("disabledBonusCategories", 8);
            for (int i = 0; i < disabledBonusList.size(); ++i) {
                CultivationBonusCategory category = CultivationBonusCategory.byId(disabledBonusList.getString(i));
                if (category == CultivationBonusCategory.BODY_DEFENSE) {
                    this.bodyDefenseEnabled = false;
                    continue;
                }
                if (category == null) continue;
                this.disabledBonusCategories.add(category.id());
            }
        }
        this.spellTerrainDestructionEnabled = !tag.contains("spellTerrainDestructionEnabled", 1) || tag.getBoolean("spellTerrainDestructionEnabled");
        this.spellTerrainDestructionPreferenceInitialized = tag.contains("spellTerrainDestructionPreferenceInitialized", 1) && tag.getBoolean("spellTerrainDestructionPreferenceInitialized");
        this.spellTerrainDestructionForcedOffByServer = tag.contains("spellTerrainDestructionForcedOffByServer", 1) && tag.getBoolean("spellTerrainDestructionForcedOffByServer");
        this.boneAge = tag.getDouble("boneAge");
        this.mortalLifespan = tag.getInt("mortalLifespan");
        this.foundationDao = FoundationDao.byId(tag.getString("foundationDao"));
        this.zhujiDanEaten = tag.getInt("zhujiDanEaten");
        this.bloodPillEaten = tag.getInt("bloodPillEaten");
        this.daoFruitEaten = tag.getInt("daoFruitEaten");
        this.zhujiSecretUsed = tag.getBoolean("zhujiSecretUsed");
        this.pendingFoundationDao = FoundationDao.byId(tag.getString("pendingFoundationDao"));
        this.goldenCoreDao = GoldenCoreDao.byId(tag.getString("goldenCoreDao"));
        if (RealmTopology.isAtLeast(this.realm, Realm.GOLDEN_CORE)
                && this.goldenCoreDao == GoldenCoreDao.NONE) {
            this.goldenCoreDao = GoldenCoreDao.HUMAN;
        }
        this.jiedanPillUsed = tag.getInt("jiedanPillUsed");
        this.bloodJiedanPillUsed = tag.getInt("bloodJiedanPillUsed");
        this.trueBloodUsed = tag.getInt("trueBloodUsed");
        this.earthEvilQiUsed = tag.getInt("earthEvilQiUsed");
        this.heavenClearQiUsed = tag.getInt("heavenClearQiUsed");
        this.creationFruitEaten = tag.getInt("creationFruitEaten");
        this.pendingGoldenCoreDao = GoldenCoreDao.byId(tag.getString("pendingGoldenCoreDao"));
        this.tribulationStrikeDamageOverride = tag.getInt("tribulationStrikeDamageOverride");
        this.tribulationType = TribulationType.byId(tag.getString("tribulationType"));
        this.tribulationDamageRatio = tag.contains("tribulationDamageRatio", 6)
                ? Math.max(0.0, tag.getDouble("tribulationDamageRatio")) : 0.0;
        if (tag.contains("tribulationSession", 10)) {
            this.tribulationSession = TribulationSession.fromTag(tag.getCompound("tribulationSession"));
        }
        boolean wasSoul = this.soulState;
        this.soulState = tag.getBoolean("soulState");
        this.soulTicks = tag.getInt("soulTicks");
        this.reincarnationPending = tag.getBoolean("reincarnationPending");
        this.reincarnationReady = tag.getBoolean("reincarnationReady");
        this.soulReaperKills = tag.getInt("soulReaperKills");
        this.killedGreatEmperor = tag.getBoolean("killedGreatEmperor");
        this.imperialArtName = tag.contains("imperialArtName", 8) ? tag.getString("imperialArtName") : "";
        this.nextReaperTick = tag.contains("nextReaperTick") ? tag.getInt("nextReaperTick") : -1;
        boolean bl = this.soulDeathChoicePending = tag.contains("soulDeathChoicePending", 1) && tag.getBoolean("soulDeathChoicePending");
        boolean bl2 = tag.contains("soulReaperPursuitEnabled", 1) ? tag.getBoolean("soulReaperPursuitEnabled") : (this.soulReaperPursuitEnabled = this.soulState && this.nextReaperTick >= 0);
        if (!this.soulState) {
            this.soulDeathChoicePending = false;
            this.soulReaperPursuitEnabled = false;
            this.nextReaperTick = -1;
        } else if (this.soulDeathChoicePending) {
            this.soulReaperPursuitEnabled = false;
            this.nextReaperTick = -1;
        }
        this.difuTicks = tag.getInt("difuTicks");
        this.difuReincarnationEntries = tag.contains("difuReincarnationEntries", 3) ? Math.max(0, tag.getInt("difuReincarnationEntries")) : (this.difuTicks > 0 || this.reincarnationReady ? 1 : 0);
        this.ghostCultivator = tag.getBoolean("ghostCultivator");
        this.soulReaperIdentity = tag.getBoolean("soulReaperIdentity");
        this.looseImmortalTribulations = tag.contains("looseImmortalTribulations", 3) ? LooseImmortalBonusHelper.clampLevel(tag.getInt("looseImmortalTribulations")) : 0;
        this.looseImmortalRewardLevel = tag.contains("looseImmortalRewardLevel", 3) ? LooseImmortalBonusHelper.clampLevel(tag.getInt("looseImmortalRewardLevel")) : 0;
        this.nextLooseImmortalTribulationTick = tag.contains("nextLooseImmortalTribulationTick", 4) ? tag.getLong("nextLooseImmortalTribulationTick") : -1L;
        this.looseImmortalChoicePending = tag.getBoolean("looseImmortalChoicePending");
        this.looseImmortalTribulationActive = tag.getBoolean("looseImmortalTribulationActive");
        if (this.tribulationSession != null) {
            this.looseImmortalTribulationActive = this.tribulationSession.looseImmortal();
        }
        if (this.tribulationSession == null && this.tribulationStrikesRemaining > 0) {
            TribulationSpec migratedSpec = new TribulationSpec(
                    this.tribulationStrikesRemaining,
                    this.tribulationBoltsPerWave,
                    this.tribulationStrikeDamageOverride,
                    this.tribulationDamageRatio,
                    0,
                    this.tribulationType);
            this.tribulationSession = TribulationSession.create(migratedSpec, this.looseImmortalTribulationActive,
                    this.realm, this.subStage, this.realm, this.subStage, "");
        }
        if (this.realm != Realm.LOOSE_IMMORTAL) {
            this.looseImmortalTribulations = 0;
            this.looseImmortalRewardLevel = 0;
            this.nextLooseImmortalTribulationTick = -1L;
        } else if (this.looseImmortalTribulations <= 0) {
            this.looseImmortalTribulations = 1;
            this.looseImmortalRewardLevel = Math.max(1, this.looseImmortalRewardLevel);
        } else {
            if (this.looseImmortalRewardLevel <= 0) {
                this.looseImmortalRewardLevel = 1;
            }
            this.looseImmortalRewardLevel = Math.min(this.looseImmortalRewardLevel, this.looseImmortalTribulations);
            this.applyMissingLooseImmortalProgressionRewards();
        }
        if (this.soulReaperIdentity) {
            this.learnSpell(Spell.YIN_YANG_EYE);
        }
        if (this.soulState) {
            this.ensureSoulRuleSpellsLearned();
        } else if (wasSoul) {
            this.removeSoulRulesAfterReturn();
        } else {
            this.removeSoulOnlySpell(Spell.GHOST_FLIGHT);
        }
        this.migrateLegacyImmortalIncantationSpell();
        this.setCurrentQi(this.currentQi);
        this.setCultivationProgress(this.cultivationProgress);
        this.ensureSpellsForRealm();
        this.dataVersion = CURRENT_DATA_VERSION;
    }

    private void migrateLegacyImmortalIncantationSpell() {
        String legacyId = Spell.IMMORTAL_INCANTATION.id();
        if (!this.learnedSpells.remove(legacyId)) {
            return;
        }
        this.learnedTechniques.add(Technique.IMMORTAL_INCANTATION.id());
        this.disabledSpells.remove(legacyId);
        for (int i = 0; i < 8; ++i) {
            if (!legacyId.equals(this.equippedSpells[i])) continue;
            this.equippedSpells[i] = "";
        }
    }

    public static enum ZhenyuanAttr {
        CONSTITUTION,
        PHYSIQUE,
        AGILITY,
        SPELL_POWER,
        QI_SEA;

    }

    public record ZhenyuanBaselineResult(int unallocatedZhenyuan, int automaticPerAttribute) {
    }

    public record LooseImmortalPromotionResult(boolean promoted, int fromLevel, int toLevel, int freeZhenyuan, int automaticAttributePerStat, long maxQiBonus) {
        public static final LooseImmortalPromotionResult EMPTY = new LooseImmortalPromotionResult(false, 0, 0, 0, 0, 0L);
    }
}

