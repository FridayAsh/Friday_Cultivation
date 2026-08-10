package com.friday.cultivation.capability;

import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import com.friday.cultivation.spirit.SpiritRoot;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.dao.FoundationDao;
import com.friday.cultivation.dao.GoldenCoreDao;
import com.friday.cultivation.dao.LooseImmortalBonusHelper;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.CultivationBonusCategory;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import java.util.EnumSet;

/**
 * 修仙数据 Capability 实现
 */
public class CultivationData implements net.minecraftforge.common.util.INBTSerializable<net.minecraft.nbt.CompoundTag> {

    // ── 境界 ──
    private Realm realm = Realm.MORTAL;
    private SubStage subStage = SubStage.EARLY;

    // ── 灵气 ──
    private long currentQi = 0;
    private long totalQiAbsorbed = 0;

    // ── 修炼进度 ──
    private long cultivationProgress = 0;

    // ── 散仙劫辅助字段（已完整实现） ──
    private int looseImmortalTribulations = 0;
    private long nextLooseImmortalTribulationTick = -1L;
    private long timeAccelerationRemaining = 0L;
    private boolean looseImmortalChoicePending = false;

    public long getTimeAccelerationRemaining() { return timeAccelerationRemaining; }
    public void setTimeAccelerationRemaining(long v) { this.timeAccelerationRemaining = Math.max(0L, v); markDirty(); }

    public void setLooseImmortalChoicePending(boolean value) { this.looseImmortalChoicePending = value; markDirty(); }
    public boolean isLooseImmortalChoicePending() { return this.looseImmortalChoicePending; }

    /** 是否处于散仙状态（照搬原模组） */
    public boolean isLooseImmortal() {
        return !this.soulState && this.realm == Realm.LOOSE_IMMORTAL && this.looseImmortalTribulations > 0;
    }

    /** 成为散仙（照搬原模组） */
    public void becomeLooseImmortal(long nextTribulationTick) {
        this.realm = Realm.LOOSE_IMMORTAL;
        this.subStage = SubStage.EARLY;
        this.looseImmortalTribulations = 1;
        this.looseImmortalRewardLevel = 1;
        this.nextLooseImmortalTribulationTick = nextTribulationTick;
        this.looseImmortalChoicePending = false;
        this.looseImmortalTribulationActive = false;
        this.soulState = false;
        this.ghostCultivator = false;
        this.reincarnationPending = false;
        this.reincarnationReady = false;
        this.soulTicks = 0;
        this.difuTicks = 0;
        this.currentQi = 0L;
        this.cultivationProgress = 0L;
        this.clearTribulation();
        this.clearCharging();
        com.friday.cultivation.dao.LooseImmortalBonusHelper.applyFirstTribulationPenalty(this);
        this.setCurrentQi(this.getMaxQi() / 2L);
        this.ensureSpellsForRealm();
        markDirty();
    }

    /** 散仙渡劫成功晋升（照搬原模组） */
    public LooseImmortalPromotionResult promoteLooseImmortal(long nextTribulationTick) {
        if (!this.isLooseImmortal() || this.looseImmortalTribulations >= 9) {
            return LooseImmortalPromotionResult.EMPTY;
        }
        int before = this.getLooseImmortalTribulations();
        this.looseImmortalTribulations = com.friday.cultivation.dao.LooseImmortalBonusHelper.clampLevel(before + 1);
        this.nextLooseImmortalTribulationTick = this.looseImmortalTribulations >= 9 ? -1L : nextTribulationTick;
        this.cultivationProgress = 0L;
        this.clearTribulation();
        LooseImmortalPromotionResult result = this.grantLooseImmortalProgressionRewards(before, this.looseImmortalTribulations);
        this.setCurrentQi(this.getMaxQi());
        this.ensureSpellsForRealm();
        markDirty();
        return result;
    }

    /** 散仙晋升奖励（照搬原模组） */
    private LooseImmortalPromotionResult grantLooseImmortalProgressionRewards(int fromLevel, int toLevel) {
        int from = com.friday.cultivation.dao.LooseImmortalBonusHelper.clampLevel(fromLevel);
        int to = com.friday.cultivation.dao.LooseImmortalBonusHelper.clampLevel(toLevel);
        if (to <= from) {
            this.looseImmortalRewardLevel = Math.max(this.looseImmortalRewardLevel, to);
            return new LooseImmortalPromotionResult(false, from, to, 0, 0, 0L);
        }
        int free = com.friday.cultivation.dao.LooseImmortalBonusHelper.freeZhenyuanRewardBetween(from, to);
        int autoPerAttr = com.friday.cultivation.dao.LooseImmortalBonusHelper.automaticZhenyuanAttributesRewardBetween(from, to);
        long maxQi = com.friday.cultivation.dao.LooseImmortalBonusHelper.maxQiBonusRewardBetween(from, to);
        this.addUnallocatedZhenyuan(free);
        this.addAllZhenyuanAttributes(autoPerAttr);
        this.looseImmortalRewardLevel = to;
        markDirty();
        return new LooseImmortalPromotionResult(true, from, to, free, autoPerAttr, maxQi);
    }

    /** 散仙晋升结果（照搬原模组） */
    public record LooseImmortalPromotionResult(boolean promoted, int fromLevel, int toLevel, int freeZhenyuan, int automaticAttributePerStat, long maxQiBonus) {
        public static final LooseImmortalPromotionResult EMPTY = new LooseImmortalPromotionResult(false, 0, 0, 0, 0, 0L);
    }

    // ── 灵根/体质/身份 ──
    private SpiritRoot spiritRoot = SpiritRoot.NONE;
    private Physique physique = Physique.MORTAL_BODY;
    private String identityId = "";
    private boolean hasChosenIdentity = false;

    // ── 寿命 ──
    private int mortalLifespan = 80;
    private double boneAge = 0;

    // ── 修炼状态 ──
    private boolean meditating = false;

    // ── 天劫 ──
    private int tribulationStrikesRemaining = 0;
    private int tribulationBoltsPerWave = 0;
    private int tribulationBoltsRemainingInWave = 0;
    private int tribulationCooldown = 0;
    private int tribulationBoltCooldown = 0;
    private int tribulationBoltIndexInWave = 0;
    private int tribulationStrikeDamageOverride = 0;
    private boolean looseImmortalTribulationActive = false;

    // ── 同步 ──
    private boolean dirty = false;

    // ═══════════════════════════════════════════
    // 境界
    // ═══════════════════════════════════════════

public Realm getRealm() { return realm; }
public void setRealm(Realm realm) { this.realm = realm; markDirty(); }

public SubStage getSubStage() { return subStage; }
public void setSubStage(SubStage subStage) { this.subStage = subStage; markDirty(); }

    // ═══════════════════════════════════════════
    // 灵气
    // ═══════════════════════════════════════════

public long getCurrentQi() { return currentQi; }


    public long getMaxQi() {
        long base = realm.maxQi(subStage);
        // 真元气海加成：每点气海 +100 灵气上限（问题1修复：真元系统联动）
        if (isBonusCategoryEnabled(com.friday.cultivation.CultivationBonusCategory.MAX_QI)) {
            base += (long) attrQiSea * 100L;
        }
        return base;
    }


    public void setCurrentQi(long qi) {
        this.currentQi = Math.max(0, Math.min(qi, getMaxQi()));
        markDirty();
    }


    public int absorbQi(int amount) {
        long max = getMaxQi();
        long before = currentQi;
        currentQi = Math.min(max, currentQi + amount);
        int absorbed = (int)(currentQi - before);
        totalQiAbsorbed += absorbed;
        markDirty();
        return absorbed;
    }

    /**
     * 严格 1:1 复刻原 mod absorbQi(int, QiElement) 重载
     * QiElement 仅作为类型标记(PURE/BLOOD/SOUL/...)，吸收逻辑与单参版一致
     * 由 BloodBurnEffect 等需要标记 qi 类型的调用方使用
     */
    public int absorbQi(int amount, QiElement element) {
        return absorbQi(amount);
    }

    /** 吸收灵气（根包 QiElement 适配重载——照搬原模组 absorbQi(int, QiElement) 语义） */
    public int absorbQi(int amount, com.friday.cultivation.QiElement element) {
        return absorbQi(amount);
    }


    public boolean consumeQi(long amount) {
        if (currentQi >= amount) {
            currentQi -= amount;
            markDirty();
            return true;
        }
        return false;
    }

public long getTotalQiAbsorbed() { return totalQiAbsorbed; }
public void setTotalQiAbsorbed(long amount) { this.totalQiAbsorbed = amount; }

    // ═══════════════════════════════════════════
    // 修炼进度
    // ═══════════════════════════════════════════


    public long getMaxCultivation() {
        return realm.maxQi(subStage) * 10L;
    }

public long getCultivationProgress() { return cultivationProgress; }


    public void setCultivationProgress(long progress) {
        this.cultivationProgress = Math.max(0, progress);
        markDirty();
    }


    public void addCultivationProgress(long amount) {
        this.cultivationProgress = Math.min(getMaxCultivation(), this.cultivationProgress + amount);
        markDirty();
    }

    // ═══════════════════════════════════════════
    // 灵根
    // ═══════════════════════════════════════════

public SpiritRoot getSpiritRoot() { return spiritRoot; }
public void setSpiritRoot(SpiritRoot root) { this.spiritRoot = root; markDirty(); }

    // ═══════════════════════════════════════════
    // 体质
    // ═══════════════════════════════════════════

public Physique getPhysique() { return physique; }
public void setPhysique(Physique physique) { this.physique = physique; markDirty(); }

    // ═══════════════════════════════════════════
    // 身份
    // ═══════════════════════════════════════════

public String getIdentityId() { return identityId; }
public void setIdentityId(String identityId) { this.identityId = identityId; hasChosenIdentity = true; markDirty(); }
public boolean hasChosenIdentity() { return hasChosenIdentity; }

    // ═══════════════════════════════════════════
    // 寿命
    // ═══════════════════════════════════════════


    public int getBaseLifespan() {
        return realm.baseLifespan();
    }

public int getMortalLifespan() { return mortalLifespan; }
public void setMortalLifespan(int years) { this.mortalLifespan = years; markDirty(); }
public double getBoneAge() { return boneAge; }
public void setBoneAge(double age) { this.boneAge = age; markDirty(); }
public void addBoneAge(double years) { this.boneAge += years; markDirty(); }

