/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Vec3i
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.core.registries.Registries
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvent
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.tags.BlockTags
 *  net.minecraft.tags.TagKey
 *  net.minecraft.util.Mth
 *  net.minecraft.util.RandomSource
 *  net.minecraft.world.DifficultyInstance
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.effect.MobEffectCategory
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.effect.MobEffects
 *  net.minecraft.world.entity.AgeableMob
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.EquipmentSlot
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.Mob
 *  net.minecraft.world.entity.MobSpawnType
 *  net.minecraft.world.entity.NeutralMob
 *  net.minecraft.world.entity.PathfinderMob
 *  net.minecraft.world.entity.SpawnGroupData
 *  net.minecraft.world.entity.ai.attributes.AttributeInstance
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier
 *  net.minecraft.world.entity.ai.attributes.AttributeModifier$Operation
 *  net.minecraft.world.entity.ai.attributes.AttributeSupplier$Builder
 *  net.minecraft.world.entity.ai.attributes.Attributes
 *  net.minecraft.world.entity.ai.goal.FloatGoal
 *  net.minecraft.world.entity.ai.goal.Goal
 *  net.minecraft.world.entity.ai.goal.LookAtPlayerGoal
 *  net.minecraft.world.entity.ai.goal.MeleeAttackGoal
 *  net.minecraft.world.entity.ai.goal.OpenDoorGoal
 *  net.minecraft.world.entity.ai.goal.RandomLookAroundGoal
 *  net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal
 *  net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal
 *  net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal
 *  net.minecraft.world.entity.ai.navigation.GroundPathNavigation
 *  net.minecraft.world.entity.ai.navigation.PathNavigation
 *  net.minecraft.world.entity.monster.Monster
 *  net.minecraft.world.entity.npc.AbstractVillager
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Items
 *  net.minecraft.world.item.SwordItem
 *  net.minecraft.world.item.trading.MerchantOffer
 *  net.minecraft.world.item.trading.MerchantOffers
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.LevelAccessor
 *  net.minecraft.world.level.ServerLevelAccessor
 *  net.minecraft.world.level.block.BedBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.BedPart
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.level.levelgen.Heightmap$Types
 *  net.minecraft.world.level.levelgen.structure.Structure
 *  net.minecraft.world.phys.AABB
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.NetworkHooks
 *  net.minecraftforge.network.PacketDistributor
 *  net.minecraftforge.registries.ForgeRegistries
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.block.CushionBlock;
import com.friday.cultivation.cultivation.BodyDefenseHelper;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.alchemy.PillEffectSpecs;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.sect.SectSavedData;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.SeatEntity;
import com.friday.cultivation.entity.npc.CorpseEntity;
import com.friday.cultivation.entity.npc.CultivatorNames;
import com.friday.cultivation.entity.npc.CultivatorRealmRoller;
import com.friday.cultivation.entity.npc.CultivatorTrades;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.SundryPricing;
import com.friday.cultivation.entity.npc.ai.CultivatorFlightCombatGoal;
import com.friday.cultivation.entity.npc.ai.CultivatorRangedKitingGoal;
import com.friday.cultivation.entity.npc.ai.CultivatorSpellAttackGoal;
import com.friday.cultivation.event.NpcPassiveSpellHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.event.SoulHookHandler;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.event.TimeStasisHandler;
import com.friday.cultivation.event.VoidStepHandler;
import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import com.friday.cultivation.item.ClearMindPillItem;
import com.friday.cultivation.item.PillItem;
import com.friday.cultivation.item.RejuvenationPillItem;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.item.SpiritStoneItem;
import com.friday.cultivation.item.weapon.TieredWeapon;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.SyncCultivatorInventoryPacket;
import com.friday.cultivation.network.SyncCultivatorOffersPacket;
import com.friday.cultivation.registry.ModDimensions;
import com.friday.cultivation.registry.ModItems;
import com.friday.cultivation.util.CultivationRandomPools;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;
import net.minecraft.ChatFormatting;
import com.friday.cultivation.worldgen.GreatEmperorTracker;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.NeutralMob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WanderingCultivatorEntity
extends AbstractVillager {
    public static final int GENDER_MALE = 1;
    public static final int GENDER_FEMALE = 2;
    static final float ZHUJI_DAN_STOCK_CHANCE = 0.62f;
    static final int ZHUJI_DAN_STOCK_MIN_COUNT = 2;
    static final int ZHUJI_DAN_STOCK_MAX_COUNT = 5;
    static final float JIEDAN_PILL_STOCK_CHANCE_CAP = 0.78f;
    static final float JIEDAN_PILL_STOCK_CHANCE_MULTIPLIER = 3.2f;
    static final int JIEDAN_PILL_STOCK_MIN_COUNT = 2;
    static final int JIEDAN_PILL_STOCK_MAX_COUNT = 5;
    private static final int[] MALE_SKIN_VARIANTS = new int[]{1, 2, 4, 7, 10, 12, 15, 17, 18, 19, 20, 21, 23, 25, 26, 28, 34, 35, 36, 40, 42, 43, 44, 47};
    private static final int[] FEMALE_SKIN_VARIANTS = new int[]{0, 3, 5, 6, 8, 9, 11, 13, 14, 16, 22, 24, 27, 29, 30, 31, 32, 33, 37, 38, 39, 41, 45, 46};
    public static final int MALE_SKIN_VARIANT_COUNT = MALE_SKIN_VARIANTS.length;
    public static final int FEMALE_SKIN_VARIANT_START = 24;
    public static final int FEMALE_SKIN_VARIANT_COUNT = FEMALE_SKIN_VARIANTS.length;
    public static final int SKIN_VARIANT_COUNT = 48;
    private static final double GROUND_WALK_MOVEMENT_SPEED = 0.23;
    private static final double VANILLA_BASE_JUMP_VELOCITY = 0.42;
    private static final UUID ZHENYUAN_GROUND_SPEED_MODIFIER_ID;
    private static final TagKey<Structure> PREFERRED_SPAWN_STRUCTURES;
    private static final int STRUCTURE_SPAWN_BONUS_RADIUS = 96;
    private static final double OVERWORLD_NATURAL_SPAWN_CHANCE = 5.0E-5;
    private static final double OVERWORLD_STRUCTURE_SPAWN_CHANCE = 2.0E-4;
    private static final int SWORD_FLIGHT_IDLE_GRACE_TICKS = 160;
    private static final double NPC_SWORD_EQUIP_CHANCE = 0.5;
    private static final int NPC_VOID_STEP_COOLDOWN_TICKS = 24;
    private static final int NPC_VOID_ESCAPE_COOLDOWN_TICKS = 360;
    private static final int NPC_VOID_ESCAPE_ACTIVE_TICKS = 160;
    private static final double NPC_VOID_STEP_TRIGGER_RANGE = 11.0;
    private static final double NPC_VOID_STEP_REENGAGE_RANGE = 18.0;
    private static final double NPC_VOID_STEP_NEAR_SPEED = 0.46;
    private static final double NPC_VOID_STEP_FAR_SPEED = 0.62;
    private static final double NPC_VOID_STEP_UPWARD_SPEED = 0.26;
    private static final double SECT_RECEPTION_GUARD_RETURN_DISTANCE_SQ = 9.0;
    private static final double SECT_RECEPTION_GUARD_TELEPORT_DISTANCE_SQ = 784.0;
    private static final double SECT_BED_SLEEP_REACH_SQ = 6.25;
    private static final double SECT_BED_SLEEP_Y_OFFSET = 0.6875;
    private static final double SECT_BED_SLEEP_SNAP_EPSILON_SQ = 0.01;
    private static final double SECT_BED_SLEEP_THREAT_DISTANCE_SQ = 144.0;
    private static final EntityDataAccessor<Integer> DATA_REALM_ORD;
    private static final EntityDataAccessor<Integer> DATA_SUB_STAGE_ORD;
    private static final EntityDataAccessor<Integer> DATA_LOOSE_IMMORTAL_TRIBULATIONS;
    private static final EntityDataAccessor<String> DATA_TECHNIQUE_ID;
    private static final EntityDataAccessor<Integer> DATA_SKIN_VARIANT;
    private static final EntityDataAccessor<Boolean> DATA_DIFU_REAPER;
    private static final EntityDataAccessor<Boolean> DATA_NPC_SOUL_STATE;
    private static final EntityDataAccessor<Integer> DATA_SURNAME_IDX;
    private static final EntityDataAccessor<Integer> DATA_GIVEN_IDX;
    private static final EntityDataAccessor<Integer> DATA_GENDER;
    private static final EntityDataAccessor<Integer> DATA_CURRENT_QI;
    private static final EntityDataAccessor<Integer> DATA_MAX_QI;
    private static final EntityDataAccessor<String> DATA_SPELL_IDS_CSV;
    private static final EntityDataAccessor<String> DATA_FAVORITE_ITEMS_CSV;
    private static final EntityDataAccessor<String> DATA_SPIRIT_ROOT_ID;
    private static final EntityDataAccessor<String> DATA_PHYSIQUE_ID;
    private static final EntityDataAccessor<String> DATA_IDENTITY_ID;
    private static final EntityDataAccessor<String> DATA_ZHENYUAN_CSV;
    private static final EntityDataAccessor<Integer> DATA_ALCHEMY_RANK_ORD;
    private static final EntityDataAccessor<Integer> DATA_REFINING_RANK_ORD;
    private static final EntityDataAccessor<Boolean> DATA_NPC_SWORD_FLIGHT_ACTIVE;
    private static final EntityDataAccessor<String> DATA_SECT_ID;
    private static final EntityDataAccessor<String> DATA_SECT_NAME;
    private static final EntityDataAccessor<String> DATA_SECT_ROLE;
    private final List<String> spellIds = new ArrayList<String>();
    private int npcSwordFlightIdleTicks = 0;
    private int npcVoidStepCooldownTicks = 0;
    private int npcVoidEscapeCooldownTicks = 0;
    private int npcVoidEscapeActiveTicks = 0;
    private boolean npcVoidEscapePreviousNoGravity = false;
    private boolean npcVoidEscapePreviousNoPhysics = false;
    private int soulDriftTicks = 0;
    private double soulDriftX = 0.0;
    private double soulDriftZ = 0.0;
    private int npcSoulTicks = 0;
    private static final Set<WanderingCultivatorEntity> LOADED_NPC_SOULS;
    private final SimpleContainer extInventory = new SimpleContainer(27);
    private boolean soulReaperTokenTradeAvailable = false;
    private long currentQi = 0L;
    private long maxQi = 0L;
    @Nullable
    private BlockPos sectHomePos;
    @Nullable
    private BlockPos sectBedPos;
    @Nullable
    private BlockPos sectCushionPos;
    @Nullable
    private BlockPos sectCorePos;
    @Nullable
    private BlockPos sectReceptionGuardPos;
    private boolean sectTemporaryTokensSeeded = false;
    private int sectRoutineCooldown = 0;
    private int sectTaskCooldown = 0;
    private int combatStartTick = -1;
    private UUID suspendedFreezeTargetUuid = null;
    private boolean wasTradingFrozen = false;
    private static final Set<String> SWORD_SPELL_IDS;

    public static Collection<WanderingCultivatorEntity> loadedNpcSouls() {
        return LOADED_NPC_SOULS;
    }

    public static void trackNpcSoulIfApplicable(WanderingCultivatorEntity npc) {
        if (npc != null && !npc.level().isClientSide() && npc.isNpcSoulState()) {
            LOADED_NPC_SOULS.add(npc);
        }
    }

    public static void untrackNpcSoul(WanderingCultivatorEntity npc) {
        if (npc != null) {
            LOADED_NPC_SOULS.remove((Object)npc);
        }
    }

    public static void clearNpcSoulRegistry() {
        LOADED_NPC_SOULS.clear();
    }

    @NotNull
    public SimpleContainer getInventory() {
        return this.extInventory;
    }

    public int getCombatTicks() {
        if (this.combatStartTick < 0) {
            return 0;
        }
        return Math.max(0, this.tickCount - this.combatStartTick);
    }

    public WanderingCultivatorEntity(EntityType<? extends WanderingCultivatorEntity> type, Level level) {
        super(type, level);
        PathNavigation pathNavigation = this.getNavigation();
        if (pathNavigation instanceof GroundPathNavigation) {
            GroundPathNavigation navigation = (GroundPathNavigation)pathNavigation;
            navigation.setCanOpenDoors(true);
            navigation.setCanPassDoors(true);
        }
        this.setPersistenceRequired();
    }

    public void setTarget(@Nullable LivingEntity target) {
        LivingEntity current;
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
            if (this.isTrading() && !(current instanceof Player)) {
                super.setTarget(null);
                return;
            }
            return;
        }
        if (target != null) {
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            if (this.isPassenger() && this.getVehicle() instanceof SeatEntity) {
                this.stopRiding();
            }
        }
        super.setTarget(target);
    }

    public boolean hurtMethod(@NotNull DamageSource source, float amount) {
        Entity entity;
        if (this.isNpcSoulState()) {
            if (!this.level().isClientSide) {
                this.clearNpcSoulAggro();
            }
            return false;
        }
        boolean hurt = super.hurt(source, amount);
        if (!this.level().isClientSide && hurt && (entity = source.getEntity()) instanceof LivingEntity) {
            LivingEntity attacker = (LivingEntity)entity;
            this.rememberCombatThreat(attacker);
        }
        return hurt;
    }

    public void rememberCombatThreat(@Nullable LivingEntity attacker) {
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

    /**
     * 大帝 NPC 死亡：释放全存档大帝名额；若为玩家亲手击杀则记录击杀者。
     */
    public void die(@NotNull DamageSource source) {
        boolean wasGreatEmperor = this.getRealm() == Realm.GREAT_EMPEROR;
        super.die(source);
        if (!this.level().isClientSide && wasGreatEmperor && this.level() instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)this.level();
            GreatEmperorTracker tracker = GreatEmperorTracker.get(serverLevel);
            tracker.releaseEmperor();
            Entity killer = source.getEntity();
            if (killer instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)killer;
                tracker.recordEmperorSlayer(killer.getUUID());
                CultivationCapability.get((Player)sp).ifPresent(data -> {
                    data.setKilledGreatEmperor(true);
                    CapabilityEvents.syncToClient(sp);
                });
            }
        }
    }

    private void clearNpcSoulAggro() {
        super.setTarget(null);
        this.combatStartTick = -1;
        this.setLastHurtByMob(null);
        this.setAggressive(false);
        this.getNavigation().stop();
        this.stopUsingItem();
        this.stopNpcSwordFlight(true);
        this.setNoGravity(false);
    }

    public boolean clearHostilityTowardPlayer(UUID playerId) {
        LivingEntity lastHurtBy;
        if (playerId == null) {
            return false;
        }
        boolean changed = false;
        LivingEntity target = this.getTarget();
        if (target != null && playerId.equals(target.getUUID())) {
            super.setTarget(null);
            changed = true;
        }
        if ((lastHurtBy = this.getLastHurtByMob()) != null && playerId.equals(lastHurtBy.getUUID())) {
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
            this.stopUsingItem();
            this.stopNpcSwordFlight(true);
            this.setNoGravity(false);
        }
        return changed;
    }

    private void clearMobsTargetingNpcSoul() {
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        for (Mob mob : sl.getEntitiesOfClass(Mob.class, this.getBoundingBox().inflate(64.0), m -> m.getTarget() == this)) {
            mob.setTarget(null);
            mob.setLastHurtByMob(null);
            if (!(mob instanceof NeutralMob)) continue;
            NeutralMob neutral = (NeutralMob)mob;
            neutral.stopBeingAngry();
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractVillager.createMobAttributes().add(Attributes.MAX_HEALTH, 20.0).add(Attributes.MOVEMENT_SPEED, 0.23).add(Attributes.FOLLOW_RANGE, 64.0).add(Attributes.ATTACK_DAMAGE, 2.0);
    }

    protected void registerGoals() {
        this.goalSelector.addGoal(0, (Goal)new FloatGoal((Mob)this));
        this.goalSelector.addGoal(1, (Goal)new CultivatorSpellAttackGoal(this));
        this.goalSelector.addGoal(2, (Goal)new CultivatorFlightCombatGoal(this));
        this.goalSelector.addGoal(3, (Goal)new CultivatorRangedKitingGoal(this));
        this.goalSelector.addGoal(4, (Goal)new MeleeAttackGoal((PathfinderMob)this, 1.0, true));
        this.goalSelector.addGoal(5, (Goal)new OpenDoorGoal((Mob)this, true));
        this.goalSelector.addGoal(6, (Goal)new WaterAvoidingRandomStrollGoal((PathfinderMob)this, 1.0));
        this.goalSelector.addGoal(7, (Goal)new LookAtPlayerGoal((Mob)this, Player.class, 8.0f));
        this.goalSelector.addGoal(8, (Goal)new RandomLookAroundGoal((Mob)this));
        this.targetSelector.addGoal(1, (Goal)new HurtByTargetGoal((PathfinderMob)this, new Class[0]));
        this.targetSelector.addGoal(2, (Goal)new NearestAttackableTargetGoal((Mob)this, Player.class, 10, true, false, e -> !this.isNpcSoulState() && this.getLastHurtByMob() != null && this.canTargetUnderYinYangRules((LivingEntity)e)));
        this.targetSelector.addGoal(3, (Goal)new NearestAttackableTargetGoal((Mob)this, Monster.class, 10, false, false, e -> !this.isNpcSoulState() && this.canTargetUnderYinYangRules((LivingEntity)e)));
    }

    private boolean canTargetUnderYinYangRules(LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (this.isNpcSoulState()) {
            return false;
        }
        if (this.isDifuReaper() && SoulStateHandler.canSoulHookTarget((Entity)target)) {
            return true;
        }
        return SoulStateHandler.canOrdinaryAffect((Entity)this, (Entity)target);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_REALM_ORD, Realm.MORTAL.ordinal());
        this.entityData.define(DATA_SUB_STAGE_ORD, SubStage.EARLY.level());
        this.entityData.define(DATA_LOOSE_IMMORTAL_TRIBULATIONS, 0);
        this.entityData.define(DATA_TECHNIQUE_ID, "");
        this.entityData.define(DATA_SKIN_VARIANT, 0);
        this.entityData.define(DATA_DIFU_REAPER, false);
        this.entityData.define(DATA_NPC_SOUL_STATE, false);
        this.entityData.define(DATA_SURNAME_IDX, 0);
        this.entityData.define(DATA_GIVEN_IDX, 0);
        this.entityData.define(DATA_GENDER, 1);
        this.entityData.define(DATA_CURRENT_QI, 0);
        this.entityData.define(DATA_MAX_QI, 0);
        this.entityData.define(DATA_SPELL_IDS_CSV, "");
        this.entityData.define(DATA_FAVORITE_ITEMS_CSV, "");
        this.entityData.define(DATA_SPIRIT_ROOT_ID, SpiritRoot.NONE.id());
        this.entityData.define(DATA_PHYSIQUE_ID, Physique.MORTAL_BODY.id());
        this.entityData.define(DATA_IDENTITY_ID, Identity.LONE_CULTIVATOR.id());
        this.entityData.define(DATA_ZHENYUAN_CSV, "0,0,0,0,0");
        this.entityData.define(DATA_ALCHEMY_RANK_ORD, 0);
        this.entityData.define(DATA_REFINING_RANK_ORD, 0);
        this.entityData.define(DATA_NPC_SWORD_FLIGHT_ACTIVE, false);
        this.entityData.define(DATA_SECT_ID, "");
        this.entityData.define(DATA_SECT_NAME, "");
        this.entityData.define(DATA_SECT_ROLE, SectRole.NONE.id());
    }

    public SpiritRoot getSpiritRoot() {
        return SpiritRoot.byId((String)this.entityData.get(DATA_SPIRIT_ROOT_ID));
    }

    public Physique getPhysique() {
        return Physique.byId((String)this.entityData.get(DATA_PHYSIQUE_ID));
    }

    public Identity getIdentity() {
        return Identity.byId((String)this.entityData.get(DATA_IDENTITY_ID));
    }

    public String getSectId() {
        return (String)this.entityData.get(DATA_SECT_ID);
    }

    public String getSectName() {
        return (String)this.entityData.get(DATA_SECT_NAME);
    }

    public SectRole getSectRole() {
        return SectRole.byId((String)this.entityData.get(DATA_SECT_ROLE));
    }

    public boolean hasSectMembership() {
        return !this.getSectId().isBlank() && this.getSectRole() != SectRole.NONE;
    }

    public void configureSectReceptionGuard(@Nullable BlockPos receptionPos) {
        this.sectReceptionGuardPos = receptionPos == null ? null : receptionPos.east();
        this.setPersistenceRequired();
    }

    public boolean isSectReceptionGuard() {
        return this.sectReceptionGuardPos != null && this.getSectRole() == SectRole.GUARD_DISCIPLE;
    }

    public Component getSectIdentityComponent() {
        if (this.hasSectMembership()) {
            return this.getSectRole().identity(this.getSectName());
        }
        return this.isDifuReaper() ? Component.translatable((String)"entity.friday_cultivation.soul_reaper") : Component.translatable((String)this.getIdentity().translationKey());
    }

    @Nullable
    public BlockPos getSectCorePos() {
        return this.sectCorePos;
    }

    public void assignSectMembership(String sectId, String sectName, SectRole role, @Nullable BlockPos homePos, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos, @Nullable BlockPos corePos, boolean addPersonalToken) {
        SectRole safeRole = role == null ? SectRole.NONE : role;
        this.entityData.set(DATA_SECT_ID, (sectId == null ? "" : sectId));
        this.entityData.set(DATA_SECT_NAME, (sectName == null ? "" : sectName));
        this.entityData.set(DATA_SECT_ROLE, safeRole.id());
        this.sectHomePos = homePos == null ? null : homePos.east();
        this.sectBedPos = bedPos == null ? null : bedPos.east();
        this.sectCushionPos = cushionPos == null ? null : cushionPos.east();
        BlockPos blockPos = this.sectCorePos = corePos == null ? null : corePos.east();
        if (safeRole != SectRole.GUARD_DISCIPLE) {
            this.sectReceptionGuardPos = null;
        }
        if (!this.level().isClientSide && addPersonalToken && this.sectCorePos != null) {
            this.addPersonalSectToken();
        }
        this.setPersistenceRequired();
        this.regenerateOffers();
        this.updateDisplayName();
    }

    public boolean ensureSectCoreLink(BlockPos corePos, boolean addTemporaryTokens) {
        if (this.level().isClientSide || corePos == null || !this.hasSectMembership()) {
            return false;
        }
        boolean changed = false;
        if (this.sectCorePos == null || !this.sectCorePos.equals((Object)corePos)) {
            this.sectCorePos = corePos.east();
            changed = true;
        }
        changed |= this.addPersonalSectToken();
        if (addTemporaryTokens) {
            changed |= this.ensureTemporarySectTokens();
        }
        if (changed) {
            this.getInventory().setChanged();
            this.setPersistenceRequired();
            this.regenerateOffers();
        }
        return changed;
    }

    private boolean addPersonalSectToken() {
        if (this.sectCorePos == null || this.getSectName().isBlank()) {
            return false;
        }
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!SectTokenItem.isLinkedToCore(stack, this.level(), this.sectCorePos) || SectTokenItem.isTemporaryLinked(stack) || !SectTokenItem.isUsableBy(stack, (Entity)this)) continue;
            return false;
        }
        ItemStack token = SectTokenItem.createLinked(this.level(), this.sectCorePos, this.getSectName(), this.getCultivatorName().getString(), false, 1);
        ItemStack leftover = inv.addItem(token);
        if (!leftover.isEmpty()) {
            this.spawnAtLocation(leftover);
        }
        inv.setChanged();
        return true;
    }

    private boolean ensureTemporarySectTokens() {
        if (this.getSectRole() != SectRole.GUARD_DISCIPLE || this.sectCorePos == null || this.getSectName().isBlank()) {
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
        ItemStack tokens = SectTokenItem.createLinked(this.level(), this.sectCorePos, this.getSectName(), "", true, count);
        ItemStack leftover = this.getInventory().addItem(tokens);
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
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!SectTokenItem.isTemporaryLinked(stack) || !SectTokenItem.isLinkedToCore(stack, this.level(), this.sectCorePos)) continue;
            return true;
        }
        return false;
    }

    public int[] getZhenyuanAttrs() {
        String csv = (String)this.entityData.get(DATA_ZHENYUAN_CSV);
        int[] arr = new int[]{0, 0, 0, 0, 0};
        if (csv == null || csv.isEmpty()) {
            return arr;
        }
        String[] parts = csv.split(",");
        for (int i = 0; i < Math.min(5, parts.length); ++i) {
            try {
                arr[i] = Integer.parseInt(parts[i].trim());
                continue;
            }
            catch (NumberFormatException numberFormatException) {
                // empty catch block
            }
        }
        return arr;
    }

    public AlchemyRank getAlchemyRank() {
        AlchemyRank[] ranks = AlchemyRank.values();
        int ord = Math.max(0, Math.min(ranks.length - 1, (Integer)this.entityData.get(DATA_ALCHEMY_RANK_ORD)));
        return ranks[ord];
    }

    public RefiningRank getRefiningRank() {
        RefiningRank[] ranks = RefiningRank.values();
        int ord = Math.max(0, Math.min(ranks.length - 1, (Integer)this.entityData.get(DATA_REFINING_RANK_ORD)));
        return ranks[ord];
    }

    private void syncSpellIdsToData() {
        String csv = String.join((CharSequence)",", this.spellIds);
        if (!((String)this.entityData.get(DATA_SPELL_IDS_CSV)).equals(csv)) {
            this.entityData.set(DATA_SPELL_IDS_CSV, csv);
        }
    }

    private void generateFavoriteItems() {
        ArrayList<Item> all = new ArrayList<Item>(SundryPricing.allAccepted());
        Collections.shuffle(all, new Random(this.random.nextLong()));
        int count = 5 + this.random.nextInt(4);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(count, all.size()); ++i) {
            if (sb.length() > 0) {
                sb.append(",");
            }
            sb.append(ForgeRegistries.ITEMS.getKey(all.get(i)).toString());
        }
        this.entityData.set(DATA_FAVORITE_ITEMS_CSV, sb.toString());
    }

    public List<Item> getFavoriteItems() {
        String csv = (String)this.entityData.get(DATA_FAVORITE_ITEMS_CSV);
        if (csv.isEmpty()) {
            return Collections.emptyList();
        }
        ArrayList<Item> result = new ArrayList<Item>();
        for (String s : csv.split(",")) {
            ResourceLocation rl = new ResourceLocation(s);
            Item it = (Item)ForgeRegistries.ITEMS.getValue(rl);
            if (it == null || it == Items.AIR) continue;
            result.add(it);
        }
        return result;
    }

    public int getSurnameIdx() {
        return (Integer)this.entityData.get(DATA_SURNAME_IDX);
    }

    public int getGivenIdx() {
        return (Integer)this.entityData.get(DATA_GIVEN_IDX);
    }

    public int getGender() {
        return WanderingCultivatorEntity.safeGender((Integer)this.entityData.get(DATA_GENDER));
    }

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

    public static int randomSkinVariantForGender(int gender, RandomSource random) {
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

    private void repairSkinVariantGenderMismatch() {
        int normalizedVariant;
        int rawVariant;
        int gender = this.getGender();
        if ((Integer)this.entityData.get(DATA_GENDER) != gender) {
            this.entityData.set(DATA_GENDER, gender);
        }
        if ((rawVariant = ((Integer)this.entityData.get(DATA_SKIN_VARIANT)).intValue()) != (normalizedVariant = WanderingCultivatorEntity.normalizeSkinVariantForGender(rawVariant, gender)) || !WanderingCultivatorEntity.skinVariantMatchesGender(normalizedVariant, gender)) {
            this.entityData.set(DATA_SKIN_VARIANT, normalizedVariant);
        }
    }

    public MutableComponent getCultivatorName() {
        return CultivatorNames.display(this.getSurnameIdx(), this.getGivenIdx());
    }

    public Realm getRealm() {
        int ord = (Integer)this.entityData.get(DATA_REALM_ORD);
        Realm[] vals = Realm.values();
        return vals[Math.floorMod(ord, vals.length)];
    }

    public SubStage getSubStage() {
        int level = (Integer)this.entityData.get(DATA_SUB_STAGE_ORD);
        Realm realm = this.getRealm();
        SubStage sub = realm.subStageAt(level);
        return sub != null ? sub : realm.firstSubStage();
    }

    public int getLooseImmortalTribulations() {
        if (this.getRealm() != Realm.LOOSE_IMMORTAL) {
            return 0;
        }
        return Math.max(1, Math.min(9, (Integer)this.entityData.get(DATA_LOOSE_IMMORTAL_TRIBULATIONS)));
    }

    public String getTechniqueId() {
        return (String)this.entityData.get(DATA_TECHNIQUE_ID);
    }

    public int getSkinVariant() {
        return WanderingCultivatorEntity.normalizeSkinVariantForGender((Integer)this.entityData.get(DATA_SKIN_VARIANT), this.getGender());
    }

    public boolean isDifuReaper() {
        return (Boolean)this.entityData.get(DATA_DIFU_REAPER);
    }

    public void setDifuReaper(boolean v) {
        this.entityData.set(DATA_DIFU_REAPER, v);
        if (v && !this.level().isClientSide) {
            this.ensureDifuReaperSpells();
            this.syncSpellIdsToData();
        }
    }

    private void ensureDifuReaperSpells() {
        if (!this.spellIds.contains(Spell.SOUL_HOOK.id())) {
            this.spellIds.add(Spell.SOUL_HOOK.id());
        }
        if (!this.spellIds.contains(Spell.YIN_YANG_EYE.id())) {
            this.spellIds.add(Spell.YIN_YANG_EYE.id());
        }
    }

    public boolean isNpcSoulState() {
        return (Boolean)this.entityData.get(DATA_NPC_SOUL_STATE);
    }

    public void enterNpcSoulState() {
        boolean wasSoul = this.isNpcSoulState();
        this.entityData.set(DATA_NPC_SOUL_STATE, true);
        if (!wasSoul) {
            this.npcSoulTicks = 0;
        }
        this.soulReaperTokenTradeAvailable = false;
        this.setTradingPlayer(null);
        this.clearNpcSoulAggro();
        this.setNoAi(false);
        this.setNoGravity(this.shouldNpcSoulFloat());
        this.currentQi = this.maxQi;
        this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
        this.setHealth(this.getMaxHealth());
        this.setPersistenceRequired();
        if (!this.level().isClientSide() && this.isAddedToWorld()) {
            LOADED_NPC_SOULS.add(this);
        }
    }

    public int getNpcSoulTicks() {
        return this.npcSoulTicks;
    }

    public boolean isSoulReaperTokenTradeAvailable() {
        return this.soulReaperTokenTradeAvailable;
    }

    public void consumeSoulReaperTokenTrade() {
        this.soulReaperTokenTradeAvailable = false;
    }

    public List<String> getSpellIds() {
        if (this.level().isClientSide) {
            String csv = (String)this.entityData.get(DATA_SPELL_IDS_CSV);
            if (csv.isEmpty()) {
                return Collections.emptyList();
            }
            return Arrays.asList(csv.split(","));
        }
        return Collections.unmodifiableList(this.spellIds);
    }

    public boolean learnSectRewardTechnique(@Nullable Technique technique) {
        if (technique == null) {
            return false;
        }
        if (technique.id().equals(this.getTechniqueId())) {
            return false;
        }
        this.entityData.set(DATA_TECHNIQUE_ID, technique.id());
        this.setPersistenceRequired();
        return true;
    }

    public boolean learnSectRewardSpell(@Nullable Spell spell) {
        if (spell == null || this.getRealm() == Realm.MORTAL || this.spellIds.contains(spell.id())) {
            return false;
        }
        if (!NpcSpellCaster.isLearnableByNpc(spell) && !this.isRealmAutomaticSpell(spell, this.getRealm())) {
            return false;
        }
        this.spellIds.add(spell.id());
        this.normalizeSpellIdsForRealm(this.getRealm());
        this.syncSpellIdsToData();
        this.ensureSwordForKnownSwordSpell(this.getRealm());
        this.setNoGravity(this.canFly() || this.isNpcSwordFlightActive());
        this.setPersistenceRequired();
        return this.spellIds.contains(spell.id());
    }

    public long getCurrentQi() {
        if (this.level().isClientSide) {
            return ((Integer)this.entityData.get(DATA_CURRENT_QI)).intValue();
        }
        return this.currentQi;
    }

    public long getMaxQi() {
        if (this.level().isClientSide) {
            return ((Integer)this.entityData.get(DATA_MAX_QI)).intValue();
        }
        return this.maxQi;
    }

    public void deductQi(long cost) {
        this.currentQi = Math.max(0L, this.currentQi - cost);
    }

    public long addQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        long room = this.maxQi - this.currentQi;
        long actual = Math.min(amount, room);
        this.currentQi += actual;
        return actual;
    }

    public long getNaturalQiRecoveryPerSecond() {
        if (this.maxQi <= 0L) {
            return 0L;
        }
        long baseRecovery = Math.max(1L, this.maxQi / 1500L);
        long qiSeaRecovery = Math.max(0L, (long)this.getZhenyuanAttrs()[4]) * 1L;
        return RealmPressureHandler.applyQiRecoveryPenalty((LivingEntity)this, baseRecovery + qiSeaRecovery);
    }

    public boolean canFly() {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)this)) {
            return false;
        }
        if (this.isNpcSoulState()) {
            return this.shouldNpcSoulFloat();
        }
        return this.spellIds.contains(Spell.QI_FLIGHT.id());
    }

    private boolean shouldNpcSoulFloat() {
        return this.isNpcSoulState() && this.level().dimension() != ModDimensions.DIFU;
    }

    public boolean isNpcSwordFlightActive() {
        return (Boolean)this.entityData.get(DATA_NPC_SWORD_FLIGHT_ACTIVE);
    }

    private void setNpcSwordFlightActive(boolean active) {
        this.entityData.set(DATA_NPC_SWORD_FLIGHT_ACTIVE, active);
    }

    public boolean canUseCombatFlight() {
        return this.canFly() || this.isNpcSwordFlightActive() || this.isNpcVoidEscapeActive();
    }

    public boolean canUseCombatFlightSoon() {
        return this.canUseCombatFlight() || this.canStartNpcSwordFlightForCombat();
    }

    private boolean canStartNpcSwordFlightForCombat() {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return false;
        }
        if (this.canFly()) {
            return false;
        }
        if (this.isTradingFreeze()) {
            return false;
        }
        if (SpiritLockHandler.isEntityLocked((Entity)this)) {
            return false;
        }
        if (!this.spellIds.contains(Spell.SWORD_FLIGHT.id())) {
            return false;
        }
        if (!(this.getMainHandItem().getItem() instanceof SwordItem)) {
            return false;
        }
        long upkeep = NpcSpellCaster.spellCost(this, Spell.SWORD_FLIGHT);
        upkeep = Math.max(1L, upkeep <= 0L ? NpcSpellCaster.generalQiCost(this, 20L) : upkeep);
        return this.getCurrentQi() >= upkeep * 3L;
    }

    public static boolean checkCultivatorSpawnRules(EntityType<WanderingCultivatorEntity> type, ServerLevelAccessor level, MobSpawnType reason, BlockPos pos, RandomSource random) {
        if (!Mob.checkMobSpawnRules(type, (LevelAccessor)level, (MobSpawnType)reason, (BlockPos)pos, (RandomSource)random)) {
            return false;
        }
        if (reason != MobSpawnType.NATURAL) {
            return true;
        }
        if (level.getLevel().dimension() != Level.OVERWORLD) {
            return true;
        }
        double chance = WanderingCultivatorEntity.isNearPreferredSpawnStructure(level, pos) ? 2.0E-4 : 5.0E-5;
        return random.nextDouble() < chance;
    }

    private static boolean isNearPreferredSpawnStructure(ServerLevelAccessor level, BlockPos pos) {
        ServerLevel serverLevel = level.getLevel();
        int step = 48;
        for (int dx = -96; dx <= 96; dx += step) {
            for (int dz = -96; dz <= 96; dz += step) {
                BlockPos sample = pos.offset(dx, 0, dz);
                if (!serverLevel.structureManager().getStructureWithPieceAt(sample, PREFERRED_SPAWN_STRUCTURES).isValid()) continue;
                return true;
            }
        }
        return false;
    }

    public boolean canScare(double distanceToClosestPlayer) {
        return false;
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor level, @NotNull DifficultyInstance difficulty, @NotNull MobSpawnType reason, @Nullable SpawnGroupData spawnData, @Nullable CompoundTag dataTag) {
        String id;
        Realm forced;
        boolean difu;
        this.setPersistenceRequired();
        boolean bl = difu = level.getLevel().dimension() == ModDimensions.DIFU || dataTag != null && dataTag.getBoolean("forcedDifuReaper");
        if (difu) {
            this.setDifuReaper(true);
        }
        Realm realm = difu ? Realm.values()[Realm.QI_REFINING.ordinal() + this.random.nextInt(Math.max(1, Realm.TRIBULATION_TRANSCENDENCE.ordinal() - Realm.QI_REFINING.ordinal() + 1))] : (dataTag != null && dataTag.contains("forcedRealmId") ? ((forced = Realm.byId(id = dataTag.getString("forcedRealmId"))) != null ? forced : CultivatorRealmRoller.roll(this.random)) : CultivatorRealmRoller.roll(this.random));
        if (realm == Realm.GREAT_EMPEROR) {
            if (level.getLevel() instanceof ServerLevel && !GreatEmperorTracker.get((ServerLevel)level.getLevel()).claimEmperor()) {
                // 全存档大帝已达上限（10），本次生成降级为散仙
                realm = Realm.LOOSE_IMMORTAL;
            }
        }
        int looseImmortalTribulations = 0;
        if (realm == Realm.LOOSE_IMMORTAL) {
            looseImmortalTribulations = dataTag != null && dataTag.contains("forcedLooseImmortalTribulations", 3) ? dataTag.getInt("forcedLooseImmortalTribulations") : 1 + this.random.nextInt(9);
            looseImmortalTribulations = Math.max(1, Math.min(9, looseImmortalTribulations));
        }
        SubStage sub = realm.subStageAt(realm.usesNumericLevels() ? 1 + this.random.nextInt(realm.subStageCount()) : this.random.nextInt(realm.subStageCount()));
        this.entityData.set(DATA_REALM_ORD, realm.ordinal());
        this.entityData.set(DATA_SUB_STAGE_ORD, sub.level());
        this.entityData.set(DATA_LOOSE_IMMORTAL_TRIBULATIONS, looseImmortalTribulations);
        int rolledGender = this.random.nextBoolean() ? 1 : 2;
        this.entityData.set(DATA_GENDER, rolledGender);
        this.entityData.set(DATA_SKIN_VARIANT, WanderingCultivatorEntity.randomSkinVariantForGender(rolledGender, this.random));
        this.repairSkinVariantGenderMismatch();
        this.entityData.set(DATA_SURNAME_IDX, CultivatorNames.randomSurnameIdx(this.random));
        this.entityData.set(DATA_GIVEN_IDX, CultivatorNames.randomGivenIdx(this.random));
        this.soulReaperTokenTradeAvailable = difu;
        this.maxQi = realm.maxQi(sub);
        if (realm == Realm.LOOSE_IMMORTAL && looseImmortalTribulations > 0) {
            this.maxQi += LooseImmortalBonusHelper.maxQiBonusForLevel(looseImmortalTribulations);
        }
        this.currentQi = this.maxQi;
        this.entityData.set(DATA_MAX_QI, ((int)Math.min(Integer.MAX_VALUE, this.maxQi)));
        this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
        Identity identity = this.rollIdentity();
        this.entityData.set(DATA_IDENTITY_ID, identity.id());
        SpiritRoot root = this.rollSpiritRoot(realm);
        this.entityData.set(DATA_SPIRIT_ROOT_ID, root.id());
        Physique physique = this.rollPhysique();
        this.entityData.set(DATA_PHYSIQUE_ID, physique.id());
        if (root == SpiritRoot.HEAVENLY_HIDDEN) {
            this.currentQi = this.maxQi = Math.max(1L, Math.round((double)this.maxQi * 1.5));
            this.entityData.set(DATA_MAX_QI, ((int)Math.min(Integer.MAX_VALUE, this.maxQi)));
            this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
        }
        if (realm != Realm.MORTAL) {
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
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(atk);
        this.maybeEquipSword(realm);
        if (difu) {
            this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)ModItems.SOUL_HOOK.get()));
            this.setDropChance(EquipmentSlot.MAINHAND, 0.0f);
        }
        if (realm == Realm.MORTAL) {
            this.stockMortalItems();
        } else {
            this.stockSpiritStones(realm);
            this.stockPillsForRealm(realm);
            this.stockGoldenCoreMaterials(realm);
        }
        this.stockTravelAndTreasureItems(realm);
        this.removeForbiddenNaturalLootFromInventory();
        if (difu && this.random.nextFloat() < 0.35f) {
            this.getInventory().addItem(new ItemStack((ItemLike)ModItems.SOUL_REAPER_TOKEN.get()));
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
        this.offers = null;
        return super.finalizeSpawn(level, difficulty, reason, spawnData, dataTag);
    }

    private void maybeEquipSword(Realm realm) {
        if (!this.knowsSwordSpell()) {
            return;
        }
        if (this.random.nextDouble() >= 0.5) {
            return;
        }
        this.equipSwordForRealm(realm);
    }

    private void equipSwordForRealm(Realm realm) {
        Item sword = this.swordForRealm(realm);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack((ItemLike)sword));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.05f);
    }

    private boolean knowsSwordSpell() {
        for (String id : this.spellIds) {
            if (!SWORD_SPELL_IDS.contains(id)) continue;
            return true;
        }
        return false;
    }

    private void ensureSwordForKnownSwordSpell(Realm realm) {
        TieredWeapon weapon;
        boolean swordLike;
        Item item = this.getMainHandItem().getItem();
        boolean bl = swordLike = item instanceof SwordItem || item instanceof TieredWeapon && (weapon = (TieredWeapon)item).isSwordWeapon();
        if (!swordLike) {
            this.stopNpcSwordFlight(true);
        }
    }

    private Item swordForRealm(Realm realm) {
        return CultivationRandomPools.randomSwordForRealm(realm, this.random).orElse(Items.IRON_SWORD);
    }

    private void stockPillsForRealm(Realm realm) {
        SimpleContainer inv = this.getInventory();
        PillTier[] tiers = this.pillTiersForRealm(realm);
        boolean advancedRealm = realm.ordinal() >= Realm.GOLDEN_CORE.ordinal();
        for (PillTier tier : tiers) {
            List<Item> pillPool = CultivationRandomPools.pillsForTier(tier);
            int rolls = realm.ordinal() >= Realm.NASCENT_SOUL.ordinal() ? 2 : 1;
            int maxCount = advancedRealm ? 3 : 2;
            for (int i = 0; i < rolls; ++i) {
                this.maybeStockRandomItem(inv, pillPool, 1.0f, 1, maxCount);
            }
            this.maybeStockItem(inv, this.cultivationPillForTier(tier), advancedRealm ? 0.55f : 0.4f, 1, tier.ordinal() >= PillTier.SUPREME.ordinal() ? 1 : 2);
        }
    }

    @Nullable
    private Item cultivationPillForTier(PillTier tier) {
        return switch (tier) {
            default -> throw new IncompatibleClassChangeError();
            case LOW -> (Item)ModItems.PILL_CULTIVATION_LOW.get();
            case MID -> (Item)ModItems.PILL_CULTIVATION_MID.get();
            case HIGH -> (Item)ModItems.PILL_CULTIVATION_HIGH.get();
            case SUPREME -> (Item)ModItems.PILL_CULTIVATION_SUPREME.get();
            case IMMORTAL -> (Item)ModItems.PILL_CULTIVATION_IMMORTAL.get();
        };
    }

    private void stockGoldenCoreMaterials(Realm realm) {
        if (realm.ordinal() < Realm.FOUNDATION_BUILDING.ordinal()) {
            return;
        }
        SimpleContainer inv = this.getInventory();
        float baseChance = this.goldenCoreMaterialBaseChance(realm);
        this.maybeStockGoldenCoreItem(inv, (Item)ModItems.JIEDAN_PILL.get(), WanderingCultivatorEntity.jiedanPillStockChanceForRealm(realm), 2, 5);
        this.maybeStockGoldenCoreItem(inv, (Item)ModItems.BLOOD_JIEDAN_PILL.get(), Math.min(0.34f, baseChance * 1.45f), 1, 2);
        this.maybeStockGoldenCoreItem(inv, (Item)ModItems.ALL_CREATURES_TRUE_BLOOD.get(), Math.min(0.3f, baseChance * 0.9f), 1, 1);
        this.maybeStockGoldenCoreItem(inv, (Item)ModItems.EARTH_EVIL_QI.get(), Math.min(0.32f, baseChance * 0.95f), 1, 1);
        this.maybeStockGoldenCoreItem(inv, (Item)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get(), Math.min(0.28f, baseChance * 0.75f), 1, 1);
        if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            this.maybeStockGoldenCoreItem(inv, (Item)ModItems.HEAVEN_CLEAR_QI.get(), Math.min(0.28f, baseChance * 0.72f), 1, 1);
        }
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) {
            this.maybeStockGoldenCoreItem(inv, (Item)ModItems.NINGZHEN_CREATION_FRUIT.get(), Math.min(0.24f, baseChance * 0.58f), 1, 1);
        }
    }

    private float goldenCoreMaterialBaseChance(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case FOUNDATION_BUILDING -> 0.14f;
            case GOLDEN_CORE -> 0.18f;
            case NASCENT_SOUL -> 0.22f;
            case SOUL_FORMATION -> 0.24f;
            case VOID_REFINING, BODY_INTEGRATION -> 0.27f;
            case MAHAYANA, TRIBULATION_TRANSCENDENCE -> 0.3f;
            case TRUE_IMMORTAL, LOOSE_IMMORTAL, GREAT_EMPEROR -> 0.33f;
            case MORTAL, BODY_TEMPERING, QI_REFINING -> 0.0f;
        };
    }

    static float jiedanPillStockChanceForRealm(Realm realm) {
        return Math.min(0.78f, WanderingCultivatorEntity.goldenCoreMaterialBaseChanceStatic(realm) * 3.2f);
    }

    private static float goldenCoreMaterialBaseChanceStatic(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case FOUNDATION_BUILDING -> 0.14f;
            case GOLDEN_CORE -> 0.18f;
            case NASCENT_SOUL -> 0.22f;
            case SOUL_FORMATION -> 0.24f;
            case VOID_REFINING, BODY_INTEGRATION -> 0.27f;
            case MAHAYANA, TRIBULATION_TRANSCENDENCE -> 0.3f;
            case TRUE_IMMORTAL, LOOSE_IMMORTAL, GREAT_EMPEROR -> 0.33f;
            case MORTAL, BODY_TEMPERING, QI_REFINING -> 0.0f;
        };
    }

    private void maybeStockGoldenCoreItem(SimpleContainer inv, Item item, float chance, int minCount, int maxCount) {
        if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return;
        }
        if (chance <= 0.0f || this.random.nextFloat() >= chance) {
            return;
        }
        int count = minCount + this.random.nextInt(Math.max(1, maxCount - minCount + 1));
        inv.addItem(new ItemStack((ItemLike)item, count));
    }

    private SpiritRoot rollSpiritRoot(Realm realm) {
        SpiritRoot[] all = SpiritRoot.values();
        ArrayList<SpiritRoot> candidates = new ArrayList<SpiritRoot>();
        ArrayList<Integer> weights = new ArrayList<Integer>();
        int totalWeight = 0;
        for (SpiritRoot r : all) {
            int w;
            if (!r.isSelectableRoot()) continue;
            w = switch (r.rarity()) {
                default -> throw new IncompatibleClassChangeError();
                case SSR -> 3;
                case SR -> 8;
                case R -> 15;
                case SPECIAL -> 1;
                case NORMAL -> 0;
            };
            if (w <= 0) continue;
            candidates.add(r);
            weights.add(w);
            totalWeight += w;
        }
        if (candidates.isEmpty()) {
            return SpiritRoot.NONE;
        }
        int target = this.random.nextInt(totalWeight);
        int acc = 0;
        for (int i = 0; i < weights.size(); ++i) {
            if ((acc += ((Integer)weights.get(i)).intValue()) <= target) continue;
            return (SpiritRoot)((Object)candidates.get(i));
        }
        return (SpiritRoot)((Object)candidates.get(candidates.size() - 1));
    }

    private Physique rollPhysique() {
        List<Physique> pool = Physique.weightedPool();
        if (pool.isEmpty()) {
            return Physique.MORTAL_BODY;
        }
        return pool.get(this.random.nextInt(pool.size()));
    }

    private Identity rollIdentity() {
        List<Identity> pool = Identity.selectableOrigins();
        if (pool.isEmpty()) {
            return Identity.LONE_CULTIVATOR;
        }
        return pool.get(this.random.nextInt(pool.size()));
    }

    private void assignZhenyuanForRealm(Realm realm, SubStage sub) {
        AttributeInstance atkAttr;
        int autoPerStat = CultivationData.computeAutomaticZhenyuanAttrPerStat(realm, sub);
        int extraPerMinor = Math.max(0, this.getSpiritRoot().bonus().extraZhenyuanPerSubLevel()) + Math.max(0, PhysiqueBonusHelper.extraZhenyuanPerMinor(this.getPhysique()));
        int randomPoints = CultivationData.computeTotalZhenyuanEarned(realm, sub, extraPerMinor);
        if (autoPerStat <= 0 && randomPoints <= 0) {
            this.entityData.set(DATA_ZHENYUAN_CSV, "0,0,0,0,0");
            this.normalizeGroundMovementSpeedAttribute();
            return;
        }
        int[] attrs = new int[]{autoPerStat, autoPerStat, autoPerStat, autoPerStat, autoPerStat};
        for (int i = 0; i < randomPoints; ++i) {
            int n = this.random.nextInt(5);
            attrs[n] = attrs[n] + 1;
        }
        this.entityData.set(DATA_ZHENYUAN_CSV, (attrs[0] + "," + attrs[1] + "," + attrs[2] + "," + attrs[3] + "," + attrs[4]));
        AttributeInstance hpAttr = this.getAttribute(Attributes.MAX_HEALTH);
        if (hpAttr != null) {
            double baseHp = hpAttr.getBaseValue();
            double physiqueHpMult = Math.max(1.0, this.getPhysique().bonus().hpMult());
            double physiqueFlatHp = Math.max(0, this.getPhysique().bonus().maxHpBonus());
            hpAttr.setBaseValue((baseHp + (double)attrs[0] + physiqueFlatHp) * physiqueHpMult);
            this.setHealth(this.getMaxHealth());
        }
        if ((atkAttr = this.getAttribute(Attributes.ATTACK_DAMAGE)) != null && attrs[1] > 0) {
            atkAttr.addPermanentModifier(new AttributeModifier(UUID.nameUUIDFromBytes(("npc.zhenyuan.atk." + String.valueOf(this.getUUID())).getBytes()), "npc_zhenyuan_attack", (double)attrs[1], AttributeModifier.Operation.ADDITION));
        }
        this.normalizeGroundMovementSpeedAttribute();
        this.normalizeGroundMovementSpeedAttribute();
        this.maxQi += (long)attrs[4] * 100L;
        double physiqueMaxQiMult = PhysiqueBonusHelper.maxQiMultiplier(this.getPhysique());
        if (physiqueMaxQiMult != 1.0) {
            this.maxQi = Math.max(1L, Math.round((double)this.maxQi * physiqueMaxQiMult));
        }
        this.currentQi = this.maxQi;
        this.entityData.set(DATA_MAX_QI, ((int)Math.min(Integer.MAX_VALUE, this.maxQi)));
        this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
    }

    public int getZhenyuanAttackBonus() {
        return this.getZhenyuanAttrs()[1];
    }

    public int getBodyDefense() {
        Technique t = Technique.byId(this.getTechniqueId());
        int techDef = t == null ? 0 : t.bonus().defense;
        return BodyDefenseHelper.npcBodyDefense(techDef);
    }

    public int getZhenyuanAgility() {
        return this.getZhenyuanAttrs()[2];
    }

    public double getZhenyuanSpellPowerMult() {
        return 1.0 + (double)this.getZhenyuanAttrs()[3] * 0.05;
    }

    private void assignCraftingRanks(Realm realm) {
        int[] nArray;
        switch (realm) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case MORTAL: {
                int[] nArray2 = new int[2];
                nArray2[0] = 0;
                nArray = nArray2;
                nArray2[1] = 0;
                break;
            }
            case QI_REFINING: {
                int[] nArray3 = new int[2];
                nArray3[0] = 0;
                nArray = nArray3;
                nArray3[1] = 1;
                break;
            }
            case BODY_TEMPERING: {
                int[] nArrayBody = new int[2];
                nArrayBody[0] = 0;
                nArray = nArrayBody;
                nArrayBody[1] = 1;
                break;
            }
            case FOUNDATION_BUILDING: {
                int[] nArray4 = new int[2];
                nArray4[0] = 0;
                nArray = nArray4;
                nArray4[1] = 2;
                break;
            }
            case GOLDEN_CORE: {
                int[] nArray5 = new int[2];
                nArray5[0] = 1;
                nArray = nArray5;
                nArray5[1] = 3;
                break;
            }
            case NASCENT_SOUL: {
                int[] nArray6 = new int[2];
                nArray6[0] = 2;
                nArray = nArray6;
                nArray6[1] = 4;
                break;
            }
            case SOUL_FORMATION: {
                int[] nArray7 = new int[2];
                nArray7[0] = 3;
                nArray = nArray7;
                nArray7[1] = 5;
                break;
            }
            case VOID_REFINING: {
                int[] nArray8 = new int[2];
                nArray8[0] = 4;
                nArray = nArray8;
                nArray8[1] = 6;
                break;
            }
            case BODY_INTEGRATION: {
                int[] nArray9 = new int[2];
                nArray9[0] = 5;
                nArray = nArray9;
                nArray9[1] = 7;
                break;
            }
            case MAHAYANA: {
                int[] nArray10 = new int[2];
                nArray10[0] = 6;
                nArray = nArray10;
                nArray10[1] = 8;
                break;
            }
            case TRIBULATION_TRANSCENDENCE: {
                int[] nArray11 = new int[2];
                nArray11[0] = 7;
                nArray = nArray11;
                nArray11[1] = 9;
                break;
            }
            case TRUE_IMMORTAL: 
            case LOOSE_IMMORTAL:
            case GREAT_EMPEROR: {
                int[] nArray12 = new int[2];
                nArray12[0] = 9;
                nArray = nArray12;
                nArray12[1] = 10;
            }
        }
        int[] range = nArray;
        int alch = range[0] + this.random.nextInt(range[1] - range[0] + 1);
        int refin = range[0] + this.random.nextInt(range[1] - range[0] + 1);
        this.entityData.set(DATA_ALCHEMY_RANK_ORD, alch);
        this.entityData.set(DATA_REFINING_RANK_ORD, refin);
    }

    private PillTier[] pillTiersForRealm(Realm realm) {
        return CultivationRandomPools.pillTiersForRealm(realm);
    }

    private void stockSpiritStones(Realm realm) {
        Item stoneItem = this.stoneItemForRealm(realm);
        int count = 15 + this.random.nextInt(26);
        SimpleContainer inv = this.getInventory();
        inv.addItem(new ItemStack((ItemLike)stoneItem, count));
    }

    private void stockMortalItems() {
        SimpleContainer inv = this.getInventory();
        inv.addItem(new ItemStack((ItemLike)ModItems.CUSHION.get(), 1 + this.random.nextInt(3)));
        inv.addItem(new ItemStack((ItemLike)ModItems.DIVINATION_COMPASS.get(), 1));
        inv.addItem(new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), 5 + this.random.nextInt(11)));
        inv.addItem(new ItemStack((ItemLike)ModItems.TECHNIQUE_BOOK_FRAGMENT.get(), 1 + this.random.nextInt(2)));
        List<Item> lowSpellBooks = CultivationRandomPools.spellBookItemsForTier(ItemTier.LOW);
        Item book = lowSpellBooks.isEmpty() ? (Item)ModItems.SPELL_BOOK_FIREBALL.get() : lowSpellBooks.get(this.random.nextInt(lowSpellBooks.size()));
        inv.addItem(new ItemStack((ItemLike)book, 1 + this.random.nextInt(2)));
    }

    private void stockTravelAndTreasureItems(Realm realm) {
        SimpleContainer inv = this.getInventory();
        this.maybeStockItem(inv, Items.BREAD, 0.62f, 2, 5);
        this.maybeStockItem(inv, Items.COOKED_BEEF, 0.3f, 1, 3);
        this.maybeStockItem(inv, Items.TORCH, 0.36f, 4, 12);
        this.maybeStockItem(inv, Items.PAPER, 0.32f, 2, 6);
        this.maybeStockItem(inv, Items.BOOK, 0.2f, 1, 2);
        this.maybeStockItem(inv, Items.ARROW, 0.22f, 4, 12);
        this.maybeStockItem(inv, Items.EMERALD, 0.26f, 1, 4);
        this.maybeStockItem(inv, Items.IRON_INGOT, 0.16f, 1, 4);
        this.maybeStockItem(inv, Items.GOLD_INGOT, 0.1f, 1, 3);
        this.maybeStockItem(inv, Items.LAPIS_LAZULI, 0.18f, 2, 8);
        this.maybeStockItem(inv, Items.REDSTONE, 0.15f, 3, 10);
        this.maybeStockItem(inv, Items.AMETHYST_SHARD, 0.08f, 1, 3);
        this.maybeStockItem(inv, Items.ENDER_PEARL, 0.035f, 1, 1);
        this.maybeStockItem(inv, (Item)ModItems.INK.get(), 0.32f, 1, 4);
        this.maybeStockItem(inv, (Item)ModItems.TALISMAN_PAPER.get(), 0.4f, 2, 7);
        this.maybeStockItem(inv, (Item)ModItems.HERB.get(), 0.42f, 2, 6);
        this.maybeStockItem(inv, (Item)ModItems.FORMATION_COMPASS.get(), 0.1f, 1, 1);
        this.maybeStockItem(inv, (Item)ModItems.FORMATION_INSCRIPTION_KNIFE.get(), 0.08f, 1, 1);
        if (realm.ordinal() >= Realm.QI_REFINING.ordinal()) {
            this.maybeStockRandomBook(inv, CultivationRandomPools.techniqueBookItemsForTier(CultivationRandomPools.techniqueTierForRealm(realm)), 0.22f);
            this.maybeStockRandomBook(inv, CultivationRandomPools.spellBookItemsForTier(CultivationRandomPools.spellTierForRealm(realm)), 0.26f);
            this.maybeStockItem(inv, (Item)ModItems.ZHUJI_DAN.get(), 0.62f, 2, 5);
            this.maybeStockItem(inv, (Item)ModItems.BLOOD_SPIRIT_PILL.get(), 0.14f, 1, 2);
            this.maybeStockItem(inv, (Item)ModItems.YOUTH_PILL.get(), 0.06f, 1, 1);
            this.maybeStockItem(inv, (Item)ModItems.SEX_CHANGE_PILL.get(), 0.05f, 1, 1);
        }
        if (realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal()) {
            this.maybeStockItem(inv, (Item)ModItems.FOUNDATION_SECRET.get(), 0.13f, 1, 1);
            this.maybeStockItem(inv, (Item)ModItems.DAO_FOUNDATION_FRUIT.get(), 0.07f, 1, 1);
            this.maybeStockItem(inv, (Item)ModItems.YOUTH_PILL.get(), 0.1f, 1, 1);
            this.maybeStockItem(inv, (Item)ModItems.SEX_CHANGE_PILL.get(), 0.08f, 1, 1);
        }
    }

    private void maybeStockItem(SimpleContainer inv, @Nullable Item item, float chance, int minCount, int maxCount) {
        if (CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            return;
        }
        if (item == null || chance <= 0.0f || this.random.nextFloat() >= chance) {
            return;
        }
        int count = minCount + this.random.nextInt(Math.max(1, maxCount - minCount + 1));
        inv.addItem(new ItemStack((ItemLike)item, count));
    }

    private void maybeStockRandomItem(SimpleContainer inv, List<Item> pool, float chance, int minCount, int maxCount) {
        if (pool.isEmpty() || chance <= 0.0f || this.random.nextFloat() >= chance) {
            return;
        }
        this.maybeStockItem(inv, pool.get(this.random.nextInt(pool.size())), 1.0f, minCount, maxCount);
    }

    private void maybeStockRandomBook(SimpleContainer inv, List<Item> pool, float chance) {
        if (pool.isEmpty() || this.random.nextFloat() >= chance) {
            return;
        }
        Item item = pool.get(this.random.nextInt(pool.size()));
        if (!CultivationRandomPools.isForbiddenNaturalLootItem(item)) {
            inv.addItem(new ItemStack((ItemLike)item));
        }
    }

    public boolean removeForbiddenNaturalLootFromInventory() {
        boolean removed = false;
        SimpleContainer inv = this.getInventory();
        for (int slot = 0; slot < inv.getContainerSize(); ++slot) {
            ItemStack stack = inv.getItem(slot);
            if (!CultivationRandomPools.isForbiddenNaturalLootStack(stack)) continue;
            inv.setItem(slot, ItemStack.EMPTY);
            removed = true;
        }
        if (removed) {
            inv.setChanged();
            this.offers = null;
        }
        return removed;
    }

    private Item stoneItemForRealm(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL, BODY_TEMPERING, QI_REFINING -> (Item)ModItems.LOW_SPIRIT_STONE.get();
            case FOUNDATION_BUILDING, GOLDEN_CORE -> (Item)ModItems.MID_SPIRIT_STONE.get();
            case NASCENT_SOUL, SOUL_FORMATION, VOID_REFINING, BODY_INTEGRATION -> (Item)ModItems.HIGH_SPIRIT_STONE.get();
            case MAHAYANA, TRIBULATION_TRANSCENDENCE, TRUE_IMMORTAL, LOOSE_IMMORTAL, GREAT_EMPEROR -> (Item)ModItems.SUPREME_SPIRIT_STONE.get();
        };
    }

    private static long qiGainFromStone(Item item) {
        if (item == ModItems.LOW_SPIRIT_STONE.get()) {
            return 10L;
        }
        if (item == ModItems.MID_SPIRIT_STONE.get()) {
            return 100L;
        }
        if (item == ModItems.HIGH_SPIRIT_STONE.get()) {
            return 1000L;
        }
        if (item == ModItems.SUPREME_SPIRIT_STONE.get()) {
            return 10000L;
        }
        return 0L;
    }

    protected void dropCustomDeathLoot(@NotNull DamageSource source, int looting, boolean recentlyHit) {
        String pickId;
        Spell pick;
        Item bookItem;
        Technique tech;
        String techId;
        super.dropCustomDeathLoot(source, looting, recentlyHit);
        if (this.level().isClientSide) {
            return;
        }
        if (this.random.nextFloat() < 0.5f && !(techId = this.getTechniqueId()).isEmpty() && (tech = Technique.byId(techId)) != null && (bookItem = WanderingCultivatorEntity.techniqueBookItem(tech)) != null) {
            this.spawnAtLocation(new ItemStack((ItemLike)bookItem));
        }
        if (!this.spellIds.isEmpty() && (pick = Spell.byId(pickId = this.spellIds.get(this.random.nextInt(this.spellIds.size())))) != null && (bookItem = WanderingCultivatorEntity.spellBookItem(pick)) != null) {
            this.spawnAtLocation(new ItemStack((ItemLike)bookItem));
        }
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (WanderingCultivatorEntity.shouldDropInventoryStack(stack)) {
                this.spawnAtLocation(stack.copy());
            }
            stack.setCount(0);
        }
    }

    public void moveDeathLootToCorpse(CorpseEntity corpse) {
        String pickId;
        Spell pick;
        Item bookItem;
        Technique tech;
        String techId;
        ItemStack offhand;
        if (corpse == null || this.level().isClientSide) {
            return;
        }
        ItemStack main = this.getMainHandItem();
        if (!main.isEmpty()) {
            corpse.moveItemIntoLoot(main.copy());
            this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        }
        if (!(offhand = this.getOffhandItem()).isEmpty()) {
            corpse.moveItemIntoLoot(offhand.copy());
            this.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
        }
        if (this.random.nextFloat() < 0.5f && !(techId = this.getTechniqueId()).isEmpty() && (tech = Technique.byId(techId)) != null && (bookItem = WanderingCultivatorEntity.techniqueBookItem(tech)) != null) {
            corpse.moveItemIntoLoot(new ItemStack((ItemLike)bookItem));
        }
        if (!this.spellIds.isEmpty() && (pick = Spell.byId(pickId = this.spellIds.get(this.random.nextInt(this.spellIds.size())))) != null && (bookItem = WanderingCultivatorEntity.spellBookItem(pick)) != null) {
            corpse.moveItemIntoLoot(new ItemStack((ItemLike)bookItem));
        }
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty()) continue;
            if (WanderingCultivatorEntity.shouldDropInventoryStack(stack)) {
                corpse.moveItemIntoLoot(stack.copy());
            }
            inv.setItem(i, ItemStack.EMPTY);
        }
        inv.setChanged();
    }

    private static boolean shouldDropInventoryStack(ItemStack stack) {
        if (CultivationRandomPools.isForbiddenNaturalLootStack(stack)) {
            return false;
        }
        return !stack.is((Item)ModItems.SECT_TOKEN.get()) || SectTokenItem.isTemporaryLinked(stack);
    }

    public static Item techniqueBookItem(Technique tech) {
        return ModItems.techniqueBookItem(tech);
    }

    public static Item spellBookItem(Spell sp) {
        return ModItems.spellBookItem(sp);
    }

    private void tryStartEatingSpiritStone() {
        if (this.isUsingItem()) {
            return;
        }
        if (this.currentQi >= this.maxQi) {
            return;
        }
        SimpleContainer inv = this.getInventory();
        ItemStack bestStack = ItemStack.EMPTY;
        long bestGain = 0L;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            long gain;
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || (gain = WanderingCultivatorEntity.qiGainFromStone(stack.getItem())) <= bestGain) continue;
            bestGain = gain;
            bestStack = stack;
        }
        if (bestStack.isEmpty()) {
            return;
        }
        ItemStack visual = new ItemStack((ItemLike)bestStack.getItem(), 1);
        this.setItemInHand(InteractionHand.OFF_HAND, visual);
        this.startUsingItem(InteractionHand.OFF_HAND);
    }

    private void completeEatingSpiritStone() {
        ItemStack used = this.getItemInHand(InteractionHand.OFF_HAND);
        long gain = WanderingCultivatorEntity.qiGainFromStone(used.getItem());
        if (gain > 0L) {
            Level level;
            this.currentQi = Math.min(this.maxQi, this.currentQi + gain);
            SimpleContainer inv = this.getInventory();
            for (int i = 0; i < inv.getContainerSize(); ++i) {
                ItemStack stack = inv.getItem(i);
                if (stack.isEmpty() || stack.getItem() != used.getItem()) continue;
                stack.shrink(1);
                break;
            }
            if ((level = this.level()) instanceof ServerLevel) {
                ServerLevel sl = (ServerLevel)level;
                sl.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, this.getX(), this.getY() + (double)this.getBbHeight() * 0.8, this.getZ(), 14, 0.4, 0.2, 0.4, 0.02);
                sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.5f, 1.6f);
            }
        }
        this.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
        this.stopUsingItem();
    }

    private boolean tryAutoEatRejuvenationPill() {
        int absorptionTicks;
        float hp = this.getHealth();
        float maxHp = this.getMaxHealth();
        if (hp <= 0.0f || maxHp <= 0.0f || hp * 3.0f > maxHp) {
            return false;
        }
        SimpleContainer inv = this.getInventory();
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            int idx;
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || (idx = WanderingCultivatorEntity.rejuvenationPillTier(s.getItem())) <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        Item consumedItem = inv.getItem(bestSlot).getItem();
        inv.getItem(bestSlot).shrink(1);
        PillTier tier = PillTier.values()[bestIdx];
        float healAmount = PillEffectSpecs.rejuvenationHeal(consumedItem, tier, maxHp);
        this.heal(healAmount);
        int regenTicks = PillEffectSpecs.regenerationTicks(consumedItem, tier);
        if (regenTicks > 0) {
            this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, regenTicks, PillEffectSpecs.regenerationAmplifier(consumedItem, tier)));
        }
        if ((absorptionTicks = PillEffectSpecs.absorptionTicks(consumedItem, tier)) > 0) {
            this.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, absorptionTicks, PillEffectSpecs.absorptionAmplifier(consumedItem, tier)));
        }
        this.playPillEatFx((ParticleOptions)ParticleTypes.HEART);
        return true;
    }

    private boolean tryAutoEatClearMindPill() {
        boolean hasNegative = false;
        for (MobEffectInstance e2 : this.getActiveEffects()) {
            if (e2.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
            hasNegative = true;
            break;
        }
        if (!hasNegative) {
            return false;
        }
        SimpleContainer inv = this.getInventory();
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            int idx;
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || (idx = WanderingCultivatorEntity.clearMindPillTier(s.getItem())) <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        inv.getItem(bestSlot).shrink(1);
        this.getActiveEffects().stream().filter(e -> e.getEffect().getCategory() == MobEffectCategory.HARMFUL).map(MobEffectInstance::getEffect).toList().forEach(arg_0 -> ((WanderingCultivatorEntity)this).removeEffect(arg_0));
        this.playPillEatFx((ParticleOptions)ParticleTypes.ENCHANTED_HIT);
        return true;
    }

    private boolean tryAutoEatQiRecoveryPill() {
        if (this.maxQi <= 0L || this.currentQi * 4L > this.maxQi) {
            return false;
        }
        SimpleContainer inv = this.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            if (WanderingCultivatorEntity.qiGainFromStone(inv.getItem(i).getItem()) <= 0L) continue;
            return false;
        }
        int bestIdx = -1;
        int bestSlot = -1;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            int idx;
            ItemStack s = inv.getItem(i);
            if (s.isEmpty() || (idx = WanderingCultivatorEntity.qiRecoveryPillTier(s.getItem())) <= bestIdx) continue;
            bestIdx = idx;
            bestSlot = i;
        }
        if (bestSlot < 0) {
            return false;
        }
        Item consumedQiPill = inv.getItem(bestSlot).getItem();
        inv.getItem(bestSlot).shrink(1);
        long fallbackGain = switch (bestIdx) {
            case 0 -> 10L;
            case 1 -> 100L;
            case 2 -> 1000L;
            case 3 -> 10000L;
            case 4 -> this.maxQi;
            default -> 0L;
        };
        long gain = PillEffectSpecs.qiAmount(consumedQiPill, (int)Math.min(Integer.MAX_VALUE, fallbackGain));
        if (gain < 0L) {
            gain = this.maxQi;
        }
        this.addQi(gain);
        this.playPillEatFx((ParticleOptions)ParticleTypes.ENCHANT);
        return true;
    }

    private static int rejuvenationPillTier(Item item) {
        if (item instanceof RejuvenationPillItem) {
            RejuvenationPillItem pill = (RejuvenationPillItem)item;
            return pill.tier().ordinal();
        }
        return -1;
    }

    private static int clearMindPillTier(Item item) {
        if (item instanceof ClearMindPillItem) {
            ClearMindPillItem pill = (ClearMindPillItem)item;
            return pill.tier().ordinal();
        }
        return -1;
    }

    private static int qiRecoveryPillTier(Item item) {
        if (item instanceof PillItem) {
            PillItem pill = (PillItem)item;
            if (item.getClass() == PillItem.class) {
                return pill.tier().ordinal();
            }
        }
        return -1;
    }

    private void playPillEatFx(ParticleOptions particle) {
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.sendParticles(particle, this.getX(), this.getY() + (double)this.getBbHeight() * 0.85, this.getZ(), 12, 0.35, 0.18, 0.35, 0.04);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GENERIC_DRINK, SoundSource.NEUTRAL, 0.45f, 1.4f);
        }
    }

    private void assignTechnique(Realm realm) {
        ItemTier tier = WanderingCultivatorEntity.techTierForRealm(realm);
        List<Technique> candidates = CultivationRandomPools.techniquesForTier(tier);
        if (candidates.isEmpty()) {
            return;
        }
        Technique chosen = candidates.get(this.random.nextInt(candidates.size()));
        this.entityData.set(DATA_TECHNIQUE_ID, chosen.id());
    }

    private void assignSpells(Realm realm) {
        this.spellIds.clear();
        this.addRealmAutomaticSpells(realm);
        int[] range = WanderingCultivatorEntity.randomSpellCountRange(realm);
        int spellCount = range[0] + this.random.nextInt(range[1] - range[0] + 1);
        List<Spell> spellPool = WanderingCultivatorEntity.buildLearnableSpellPool(realm);
        Collections.shuffle(spellPool, new Random(this.random.nextLong()));
        for (int i = 0; i < Math.min(spellCount, spellPool.size()); ++i) {
            this.addSpellIfMissing(spellPool.get(i));
        }
        if (realm == Realm.TRIBULATION_TRANSCENDENCE) {
            this.addImmortalSpells(1);
        }
        if (realm == Realm.TRUE_IMMORTAL || realm == Realm.LOOSE_IMMORTAL) {
            this.addImmortalSpells(1 + this.random.nextInt(3));
        }
        if (realm == Realm.GREAT_EMPEROR) {
            this.addImmortalSpells(3 + this.random.nextInt(3));
        }
        this.normalizeSpellIdsForRealm(realm);
    }

    private void addSpellIfMissing(Spell spell) {
        if (spell != null && !this.spellIds.contains(spell.id())) {
            this.spellIds.add(spell.id());
        }
    }

    private void addRealmAutomaticSpells(Realm realm) {
        if (realm == Realm.MORTAL) {
            return;
        }
        if (realm.ordinal() >= Realm.QI_REFINING.ordinal()) {
            this.addSpellIfMissing(Spell.SPIRIT_VISION);
            this.addSpellIfMissing(Spell.QI_TRANSFER);
            this.addSpellIfMissing(Spell.QI_SHIELD);
            this.addSpellIfMissing(Spell.REALM_PRESSURE);
            if (this.getPhysique() == Physique.INNATE_SWORD_BODY) {
                this.addSpellIfMissing(Spell.SWORD_AURA);
            }
        }
        if (realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal()) {
            this.addSpellIfMissing(Spell.SWORD_FLIGHT);
            this.addSpellIfMissing(Spell.BIGU);
        }
        if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal()) {
            this.addSpellIfMissing(Spell.CORE_SELF_DESTRUCT);
        }
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal()) {
            this.addSpellIfMissing(Spell.NASCENT_SOUL_OUT_OF_BODY);
        }
        if (realm.ordinal() >= Realm.SOUL_FORMATION.ordinal()) {
            this.addSpellIfMissing(Spell.DIVINE_SENSE);
        }
        if (realm.ordinal() >= Realm.VOID_REFINING.ordinal()) {
            this.addSpellIfMissing(Spell.VOID_STEP);
            this.addSpellIfMissing(Spell.VOID_ESCAPE);
        }
        if (realm.ordinal() >= Realm.BODY_INTEGRATION.ordinal()) {
            this.addSpellIfMissing(Spell.DHARMA_BODY_MANIFESTATION);
        }
        if (realm.ordinal() >= Realm.TRUE_IMMORTAL.ordinal()) {
            this.addSpellIfMissing(Spell.QI_FLIGHT);
        }
    }

    private boolean isRealmAutomaticSpell(Spell spell, Realm realm) {
        if (spell == null || realm == Realm.MORTAL) {
            return false;
        }
        if (realm.ordinal() >= Realm.QI_REFINING.ordinal()) {
            if (spell == Spell.SPIRIT_VISION || spell == Spell.QI_TRANSFER || spell == Spell.QI_SHIELD || spell == Spell.REALM_PRESSURE) {
                return true;
            }
            if (spell == Spell.SWORD_AURA && this.getPhysique() == Physique.INNATE_SWORD_BODY) {
                return true;
            }
        }
        if (realm.ordinal() >= Realm.FOUNDATION_BUILDING.ordinal() && (spell == Spell.SWORD_FLIGHT || spell == Spell.BIGU)) {
            return true;
        }
        if (realm.ordinal() >= Realm.GOLDEN_CORE.ordinal() && spell == Spell.CORE_SELF_DESTRUCT) {
            return true;
        }
        if (realm.ordinal() >= Realm.NASCENT_SOUL.ordinal() && spell == Spell.NASCENT_SOUL_OUT_OF_BODY) {
            return true;
        }
        if (realm.ordinal() >= Realm.SOUL_FORMATION.ordinal() && spell == Spell.DIVINE_SENSE) {
            return true;
        }
        if (realm.ordinal() >= Realm.VOID_REFINING.ordinal() && (spell == Spell.VOID_STEP || spell == Spell.VOID_ESCAPE)) {
            return true;
        }
        if (realm.ordinal() >= Realm.BODY_INTEGRATION.ordinal() && spell == Spell.DHARMA_BODY_MANIFESTATION) {
            return true;
        }
        return realm.ordinal() >= Realm.TRUE_IMMORTAL.ordinal() && spell == Spell.QI_FLIGHT;
    }

    private void normalizeSpellIdsForRealm(Realm realm) {
        Spell spell;
        LinkedHashSet<String> normalized = new LinkedHashSet<String>();
        this.addRealmAutomaticSpells(realm);
        for (String id : this.spellIds) {
            spell = Spell.byId(id);
            if (spell == null || !this.isRealmAutomaticSpell(spell, realm)) continue;
            normalized.add(id);
        }
        for (String id : this.spellIds) {
            boolean mandatory;
            spell = Spell.byId(id);
            if (spell == null || realm == Realm.MORTAL || !(mandatory = this.isRealmAutomaticSpell(spell, realm)) && !NpcSpellCaster.isLearnableByNpc(spell)) continue;
            normalized.add(id);
        }
        this.spellIds.clear();
        this.spellIds.addAll(normalized);
    }

    private static List<Spell> buildLearnableSpellPool(Realm realm) {
        ItemTier targetTier = WanderingCultivatorEntity.spellTierForRealm(realm);
        List<Spell> pool = WanderingCultivatorEntity.collectLearnable(targetTier);
        if (pool.isEmpty() && targetTier == ItemTier.HIGH) {
            pool = WanderingCultivatorEntity.collectLearnable(ItemTier.MID);
        }
        if (pool.isEmpty()) {
            pool = WanderingCultivatorEntity.collectLearnable(ItemTier.LOW);
        }
        return pool;
    }

    private static List<Spell> collectLearnable(ItemTier tier) {
        return new ArrayList<Spell>(CultivationRandomPools.npcLearnableSpellsForTier(tier));
    }

    private void addImmortalSpells(int count) {
        ArrayList<Spell> immortalPool = new ArrayList<Spell>();
        for (Spell s : Spell.values()) {
            if (s.tier() != ItemTier.IMMORTAL || !NpcSpellCaster.isLearnableByNpc(s)) continue;
            immortalPool.add(s);
        }
        if (immortalPool.isEmpty()) {
            return;
        }
        Collections.shuffle(immortalPool, new Random(this.random.nextLong()));
        for (int i = 0; i < Math.min(count, immortalPool.size()); ++i) {
            this.addSpellIfMissing((Spell)((Object)immortalPool.get(i)));
        }
    }

    private static ItemTier techTierForRealm(Realm realm) {
        return CultivationRandomPools.techniqueTierForRealm(realm);
    }

    private static ItemTier spellTierForRealm(Realm realm) {
        return CultivationRandomPools.spellTierForRealm(realm);
    }

    private static int[] randomSpellCountRange(Realm realm) {
        int[] nArray;
        switch (realm) {
            default: {
                throw new IncompatibleClassChangeError();
            }
            case MORTAL: {
                int[] nArray2 = new int[2];
                nArray2[0] = 0;
                nArray = nArray2;
                nArray2[1] = 0;
                break;
            }
            case GOLDEN_CORE: 
            case SOUL_FORMATION: 
            case BODY_INTEGRATION: 
            case BODY_TEMPERING: 
            case QI_REFINING: {
                int[] nArray3 = new int[2];
                nArray3[0] = 1;
                nArray = nArray3;
                nArray3[1] = 3;
                break;
            }
            case FOUNDATION_BUILDING: 
            case NASCENT_SOUL: 
            case VOID_REFINING: 
            case MAHAYANA: 
            case TRIBULATION_TRANSCENDENCE: 
            case TRUE_IMMORTAL: 
            case LOOSE_IMMORTAL:
            case GREAT_EMPEROR: {
                int[] nArray4 = new int[2];
                nArray4[0] = 3;
                nArray = nArray4;
                nArray4[1] = 5;
            }
        }
        return nArray;
    }

    private static double scaleAttackByRealm(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL -> 2.0;
            case BODY_TEMPERING -> 3.0;
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
            case GREAT_EMPEROR -> 35.0;
            case LOOSE_IMMORTAL -> 28.0;
        };
    }

    protected void updateTrades() {
        this.removeForbiddenNaturalLootFromInventory();
        MerchantOffers gen = CultivatorTrades.generateOffers(this);
        if (this.offers == null) {
            this.offers = new MerchantOffers();
        }
        this.offers.clear();
        this.offers.addAll((Collection)gen);
    }

    public void regenerateOffers() {
        this.updateTrades();
    }

    protected void rewardTradeXp(@NotNull MerchantOffer offer) {
    }

    @Nullable
    public AgeableMob canContinueToUse(@NotNull ServerLevel level, @NotNull AgeableMob otherParent) {
        return null;
    }

    public boolean showProgressBar() {
        return false;
    }

    private boolean hasActiveCultivatorMenu(@Nullable Player player) {
        WanderingCultivatorMenu menu;
        if (player == null || !player.isAlive()) {
            return false;
        }
        if (player.level() != this.level()) {
            return false;
        }
        AbstractContainerMenu abstractContainerMenu = player.containerMenu;
        return abstractContainerMenu instanceof WanderingCultivatorMenu && (menu = (WanderingCultivatorMenu)abstractContainerMenu).getEntityId() == this.getId();
    }

    private void clearStaleTradingPlayer() {
        Player trader = this.getTradingPlayer();
        if (trader != null && !this.hasActiveCultivatorMenu(trader)) {
            this.setTradingPlayer(null);
        }
    }

    @Nullable
    private Player getBlockingTradingPlayer(@NotNull Player opener) {
        this.clearStaleTradingPlayer();
        Player trader = this.getTradingPlayer();
        return trader != null && trader != opener && this.hasActiveCultivatorMenu(trader) ? trader : null;
    }

    private void clearOpenerMenuState(@NotNull ServerPlayer opener) {
        if (this.hasActiveCultivatorMenu((Player)opener)) {
            opener.closeContainer();
        }
        if (this.getTradingPlayer() == opener) {
            this.setTradingPlayer(null);
        }
    }

    @NotNull
    public InteractionResult mobInteract(@NotNull Player player, @NotNull InteractionHand hand) {
        Level level;
        this.clearStaleTradingPlayer();
        if (!this.isAlive()) {
            return InteractionResult.sidedSuccess((boolean)this.level().isClientSide);
        }
        Player blockingTrader = this.getBlockingTradingPlayer(player);
        if (blockingTrader != null) {
            if (!this.level().isClientSide && player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.busy_trading"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (this.isNpcSoulState()) {
            if (!WanderingCultivatorEntity.canPlayerPerceiveNpcSoul(player)) {
                return InteractionResult.PASS;
            }
            if (this.level().isClientSide) {
                return InteractionResult.SUCCESS;
            }
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.soul_reaper_token.npc_soul_no_trade"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (this.isSectEnemy((Entity)player)) {
            this.setTarget((LivingEntity)player);
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.busy_fighting_player"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (this.isHostileToPlayer()) {
            if (player instanceof ServerPlayer) {
                ServerPlayer sp = (ServerPlayer)player;
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivator.busy_fighting_player"), true);
            }
            return InteractionResult.CONSUME;
        }
        if (!this.level().isClientSide && (level = this.level()) instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            if (this.hasSectMembership()) {
                SectSavedData.get(serverLevel).ensureNpcCoreAndTokens(this);
            }
        }
        this.regenerateOffers();
        if (player instanceof ServerPlayer) {
            WanderingCultivatorMenu menu;
            ServerPlayer sp = (ServerPlayer)player;
            this.clearOpenerMenuState(sp);
            NetworkHooks.openScreen((ServerPlayer)sp, (MenuProvider)new SimpleMenuProvider((containerId, playerInv, p) -> new WanderingCultivatorMenu(containerId, playerInv, this), this.getDisplayName()), buf -> buf.writeVarInt(this.getId()));
            AbstractContainerMenu abstractContainerMenu = sp.containerMenu;
            if (abstractContainerMenu instanceof WanderingCultivatorMenu && (menu = (WanderingCultivatorMenu)abstractContainerMenu).getEntityId() == this.getId()) {
                this.setTradingPlayer(player);
                if (this.getOffers() != null) {
                    ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncCultivatorOffersPacket(menu.containerId, this.getOffers()));
                }
                ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> sp), (Object)new SyncCultivatorInventoryPacket(menu.containerId, SyncCultivatorInventoryPacket.toTag(this.getInventory())));
            } else {
                this.setTradingPlayer(null);
            }
        }
        return InteractionResult.sidedSuccess((boolean)false);
    }

    private static boolean canPlayerPerceiveNpcSoul(@NotNull Player player) {
        return CultivationCapability.get(player).map(data -> data.isSpellEnabled(Spell.YIN_YANG_EYE)).orElse(false);
    }

    public void notifyTrade(@NotNull MerchantOffer offer) {
        super.notifyTrade(offer);
        SimpleContainer inv = this.getInventory();
        if (!offer.getResult().isEmpty()) {
            inv.addItem(offer.getResult().copy());
        }
        if (!offer.getCostA().isEmpty()) {
            inv.addItem(offer.getCostA().copy());
        }
        this.removeHeldSwordIfSold(offer.getResult());
    }

    @NotNull
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    public boolean isHeldSwordTradeResult(@NotNull ItemStack result) {
        ItemStack held = this.getMainHandItem();
        return !result.isEmpty() && held.getItem() instanceof SwordItem && ItemStack.isSameItemSameTags((ItemStack)held, (ItemStack)result);
    }

    public boolean removeHeldSwordIfSold(@NotNull ItemStack result) {
        if (!this.isHeldSwordTradeResult(result)) {
            return false;
        }
        this.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
        this.stopNpcSwordFlight(true);
        return true;
    }

    public void addAdditionalSaveData(@NotNull CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("realmOrd", ((Integer)this.entityData.get(DATA_REALM_ORD)).intValue());
        tag.putInt("subStageOrd", ((Integer)this.entityData.get(DATA_SUB_STAGE_ORD)).intValue());
        tag.putInt("looseImmortalTribulations", ((Integer)this.entityData.get(DATA_LOOSE_IMMORTAL_TRIBULATIONS)).intValue());
        tag.putString("techniqueId", (String)this.entityData.get(DATA_TECHNIQUE_ID));
        this.repairSkinVariantGenderMismatch();
        tag.putInt("skinVariant", this.getSkinVariant());
        tag.putBoolean("difuReaper", ((Boolean)this.entityData.get(DATA_DIFU_REAPER)).booleanValue());
        tag.putBoolean("npcSoulState", ((Boolean)this.entityData.get(DATA_NPC_SOUL_STATE)).booleanValue());
        tag.putInt("npcSoulTicks", this.npcSoulTicks);
        tag.putInt("surnameIdx", ((Integer)this.entityData.get(DATA_SURNAME_IDX)).intValue());
        tag.putInt("givenIdx", ((Integer)this.entityData.get(DATA_GIVEN_IDX)).intValue());
        tag.putInt("gender", this.getGender());
        tag.putBoolean("soulReaperTokenTradeAvailable", this.soulReaperTokenTradeAvailable);
        tag.putLong("currentQi", this.currentQi);
        tag.putLong("maxQi", this.maxQi);
        ListTag spellList = new ListTag();
        for (String id : this.spellIds) {
            spellList.add(StringTag.valueOf(id));
        }
        tag.put("spells", (Tag)spellList);
        tag.put("ExtInventory", (Tag)this.extInventory.createTag());
        tag.putString("spiritRoot", (String)this.entityData.get(DATA_SPIRIT_ROOT_ID));
        tag.putString("physique", (String)this.entityData.get(DATA_PHYSIQUE_ID));
        tag.putString("identity", (String)this.entityData.get(DATA_IDENTITY_ID));
        tag.putString("sectId", (String)this.entityData.get(DATA_SECT_ID));
        tag.putString("sectName", (String)this.entityData.get(DATA_SECT_NAME));
        tag.putString("sectRole", (String)this.entityData.get(DATA_SECT_ROLE));
        WanderingCultivatorEntity.putOptionalBlockPos(tag, "sectHomePos", this.sectHomePos);
        WanderingCultivatorEntity.putOptionalBlockPos(tag, "sectBedPos", this.sectBedPos);
        WanderingCultivatorEntity.putOptionalBlockPos(tag, "sectCushionPos", this.sectCushionPos);
        WanderingCultivatorEntity.putOptionalBlockPos(tag, "sectCorePos", this.sectCorePos);
        WanderingCultivatorEntity.putOptionalBlockPos(tag, "sectReceptionGuardPos", this.sectReceptionGuardPos);
        tag.putBoolean("sectTemporaryTokensSeeded", this.sectTemporaryTokensSeeded);
        tag.putString("zhenyuanCsv", (String)this.entityData.get(DATA_ZHENYUAN_CSV));
        tag.putInt("alchemyRankOrd", ((Integer)this.entityData.get(DATA_ALCHEMY_RANK_ORD)).intValue());
        tag.putInt("refiningRankOrd", ((Integer)this.entityData.get(DATA_REFINING_RANK_ORD)).intValue());
        tag.putBoolean("npcSwordFlightActive", this.isNpcSwordFlightActive());
        tag.putInt("npcVoidEscapeActiveTicks", this.npcVoidEscapeActiveTicks);
        tag.putBoolean("npcVoidEscapePreviousNoGravity", this.npcVoidEscapePreviousNoGravity);
        tag.putBoolean("npcVoidEscapePreviousNoPhysics", this.npcVoidEscapePreviousNoPhysics);
    }

    public void readAdditionalSaveData(@NotNull CompoundTag tag) {
        int i;
        super.readAdditionalSaveData(tag);
        this.entityData.set(DATA_REALM_ORD, tag.getInt("realmOrd"));
        this.entityData.set(DATA_SUB_STAGE_ORD, tag.getInt("subStageOrd"));
        int looseLevel = tag.contains("looseImmortalTribulations", 3) ? tag.getInt("looseImmortalTribulations") : 0;
        this.entityData.set(DATA_LOOSE_IMMORTAL_TRIBULATIONS, (this.getRealm() == Realm.LOOSE_IMMORTAL ? Math.max(1, Math.min(9, looseLevel)) : 0));
        this.entityData.set(DATA_TECHNIQUE_ID, tag.getString("techniqueId"));
        this.entityData.set(DATA_DIFU_REAPER, tag.getBoolean("difuReaper"));
        this.entityData.set(DATA_NPC_SOUL_STATE, tag.getBoolean("npcSoulState"));
        this.npcSoulTicks = Math.max(0, tag.getInt("npcSoulTicks"));
        this.entityData.set(DATA_SURNAME_IDX, tag.getInt("surnameIdx"));
        this.entityData.set(DATA_GIVEN_IDX, tag.getInt("givenIdx"));
        int loadedGender = tag.contains("gender", 3) ? WanderingCultivatorEntity.safeGender(tag.getInt("gender")) : 1;
        this.entityData.set(DATA_GENDER, loadedGender);
        int loadedSkinVariant = tag.contains("skinVariant", 3) ? tag.getInt("skinVariant") : WanderingCultivatorEntity.randomSkinVariantForGender(loadedGender, this.random);
        this.entityData.set(DATA_SKIN_VARIANT, WanderingCultivatorEntity.normalizeSkinVariantForGender(loadedSkinVariant, loadedGender));
        this.repairSkinVariantGenderMismatch();
        this.soulReaperTokenTradeAvailable = tag.contains("soulReaperTokenTradeAvailable", 1) ? tag.getBoolean("soulReaperTokenTradeAvailable") : this.isDifuReaper();
        this.currentQi = tag.getLong("currentQi");
        this.maxQi = tag.getLong("maxQi");
        this.entityData.set(DATA_MAX_QI, ((int)Math.min(Integer.MAX_VALUE, this.maxQi)));
        this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
        this.spellIds.clear();
        if (tag.contains("spells", 9)) {
            ListTag spellList = tag.getList("spells", 8);
            for (i = 0; i < spellList.size(); ++i) {
                this.spellIds.add(spellList.getString(i));
            }
        }
        if (tag.contains("ExtInventory", 9)) {
            this.extInventory.fromTag(tag.getList("ExtInventory", 10));
        } else {
            SimpleContainer oldInv = super.getInventory();
            for (i = 0; i < oldInv.getContainerSize(); ++i) {
                ItemStack stack = oldInv.getItem(i);
                if (stack.isEmpty()) continue;
                this.extInventory.addItem(stack.copy());
            }
            oldInv.clearContent();
        }
        this.removeForbiddenNaturalLootFromInventory();
        if (tag.contains("spiritRoot", 8)) {
            String rootId = tag.getString("spiritRoot");
            Physique legacy = Physique.fromLegacySpiritRootId(rootId);
            if (legacy != null) {
                this.entityData.set(DATA_SPIRIT_ROOT_ID, SpiritRoot.NONE.id());
                this.entityData.set(DATA_PHYSIQUE_ID, legacy.id());
            } else {
                this.entityData.set(DATA_SPIRIT_ROOT_ID, rootId);
            }
        }
        if (tag.contains("physique", 8)) {
            this.entityData.set(DATA_PHYSIQUE_ID, tag.getString("physique"));
        }
        if (tag.contains("identity", 8)) {
            this.entityData.set(DATA_IDENTITY_ID, tag.getString("identity"));
        }
        this.entityData.set(DATA_SECT_ID, (tag.contains("sectId", 8) ? tag.getString("sectId") : ""));
        this.entityData.set(DATA_SECT_NAME, (tag.contains("sectName", 8) ? tag.getString("sectName") : ""));
        this.entityData.set(DATA_SECT_ROLE, (tag.contains("sectRole", 8) ? tag.getString("sectRole") : SectRole.NONE.id()));
        this.sectHomePos = WanderingCultivatorEntity.readOptionalBlockPos(tag, "sectHomePos");
        this.sectBedPos = WanderingCultivatorEntity.readOptionalBlockPos(tag, "sectBedPos");
        this.sectCushionPos = WanderingCultivatorEntity.readOptionalBlockPos(tag, "sectCushionPos");
        this.sectCorePos = WanderingCultivatorEntity.readOptionalBlockPos(tag, "sectCorePos");
        this.sectReceptionGuardPos = WanderingCultivatorEntity.readOptionalBlockPos(tag, "sectReceptionGuardPos");
        this.sectTemporaryTokensSeeded = tag.getBoolean("sectTemporaryTokensSeeded");
        this.normalizeSpellIdsForRealm(this.getRealm());
        if (this.isDifuReaper()) {
            this.ensureDifuReaperSpells();
        }
        this.ensureSwordForKnownSwordSpell(this.getRealm());
        this.syncSpellIdsToData();
        this.setNoGravity(this.canFly());
        if (tag.contains("zhenyuanCsv", 8)) {
            this.entityData.set(DATA_ZHENYUAN_CSV, tag.getString("zhenyuanCsv"));
        }
        if (tag.contains("alchemyRankOrd", 3)) {
            this.entityData.set(DATA_ALCHEMY_RANK_ORD, tag.getInt("alchemyRankOrd"));
        }
        if (tag.contains("refiningRankOrd", 3)) {
            this.entityData.set(DATA_REFINING_RANK_ORD, tag.getInt("refiningRankOrd"));
        }
        if (tag.contains("npcSwordFlightActive", 1)) {
            this.setNpcSwordFlightActive(tag.getBoolean("npcSwordFlightActive"));
        }
        this.npcVoidEscapeActiveTicks = tag.contains("npcVoidEscapeActiveTicks", 3) ? Math.max(0, tag.getInt("npcVoidEscapeActiveTicks")) : 0;
        this.npcVoidEscapePreviousNoGravity = tag.getBoolean("npcVoidEscapePreviousNoGravity");
        this.npcVoidEscapePreviousNoPhysics = tag.getBoolean("npcVoidEscapePreviousNoPhysics");
        this.setNoGravity(this.canFly() || this.isNpcSwordFlightActive());
        if (this.isNpcSoulState()) {
            this.setNoAi(false);
            this.setNoGravity(this.canFly());
            if (this.currentQi <= 0L && this.maxQi > 0L) {
                this.currentQi = this.maxQi;
                this.entityData.set(DATA_CURRENT_QI, ((int)Math.min(Integer.MAX_VALUE, this.currentQi)));
            }
        }
        if (this.isNpcVoidEscapeActive()) {
            this.applyNpcVoidEscapePhase();
        }
        this.setPersistenceRequired();
        this.normalizeGroundMovementSpeedAttribute();
    }

    private void tickSectRoutine() {
        if (this.isSectReceptionGuard()) {
            return;
        }
        if (!this.hasSectMembership() || this.isTradingFreeze()) {
            return;
        }
        if (this.sectHomePos == null && this.sectBedPos == null && this.sectCushionPos == null) {
            return;
        }
        long dayTime = this.level().getDayTime() % 24000L;
        boolean night = dayTime >= 12500L && dayTime <= 23500L;
        LivingEntity target = this.getTarget();
        if (target != null) {
            if (night && this.shouldReleaseTargetForSectSleep(target)) {
                this.clearTargetForSectSleep();
            } else {
                return;
            }
        }
        if (night && this.canAttemptImmediateSectBedSleep()) {
            this.tickSectNightRoutine();
            return;
        }
        if (this.sectRoutineCooldown > 0) {
            --this.sectRoutineCooldown;
            return;
        }
        this.sectRoutineCooldown = 20 + this.random.nextInt(30);
        if (night) {
            this.tickSectNightRoutine();
        } else {
            this.tickSectDayRoutine();
        }
    }

    private void tickSectNightRoutine() {
        BlockPos bed;
        if (this.isPassenger()) {
            this.stopRiding();
        }
        if ((bed = this.findUsableBed()) != null) {
            BlockPos head;
            BlockPos foot = this.normalizeBedFootPos(bed);
            BlockPos blockPos = head = foot == null ? null : this.bedHeadPos(foot);
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

    private boolean canReachSectBedForSleep(BlockPos foot, BlockPos head) {
        return this.distanceToSqr(Vec3.atCenterOf((Vec3i)foot)) <= 6.25 || this.distanceToSqr(Vec3.atCenterOf((Vec3i)head)) <= 6.25;
    }

    private boolean canAttemptImmediateSectBedSleep() {
        BlockPos bed = this.findUsableBed();
        BlockPos foot = this.normalizeBedFootPos(bed);
        BlockPos head = foot == null ? null : this.bedHeadPos(foot);
        return foot != null && head != null && this.canReachSectBedForSleep(foot, head);
    }

    private boolean shouldReleaseTargetForSectSleep(LivingEntity target) {
        if (target instanceof Player) {
            return false;
        }
        if (this.getLastHurtByMob() == target && this.tickCount - this.getLastHurtByMobTimestamp() < 200) {
            return false;
        }
        BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        BlockPos head = foot == null ? null : this.bedHeadPos(foot);
        return foot != null && head != null && this.canReachSectBedForSleep(foot, head) && this.distanceToSqr((Entity)target) > 144.0;
    }

    private void clearTargetForSectSleep() {
        super.setTarget(null);
        this.combatStartTick = -1;
        this.setAggressive(false);
        this.getNavigation().stop();
        this.stopUsingItem();
        this.stopNpcSwordFlight(true);
        this.setNoGravity(false);
    }

    private void sleepOnSectBed(BlockPos foot, BlockPos head) {
        this.stopSectSleepMotion();
        Optional currentSleepPos = this.getSleepingPos();
        if (!this.isSleeping() || currentSleepPos.isEmpty() || !((BlockPos)currentSleepPos.get()).equals((Object)head)) {
            if (this.isSleeping()) {
                this.stopSleeping();
            }
            this.startSleeping(head);
        }
        if (!Objects.equals(this.sectBedPos, foot)) {
            this.updateSectBedClaim(foot);
        }
        this.snapToSectBed(head);
    }

    private void tickSectTaskAutomation() {
        if (this.isSectReceptionGuard()) {
            return;
        }
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze() || this.isSleeping()) {
            return;
        }
        if (this.sectTaskCooldown > 0) {
            --this.sectTaskCooldown;
            return;
        }
        this.sectTaskCooldown = 220 + this.random.nextInt(260);
        Level level = this.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        SectSavedData sectData = SectSavedData.get(serverLevel);
        sectData.ensureNpcCoreAndTokens(this);
        sectData.ensureBasicTask(this.getSectId(), this);
        sectData.tickNpcTaskAutomation(this);
    }

    private void tickSectEnemyAwareness() {
        Level level;
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze() || this.tickCount % 40 != 0 || !((level = this.level()) instanceof ServerLevel)) {
            return;
        }
        ServerLevel serverLevel = (ServerLevel)level;
        LivingEntity enemy = SectSavedData.get(serverLevel).findNearestEnemyOfSect(serverLevel, this.getSectId(), this.getSectRole(), (LivingEntity)this, 64.0);
        if (enemy != null) {
            this.setTarget(enemy);
        }
    }

    private void tickSectDayRoutine() {
        if (this.isSleeping()) {
            this.stopSleeping();
        }
        if (this.isPassenger()) {
            return;
        }
        if (this.random.nextInt(4) != 0) {
            return;
        }
        BlockPos cushion = this.findUsableCushion();
        if (cushion == null) {
            return;
        }
        if (this.distanceToSqr(Vec3.atCenterOf((Vec3i)cushion)) <= 2.25) {
            this.sitOnCushion(cushion);
        } else {
            this.moveToward(cushion, 0.9);
        }
    }

    @Nullable
    private BlockPos findUsableBed() {
        BlockPos found;
        if (this.sectBedPos != null && WanderingCultivatorEntity.isUsableBedState(this.level().getBlockState(this.sectBedPos))) {
            return this.sectBedPos;
        }
        if (this.sectBedPos != null) {
            this.updateSectBedClaim(null);
        }
        if ((found = this.findNearbyBlock(this.sectHomePos != null ? this.sectHomePos : this.blockPosition(), 10, WanderingCultivatorEntity::isUsableBedState)) != null) {
            this.updateSectBedClaim(found);
        }
        return found;
    }

    private static boolean isUsableBedState(BlockState state) {
        return state.is(BlockTags.BEDS) && (!state.hasProperty((Property)BedBlock.PART) || state.getValue(BedBlock.PART) == BedPart.FOOT);
    }

    @Nullable
    private BlockPos normalizeBedFootPos(@Nullable BlockPos pos) {
        if (pos == null) {
            return null;
        }
        BlockState state = this.level().getBlockState(pos);
        if (!state.is(BlockTags.BEDS)) {
            return null;
        }
        if (!state.hasProperty((Property)BedBlock.PART) || !state.hasProperty((Property)BedBlock.FACING)) {
            return pos.east();
        }
        Direction facing = (Direction)state.getValue(BedBlock.FACING);
        if (state.getValue(BedBlock.PART) == BedPart.FOOT) {
            return pos.east();
        }
        BlockPos foot = pos.relative(facing.getOpposite());
        BlockState footState = this.level().getBlockState(foot);
        if (footState.is(BlockTags.BEDS) && footState.hasProperty((Property)BedBlock.PART) && footState.hasProperty((Property)BedBlock.FACING) && footState.getValue(BedBlock.PART) == BedPart.FOOT && footState.getValue(BedBlock.FACING) == facing) {
            return foot.east();
        }
        return null;
    }

    @Nullable
    private BlockPos bedHeadPos(BlockPos foot) {
        BlockState footState = this.level().getBlockState(foot);
        if (!footState.is(BlockTags.BEDS)) {
            return null;
        }
        if (!footState.hasProperty((Property)BedBlock.PART) || !footState.hasProperty((Property)BedBlock.FACING)) {
            return foot.east();
        }
        if (footState.getValue(BedBlock.PART) != BedPart.FOOT) {
            return null;
        }
        Direction facing = (Direction)footState.getValue(BedBlock.FACING);
        BlockPos head = foot.relative(facing);
        BlockState headState = this.level().getBlockState(head);
        if (headState.is(BlockTags.BEDS) && headState.hasProperty((Property)BedBlock.PART) && headState.hasProperty((Property)BedBlock.FACING) && headState.getValue(BedBlock.PART) == BedPart.HEAD && headState.getValue(BedBlock.FACING) == facing) {
            return head.east();
        }
        return null;
    }

    private void tickSectSleepingBedLock() {
        BlockPos head;
        boolean night;
        if (!this.isSleeping()) {
            return;
        }
        if (this.isNpcSoulState() || !this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze()) {
            this.stopSleeping();
            return;
        }
        long dayTime = this.level().getDayTime() % 24000L;
        boolean bl = night = dayTime >= 12500L && dayTime <= 23500L;
        if (!night) {
            this.stopSleeping();
            return;
        }
        BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        if (foot == null) {
            Optional<BlockPos> sleepingPos = this.getSleepingPos();
            foot = sleepingPos.flatMap(pos -> Optional.ofNullable(this.normalizeBedFootPos(pos))).orElse(null);
        }
        BlockPos blockPos = head = foot == null ? null : this.bedHeadPos(foot);
        if (foot == null || head == null) {
            this.updateSectBedClaim(null);
            this.stopSleeping();
            return;
        }
        this.sleepOnSectBed(foot, head);
    }

    private void snapToSectBed(BlockPos head) {
        double z;
        double y;
        BlockState headState = this.level().getBlockState(head);
        Direction facing = headState.hasProperty((Property)BedBlock.FACING) ? (Direction)headState.getValue(BedBlock.FACING) : this.getDirection();
        float yaw = facing.toYRot();
        double x = (double)head.getX() + 0.5;
        if (this.distanceToSqr(x, y = (double)head.getY() + 0.6875, z = (double)head.getZ() + 0.5) > 0.01 || Math.abs(this.getXRot()) > 0.001f || Math.abs(Mth.degreesDifference(this.getYRot(), yaw)) > 0.001f) {
            this.moveTo(x, y, z, yaw, 0.0f);
        }
        this.setYRot(yaw);
        this.setYHeadRot(yaw);
        this.setYBodyRot(yaw);
        this.setXRot(0.0f);
        this.xxa = 0.0f;
        this.yya = 0.0f;
        this.zza = 0.0f;
    }

    private void stopSectSleepMotion() {
        this.getNavigation().stop();
        this.stopUsingItem();
        this.stopNpcSwordFlight(true);
        this.stopNpcVoidEscapePhase();
        this.setNoGravity(false);
        this.noPhysics = false;
        this.setSpeed(0.0f);
        this.setDeltaMovement(0.0, 0.0, 0.0);
        this.setNoGravity(false);
        this.fallDistance = 0.0f;
        this.hasImpulse = true;
    }

    @Nullable
    private BlockPos findUsableCushion() {
        BlockPos found;
        if (this.sectCushionPos != null) {
            if (this.level().getBlockState(this.sectCushionPos).getBlock() instanceof CushionBlock && this.ownsCurrentCushionClaim(this.sectCushionPos)) {
                return this.sectCushionPos;
            }
            this.updateSectCushionClaim(null);
        }
        if ((found = this.findNearbyUnclaimedCushion(this.sectHomePos != null ? this.sectHomePos : this.blockPosition(), 10)) != null) {
            this.updateSectCushionClaim(found);
        }
        return found;
    }

    private void updateSectBedClaim(@Nullable BlockPos next) {
        BlockPos safe;
        BlockPos blockPos = safe = next == null ? null : next.east();
        if (Objects.equals(this.sectBedPos, safe)) {
            return;
        }
        this.sectBedPos = safe;
        this.syncSectRoutineClaims();
    }

    private void updateSectCushionClaim(@Nullable BlockPos next) {
        BlockPos safe;
        BlockPos blockPos = safe = next == null ? null : next.east();
        if (Objects.equals(this.sectCushionPos, safe)) {
            return;
        }
        this.sectCushionPos = safe;
        this.syncSectRoutineClaims();
    }

    private void syncSectRoutineClaims() {
        ServerLevel serverLevel;
        block3: {
            block2: {
                Level level = this.level();
                if (!(level instanceof ServerLevel)) break block2;
                serverLevel = (ServerLevel)level;
                if (this.hasSectMembership()) break block3;
            }
            return;
        }
        SectSavedData.get(serverLevel).updateNpcRoutineClaims(this, this.sectBedPos, this.sectCushionPos);
    }

    private void tickSectReceptionGuardCoreLink() {
        if (!this.isSectReceptionGuard() || !this.hasSectMembership() || this.tickCount % 40 != 0) {
            return;
        }
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel serverLevel = (ServerLevel)level;
            SectSavedData.get(serverLevel).ensureNpcCoreAndTokens(this);
        }
    }

    private void tickSectReceptionGuardAnchor() {
        Vec3 anchor;
        double distance;
        if (!this.isSectReceptionGuard() || this.sectReceptionGuardPos == null) {
            return;
        }
        if (this.getTarget() != null || this.isTradingFreeze() || this.isSleeping()) {
            return;
        }
        if (SoulHookHandler.isActionLocked((Entity)this) || SpiritLockHandler.isEntityLocked((Entity)this)) {
            return;
        }
        if (this.isPassenger()) {
            this.stopRiding();
        }
        if ((distance = this.distanceToSqr(anchor = Vec3.atBottomCenterOf((Vec3i)this.sectReceptionGuardPos))) > 784.0) {
            this.getNavigation().stop();
            this.moveTo(anchor.x, anchor.y, anchor.z, this.getYRot(), this.getXRot());
            this.setDeltaMovement(0.0, 0.0, 0.0);
            this.setNoGravity(false);
            return;
        }
        if (distance > 9.0) {
            this.moveToward(this.sectReceptionGuardPos, 1.0);
            return;
        }
        this.getNavigation().stop();
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(0.0, this.isNoGravity() ? 0.0 : movement.y, 0.0);
        this.setNoGravity(false);
    }

    @Nullable
    private BlockPos findNearbyUnclaimedCushion(BlockPos center, int radius) {
        if (center == null) {
            return null;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 3; ++y) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    pos.set(center.getX() + dx, center.getY() + y, center.getZ() + dz);
                    if (!(this.level().getBlockState((BlockPos)pos).getBlock() instanceof CushionBlock) || this.isCushionClaimedByOther((BlockPos)pos)) continue;
                    return pos.east();
                }
            }
        }
        return null;
    }

    private boolean ownsCurrentCushionClaim(BlockPos cushion) {
        for (WanderingCultivatorEntity other : this.nearbySectCultivators(cushion)) {
            if (other == this || !other.isAlive() || !cushion.equals((Object)other.sectCushionPos) || other.getUUID().compareTo(this.getUUID()) >= 0) continue;
            return false;
        }
        return true;
    }

    private boolean isCushionClaimedByOther(BlockPos cushion) {
        for (WanderingCultivatorEntity other : this.nearbySectCultivators(cushion)) {
            if (other == this || !other.isAlive() || !cushion.equals((Object)other.sectCushionPos)) continue;
            return true;
        }
        return false;
    }

    private List<WanderingCultivatorEntity> nearbySectCultivators(BlockPos center) {
        return this.level().getEntitiesOfClass(WanderingCultivatorEntity.class, new AABB(center).inflate(32.0, 8.0, 32.0), npc -> npc != this && npc.hasSectMembership() && this.getSectId().equals(npc.getSectId()));
    }

    @Nullable
    private BlockPos findNearbyBlock(BlockPos center, int radius, Predicate<BlockState> predicate) {
        if (center == null) {
            return null;
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = -2; y <= 3; ++y) {
            for (int dx = -radius; dx <= radius; ++dx) {
                for (int dz = -radius; dz <= radius; ++dz) {
                    pos.set(center.getX() + dx, center.getY() + y, center.getZ() + dz);
                    if (!predicate.test(this.level().getBlockState((BlockPos)pos))) continue;
                    return pos.east();
                }
            }
        }
        return null;
    }

    private void sitOnCushion(BlockPos cushion) {
        List<SeatEntity> existing = this.level().getEntitiesOfClass(SeatEntity.class, new AABB(cushion).inflate(0.5));
        for (SeatEntity seat : existing) {
            if (seat.getPassengers().isEmpty()) continue;
            return;
        }
        SeatEntity seat = new SeatEntity(this.level(), cushion);
        this.level().addFreshEntity((Entity)seat);
        this.startRiding(seat, true);
    }

    private void moveToward(BlockPos pos, double speed) {
        this.getNavigation().moveTo((double)pos.getX() + 0.5, (double)pos.getY(), (double)pos.getZ() + 0.5, speed);
    }

    private static void putOptionalBlockPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.putLong(key, pos.asLong());
        }
    }

    @Nullable
    private static BlockPos readOptionalBlockPos(CompoundTag tag, String key) {
        return tag.contains(key, 4) ? BlockPos.of((long)tag.getLong(key)) : null;
    }

    public void tick() {
        super.tick();
        if (!this.level().isClientSide && !this.isNpcSoulState()) {
            this.tickSectSleepMaintenance();
        }
    }

    public void aiStep() {
        LivingEntity target;
        boolean tradingFrozen;
        super.aiStep();
        if (this.level().isClientSide) {
            return;
        }
        this.repairSkinVariantGenderMismatch();
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
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
        if (tradingFrozen = this.isTradingFreeze()) {
            LivingEntity frozenTarget = this.getTarget();
            if (frozenTarget != null) {
                if (!(frozenTarget instanceof Player)) {
                    this.suspendedFreezeTargetUuid = frozenTarget.getUUID();
                }
                super.setTarget(null);
                this.combatStartTick = -1;
            }
        } else if (this.wasTradingFrozen && this.suspendedFreezeTargetUuid != null) {
            LivingEntity resumedTarget;
            ServerLevel sl;
            Entity resumed;
            Level level;
            if (this.getTarget() == null && (level = this.level()) instanceof ServerLevel && (resumed = (sl = (ServerLevel)level).getEntity(this.suspendedFreezeTargetUuid)) instanceof LivingEntity && (resumedTarget = (LivingEntity)resumed).isAlive() && this.canTargetUnderYinYangRules(resumedTarget)) {
                this.setTarget(resumedTarget);
            }
            this.suspendedFreezeTargetUuid = null;
        }
        this.wasTradingFrozen = tradingFrozen;
        if (SoulHookHandler.isActionLocked((Entity)this)) {
            super.setTarget(null);
            this.combatStartTick = -1;
            this.getNavigation().stop();
            this.stopUsingItem();
            this.setDeltaMovement(0.0, 0.0, 0.0);
            this.fallDistance = 0.0f;
            return;
        }
        if (this.tickCount % 5 == 0 && NpcSpellCaster.trySelfRescue(this)) {
            return;
        }
        NpcPassiveSpellHandler.tick(this);
        boolean npcVoidEscaping = this.tickNpcVoidEscapePhase();
        if (!npcVoidEscaping) {
            this.tickNpcCombatMobility();
        }
        if ((target = this.getTarget()) != null && this.combatStartTick < 0) {
            this.combatStartTick = this.tickCount;
        } else if (target == null && this.combatStartTick >= 0) {
            this.combatStartTick = -1;
        }
        if (this.tickCount % 20 == 0 && this.currentQi < this.maxQi && PhysiqueBonusHelper.canCultivate(this.getPhysique())) {
            long perSecond = this.getNaturalQiRecoveryPerSecond();
            this.currentQi = Math.min(this.maxQi, this.currentQi + perSecond);
        }
        if (this.tickCount % 10 == 0) {
            int curInt = (int)Math.min(Integer.MAX_VALUE, this.currentQi);
            int maxInt = (int)Math.min(Integer.MAX_VALUE, this.maxQi);
            if ((Integer)this.entityData.get(DATA_CURRENT_QI) != curInt) {
                this.entityData.set(DATA_CURRENT_QI, curInt);
            }
            if ((Integer)this.entityData.get(DATA_MAX_QI) != maxInt) {
                this.entityData.set(DATA_MAX_QI, maxInt);
            }
        }
        if (this.tickCount % 20 == 0 && this.maxQi > 0L && this.currentQi * 4L < this.maxQi) {
            this.tryStartEatingSpiritStone();
        }
        if (this.isUsingItem() && this.getUseItem().getItem() instanceof SpiritStoneItem && this.getUseItemRemainingTicks() <= 1) {
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
            Vec3 dm = this.getDeltaMovement();
            this.setDeltaMovement(0.0, this.isNoGravity() ? 0.0 : dm.y, 0.0);
            this.setNoGravity(false);
        } else if (receptionGuard) {
            this.tickSectReceptionGuardAnchor();
        }
        if (this.isSleeping()) {
            this.tickSectSleepingBedLock();
        }
    }

    private void tickSectSleepMaintenance() {
        BlockPos head;
        boolean night;
        if (this.isSleeping()) {
            this.tickSectSleepingBedLock();
            return;
        }
        if (!this.hasSectMembership() || this.getTarget() != null || this.isTradingFreeze()) {
            return;
        }
        long dayTime = this.level().getDayTime() % 24000L;
        boolean bl = night = dayTime >= 12500L && dayTime <= 23500L;
        if (!night) {
            return;
        }
        BlockPos foot = this.normalizeBedFootPos(this.sectBedPos);
        BlockPos blockPos = head = foot == null ? null : this.bedHeadPos(foot);
        if (foot != null && head != null && this.canReachSectBedForSleep(foot, head)) {
            this.sleepOnSectBed(foot, head);
        }
    }

    private void tickNpcSoulState() {
        int mobClearInterval;
        ++this.npcSoulTicks;
        if (this.isNoAi() && !TimeStasisHandler.isEntityStopped((Entity)this)) {
            this.setNoAi(false);
        }
        this.clearNpcSoulAggro();
        boolean locked = SpiritLockHandler.isEntityLocked((Entity)this) || SoulHookHandler.isActionLocked((Entity)this) || RealmPressureHandler.isSuppressed((LivingEntity)this);
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
        int n = mobClearInterval = this.npcSoulTicks < 200 ? 20 : 200;
        if ((this.tickCount + this.getId()) % mobClearInterval == 0) {
            this.clearMobsTargetingNpcSoul();
        }
        if (this.tickCount % 10 == 0) {
            this.updateDisplayName();
        }
    }

    public boolean isHostileToPlayer() {
        LivingEntity target = this.getTarget();
        if (target instanceof Player && target.isAlive()) {
            return true;
        }
        LivingEntity lastHurtBy = this.getLastHurtByMob();
        return lastHurtBy instanceof Player && lastHurtBy.isAlive() && this.tickCount - this.getLastHurtByMobTimestamp() < 200;
    }

    private boolean isSectEnemy(Entity entity) {
        ServerLevel serverLevel;
        Level level;
        return this.hasSectMembership() && entity != null && (level = this.level()) instanceof ServerLevel && SectSavedData.get(serverLevel = (ServerLevel)level).isEnemyOfSect(this.getSectId(), entity);
    }

    public boolean isTradingFreeze() {
        this.clearStaleTradingPlayer();
        Player trader = this.getTradingPlayer();
        return trader != null && this.hasActiveCultivatorMenu(trader) && !this.isHostileToPlayer();
    }

    private boolean isNpcVoidEscapeActive() {
        return this.npcVoidEscapeActiveTicks > 0;
    }

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
        if (SpiritLockHandler.isEntityLocked((Entity)this)) {
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
        boolean restoreNoGravity = !RealmPressureHandler.isSuppressed((LivingEntity)this) && !SpiritLockHandler.isEntityLocked((Entity)this) && (this.npcVoidEscapePreviousNoGravity || this.canFly() || this.isNpcSwordFlightActive());
        this.setNoGravity(restoreNoGravity);
        this.fallDistance = 0.0f;
        this.npcVoidEscapePreviousNoGravity = false;
        this.npcVoidEscapePreviousNoPhysics = false;
    }

    private void updateNpcVoidEscapeDrift() {
        Level level;
        this.updateIdleFlight();
        if (this.tickCount % 10 == 0 && (level = this.level()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + (double)this.getBbHeight() * 0.5, this.getZ(), 3, 0.28, 0.35, 0.28, 0.015);
        }
    }

    private void tickNpcCombatMobility() {
        if (this.npcVoidStepCooldownTicks > 0) {
            --this.npcVoidStepCooldownTicks;
        }
        if (this.npcVoidEscapeCooldownTicks > 0) {
            --this.npcVoidEscapeCooldownTicks;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return;
        }
        if (this.isNpcVoidEscapeActive()) {
            return;
        }
        if (this.isTradingFreeze() || SpiritLockHandler.isEntityLocked((Entity)this)) {
            return;
        }
        LivingEntity target = this.getTarget();
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

    private boolean tryNpcVoidEscape(LivingEntity threat) {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return false;
        }
        if (this.npcVoidEscapeCooldownTicks > 0) {
            return false;
        }
        if (!this.spellIds.contains(Spell.VOID_ESCAPE.id())) {
            return false;
        }
        if (this.getHealth() > this.getMaxHealth() * 0.32f) {
            return false;
        }
        long cost = Math.max(1L, NpcSpellCaster.generalQiCost(this, 400L));
        if (this.getCurrentQi() < cost) {
            return false;
        }
        this.deductQi(cost);
        this.npcVoidEscapeCooldownTicks = 360;
        this.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 160, 0, false, false, true));
        this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 160, 1, false, false, true));
        this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 80, 0, false, false, true));
        this.teleportAwayFromThreat(threat);
        this.startNpcVoidEscapePhase();
        super.setTarget(null);
        this.combatStartTick = -1;
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ENDERMAN_TELEPORT, this.getSoundSource(), 0.8f, 0.7f);
            sl.sendParticles((ParticleOptions)ParticleTypes.REVERSE_PORTAL, this.getX(), this.getY() + (double)this.getBbHeight() * 0.5, this.getZ(), 24, 0.35, 0.5, 0.35, 0.05);
        }
        return true;
    }

    private void teleportAwayFromThreat(LivingEntity threat) {
        Vec3 away = this.position().multiply(threat.position());
        if (away.lengthSqr() < 1.0E-4) {
            away = new Vec3(this.getRandom().nextDouble() - 0.5, 0.0, this.getRandom().nextDouble() - 0.5);
        }
        Vec3 horizontalAway = new Vec3(away.x, 0.0, away.z).normalize();
        for (int i = 0; i < 8; ++i) {
            double spread = (this.getRandom().nextDouble() - 0.5) * 1.6;
            double cos = Math.cos(spread);
            double sin = Math.sin(spread);
            double dx = horizontalAway.x * cos - horizontalAway.z * sin;
            double dz = horizontalAway.x * sin + horizontalAway.z * cos;
            double distance = 12.0 + this.getRandom().nextDouble() * 10.0;
            double x = this.getX() + dx * distance;
            double z = this.getZ() + dz * distance;
            int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor((double)x), Mth.floor((double)z));
            double y = Math.max((double)this.level().getMinBuildHeight() + 1.0, Math.min((double)this.level().getMaxBuildHeight() - 2.0, (double)groundY + 0.1));
            if (!this.randomTeleport(x, y, z, true)) continue;
            return;
        }
    }

    /*
     * Unable to fully structure code
     */
    private boolean tryNpcVoidStep(LivingEntity target) {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return false;
        }
        if (this.npcVoidStepCooldownTicks > 0) {
            return false;
        }
        if (!this.spellIds.contains(Spell.VOID_STEP.id())) {
            return false;
        }
        if (this.canUseCombatFlight()) {
            return false;
        }
        double distSqr = this.distanceToSqr(target);
        boolean targetFlying = target.isNoGravity() || target.isFallFlying()
                || (target instanceof Player player && player.getAbilities().flying);
        boolean needsReengage = distSqr > 121.0 || target.getEyeY() > this.getEyeY() + 2.5 || targetFlying;
        if (!needsReengage) {
            return false;
        }
        long cost = Math.max(1L, NpcSpellCaster.generalQiCost(this, 60L));
        if (this.getCurrentQi() < cost) {
            return false;
        }
        Vec3 toTarget = target.getEyePosition().subtract(this.getEyePosition());
        Vec3 horizontal = new Vec3(toTarget.x, 0.0, toTarget.z);
        if (horizontal.lengthSqr() < 1.0E-4) {
            return false;
        }
        Vec3 dir = horizontal.normalize();
        double distance = Math.sqrt(distSqr);
        double speed = distance > 18.0 ? 0.62 : 0.46;
        double vertical = target.getEyeY() > this.getEyeY() + 2.5 ? 0.26 : Math.max(-0.1, this.getDeltaMovement().y);
        this.deductQi(cost);
        this.npcVoidStepCooldownTicks = 24 + this.getRandom().nextInt(10);
        this.setNoGravity(false);
        this.setDeltaMovement(dir.x * speed, vertical, dir.z * speed);
        this.hasImpulse = true;
        this.fallDistance = 0.0f;
        Level level = this.level();
        if (level instanceof ServerLevel sl) {
            sl.sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.05, this.getZ(), 6, 0.25, 0.05, 0.25, 0.01);
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
        Level level;
        if (this.onGround()) {
            return;
        }
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return;
        }
        if (this.isTradingFreeze() || SpiritLockHandler.isEntityLocked((Entity)this)) {
            return;
        }
        if (!this.spellIds.contains(Spell.VOID_STEP.id())) {
            return;
        }
        if (this.canUseCombatFlight()) {
            return;
        }
        if (!VoidStepHandler.hasSlowFallClearance(this.level(), this.blockPosition(), this.getY())) {
            return;
        }
        Vec3 v = this.getDeltaMovement();
        if (v.y < -0.1) {
            this.setDeltaMovement(v.x, -0.1, v.z);
            this.hasImpulse = true;
        }
        this.fallDistance = 0.0f;
        if ((this.tickCount & 7) == 0 && (level = this.level()) instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.sendParticles((ParticleOptions)ParticleTypes.CLOUD, this.getX(), this.getY() - 0.1, this.getZ(), 2, 0.3, 0.05, 0.3, 0.0);
        }
    }

    private void normalizeGroundMovementSpeedAttribute() {
        UUID oldZhenyuanSpeed;
        AttributeInstance speedAttr = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (speedAttr == null) {
            return;
        }
        if (Math.abs(speedAttr.getBaseValue() - 0.23) > 1.0E-6) {
            speedAttr.setBaseValue(0.23);
        }
        if (speedAttr.getModifier(oldZhenyuanSpeed = UUID.nameUUIDFromBytes(("npc.zhenyuan.spd." + String.valueOf(this.getUUID())).getBytes(StandardCharsets.UTF_8))) != null) {
            speedAttr.removeModifier(oldZhenyuanSpeed);
        }
        double speedBonus = (double)this.getZhenyuanAgility() * 1.0 / 100.0;
        AttributeModifier existing = speedAttr.getModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
        if (speedBonus <= 0.0) {
            if (existing != null) {
                speedAttr.removeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
            }
            return;
        }
        if (existing != null && Math.abs(existing.getAmount() - speedBonus) <= 1.0E-6 && existing.getOperation() == AttributeModifier.Operation.MULTIPLY_BASE) {
            return;
        }
        if (existing != null) {
            speedAttr.removeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID);
        }
        speedAttr.addPermanentModifier(new AttributeModifier(ZHENYUAN_GROUND_SPEED_MODIFIER_ID, "npc_zhenyuan_ground_speed", speedBonus, AttributeModifier.Operation.MULTIPLY_BASE));
    }

    protected void jumpFromGround() {
        super.jumpFromGround();
        double heightBonus = (double)this.getZhenyuanAgility() * 0.2 / 100.0;
        if (heightBonus <= 0.0) {
            return;
        }
        double velocityBonus = Math.sqrt(1.0 + heightBonus) - 1.0;
        Vec3 movement = this.getDeltaMovement();
        this.setDeltaMovement(movement.x, movement.y + 0.42 * velocityBonus, movement.z);
        this.hasImpulse = true;
    }

    private void updateNpcSwordFlight() {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            this.stopNpcSwordFlight(true);
            return;
        }
        if (this.canFly()) {
            this.stopNpcSwordFlight(false);
            return;
        }
        if (this.isTradingFreeze() || SpiritLockHandler.isEntityLocked((Entity)this) || !(this.getMainHandItem().getItem() instanceof SwordItem)) {
            this.stopNpcSwordFlight(true);
            return;
        }
        LivingEntity target = this.getTarget();
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
            long upkeep = Math.max(1L, NpcSpellCaster.generalQiCost(this, 20L));
            if (this.currentQi < upkeep) {
                this.stopNpcSwordFlight(true);
            } else {
                this.deductQi(upkeep);
            }
        }
    }

    /*
     * Unable to fully structure code
     */
    private boolean shouldStartNpcSwordFlight(@Nullable LivingEntity target) {
        if (target == null || !target.isAlive()) {
            return false;
        }
        if (!this.canStartNpcSwordFlightForCombat()) {
            return false;
        }
        double distSqr = this.distanceToSqr(target);
        boolean targetFlying = target.isNoGravity() || target.isFallFlying()
                || (target instanceof Player player && player.getAbilities().flying);
        boolean verticalPressure = target.getEyeY() > this.getEyeY() + 3.0;
        boolean rangedCombat = distSqr > 144.0;
        boolean pressured = this.getHealth() < this.getMaxHealth() * 0.55f && distSqr < 100.0;
        return targetFlying || verticalPressure || rangedCombat || pressured || this.getRandom().nextInt(100) == 0;
    }

    private void startNpcSwordFlight() {
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
            return;
        }
        this.setNpcSwordFlightActive(true);
        this.npcSwordFlightIdleTicks = 0;
        this.setNoGravity(true);
        this.setDeltaMovement(this.getDeltaMovement().x, Math.max(0.35, this.getDeltaMovement().y), this.getDeltaMovement().z);
        Level level = this.level();
        if (level instanceof ServerLevel) {
            ServerLevel sl = (ServerLevel)level;
            sl.sendParticles((ParticleOptions)ParticleTypes.ENCHANT, this.getX(), this.getY() + 0.15, this.getZ(), 16, 0.45, 0.08, 0.45, 0.05);
            sl.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.TRIDENT_RIPTIDE_1, this.getSoundSource(), 0.55f, 1.35f);
        }
    }

    private void stopNpcSwordFlight(boolean restoreGravity) {
        if (!this.isNpcSwordFlightActive()) {
            return;
        }
        this.setNpcSwordFlightActive(false);
        this.npcSwordFlightIdleTicks = 0;
        if (restoreGravity && !this.canFly()) {
            this.setNoGravity(false);
            this.fallDistance = 0.0f;
            Vec3 v = this.getDeltaMovement();
            this.setDeltaMovement(v.x, Math.min(v.y, -0.02), v.z);
        }
    }

    private void updateFlightAndQiDrain() {
        boolean shouldFly;
        if (RealmPressureHandler.isSuppressed((LivingEntity)this)) {
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
        if (wasFlying) {
            shouldFly = this.currentQi > oneThird;
        } else {
            boolean bl = shouldFly = this.currentQi >= twoThirds;
        }
        if (shouldFly != wasFlying) {
            this.setNoGravity(shouldFly);
            if (shouldFly) {
                this.setDeltaMovement(this.getDeltaMovement().x, 0.4, this.getDeltaMovement().z);
                Level level = this.level();
                if (level instanceof ServerLevel) {
                    ServerLevel sl = (ServerLevel)level;
                    sl.sendParticles((ParticleOptions)ParticleTypes.CLOUD, this.getX(), this.getY() + 0.1, this.getZ(), 16, 0.4, 0.05, 0.4, 0.04);
                }
            } else {
                Vec3 v = this.getDeltaMovement();
                this.setDeltaMovement(v.x, Math.min(v.y, -0.05), v.z);
                Level level = this.level();
                if (level instanceof ServerLevel) {
                    ServerLevel sl = (ServerLevel)level;
                    sl.sendParticles((ParticleOptions)ParticleTypes.POOF, this.getX(), this.getY() + 0.1, this.getZ(), 8, 0.3, 0.05, 0.3, 0.02);
                }
            }
        }
        if (shouldFly && this.tickCount % 10 == 0) {
            this.currentQi = Math.max(0L, this.currentQi - 5L);
        }
    }

    private void updateIdleFlight() {
        double driftZ;
        double driftX;
        int groundY = this.level().getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, this.getBlockX(), this.getBlockZ());
        double targetY = (double)groundY + 8.0;
        double delta = targetY - this.getY();
        double yMomentum = delta > 1.5 ? 0.1 : (delta < -1.5 ? -0.05 : Math.sin((double)this.tickCount / 20.0) * 0.025);
        Vec3 v = this.getDeltaMovement();
        if (this.isNpcSoulState()) {
            if (--this.soulDriftTicks <= 0) {
                this.soulDriftTicks = 35 + this.random.nextInt(41);
                double angle = this.random.nextDouble() * Math.PI * 2.0;
                double speed = 0.035 + this.random.nextDouble() * 0.045;
                this.soulDriftX = Math.cos(angle) * speed;
                this.soulDriftZ = Math.sin(angle) * speed;
            }
            driftX = this.soulDriftX;
            driftZ = this.soulDriftZ;
        } else {
            double driftAngle = (double)this.tickCount / 80.0 * Math.PI * 2.0;
            driftX = Math.cos(driftAngle) * 0.02;
            driftZ = Math.sin(driftAngle) * 0.02;
        }
        this.setDeltaMovement(v.x * 0.8 + driftX, yMomentum, v.z * 0.8 + driftZ);
    }

    private void updateDisplayName() {
        Realm realm = this.getRealm();
        SubStage sub = this.getSubStage();
        Component realmDisplay = realm == Realm.LOOSE_IMMORTAL ? Component.translatable((String)("realm.friday_cultivation.loose_immortal.level." + this.getLooseImmortalTribulations())) : realm.displayName();
        MutableComponent realmText = realm.npcCategoryName().copy().append((Component)Component.literal((String)" ")).append((Component)this.getCultivatorName()).append((Component)Component.literal((String)" (")).append(realmDisplay);
        if (realm != Realm.LOOSE_IMMORTAL) {
            realmText.append((Component)Component.literal((String)" ")).append((Component)sub.displayName());
        }
        realmText.append((Component)Component.literal((String)")")).withStyle(WanderingCultivatorEntity.realmColor(realm));
        ChatFormatting qiColor = this.maxQi == 0L ? ChatFormatting.GRAY : (this.currentQi * 100L / Math.max(1L, this.maxQi) >= 50L ? ChatFormatting.GREEN : (this.currentQi * 100L / Math.max(1L, this.maxQi) >= 20L ? ChatFormatting.YELLOW : ChatFormatting.RED));
        MutableComponent qiText = Component.literal((String)("  \u9748\u6c23 " + this.currentQi + "/" + this.maxQi)).withStyle(qiColor);
        this.setCustomName(realmText.append(qiText));
        this.setCustomNameVisible(true);
    }

    private static ChatFormatting realmColor(Realm realm) {
        return switch (realm) {
            default -> throw new IncompatibleClassChangeError();
            case MORTAL -> ChatFormatting.GRAY;
            case BODY_TEMPERING -> ChatFormatting.DARK_GREEN;
            case QI_REFINING -> ChatFormatting.AQUA;
            case FOUNDATION_BUILDING -> ChatFormatting.GOLD;
            case GOLDEN_CORE -> ChatFormatting.YELLOW;
            case NASCENT_SOUL -> ChatFormatting.LIGHT_PURPLE;
            case SOUL_FORMATION -> ChatFormatting.DARK_PURPLE;
            case VOID_REFINING -> ChatFormatting.BLUE;
            case BODY_INTEGRATION -> ChatFormatting.WHITE;
            case MAHAYANA -> ChatFormatting.RED;
            case TRIBULATION_TRANSCENDENCE -> ChatFormatting.DARK_BLUE;
            case TRUE_IMMORTAL -> ChatFormatting.GOLD;
            case GREAT_EMPEROR -> ChatFormatting.DARK_RED;
            case LOOSE_IMMORTAL -> ChatFormatting.LIGHT_PURPLE;
        };
    }

    static {
        WanderingCultivatorEntity.validateSkinGenderManifest();
        ZHENYUAN_GROUND_SPEED_MODIFIER_ID = UUID.nameUUIDFromBytes("friday_cultivation:npc_zhenyuan_ground_speed".getBytes(StandardCharsets.UTF_8));
        PREFERRED_SPAWN_STRUCTURES = TagKey.create((ResourceKey)Registries.STRUCTURE, (ResourceLocation)new ResourceLocation("friday_cultivation", "wandering_cultivator_preferred"));
        DATA_REALM_ORD = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_SUB_STAGE_ORD = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_LOOSE_IMMORTAL_TRIBULATIONS = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_TECHNIQUE_ID = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_SKIN_VARIANT = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_DIFU_REAPER = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
        DATA_NPC_SOUL_STATE = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
        DATA_SURNAME_IDX = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_GIVEN_IDX = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_GENDER = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_CURRENT_QI = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_MAX_QI = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_SPELL_IDS_CSV = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_FAVORITE_ITEMS_CSV = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_SPIRIT_ROOT_ID = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_PHYSIQUE_ID = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_IDENTITY_ID = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_ZHENYUAN_CSV = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_ALCHEMY_RANK_ORD = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_REFINING_RANK_ORD = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
        DATA_NPC_SWORD_FLIGHT_ACTIVE = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
        DATA_SECT_ID = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_SECT_NAME = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        DATA_SECT_ROLE = SynchedEntityData.defineId(WanderingCultivatorEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
        LOADED_NPC_SOULS = Collections.newSetFromMap(new ConcurrentHashMap());
        SWORD_SPELL_IDS = Set.of(Spell.SWORD_CONVERGENCE.id(), Spell.FLYING_SWORD.id(), Spell.SWORD_FLIGHT.id(), Spell.SKY_SPLITTING_SWORD_AURA.id(), Spell.SWORD_AURA.id());
    }

    @Override
    public AgeableMob getBreedOffspring(ServerLevel level, AgeableMob otherParent) {
        return null;
    }
}
