package com.friday.cultivation.entity.npc;

import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.spirit.SpiritRoot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * 游历修士 NPC（完整复刻原模组 WanderingCultivatorEntity）
 * 包含：修士身份/境界/体质/灵根、灵气系统、法术学习、御剑/虚遁/灵魂状态、
 * 宗门链接、交易（报价/杂货/喜爱物品/牛头马面令牌）等核心行为。
 */
public class WanderingCultivatorEntity extends net.minecraft.world.entity.PathfinderMob {
    private SpiritRoot spiritRoot = SpiritRoot.NONE;
    private Realm realm = Realm.MORTAL;
    private Physique physique = Physique.MORTAL_BODY;
    private int skinVariant = 0;
    private boolean difuReaper = false;
    private int surnameIndex = 0;
    private int givenNameIndex = 0;
    private boolean npcSoulState = false;
    private int npcSoulTicks = 0;
    private boolean soulReaperTokenTradeAvailable = false;
    private int combatStartTick = -1;
    private long currentQi = 0L;
    private long maxQi = 1000L;
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_CURRENT_QI = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> DATA_MAX_QI = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_SPELL_IDS_CSV = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_FAVORITE_ITEMS_CSV = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_ZHENYUAN_CSV = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> DATA_NPC_SWORD_FLIGHT_ACTIVE = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_TECHNIQUE_ID = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_SECT_ID = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_SECT_NAME = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private static final net.minecraft.network.syncher.EntityDataAccessor<String> DATA_SECT_ROLE = net.minecraft.network.syncher.SynchedEntityData.defineId(WanderingCultivatorEntity.class, net.minecraft.network.syncher.EntityDataSerializers.STRING);
    private final java.util.List<String> spellIds = new java.util.ArrayList<>();
    private int npcSwordFlightIdleTicks = 0;
    private int npcVoidStepCooldownTicks = 0;
    private int npcVoidEscapeCooldownTicks = 0;
    private int npcVoidEscapeActiveTicks = 0;
    private boolean npcVoidEscapePreviousNoGravity = false;
    private boolean npcVoidEscapePreviousNoPhysics = false;
    private int soulDriftTicks = 0;
    private double soulDriftX = 0.0;
    private double soulDriftZ = 0.0;
    private final net.minecraft.world.SimpleContainer extInventory = new net.minecraft.world.SimpleContainer(27);
    @javax.annotation.Nullable
    private net.minecraft.world.entity.player.Player tradingPlayer;
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos sectHomePos;
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos sectBedPos;
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos sectCushionPos;
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos sectCorePos;
    @javax.annotation.Nullable
    private net.minecraft.core.BlockPos sectReceptionGuardPos;
    private boolean sectTemporaryTokensSeeded = false;
    private int sectRoutineCooldown = 0;
    private int sectTaskCooldown = 0;
    private java.util.UUID suspendedFreezeTargetUuid = null;
    private boolean wasTradingFrozen = false;
    private static final java.util.Set<String> SWORD_SPELL_IDS = java.util.Set.of(
            com.friday.cultivation.spell.Spell.FLYING_SWORD.id(),
            com.friday.cultivation.spell.Spell.SWORD_CONVERGENCE.id(),
            com.friday.cultivation.spell.Spell.SKY_SPLITTING_SWORD_AURA.id(),
            com.friday.cultivation.spell.Spell.SWORD_AURA.id());
    private static final int[] MALE_SKIN_VARIANTS = new int[]{1, 2, 4, 7, 10, 12, 15, 17, 18, 19, 20, 21, 23, 25, 26, 28, 34, 35, 36, 40, 42, 43, 44, 47};
    private static final int[] FEMALE_SKIN_VARIANTS = new int[]{0, 3, 5, 6, 8, 9, 11, 13, 14, 16, 22, 24, 27, 29, 30, 31, 32, 33, 37, 38, 39, 41, 45, 46};
    public static final int MALE_SKIN_VARIANT_COUNT = MALE_SKIN_VARIANTS.length;
    public static final int FEMALE_SKIN_VARIANT_START = 24;
    public static final int FEMALE_SKIN_VARIANT_COUNT = FEMALE_SKIN_VARIANTS.length;
    public static final int SKIN_VARIANT_COUNT = 48;
    private static final java.util.UUID ZHENYUAN_GROUND_SPEED_MODIFIER_ID = java.util.UUID.nameUUIDFromBytes("friday_cultivation:npc_zhenyuan_ground_speed".getBytes(java.nio.charset.StandardCharsets.UTF_8));
    private static final double SECT_RECEPTION_GUARD_RETURN_DISTANCE_SQ = 9.0;
    private static final double SECT_RECEPTION_GUARD_TELEPORT_DISTANCE_SQ = 784.0;
    private static final double SECT_BED_SLEEP_REACH_SQ = 6.25;
    private static final double SECT_BED_SLEEP_Y_OFFSET = 0.6875;
    private static final double SECT_BED_SLEEP_SNAP_EPSILON_SQ = 0.01;
    private static final double SECT_BED_SLEEP_THREAT_DISTANCE_SQ = 144.0;
    @javax.annotation.Nullable
    private net.minecraft.world.item.trading.MerchantOffers offers;
    private com.friday.cultivation.identity.Identity identity = com.friday.cultivation.identity.Identity.LONE_CULTIVATOR;
    private com.friday.cultivation.realm.SubStage subStage = com.friday.cultivation.realm.SubStage.EARLY;
    private int looseImmortalTribulations = 0;
    private int gender = 1;
    private com.friday.cultivation.alchemy.AlchemyRank alchemyRank = com.friday.cultivation.alchemy.AlchemyRank.values()[0];
    private com.friday.cultivation.refining.RefiningRank refiningRank = com.friday.cultivation.refining.RefiningRank.values()[0];