    // ═══════════════════════════════════════════
    // 修炼状态
    // ═══════════════════════════════════════════

public boolean isMeditating() { return meditating; }
public void setMeditating(boolean meditating) { this.meditating = meditating; markDirty(); }

    // ═══════════════════════════════════════════
    // 时间加速（原模组完整接口）
    // ═══════════════════════════════════════════
    public static final int[] allowedTimeAccelerationMultipliers = {1, 2, 4, 8};
    private int timeAccelerationMultiplier = 1;
    private long timeAccelerationElapsedTicks = 0;

    public static int[] allowedTimeAccelerationMultipliers() { return allowedTimeAccelerationMultipliers; }
    public static boolean isAllowedTimeAccelerationMultiplier(int mult) {
        for (int m : allowedTimeAccelerationMultipliers) if (m == mult) return true;
        return false;
    }
    public boolean canUseTimeAcceleration() {
        // 原模组时间加速仅在炼气/筑基期可用
        return realm == Realm.QI_REFINING || realm == Realm.FOUNDATION_BUILDING;
    }
    public boolean isTimeAccelerationActive() { return timeAccelerationMultiplier > 1; }
    public int getTimeAccelerationMultiplier() { return timeAccelerationMultiplier; }
    public long getTimeAccelerationElapsedTicks() { return timeAccelerationElapsedTicks; }
    public void startTimeAcceleration(int multiplier) {
        if (!isAllowedTimeAccelerationMultiplier(multiplier)) return;
        this.timeAccelerationMultiplier = multiplier;
        this.timeAccelerationElapsedTicks = 0;
        markDirty();
    }
    public void stopTimeAcceleration() { this.timeAccelerationMultiplier = 1; this.timeAccelerationElapsedTicks = 0; markDirty(); }
    public void tickTimeAccelerationElapsed() { tickTimeAccelerationElapsed(1); }
    public void tickTimeAccelerationElapsed(long amount) { this.timeAccelerationElapsedTicks += amount; markDirty(); }

    // ═══════════════════════════════════════════
    // 自定义名 / 性别（原模组完整接口）
    // ═══════════════════════════════════════════
    private String customName = "";
    private int gender = 0;            // 0=男, 1=女
    private int genderEditsLeft = 1;

    public String getCustomName() { return customName; }
    public void setCustomName(String customName) { this.customName = customName == null ? "" : customName; markDirty(); }
    public int getGender() { return gender; }
    public void setGender(int gender) { this.gender = gender; markDirty(); }
    public int getGenderEditsLeft() { return genderEditsLeft; }
    public void setGenderEditsLeft(int v) { this.genderEditsLeft = v; markDirty(); }

    // ═══════════════════════════════════════════
    // 武器熟练度（御剑/飞剑预留）
    // ═══════════════════════════════════════════
    private int swordSkillLevel = 1;
    private int swordSkillXp = 0;

    public int getSwordSkillLevel() { return swordSkillLevel; }
    public void setSwordSkillLevel(int v) { this.swordSkillLevel = Math.max(1, v); markDirty(); }
    public int getSwordSkillXp() { return swordSkillXp; }
    public void setSwordSkillXp(int v) { this.swordSkillXp = v; markDirty(); }
    /** 增加剑道熟练度，升级时返回 true */
    public boolean addSwordSkillXp(int amount) {
        this.swordSkillXp += amount;
        int newLevel = 1;
        int threshold = 50;
        while (this.swordSkillXp >= threshold) {
            this.swordSkillXp -= threshold;
            newLevel++;
            threshold += 50;
        }
        boolean leveled = newLevel > swordSkillLevel;
        this.swordSkillLevel = newLevel;
        markDirty();
        return leveled;
    }

    // ═══════════════════════════════════════════
    // 炼丹/炼器用量统计（原模组完整接口）
    // ═══════════════════════════════════════════
    private int zhujiDanEaten = 0;
    private int bloodPillEaten = 0;
    private int daoFruitEaten = 0;
    private int jiedanPillUsed = 0;
    private int bloodJiedanPillUsed = 0;
    private int creationFruitEaten = 0;
    private int trueBloodUsed = 0;
    private int earthEvilQiUsed = 0;
    private int heavenClearQiUsed = 0;

    public int getZhujiDanEaten() { return zhujiDanEaten; }
    public void addZhujiDanEaten(int v) { this.zhujiDanEaten += v; markDirty(); }
    public int getBloodPillEaten() { return bloodPillEaten; }
    public void addBloodPillEaten(int v) { this.bloodPillEaten += v; markDirty(); }
    public int getDaoFruitEaten() { return daoFruitEaten; }
    public void addDaoFruitEaten(int v) { this.daoFruitEaten += v; markDirty(); }
    public int getJiedanPillUsed() { return jiedanPillUsed; }
    public int addJiedanPillUsed(int v) { this.jiedanPillUsed += v; markDirty(); return this.jiedanPillUsed; }
    public int getBloodJiedanPillUsed() { return bloodJiedanPillUsed; }
    public int addBloodJiedanPillUsed(int v) { this.bloodJiedanPillUsed += v; markDirty(); return this.bloodJiedanPillUsed; }
    public int getCreationFruitEaten() { return creationFruitEaten; }
    public int addCreationFruitEaten(int v) { this.creationFruitEaten += v; markDirty(); return this.creationFruitEaten; }
    public int getTrueBloodUsed() { return trueBloodUsed; }
    public int addTrueBloodUsed(int v) { this.trueBloodUsed += v; markDirty(); return this.trueBloodUsed; }
    public int getEarthEvilQiUsed() { return earthEvilQiUsed; }
    public int addEarthEvilQiUsed(int v) { this.earthEvilQiUsed += v; markDirty(); return this.earthEvilQiUsed; }
    public int getHeavenClearQiUsed() { return heavenClearQiUsed; }
    public int addHeavenClearQiUsed(int v) { this.heavenClearQiUsed += v; markDirty(); return this.heavenClearQiUsed; }

    // ═══════════════════════════════════════════
    // 天劫
    // ═══════════════════════════════════════════

public boolean isInTribulation() { return tribulationStrikesRemaining > 0; }


    public void startTribulation(int strikes) {
        this.startTribulation(strikes, 0);
    }

    public void startTribulation(int strikes, int strikeDamageOverride) {
        this.startTribulation(strikes, strikeDamageOverride, 1);
    }

    public void startTribulation(int strikes, int strikeDamageOverride, int boltsPerWave) {
        this.tribulationStrikesRemaining = Math.max(0, strikes);
        this.tribulationCooldown = 60;
        this.tribulationStrikeDamageOverride = Math.max(0, strikeDamageOverride);
        this.tribulationBoltsPerWave = Math.max(1, boltsPerWave);
        this.looseImmortalTribulationActive = false;
        this.clearPendingTribulationWave();
        markDirty();
    }

    /** 散仙劫开始（照搬原模组） */
    public void startLooseImmortalTribulation(int strikes, int strikeDamageOverride, int boltsPerWave) {
        this.startTribulation(strikes, strikeDamageOverride, boltsPerWave);
        this.looseImmortalTribulationActive = true;
        markDirty();
    }


    public void clearTribulation() {
        this.tribulationStrikesRemaining = 0;
        this.tribulationCooldown = 0;
        this.tribulationStrikeDamageOverride = 0;
        this.tribulationBoltsPerWave = 1;
        this.looseImmortalTribulationActive = false;
        this.clearPendingTribulationWave();
        markDirty();
    }

    /** 清除待处理波次（照搬原模组） */
    public void clearPendingTribulationWave() {
        this.tribulationBoltsRemainingInWave = 0;
        this.tribulationBoltCooldown = 0;
        this.tribulationBoltIndexInWave = 0;
    }

public int getTribulationStrikesRemaining() { return tribulationStrikesRemaining; }
public int getTribulationBoltsPerWave() { return Math.max(1, tribulationBoltsPerWave); }
public int getTribulationBoltsRemainingInWave() { return tribulationBoltsRemainingInWave; }
public int getTribulationCooldown() { return Math.max(0, tribulationCooldown); }
    public int getTribulationBoltCooldown() { return Math.max(0, tribulationBoltCooldown); }


    public void decrementTribulationCooldown() {
        if (tribulationCooldown > 0) tribulationCooldown--;
    }


    public void decrementTribulationBoltCooldown() {
        if (tribulationBoltCooldown > 0) tribulationBoltCooldown--;
    }


    public void consumePendingTribulationBolt(int nextDelayTicks) {
        if (tribulationBoltsRemainingInWave <= 0) {
            clearPendingTribulationWave();
            return;
        }
        tribulationBoltsRemainingInWave--;
        tribulationBoltIndexInWave++;
        tribulationBoltCooldown = tribulationBoltsRemainingInWave > 0 ? Math.max(0, nextDelayTicks) : 0;
        markDirty();
    }


    public int getCurrentTribulationStrikeDamage() {
        return tribulationStrikeDamageOverride > 0 ? tribulationStrikeDamageOverride : realm.tribulationStrikeDamage();
    }

    /** 波次开始（照搬原模组） */
    public void beginTribulationWave() {
        this.tribulationBoltsRemainingInWave = this.getTribulationBoltsPerWave();
        this.tribulationBoltCooldown = 0;
        this.tribulationBoltIndexInWave = 0;
    }

    /** 波次剩余数量（照搬原模组） */
    public boolean hasPendingTribulationBolts() {
        return this.tribulationBoltsRemainingInWave > 0;
    }

    /** 波次间扣减（照搬原模组） */
    public void decrementTribulationStrikes() {
        if (this.tribulationStrikesRemaining > 0) {
            --this.tribulationStrikesRemaining;
            this.tribulationCooldown = 20;
        }
    }

    /** 散仙劫是否激活（照搬原模组） */
    public boolean isLooseImmortalTribulationActive() {
        return this.looseImmortalTribulationActive;
    }

    // ═══════════════════════════════════════════
    // 突破
    // ═══════════════════════════════════════════

    /** 死亡失败时降级（照搬原模组 demoteOnFailure） */
    public void demoteOnFailure() {
        if (realm == Realm.MORTAL) return;
        if (subStage == SubStage.EARLY) {
            // 降到前一境界的圆满期
            Realm[] realms = Realm.values();
            int idx = Math.max(0, realm.ordinal() - 1);
            realm = realms[idx];
            subStage = SubStage.PEAK;
        } else {
            SubStage[] stages = SubStage.values();
            int idx = Math.max(0, subStage.ordinal() - 1);
            subStage = stages[idx];
        }
        cultivationProgress = 0;
        currentQi = getMaxQi();
        markDirty();
    }


    public boolean tryBreakthrough() {
        if (realm == Realm.TRUE_IMMORTAL) return false;

        long required = getMaxCultivation();
        if (cultivationProgress < required) return false;

        // 凡人→炼气：全维度固定加点（复刻原模组 advanceOnSuccess：5维各+5）
        if (realm == Realm.MORTAL) {
            realm = Realm.QI_REFINING;
            subStage = SubStage.EARLY;
            cultivationProgress = 0;
            currentQi = getMaxQi();
            addUnallocatedZhenyuan(5);
            addAllZhenyuanAttributes(5);
            markDirty();
            return true;
        }

        if (subStage == SubStage.PEAK) {
            // 大境界突破 — 需要天劫
            Realm next = realm.next();
            int tribCount = realm.tribulationCount(subStage);
            if (tribCount > 0) {
                startTribulation(tribCount);
            }
            // 突破到下一境界初期
            realm = next;
            subStage = SubStage.EARLY;
            cultivationProgress = 0;
            currentQi = getMaxQi();
            addUnallocatedZhenyuan(5);
            addAllZhenyuanAttributes(5); // 大境界突破：5维各+5
        } else {
            // 小境界突破
            subStage = subStage.next();
            cultivationProgress = 0;
            // 照搬原模组：reward = 1 + 灵根每子境界额外真元 + 体质每小境界额外真元
            int reward = 1 + spiritRoot.bonus().extraZhenyuanPerSubLevel() + com.friday.cultivation.physique.PhysiqueBonusHelper.extraZhenyuanPerMinor(physique);
            addUnallocatedZhenyuan(reward);
            addAllZhenyuanAttributes(1); // 小境界突破：5维各+1
        }
        markDirty();
        return true;
    }

    /**
     * 真元全维度固定加点（复刻原模组 addAllZhenyuanAttributes）
     * 5个属性（体质/筋骨/身法/法伤/气海）同时各加 delta 点。
     */
    public void addAllZhenyuanAttributes(int delta) {
        if (delta == 0) return;
        attrConstitution = Math.max(0, attrConstitution + delta);
        attrPhysique = Math.max(0, attrPhysique + delta);
        attrAgility = Math.max(0, attrAgility + delta);
        attrSpellPower = Math.max(0, attrSpellPower + delta);
        attrQiSea = Math.max(0, attrQiSea + delta);
        markDirty();
    }

    /** 突破成功推进（照搬原模组 advanceOnSuccess） */
    public void advanceOnSuccess() {
        if (this.isLooseImmortal()) {
            this.cultivationProgress = 0L;
            this.setCurrentQi(this.getMaxQi());
            markDirty();
            return;
        }
        this.cultivationProgress = 0L;
        if (this.realm == Realm.MORTAL) {
            this.realm = Realm.QI_REFINING;
            this.subStage = SubStage.EARLY;
            this.currentQi = 0L;
            this.ensureSpellsForRealm();
            this.addUnallocatedZhenyuan(5);
            this.addAllZhenyuanAttributes(5);
            markDirty();
            return;
        }
        if (this.subStage.isPeak()) {
            Realm n = this.realm.next();
            if (n != this.realm) {
                this.realm = n;
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
                this.subStage = SubStage.EARLY;
                this.currentQi = this.getMaxQi() / 2L;
                this.ensureSpellsForRealm();
                this.addUnallocatedZhenyuan(5);
                this.addAllZhenyuanAttributes(5);
                markDirty();
                return;
            }
            this.currentQi = this.getMaxQi();
            markDirty();
            return;
        }
        this.subStage = this.subStage.next();
        this.currentQi = this.getMaxQi() / 2L;
        int reward = 1 + this.spiritRoot.bonus().extraZhenyuanPerSubLevel() + com.friday.cultivation.physique.PhysiqueBonusHelper.extraZhenyuanPerMinor(this.physique);
        this.addUnallocatedZhenyuan(reward);
        this.addAllZhenyuanAttributes(1);
        markDirty();
    }