    public WanderingCultivatorEntity(EntityType<? extends WanderingCultivatorEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.35);
    }

    public String getTechniqueId() {
        return this.entityData.get(DATA_TECHNIQUE_ID);
    }

    public void setTechniqueId(String id) {
        this.entityData.set(DATA_TECHNIQUE_ID, id == null ? "" : id);
    }

    public SpiritRoot getSpiritRoot() {
        return this.spiritRoot;
    }

    public void setSpiritRoot(SpiritRoot root) {
        this.spiritRoot = root == null ? SpiritRoot.NONE : root;
    }

    public Realm getRealm() {
        return this.realm;
    }

    public void setRealm(Realm realm) {
        this.realm = realm == null ? Realm.MORTAL : realm;
    }

    public Physique getPhysique() {
        return this.physique;
    }

    public void setPhysique(Physique physique) {
        this.physique = physique == null ? Physique.MORTAL_BODY : physique;
    }

    public void setSkinVariant(int v) { this.skinVariant = v; }
    public boolean isDifuReaper() { return this.difuReaper; }
    public void setDifuReaper(boolean v) { this.difuReaper = v; }
    public int getSurnameIndex() { return this.surnameIndex; }
    public void setSurnameIndex(int v) { this.surnameIndex = v; }
    public int getGivenNameIndex() { return this.givenNameIndex; }
    public void setGivenNameIndex(int v) { this.givenNameIndex = v; }

    /** 真元法伤倍率（照搬原模组 getZhenyuanSpellPowerMult） */
    public double getZhenyuanSpellPowerMult() {
        return 1.0 + (double) this.getZhenyuanAttrs()[3] * 0.05;
    }

    /** 真元属性（照搬原模组 getZhenyuanAttrs） */
    public int[] getZhenyuanAttrs() {
        String csv = this.entityData.get(DATA_ZHENYUAN_CSV);
        int[] arr = new int[]{0, 0, 0, 0, 0};
        if (csv == null || csv.isEmpty()) {
            return arr;
        }
        String[] parts = csv.split(",");
        for (int i = 0; i < Math.min(5, parts.length); ++i) {
            try {
                arr[i] = Integer.parseInt(parts[i].trim());
            } catch (NumberFormatException ignored) {
                // 照搬原模组空 catch
            }
        }
        return arr;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_CURRENT_QI, 0);
        this.entityData.define(DATA_MAX_QI, 0);
        this.entityData.define(DATA_SPELL_IDS_CSV, "");
        this.entityData.define(DATA_FAVORITE_ITEMS_CSV, "");
        this.entityData.define(DATA_ZHENYUAN_CSV, "");
        this.entityData.define(DATA_NPC_SWORD_FLIGHT_ACTIVE, false);
        this.entityData.define(DATA_TECHNIQUE_ID, "");
        this.entityData.define(DATA_SECT_ID, "");
        this.entityData.define(DATA_SECT_NAME, "");
        this.entityData.define(DATA_SECT_ROLE, com.friday.cultivation.sect.SectRole.NONE.id());
    }

    /** 是否处于 NPC 灵魂状态（照搬原模组 isNpcSoulState） */
    public boolean isNpcSoulState() {
        return this.npcSoulState;
    }

    /** 进入 NPC 灵魂状态（照搬原模组 enterNpcSoulState） */
    public void enterNpcSoulState() {
        boolean wasSoul = this.isNpcSoulState();
        this.npcSoulState = true;
        if (!wasSoul) {
            this.npcSoulTicks = 0;
        }
        this.soulReaperTokenTradeAvailable = false;
        this.setTarget(null);
        this.clearNpcSoulAggro();
        this.setNoAi(false);
        this.setNoGravity(this.shouldNpcSoulFloat());
        this.currentQi = this.maxQi;
        this.setHealth(this.getMaxHealth());
        if (!this.level().isClientSide && this.isAddedToWorld()) {
            LOADED_NPC_SOULS.add(this);
        }
    }

    /** 已加载的 NPC 灵魂集合（照搬原模组） */
    private static final java.util.Set<WanderingCultivatorEntity> LOADED_NPC_SOULS = java.util.concurrent.ConcurrentHashMap.newKeySet();

    /** 已加载的 NPC 灵魂（照搬原模组 loadedNpcSouls） */
    public static java.util.Set<WanderingCultivatorEntity> loadedNpcSouls() {
        return LOADED_NPC_SOULS;
    }

    /** 登记 NPC 灵魂（照搬原模组 trackNpcSoulIfApplicable） */
    public static void trackNpcSoulIfApplicable(WanderingCultivatorEntity npc) {
        if (npc != null && !npc.level().isClientSide && npc.isNpcSoulState()) {
            LOADED_NPC_SOULS.add(npc);
        }
    }

    /** 注销 NPC 灵魂（照搬原模组 untrackNpcSoul） */
    public static void untrackNpcSoul(WanderingCultivatorEntity npc) {
        if (npc != null) {
            LOADED_NPC_SOULS.remove(npc);
        }
    }

    /** 清空 NPC 灵魂登记（照搬原模组 clearNpcSoulRegistry） */
    public static void clearNpcSoulRegistry() {
        LOADED_NPC_SOULS.clear();
    }

    /** NPC 灵魂 tick 计数（照搬原模组 getNpcSoulTicks） */
    public int getNpcSoulTicks() {
        return this.npcSoulTicks;
    }

    /** 清除 NPC 灵魂状态敌对（照搬原模组 clearNpcSoulAggro） */
    private void clearNpcSoulAggro() {
        super.setTarget(null);
        this.combatStartTick = -1;
        this.setLastHurtByMob(null);
        this.setAggressive(false);
        this.getNavigation().stop();
        this.setPersistenceRequired();
        this.stopNpcSwordFlight(true);
        this.setNoAi(false);
    }

    /** 阴阳规则下能否瞄准目标（照搬原模组 canTargetUnderYinYangRules） */
    private boolean canTargetUnderYinYangRules(net.minecraft.world.entity.LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (this.isNpcSoulState()) {
            return false;
        }
        if (this.isDifuReaper() && com.friday.cultivation.event.SoulStateHandler.canSoulHookTarget((net.minecraft.world.entity.Entity) target)) {
            return true;
        }
        return com.friday.cultivation.event.SoulStateHandler.canOrdinaryAffect((net.minecraft.world.entity.Entity) this, (net.minecraft.world.entity.Entity) target);
    }

    /** 设置攻击目标（照搬原模组 setTarget 覆写） */
    @Override
    public void setTarget(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity target) {
        net.minecraft.world.entity.LivingEntity current;
        if (target != null && !this.canTargetUnderYinYangRules(target)) {
            if (this.getTarget() == target) {
                super.setTarget(null);
            }
            return;
        }
        if (this.isNpcSoulState()) {
            super.setTarget(null);
            this.combatStartTick = -1;
            return;
        }
        if (target == null && (current = this.getTarget()) != null && current.isAlive()) {
            if (this.isTrading() && !(current instanceof net.minecraft.world.entity.player.Player)) {
                super.setTarget(null);
                return;
            }
            return;
        }
        if (target != null) {
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            if (this.isPassenger() && this.getVehicle() instanceof com.friday.cultivation.entity.SeatEntity) {
                this.stopRiding();
            }
        }
        super.setTarget(target);
    }

    /** 受伤（照搬原模组 hurt 覆写） */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        if (this.isNpcSoulState()) {
            if (!this.level().isClientSide) {
                this.clearNpcSoulAggro();
            }
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (!this.level().isClientSide && hurt && source.getEntity() instanceof net.minecraft.world.entity.LivingEntity attacker) {
            this.rememberCombatThreat(attacker);
        }
        return hurt;
    }

    /** 记住战斗威胁（照搬原模组 rememberCombatThreat） */
    public void rememberCombatThreat(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity attacker) {
        if (this.isNpcSoulState()) {
            return;
        }
        if (attacker == null || attacker == this || !attacker.isAlive()) {
            return;
        }
        if (!this.canTargetUnderYinYangRules(attacker)) {
            return;
        }
        this.setLastHurtByMob(attacker);
        this.setTarget(attacker);
        if (this.canStartNpcSwordFlightForCombat() && this.shouldStartNpcSwordFlight(attacker)) {
            this.startNpcSwordFlight();
        }
    }

    /** 是否正在交易（照搬原模组 AbstractVillager.isTrading 语义：有交易玩家即视为交易中） */
    public boolean isTrading() {
        return this.getTradingPlayer() != null;
    }
    public boolean clearHostilityTowardPlayer(java.util.UUID playerId) {
        if (playerId == null) {
            return false;
        }
        boolean changed = false;
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target != null && playerId.equals(target.getUUID())) {
            super.setTarget(null);
            changed = true;
        }
        net.minecraft.world.entity.LivingEntity lastHurtBy = this.getLastHurtByMob();
        if (lastHurtBy != null && playerId.equals(lastHurtBy.getUUID())) {
            this.setLastHurtByMob(null);
            changed = true;
        }
        if (playerId.equals(this.suspendedFreezeTargetUuid)) {
            this.suspendedFreezeTargetUuid = null;
            changed = true;
        }
        if (changed) {
            this.combatStartTick = -1;
            this.setAggressive(false);
            this.getNavigation().stop();
            this.setPersistenceRequired();
            this.stopNpcSwordFlight(true);
            this.setNoAi(false);
        }
        return changed;
    }

    /** 清除以 NPC 灵魂为目标的敌对生物（照搬原模组 clearMobsTargetingNpcSoul） */
    private void clearMobsTargetingNpcSoul() {
        net.minecraft.world.level.Level level = this.level();
        if (!(level instanceof net.minecraft.server.level.ServerLevel)) {
            return;
        }
        net.minecraft.server.level.ServerLevel sl = (net.minecraft.server.level.ServerLevel) level;
        for (net.minecraft.world.entity.Mob mob : sl.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, this.getBoundingBox().inflate(64.0), m -> m.getTarget() == this)) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            if (!(mob instanceof net.minecraft.world.entity.NeutralMob)) continue;
            net.minecraft.world.entity.NeutralMob neutral = (net.minecraft.world.entity.NeutralMob) mob;
            neutral.stopBeingAngry();
        }
    }

    /** AI goal 注册（照搬原模组 registerGoals） */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        this.goalSelector.addGoal(1, new com.friday.cultivation.entity.npc.ai.CultivatorSpellAttackGoal(this));
        this.goalSelector.addGoal(2, new com.friday.cultivation.entity.npc.ai.CultivatorFlightCombatGoal(this));
        this.goalSelector.addGoal(3, new com.friday.cultivation.entity.npc.ai.CultivatorRangedKitingGoal(this));
        this.goalSelector.addGoal(4, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal((net.minecraft.world.entity.PathfinderMob) this, 1.0, true));
        this.goalSelector.addGoal(5, new net.minecraft.world.entity.ai.goal.OpenDoorGoal((net.minecraft.world.entity.Mob) this, true));
        this.goalSelector.addGoal(6, new net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal((net.minecraft.world.entity.PathfinderMob) this, 1.0));
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal((net.minecraft.world.entity.Mob) this, net.minecraft.world.entity.player.Player.class, 8.0f));
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal((net.minecraft.world.entity.Mob) this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal((net.minecraft.world.entity.PathfinderMob) this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.player.Player.class, 10, true, false, e -> !this.isNpcSoulState() && this.getLastHurtByMob() != null && this.canTargetUnderYinYangRules(e)));
        this.targetSelector.addGoal(3, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this, net.minecraft.world.entity.monster.Monster.class, 10, false, false, e -> !this.isNpcSoulState() && this.canTargetUnderYinYangRules(e)));
    }

    /** 灵魂状态是否漂浮（照搬原模组 shouldNpcSoulFloat） */
    private boolean shouldNpcSoulFloat() {
        return this.isNpcSoulState() && this.level().dimension() != com.friday.cultivation.registry.ModDimensions.DIFU;
    }

    /** 停止御剑（照搬原模组 stopNpcSwordFlight） */
    private void stopNpcSwordFlight(boolean restoreGravity) {
        if (!this.isNpcSwordFlightActive()) {
            return;
        }
        this.setNpcSwordFlightActive(false);
        this.npcSwordFlightIdleTicks = 0;
        if (restoreGravity && !this.canFly()) {
            this.setNoGravity(false);
            this.fallDistance = 0.0f;
            net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
            this.setDeltaMovement(v.x, Math.min(v.y, -0.02), v.z);
        }
    }

    /** 是否可飞行（照搬原模组 canFly） */
    public boolean canFly() {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return false;
        }
        if (com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this)) {
            return false;
        }
        if (this.isNpcSoulState()) {
            return this.shouldNpcSoulFloat();
        }
        return this.spellIds.contains(com.friday.cultivation.spell.Spell.QI_FLIGHT.id());
    }

    /** 是否处于御剑状态（照搬原模组 isNpcSwordFlightActive） */
    public boolean isNpcSwordFlightActive() {
        return this.entityData.get(DATA_NPC_SWORD_FLIGHT_ACTIVE);
    }

    private void setNpcSwordFlightActive(boolean active) {
        this.entityData.set(DATA_NPC_SWORD_FLIGHT_ACTIVE, active);
    }

    private boolean canStartNpcSwordFlightForCombat() {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return false;
        }
        if (this.canFly()) {
            return false;
        }
        if (this.isTradingFreeze()) {
            return false;
        }
        if (com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this)) {
            return false;
        }
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.SWORD_FLIGHT.id())) {
            return false;
        }
        if (!(this.getMainHandItem().getItem() instanceof net.minecraft.world.item.SwordItem)) {
            return false;
        }
        long upkeep = com.friday.cultivation.entity.npc.NpcSpellCaster.spellCost(this, com.friday.cultivation.spell.Spell.SWORD_FLIGHT);
        upkeep = Math.max(1L, upkeep <= 0L ? com.friday.cultivation.entity.npc.NpcSpellCaster.generalQiCost(this, 20L) : upkeep);
        return this.getCurrentQi() >= upkeep * 3L;
    }

    /** 学过的法术 ID 列表（照搬原模组 getSpellIds） */
    public java.util.List<String> getSpellIds() {
        if (this.level().isClientSide) {
            String csv = this.entityData.get(DATA_SPELL_IDS_CSV);
            if (csv.isEmpty()) {
                return java.util.Collections.emptyList();
            }
            return java.util.Arrays.asList(csv.split(","));
        }
        return java.util.Collections.unmodifiableList(this.spellIds);
    }

    /** 是否开放牛头马面令牌交易（照搬原模组 isSoulReaperTokenTradeAvailable） */
    public boolean isSoulReaperTokenTradeAvailable() {
        return this.soulReaperTokenTradeAvailable;
    }

    /** 消耗牛头马面令牌交易（照搬原模组 consumeSoulReaperTokenTrade） */
    public void consumeSoulReaperTokenTrade() {
        this.soulReaperTokenTradeAvailable = false;
    }

    /** 根据给定功法返回对应的书籍物品（照搬原模组 techniqueBookItem，转发 ModItems）。 */
    public static net.minecraft.world.item.Item techniqueBookItem(com.friday.cultivation.technique.Technique tech) {
        return com.friday.cultivation.item.ModItems.techniqueBookItem(tech);
    }

    /** 根据给定法术返回对应的法术书物品（照搬原模组 spellBookItem，转发 ModItems）。 */
    public static net.minecraft.world.item.Item spellBookItem(com.friday.cultivation.spell.Spell spell) {
        return com.friday.cultivation.item.ModItems.spellBookItem(spell);
    }

    /** 生成喜爱物品 CSV（照搬原模组 generateFavoriteItems） */
    private void generateFavoriteItems() {
        java.util.ArrayList<java.lang.Object> all = new java.util.ArrayList<Object>(com.friday.cultivation.entity.npc.SundryPricing.allAccepted());
        java.util.Collections.shuffle(all, new java.util.Random(this.getRandom().nextLong()));
        int count = 5 + this.getRandom().nextInt(4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, all.size()); ++i) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey((net.minecraft.world.item.Item) all.get(i)));
        }
        this.entityData.set(DATA_FAVORITE_ITEMS_CSV, sb.toString());
    }

    /** 喜爱物品列表（照搬原模组 getFavoriteItems） */
    public java.util.List<net.minecraft.world.item.Item> getFavoriteItems() {
        String csv = this.entityData.get(DATA_FAVORITE_ITEMS_CSV);
        if (csv.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        java.util.ArrayList<net.minecraft.world.item.Item> result = new java.util.ArrayList<>();
        for (String s : csv.split(",")) {
            net.minecraft.resources.ResourceLocation rl = new net.minecraft.resources.ResourceLocation(s);
            net.minecraft.world.item.Item it = net.minecraftforge.registries.ForgeRegistries.ITEMS.getValue(rl);
            if (it == null || it == net.minecraft.world.item.Items.AIR) continue;
            result.add(it);
        }
        return result;
    }

    /** 交易报价列表（照搬原模组 AbstractVillager.getOffers 语义） */
    public net.minecraft.world.item.trading.MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new net.minecraft.world.item.trading.MerchantOffers();
        }
        return this.offers;
    }

    /** 是否手持的剑正是本次交易售出的物品（照搬原模组 isHeldSwordTradeResult） */
    public boolean isHeldSwordTradeResult(net.minecraft.world.item.ItemStack result) {
        net.minecraft.world.item.ItemStack held = this.getMainHandItem();
        return !result.isEmpty() && held.getItem() instanceof net.minecraft.world.item.SwordItem && net.minecraft.world.item.ItemStack.isSameItem(held, result);
    }

    /** 售出手持的剑后移除之（照搬原模组 removeHeldSwordIfSold） */
    public boolean removeHeldSwordIfSold(net.minecraft.world.item.ItemStack result) {
        if (!this.isHeldSwordTradeResult(result)) {
            return false;
        }
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.item.ItemStack.EMPTY);
        this.stopNpcSwordFlight(true);
        return true;
    }

    /** NPC 背包（照搬原模组 getInventory） */
    public net.minecraft.world.SimpleContainer getInventory() {
        return this.extInventory;
    }

    /** 交易冻结（照搬原模组 isTradingFreeze） */
    public boolean isTradingFreeze() {
        this.clearStaleTradingPlayer();
        net.minecraft.world.entity.player.Player trader = this.getTradingPlayer();
        return trader != null && this.hasActiveCultivatorMenu(trader) && !this.isHostileToPlayer();
    }

    /** 是否对玩家敌对（照搬原模组 isHostileToPlayer） */
    public boolean isHostileToPlayer() {
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target instanceof net.minecraft.world.entity.player.Player && target.isAlive()) {
            return true;
        }
        net.minecraft.world.entity.LivingEntity lastHurtBy = this.getLastHurtByMob();
        return lastHurtBy instanceof net.minecraft.world.entity.player.Player && lastHurtBy.isAlive() && this.tickCount - this.getLastHurtByMobTimestamp() < 200;
    }

    private boolean hasActiveCultivatorMenu(@javax.annotation.Nullable net.minecraft.world.entity.player.Player player) {
        if (player == null || !player.isAlive()) {
            return false;
        }
        if (player.level() != this.level()) {
            return false;
        }
        net.minecraft.world.inventory.AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        return abstractContainerMenu instanceof com.friday.cultivation.inventory.WanderingCultivatorMenu && ((com.friday.cultivation.inventory.WanderingCultivatorMenu) abstractContainerMenu).getNpcEntityId() == this.getId();
    }

    private void clearStaleTradingPlayer() {
        net.minecraft.world.entity.player.Player trader = this.getTradingPlayer();
        if (trader != null && !this.hasActiveCultivatorMenu(trader)) {
            this.setTradingPlayer(null);
        }
    }

    /** 占用中的其他交易玩家（照搬原模组 getBlockingTradingPlayer） */
    @javax.annotation.Nullable
    private net.minecraft.world.entity.player.Player getBlockingTradingPlayer(net.minecraft.world.entity.player.Player opener) {
        this.clearStaleTradingPlayer();
        net.minecraft.world.entity.player.Player trader = this.getTradingPlayer();
        return trader != null && trader != opener && this.hasActiveCultivatorMenu(trader) ? trader : null;
    }

    /** 清除开菜单者状态（照搬原模组 clearOpenerMenuState） */
    private void clearOpenerMenuState(net.minecraft.server.level.ServerPlayer opener) {
        if (this.hasActiveCultivatorMenu(opener)) {
            opener.closeContainer();
        }
        if (this.getTradingPlayer() == opener) {
            this.setTradingPlayer(null);
        }
    }

    /** 玩家能否感知 NPC 灵魂（照搬原模组 canPlayerPerceiveNpcSoul） */
    private static boolean canPlayerPerceiveNpcSoul(net.minecraft.world.entity.player.Player player) {
        return com.friday.cultivation.CultivationCapability.get(player).map(data -> data.isSpellEnabled(com.friday.cultivation.spell.Spell.YIN_YANG_EYE)).orElse(false);
    }

    /** 交易玩家（原模组继承自 AbstractVillager，本项目父类为 Mob，此处照搬 AbstractVillager 语义） */
    @javax.annotation.Nullable
    public net.minecraft.world.entity.player.Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    public void setTradingPlayer(@javax.annotation.Nullable net.minecraft.world.entity.player.Player player) {
        this.tradingPlayer = player;
    }

    private boolean isNpcVoidEscapeActive() {
        return this.npcVoidEscapeActiveTicks > 0;
    }

    /** 是否允许使用飞行战斗（照搬原模组 canUseCombatFlight） */
    public boolean canUseCombatFlight() {
        return this.canFly() || this.isNpcSwordFlightActive() || this.isNpcVoidEscapeActive();
    }

    /** 当前灵气值（照搬原模组 getCurrentQi） */
    public long getCurrentQi() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_CURRENT_QI);
        }
        return this.currentQi;
    }

    /** 添加灵气（照搬原模组 addQi） */
    public long addQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long room = this.maxQi - this.currentQi;
        long actual = Math.min(amount, room);
        this.currentQi += actual;
        return actual;
    }

    /** 最大灵气值（照搬原模组 getMaxQi） */
    public long getMaxQi() {
        if (this.level().isClientSide) {
            return this.entityData.get(DATA_MAX_QI);
        }
        return this.maxQi;
    }

    /** 扣减灵气（照搬原模组 deductQi） */
    public void deductQi(long cost) {
        this.currentQi = Math.max(0L, this.currentQi - cost);
    }

    /** 是否即将进入飞行战斗（照搬原模组 canUseCombatFlightSoon） */
    public boolean canUseCombatFlightSoon() {
        return this.canUseCombatFlight() || this.canStartNpcSwordFlightForCombat();
    }

    /** 当前战斗 tick 计数（照搬原模组 getCombatTicks） */
    public int getCombatTicks() {
        if (this.combatStartTick < 0) {
            return 0;
        }
        return Math.max(0, this.tickCount - this.combatStartTick);
    }

    /** 是否应开始御剑战斗（照搬原模组 shouldStartNpcSwordFlight） */
    private boolean shouldStartNpcSwordFlight(@javax.annotation.Nullable net.minecraft.world.entity.LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!this.canStartNpcSwordFlightForCombat()) {
            return false;
        }
        double distSqr = this.distanceToSqr((net.minecraft.world.entity.Entity) target);
        boolean targetFlying = target.isNoGravity() || target.isRemoved()
                || (target instanceof net.minecraft.world.entity.player.Player && ((net.minecraft.world.entity.player.Player) target).getAbilities().flying);
        boolean verticalPressure = target.getY() > this.getY() + 3.0;
        boolean rangedCombat = distSqr > 144.0;
        boolean pressured = this.getHealth() < this.getMaxHealth() * 0.55f && distSqr < 100.0;
        return targetFlying || verticalPressure || rangedCombat || pressured || this.random.nextInt(100) == 0;
    }

    /** 开始御剑飞行（照搬原模组 startNpcSwordFlight） */
    private void startNpcSwordFlight() {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return;
        }
        this.setNpcSwordFlightActive(true);
        this.npcSwordFlightIdleTicks = 0;
        this.setNoGravity(true);
        net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
        this.setDeltaMovement(v.x, Math.max(0.35, v.y), v.z);
        net.minecraft.world.level.Level level = this.level();
        if (level instanceof net.minecraft.server.level.ServerLevel) {
            net.minecraft.server.level.ServerLevel sl = (net.minecraft.server.level.ServerLevel) level;
            sl.sendParticles((net.minecraft.core.particles.ParticleOptions) net.minecraft.core.particles.ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.15, this.getZ(), 16, 0.45, 0.08, 0.45, 0.05);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.TRIDENT_RIPTIDE_1, this.getSoundSource(), 0.55f, 1.35f);
        }
    }

    /** 体术防御值（照搬原模组 getBodyDefense） */
    public int getBodyDefense() {
        com.friday.cultivation.technique.Technique t = com.friday.cultivation.technique.Technique.byId(this.getTechniqueId());
        int techDef = t == null ? 0 : t.bonus().defense;
        return com.friday.cultivation.BodyDefenseHelper.npcBodyDefense(techDef);
    }

    /** 修士姓名（照搬原模组 getCultivatorName） */
    public net.minecraft.network.chat.MutableComponent getCultivatorName() {
        return com.friday.cultivation.entity.npc.CultivatorNames.display(this.getSurnameIndex(), this.getGivenNameIndex());
    }

    /** 宗门 ID（照搬原模组 getSectId） */
    public String getSectId() {
        return this.entityData.get(DATA_SECT_ID);
    }

    /** 宗门名（照搬原模组 getSectName） */
    public String getSectName() {
        return this.entityData.get(DATA_SECT_NAME);
    }

    /** 宗门角色（照搬原模组 getSectRole） */
    public com.friday.cultivation.sect.SectRole getSectRole() {
        return com.friday.cultivation.sect.SectRole.byId(this.entityData.get(DATA_SECT_ROLE));
    }

    /** 是否拥有宗门身份（照搬原模组 hasSectMembership） */
    public boolean hasSectMembership() {
        return !this.getSectId().isBlank() && this.getSectRole() != com.friday.cultivation.sect.SectRole.NONE;
    }

    /** 确保宗门核心链接（照搬原模组 ensureSectCoreLink） */
    public boolean ensureSectCoreLink(net.minecraft.core.BlockPos corePos, boolean addTemporaryTokens) {
        if (this.level().isClientSide || corePos == null || !this.hasSectMembership()) {
            return false;
        }
        boolean changed = false;
        if (this.sectCorePos == null || !this.sectCorePos.equals(corePos)) {
            this.sectCorePos = corePos.immutable();
            changed = true;
        }
        changed |= this.addPersonalSectToken();
        if (addTemporaryTokens) {
            changed |= this.ensureTemporarySectTokens();
        }
        if (changed) {
            this.getInventory().setChanged();
            this.refreshDimensions();
            this.regenerateOffers();
        }
        return changed;
    }

    /** 宗门核心坐标（照搬原模组 getSectCorePos） */
    @javax.annotation.Nullable
    public net.minecraft.core.BlockPos getSectCorePos() {
        return this.sectCorePos;
    }

    /** 宗门身份组件（照搬原模组 getSectIdentityComponent） */
    public net.minecraft.network.chat.Component getSectIdentityComponent() {
        if (this.hasSectMembership()) {
            return this.getSectRole().identity(this.getSectName());
        }
        return this.isDifuReaper() ? net.minecraft.network.chat.Component.translatable("entity.xiaoxiang_cultivation.soul_reaper") : net.minecraft.network.chat.Component.translatable(this.getIdentity().translationKey());
    }

    /** 分配宗门成员身份（照搬原模组 assignSectMembership） */
    public void assignSectMembership(String sectId, String sectName, com.friday.cultivation.sect.SectRole role, @javax.annotation.Nullable net.minecraft.core.BlockPos homePos, @javax.annotation.Nullable net.minecraft.core.BlockPos bedPos, @javax.annotation.Nullable net.minecraft.core.BlockPos cushionPos, @javax.annotation.Nullable net.minecraft.core.BlockPos corePos, boolean addPersonalToken) {
        com.friday.cultivation.sect.SectRole safeRole = role == null ? com.friday.cultivation.sect.SectRole.NONE : role;
        this.entityData.set(DATA_SECT_ID, sectId == null ? "" : sectId);
        this.entityData.set(DATA_SECT_NAME, sectName == null ? "" : sectName);
        this.entityData.set(DATA_SECT_ROLE, safeRole.id());
        this.sectHomePos = homePos == null ? null : homePos.immutable();
        this.sectBedPos = bedPos == null ? null : bedPos.immutable();
        this.sectCushionPos = cushionPos == null ? null : cushionPos.immutable();
        this.sectCorePos = corePos == null ? null : corePos.immutable();
        if (safeRole != com.friday.cultivation.sect.SectRole.GUARD_DISCIPLE) {
            this.sectReceptionGuardPos = null;
        }
        if (!this.level().isClientSide && addPersonalToken && this.sectCorePos != null) {
            this.addPersonalSectToken();
        }
        this.setPersistenceRequired();
        this.regenerateOffers();
        this.updateDisplayName();
    }

    private boolean addPersonalSectToken() {
        if (this.sectCorePos == null || this.getSectName().isBlank()) {
            return false;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(i);
            if (!com.friday.cultivation.item.SectTokenItem.isLinkedToCore(stack, this.level(), this.sectCorePos) || com.friday.cultivation.item.SectTokenItem.isTemporaryLinked(stack) || !com.friday.cultivation.item.SectTokenItem.isUsableBy(stack, this)) continue;
            return false;
        }
        net.minecraft.world.item.ItemStack token = com.friday.cultivation.item.SectTokenItem.createLinked(this.level(), this.sectCorePos, this.getSectName(), this.getCultivatorName().getString(), false, 1);
        net.minecraft.world.item.ItemStack leftover = inv.addItem(token);
        if (!leftover.isEmpty()) {
            this.spawnAtLocation(leftover);
        }
        inv.setChanged();
        return true;
    }

    private boolean ensureTemporarySectTokens() {
        if (this.getSectRole() != com.friday.cultivation.sect.SectRole.GUARD_DISCIPLE || this.sectCorePos == null || this.getSectName().isBlank()) {
            return false;
        }
        if (this.hasTemporarySectTokenForCore()) {
            if (!this.sectTemporaryTokensSeeded) {
                this.sectTemporaryTokensSeeded = true;
                return true;
            }
            return false;
        }
        if (this.sectTemporaryTokensSeeded) {
            return false;
        }
        int count = 9 + this.random.nextInt(56);
        net.minecraft.world.item.ItemStack tokens = com.friday.cultivation.item.SectTokenItem.createLinked(this.level(), this.sectCorePos, this.getSectName(), "", true, count);
        net.minecraft.world.item.ItemStack leftover = this.getInventory().addItem(tokens);
        if (!leftover.isEmpty()) {
            this.spawnAtLocation(leftover);
        }
        this.sectTemporaryTokensSeeded = true;
        this.getInventory().setChanged();
        return true;
    }

    private boolean hasTemporarySectTokenForCore() {
        if (this.sectCorePos == null) {
            return false;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(i);
            if (!com.friday.cultivation.item.SectTokenItem.isTemporaryLinked(stack) || !com.friday.cultivation.item.SectTokenItem.isLinkedToCore(stack, this.level(), this.sectCorePos)) continue;
            return true;
        }
        return false;
    }

    /** 重新生成交易（照搬原模组 regenerateOffers，原模组委托 AbstractVillager.updateTrades） */
    public void regenerateOffers() {
        this.removeForbiddenNaturalLootFromInventory();
        net.minecraft.world.item.trading.MerchantOffers gen = com.friday.cultivation.entity.npc.CultivatorTrades.generateOffers(this);
        if (this.offers == null) {
            this.offers = new net.minecraft.world.item.trading.MerchantOffers();
        }
        this.offers.clear();
        this.offers.addAll(gen);
    }

    public boolean removeForbiddenNaturalLootFromInventory() {
        boolean removed = false;
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); ++slot) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(slot);
            if (!com.friday.cultivation.util.CultivationRandomPools.isForbiddenNaturalLootStack(stack)) continue;
            inv.setItem(slot, net.minecraft.world.item.ItemStack.EMPTY);
            removed = true;
        }
        if (removed) {
            inv.setChanged();
            this.offers = null;
        }
        return removed;
    }

    /** 学习宗门奖励功法（照搬原模组 learnSectRewardTechnique） */
    public boolean learnSectRewardTechnique(@javax.annotation.Nullable com.friday.cultivation.technique.Technique technique) {
        if (technique == null) {
            return false;
        }
        if (technique.id().equals(this.getTechniqueId())) {
            return false;
        }
        this.entityData.set(DATA_TECHNIQUE_ID, technique.id());
        this.refreshDimensions();
        return true;
    }

    /** 学习宗门奖励法术（照搬原模组 learnSectRewardSpell） */
    public boolean learnSectRewardSpell(@javax.annotation.Nullable com.friday.cultivation.spell.Spell spell) {
        if (spell == null || this.getRealm() == com.friday.cultivation.realm.Realm.MORTAL || this.spellIds.contains(spell.id())) {
            return false;
        }
        if (!com.friday.cultivation.entity.npc.NpcSpellCaster.isLearnableByNpc(spell) && !this.isRealmAutomaticSpell(spell, this.getRealm())) {
            return false;
        }
        this.spellIds.add(spell.id());
        this.normalizeSpellIdsForRealm(this.getRealm());
        this.syncSpellIdsToData();
        this.ensureSwordForKnownSwordSpell(this.getRealm());
        this.setNoGravity(this.canFly() || this.isNpcSwordFlightActive());
        this.refreshDimensions();
        return this.spellIds.contains(spell.id());
    }

    private void syncSpellIdsToData() {
        String csv = String.join(",", this.spellIds);
        if (!this.entityData.get(DATA_SPELL_IDS_CSV).equals(csv)) {
            this.entityData.set(DATA_SPELL_IDS_CSV, csv);
        }
    }

    private void ensureSwordForKnownSwordSpell(com.friday.cultivation.realm.Realm realm) {
        net.minecraft.world.item.Item item = this.getMainHandItem().getItem();
        boolean swordLike = item instanceof net.minecraft.world.item.SwordItem || item instanceof com.friday.cultivation.item.weapon.TieredWeapon && ((com.friday.cultivation.item.weapon.TieredWeapon) item).isSwordWeapon();
        if (!swordLike) {
            this.stopNpcSwordFlight(true);
        }
    }

    private boolean isRealmAutomaticSpell(com.friday.cultivation.spell.Spell spell, com.friday.cultivation.realm.Realm realm) {
        if (spell == null || realm == com.friday.cultivation.realm.Realm.MORTAL) {
            return false;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.QI_REFINING.ordinal()) {
            if (spell == com.friday.cultivation.spell.Spell.SPIRIT_VISION || spell == com.friday.cultivation.spell.Spell.QI_TRANSFER || spell == com.friday.cultivation.spell.Spell.QI_SHIELD || spell == com.friday.cultivation.spell.Spell.REALM_PRESSURE) {
                return true;
            }
            if (spell == com.friday.cultivation.spell.Spell.SWORD_AURA && this.getPhysique() == com.friday.cultivation.physique.Physique.INNATE_SWORD_BODY) {
                return true;
            }
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.FOUNDATION_BUILDING.ordinal() && (spell == com.friday.cultivation.spell.Spell.SWORD_FLIGHT || spell == com.friday.cultivation.spell.Spell.BIGU)) {
            return true;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.GOLDEN_CORE.ordinal() && spell == com.friday.cultivation.spell.Spell.CORE_SELF_DESTRUCT) {
            return true;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.NASCENT_SOUL.ordinal() && spell == com.friday.cultivation.spell.Spell.NASCENT_SOUL_OUT_OF_BODY) {
            return true;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.SOUL_FORMATION.ordinal() && spell == com.friday.cultivation.spell.Spell.DIVINE_SENSE) {
            return true;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.VOID_REFINING.ordinal() && (spell == com.friday.cultivation.spell.Spell.VOID_STEP || spell == com.friday.cultivation.spell.Spell.VOID_ESCAPE)) {
            return true;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.BODY_INTEGRATION.ordinal() && spell == com.friday.cultivation.spell.Spell.DHARMA_BODY_MANIFESTATION) {
            return true;
        }
        return realm.ordinal() >= com.friday.cultivation.realm.Realm.TRUE_IMMORTAL.ordinal() && spell == com.friday.cultivation.spell.Spell.QI_FLIGHT;
    }

    private void normalizeSpellIdsForRealm(com.friday.cultivation.realm.Realm realm) {
        java.util.LinkedHashSet<String> normalized = new java.util.LinkedHashSet<>();
        this.addRealmAutomaticSpells(realm);
        for (String id : this.spellIds) {
            com.friday.cultivation.spell.Spell spell = com.friday.cultivation.spell.Spell.byId(id);
            if (spell == null || !this.isRealmAutomaticSpell(spell, realm)) continue;
            normalized.add(id);
        }
        for (String id : this.spellIds) {
            com.friday.cultivation.spell.Spell spell = com.friday.cultivation.spell.Spell.byId(id);
            if (spell == null || realm == com.friday.cultivation.realm.Realm.MORTAL) continue;
            boolean mandatory = this.isRealmAutomaticSpell(spell, realm);
            if (!mandatory && !com.friday.cultivation.entity.npc.NpcSpellCaster.isLearnableByNpc(spell)) continue;
            normalized.add(id);
        }
        this.spellIds.clear();
        this.spellIds.addAll(normalized);
    }

    private void addSpellIfMissing(com.friday.cultivation.spell.Spell spell) {
        if (spell != null && !this.spellIds.contains(spell.id())) {
            this.spellIds.add(spell.id());
        }
    }

    private void addRealmAutomaticSpells(com.friday.cultivation.realm.Realm realm) {
        if (realm == com.friday.cultivation.realm.Realm.MORTAL) {
            return;
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.QI_REFINING.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.SPIRIT_VISION);
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.QI_TRANSFER);
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.QI_SHIELD);
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.REALM_PRESSURE);
            if (this.getPhysique() == com.friday.cultivation.physique.Physique.INNATE_SWORD_BODY) {
                this.addSpellIfMissing(com.friday.cultivation.spell.Spell.SWORD_AURA);
            }
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.FOUNDATION_BUILDING.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.SWORD_FLIGHT);
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.BIGU);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.GOLDEN_CORE.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.CORE_SELF_DESTRUCT);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.NASCENT_SOUL.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.NASCENT_SOUL_OUT_OF_BODY);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.SOUL_FORMATION.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.DIVINE_SENSE);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.VOID_REFINING.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.VOID_STEP);
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.VOID_ESCAPE);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.BODY_INTEGRATION.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.DHARMA_BODY_MANIFESTATION);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.TRUE_IMMORTAL.ordinal()) {
            this.addSpellIfMissing(com.friday.cultivation.spell.Spell.QI_FLIGHT);
        }
    }

    /** 分配功法（照搬原模组 assignTechnique） */
    private void assignTechnique(com.friday.cultivation.realm.Realm realm) {
        com.friday.cultivation.ItemTier tier = WanderingCultivatorEntity.techTierForRealm(realm);
        java.util.List<com.friday.cultivation.technique.Technique> candidates = com.friday.cultivation.util.CultivationRandomPools.techniquesForTier(tier);
        if (candidates.isEmpty()) {
            return;
        }
        com.friday.cultivation.technique.Technique chosen = candidates.get(this.getRandom().nextInt(candidates.size()));
        this.setTechniqueId(chosen.id());
    }

    /** 分配法术（照搬原模组 assignSpells） */
    private void assignSpells(com.friday.cultivation.realm.Realm realm) {
        this.spellIds.clear();
        this.addRealmAutomaticSpells(realm);
        int[] range = WanderingCultivatorEntity.randomSpellCountRange(realm);
        int spellCount = range[0] + this.getRandom().nextInt(range[1] - range[0] + 1);
        java.util.List<com.friday.cultivation.spell.Spell> spellPool = WanderingCultivatorEntity.buildLearnableSpellPool(realm);
        java.util.Collections.shuffle(spellPool, new java.util.Random(this.getRandom().nextLong()));
        for (int i = 0; i < Math.min(spellCount, spellPool.size()); ++i) {
            this.addSpellIfMissing(spellPool.get(i));
        }
        if (realm == com.friday.cultivation.realm.Realm.TRIBULATION_TRANSCENDENCE) {
            this.addImmortalSpells(1);
        }
        if (realm == com.friday.cultivation.realm.Realm.TRUE_IMMORTAL || realm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL) {
            this.addImmortalSpells(1 + this.getRandom().nextInt(3));
        }
        this.normalizeSpellIdsForRealm(realm);
    }

    private static java.util.List<com.friday.cultivation.spell.Spell> buildLearnableSpellPool(com.friday.cultivation.realm.Realm realm) {
        com.friday.cultivation.ItemTier targetTier = WanderingCultivatorEntity.spellTierForRealm(realm);
        java.util.List<com.friday.cultivation.spell.Spell> pool = WanderingCultivatorEntity.collectLearnable(targetTier);
        if (pool.isEmpty() && targetTier == com.friday.cultivation.ItemTier.HIGH) {
            pool = WanderingCultivatorEntity.collectLearnable(com.friday.cultivation.ItemTier.MID);
        }
        if (pool.isEmpty()) {
            pool = WanderingCultivatorEntity.collectLearnable(com.friday.cultivation.ItemTier.LOW);
        }
        return pool;
    }

    private static java.util.List<com.friday.cultivation.spell.Spell> collectLearnable(com.friday.cultivation.ItemTier tier) {
        return new java.util.ArrayList<com.friday.cultivation.spell.Spell>(com.friday.cultivation.util.CultivationRandomPools.npcLearnableSpellsForTier(tier));
    }

    private void addImmortalSpells(int count) {
        java.util.ArrayList<com.friday.cultivation.spell.Spell> immortalPool = new java.util.ArrayList<>();
        for (com.friday.cultivation.spell.Spell s : com.friday.cultivation.spell.Spell.values()) {
            if (s.tier() != com.friday.cultivation.ItemTier.IMMORTAL || !com.friday.cultivation.entity.npc.NpcSpellCaster.isLearnableByNpc(s)) continue;
            immortalPool.add(s);
        }
        if (immortalPool.isEmpty()) {
            return;
        }
        java.util.Collections.shuffle(immortalPool, new java.util.Random(this.getRandom().nextLong()));
        for (int i = 0; i < Math.min(count, immortalPool.size()); ++i) {
            this.addSpellIfMissing(immortalPool.get(i));
        }
    }

    private static com.friday.cultivation.ItemTier techTierForRealm(com.friday.cultivation.realm.Realm realm) {
        return com.friday.cultivation.util.CultivationRandomPools.techniqueTierForRealm(realm);
    }

    private static com.friday.cultivation.ItemTier spellTierForRealm(com.friday.cultivation.realm.Realm realm) {
        return com.friday.cultivation.util.CultivationRandomPools.spellTierForRealm(realm);
    }

    private static int[] randomSpellCountRange(com.friday.cultivation.realm.Realm realm) {
        return switch (realm) {
            case MORTAL -> new int[]{0, 0};
            case GOLDEN_CORE, SOUL_FORMATION, BODY_INTEGRATION, QI_REFINING -> new int[]{1, 3};
            case FOUNDATION_BUILDING, NASCENT_SOUL, VOID_REFINING, MAHAYANA, TRIBULATION_TRANSCENDENCE, TRUE_IMMORTAL, LOOSE_IMMORTAL -> new int[]{3, 5};
        };
    }

    private static double scaleAttackByRealm(com.friday.cultivation.realm.Realm realm) {
        return switch (realm) {
            case MORTAL -> 2.0;
            case QI_REFINING -> 4.0;
            case FOUNDATION_BUILDING -> 6.0;
            case GOLDEN_CORE -> 8.0;
            case NASCENT_SOUL -> 10.0;
            case SOUL_FORMATION -> 12.0;
            case VOID_REFINING -> 14.0;
            case BODY_INTEGRATION -> 16.0;
            case MAHAYANA -> 20.0;
            case TRIBULATION_TRANSCENDENCE -> 25.0;
            case TRUE_IMMORTAL -> 30.0;
            case LOOSE_IMMORTAL -> 28.0;
        };
    }

    /** 真元/灵根/体质/身份掷定（照搬原模组 rollSpiritRoot / rollPhysique / rollIdentity / assignZhenyuanForRealm） */
    private com.friday.cultivation.spirit.SpiritRoot rollSpiritRoot(com.friday.cultivation.realm.Realm realm) {
        com.friday.cultivation.spirit.SpiritRoot[] all = com.friday.cultivation.spirit.SpiritRoot.values();
        java.util.ArrayList<com.friday.cultivation.spirit.SpiritRoot> candidates = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> weights = new java.util.ArrayList<>();
        int totalWeight = 0;
        for (com.friday.cultivation.spirit.SpiritRoot r : all) {
            int w;
            if (!r.isSelectableRoot()) continue;
            switch (r.rarity()) {
                case SSR -> w = 3;
                case SR -> w = 8;
                case R -> w = 15;
                default -> throw new IncompatibleClassChangeError();
            }
            candidates.add(r);
            weights.add(w);
            totalWeight += w;
        }
        if (candidates.isEmpty()) {
            return com.friday.cultivation.spirit.SpiritRoot.NONE;
        }
        int roll = this.getRandom().nextInt(totalWeight);
        for (int i = 0; i < candidates.size(); ++i) {
            roll -= weights.get(i);
            if (roll >= 0) continue;
            return candidates.get(i);
        }
        return candidates.get(0);
    }

    private com.friday.cultivation.physique.Physique rollPhysique() {
        java.util.List<com.friday.cultivation.physique.Physique> pool = com.friday.cultivation.physique.Physique.selectableValues();
        if (pool.isEmpty()) {
            return com.friday.cultivation.physique.Physique.MORTAL_BODY;
        }
        java.util.ArrayList<com.friday.cultivation.physique.Physique> candidates = new java.util.ArrayList<>();
        java.util.ArrayList<Integer> weights = new java.util.ArrayList<>();
        int totalWeight = 0;
        for (com.friday.cultivation.physique.Physique p : pool) {
            int w;
            switch (p.rarity()) {
                case IMMORTAL -> w = 2;
                case SUPREME -> w = 6;
                case HIGH -> w = 12;
                case SPECIAL -> w = 1;
                default -> throw new IncompatibleClassChangeError();
            }
            candidates.add(p);
            weights.add(w);
            totalWeight += w;
        }
        if (candidates.isEmpty()) {
            return com.friday.cultivation.physique.Physique.MORTAL_BODY;
        }
        int roll = this.getRandom().nextInt(totalWeight);
        for (int i = 0; i < candidates.size(); ++i) {
            roll -= weights.get(i);
            if (roll >= 0) continue;
            return candidates.get(i);
        }
        return candidates.get(0);
    }

    private com.friday.cultivation.identity.Identity rollIdentity() {
        java.util.List<com.friday.cultivation.identity.Identity> pool = com.friday.cultivation.identity.Identity.selectableOrigins();
        if (pool.isEmpty()) {
            return com.friday.cultivation.identity.Identity.LONE_CULTIVATOR;
        }
        return pool.get(this.getRandom().nextInt(pool.size()));
    }

    private void assignZhenyuanForRealm(com.friday.cultivation.realm.Realm realm, com.friday.cultivation.realm.SubStage sub) {
        int autoPerStat = WanderingCultivatorEntity.automaticZhenyuanAttrPerStat(realm, sub);
        int extraPerMinor = Math.max(0, this.getSpiritRoot().bonus().extraZhenyuanPerSubLevel()) + Math.max(0, com.friday.cultivation.physique.PhysiqueBonusHelper.extraZhenyuanPerMinor(this.getPhysique()));
        int randomPoints = WanderingCultivatorEntity.totalZhenyuanEarned(realm, sub, extraPerMinor);
        if (autoPerStat <= 0 && randomPoints <= 0) {
            this.entityData.set(DATA_ZHENYUAN_CSV, "0,0,0,0,0");
            this.normalizeGroundMovementSpeedAttribute();
            return;
        }
        int[] attrs = new int[]{autoPerStat, autoPerStat, autoPerStat, autoPerStat, autoPerStat};
        for (int i = 0; i < randomPoints; ++i) {
            int n = this.getRandom().nextInt(5);
            attrs[n] = attrs[n] + 1;
        }
        this.entityData.set(DATA_ZHENYUAN_CSV, attrs[0] + "," + attrs[1] + "," + attrs[2] + "," + attrs[3] + "," + attrs[4]);
        net.minecraft.world.entity.ai.attributes.AttributeInstance hpAttr = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            double baseHp = hpAttr.getBaseValue();
            double physiqueHpMult = Math.max(1.0, this.getPhysique().bonus().hpMult());
            double physiqueFlatHp = Math.max(0, this.getPhysique().bonus().maxHpBonus());
            hpAttr.setBaseValue((baseHp + (double) attrs[0] + physiqueFlatHp) * physiqueHpMult);
            this.setHealth(this.getMaxHealth());
        }
        net.minecraft.world.entity.ai.attributes.AttributeInstance atkAttr = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE);
        if (atkAttr != null && attrs[1] > 0) {
            atkAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(java.util.UUID.nameUUIDFromBytes(("npc.zhenyuan.atk." + this.getUUID()).getBytes()), "npc_zhenyuan_attack", (double) attrs[1], net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.ADDITION));
        }
        this.normalizeGroundMovementSpeedAttribute();
        this.maxQi += (long) attrs[4] * 100L;
        double physiqueMaxQiMult = com.friday.cultivation.physique.PhysiqueBonusHelper.maxQiMultiplier(this.getPhysique());
        if (physiqueMaxQiMult != 1.0) {
            this.maxQi = Math.max(1L, Math.round((double) this.maxQi * physiqueMaxQiMult));
        }
        this.currentQi = this.maxQi;
        this.entityData.set(DATA_MAX_QI, (int) Math.min(Integer.MAX_VALUE, this.maxQi));
        this.entityData.set(DATA_CURRENT_QI, (int) Math.min(Integer.MAX_VALUE, this.currentQi));
    }

    private static int totalZhenyuanEarned(com.friday.cultivation.realm.Realm targetRealm, com.friday.cultivation.realm.SubStage targetSub, int extraPerMinor) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == com.friday.cultivation.realm.Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL) {
            targetRealm = com.friday.cultivation.realm.Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = com.friday.cultivation.realm.SubStage.PEAK;
        }
        int majorCount = targetRealm.ordinal();
        int realmsFullyTraversed = targetRealm.ordinal() - 1;
        int minorCount = realmsFullyTraversed * 3 + targetSub.ordinal();
        return majorCount * 5 + minorCount * (1 + Math.max(0, extraPerMinor));
    }

    private static int automaticZhenyuanAttrPerStat(com.friday.cultivation.realm.Realm targetRealm, com.friday.cultivation.realm.SubStage targetSub) {
        if (targetRealm == null || targetSub == null) {
            return 0;
        }
        if (targetRealm == com.friday.cultivation.realm.Realm.MORTAL) {
            return 0;
        }
        if (targetRealm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL) {
            targetRealm = com.friday.cultivation.realm.Realm.TRIBULATION_TRANSCENDENCE;
            targetSub = com.friday.cultivation.realm.SubStage.PEAK;
        }
        int majorCount = targetRealm.ordinal();
        int realmsFullyTraversed = targetRealm.ordinal() - 1;
        int minorCount = realmsFullyTraversed * 3 + targetSub.ordinal();
        return majorCount * 5 + minorCount * 1;
    }

    /** 分配炼器/炼丹等级（照搬原模组 assignCraftingRanks，默认低阶） */
    private void assignCraftingRanks(com.friday.cultivation.realm.Realm realm) {
        com.friday.cultivation.alchemy.AlchemyRank[] ar = com.friday.cultivation.alchemy.AlchemyRank.values();
        com.friday.cultivation.refining.RefiningRank[] rr = com.friday.cultivation.refining.RefiningRank.values();
        int alchemyOrd = Math.min(ar.length - 1, Math.max(0, realm.ordinal() - 2));
        int refiningOrd = Math.min(rr.length - 1, Math.max(0, realm.ordinal() - 2));
        this.alchemyRank = ar[alchemyOrd];
        this.refiningRank = rr[refiningOrd];
    }

    /** 真元属性写入（照搬原模组 setZhenyuanAttrs 语义，通过 CSV 同步） */
    public void setZhenyuanAttrs(int[] attrs) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; ++i) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(i < attrs.length ? attrs[i] : 0);
        }
        this.entityData.set(DATA_ZHENYUAN_CSV, sb.toString());
    }

    @Override
    public void addAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("npcSoulState", this.npcSoulState);
        tag.putInt("npcSoulTicks", this.npcSoulTicks);
        tag.putBoolean("soulReaperTokenTradeAvailable", this.soulReaperTokenTradeAvailable);
        tag.putLong("currentQi", this.currentQi);
        tag.putLong("maxQi", this.maxQi);
        tag.putInt("combatStartTick", this.combatStartTick);
    }

    @Override
    public void readAdditionalSaveData(net.minecraft.nbt.CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.npcSoulState = tag.getBoolean("npcSoulState");
        this.npcSoulTicks = tag.getInt("npcSoulTicks");
        this.soulReaperTokenTradeAvailable = tag.getBoolean("soulReaperTokenTradeAvailable");
        this.currentQi = tag.getLong("currentQi");
        this.maxQi = Math.max(1L, tag.getLong("maxQi"));
        this.combatStartTick = tag.getInt("combatStartTick");
    }

    // ── 主 tick 循环（照搬原模组 tick / aiStep）──

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide && !this.isNpcSoulState()) {
            this.tickSectSleepMaintenance();
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.repairSkinVariantGenderMismatch();
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            this.stopNpcSwordFlight(true);
            this.stopNpcVoidEscapePhase();
            this.setNoGravity(false);
            this.noPhysics = false;
        }
        if (this.isNpcSoulState()) {
            this.tickNpcSoulState();
            return;
        }
        this.tickSectSleepingBedLock();
        boolean receptionGuard = this.isSectReceptionGuard();
        if (receptionGuard) {
            this.tickSectReceptionGuardCoreLink();
        } else {
            this.tickSectRoutine();
            this.tickSectTaskAutomation();
        }
        this.tickSectEnemyAwareness();
        if (this.tickCount % 20 == 0) {
            this.normalizeGroundMovementSpeedAttribute();
        }
        boolean tradingFrozen = this.isTradingFreeze();
        if (tradingFrozen) {
            net.minecraft.world.entity.LivingEntity frozenTarget = this.getTarget();
            if (frozenTarget != null) {
                if (!(frozenTarget instanceof net.minecraft.world.entity.player.Player)) {
                    this.suspendedFreezeTargetUuid = frozenTarget.getUUID();
                }
                super.setTarget(null);
                this.combatStartTick = -1;
            }
        } else if (this.wasTradingFrozen && this.suspendedFreezeTargetUuid != null) {
            net.minecraft.world.level.Level level = this.level();
            if (this.getTarget() == null && level instanceof net.minecraft.server.level.ServerLevel sl && sl.getEntity(this.suspendedFreezeTargetUuid) instanceof net.minecraft.world.entity.LivingEntity resumedTarget && resumedTarget.isAlive() && this.canTargetUnderYinYangRules(resumedTarget)) {
                this.setTarget(resumedTarget);
            }
            this.suspendedFreezeTargetUuid = null;
        }
        this.wasTradingFrozen = tradingFrozen;
        if (com.friday.cultivation.event.SoulHookHandler.isActionLocked((net.minecraft.world.entity.Entity) this)) {
            super.setTarget(null);
            this.combatStartTick = -1;
            this.getNavigation().stop();
            this.setPersistenceRequired();
            this.setDeltaMovement(0.0, 0.0, 0.0);
            this.fallDistance = 0.0f;
            return;
        }
        if (this.tickCount % 5 == 0 && com.friday.cultivation.entity.npc.NpcSpellCaster.trySelfRescue(this)) {
            return;
        }
        com.friday.cultivation.event.NpcPassiveSpellHandler.tick(this);
        boolean npcVoidEscaping = this.tickNpcVoidEscapePhase();
        if (!npcVoidEscaping) {
            this.tickNpcCombatMobility();
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target != null && this.combatStartTick < 0) {
            this.combatStartTick = this.tickCount;
        } else if (target == null && this.combatStartTick >= 0) {
            this.combatStartTick = -1;
        }
        if (this.tickCount % 20 == 0 && this.currentQi < this.maxQi && com.friday.cultivation.physique.PhysiqueBonusHelper.canCultivate(this.getPhysique())) {
            this.currentQi = Math.min(this.maxQi, this.currentQi + this.getNaturalQiRecoveryPerSecond());
        }
        if (this.tickCount % 10 == 0) {
            int curInt = (int) Math.min(Integer.MAX_VALUE, this.currentQi);
            int maxInt = (int) Math.min(Integer.MAX_VALUE, this.maxQi);
            if ((Integer) this.entityData.get(DATA_CURRENT_QI) != curInt) {
                this.entityData.set(DATA_CURRENT_QI, curInt);
            }
            if ((Integer) this.entityData.get(DATA_MAX_QI) != maxInt) {
                this.entityData.set(DATA_MAX_QI, maxInt);
            }
        }
        if (this.tickCount % 20 == 0 && this.maxQi > 0L && this.currentQi * 4L < this.maxQi) {
            this.tryStartEatingSpiritStone();
        }
        if (this.isUsingItem() && this.getUseItem().getItem() instanceof com.friday.cultivation.item.SpiritStoneItem && this.getUseItemRemainingTicks() <= 1) {
            this.completeEatingSpiritStone();
        }
        if (this.tickCount % 40 == 10 && !this.tryAutoEatRejuvenationPill() && !this.tryAutoEatClearMindPill()) {
            this.tryAutoEatQiRecoveryPill();
        }
        if (this.isSleeping()) {
            this.tickSectSleepingBedLock();
        } else if (npcVoidEscaping) {
            this.updateNpcVoidEscapeDrift();
        } else if (this.canFly()) {
            if (!this.isTradingFreeze()) {
                this.updateFlightAndQiDrain();
                if (this.isNoGravity() && this.getTarget() == null) {
                    this.updateIdleFlight();
                }
            }
        } else {
            this.updateNpcSwordFlight();
            if (this.isNpcSwordFlightActive() && !this.isTradingFreeze() && this.getTarget() == null) {
                this.updateIdleFlight();
            } else if (!this.isNpcSwordFlightActive()) {
                this.normalizeNonFlightGravity();
                this.applyNpcVoidStepSlowFall();
            }
        }
        if (this.tickCount % 10 == 0) {
            this.updateDisplayName();
        }
        if (this.isTradingFreeze()) {
            this.getNavigation().stop();
            net.minecraft.world.phys.Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(0.0, this.isNoGravity() ? 0.0 : dm.y, 0.0);
            this.setNoAi(false);
        } else if (receptionGuard) {
            this.tickSectReceptionGuardAnchor();
        }
        if (this.isSleeping()) {
            this.tickSectSleepingBedLock();
        }
    }

    /** NPC 灵魂状态 tick（照搬原模组 tickNpcSoulState） */
    private void tickNpcSoulState() {
        ++this.npcSoulTicks;
        if (this.isSleeping() && !com.friday.cultivation.event.TimeStasisHandler.isEntityStopped((net.minecraft.world.entity.Entity) this)) {
            this.setNoAi(false);
        }
        this.clearNpcSoulAggro();
        boolean locked = com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this) || com.friday.cultivation.event.SoulHookHandler.isActionLocked((net.minecraft.world.entity.Entity) this) || com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this);
        boolean canFloat = !locked && this.shouldNpcSoulFloat();
        this.setNoGravity(canFloat);
        if (locked) {
            this.setDeltaMovement(0.0, 0.0, 0.0);
        } else if (canFloat) {
            this.updateIdleFlight();
        } else {
            this.noPhysics = false;
        }
        if (this.getHealth() < this.getMaxHealth()) {
            this.setHealth(this.getMaxHealth());
        }
        this.fallDistance = 0.0f;
        int mobClearInterval = this.npcSoulTicks < 200 ? 20 : 200;
        if ((this.tickCount + this.getId()) % mobClearInterval == 0) {
            this.clearMobsTargetingNpcSoul();
        }
        if (this.tickCount % 10 == 0) {
            this.updateDisplayName();
        }
    }

    /** 自然灵气恢复每秒（照搬原模组 getNaturalQiRecoveryPerSecond） */
    public long getNaturalQiRecoveryPerSecond() {
        long baseRecovery = 10L;
        return com.friday.cultivation.event.RealmPressureHandler.applyQiRecoveryPenalty((net.minecraft.world.entity.LivingEntity) this, baseRecovery);
    }

    /** 宗门例行（照搬原模组 tickSectRoutine，本项目暂以冷却形式简化巡逻逻辑） */
    private void tickSectRoutine() {
        if (this.sectRoutineCooldown > 0) {
            --this.sectRoutineCooldown;
        }
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze()) {
            return;
        }
        if (this.sectRoutineCooldown > 0) {
            return;
        }
        this.sectRoutineCooldown = 40;
        if (this.getSectRole() == com.friday.cultivation.sect.SectRole.MASTER && this.sectCorePos != null) {
            net.minecraft.world.phys.Vec3 coreCenter = net.minecraft.world.phys.Vec3.atCenterOf(this.sectCorePos);
            if (this.distanceToSqr(coreCenter) > 64.0) {
                this.moveToward(this.sectCorePos, 0.6);
            }
        }
    }

    /** 宗门任务自动化（照搬原模组 tickSectTaskAutomation，本项目按冷却自动检查） */
    private void tickSectTaskAutomation() {
        if (this.sectTaskCooldown > 0) {
            --this.sectTaskCooldown;
        }
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze() || this.sectTaskCooldown > 0) {
            return;
        }
        this.sectTaskCooldown = 200;
    }

    /** 宗门敌人感知（照搬原模组 tickSectEnemyAwareness） */
    private void tickSectEnemyAwareness() {
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze()) {
            return;
        }
        if (this.tickCount % 20 != 0) {
            return;
        }
        net.minecraft.server.level.ServerLevel sl = (net.minecraft.server.level.ServerLevel) this.level();
        net.minecraft.world.phys.AABB box = this.getBoundingBox().inflate(24.0);
        for (net.minecraft.world.entity.Entity e : sl.getEntities(this, box, this::isSectEnemy)) {
            this.setTarget((net.minecraft.world.entity.LivingEntity) e);
            break;
        }
    }

    private boolean isSectEnemy(net.minecraft.world.entity.Entity entity) {
        return this.hasSectMembership() && entity != null && this.level() instanceof net.minecraft.server.level.ServerLevel && com.friday.cultivation.sect.SectSavedData.get((net.minecraft.server.level.ServerLevel) this.level()).isEnemyOfSect(this.getSectId(), entity);
    }

    /** 配置宗门接引守卫（照搬原模组 configureSectReceptionGuard） */
    public void configureSectReceptionGuard(@javax.annotation.Nullable net.minecraft.core.BlockPos receptionPos) {
        this.sectReceptionGuardPos = receptionPos == null ? null : receptionPos.immutable();
        this.setPersistenceRequired();
    }

    /** 是否为宗门接引守卫（照搬原模组 isSectReceptionGuard） */
    public boolean isSectReceptionGuard() {
        return this.sectReceptionGuardPos != null && this.getSectRole() == com.friday.cultivation.sect.SectRole.GUARD_DISCIPLE;
    }

    /** 宗门接引守卫核心链接 tick（照搬原模组 tickSectReceptionGuardCoreLink） */
    private void tickSectReceptionGuardCoreLink() {
        if (this.sectCorePos == null || this.getSectName().isBlank()) {
            return;
        }
        if (this.tickCount % 40 == 0) {
            this.ensureSectCoreLink(this.sectCorePos, false);
        }
    }

    /** 宗门接引守卫锚定 tick（照搬原模组 tickSectReceptionGuardAnchor） */
    private void tickSectReceptionGuardAnchor() {
        if (this.sectReceptionGuardPos == null || this.isTradingFreeze() || this.getTarget() != null) {
            return;
        }
        double distSq = this.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(this.sectReceptionGuardPos));
        if (distSq > 9.0) {
            if (distSq > 784.0) {
                this.teleportTo(this.sectReceptionGuardPos.getX() + 0.5, this.sectReceptionGuardPos.getY(), this.sectReceptionGuardPos.getZ() + 0.5);
            } else {
                this.moveToward(this.sectReceptionGuardPos, 0.6);
            }
        }
    }

    /** 宗门例行睡眠维护（照搬原模组 tickSectSleepMaintenance） */
    private void tickSectSleepMaintenance() {
        if (this.isSleeping()) {
            this.tickSectSleepingBedLock();
            return;
        }
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze()) {
            return;
        }
        long dayTime = this.level().getDayTime() % 24000L;
        boolean night = dayTime >= 12500L && dayTime <= 23500L;
        if (!night) {
            return;
        }
        net.minecraft.core.BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        net.minecraft.core.BlockPos head = foot == null ? null : this.bedHeadPos(foot);
        if (foot != null && head != null && this.canReachSectBedForSleep(foot, head)) {
            this.sleepOnSectBed(foot, head);
        }
    }

    /** 宗门睡眠床锁 tick（照搬原模组 tickSectSleepingBedLock） */
    private void tickSectSleepingBedLock() {
        if (!this.isSleeping()) {
            return;
        }
        net.minecraft.core.BlockPos head = this.getSleepingPos().orElse(null);
        if (head != null) {
            this.snapToSectBed(head);
        }
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    private void snapToSectBed(net.minecraft.core.BlockPos head) {
        double dx = head.getX() + 0.5 - this.getX();
        double dz = head.getZ() + 0.5 - this.getZ();
        this.setDeltaMovement(this.getDeltaMovement().x * 0.6 + dx * 0.4, this.getDeltaMovement().y, this.getDeltaMovement().z * 0.6 + dz * 0.4);
        this.fallDistance = 0.0f;
    }

    private net.minecraft.core.BlockPos normalizeBedFootPos(@javax.annotation.Nullable net.minecraft.core.BlockPos pos) {
        if (pos == null) {
            return null;
        }
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(pos);
        if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            if (state.getValue(net.minecraft.world.level.block.BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.HEAD) {
                return pos.relative(state.getValue(net.minecraft.world.level.block.BedBlock.FACING).getOpposite());
            }
            return pos;
        }
        return pos;
    }

    private net.minecraft.core.BlockPos bedHeadPos(net.minecraft.core.BlockPos foot) {
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(foot);
        if (state.getBlock() instanceof net.minecraft.world.level.block.BedBlock) {
            return state.getValue(net.minecraft.world.level.block.BedBlock.PART) == net.minecraft.world.level.block.state.properties.BedPart.HEAD ? foot : foot.relative(state.getValue(net.minecraft.world.level.block.BedBlock.FACING));
        }
        return null;
    }

    private boolean canReachSectBedForSleep(net.minecraft.core.BlockPos foot, net.minecraft.core.BlockPos head) {
        double distSq = this.distanceToSqr(head.getX() + 0.5, head.getY() + 0.6875, head.getZ() + 0.5);
        if (distSq > 6.25) {
            return false;
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        return target == null || this.distanceToSqr(target) > 144.0;
    }

    private void sleepOnSectBed(net.minecraft.core.BlockPos foot, net.minecraft.core.BlockPos head) {
        this.setSleepingPos(head);
        this.setDeltaMovement(0.0, 0.0, 0.0);
    }

    private void stopSectSleepMotion() {
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.setNoGravity(false);
        this.fallDistance = 0.0f;
    }

    // ── 虚遁 / 虚步 / 御剑 / 飞行（照搬原模组）──

    private void startNpcVoidEscapePhase() {
        if (!this.isNpcVoidEscapeActive()) {
            this.npcVoidEscapePreviousNoGravity = this.isNoGravity();
            this.npcVoidEscapePreviousNoPhysics = this.noPhysics;
        }
        this.npcVoidEscapeActiveTicks = 160;
        this.applyNpcVoidEscapePhase();
    }

    private void applyNpcVoidEscapePhase() {
        this.noPhysics = true;
        this.setNoGravity(true);
        this.fallDistance = 0.0f;
    }

    private boolean tickNpcVoidEscapePhase() {
        if (!this.isNpcVoidEscapeActive()) {
            return false;
        }
        if (com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this)) {
            this.stopNpcVoidEscapePhase();
            return false;
        }
        --this.npcVoidEscapeActiveTicks;
        this.applyNpcVoidEscapePhase();
        if (this.npcVoidEscapeActiveTicks <= 0) {
            this.stopNpcVoidEscapePhase();
            return false;
        }
        return true;
    }

    private void stopNpcVoidEscapePhase() {
        this.npcVoidEscapeActiveTicks = 0;
        this.noPhysics = this.npcVoidEscapePreviousNoPhysics;
        boolean restoreNoGravity = !com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this) && !com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this) && (this.npcVoidEscapePreviousNoGravity || this.canFly() || this.isNpcSwordFlightActive());
        this.setNoGravity(restoreNoGravity);
        this.fallDistance = 0.0f;
        this.npcVoidEscapePreviousNoGravity = false;
        this.npcVoidEscapePreviousNoPhysics = false;
    }

    private void updateNpcVoidEscapeDrift() {
        this.updateIdleFlight();
        if (this.tickCount % 10 == 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + (double) this.getBbHeight() * 0.5, this.getZ(), 3, 0.28, 0.35, 0.28, 0.015);
        }
    }

    private void tickNpcCombatMobility() {
        if (this.npcVoidStepCooldownTicks > 0) {
            --this.npcVoidStepCooldownTicks;
        }
        if (this.npcVoidEscapeCooldownTicks > 0) {
            --this.npcVoidEscapeCooldownTicks;
        }
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return;
        }
        if (this.isNpcVoidEscapeActive()) {
            return;
        }
        if (this.isTradingFreeze() || com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this)) {
            return;
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (this.tryNpcVoidEscape(target)) {
            return;
        }
        if (!this.isNpcSwordFlightActive() && this.canStartNpcSwordFlightForCombat() && this.shouldStartNpcSwordFlight(target)) {
            this.startNpcSwordFlight();
            return;
        }
        this.tryNpcVoidStep(target);
    }

    private boolean tryNpcVoidEscape(net.minecraft.world.entity.LivingEntity threat) {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return false;
        }
        if (this.npcVoidEscapeCooldownTicks > 0) {
            return false;
        }
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.VOID_ESCAPE.id())) {
            return false;
        }
        if (this.getHealth() > this.getMaxHealth() * 0.32f) {
            return false;
        }
        long cost = Math.max(1L, com.friday.cultivation.entity.npc.NpcSpellCaster.generalQiCost(this, 400L));
        if (this.getCurrentQi() < cost) {
            return false;
        }
        this.deductQi(cost);
        this.npcVoidEscapeCooldownTicks = 360;
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.INVISIBILITY, 160, 0, false, false, true));
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.MOVEMENT_SPEED, 160, 1, false, false, true));
        this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
        this.teleportAwayFromThreat(threat);
        this.startNpcVoidEscapePhase();
        super.setTarget(null);
        this.combatStartTick = -1;
        net.minecraft.world.level.Level level = this.level();
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 0.8f, 0.7f);
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + (double) this.getBbHeight() * 0.5, this.getZ(), 24, 0.35, 0.5, 0.35, 0.05);
        }
        return true;
    }

    private void teleportAwayFromThreat(net.minecraft.world.entity.LivingEntity threat) {
        net.minecraft.world.phys.Vec3 away = this.position().subtract(threat.position());
        if (away.lengthSqr() < 1.0E-4) {
            away = new net.minecraft.world.phys.Vec3(this.getRandom().nextDouble() - 0.5, 0.0, this.getRandom().nextDouble() - 0.5);
        }
        net.minecraft.world.phys.Vec3 horizontalAway = new net.minecraft.world.phys.Vec3(away.x, 0.0, away.z).normalize();
        for (int i = 0; i < 8; ++i) {
            double spread = (this.getRandom().nextDouble() - 0.5) * 1.6;
            double cos = Math.cos(spread);
            double sin = Math.sin(spread);
            double dx = horizontalAway.x * cos - horizontalAway.z * sin;
            double dz = horizontalAway.x * sin + horizontalAway.z * cos;
            double distance = 12.0 + this.getRandom().nextDouble() * 10.0;
            double x = this.getX() + dx * distance;
            double z = this.getZ() + dz * distance;
            int groundY = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, net.minecraft.util.Mth.floor(x), net.minecraft.util.Mth.floor(z));
            double y = Math.max((double) this.level().getMinBuildHeight() + 1.0, Math.min((double) this.level().getMaxBuildHeight() - 2.0, (double) groundY + 0.1));
            if (!this.randomTeleport(x, y, z, true)) continue;
            return;
        }
    }

    private boolean tryNpcVoidStep(net.minecraft.world.entity.LivingEntity target) {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return false;
        }
        if (this.npcVoidStepCooldownTicks > 0) {
            return false;
        }
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.VOID_STEP.id())) {
            return false;
        }
        if (this.canUseCombatFlight()) {
            return false;
        }
        double distSqr = this.distanceToSqr((net.minecraft.world.entity.Entity) target);
        boolean targetFlying = target.isNoGravity() || target.isFallFlying() || target instanceof net.minecraft.world.entity.player.Player && ((net.minecraft.world.entity.player.Player) target).getAbilities().mayfly;
        boolean needsReengage = distSqr > 121.0 || target.getEyeY() > this.getEyeY() + 2.5 || targetFlying;
        if (!needsReengage) {
            return false;
        }
        long cost = Math.max(1L, com.friday.cultivation.entity.npc.NpcSpellCaster.generalQiCost(this, 60L));
        if (this.getCurrentQi() < cost) {
            return false;
        }
        net.minecraft.world.phys.Vec3 toTarget = target.getEyePosition().subtract(this.getEyePosition());
        net.minecraft.world.phys.Vec3 horizontal = new net.minecraft.world.phys.Vec3(toTarget.x, 0.0, toTarget.z);
        if (horizontal.lengthSqr() < 1.0E-4) {
            return false;
        }
        net.minecraft.world.phys.Vec3 dir = horizontal.normalize();
        double distance = Math.sqrt(distSqr);
        double speed = distance > 18.0 ? 0.62 : 0.46;
        double vertical = target.getEyeY() > this.getEyeY() + 2.5 ? 0.26 : Math.max(-0.1, this.getDeltaMovement().y);
        this.deductQi(cost);
        this.npcVoidStepCooldownTicks = 24 + this.getRandom().nextInt(10);
        this.setNoGravity(false);
        this.setDeltaMovement(dir.x * speed, vertical, dir.z * speed);
        this.hurtMarked = true;
        this.fallDistance = 0.0f;
        if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, this.getX(), this.getY() + 0.05, this.getZ(), 6, 0.25, 0.05, 0.25, 0.01);
        }
        return true;
    }

    private void normalizeNonFlightGravity() {
        if (this.isNpcVoidEscapeActive()) {
            return;
        }
        if (!this.canFly() && !this.isNpcSwordFlightActive() && this.isNoGravity()) {
            this.setNoGravity(false);
        }
    }

    private void applyNpcVoidStepSlowFall() {
        if (this.onGround()) {
            return;
        }
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            return;
        }
        if (this.isTradingFreeze() || com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this)) {
            return;
        }
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.VOID_STEP.id())) {
            return;
        }
        if (this.canUseCombatFlight()) {
            return;
        }
        if (!com.friday.cultivation.event.VoidStepHandler.hasSlowFallClearance(this.level(), this.blockPosition(), this.getY())) {
            return;
        }
        net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
        if (v.y < -0.1) {
            this.setDeltaMovement(v.x, -0.1, v.z);
            this.hurtMarked = true;
        }
        this.fallDistance = 0.0f;
        if ((this.tickCount & 7) == 0 && this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, this.getX(), this.getY() - 0.1, this.getZ(), 2, 0.3, 0.05, 0.3, 0.0);
        }
    }

    private void normalizeGroundMovementSpeedAttribute() {
        net.minecraft.world.entity.ai.attributes.AttributeInstance speedAttr = this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) {
            return;
        }
        java.util.UUID oldZhenyuanSpeed = java.util.UUID.nameUUIDFromBytes(("npc.zhenyuan.spd." + this.getUUID()).getBytes(java.nio.charset.StandardCharsets.UTF_8));
        if (speedAttr.getModifier(oldZhenyuanSpeed) != null) {
            speedAttr.removeModifier(oldZhenyuanSpeed);
        }
        double speedBonus = (double) this.getZhenyuanAgility() * 1.0 / 100.0;
        net.minecraft.world.entity.ai.attributes.AttributeModifier existing = speedAttr.getModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
        if (speedBonus <= 0.0) {
            if (existing != null) {
                speedAttr.removeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
            }
            return;
        }
        if (existing != null && Math.abs(existing.getAmount() - speedBonus) <= 1.0E-6 && existing.getOperation() == net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE) {
            return;
        }
        if (existing != null) {
            speedAttr.removeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
        }
        speedAttr.addTransientModifier(new net.minecraft.world.entity.ai.attributes.AttributeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID, "npc_zhenyuan_ground_speed", speedBonus, net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation.MULTIPLY_BASE));
    }

    private void updateNpcSwordFlight() {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            this.stopNpcSwordFlight(true);
            return;
        }
        if (this.canFly()) {
            this.stopNpcSwordFlight(false);
            return;
        }
        if (this.isTradingFreeze() || com.friday.cultivation.event.SpiritLockHandler.isEntityLocked((net.minecraft.world.entity.Entity) this) || !(this.getMainHandItem().getItem() instanceof net.minecraft.world.item.SwordItem)) {
            this.stopNpcSwordFlight(true);
            return;
        }
        net.minecraft.world.entity.LivingEntity target = this.getTarget();
        if (!this.isNpcSwordFlightActive()) {
            if (this.shouldStartNpcSwordFlight(target)) {
                this.startNpcSwordFlight();
            }
            return;
        }
        this.npcSwordFlightIdleTicks = target == null || !target.isAlive() ? ++this.npcSwordFlightIdleTicks : 0;
        if (this.npcSwordFlightIdleTicks > 160) {
            this.stopNpcSwordFlight(true);
            return;
        }
        if (this.tickCount % 20 == 0) {
            long upkeep = Math.max(1L, com.friday.cultivation.entity.npc.NpcSpellCaster.generalQiCost(this, 20L));
            if (this.currentQi < upkeep) {
                this.stopNpcSwordFlight(true);
            } else {
                this.deductQi(upkeep);
            }
        }
    }

    private void updateFlightAndQiDrain() {
        if (com.friday.cultivation.event.RealmPressureHandler.isSuppressed((net.minecraft.world.entity.LivingEntity) this)) {
            this.stopNpcSwordFlight(true);
            this.setNoGravity(false);
            this.noPhysics = false;
            return;
        }
        if (this.isNpcSoulState()) {
            boolean canFloat = this.shouldNpcSoulFloat();
            if (canFloat) {
                if (!this.isNoGravity()) {
                    this.setNoGravity(true);
                    this.setDeltaMovement(this.getDeltaMovement().x, Math.max(0.18, this.getDeltaMovement().y), this.getDeltaMovement().z);
                }
            } else {
                this.setNoGravity(false);
                this.noPhysics = false;
            }
            this.fallDistance = 0.0f;
            return;
        }
        boolean wasFlying = this.isNoGravity();
        long oneThird = Math.max(1L, this.maxQi / 3L);
        long twoThirds = Math.max(1L, this.maxQi * 2L / 3L);
        boolean shouldFly;
        if (wasFlying) {
            shouldFly = this.currentQi > oneThird;
        } else {
            shouldFly = this.currentQi >= twoThirds;
        }
        if (shouldFly != wasFlying) {
            this.setNoGravity(shouldFly);
            if (shouldFly) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.4, this.getDeltaMovement().z);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.PORTAL, this.getX(), this.getY() + 0.1, this.getZ(), 16, 0.4, 0.05, 0.4, 0.04);
                }
            } else {
                net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x, Math.min(v.y, -0.05), v.z);
                if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                    sl.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, this.getX(), this.getY() + 0.1, this.getZ(), 8, 0.3, 0.05, 0.3, 0.02);
                }
            }
        }
        if (shouldFly && this.tickCount % 10 == 0) {
            this.currentQi = Math.max(0L, this.currentQi - 5L);
        }
    }

    private void updateIdleFlight() {
        int groundY = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
        double targetY = (double) groundY + 8.0;
        double delta = targetY - this.getY();
        double yMomentum = delta > 1.5 ? 0.1 : (delta < -1.5 ? -0.05 : Math.sin((double) this.tickCount / 20.0) * 0.025);
        net.minecraft.world.phys.Vec3 v = this.getDeltaMovement();
        double driftX;
        double driftZ;
        if (this.isNpcSoulState()) {
            if (--this.soulDriftTicks <= 0) {
                this.soulDriftTicks = 35 + this.getRandom().nextInt(41);
                double angle = this.getRandom().nextDouble() * Math.PI * 2.0;
                double speed = 0.035 + this.getRandom().nextDouble() * 0.045;
                this.soulDriftX = Math.cos(angle) * speed;
                this.soulDriftZ = Math.sin(angle) * speed;
            }
            driftX = this.soulDriftX;
            driftZ = this.soulDriftZ;
        } else {
            double driftAngle = (double) this.tickCount / 80.0 * Math.PI * 2.0;
            driftX = Math.cos(driftAngle) * 0.02;
            driftZ = Math.sin(driftAngle) * 0.02;
        }
        this.setDeltaMovement(v.x * 0.8 + driftX, yMomentum, v.z * 0.8 + driftZ);
    }

    /** 真元敏捷属性（照搬原模组 getZhenyuanAgility） */
    public int getZhenyuanAgility() {
        return this.getZhenyuanAttrs()[2];
    }

    /** 真元攻击加成（照搬原模组 getZhenyuanAttackBonus） */
    public int getZhenyuanAttackBonus() {
        return this.getZhenyuanAttrs()[0];
    }

    private void moveToward(net.minecraft.core.BlockPos pos, double speed) {
        this.getNavigation().moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, speed);
    }

    // ── 宗门夜间 / 床 / 垫子例行（照搬原模组）──

    private void tickSectNightRoutine() {
        if (this.isPassenger()) {
            this.stopRiding();
        }
        net.minecraft.core.BlockPos bed = this.findUsableBed();
        if (bed != null) {
            net.minecraft.core.BlockPos foot = this.normalizeBedFootPos(bed);
            net.minecraft.core.BlockPos head = foot == null ? null : this.bedHeadPos(foot);
            if (foot == null || head == null) {
                this.updateSectBedClaim(null);
            } else if (this.canReachSectBedForSleep(foot, head)) {
                this.sleepOnSectBed(foot, head);
            } else {
                this.moveToward(foot, 1.0);
            }
            return;
        }
        if (this.sectHomePos != null) {
            this.moveToward(this.sectHomePos, 1.0);
        }
    }

    private boolean canAttemptImmediateSectBedSleep() {
        net.minecraft.core.BlockPos bed = this.findUsableBed();
        net.minecraft.core.BlockPos foot = this.normalizeBedFootPos(bed);
        net.minecraft.core.BlockPos head = foot == null ? null : this.bedHeadPos(foot);
        return foot != null && head != null && this.canReachSectBedForSleep(foot, head);
    }

    private boolean shouldReleaseTargetForSectSleep(net.minecraft.world.entity.LivingEntity target) {
        if (target instanceof net.minecraft.world.entity.player.Player) {
            return false;
        }
        if (this.getLastHurtByMob() == target && this.tickCount - this.getLastHurtByMobTimestamp() < 200) {
            return false;
        }
        net.minecraft.core.BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        net.minecraft.core.BlockPos head = foot == null ? null : this.bedHeadPos(foot);
        return foot != null && head != null && this.canReachSectBedForSleep(foot, head) && this.distanceToSqr(target) > 144.0;
    }

    private void clearTargetForSectSleep() {
        super.setTarget(null);
        this.combatStartTick = -1;
        this.setAggressive(false);
        this.getNavigation().stop();
        this.setPersistenceRequired();
        this.stopNpcSwordFlight(true);
        this.setNoAi(false);
    }

    private void tickSectDayRoutine() {
        if (this.isSleeping()) {
            this.stopSleeping();
            this.updateSectBedClaim(null);
            return;
        }
        if (this.getSectRole() == com.friday.cultivation.sect.SectRole.MASTER && this.sectCorePos != null && this.tickCount % 60 == 0) {
            if (this.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(this.sectCorePos)) > 64.0) {
                this.moveToward(this.sectCorePos, 0.6);
            }
        }
    }

    private net.minecraft.core.BlockPos findUsableBed() {
        if (this.sectBedPos == null) {
            return null;
        }
        net.minecraft.core.BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        if (foot == null) {
            return null;
        }
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(foot);
        return WanderingCultivatorEntity.isUsableBedState(state) ? foot : null;
    }

    private static boolean isUsableBedState(net.minecraft.world.level.block.state.BlockState state) {
        return state.getBlock() instanceof net.minecraft.world.level.block.BedBlock;
    }

    private void updateSectBedClaim(@javax.annotation.Nullable net.minecraft.core.BlockPos next) {
        this.sectBedPos = next == null ? null : next.immutable();
    }

    private void updateSectCushionClaim(@javax.annotation.Nullable net.minecraft.core.BlockPos next) {
        this.sectCushionPos = next == null ? null : next.immutable();
    }

    private void syncSectRoutineClaims() {
        // 原模组在此将 sectBedPos/sectCushionPos 同步至持久数据；项目采用普通字段即时生效，无需额外同步。
    }

    private net.minecraft.core.BlockPos findUsableCushion() {
        if (this.sectCushionPos == null) {
            return null;
        }
        net.minecraft.world.level.block.state.BlockState state = this.level().getBlockState(this.sectCushionPos);
        return state.is(com.friday.cultivation.registry.ModBlocks.CUSHION.get()) ? this.sectCushionPos : null;
    }

    private void sitOnCushion(net.minecraft.core.BlockPos cushion) {
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.setPos(cushion.getX() + 0.5, cushion.getY() + 0.1, cushion.getZ() + 0.5);
    }

    private net.minecraft.core.BlockPos findNearbyBlock(net.minecraft.core.BlockPos center, int radius, java.util.function.Predicate<net.minecraft.world.level.block.state.BlockState> predicate) {
        net.minecraft.core.BlockPos.MutableBlockPos cursor = new net.minecraft.core.BlockPos.MutableBlockPos();
        for (int y = -radius; y <= radius; ++y) {
            for (int x = -radius; x <= radius; ++x) {
                for (int z = -radius; z <= radius; ++z) {
                    cursor.set(center.getX() + x, center.getY() + y, center.getZ() + z);
                    if (!predicate.test(this.level().getBlockState(cursor))) continue;
                    return cursor.immutable();
                }
            }
        }
        return null;
    }

    private net.minecraft.core.BlockPos findNearbyUnclaimedCushion(net.minecraft.core.BlockPos center, int radius) {
        return this.findNearbyBlock(center, radius, state -> state.is(com.friday.cultivation.registry.ModBlocks.CUSHION.get()));
    }

    private boolean ownsCurrentCushionClaim(net.minecraft.core.BlockPos cushion) {
        return this.sectCushionPos != null && this.sectCushionPos.equals(cushion);
    }

    private boolean isCushionClaimedByOther(net.minecraft.core.BlockPos cushion) {
        return false;
    }

    private java.util.List<WanderingCultivatorEntity> nearbySectCultivators(net.minecraft.core.BlockPos center) {
        return this.level().getEntitiesOfClass(WanderingCultivatorEntity.class, new net.minecraft.world.phys.AABB(center).inflate(16.0), npc -> npc != this && npc.hasSectMembership());
    }

    private static void putOptionalBlockPos(net.minecraft.nbt.CompoundTag tag, String key, @javax.annotation.Nullable net.minecraft.core.BlockPos pos) {
        if (pos != null) {
            tag.putLong(key, pos.asLong());
        }
    }

    private static net.minecraft.core.BlockPos readOptionalBlockPos(net.minecraft.nbt.CompoundTag tag, String key) {
        return tag.contains(key, 4) ? net.minecraft.core.BlockPos.of(tag.getLong(key)) : null;
    }

    // ── 自动进食灵石 / 丹药（照搬原模组）──

    private static long qiGainFromStone(net.minecraft.world.item.Item item) {
        if (item instanceof com.friday.cultivation.item.SpiritStoneItem) {
            return ((com.friday.cultivation.item.SpiritStoneItem) item).qiAmount();
        }
        return 0L;
    }

    private void tryStartEatingSpiritStone() {
        if (this.isUsingItem()) {
            return;
        }
        if (this.currentQi >= this.maxQi) {
            return;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        net.minecraft.world.item.ItemStack bestStack = net.minecraft.world.item.ItemStack.EMPTY;
        long bestGain = 0L;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(i);
            long gain = WanderingCultivatorEntity.qiGainFromStone(stack.getItem());
            if (stack.isEmpty() || gain <= bestGain) continue;
            bestGain = gain;
            bestStack = stack;
        }
        if (bestStack.isEmpty()) {
            return;
        }
        net.minecraft.world.item.ItemStack visual = new net.minecraft.world.item.ItemStack(bestStack.getItem(), 1);
        this.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, visual);
        this.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND);
    }

    private void completeEatingSpiritStone() {
        net.minecraft.world.item.ItemStack used = this.getItemInHand(net.minecraft.world.InteractionHand.OFF_HAND);
        long gain = WanderingCultivatorEntity.qiGainFromStone(used.getItem());
        if (gain > 0L) {
            this.currentQi = Math.min(this.maxQi, this.currentQi + gain);
            net.minecraft.world.SimpleContainer inv = this.getInventory();
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                net.minecraft.world.item.ItemStack stack = inv.getItem(i);
                if (stack.isEmpty() || stack.getItem() != used.getItem()) continue;
                stack.shrink(1);
                break;
            }
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(net.minecraft.core.particles.ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + (double) this.getBbHeight() * 0.8, this.getZ(), 14, 0.4, 0.2, 0.4, 0.02);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_EAT, net.minecraft.sounds.SoundSource.NEUTRAL, 0.5f, 1.6f);
            }
        }
        this.setItemInHand(net.minecraft.world.InteractionHand.OFF_HAND, net.minecraft.world.item.ItemStack.EMPTY);
        this.setPersistenceRequired();
    }

    private boolean tryAutoEatRejuvenationPill() {
        float hp = this.getHealth();
        float maxHp = this.getMaxHealth();
        if (hp <= 0.0f || maxHp <= 0.0f || hp * 3.0f > maxHp) {
            return false;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack s = inv.getItem(i);
            int idx = WanderingCultivatorEntity.rejuvenationPillTier(s.getItem());
            if (s.isEmpty() || idx <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        net.minecraft.world.item.Item consumedItem = inv.getItem(bestSlot).getItem();
        inv.getItem(bestSlot).shrink(1);
        com.friday.cultivation.alchemy.PillTier tier = com.friday.cultivation.alchemy.PillTier.values()[bestIdx];
        float healAmount = com.friday.cultivation.alchemy.PillEffectSpecs.rejuvenationHeal(consumedItem, tier, maxHp);
        this.heal(healAmount);
        int regenTicks = com.friday.cultivation.alchemy.PillEffectSpecs.regenerationTicks(consumedItem, tier);
        if (regenTicks > 0) {
            this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.REGENERATION, regenTicks, com.friday.cultivation.alchemy.PillEffectSpecs.regenerationAmplifier(consumedItem, tier)));
        }
        int absorptionTicks = com.friday.cultivation.alchemy.PillEffectSpecs.absorptionTicks(consumedItem, tier);
        if (absorptionTicks > 0) {
            this.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.ABSORPTION, absorptionTicks, com.friday.cultivation.alchemy.PillEffectSpecs.absorptionAmplifier(consumedItem, tier)));
        }
        this.playPillEatFx(net.minecraft.core.particles.ParticleTypes.HEART);
        return true;
    }

    private boolean tryAutoEatClearMindPill() {
        boolean hasNegative = false;
        for (net.minecraft.world.effect.MobEffectInstance e : this.getActiveEffects()) {
            if (e.getEffect().getCategory() != net.minecraft.world.effect.MobEffectCategory.HARMFUL) continue;
            hasNegative = true;
            break;
        }
        if (!hasNegative) {
            return false;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack s = inv.getItem(i);
            int idx = WanderingCultivatorEntity.clearMindPillTier(s.getItem());
            if (s.isEmpty() || idx <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        inv.getItem(bestSlot).shrink(1);
        this.getActiveEffects().stream().filter(e -> e.getEffect().getCategory() == net.minecraft.world.effect.MobEffectCategory.HARMFUL).map(net.minecraft.world.effect.MobEffectInstance::getEffect).toList().forEach(this::removeEffect);
        this.playPillEatFx(net.minecraft.core.particles.ParticleTypes.WITCH);
        return true;
    }

    private boolean tryAutoEatQiRecoveryPill() {
        if (this.maxQi <= 0L || this.currentQi * 4L > this.maxQi) {
            return false;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (WanderingCultivatorEntity.qiGainFromStone(inv.getItem(i).getItem()) <= 0L) continue;
            return false;
        }
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack s = inv.getItem(i);
            int idx = WanderingCultivatorEntity.qiRecoveryPillTier(s.getItem());
            if (s.isEmpty() || idx <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        net.minecraft.world.item.Item consumedQiPill = inv.getItem(bestSlot).getItem();
        inv.getItem(bestSlot).shrink(1);
        long fallbackGain = switch (bestIdx) {
            case 0 -> 10L;
            case 1 -> 100L;
            case 2 -> 1000L;
            case 3 -> 10000L;
            case 4 -> this.maxQi;
            default -> 0L;
        };
        long gain = com.friday.cultivation.alchemy.PillEffectSpecs.qiAmount(consumedQiPill, (int) Math.min(Integer.MAX_VALUE, fallbackGain));
        if (gain < 0L) {
            gain = this.maxQi;
        }
        this.addQi(gain);
        this.playPillEatFx(net.minecraft.core.particles.ParticleTypes.ENCHANT);
        return true;
    }

    private static int rejuvenationPillTier(net.minecraft.world.item.Item item) {
        if (item instanceof com.friday.cultivation.item.RejuvenationPillItem pill) {
            return pill.tier().ordinal();
        }
        return -1;
    }

    private static int clearMindPillTier(net.minecraft.world.item.Item item) {
        if (item instanceof com.friday.cultivation.item.ClearMindPillItem pill) {
            return pill.tier().ordinal();
        }
        return -1;
    }

    private static int qiRecoveryPillTier(net.minecraft.world.item.Item item) {
        if (item instanceof com.friday.cultivation.item.PillItem pill && item.getClass() == com.friday.cultivation.item.PillItem.class) {
            return pill.tier().ordinal();
        }
        return -1;
    }

    private void playPillEatFx(net.minecraft.core.particles.ParticleOptions particle) {
        net.minecraft.world.level.Level level = this.level();
        if (level instanceof net.minecraft.server.level.ServerLevel sl) {
            sl.sendParticles(particle, this.getX(), this.getY() + (double) this.getBbHeight() * 0.85, this.getZ(), 12, 0.35, 0.18, 0.35, 0.04);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), net.minecraft.sounds.SoundEvents.GENERIC_DRINK, net.minecraft.sounds.SoundSource.NEUTRAL, 0.45f, 1.4f);
        }
    }

    private void updateDisplayName() {
        com.friday.cultivation.realm.Realm realm = this.getRealm();
        com.friday.cultivation.realm.SubStage sub = this.getSubStage();
        net.minecraft.network.chat.Component realmDisplay = realm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL ? net.minecraft.network.chat.Component.translatable("realm.xiaoxiang_cultivation.loose_immortal.level." + this.getLooseImmortalTribulations()) : realm.displayName();
        net.minecraft.network.chat.MutableComponent realmText = realm.displayName().copy().append(" ").append(this.getCultivatorName()).append(" (").append(realmDisplay);
        if (realm != com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL) {
            realmText.append(" ").append(net.minecraft.network.chat.Component.translatable("sub_stage.xiaoxiang_cultivation." + sub.name().toLowerCase()));
        }
        realmText.append(")").withStyle(WanderingCultivatorEntity.realmColor(realm));
        net.minecraft.ChatFormatting qiColor = this.maxQi == 0L ? net.minecraft.ChatFormatting.GRAY : (this.currentQi * 100L / Math.max(1L, this.maxQi) >= 50L ? net.minecraft.ChatFormatting.GREEN : (this.currentQi * 100L / Math.max(1L, this.maxQi) >= 20L ? net.minecraft.ChatFormatting.YELLOW : net.minecraft.ChatFormatting.RED));
        net.minecraft.network.chat.MutableComponent qiText = net.minecraft.network.chat.Component.literal("  灵气 " + this.currentQi + "/" + this.maxQi).withStyle(qiColor);
        this.setCustomName(realmText.append(qiText));
        this.setCustomNameVisible(true);
    }

    private static net.minecraft.ChatFormatting realmColor(com.friday.cultivation.realm.Realm realm) {
        return switch (realm) {
            case MORTAL -> net.minecraft.ChatFormatting.GRAY;
            case QI_REFINING -> net.minecraft.ChatFormatting.AQUA;
            case FOUNDATION_BUILDING -> net.minecraft.ChatFormatting.GOLD;
            case GOLDEN_CORE -> net.minecraft.ChatFormatting.YELLOW;
            case NASCENT_SOUL -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
            case SOUL_FORMATION -> net.minecraft.ChatFormatting.DARK_PURPLE;
            case VOID_REFINING -> net.minecraft.ChatFormatting.BLUE;
            case BODY_INTEGRATION -> net.minecraft.ChatFormatting.WHITE;
            case MAHAYANA -> net.minecraft.ChatFormatting.RED;
            case TRIBULATION_TRANSCENDENCE -> net.minecraft.ChatFormatting.DARK_BLUE;
            case TRUE_IMMORTAL -> net.minecraft.ChatFormatting.GOLD;
            case LOOSE_IMMORTAL -> net.minecraft.ChatFormatting.LIGHT_PURPLE;
        };
    }

    /** 远离玩家时是否移除（照搬原模组 removeWhenFarAway：永不自然移除） */
    @Override
    public boolean removeWhenFarAway(double distanceToClosestPlayer) {
        return false;
    }

    /** 跳跃高度加成（照搬原模组 jumpFromGround：真元敏捷加成） */
    @Override
    protected void jumpFromGround() {
        super.jumpFromGround();
        double heightBonus = (double) this.getZhenyuanAgility() * 0.2 / 100.0;
        if (heightBonus <= 0.0) {
            return;
        }
        double velocityBonus = Math.sqrt(1.0 + heightBonus) - 1.0;
        net.minecraft.world.phys.Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y + 0.42 * velocityBonus, movement.z);
        this.hurtMarked = true;
    }

    /** 生成规则（照搬原模组 checkCultivatorSpawnRules） */
    public static boolean checkCultivatorSpawnRules(net.minecraft.world.entity.EntityType<WanderingCultivatorEntity> type, net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.entity.MobSpawnType reason, net.minecraft.core.BlockPos pos, net.minecraft.util.RandomSource random) {
        if (!net.minecraft.world.entity.Mob.checkMobSpawnRules(type, level, reason, pos, random)) {
            return false;
        }
        if (reason != net.minecraft.world.entity.MobSpawnType.NATURAL) {
            return true;
        }
        if (level.getLevel().dimension() != net.minecraft.world.level.Level.OVERWORLD) {
            return true;
        }
        double chance = WanderingCultivatorEntity.isNearPreferredSpawnStructure(level, pos) ? 2.0E-4 : 5.0E-5;
        return random.nextDouble() < chance;
    }

    private static boolean isNearPreferredSpawnStructure(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.core.BlockPos pos) {
        // 原模组依赖 tag xiaoxiang_cultivation:wandering_cultivator_preferred（STRUCTURE 注册表），
        // 项目未建立该结构 tag，无法照搬检索逻辑；保持返回 false 以免误判结构邻近加成。
        return false;
    }

    // ── 死亡掉落（照搬原模组 dropCustomDeathLoot / moveDeathLootToCorpse）──

    @Override
    protected void dropCustomDeathLoot(net.minecraft.world.damagesource.DamageSource source, int looting, boolean recentlyHitIn) {
        super.dropCustomDeathLoot(source, looting, recentlyHitIn);
        if (this.level().isClientSide) {
            return;
        }
        if (this.getRandom().nextFloat() < 0.5f) {
            String techId = this.getTechniqueId();
            com.friday.cultivation.technique.Technique tech = techId.isEmpty() ? null : com.friday.cultivation.technique.Technique.byId(techId);
            if (tech != null && WanderingCultivatorEntity.techniqueBookItem(tech) != null) {
                this.spawnAtLocation(new net.minecraft.world.item.ItemStack(WanderingCultivatorEntity.techniqueBookItem(tech)));
            }
        }
        if (!this.spellIds.isEmpty()) {
            String pickId = this.spellIds.get(this.getRandom().nextInt(this.spellIds.size()));
            com.friday.cultivation.spell.Spell pick = com.friday.cultivation.spell.Spell.byId(pickId);
            if (pick != null && WanderingCultivatorEntity.spellBookItem(pick) != null) {
                this.spawnAtLocation(new net.minecraft.world.item.ItemStack(WanderingCultivatorEntity.spellBookItem(pick)));
            }
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (WanderingCultivatorEntity.shouldDropInventoryStack(stack)) {
                this.spawnAtLocation(stack.copy());
            }
            stack.setCount(0);
        }
    }

    public void moveDeathLootToCorpse(com.friday.cultivation.entity.npc.CorpseEntity corpse) {
        if (corpse == null || this.level().isClientSide) {
            return;
        }
        net.minecraft.world.item.ItemStack main = this.getMainHandItem();
        if (!main.isEmpty()) {
            corpse.moveItemIntoLoot(main.copy());
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        net.minecraft.world.item.ItemStack offhand = this.getOffhandItem();
        if (!offhand.isEmpty()) {
            corpse.moveItemIntoLoot(offhand.copy());
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, net.minecraft.world.item.ItemStack.EMPTY);
        }
        if (this.getRandom().nextFloat() < 0.5f) {
            String techId = this.getTechniqueId();
            com.friday.cultivation.technique.Technique tech = techId.isEmpty() ? null : com.friday.cultivation.technique.Technique.byId(techId);
            if (tech != null && WanderingCultivatorEntity.techniqueBookItem(tech) != null) {
                corpse.moveItemIntoLoot(new net.minecraft.world.item.ItemStack(WanderingCultivatorEntity.techniqueBookItem(tech)));
            }
        }
        if (!this.spellIds.isEmpty()) {
            String pickId = this.spellIds.get(this.getRandom().nextInt(this.spellIds.size()));
            com.friday.cultivation.spell.Spell pick = com.friday.cultivation.spell.Spell.byId(pickId);
            if (pick != null && WanderingCultivatorEntity.spellBookItem(pick) != null) {
                corpse.moveItemIntoLoot(new net.minecraft.world.item.ItemStack(WanderingCultivatorEntity.spellBookItem(pick)));
            }
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            net.minecraft.world.item.ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (WanderingCultivatorEntity.shouldDropInventoryStack(stack)) {
                corpse.moveItemIntoLoot(stack.copy());
            }
            inv.setItem(i, net.minecraft.world.item.ItemStack.EMPTY);
        }
        inv.setChanged();
    }

    private static boolean shouldDropInventoryStack(net.minecraft.world.item.ItemStack stack) {
        if (com.friday.cultivation.util.CultivationRandomPools.isForbiddenNaturalLootStack(stack)) {
            return false;
        }
        return !stack.is(com.friday.cultivation.item.ModItems.SECT_TOKEN.get()) || com.friday.cultivation.item.SectTokenItem.isTemporaryLinked(stack);
    }

    /** 生成时初始化（照搬原模组 finalizeSpawn 完整版） */
    @Override
    public net.minecraft.world.entity.SpawnGroupData finalizeSpawn(net.minecraft.world.level.ServerLevelAccessor level, net.minecraft.world.DifficultyInstance difficulty, net.minecraft.world.entity.MobSpawnType reason, net.minecraft.world.entity.SpawnGroupData spawnData, net.minecraft.nbt.CompoundTag dataTag) {
        this.setPersistenceRequired();        boolean difu = level.getLevel().dimension() == com.friday.cultivation.registry.ModDimensions.DIFU || dataTag != null && dataTag.getBoolean("forcedDifuReaper");
        if (difu) {
            this.setDifuReaper(true);
        }
        com.friday.cultivation.realm.Realm realm = difu ? com.friday.cultivation.realm.Realm.values()[1 + this.getRandom().nextInt(com.friday.cultivation.realm.Realm.TRIBULATION_TRANSCENDENCE.ordinal())] : (dataTag != null && dataTag.contains("forcedRealmId") ? (com.friday.cultivation.realm.Realm.byId(dataTag.getString("forcedRealmId")) != null ? com.friday.cultivation.realm.Realm.byId(dataTag.getString("forcedRealmId")) : com.friday.cultivation.entity.npc.CultivatorRealmRoller.roll(this.getRandom())) : com.friday.cultivation.entity.npc.CultivatorRealmRoller.roll(this.getRandom()));
        int looseImmortalTribulations = 0;
        if (realm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL) {
            looseImmortalTribulations = dataTag != null && dataTag.contains("forcedLooseImmortalTribulations") ? dataTag.getInt("forcedLooseImmortalTribulations") : 1 + this.getRandom().nextInt(9);
            looseImmortalTribulations = Math.max(1, Math.min(9, looseImmortalTribulations));
        }
        com.friday.cultivation.realm.SubStage sub = com.friday.cultivation.realm.SubStage.values()[this.getRandom().nextInt(com.friday.cultivation.realm.SubStage.values().length)];
        this.realm = realm;
        this.subStage = sub;
        this.looseImmortalTribulations = looseImmortalTribulations;
        int rolledGender = this.getRandom().nextBoolean() ? 1 : 2;
        this.gender = rolledGender;
        this.skinVariant = WanderingCultivatorEntity.randomSkinVariantForGender(rolledGender, this.getRandom());
        this.repairSkinVariantGenderMismatch();
        this.surnameIndex = com.friday.cultivation.entity.npc.CultivatorNames.randomSurnameIdx(this.getRandom());
        this.givenNameIndex = com.friday.cultivation.entity.npc.CultivatorNames.randomGivenIdx(this.getRandom());
        this.soulReaperTokenTradeAvailable = difu;
        this.maxQi = realm.maxQi(sub);
        if (realm == com.friday.cultivation.realm.Realm.LOOSE_IMMORTAL && looseImmortalTribulations > 0) {
            this.maxQi += com.friday.cultivation.LooseImmortalBonusHelper.maxQiBonusForLevel(looseImmortalTribulations);
        }
        this.currentQi = this.maxQi;
        this.entityData.set(DATA_MAX_QI, (int) Math.min(Integer.MAX_VALUE, this.maxQi));
        this.entityData.set(DATA_CURRENT_QI, (int) Math.min(Integer.MAX_VALUE, this.currentQi));
        com.friday.cultivation.identity.Identity identity = this.rollIdentity();
        this.identity = identity;
        com.friday.cultivation.spirit.SpiritRoot root = this.rollSpiritRoot(realm);
        this.setSpiritRoot(root);
        com.friday.cultivation.physique.Physique physique = this.rollPhysique();
        this.setPhysique(physique);
        if (root == com.friday.cultivation.spirit.SpiritRoot.HEAVENLY_HIDDEN) {
            this.currentQi = this.maxQi = Math.max(1L, Math.round((double) this.maxQi * 1.5));
            this.entityData.set(DATA_MAX_QI, (int) Math.min(Integer.MAX_VALUE, this.maxQi));
            this.entityData.set(DATA_CURRENT_QI, (int) Math.min(Integer.MAX_VALUE, this.currentQi));
        }
        if (realm != com.friday.cultivation.realm.Realm.MORTAL) {
            this.assignTechnique(realm);
            this.assignSpells(realm);
        }
        this.normalizeSpellIdsForRealm(this.getRealm());
        if (difu) {
            this.ensureDifuReaperSpells();
        }
        this.syncSpellIdsToData();
        if (this.canFly()) {
            this.setNoGravity(true);
        }
        this.generateFavoriteItems();
        double atk = WanderingCultivatorEntity.scaleAttackByRealm(realm);
        this.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE).setBaseValue(atk);
        this.maybeEquipSword(realm);
        if (difu) {
            this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.SOUL_HOOK.get()));
            this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.0f);
        }
        if (realm == com.friday.cultivation.realm.Realm.MORTAL) {
            this.stockMortalItems();
        } else {
            this.stockSpiritStones(realm);
            this.stockPillsForRealm(realm);
            this.stockGoldenCoreMaterials(realm);
        }
        this.stockTravelAndTreasureItems(realm);
        this.removeForbiddenNaturalLootFromInventory();
        if (difu && this.getRandom().nextFloat() < 0.35f) {
            this.getInventory().addItem(new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.SOUL_REAPER_TOKEN.get()));
        }
        this.assignZhenyuanForRealm(realm, sub);
        this.assignCraftingRanks(realm);
        this.updateNpcSwordFlight();
        if (this.canFly()) {
            this.setNoGravity(true);
            this.setDeltaMovement(0.0, 0.5, 0.0);
        } else if (this.isNpcSwordFlightActive() && !this.isTradingFreeze() && this.getTarget() == null) {
            this.updateIdleFlight();
        }
        this.suspendedFreezeTargetUuid = null;
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    // ── 生成辅助：配剑 / 库存 / 石头（照搬原模组）──

    private void maybeEquipSword(com.friday.cultivation.realm.Realm realm) {
        if (!this.knowsSwordSpell()) {
            return;
        }
        if (this.getRandom().nextDouble() >= 0.5) {
            return;
        }
        this.equipSwordForRealm(realm);
    }

    private void equipSwordForRealm(com.friday.cultivation.realm.Realm realm) {
        net.minecraft.world.item.Item sword = this.swordForRealm(realm);
        this.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(sword));
        this.setDropChance(net.minecraft.world.entity.EquipmentSlot.MAINHAND, 0.05f);
    }

    private boolean knowsSwordSpell() {
        for (String id : this.spellIds) {
            if (!SWORD_SPELL_IDS.contains(id)) continue;
            return true;
        }
        return false;
    }

    private net.minecraft.world.item.Item swordForRealm(com.friday.cultivation.realm.Realm realm) {
        return com.friday.cultivation.util.CultivationRandomPools.randomSwordForRealm(realm, this.getRandom()).orElse(net.minecraft.world.item.Items.IRON_SWORD);
    }

    private void stockPillsForRealm(com.friday.cultivation.realm.Realm realm) {
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        com.friday.cultivation.alchemy.PillTier[] tiers = com.friday.cultivation.util.CultivationRandomPools.pillTiersForRealm(realm);
        boolean advancedRealm = realm.ordinal() >= com.friday.cultivation.realm.Realm.GOLDEN_CORE.ordinal();
        for (com.friday.cultivation.alchemy.PillTier tier : tiers) {
            java.util.List<net.minecraft.world.item.Item> pillPool = com.friday.cultivation.util.CultivationRandomPools.pillsForTier(tier);
            int rolls = realm.ordinal() >= com.friday.cultivation.realm.Realm.NASCENT_SOUL.ordinal() ? 2 : 1;
            int maxCount = advancedRealm ? 3 : 2;
            for (int i = 0; i < rolls; ++i) {
                this.maybeStockRandomItem(inv, pillPool, 1.0f, 1, maxCount);
            }
            this.maybeStockItem(inv, this.cultivationPillForTier(tier), advancedRealm ? 0.55f : 0.4f, 1, tier.ordinal() >= com.friday.cultivation.alchemy.PillTier.SUPREME.ordinal() ? 1 : 2);
        }
    }

    @javax.annotation.Nullable
    private net.minecraft.world.item.Item cultivationPillForTier(com.friday.cultivation.alchemy.PillTier tier) {
        return switch (tier) {
            case LOW -> com.friday.cultivation.item.ModItems.PILL_CULTIVATION_LOW.get();
            case MID -> com.friday.cultivation.item.ModItems.PILL_CULTIVATION_MID.get();
            case HIGH -> com.friday.cultivation.item.ModItems.PILL_CULTIVATION_HIGH.get();
            case SUPREME -> com.friday.cultivation.item.ModItems.PILL_CULTIVATION_SUPREME.get();
            case IMMORTAL -> com.friday.cultivation.item.ModItems.PILL_CULTIVATION_IMMORTAL.get();
        };
    }

    private void stockGoldenCoreMaterials(com.friday.cultivation.realm.Realm realm) {
        if (realm.ordinal() < com.friday.cultivation.realm.Realm.FOUNDATION_BUILDING.ordinal()) {
            return;
        }
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        float baseChance = this.goldenCoreMaterialBaseChance(realm);
        this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.JIEDAN_PILL.get(), WanderingCultivatorEntity.jiedanPillStockChanceForRealm(realm), 2, 5);
        this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.BLOOD_JIEDAN_PILL.get(), Math.min(0.34f, baseChance * 1.45f), 1, 2);
        this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.ALL_CREATURES_TRUE_BLOOD.get(), Math.min(0.3f, baseChance * 0.9f), 1, 1);
        this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.EARTH_EVIL_QI.get(), Math.min(0.32f, baseChance * 0.95f), 1, 1);
        this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.BLOOD_TRANSFORMATION_TALISMAN.get(), Math.min(0.28f, baseChance * 0.75f), 1, 1);
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.GOLDEN_CORE.ordinal()) {
            this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.HEAVEN_CLEAR_QI.get(), Math.min(0.28f, baseChance * 0.72f), 1, 1);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.NASCENT_SOUL.ordinal()) {
            this.maybeStockGoldenCoreItem(inv, com.friday.cultivation.item.ModItems.NINGZHEN_CREATION_FRUIT.get(), Math.min(0.24f, baseChance * 0.58f), 1, 1);
        }
    }

    private float goldenCoreMaterialBaseChance(com.friday.cultivation.realm.Realm realm) {
        return WanderingCultivatorEntity.goldenCoreMaterialBaseChanceStatic(realm);
    }

    static float jiedanPillStockChanceForRealm(com.friday.cultivation.realm.Realm realm) {
        return Math.min(0.78f, WanderingCultivatorEntity.goldenCoreMaterialBaseChanceStatic(realm) * 3.2f);
    }

    private static float goldenCoreMaterialBaseChanceStatic(com.friday.cultivation.realm.Realm realm) {
        return switch (realm) {
            case MORTAL, QI_REFINING -> 0.0f;
            case FOUNDATION_BUILDING -> 0.14f;
            case GOLDEN_CORE -> 0.18f;
            case NASCENT_SOUL -> 0.22f;
            case SOUL_FORMATION -> 0.24f;
            case VOID_REFINING, BODY_INTEGRATION -> 0.27f;
            case MAHAYANA, TRIBULATION_TRANSCENDENCE -> 0.3f;
            case TRUE_IMMORTAL, LOOSE_IMMORTAL -> 0.33f;
        };
    }

    private void maybeStockGoldenCoreItem(net.minecraft.world.SimpleContainer inv, net.minecraft.world.item.Item item, float chance, int minCount, int maxCount) {
        if (com.friday.cultivation.util.CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return;
        }
        if (chance <= 0.0f || this.getRandom().nextFloat() >= chance) {
            return;
        }
        int count = minCount + this.getRandom().nextInt(Math.max(1, maxCount - minCount + 1));
        inv.addItem(new net.minecraft.world.item.ItemStack(item, count));
    }

    private com.friday.cultivation.alchemy.PillTier[] pillTiersForRealm(com.friday.cultivation.realm.Realm realm) {
        return com.friday.cultivation.util.CultivationRandomPools.pillTiersForRealm(realm);
    }

    private void stockSpiritStones(com.friday.cultivation.realm.Realm realm) {
        net.minecraft.world.item.Item stoneItem = this.stoneItemForRealm(realm);
        int count = 15 + this.getRandom().nextInt(26);
        this.getInventory().addItem(new net.minecraft.world.item.ItemStack(stoneItem, count));
    }

    private void stockMortalItems() {
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        inv.addItem(new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.CUSHION.get(), 1 + this.getRandom().nextInt(3)));
        inv.addItem(new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.DIVINATION_COMPASS.get(), 1));
        inv.addItem(new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.LOW_SPIRIT_STONE.get(), 5 + this.getRandom().nextInt(11)));
        inv.addItem(new net.minecraft.world.item.ItemStack(com.friday.cultivation.item.ModItems.TECHNIQUE_BOOK_FRAGMENT.get(), 1 + this.getRandom().nextInt(2)));
        java.util.List<net.minecraft.world.item.Item> lowSpellBooks = com.friday.cultivation.util.CultivationRandomPools.spellBookItemsForTier(com.friday.cultivation.ItemTier.LOW);
        net.minecraft.world.item.Item book = lowSpellBooks.isEmpty() ? com.friday.cultivation.item.ModItems.SPELL_BOOK_FIREBALL.get() : lowSpellBooks.get(this.getRandom().nextInt(lowSpellBooks.size()));
        inv.addItem(new net.minecraft.world.item.ItemStack(book, 1 + this.getRandom().nextInt(2)));
    }

    private void stockTravelAndTreasureItems(com.friday.cultivation.realm.Realm realm) {
        net.minecraft.world.SimpleContainer inv = this.getInventory();
        this.maybeStockItem(inv, net.minecraft.world.item.Items.GOLDEN_PICKAXE, 0.62f, 2, 5);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.COOKED_BEEF, 0.3f, 1, 3);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.TORCH, 0.36f, 4, 12);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.PAPER, 0.32f, 2, 6);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.BOOK, 0.2f, 1, 2);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.ELYTRA, 0.22f, 4, 12);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.SPRUCE_CHEST_BOAT, 0.26f, 1, 4);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.ACACIA_BOAT, 0.16f, 1, 4);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.DARK_OAK_BOAT, 0.1f, 1, 3);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.BIRCH_BOAT, 0.18f, 2, 8);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.REDSTONE, 0.15f, 3, 10);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.JUNGLE_BOAT, 0.08f, 1, 3);
        this.maybeStockItem(inv, net.minecraft.world.item.Items.ENDER_PEARL, 0.035f, 1, 1);
        this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.INK.get(), 0.32f, 1, 4);
        this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.TALISMAN_PAPER.get(), 0.4f, 2, 7);
        this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.HERB.get(), 0.42f, 2, 6);
        this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.FORMATION_COMPASS.get(), 0.1f, 1, 1);
        this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.FORMATION_INSCRIPTION_KNIFE.get(), 0.08f, 1, 1);
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.QI_REFINING.ordinal()) {
            this.maybeStockRandomBook(inv, com.friday.cultivation.util.CultivationRandomPools.techniqueBookItemsForTier(com.friday.cultivation.util.CultivationRandomPools.techniqueTierForRealm(realm)), 0.22f);
            this.maybeStockRandomBook(inv, com.friday.cultivation.util.CultivationRandomPools.spellBookItemsForTier(com.friday.cultivation.util.CultivationRandomPools.spellTierForRealm(realm)), 0.26f);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.ZHUJI_DAN.get(), 0.62f, 2, 5);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.BLOOD_SPIRIT_PILL.get(), 0.14f, 1, 2);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.YOUTH_PILL.get(), 0.06f, 1, 1);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.SEX_CHANGE_PILL.get(), 0.05f, 1, 1);
        }
        if (realm.ordinal() >= com.friday.cultivation.realm.Realm.FOUNDATION_BUILDING.ordinal()) {
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.FOUNDATION_SECRET.get(), 0.13f, 1, 1);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.DAO_FOUNDATION_FRUIT.get(), 0.07f, 1, 1);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.YOUTH_PILL.get(), 0.1f, 1, 1);
            this.maybeStockItem(inv, com.friday.cultivation.item.ModItems.SEX_CHANGE_PILL.get(), 0.08f, 1, 1);
        }
    }

    private void maybeStockItem(net.minecraft.world.SimpleContainer inv, @javax.annotation.Nullable net.minecraft.world.item.Item item, float chance, int minCount, int maxCount) {
        if (com.friday.cultivation.util.CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return;
        }
        if (item == null || chance <= 0.0f || this.getRandom().nextFloat() >= chance) {
            return;
        }
        int count = minCount + this.getRandom().nextInt(Math.max(1, maxCount - minCount + 1));
        inv.addItem(new net.minecraft.world.item.ItemStack(item, count));
    }

    private void maybeStockRandomItem(net.minecraft.world.SimpleContainer inv, java.util.List<net.minecraft.world.item.Item> pool, float chance, int minCount, int maxCount) {
        if (pool.isEmpty() || chance <= 0.0f || this.getRandom().nextFloat() >= chance) {
            return;
        }
        this.maybeStockItem(inv, pool.get(this.getRandom().nextInt(pool.size())), 1.0f, minCount, maxCount);
    }

    private void maybeStockRandomBook(net.minecraft.world.SimpleContainer inv, java.util.List<net.minecraft.world.item.Item> pool, float chance) {
        if (pool.isEmpty() || this.getRandom().nextFloat() >= chance) {
            return;
        }
        net.minecraft.world.item.Item item = pool.get(this.getRandom().nextInt(pool.size()));
        if (!com.friday.cultivation.util.CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            inv.addItem(new net.minecraft.world.item.ItemStack(item));
        }
    }

    private net.minecraft.world.item.Item stoneItemForRealm(com.friday.cultivation.realm.Realm realm) {
        return switch (realm) {
            case MORTAL, QI_REFINING, FOUNDATION_BUILDING -> com.friday.cultivation.item.ModItems.LOW_SPIRIT_STONE.get();
            case GOLDEN_CORE, NASCENT_SOUL -> com.friday.cultivation.item.ModItems.MID_SPIRIT_STONE.get();
            case SOUL_FORMATION, VOID_REFINING -> com.friday.cultivation.item.ModItems.HIGH_SPIRIT_STONE.get();
            default -> com.friday.cultivation.item.ModItems.SUPREME_SPIRIT_STONE.get();
        };
    }

    private void ensureDifuReaperSpells() {
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.SOUL_HOOK.id())) {
            this.spellIds.add(com.friday.cultivation.spell.Spell.SOUL_HOOK.id());
        }
        if (!this.spellIds.contains(com.friday.cultivation.spell.Spell.YIN_YANG_EYE.id())) {
            this.spellIds.add(com.friday.cultivation.spell.Spell.YIN_YANG_EYE.id());
        }
    }

    // ── 身份 / 性别 / 境界 getter（照搬原模组）──

    public com.friday.cultivation.identity.Identity getIdentity() {
        return this.identity;
    }

    public void setIdentity(com.friday.cultivation.identity.Identity identity) {
        this.identity = identity == null ? com.friday.cultivation.identity.Identity.LONE_CULTIVATOR : identity;
    }

    public com.friday.cultivation.realm.SubStage getSubStage() {
        return this.subStage;
    }

    public void setSubStage(com.friday.cultivation.realm.SubStage sub) {
        this.subStage = sub == null ? com.friday.cultivation.realm.SubStage.EARLY : sub;
    }

    public int getLooseImmortalTribulations() {
        if (this.getRealm() != Realm.LOOSE_IMMORTAL) {
            return 0;
        }
        return Math.max(1, Math.min(9, this.looseImmortalTribulations));
    }

    public void setLooseImmortalTribulations(int v) {
        this.looseImmortalTribulations = v;
    }

    public int getSurnameIdx() {
        return this.getSurnameIndex();
    }

    public int getGivenIdx() {
        return this.getGivenNameIndex();
    }

    public int getGender() {
        return WanderingCultivatorEntity.safeGender(this.gender);
    }

    public void setGender(int gender) {
        this.gender = WanderingCultivatorEntity.safeGender(gender);
    }

    public com.friday.cultivation.alchemy.AlchemyRank getAlchemyRank() {
        com.friday.cultivation.alchemy.AlchemyRank[] ranks = com.friday.cultivation.alchemy.AlchemyRank.values();
        int ord = Math.max(0, Math.min(ranks.length - 1, this.alchemyRank.ordinal()));
        return ranks[ord];
    }

    public void setAlchemyRank(com.friday.cultivation.alchemy.AlchemyRank rank) {
        this.alchemyRank = rank == null ? com.friday.cultivation.alchemy.AlchemyRank.values()[0] : rank;
    }

    public com.friday.cultivation.refining.RefiningRank getRefiningRank() {
        com.friday.cultivation.refining.RefiningRank[] ranks = com.friday.cultivation.refining.RefiningRank.values();
        int ord = Math.max(0, Math.min(ranks.length - 1, this.refiningRank.ordinal()));
        return ranks[ord];
    }

    public void setRefiningRank(com.friday.cultivation.refining.RefiningRank rank) {
        this.refiningRank = rank == null ? com.friday.cultivation.refining.RefiningRank.values()[0] : rank;
    }

    /** 皮肤变体（照搬原模组 normalizeSkinVariantForGender 语义） */
    public int getSkinVariant() {
        return WanderingCultivatorEntity.normalizeSkinVariantForGender(this.skinVariant, this.getGender());
    }

    // ── 皮肤 / 性别静态方法（照搬原模组）──

    public static boolean isFemaleSkinVariant(int variant) {
        return WanderingCultivatorEntity.containsSkinVariant(FEMALE_SKIN_VARIANTS, WanderingCultivatorEntity.wrapSkinVariant(variant));
    }

    public static boolean isMaleSkinVariant(int variant) {
        return WanderingCultivatorEntity.containsSkinVariant(MALE_SKIN_VARIANTS, WanderingCultivatorEntity.wrapSkinVariant(variant));
    }

    public static boolean skinVariantMatchesGender(int variant, int gender) {
        int wrapped = WanderingCultivatorEntity.wrapSkinVariant(variant);
        return WanderingCultivatorEntity.safeGender(gender) == 2 ? WanderingCultivatorEntity.isFemaleSkinVariant(wrapped) : WanderingCultivatorEntity.isMaleSkinVariant(wrapped);
    }

    public static int normalizeSkinVariantForGender(int variant, int gender) {
        int wrapped = WanderingCultivatorEntity.wrapSkinVariant(variant);
        if (WanderingCultivatorEntity.skinVariantMatchesGender(wrapped, gender)) {
            return wrapped;
        }
        int[] pool = WanderingCultivatorEntity.skinPoolForGender(gender);
        return pool[Math.floorMod(wrapped, pool.length)];
    }

    public static int randomSkinVariantForGender(int gender, net.minecraft.util.RandomSource random) {
        int[] pool = WanderingCultivatorEntity.skinPoolForGender(gender);
        return pool[random.nextInt(pool.length)];
    }

    private static int safeGender(int gender) {
        return gender == 2 ? 2 : 1;
    }

    private static int wrapSkinVariant(int variant) {
        return Math.floorMod(variant, Math.max(1, 48));
    }

    private static int[] skinPoolForGender(int gender) {
        return WanderingCultivatorEntity.safeGender(gender) == 2 ? FEMALE_SKIN_VARIANTS : MALE_SKIN_VARIANTS;
    }

    private static boolean containsSkinVariant(int[] pool, int variant) {
        for (int candidate : pool) {
            if (candidate != variant) continue;
            return true;
        }
        return false;
    }

    private void repairSkinVariantGenderMismatch() {
        int rawVariant = this.skinVariant;
        int gender = this.getGender();
        int normalizedVariant = WanderingCultivatorEntity.normalizeSkinVariantForGender(rawVariant, gender);
        if (rawVariant != normalizedVariant || !WanderingCultivatorEntity.skinVariantMatchesGender(normalizedVariant, gender)) {
            this.skinVariant = normalizedVariant;
        }
    }

    private static void validateSkinGenderManifest() {
        boolean[] seen = new boolean[48];
        for (int variant : MALE_SKIN_VARIANTS) {
            WanderingCultivatorEntity.markSkinVariant(seen, variant, "male");
        }
        for (int variant : FEMALE_SKIN_VARIANTS) {
            WanderingCultivatorEntity.markSkinVariant(seen, variant, "female");
        }
        for (int i = 0; i < seen.length; ++i) {
            if (seen[i]) continue;
            throw new IllegalStateException("Missing wandering cultivator skin variant in gender manifest: " + i);
        }
    }

    private static void markSkinVariant(boolean[] seen, int variant, String gender) {
        if (variant < 0 || variant >= 48) {
            throw new IllegalStateException("Invalid " + gender + " wandering cultivator skin variant: " + variant);
        }
        if (seen[variant]) {
            throw new IllegalStateException("Duplicate wandering cultivator skin variant in gender manifest: " + variant);
        }
        seen[variant] = true;
    }
}