    /** 按境界自动习得法术（照搬原模组 ensureSpellsForRealm） */
    public void ensureSpellsForRealm() {
        if (this.realm.ordinal() >= Realm.QI_REFINING.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.SPIRIT_VISION.id());
            this.learnSpell(com.friday.cultivation.spell.Spell.QI_TRANSFER.id());
            this.learnSpell(com.friday.cultivation.spell.Spell.QI_SHIELD.id());
            this.learnSpell(com.friday.cultivation.spell.Spell.REALM_PRESSURE.id());
            if (this.physique == com.friday.cultivation.physique.Physique.INNATE_SWORD_BODY) {
                this.learnSpell(com.friday.cultivation.spell.Spell.SWORD_AURA.id());
            }
        }
        if (this.realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.SWORD_FLIGHT.id());
            this.learnSpell(com.friday.cultivation.spell.Spell.BIGU.id());
        }
        if (this.realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.CORE_SELF_DESTRUCT.id());
        }
        if (this.realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.NASCENT_SOUL_OUT_OF_BODY.id());
        }
        if (this.realm.ordinal() >= Realm.SOUL_FORMATION.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.DIVINE_SENSE.id());
        }
        if (this.realm.ordinal() >= Realm.VOID_REFINING.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.VOID_STEP.id());
            this.learnSpell(com.friday.cultivation.spell.Spell.VOID_ESCAPE.id());
        }
        if (this.realm.ordinal() >= Realm.BODY_INTEGRATION.ordinal()) {
            this.learnSpell(com.friday.cultivation.spell.Spell.DHARMA_BODY_MANIFESTATION.id());
        }
        if (this.realm.ordinal() >= Realm.TRUE_IMMORTAL.ordinal() || this.realm == Realm.LOOSE_IMMORTAL) {
            this.learnSpell(com.friday.cultivation.spell.Spell.QI_FLIGHT.id());
        }
    }

    // ═══════════════════════════════════════════
    // 筑基/金丹道 (Phase 6)
    // ═══════════════════════════════════════════

    private FoundationDao foundationDao = FoundationDao.NONE;
    private GoldenCoreDao goldenCoreDao = GoldenCoreDao.NONE;
    private FoundationDao pendingFoundationDao = FoundationDao.NONE;
    private GoldenCoreDao pendingGoldenCoreDao = GoldenCoreDao.NONE;

    public FoundationDao getFoundationDao() { return foundationDao; }
    public void setFoundationDao(FoundationDao d) { this.foundationDao = d == null ? FoundationDao.NONE : d; markDirty(); }
    public GoldenCoreDao getGoldenCoreDao() { return goldenCoreDao; }
    public void setGoldenCoreDao(GoldenCoreDao d) { this.goldenCoreDao = d == null ? GoldenCoreDao.NONE : d; markDirty(); }

    public void setPendingFoundationDao(FoundationDao d) { this.pendingFoundationDao = d == null ? FoundationDao.NONE : d; markDirty(); }
    public FoundationDao getPendingFoundationDao() { return this.pendingFoundationDao; }
    public void setPendingGoldenCoreDao(GoldenCoreDao d) { this.pendingGoldenCoreDao = d == null ? GoldenCoreDao.NONE : d; markDirty(); }
    public GoldenCoreDao getPendingGoldenCoreDao() { return this.pendingGoldenCoreDao; }

    /** 当前境界是否允许选择筑基 Dao（筑基境及以上） */
    public boolean isEligibleFoundationDao() {
        return realm != null && realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal();
    }

    /** 当前境界是否允许选择金丹 Dao（金丹境及以上） */
    public boolean isEligibleGoldenCoreDao() {
        return realm != null && realm.ordinal() >= Realm.GOLDEN_CORE.ordinal();
    }

    /** 是否允许在金丹境沿用筑基 Dao（筑基 Dao 已选择则允许） */
    public boolean isFoundationAllowedForGoldenCore() {
        return foundationDao != null && foundationDao != FoundationDao.NONE;
    }

    // ── 突破标签页 GUI 使用的带参数版本（照搬原模组） ──

    /** 判断指定筑基Dao在当前骨龄是否满足突破需求（照搬原模组） */
    public boolean isEligibleFoundationDao(FoundationDao dao, int boneAge) {
        if (dao == null || dao == FoundationDao.NONE) return false;
        return switch (dao) {
            case HUMAN -> getZhujiDanEaten() >= 1;
            case BLOOD -> getBloodPillEaten() >= 3;
            case EARTH -> getZhujiDanEaten() >= 6 && isZhujiSecretUsed();
            case HEAVEN -> getZhujiDanEaten() >= 9 && getDaoFruitEaten() >= 1 && isZhujiSecretUsed() && boneAge < 21;
            default -> false;
        };
    }

    /** 判断指定金丹Dao是否被筑基Dao允许（照搬原模组） */
    public boolean isFoundationAllowedForGoldenCore(GoldenCoreDao dao, boolean hasBloodTransformTalisman) {
        if (dao == null || dao == GoldenCoreDao.NONE) {
            return false;
        }
        return switch (dao) {
            case HUMAN -> foundationDao != FoundationDao.NONE;
            case BLOOD -> foundationDao == FoundationDao.BLOOD || hasBloodTransformTalisman;
            case EARTH -> foundationDao == FoundationDao.EARTH || foundationDao == FoundationDao.HEAVEN;
            case HEAVEN -> foundationDao == FoundationDao.HEAVEN;
            default -> false;
        };
    }

    /** 判断指定金丹Dao在当前骨龄+血符状态是否满足突破需求（照搬原模组） */
    public boolean isEligibleGoldenCoreDao(GoldenCoreDao dao, int boneAge, boolean hasBloodTalisman) {
        if (!this.isFoundationAllowedForGoldenCore(dao, hasBloodTalisman)) {
            return false;
        }
        return switch (dao) {
            case HUMAN -> getJiedanPillUsed() >= 1;
            case BLOOD -> getBloodJiedanPillUsed() >= 3 && getTrueBloodUsed() >= 1;
            case EARTH -> getJiedanPillUsed() >= 6 && getEarthEvilQiUsed() >= 1;
            case HEAVEN -> getJiedanPillUsed() >= 9 && getHeavenClearQiUsed() >= 1 && getCreationFruitEaten() >= 1 && boneAge < 60;
            default -> false;
        };
    }

    /** 最佳可选金丹Dao（照搬原模组） */
    public GoldenCoreDao bestEligibleGoldenCoreDao(int boneAgeYears, boolean hasBloodTransformTalisman) {
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.HEAVEN, boneAgeYears, hasBloodTransformTalisman)) return GoldenCoreDao.HEAVEN;
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.EARTH, boneAgeYears, hasBloodTransformTalisman)) return GoldenCoreDao.EARTH;
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.BLOOD, boneAgeYears, hasBloodTransformTalisman)) return GoldenCoreDao.BLOOD;
        if (this.isEligibleGoldenCoreDao(GoldenCoreDao.HUMAN, boneAgeYears, hasBloodTransformTalisman)) return GoldenCoreDao.HUMAN;
        return GoldenCoreDao.NONE;
    }

    /** 最佳可选道基Dao（照搬原模组） */
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

    /** 重置筑基进度（照搬原模组） */
    public void resetFoundationProgress() {
        this.zhujiDanEaten = 0;
        this.bloodPillEaten = 0;
        this.daoFruitEaten = 0;
        this.zhujiSecretUsed = false;
        markDirty();
    }

    /** 重置金丹进度（照搬原模组） */
    public void resetGoldenCoreProgress() {
        this.jiedanPillUsed = 0;
        this.bloodJiedanPillUsed = 0;
        this.trueBloodUsed = 0;
        this.earthEvilQiUsed = 0;
        this.heavenClearQiUsed = 0;
        this.creationFruitEaten = 0;
        markDirty();
    }

    /** 判断当前是否可以选择金丹路线（金丹境且筑基已选） */
    public boolean canChooseGoldenCoreRoute() {
        return realm == Realm.FOUNDATION_BUILDING && foundationDao != FoundationDao.NONE;
    }

    /** 散仙劫剩余 ticks（带now参数版本，照搬原模组） */
    public long getLooseImmortalTribulationRemainingTicks(long now) {
        return getLooseImmortalTribulationRemainingTicks();
    }

    /** 筑基秘药是否已使用 */
    private boolean zhujiSecretUsed = false;
    public boolean isZhujiSecretUsed() { return zhujiSecretUsed; }
    public void setZhujiSecretUsed(boolean v) { this.zhujiSecretUsed = v; markDirty(); }

    /** 散仙劫剩余 ticks（预留接口，当前无散仙劫推进时返回 0） */
    public int getLooseImmortalTribulationRemainingTicks() {
        return isInTribulation() ? getTribulationStrikesRemaining() : 0;
    }

    // ═══════════════════════════════════════════
    // 真元系统 (Phase 7 — 基于原模组 CFR 反编译)
    // ═══════════════════════════════════════════

    /** 同步当前境界的真元基线（照搬原模组） */
    public ZhenyuanBaselineResult syncZhenyuanToRealmBaseline(Realm targetRealm, SubStage targetSub) {
        Realm safeRealm = targetRealm == null ? Realm.MORTAL : targetRealm;
        SubStage safeSub = targetSub == null ? SubStage.EARLY : targetSub;
        int extraPerMinor = this.getSpiritRoot().bonus().extraZhenyuanPerSubLevel() + PhysiqueBonusHelper.extraZhenyuanPerMinor(this.getPhysique());
        boolean loose = safeRealm == Realm.LOOSE_IMMORTAL;
        int looseLevel = loose ? Math.max(1, this.getLooseImmortalTribulations()) : 0;
        Realm zhenyuanRealm = loose ? Realm.TRIBULATION_TRANSCENDENCE : safeRealm;
        SubStage zhenyuanSub = loose ? SubStage.PEAK : safeSub;
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
        markDirty();
        return new ZhenyuanBaselineResult(this.unallocatedZhenyuan, autoPerAttr);
    }

    /** 真元大境界自动再平衡迁移（照搬原模组 applyZhenyuanMajorAutoRebalanceMigration） */
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

    /** 旧版每项属性自动基准（照搬原模组 computeLegacyAutomaticZhenyuanAttrPerStat） */
    private static int computeLegacyAutomaticZhenyuanAttrPerStat(Realm targetRealm, SubStage targetSub) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        int majorCount = targetRealm.ordinal();
        int realmsFullyTraversed = targetRealm.ordinal() - 1;
        int minorCount = realmsFullyTraversed * 3 + targetSub.ordinal();
        return majorCount + minorCount;
    }

    /** 计算该境界累计获得的真元总量（照搬原模组） */
    static int computeTotalZhenyuanEarned(Realm targetRealm, SubStage targetSub, int extraPerMinor) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == Realm.LOOSE_IMMORTAL) {
            targetRealm = Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = SubStage.PEAK;
        }
        int majorCount = targetRealm.ordinal();
        int realmsFullyTraversed = targetRealm.ordinal() - 1;
        int minorCount = realmsFullyTraversed * 3 + targetSub.ordinal();
        return majorCount * 5 + minorCount * (1 + Math.max(0, extraPerMinor));
    }

    /** 计算该境界每项属性的自动基准（照搬原模组） */
    static int computeAutomaticZhenyuanAttrPerStat(Realm targetRealm, SubStage targetSub) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == Realm.LOOSE_IMMORTAL) {
            targetRealm = Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = SubStage.PEAK;
        }
        int majorCount = targetRealm.ordinal();
        int realmsFullyTraversed = targetRealm.ordinal() - 1;
        int minorCount = realmsFullyTraversed * 3 + targetSub.ordinal();
        return majorCount * 5 + minorCount * 1;
    }

    /** 真元基线同步结果（照搬原模组） */
    public record ZhenyuanBaselineResult(int unallocatedZhenyuan, int automaticPerAttribute) {
    }

    /** 真元属性枚举 */
    public enum ZhenyuanAttr {
        CONSTITUTION("constitution", "体质"),
        PHYSIQUE("physique", "筋骨"),
        AGILITY("agility", "身法"),
        SPELL_POWER("spell_power", "法伤"),
        QI_SEA("qi_sea", "气海");

        private final String id, displayName;
        ZhenyuanAttr(String id, String displayName) { this.id = id; this.displayName = displayName; }
        public String id() { return id; }
        public String displayName() { return displayName; }
        public String translationKey() { return "attr.friday_cultivation.zhenyuan." + id; }
        public static ZhenyuanAttr byId(String id) {
            for (var a : values()) { if (a.id.equals(id)) return a; }
            return CONSTITUTION;
        }
    }

    // 真元未分配点数
    private int zhenyuanPoints = 0;
    // 散仙劫真元奖励等级（照搬原模组）
    private int looseImmortalRewardLevel = 0;
    // 真元大境界自动再平衡是否已应用（照搬原模组）
    private boolean zhenyuanMajorAutoRebalanceApplied = false;
    // 各属性已分配点数
    private int attrConstitution = 0;
    private int attrPhysique = 0;
    private int attrAgility = 0;
    private int attrSpellPower = 0;
    private int attrQiSea = 0;
    // 加成分类启用状态
    private final EnumSet<CultivationBonusCategory> enabledBonusCategories = EnumSet.allOf(CultivationBonusCategory.class);

    public int getZhenyuanPoints() { return zhenyuanPoints; }
    public void addZhenyuanPoints(int pts) { this.zhenyuanPoints += pts; markDirty(); }

    public int getAttrConstitution() { return attrConstitution; }
    public int getAttrPhysique() { return attrPhysique; }
    public int getAttrAgility() { return attrAgility; }
    public int getAttrSpellPower() { return attrSpellPower; }
    public int getAttrQiSea() { return attrQiSea; }

    public void setAttrConstitution(int v) { this.attrConstitution = v; markDirty(); }
    public void setAttrPhysique(int v) { this.attrPhysique = v; markDirty(); }
    public void setAttrAgility(int v) { this.attrAgility = v; markDirty(); }
    public void setAttrSpellPower(int v) { this.attrSpellPower = v; markDirty(); }
    public void setAttrQiSea(int v) { this.attrQiSea = v; markDirty(); }

    // ═══════════════════════════════════════════
    // 未分配真元（原模组完整接口）
    // ═══════════════════════════════════════════
    private int unallocatedZhenyuan = 0;

    public int getUnallocatedZhenyuan() { return unallocatedZhenyuan; }
    public void setUnallocatedZhenyuan(int v) { this.unallocatedZhenyuan = v; markDirty(); }
    public void addUnallocatedZhenyuan(int v) { this.unallocatedZhenyuan += v; markDirty(); }

    /** 累计获得真元（未分配+已分配之和） */
    public int getTotalZhenyuanEarned() {
        return unallocatedZhenyuan + zhenyuanPoints;
    }

    // ═══════════════════════════════════════════
    // 五行元素灵气（元素法术/相生相克预留）
    // ═══════════════════════════════════════════
    private final java.util.Map<QiElement, Long> elementQi = new java.util.EnumMap<>(QiElement.class);

    public long getElementCount(QiElement element) {
        return elementQi.getOrDefault(element, 0L);
    }
    public void addElementQi(QiElement element, long amount) {
        if (element == null || amount <= 0) return;
        elementQi.put(element, getElementCount(element) + amount);
        markDirty();
    }
    public long getTotalElementQi() {
        long sum = 0;
        for (long v : elementQi.values()) sum += v;
        return sum;
    }
    public double getElementPercent(QiElement element) {
        long total = getTotalElementQi();
        return total == 0 ? 0 : (double) getElementCount(element) / total;
    }
    public QiElement getDominantElement() {
        QiElement best = null;
        long bestVal = 0;
        for (java.util.Map.Entry<QiElement, Long> e : elementQi.entrySet()) {
            if (e.getValue() > bestVal) { bestVal = e.getValue(); best = e.getKey(); }
        }
        return best;
    }

    /** 元素伤害加成（照搬原模组：原模组亦返回 0） */
    public int getElementDamageBonus(QiElement element) { return 0; }

    // ═══════════════════════════════════════════
    // 战斗开关 / 宗门补充
    // ═══════════════════════════════════════════
    private boolean bodyDefenseEnabled = true;
    private boolean spellTerrainDestructionEnabled = true;
    private boolean spellTerrainDestructionPreferenceInitialized = false;
    private boolean spellTerrainDestructionForcedOffByServer = false;

    public boolean isBodyDefenseEnabled() { return bodyDefenseEnabled; }
    public void setBodyDefenseEnabled(boolean v) { this.bodyDefenseEnabled = v; markDirty(); }
    public boolean isSpellTerrainDestructionEnabled() { return spellTerrainDestructionEnabled; }
    public void setSpellTerrainDestructionEnabled(boolean v) { this.spellTerrainDestructionEnabled = v; markDirty(); }
    public boolean isSpellTerrainDestructionPreferenceInitialized() { return spellTerrainDestructionPreferenceInitialized; }
    public boolean initializeSpellTerrainDestructionPreference(boolean value) {
        if (spellTerrainDestructionPreferenceInitialized) return false;
        this.spellTerrainDestructionPreferenceInitialized = true;
        this.spellTerrainDestructionEnabled = value;
        markDirty();
        return true;
    }
    public boolean isSpellTerrainDestructionForcedOffByServer() { return spellTerrainDestructionForcedOffByServer; }
    public boolean setSpellTerrainDestructionForcedOffByServer(boolean v) {
        this.spellTerrainDestructionForcedOffByServer = v;
        this.spellTerrainDestructionEnabled = !v;
        markDirty();
        return true;
    }
    public boolean isSpellTerrainDestructionEffective() {
        return spellTerrainDestructionEnabled && !spellTerrainDestructionForcedOffByServer;
    }

    /** 是否单干（无宗门） */
    public boolean isSolo() { return sectId.isEmpty(); }

    /** 当前选中槽位中的法术ID（空串表示无） */
    public String getSelectedSpellId() {
        return getEquippedSpellAt(selectedSpellSlot);
    }

    // ═══════════════════════════════════════════
    // 战斗属性（Phase 11 战斗系统预留）
    // ═══════════════════════════════════════════
    private int attack = 0;
    private int defense = 0;
    private int critRate = 0;
    private int qiAbsorbRange = 0;

    public int getAttack() { return attack; }
    public void setAttack(int v) { this.attack = v; markDirty(); }
    public int getDefense() { return defense; }
    public void setDefense(int v) { this.defense = v; markDirty(); }
    public int getCritRate() { return critRate; }
    public void setCritRate(int v) { this.critRate = v; markDirty(); }
    public int getQiAbsorbRange() { return qiAbsorbRange; }
    public void setQiAbsorbRange(int v) { this.qiAbsorbRange = v; markDirty(); }

    public int getAttr(ZhenyuanAttr attr) {
        return switch (attr) {
            case CONSTITUTION -> attrConstitution;
            case PHYSIQUE -> attrPhysique;
            case AGILITY -> attrAgility;
            case SPELL_POWER -> attrSpellPower;
            case QI_SEA -> attrQiSea;
        };
    }

    public void setAttr(ZhenyuanAttr attr, int value) {
        switch (attr) {
            case CONSTITUTION -> attrConstitution = value;
            case PHYSIQUE -> attrPhysique = value;
            case AGILITY -> attrAgility = value;
            case SPELL_POWER -> attrSpellPower = value;
            case QI_SEA -> attrQiSea = value;
        }
        markDirty();
    }

    /** 消耗真元点数给指定属性加1点 */
    public boolean spendZhenyuanOn(ZhenyuanAttr attr) {
        if (unallocatedZhenyuan <= 0) return false;
        unallocatedZhenyuan--;
        setAttr(attr, getAttr(attr) + 1);
        markDirty();
        return true;
    }

    public boolean isBonusCategoryEnabled(CultivationBonusCategory cat) {
        return enabledBonusCategories.contains(cat);
    }

    public void setBonusCategoryEnabled(CultivationBonusCategory cat, boolean enabled) {
        if (enabled) enabledBonusCategories.add(cat);
        else enabledBonusCategories.remove(cat);
        markDirty();
    }

    // ═══════════════════════════════════════════
    // HUD 所需方法 (原模组兼容)
    // ═══════════════════════════════════════════

    public int getLooseImmortalTribulations() { return looseImmortalTribulations; }
    public void setLooseImmortalTribulations(int v) { this.looseImmortalTribulations = v; markDirty(); }
    public long getNextLooseImmortalTribulationTick() { return nextLooseImmortalTribulationTick; }
    public void setLooseImmortalChoice(String choice) { this.looseImmortalTribulations = this.looseImmortalTribulations; markDirty(); }
    public String getLooseImmortalChoice() { return ""; }
    public void setNextLooseImmortalTribulationTick(long tick) { this.nextLooseImmortalTribulationTick = tick; markDirty(); }

    /** 散仙劫倒计时推进（照搬原模组 CultivationData.advanceLooseImmortalTribulationCountdown） */
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
    public boolean hasActiveInverseFiveElementMark(long gameTime) {
        return inverseFiveElementMark != null && gameTime < inverseFiveElementMarkExpiresAt;
    }
    public int getActiveInverseFiveElementStacks(long gameTime) {
        if (inverseFiveElementStacks == 0 || gameTime >= inverseFiveElementStacksExpiresAt) return 0;
        return inverseFiveElementStacks;
    }
    // ═══════════════════════════════════════════
    // 地府系统完整接口 (Phase 15 完整版预留)
    // ═══════════════════════════════════════════
    private boolean soulState = false;
    private int soulTicks = 0;
    private boolean reincarnationPending = false;
    private boolean reincarnationReady = false;
    private int soulReaperKills = 0;
    private int nextReaperTick = 0;
    private boolean soulDeathChoicePending = false;
    private boolean soulReaperPursuitEnabled = false;
    private int difuTicks = 0;
    private boolean ghostCultivator = false;
    private boolean soulReaperIdentity = false;

    public boolean isSoulState() { return soulState; }
    public void setSoulState(boolean v) { this.soulState = v; markDirty(); }
    public int getSoulTicks() { return soulTicks; }
    public void setSoulTicks(int v) { this.soulTicks = v; markDirty(); }
    public void incrementSoulTicks() { this.soulTicks++; markDirty(); }
    public void addSoulTicks(int v) { this.soulTicks += v; markDirty(); }

    public boolean isReincarnationPending() { return reincarnationPending; }
    public void setReincarnationPending(boolean v) { this.reincarnationPending = v; markDirty(); }
    public boolean isReincarnationReady() { return reincarnationReady; }
    public void setReincarnationReady(boolean v) { this.reincarnationReady = v; markDirty(); }

    public int getSoulReaperKills() { return soulReaperKills; }
    public void setSoulReaperKills(int v) { this.soulReaperKills = v; markDirty(); }
    public int getNextReaperTick() { return nextReaperTick; }
    public void setNextReaperTick(int v) { this.nextReaperTick = v; markDirty(); }
    public boolean isSoulDeathChoicePending() { return soulDeathChoicePending; }
    public void setSoulDeathChoicePending(boolean v) { this.soulDeathChoicePending = v; markDirty(); }
    public boolean isSoulReaperPursuitEnabled() { return soulReaperPursuitEnabled; }
    public void setSoulReaperPursuitEnabled(boolean v) { this.soulReaperPursuitEnabled = v; markDirty(); }

    public int getDifuTicks() { return difuTicks; }
    public void setDifuTicks(int v) { this.difuTicks = v; markDirty(); }
    public void incrementDifuTicks() { this.difuTicks++; markDirty(); }
    public void addDifuTicks(int v) { this.difuTicks += v; markDirty(); }

    public boolean isGhostCultivator() { return ghostCultivator; }
    public void setGhostCultivator(boolean v) { this.ghostCultivator = v; markDirty(); }
    public boolean isSoulReaperIdentity() { return soulReaperIdentity; }
    public void setSoulReaperIdentity(boolean v) { this.soulReaperIdentity = v; markDirty(); }

    public boolean canBreakthrough() { return realm != Realm.TRUE_IMMORTAL && cultivationProgress >= getMaxCultivation(); }

    // ═══════════════════════════════════════════
    // 灵气场监测 (Phase 22 完整版预留)
    // ═══════════════════════════════════════════
    private int nearbyQiFieldCount = 0;
    public int getNearbyQiFieldCount() { return nearbyQiFieldCount; }
    public void setNearbyQiFieldCount(int v) { this.nearbyQiFieldCount = v & 0x7F; markDirty(); }

    // ═══════════════════════════════════════════
    // 宗门 + 地府 (Phase 8 身份抽取需要)
    // ═══════════════════════════════════════════

    private String sectId = "";
    private String sectName = "";
    private String equippedTechniqueId = "";
    private String learnedTechniques = "";
    private String learnedSpells = "";

    // ── 炼丹/炼器等级 (Phase 12/13 预留) ──
    private int alchemy = 0;       // 炼丹等级序号
    private int alchemyXp = 0;     // 炼丹熟练度
    private int refining = 0;      // 炼器等级序号
    private int refiningXp = 0;    // 炼器熟练度

    public String getSectId() { return sectId; }
    public void setSectId(String sectId) { this.sectId = sectId == null ? "" : sectId; markDirty(); }

    public String getSectName() { return sectName.isEmpty() ? sectId : sectName; }
    public void setSectName(String sectName) { this.sectName = sectName == null ? "" : sectName; markDirty(); }
    public boolean hasSectDisplay() { return !sectId.isEmpty(); }
    /** 一次性设置宗门显示信息（id/name/role） */
    public void setSectDisplay(String sectId, String sectName, String roleId) {
        this.sectId = sectId == null ? "" : sectId;
        this.sectName = sectName == null ? "" : sectName;
        this.sectRoleId = roleId == null ? "" : roleId;
        markDirty();
    }
    public void clearSectDisplay() {
        this.sectId = "";
        this.sectName = "";
        this.sectRoleId = "";
        markDirty();
    }

    // ── 炼丹等级 ──
    public int getAlchemy() { return alchemy; }
    public void setAlchemy(int alchemy) { this.alchemy = alchemy; markDirty(); }
    public int getAlchemyXp() { return alchemyXp; }
    public void setAlchemyXp(int alchemyXp) { this.alchemyXp = alchemyXp; markDirty(); }
    /** 添加炼丹熟练度，返回是否升级 */
    public boolean addAlchemyXp(int amount) {
        this.alchemyXp += amount;
        markDirty();
        com.friday.cultivation.alchemy.AlchemyRank old = getAlchemyRank();
        com.friday.cultivation.alchemy.AlchemyRank nw = com.friday.cultivation.alchemy.AlchemyRank.fromXp(this.alchemyXp);
        if (nw != old) {
            this.alchemy = nw.ordinal();
            return true;
        }
        return false;
    }
    public com.friday.cultivation.alchemy.AlchemyRank getAlchemyRank() {
        return com.friday.cultivation.alchemy.AlchemyRank.fromXp(alchemyXp);
    }

    // ── 炼器等级 ──
    public int getRefining() { return refining; }
    public void setRefining(int refining) { this.refining = refining; markDirty(); }
    public int getRefiningXp() { return refiningXp; }
    public void setRefiningXp(int refiningXp) { this.refiningXp = refiningXp; markDirty(); }
    /** 添加炼器熟练度，返回是否升级 */
    public boolean addRefiningXp(int amount) {
        this.refiningXp += amount;
        markDirty();
        com.friday.cultivation.refining.RefiningRank old = getRefiningRank();
        com.friday.cultivation.refining.RefiningRank nw = com.friday.cultivation.refining.RefiningRank.fromXp(this.refiningXp);
        if (nw != old) {
            this.refining = nw.ordinal();
            return true;
        }
        return false;
    }
    public com.friday.cultivation.refining.RefiningRank getRefiningRank() {
        return com.friday.cultivation.refining.RefiningRank.fromXp(refiningXp);
    }

    // 宗门角色（原模组由 SectSavedData 计算，这里冗余缓存便于客户端显示）
    private String sectRoleId = "";

    public String getSectRoleId() { return sectRoleId; }
    public void setSectRoleId(String roleId) { this.sectRoleId = roleId == null ? "" : roleId; markDirty(); }

    public com.friday.cultivation.sect.SectRole getSectRole() {
        return com.friday.cultivation.sect.SectRole.byId(sectRoleId);
    }
    public void setSectRole(com.friday.cultivation.sect.SectRole role) {
        this.sectRoleId = role == null ? "" : role.id();
        markDirty();
    }

    public String getEquippedTechniqueId() { return equippedTechniqueId; }
    public void setEquippedTechniqueId(String id) { this.equippedTechniqueId = id == null ? "" : id; markDirty(); }
    public boolean hasEquippedTechnique() { return !equippedTechniqueId.isEmpty(); }

    /** 已学习的功法ID列表（逗号分隔） */
    public java.util.List<String> getLearnedTechniques() {
        if (learnedTechniques.isEmpty()) return java.util.List.of();
        return java.util.Arrays.asList(learnedTechniques.split(","));
    }

    public void learnTechnique(String techniqueId) {
        if (techniqueId == null || techniqueId.isEmpty()) return;
        java.util.List<String> list = new java.util.ArrayList<>(getLearnedTechniques());
        if (!list.contains(techniqueId)) {
            list.add(techniqueId);
            learnedTechniques = String.join(",", list);
            markDirty();
        }
    }

    public boolean hasLearnedTechnique(String techniqueId) {
        return getLearnedTechniques().contains(techniqueId);
    }

    // ═══════════════════════════════════════════
    // 已学法术 (Phase 9)
    // ═══════════════════════════════════════════

    /** 已学习的法术ID列表（逗号分隔） */
    public java.util.List<String> getLearnedSpells() {
        if (learnedSpells.isEmpty()) return java.util.List.of();
        return java.util.Arrays.asList(learnedSpells.split(","));
    }

    public void learnSpell(String spellId) {
        if (spellId == null || spellId.isEmpty()) return;
        java.util.List<String> list = new java.util.ArrayList<>(getLearnedSpells());
        if (!list.contains(spellId)) {
            list.add(spellId);
            learnedSpells = String.join(",", list);
            markDirty();
        }
    }

    public boolean hasSpell(String spellId) {
        return getLearnedSpells().contains(spellId);
    }

    public boolean hasSpell(com.friday.cultivation.spell.Spell spell) {
        return spell != null && hasSpell(spell.id());
    }

    // ═══════════════════════════════════════════
    // 法术槽位 (8个) + 当前选中槽位 (原模组机制)
    // ═══════════════════════════════════════════

    /** 8个法术槽位，每个存储法术ID（空槽位为空串） */
    private final String[] equippedSpells = new String[8];
    /** 当前选中的法术槽位索引 0-7 */
    private int selectedSpellSlot = 0;

    public String getEquippedSpellAt(int slot) {
        if (slot < 0 || slot >= equippedSpells.length) return "";
        return equippedSpells[slot] == null ? "" : equippedSpells[slot];
    }

    public void setEquippedSpellAt(int slot, String spellId) {
        if (slot < 0 || slot >= equippedSpells.length) return;
        equippedSpells[slot] = spellId == null ? "" : spellId;
        markDirty();
    }

    /** 已装备法术ID列表（8槽位，空位为""） */
    public java.util.List<String> getEquippedSpells() {
        java.util.List<String> list = new java.util.ArrayList<>(8);
        for (int i = 0; i < equippedSpells.length; i++) {
            list.add(getEquippedSpellAt(i));
        }
        return list;
    }

    /** 当前选中槽位中的法术（可为空） */
    public com.friday.cultivation.spell.Spell getEquippedSpellAtSelectedSlot() {
        String id = getEquippedSpellAt(selectedSpellSlot);
        if (id.isEmpty()) return null;
        return com.friday.cultivation.spell.Spell.byId(id);
    }

    /** 判断指定法术ID是否已装备到任一槽位 */
    public boolean isSpellEquipped(String spellId) {
        if (spellId == null || spellId.isEmpty()) return false;
        for (int i = 0; i < equippedSpells.length; i++) {
            if (spellId.equals(equippedSpells[i])) return true;
        }
        return false;
    }

    public int getSelectedSpellSlot() {
        return selectedSpellSlot;
    }

    public void setSelectedSpellSlot(int slot) {
        if (slot < 0 || slot >= equippedSpells.length) return;
        this.selectedSpellSlot = slot;
        markDirty();
    }

    // ═══════════════════════════════════════════
    // 法术完整版接口 (Phase 9 蓄力/御剑/虚遁/禁用) 预留
    // ═══════════════════════════════════════════

    /** 禁用的法术ID集合（玩家主动禁用，仍已学习） */
    private final java.util.Set<String> disabledSpells = new java.util.HashSet<>();
    /** 正在蓄力的法术ID（空串表示无） */
    private String chargingSpellId = "";
    private long chargedQi = 0;
    private int chargingTicks = 0;
    private int chargingEntityId = -1;
    /** 御剑飞行状态（照搬原模组：存储飞行中的剑栈，非空即激活） */
    private net.minecraft.world.item.ItemStack swordFlightStack = net.minecraft.world.item.ItemStack.EMPTY;
    private int swordFlightOriginalSlot = -1;
    /** 虚遁状态 */
    private boolean voidEscapeActive = false;
    private int voidEscapeStability = 0;

    public java.util.Set<String> getDisabledSpells() { return disabledSpells; }
    public boolean isSpellEnabled(com.friday.cultivation.spell.Spell spell) {
        return spell == null || !disabledSpells.contains(spell.id());
    }
    public void setSpellEnabled(com.friday.cultivation.spell.Spell spell, boolean enabled) {
        if (spell == null) return;
        if (enabled) disabledSpells.remove(spell.id());
        else disabledSpells.add(spell.id());
        markDirty();
    }

    public String getChargingSpellId() { return chargingSpellId; }
    public void setChargingSpellId(String id) { this.chargingSpellId = id == null ? "" : id; markDirty(); }
    public boolean isCharging() { return !chargingSpellId.isEmpty(); }

    /** 元素威力百分比（照搬原模组：原模组亦返回 0） */
    public int getElementPowerPercent(com.friday.cultivation.spirit.QiElement element) { return 0; }
    public long getChargedQi() { return chargedQi; }
    public void setChargedQi(long v) { this.chargedQi = v; markDirty(); }
    public void addChargedQi(long v) { this.chargedQi += v; markDirty(); }
    public int getChargingTicks() { return chargingTicks; }
    public void incrementChargingTicks() { this.chargingTicks++; markDirty(); }
    public int getChargingEntityId() { return chargingEntityId; }
    public void setChargingEntityId(int id) { this.chargingEntityId = id; markDirty(); }
    public void clearCharging() {
        this.chargingSpellId = "";
        this.chargedQi = 0;
        this.chargingTicks = 0;
        this.chargingEntityId = -1;
        markDirty();
    }

    /** 御剑飞行是否激活（照搬原模组 isSwordFlightActive）。 */
    public boolean isSwordFlightActive() { return !swordFlightStack.isEmpty(); }

    /** 飞行中的剑栈（照搬原模组 getSwordFlightStack）。 */
    public net.minecraft.world.item.ItemStack getSwordFlightStack() { return swordFlightStack; }

    public int getSwordFlightOriginalSlot() { return swordFlightOriginalSlot; }

    /** 开始御剑飞行（照搬原模组 startSwordFlight(ItemStack, int)）。 */
    public void startSwordFlight(net.minecraft.world.item.ItemStack stack, int originalSlot) {
        this.swordFlightStack = stack == null ? net.minecraft.world.item.ItemStack.EMPTY : stack.copy();
        this.swordFlightOriginalSlot = originalSlot;
        markDirty();
    }

    public void clearSwordFlight() {
        this.swordFlightStack = net.minecraft.world.item.ItemStack.EMPTY;
        this.swordFlightOriginalSlot = -1;
        markDirty();
    }

    public boolean isVoidEscapeActive() { return voidEscapeActive; }
    public int getVoidEscapeStability() { return voidEscapeStability; }
    public void startVoidEscape(int stability) { this.voidEscapeActive = true; this.voidEscapeStability = stability; markDirty(); }
    public void clearVoidEscape() { this.voidEscapeActive = false; this.voidEscapeStability = 0; markDirty(); }
    public int decrementVoidEscapeStability() { this.voidEscapeStability--; markDirty(); return this.voidEscapeStability; }

    // ═══════════════════════════════════════════
    // 五行逆反（功法系统）预留
    // ═══════════════════════════════════════════

    private com.friday.cultivation.spirit.QiElement inverseFiveElementMark = null;
    private long inverseFiveElementMarkExpiresAt = 0;
    private int inverseFiveElementStacks = 0;
    private long inverseFiveElementStacksExpiresAt = 0;

    public com.friday.cultivation.spirit.QiElement getInverseFiveElementMark() { return inverseFiveElementMark; }
    public long getInverseFiveElementMarkExpiresAt() { return inverseFiveElementMarkExpiresAt; }
    public void setInverseFiveElementMark(com.friday.cultivation.spirit.QiElement element, long expiresAt) {
        this.inverseFiveElementMark = element;
        this.inverseFiveElementMarkExpiresAt = expiresAt;
        markDirty();
    }
    public int getInverseFiveElementStacks() { return inverseFiveElementStacks; }
    public long getInverseFiveElementStacksExpiresAt() { return inverseFiveElementStacksExpiresAt; }
    public void setInverseFiveElementStacks(int stacks, long expiresAt) {
        this.inverseFiveElementStacks = stacks;
        this.inverseFiveElementStacksExpiresAt = expiresAt;
        markDirty();
    }
    public void clearInverseFiveElementState() {
        this.inverseFiveElementMark = null;
        this.inverseFiveElementMarkExpiresAt = 0;
        this.inverseFiveElementStacks = 0;
        this.inverseFiveElementStacksExpiresAt = 0;
        markDirty();
    }

    private int difuReincarnationEntries = 0;

    public int getDifuReincarnationEntries() { return difuReincarnationEntries; }
    public void setDifuReincarnationEntries(int entries) { this.difuReincarnationEntries = entries; markDirty(); }

    /** 记录一次地府转世进入（照搬原模组） */
    public int recordDifuReincarnationEntry() {
        this.difuReincarnationEntries = saturatedAddInt(this.difuReincarnationEntries, 1);
        markDirty();
        return this.difuReincarnationEntries;
    }

    /** 饱和加法（照搬原模组） */
    static int saturatedAddInt(int value, int delta) {
        if (delta <= 0) {
            return Math.max(0, value);
        }
        return value > Integer.MAX_VALUE - delta ? Integer.MAX_VALUE : value + delta;
    }

    /** 复制另一个 CultivationData 的所有数据（用于重置已有身份） */
    public void copyFrom(CultivationData other) {
        this.realm = other.realm;
        this.subStage = other.subStage;
        this.currentQi = other.currentQi;
        this.totalQiAbsorbed = other.totalQiAbsorbed;
        this.cultivationProgress = other.cultivationProgress;
        this.spiritRoot = other.spiritRoot;
        this.physique = other.physique;
        this.identityId = other.identityId;
        this.hasChosenIdentity = other.hasChosenIdentity;
        this.mortalLifespan = other.mortalLifespan;
        this.boneAge = other.boneAge;
        this.meditating = other.meditating;
        this.foundationDao = other.foundationDao;
        this.goldenCoreDao = other.goldenCoreDao;
        this.sectId = other.sectId;
        this.sectName = other.sectName;
        this.sectRoleId = other.sectRoleId;
        this.alchemy = other.alchemy;
        this.alchemyXp = other.alchemyXp;
        this.refining = other.refining;
        this.refiningXp = other.refiningXp;
        this.equippedTechniqueId = other.equippedTechniqueId;
        this.learnedTechniques = other.learnedTechniques;
        this.learnedSpells = other.learnedSpells;
        for (int i = 0; i < 8; i++) this.equippedSpells[i] = other.getEquippedSpellAt(i);
        this.selectedSpellSlot = other.selectedSpellSlot;
        this.disabledSpells.clear();
        this.disabledSpells.addAll(other.disabledSpells);
        this.chargingSpellId = other.chargingSpellId;
        this.chargedQi = other.chargedQi;
        this.chargingTicks = other.chargingTicks;
        this.chargingEntityId = other.chargingEntityId;
        this.swordFlightStack = other.swordFlightStack.copy();
        this.swordFlightOriginalSlot = other.swordFlightOriginalSlot;
        this.voidEscapeActive = other.voidEscapeActive;
        this.voidEscapeStability = other.voidEscapeStability;
        this.inverseFiveElementMark = other.inverseFiveElementMark;
        this.inverseFiveElementMarkExpiresAt = other.inverseFiveElementMarkExpiresAt;
        this.inverseFiveElementStacks = other.inverseFiveElementStacks;
        this.inverseFiveElementStacksExpiresAt = other.inverseFiveElementStacksExpiresAt;
        this.zhenyuanPoints = other.zhenyuanPoints;
        this.attrConstitution = other.attrConstitution;
        this.attrPhysique = other.attrPhysique;
        this.attrAgility = other.attrAgility;
        this.attrSpellPower = other.attrSpellPower;
        this.attrQiSea = other.attrQiSea;
        this.attack = other.attack;
        this.defense = other.defense;
        this.critRate = other.critRate;
        this.qiAbsorbRange = other.qiAbsorbRange;
        this.soulState = other.soulState;
        this.soulTicks = other.soulTicks;
        this.reincarnationPending = other.reincarnationPending;
        this.reincarnationReady = other.reincarnationReady;
        this.soulReaperKills = other.soulReaperKills;
        this.nextReaperTick = other.nextReaperTick;
        this.soulDeathChoicePending = other.soulDeathChoicePending;
        this.soulReaperPursuitEnabled = other.soulReaperPursuitEnabled;
        this.difuTicks = other.difuTicks;
        this.ghostCultivator = other.ghostCultivator;
        this.soulReaperIdentity = other.soulReaperIdentity;
        this.timeAccelerationMultiplier = other.timeAccelerationMultiplier;
        this.timeAccelerationElapsedTicks = other.timeAccelerationElapsedTicks;
        this.customName = other.customName;
        this.gender = other.gender;
        this.genderEditsLeft = other.genderEditsLeft;
        this.swordSkillLevel = other.swordSkillLevel;
        this.swordSkillXp = other.swordSkillXp;
        this.zhujiDanEaten = other.zhujiDanEaten;
        this.bloodPillEaten = other.bloodPillEaten;
        this.daoFruitEaten = other.daoFruitEaten;
        this.zhujiSecretUsed = other.zhujiSecretUsed;
        this.jiedanPillUsed = other.jiedanPillUsed;
        this.bloodJiedanPillUsed = other.bloodJiedanPillUsed;
        this.creationFruitEaten = other.creationFruitEaten;
        this.trueBloodUsed = other.trueBloodUsed;
        this.earthEvilQiUsed = other.earthEvilQiUsed;
        this.heavenClearQiUsed = other.heavenClearQiUsed;
        this.unallocatedZhenyuan = other.unallocatedZhenyuan;
        this.elementQi.clear();
        this.elementQi.putAll(other.elementQi);
        this.bodyDefenseEnabled = other.bodyDefenseEnabled;
        this.spellTerrainDestructionEnabled = other.spellTerrainDestructionEnabled;
        this.spellTerrainDestructionPreferenceInitialized = other.spellTerrainDestructionPreferenceInitialized;
        this.spellTerrainDestructionForcedOffByServer = other.spellTerrainDestructionForcedOffByServer;
        markDirty();
    }

    // ═══════════════════════════════════════════
    // 同步标记
    // ═══════════════════════════════════════════

public void markDirty() { this.dirty = true; }
public boolean isDirty() { return dirty; }
public void clearDirty() { this.dirty = false; }

    // ═══════════════════════════════════════════
    // NBT 序列化
    // ═══════════════════════════════════════════

    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("realm", realm.id());
        tag.putString("subStage", subStage.id());
        tag.putLong("currentQi", currentQi);
        tag.putLong("totalQiAbsorbed", totalQiAbsorbed);
        tag.putLong("cultivationProgress", cultivationProgress);
        tag.putString("spiritRoot", spiritRoot.id());
        tag.putString("physique", physique.id());
        tag.putString("identityId", identityId);
        tag.putBoolean("hasChosenIdentity", hasChosenIdentity);
        tag.putInt("mortalLifespan", mortalLifespan);
        tag.putDouble("boneAge", boneAge);
        tag.putBoolean("meditating", meditating);
        tag.putInt("tribulationStrikesRemaining", tribulationStrikesRemaining);
        tag.putInt("tribulationBoltsPerWave", tribulationBoltsPerWave);
        tag.putInt("tribulationBoltsRemainingInWave", tribulationBoltsRemainingInWave);
        tag.putInt("tribulationCooldown", tribulationCooldown);
        tag.putInt("tribulationBoltCooldown", tribulationBoltCooldown);
        tag.putString("foundationDao", foundationDao.id());
        tag.putString("goldenCoreDao", goldenCoreDao.id());
        tag.putString("sectId", sectId);
        tag.putString("sectName", sectName);
        tag.putString("sectRoleId", sectRoleId);
        tag.putInt("alchemy", alchemy);
        tag.putInt("alchemyXp", alchemyXp);
        tag.putInt("refining", refining);
        tag.putInt("refiningXp", refiningXp);
        tag.putString("equippedTechniqueId", equippedTechniqueId);
        tag.putString("learnedTechniques", learnedTechniques);
        tag.putString("learnedSpells", learnedSpells);
        for (int i = 0; i < 8; i++) tag.putString("equippedSpell_" + i, getEquippedSpellAt(i));
        tag.putInt("selectedSpellSlot", selectedSpellSlot);
        ListTag disabledSpellList = new ListTag();
        for (String d : disabledSpells) disabledSpellList.add(StringTag.valueOf(d));
        tag.put("disabledSpells", disabledSpellList);
        tag.putString("chargingSpellId", chargingSpellId);
        tag.putLong("chargedQi", chargedQi);
        tag.putInt("chargingTicks", chargingTicks);
        tag.putInt("chargingEntityId", chargingEntityId);
        if (!this.swordFlightStack.isEmpty()) {
            tag.put("swordFlightStack", this.swordFlightStack.save(new net.minecraft.nbt.CompoundTag()));
        }
        tag.putInt("swordFlightOriginalSlot", swordFlightOriginalSlot);
        tag.putBoolean("voidEscapeActive", voidEscapeActive);
        tag.putInt("voidEscapeStability", voidEscapeStability);
        if (inverseFiveElementMark != null) {
            tag.putString("inverseFiveElementMark", inverseFiveElementMark.id());
        }
        tag.putLong("inverseFiveElementMarkExpiresAt", inverseFiveElementMarkExpiresAt);
        tag.putInt("inverseFiveElementStacks", inverseFiveElementStacks);
        tag.putLong("inverseFiveElementStacksExpiresAt", inverseFiveElementStacksExpiresAt);
        tag.putInt("difuReincarnationEntries", difuReincarnationEntries);
        tag.putBoolean("soulState", soulState);
        tag.putInt("soulTicks", soulTicks);
        tag.putBoolean("reincarnationPending", reincarnationPending);
        tag.putBoolean("reincarnationReady", reincarnationReady);
        tag.putInt("soulReaperKills", soulReaperKills);
        tag.putInt("nextReaperTick", nextReaperTick);
        tag.putBoolean("soulDeathChoicePending", soulDeathChoicePending);
        tag.putBoolean("soulReaperPursuitEnabled", soulReaperPursuitEnabled);
        tag.putInt("difuTicks", difuTicks);
        tag.putBoolean("ghostCultivator", ghostCultivator);
        tag.putBoolean("soulReaperIdentity", soulReaperIdentity);
        tag.putInt("zhenyuanPoints", zhenyuanPoints);
        tag.putInt("attrConstitution", attrConstitution);
        tag.putInt("attrPhysique", attrPhysique);
        tag.putInt("attrAgility", attrAgility);
        tag.putInt("attrSpellPower", attrSpellPower);
        tag.putInt("attrQiSea", attrQiSea);
        tag.putInt("attack", attack);
        tag.putInt("defense", defense);
        tag.putInt("critRate", critRate);
        tag.putInt("qiAbsorbRange", qiAbsorbRange);
        tag.putInt("timeAccelerationMultiplier", timeAccelerationMultiplier);
        tag.putLong("timeAccelerationElapsedTicks", timeAccelerationElapsedTicks);
        tag.putString("customName", customName);
        tag.putInt("gender", gender);
        tag.putInt("genderEditsLeft", genderEditsLeft);
        tag.putInt("swordSkillLevel", swordSkillLevel);
        tag.putInt("swordSkillXp", swordSkillXp);
        tag.putInt("zhujiDanEaten", zhujiDanEaten);
        tag.putInt("bloodPillEaten", bloodPillEaten);
        tag.putInt("daoFruitEaten", daoFruitEaten);
        tag.putBoolean("zhujiSecretUsed", zhujiSecretUsed);
        tag.putInt("jiedanPillUsed", jiedanPillUsed);
        tag.putInt("bloodJiedanPillUsed", bloodJiedanPillUsed);
        tag.putInt("creationFruitEaten", creationFruitEaten);
        tag.putInt("trueBloodUsed", trueBloodUsed);
        tag.putInt("earthEvilQiUsed", earthEvilQiUsed);
        tag.putInt("heavenClearQiUsed", heavenClearQiUsed);
        tag.putInt("unallocatedZhenyuan", unallocatedZhenyuan);
        // 元素灵气
        for (QiElement e : QiElement.values()) {
            tag.putLong("elementQi_" + e.id(), getElementCount(e));
        }
        tag.putBoolean("bodyDefenseEnabled", bodyDefenseEnabled);
        tag.putBoolean("spellTerrainDestructionEnabled", spellTerrainDestructionEnabled);
        tag.putBoolean("spellTerrainDestructionPreferenceInitialized", spellTerrainDestructionPreferenceInitialized);
        tag.putBoolean("spellTerrainDestructionForcedOffByServer", spellTerrainDestructionForcedOffByServer);
        return tag;
    }

    public void deserializeNBT(CompoundTag tag) {
        this.realm = Realm.byId(tag.getString("realm"));
        this.subStage = SubStage.byId(tag.getString("subStage"));
        this.currentQi = tag.getLong("currentQi");
        this.totalQiAbsorbed = tag.getLong("totalQiAbsorbed");
        this.cultivationProgress = tag.getLong("cultivationProgress");
        this.spiritRoot = SpiritRoot.byId(tag.getString("spiritRoot"));
        this.physique = Physique.byId(tag.getString("physique"));
        this.identityId = tag.getString("identityId");
        this.hasChosenIdentity = tag.getBoolean("hasChosenIdentity");
        this.mortalLifespan = tag.getInt("mortalLifespan");
        this.boneAge = tag.getDouble("boneAge");
        this.meditating = tag.getBoolean("meditating");
        this.tribulationStrikesRemaining = tag.getInt("tribulationStrikesRemaining");
        this.tribulationBoltsPerWave = tag.getInt("tribulationBoltsPerWave");
        this.tribulationBoltsRemainingInWave = tag.getInt("tribulationBoltsRemainingInWave");
        this.tribulationCooldown = tag.getInt("tribulationCooldown");
        this.tribulationBoltCooldown = tag.getInt("tribulationBoltCooldown");
        this.foundationDao = FoundationDao.byId(tag.getString("foundationDao"));
        this.goldenCoreDao = GoldenCoreDao.byId(tag.getString("goldenCoreDao"));
        this.sectId = tag.getString("sectId");
        this.sectName = tag.getString("sectName");
        this.sectRoleId = tag.getString("sectRoleId");
        this.alchemy = tag.getInt("alchemy");
        this.alchemyXp = tag.getInt("alchemyXp");
        this.refining = tag.getInt("refining");
        this.refiningXp = tag.getInt("refiningXp");
        this.equippedTechniqueId = tag.getString("equippedTechniqueId");
        this.learnedTechniques = tag.getString("learnedTechniques");
        this.learnedSpells = tag.getString("learnedSpells");
        for (int i = 0; i < 8; i++) this.equippedSpells[i] = tag.getString("equippedSpell_" + i);
        this.selectedSpellSlot = tag.getInt("selectedSpellSlot");
        this.disabledSpells.clear();
        ListTag disabledList = tag.getList("disabledSpells", Tag.TAG_STRING);
        for (int i = 0; i < disabledList.size(); i++) this.disabledSpells.add(disabledList.getString(i));
        this.chargingSpellId = tag.getString("chargingSpellId");
        this.chargedQi = tag.getLong("chargedQi");
        this.chargingTicks = tag.getInt("chargingTicks");
        this.chargingEntityId = tag.getInt("chargingEntityId");
        this.swordFlightStack = tag.contains("swordFlightStack", 10) ? net.minecraft.world.item.ItemStack.of(tag.getCompound("swordFlightStack")) : net.minecraft.world.item.ItemStack.EMPTY;
        this.swordFlightOriginalSlot = tag.contains("swordFlightOriginalSlot", 3) ? tag.getInt("swordFlightOriginalSlot") : -1;
        this.voidEscapeActive = tag.getBoolean("voidEscapeActive");
        this.voidEscapeStability = tag.getInt("voidEscapeStability");
        String invMark = tag.getString("inverseFiveElementMark");
        this.inverseFiveElementMark = invMark.isEmpty() ? null : QiElement.byId(invMark);
        this.inverseFiveElementMarkExpiresAt = tag.getLong("inverseFiveElementMarkExpiresAt");
        this.inverseFiveElementStacks = tag.getInt("inverseFiveElementStacks");
        this.inverseFiveElementStacksExpiresAt = tag.getLong("inverseFiveElementStacksExpiresAt");
        this.difuReincarnationEntries = tag.getInt("difuReincarnationEntries");
        this.soulState = tag.getBoolean("soulState");
        this.soulTicks = tag.getInt("soulTicks");
        this.reincarnationPending = tag.getBoolean("reincarnationPending");
        this.reincarnationReady = tag.getBoolean("reincarnationReady");
        this.soulReaperKills = tag.getInt("soulReaperKills");
        this.nextReaperTick = tag.getInt("nextReaperTick");
        this.soulDeathChoicePending = tag.getBoolean("soulDeathChoicePending");
        this.soulReaperPursuitEnabled = tag.getBoolean("soulReaperPursuitEnabled");
        this.difuTicks = tag.getInt("difuTicks");
        this.ghostCultivator = tag.getBoolean("ghostCultivator");
        this.soulReaperIdentity = tag.getBoolean("soulReaperIdentity");
        this.zhenyuanPoints = tag.getInt("zhenyuanPoints");
        this.attrConstitution = tag.getInt("attrConstitution");
        this.attrPhysique = tag.getInt("attrPhysique");
        this.attrAgility = tag.getInt("attrAgility");
        this.attrSpellPower = tag.getInt("attrSpellPower");
        this.attrQiSea = tag.getInt("attrQiSea");
        this.attack = tag.getInt("attack");
        this.defense = tag.getInt("defense");
        this.critRate = tag.getInt("critRate");
        this.qiAbsorbRange = tag.getInt("qiAbsorbRange");
        this.timeAccelerationMultiplier = tag.getInt("timeAccelerationMultiplier");
        this.timeAccelerationElapsedTicks = tag.getLong("timeAccelerationElapsedTicks");
        this.customName = tag.getString("customName");
        this.gender = tag.getInt("gender");
        this.genderEditsLeft = tag.getInt("genderEditsLeft");
        this.swordSkillLevel = tag.getInt("swordSkillLevel");
        this.swordSkillXp = tag.getInt("swordSkillXp");
        this.zhujiDanEaten = tag.getInt("zhujiDanEaten");
        this.bloodPillEaten = tag.getInt("bloodPillEaten");
        this.daoFruitEaten = tag.getInt("daoFruitEaten");
        this.zhujiSecretUsed = tag.getBoolean("zhujiSecretUsed");
        this.jiedanPillUsed = tag.getInt("jiedanPillUsed");
        this.bloodJiedanPillUsed = tag.getInt("bloodJiedanPillUsed");
        this.creationFruitEaten = tag.getInt("creationFruitEaten");
        this.trueBloodUsed = tag.getInt("trueBloodUsed");
        this.earthEvilQiUsed = tag.getInt("earthEvilQiUsed");
        this.heavenClearQiUsed = tag.getInt("heavenClearQiUsed");
        this.unallocatedZhenyuan = tag.getInt("unallocatedZhenyuan");
        this.elementQi.clear();
        for (QiElement e : QiElement.values()) {
            this.elementQi.put(e, tag.getLong("elementQi_" + e.id()));
        }
        this.bodyDefenseEnabled = tag.getBoolean("bodyDefenseEnabled");
        this.spellTerrainDestructionEnabled = tag.getBoolean("spellTerrainDestructionEnabled");
        this.spellTerrainDestructionPreferenceInitialized = tag.getBoolean("spellTerrainDestructionPreferenceInitialized");
        this.spellTerrainDestructionForcedOffByServer = tag.getBoolean("spellTerrainDestructionForcedOffByServer");
    }
}
