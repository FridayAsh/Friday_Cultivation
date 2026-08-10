/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.LongTag
 *  net.minecraft.nbt.StringTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.saveddata.SavedData
 *  net.minecraft.world.phys.AABB
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.sect;

import com.friday.cultivation.block.formation.FormationCorePlateBlock;
import com.friday.cultivation.block.formation.FormationCorePlateBlockEntity;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.block.spirit.SpiritVeinCoreBlock;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.qi.SpiritVeinCoreTier;
import com.friday.cultivation.cultivation.qi.formation.CoreTier;
import com.friday.cultivation.cultivation.qi.formation.FormationType;
import com.friday.cultivation.cultivation.sect.SectNameGenerator;
import com.friday.cultivation.cultivation.sect.SectRole;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.cultivation.technique.Technique;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.item.SectTokenItem;
import com.friday.cultivation.item.SpellBookItem;
import com.friday.cultivation.item.TechniqueBookItem;
import com.friday.cultivation.registry.ModItems;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.LongTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

public class SectSavedData
extends SavedData {
    private static final int GENERATED_MAZE_RADIUS_SHRINK = 2;
    private static final String DATA_NAME = "friday_cultivation_sects";
    private final Map<String, SectRecord> sects = new LinkedHashMap<String, SectRecord>();

    public static SectSavedData get(ServerLevel level) {
        return (SectSavedData)level.getDataStorage().computeIfAbsent(SectSavedData::load, SectSavedData::new, DATA_NAME);
    }

    public static SectSavedData load(CompoundTag tag) {
        SectSavedData data = new SectSavedData();
        ListTag list = tag.getList("sects", 10);
        for (int i = 0; i < list.size(); ++i) {
            SectRecord record = SectRecord.load(list.getCompound(i));
            if (record.id.isBlank()) continue;
            data.sects.put(record.id, record);
        }
        return data;
    }

    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        for (SectRecord record : this.sects.values()) {
            list.add(record.save());
        }
        tag.put("sects", (Tag)list);
        return tag;
    }

    public SectRecord getOrCreateGeneratedSect(ServerLevel level, long seed, BlockPos center, boolean hasProtectionArray, int plannedRadius) {
        String id = SectSavedData.generatedSectId(level, center);
        SectRecord record = this.sects.get(id);
        if (record == null) {
            record = new SectRecord(id, SectNameGenerator.randomName(seed), level.dimension().location().toString(), center.east(), hasProtectionArray, Math.max(1, plannedRadius));
            this.sects.put(id, record);
            this.setDirty();
        } else {
            boolean changed = false;
            changed |= record.updateCenter(center);
            changed |= record.updateHasProtectionArray(hasProtectionArray);
            if (record.corePos == null) {
                changed |= record.updateRadius(plannedRadius);
            }
            if (changed) {
                this.setDirty();
            }
        }
        return record;
    }

    private static String generatedSectId(ServerLevel level, BlockPos center) {
        return String.valueOf(level.dimension().location()) + ":" + center.getX() + "," + center.getZ();
    }

    public Optional<SectRecord> byId(String sectId) {
        return Optional.ofNullable(this.sects.get(sectId));
    }

    public boolean claimSpawn(String sectId, String key) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || record.destroyed || key == null || key.isBlank()) {
            return false;
        }
        if (!record.spawnClaims.add(key)) {
            return false;
        }
        this.setDirty();
        return true;
    }

    public boolean claimCoreInit(String sectId, BlockPos corePos) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || corePos == null) {
            return false;
        }
        long packed = corePos.asLong();
        if (!record.configuredCores.add(packed)) {
            return false;
        }
        this.setDirty();
        return true;
    }

    public void unclaimCoreInit(String sectId, BlockPos corePos) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || corePos == null) {
            return;
        }
        if (record.configuredCores.remove(corePos.asLong())) {
            this.setDirty();
        }
    }

    public void registerBuilding(String sectId, String type, BlockPos origin, int sizeX, int sizeZ) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || origin == null) {
            return;
        }
        String key = type + "@" + origin.getX() + "," + origin.getZ();
        if (record.buildings.containsKey(key)) {
            return;
        }
        record.buildings.put(key, new BuildingRecord(type, origin.east(), sizeX, sizeZ));
        this.setDirty();
    }

    @Nullable
    public ArrayReplacement claimArrayReplacement(String sectId, BlockPos pos) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || pos == null) {
            return null;
        }
        long packed = pos.asLong();
        ArrayReplacement existing = record.arrayReplacements.get(packed);
        if (existing != null) {
            ArrayReplacement migrated = SectSavedData.migrateGeneratedArrayReplacement(record, packed, existing);
            if (migrated != existing) {
                record.arrayReplacements.put(packed, migrated);
                this.setDirty();
            }
            return migrated;
        }
        int index = record.arrayReplacements.size();
        ArrayReplacement next = ArrayReplacement.byIndex(index);
        if (next == null) {
            return null;
        }
        record.arrayReplacements.put(packed, next);
        this.setDirty();
        return next;
    }

    private static ArrayReplacement migrateGeneratedArrayReplacement(SectRecord record, long packed, ArrayReplacement existing) {
        if (existing != ArrayReplacement.IMMORTAL_REJUVENATION_FLAG) {
            return existing;
        }
        int index = SectSavedData.arrayReplacementIndex(record, packed);
        return index == 3 ? ArrayReplacement.IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT : existing;
    }

    private static int arrayReplacementIndex(SectRecord record, long packed) {
        int index = 0;
        for (Long key : record.arrayReplacements.keySet()) {
            if (key != null && key == packed) {
                return index;
            }
            ++index;
        }
        return -1;
    }

    public List<BlockPos> arrayFlagPositions(String sectId) {
        SectRecord record = this.sects.get(sectId);
        if (record == null) {
            return List.of();
        }
        ArrayList<BlockPos> flags = new ArrayList<BlockPos>();
        for (Map.Entry<Long, ArrayReplacement> entry : record.arrayReplacements.entrySet()) {
            if (!entry.getValue().isFlag()) continue;
            flags.add(BlockPos.of((long)entry.getKey()));
        }
        return flags;
    }

    public void registerCore(String sectId, BlockPos corePos, int radius) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || corePos == null) {
            return;
        }
        boolean changed = record.corePos == null || !record.corePos.equals((Object)corePos);
        record.corePos = corePos.east();
        changed |= record.updateRadius(radius);
        for (MemberRecord member : record.members.values()) {
            if (member.corePos != null && member.corePos.equals((Object)record.corePos)) continue;
            member.corePos = record.corePos;
            changed = true;
        }
        if (changed) {
            this.setDirty();
        }
    }

    @Nullable
    public BlockPos corePos(String sectId) {
        SectRecord record = this.sects.get(sectId);
        return record == null ? null : record.corePos;
    }

    @Nullable
    public SectRecord findGeneratedSectByCore(BlockPos corePos) {
        if (corePos == null) {
            return null;
        }
        for (SectRecord record : this.sects.values()) {
            if (!record.hasProtectionArray || record.corePos == null || !record.corePos.equals((Object)corePos)) continue;
            return record;
        }
        return null;
    }

    public boolean isPlayerHostileToCore(BlockPos corePos, Entity entity) {
        SectRecord sect = this.findGeneratedSectByCore(corePos);
        return sect != null && this.isEnemyOfSect(sect.id, entity);
    }

    public int memberContribution(String sectId, UUID memberId) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || memberId == null) {
            return 0;
        }
        MemberRecord member = record.members.get(memberId);
        return member == null ? 0 : member.contribution;
    }

    public boolean configureGeneratedArrayCoreIfReady(ServerLevel level, FormationCorePlateBlockEntity core) {
        if (level == null || core == null) {
            return false;
        }
        BlockPos corePos = core.getBlockPos();
        for (SectRecord record : this.sects.values()) {
            boolean configured;
            if (!record.hasProtectionArray || record.corePos == null || !record.corePos.equals((Object)corePos)) continue;
            if (core.isGeneratedArrayPlayerEdited()) {
                return false;
            }
            List<BlockPos> flags = this.validArrayFlagPositions(level, record);
            if (flags.size() < ArrayReplacement.flagCount()) {
                return false;
            }
            Map<BlockPos, Integer> flagRadii = SectSavedData.generatedFlagRadii(level, flags, record.radius);
            if (core.isActive()) {
                if (core.generatedSectArrayMatches(record.name, flagRadii, record.radius)) {
                    return false;
                }
                boolean configured2 = core.configureGeneratedSectArray(record.name, flagRadii, record.radius);
                if (configured2) {
                    this.backfillLoadedNpcCoreTokens(level, record.id);
                }
                return configured2;
            }
            if (!this.claimCoreInit(record.id, corePos)) {
                if (core.hasStoredLinkedFlags()) {
                    return false;
                }
                this.unclaimCoreInit(record.id, corePos);
                if (!this.claimCoreInit(record.id, corePos)) {
                    return false;
                }
            }
            if (!(configured = core.configureGeneratedSectArray(record.name, flagRadii, record.radius))) {
                this.unclaimCoreInit(record.id, corePos);
                return false;
            }
            this.backfillLoadedNpcCoreTokens(level, record.id);
            return true;
        }
        return false;
    }

    private static Map<BlockPos, Integer> generatedFlagRadii(ServerLevel level, List<BlockPos> flags, int protectionRadius) {
        LinkedHashMap<BlockPos, Integer> radii = new LinkedHashMap<BlockPos, Integer>();
        int clampedProtectionRadius = FormationCorePlateBlockEntity.clampFlagEffectRadius(protectionRadius);
        int clampedMazeRadius = FormationCorePlateBlockEntity.clampFlagEffectRadius(clampedProtectionRadius - 2);
        for (BlockPos flagPos : flags) {
            FormationFlagBlock flag;
            if (flagPos == null) continue;
            BlockState state = level.getBlockState(flagPos);
            int radius = clampedProtectionRadius;
            Block block = state.getBlock();
            if (block instanceof FormationFlagBlock && (flag = (FormationFlagBlock)block).formationType() == FormationType.MAZE) {
                radius = clampedMazeRadius;
            }
            radii.put(flagPos.east(), radius);
        }
        return radii;
    }

    private List<BlockPos> validArrayFlagPositions(ServerLevel level, SectRecord record) {
        ArrayList<BlockPos> flags = new ArrayList<BlockPos>();
        for (Map.Entry<Long, ArrayReplacement> entry : record.arrayReplacements.entrySet()) {
            BlockPos flagPos;
            BlockState state;
            ArrayReplacement replacement = entry.getValue();
            if (!replacement.isFlag() || !SectSavedData.matchesArrayReplacement(replacement, state = level.getBlockState(flagPos = BlockPos.of((long)entry.getKey())))) continue;
            flags.add(flagPos);
        }
        return flags;
    }

    @Nullable
    public SectRecord findSectByArrayBlock(BlockPos pos, @Nullable BlockState brokenState) {
        if (pos == null || brokenState == null || brokenState.isAir()) {
            return null;
        }
        long packed = pos.asLong();
        for (SectRecord record : this.sects.values()) {
            ArrayReplacement replacement;
            if (!(record.corePos != null && record.corePos.equals((Object)pos) ? SectSavedData.isGeneratedArrayCorePlate(brokenState) : (replacement = record.arrayReplacements.get(packed)) != null && SectSavedData.matchesArrayReplacement(replacement, brokenState))) continue;
            return record;
        }
        return null;
    }

    @Nullable
    public SectRecord markArrayBlockSaboteur(ServerLevel level, BlockPos pos, LivingEntity enemy, @Nullable BlockState brokenState) {
        if (level == null || pos == null || enemy == null) {
            return null;
        }
        SectRecord sect = this.findSectByArrayBlock(pos, brokenState);
        if (sect == null) {
            return null;
        }
        if (this.isCurrentPlayerMemberOf(enemy, sect)) {
            return null;
        }
        this.markEnemyAndRetaliate(level, sect, enemy, SectRole.GUARD_DISCIPLE, true);
        return sect;
    }

    private boolean isCurrentPlayerMemberOf(LivingEntity entity, SectRecord sect) {
        ServerPlayer player;
        block5: {
            block4: {
                if (!(entity instanceof ServerPlayer)) break block4;
                player = (ServerPlayer)entity;
                if (sect != null) break block5;
            }
            return false;
        }
        if (!sect.id.equals(this.sectIdOf((Entity)player))) {
            return false;
        }
        MemberRecord member = sect.members.get(player.getUUID());
        return member != null && member.player;
    }

    private static boolean isGeneratedArrayCorePlate(BlockState state) {
        FormationCorePlateBlock core;
        Block block = state.getBlock();
        return block instanceof FormationCorePlateBlock && (core = (FormationCorePlateBlock)block).coreTier() == CoreTier.IMMORTAL;
    }

    private static boolean matchesArrayReplacement(ArrayReplacement replacement, BlockState state) {
        return switch (replacement) {
            default -> throw new IncompatibleClassChangeError();
            case IMMORTAL_SPIRIT_CORE, IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT -> {
                SpiritVeinCoreBlock core;
                Block var3_2 = state.getBlock();
                if (var3_2 instanceof SpiritVeinCoreBlock && (core = (SpiritVeinCoreBlock)var3_2).tier() == SpiritVeinCoreTier.IMMORTAL) {
                    yield true;
                }
                yield false;
            }
            case IMMORTAL_SECT_PROTECTION_FLAG -> SectSavedData.isImmortalFlag(state, FormationType.SECT_PROTECTION);
            case IMMORTAL_MAZE_FLAG -> SectSavedData.isImmortalFlag(state, FormationType.MAZE);
            case IMMORTAL_REJUVENATION_FLAG -> SectSavedData.isImmortalFlag(state, FormationType.REJUVENATION);
        };
    }

    private static boolean isImmortalFlag(BlockState state, FormationType expectedType) {
        FormationFlagBlock flag;
        Block block = state.getBlock();
        return block instanceof FormationFlagBlock && (flag = (FormationFlagBlock)block).flagTier() == ItemTier.IMMORTAL && flag.formationType() == expectedType;
    }

    public void registerNpcMember(WanderingCultivatorEntity npc, String sectId, SectRole role, @Nullable BlockPos homePos, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos, @Nullable BlockPos corePos) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || record.destroyed || npc == null || role == null || role == SectRole.NONE) {
            return;
        }
        MemberRecord member = new MemberRecord(npc.getUUID(), npc.getCultivatorName().getString(), role, false, homePos, bedPos, cushionPos, corePos, true, 0);
        record.members.put(npc.getUUID(), member);
        this.setDirty();
    }

    public boolean ensureNpcCoreAndTokens(WanderingCultivatorEntity npc) {
        if (npc == null || !(npc.level() instanceof ServerLevel) || !npc.hasSectMembership()) {
            return false;
        }
        SectRecord record = this.sects.get(npc.getSectId());
        if (record == null || record.corePos == null) {
            return false;
        }
        boolean changed = false;
        MemberRecord member = record.members.get(npc.getUUID());
        if (!(member == null || member.corePos != null && member.corePos.equals((Object)record.corePos))) {
            member.corePos = record.corePos;
            changed = true;
        }
        if (changed |= npc.ensureSectCoreLink(record.corePos, true)) {
            this.setDirty();
        }
        return changed;
    }

    public boolean backfillLoadedNpcCoreTokens(ServerLevel level, String sectId) {
        SectRecord record = this.sects.get(sectId);
        if (level == null || record == null || record.corePos == null) {
            return false;
        }
        double radius = Math.max(96.0, (double)record.radius + 64.0);
        AABB scan = new AABB(record.center).inflate(radius, 96.0, radius);
        boolean changed = false;
        for (WanderingCultivatorEntity npc2 : level.getEntitiesOfClass(WanderingCultivatorEntity.class, scan, npc -> npc.isAlive() && sectId.equals(npc.getSectId()))) {
            changed |= this.ensureNpcCoreAndTokens(npc2);
        }
        return changed;
    }

    public boolean removeNpcMember(WanderingCultivatorEntity npc) {
        if (npc == null) {
            return false;
        }
        String sectId = npc.getSectId();
        if (sectId == null || sectId.isBlank()) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        if (record == null) {
            return false;
        }
        UUID npcId = npc.getUUID();
        String npcKey = npcId.toString();
        boolean changed = record.members.remove(npcId) != null;
        changed |= record.enemies.remove(npcId);
        changed |= record.enemyRetaliationRoles.remove(npcId) != null;
        changed |= record.sameSectCombatPairs.removeIf(pair -> pair.contains(npcKey));
        changed |= record.tasks.entrySet().removeIf(entry -> ((TaskRecord)entry.getValue()).issuerUuid.equals(npcId));
        for (TaskRecord task : record.tasks.values()) {
            changed |= task.acceptedBy.remove(npcId);
            changed |= task.readyBy.remove(npcId);
            changed |= task.completedBy.remove(npcId);
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public boolean clearPlayerHostility(UUID playerId) {
        if (playerId == null) {
            return false;
        }
        String playerKey = playerId.toString();
        boolean changed = false;
        for (SectRecord record : this.sects.values()) {
            changed |= record.enemies.remove(playerId);
            changed |= record.enemyRetaliationRoles.remove(playerId) != null;
            changed |= record.sameSectCombatPairs.removeIf(pair -> pair.startsWith(playerKey + ">") || pair.endsWith(">" + playerKey));
        }
        if (changed) {
            this.setDirty();
        }
        return changed;
    }

    public boolean markDestroyedIfNoNpcMembers(String sectId) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || record.destroyed || SectSavedData.hasNpcMembers(record)) {
            return false;
        }
        record.destroyed = true;
        this.setDirty();
        return true;
    }

    public boolean isSectDestroyed(String sectId) {
        SectRecord record = this.sects.get(sectId);
        return record != null && record.destroyed;
    }

    private static boolean hasNpcMembers(SectRecord record) {
        if (record == null) {
            return false;
        }
        for (MemberRecord member : record.members.values()) {
            if (member.player) continue;
            return true;
        }
        return false;
    }

    public boolean isRoutineTargetClaimed(String sectId, BlockPos pos, boolean cushion) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || pos == null) {
            return false;
        }
        for (MemberRecord member : record.members.values()) {
            BlockPos claimed = cushion ? member.cushionPos : member.bedPos;
            if (!pos.equals((Object)claimed)) continue;
            return true;
        }
        return false;
    }

    public void updateNpcRoutineClaims(WanderingCultivatorEntity npc, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos) {
        if (npc == null || !npc.hasSectMembership()) {
            return;
        }
        SectRecord record = this.sects.get(npc.getSectId());
        if (record == null) {
            return;
        }
        MemberRecord member = record.members.get(npc.getUUID());
        if (member == null || member.player) {
            return;
        }
        BlockPos safeBed = bedPos == null ? null : bedPos.east();
        BlockPos safeCushion = cushionPos == null ? null : cushionPos.east();
        boolean changed = false;
        if (!Objects.equals(member.bedPos, safeBed)) {
            member.bedPos = safeBed;
            changed = true;
        }
        if (!Objects.equals(member.cushionPos, safeCushion)) {
            member.cushionPos = safeCushion;
            changed = true;
        }
        if (changed) {
            this.setDirty();
        }
    }

    public void joinPlayer(ServerPlayer player, String sectId) {
        SectRecord record = this.sects.get(sectId);
        if (record == null || player == null) {
            return;
        }
        MemberRecord member = new MemberRecord(player.getUUID(), player.getGameProfile().getName(), SectRole.SERVANT, true, null, null, null, record.corePos, true, 0);
        CultivationCapability.get((Player)player).ifPresent(data -> data.setSectDisplay(record.id, record.name, member.role.id()));
        record.members.put(player.getUUID(), member);
        SectSavedData.ensurePlayerPersonalToken(player, record);
        this.setDirty();
    }

    public void syncPlayerSectDisplay(ServerPlayer player) {
        if (player == null) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(data -> {
            String sectId = data.getSectId();
            if (sectId.isBlank()) {
                data.clearSectDisplay();
                return;
            }
            SectRecord record = this.sects.get(sectId);
            if (record == null) {
                return;
            }
            MemberRecord member = record.members.get(player.getUUID());
            if (member == null) {
                data.setSectDisplay("", "", "");
                return;
            }
            data.setSectDisplay(record.id, record.name, member.role.id());
        });
    }

    private static void ensurePlayerPersonalToken(ServerPlayer player, SectRecord record) {
        if (player == null || record == null || record.corePos == null) {
            return;
        }
        Inventory inv = player.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (!SectTokenItem.isLinkedToCore(stack, player.level(), record.corePos) || SectTokenItem.isTemporaryLinked(stack) || !SectTokenItem.isUsableBy(stack, (Entity)player)) continue;
            return;
        }
        ItemStack token = SectTokenItem.createLinked(player.level(), record.corePos, record.name, player.getGameProfile().getName(), false, 1);
        if (!inv.add(token)) {
            player.drop(token, false);
        }
    }

    private void ensurePlayerCoreAndToken(ServerPlayer player, SectRecord record) {
        if (player == null || record == null || record.corePos == null) {
            return;
        }
        MemberRecord member = record.members.get(player.getUUID());
        boolean changed = false;
        if (!(member == null || member.corePos != null && member.corePos.equals((Object)record.corePos))) {
            member.corePos = record.corePos;
            changed = true;
        }
        SectSavedData.ensurePlayerPersonalToken(player, record);
        if (changed) {
            this.setDirty();
        }
    }

    @Nullable
    public String sectIdOf(Entity entity) {
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            String id = npc.getSectId();
            return id.isBlank() ? null : id;
        }
        if (entity instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)entity;
            return CultivationCapability.get((Player)player).map(data -> data.getSectId().isBlank() ? null : data.getSectId()).orElse(null);
        }
        return null;
    }

    public SectRole roleOf(Entity entity) {
        if (entity instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)entity;
            return npc.getSectRole();
        }
        if (entity != null) {
            for (SectRecord record : this.sects.values()) {
                MemberRecord member = record.members.get(entity.getUUID());
                if (member == null) continue;
                return member.role;
            }
        }
        return SectRole.NONE;
    }

    public boolean sameSect(Entity a, Entity b) {
        String aSect = this.sectIdOf(a);
        String bSect = this.sectIdOf(b);
        return aSect != null && bSect != null && aSect.equals(bSect);
    }

    public boolean isEnemyOfSect(String sectId, Entity entity) {
        SectRecord record = this.sects.get(sectId);
        return record != null && entity != null && record.enemies.contains(entity.getUUID());
    }

    public boolean hasEnemyRelation(Entity a, Entity b) {
        if (a == null || b == null) {
            return false;
        }
        String aSect = this.sectIdOf(a);
        if (aSect != null && this.isEnemyOfSect(aSect, b)) {
            return true;
        }
        String bSect = this.sectIdOf(b);
        return bSect != null && this.isEnemyOfSect(bSect, a);
    }

    @Nullable
    public LivingEntity findNearestEnemyOfSect(ServerLevel level, String sectId, LivingEntity seeker, double radius) {
        return this.findNearestEnemyOfSect(level, sectId, SectRole.GUARD_DISCIPLE, seeker, radius);
    }

    @Nullable
    public LivingEntity findNearestEnemyOfSect(ServerLevel level, String sectId, SectRole seekerRole, LivingEntity seeker, double radius) {
        if (level == null || sectId == null || sectId.isBlank() || seeker == null) {
            return null;
        }
        SectRecord record = this.sects.get(sectId);
        if (record == null || record.enemies.isEmpty()) {
            return null;
        }
        SectRole safeRole = seekerRole == null ? SectRole.NONE : seekerRole;
        AABB scan = new AABB(seeker.blockPosition()).inflate(radius, 16.0, radius);
        LivingEntity nearest = null;
        double bestDistance = Double.MAX_VALUE;
        for (LivingEntity candidate : level.getEntitiesOfClass(LivingEntity.class, scan, e -> e != seeker && e.isAlive() && !e.isSpectator() && record.enemies.contains(e.getUUID()) && SectSavedData.canRoleRetaliateAgainst(record, safeRole, e.getUUID()))) {
            double distance = seeker.distanceToSqr((Entity)candidate);
            if (!(distance < bestDistance)) continue;
            bestDistance = distance;
            nearest = candidate;
        }
        return nearest;
    }

    public boolean canStartSameSectDamage(Entity attacker) {
        if (!(attacker instanceof ServerPlayer)) {
            return false;
        }
        ServerPlayer player = (ServerPlayer)attacker;
        String sectId = this.sectIdOf((Entity)player);
        if (sectId == null || sectId.isBlank()) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        if (record == null) {
            return false;
        }
        MemberRecord member = record.members.get(player.getUUID());
        return member != null && !member.sameSectImmunity;
    }

    public boolean hasSameSectCombatPair(Entity attacker, Entity target) {
        if (attacker == null || target == null) {
            return false;
        }
        String sectId = this.sectIdOf(attacker);
        if (sectId == null || sectId.isBlank() || !sectId.equals(this.sectIdOf(target))) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        return record != null && record.sameSectCombatPairs.contains(SectSavedData.pairKey(attacker.getUUID(), target.getUUID()));
    }

    public void recordSameSectCombatPair(Entity attacker, Entity target) {
        if (attacker == null || target == null) {
            return;
        }
        String sectId = this.sectIdOf(attacker);
        if (sectId == null || sectId.isBlank() || !sectId.equals(this.sectIdOf(target))) {
            return;
        }
        SectRecord record = this.sects.get(sectId);
        if (record == null) {
            return;
        }
        boolean changed = record.sameSectCombatPairs.add(SectSavedData.pairKey(attacker.getUUID(), target.getUUID()));
        if (changed |= record.sameSectCombatPairs.add(SectSavedData.pairKey(target.getUUID(), attacker.getUUID()))) {
            this.setDirty();
        }
    }

    public boolean setSameSectImmunity(ServerPlayer player, boolean enabled) {
        if (player == null) {
            return false;
        }
        String sectId = this.sectIdOf((Entity)player);
        if (sectId == null || sectId.isBlank()) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        if (record == null) {
            return false;
        }
        MemberRecord member = record.members.get(player.getUUID());
        if (member == null) {
            return false;
        }
        if (member.sameSectImmunity == enabled) {
            return false;
        }
        member.sameSectImmunity = enabled;
        this.setDirty();
        return true;
    }

    public boolean isRegisteredMember(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        String sectId = this.sectIdOf((Entity)player);
        if (sectId == null || sectId.isBlank()) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        return record != null && record.members.containsKey(player.getUUID());
    }

    public boolean isEnemyOf(ServerPlayer player, String sectId) {
        if (player == null || sectId == null || sectId.isBlank()) {
            return false;
        }
        SectRecord record = this.sects.get(sectId);
        return record != null && record.enemies.contains(player.getUUID());
    }

    private static String pairKey(UUID attacker, UUID target) {
        return String.valueOf(attacker) + ">" + String.valueOf(target);
    }

    public void markEnemyAndRetaliate(ServerLevel level, SectRecord sect, LivingEntity enemy, SectRole victimRole, boolean guardsOnly) {
        if (level == null || sect == null || enemy == null) {
            return;
        }
        if (SectSavedData.recordEnemy(sect, enemy.getUUID(), victimRole, guardsOnly)) {
            this.setDirty();
        }
        double radius = Math.max(96.0, (double)sect.radius + 64.0);
        AABB scan = new AABB(sect.center).inflate(radius, 96.0, radius);
        for (WanderingCultivatorEntity npc : level.getEntitiesOfClass(WanderingCultivatorEntity.class, scan, e -> e.isAlive() && sect.id.equals(e.getSectId()))) {
            SectRole role = npc.getSectRole();
            if (guardsOnly && role != SectRole.GUARD_DISCIPLE || !guardsOnly && !role.sameOrHigherThan(victimRole)) continue;
            npc.setTarget(enemy);
        }
    }

    private static boolean recordEnemy(SectRecord sect, UUID enemyId, SectRole victimRole, boolean guardsOnly) {
        SectRole next;
        SectRole safeRole;
        if (sect == null || enemyId == null) {
            return false;
        }
        boolean changed = sect.enemies.add(enemyId);
        if (guardsOnly) {
            return changed;
        }
        SectRole sectRole = safeRole = victimRole == null ? SectRole.NONE : victimRole;
        if (safeRole == SectRole.NONE) {
            return changed;
        }
        SectRole previous = sect.enemyRetaliationRoles.get(enemyId);
        SectRole sectRole2 = next = previous == null || safeRole.rank() < previous.rank() ? safeRole : previous;
        if (previous != next) {
            sect.enemyRetaliationRoles.put(enemyId, next);
            changed = true;
        }
        return changed;
    }

    private static boolean canRoleRetaliateAgainst(SectRecord sect, SectRole seekerRole, UUID enemyId) {
        SectRole threshold = sect.enemyRetaliationRoles.get(enemyId);
        if (threshold != null) {
            return seekerRole.sameOrHigherThan(threshold);
        }
        return seekerRole == SectRole.GUARD_DISCIPLE;
    }

    public CompoundTag snapshot(ServerPlayer viewer, @Nullable WanderingCultivatorEntity target) {
        String sectId = target != null ? target.getSectId() : this.sectIdOf((Entity)viewer);
        CompoundTag tag = new CompoundTag();
        if (sectId == null || sectId.isBlank() || !this.sects.containsKey(sectId)) {
            tag.putBoolean("hasSect", false);
            return tag;
        }
        SectRecord sect = this.sects.get(sectId);
        String viewerSectId = this.sectIdOf((Entity)viewer);
        boolean viewerBelongsHere = viewerSectId != null && viewerSectId.equals(sect.id);
        boolean viewerEnemy = sect.enemies.contains(viewer.getUUID());
        MemberRecord viewerMember = viewerBelongsHere ? sect.members.get(viewer.getUUID()) : null;
        tag.putBoolean("hasSect", true);
        tag.putString("sectId", sect.id);
        tag.putString("sectName", sect.name);
        tag.putBoolean("viewerEnemy", viewerEnemy);
        tag.putBoolean("viewerMember", viewerMember != null);
        tag.putString("viewerRole", viewerMember == null ? SectRole.NONE.id() : viewerMember.role.id());
        tag.putInt("viewerContribution", viewerMember == null ? 0 : viewerMember.contribution);
        if (viewerMember != null) {
            this.ensurePlayerCoreAndToken(viewer, sect);
        }
        tag.putBoolean("sameSectImmunity", this.sameSectImmunityOf(viewer, sect));
        tag.putBoolean("canJoin", this.canViewerJoinFromTarget(viewer, target, sect));
        if (target != null) {
            tag.putInt("targetEntityId", target.getId());
            tag.putString("targetRole", target.getSectRole().id());
            this.ensureBasicTask(sect.id, target);
        }
        ArrayList<MemberRecord> members = new ArrayList<MemberRecord>(sect.members.values());
        members.sort(Comparator.comparingInt((MemberRecord m) -> m.role.rank()).thenComparing(m -> m.name));
        ListTag memberList = new ListTag();
        for (MemberRecord member : members) {
            CompoundTag row = new CompoundTag();
            row.putUUID("uuid", member.uuid);
            row.putString("name", member.name);
            row.putString("role", member.role.id());
            row.putBoolean("player", member.player);
            row.putInt("contribution", member.contribution);
            memberList.add(row);
        }
        tag.put("members", (Tag)memberList);
        tag.put("tasks", (Tag)this.tasksSnapshot(viewer, target, sect));
        return tag;
    }

    private boolean sameSectImmunityOf(ServerPlayer viewer, SectRecord sect) {
        MemberRecord member = sect.members.get(viewer.getUUID());
        return member == null || member.sameSectImmunity;
    }

    private boolean canViewerJoinFromTarget(ServerPlayer viewer, @Nullable WanderingCultivatorEntity target, SectRecord sect) {
        if (viewer == null || target == null || target.getSectRole() != SectRole.MASTER) {
            return false;
        }
        if (sect.destroyed) {
            return false;
        }
        if (sect.enemies.contains(viewer.getUUID())) {
            return false;
        }
        return !this.isRegisteredMember(viewer);
    }

    private ListTag tasksSnapshot(ServerPlayer viewer, @Nullable WanderingCultivatorEntity target, SectRecord sect) {
        ListTag list = new ListTag();
        boolean viewerMember = sect.members.containsKey(viewer.getUUID()) && sect.id.equals(this.sectIdOf((Entity)viewer));
        boolean viewerEnemy = sect.enemies.contains(viewer.getUUID());
        boolean changed = false;
        for (TaskRecord task : sect.tasks.values()) {
            if (viewerMember) {
                changed |= SectSavedData.refreshPlayerTaskReady(viewer, task);
            }
            boolean nearIssuer = target != null && task.issuerUuid.equals(target.getUUID());
            boolean rewardAvailable = !nearIssuer || SectSavedData.issuerRewardAvailable(target, task);
            list.add(task.snapshot(viewer, viewerMember && !viewerEnemy, nearIssuer, rewardAvailable));
        }
        if (changed) {
            this.setDirty();
        }
        return list;
    }

    private static boolean issuerRewardAvailable(@Nullable WanderingCultivatorEntity issuer, TaskRecord task) {
        if (task == null || task.rewardKind != RewardKind.ITEM) {
            return true;
        }
        return issuer != null && SectSavedData.containerHasItems(issuer.getInventory(), task.rewardStack);
    }

    private static boolean refreshPlayerTaskReady(ServerPlayer player, TaskRecord task) {
        if (player == null || task == null) {
            return false;
        }
        UUID playerId = player.getUUID();
        if (!task.acceptedBy.contains(playerId) || task.completedBy.contains(playerId) || task.readyBy.contains(playerId)) {
            return false;
        }
        if (!SectSavedData.playerHasItems(player.getInventory(), task.requiredStack)) {
            return false;
        }
        return task.readyBy.add(playerId);
    }

    public void ensureBasicTask(String sectId, WanderingCultivatorEntity issuer) {
        TaskReward spell;
        SectRecord record = this.sects.get(sectId);
        if (record == null || issuer == null) {
            return;
        }
        String baseKey = "issuer:" + String.valueOf(issuer.getUUID());
        boolean changed = false;
        ItemStack required = SectSavedData.requirementFor(issuer.getSectRole());
        TaskReward primary = SectSavedData.choosePrimaryRewardFor(issuer, required);
        changed |= SectSavedData.ensureTask(record, baseKey, issuer, required, primary);
        TaskReward technique = SectSavedData.chooseTechniqueRewardFor(issuer);
        if (technique.kind() == RewardKind.TECHNIQUE && primary.kind() != RewardKind.TECHNIQUE) {
            changed |= SectSavedData.ensureTask(record, baseKey + ":technique", issuer, required, technique);
        }
        if ((spell = SectSavedData.chooseSpellRewardFor(issuer)).kind() == RewardKind.SPELL && primary.kind() != RewardKind.SPELL) {
            changed |= SectSavedData.ensureTask(record, baseKey + ":spell", issuer, required, spell);
        }
        if (changed) {
            this.setDirty();
        }
    }

    private static boolean ensureTask(SectRecord record, String key, WanderingCultivatorEntity issuer, ItemStack required, TaskReward reward) {
        TaskRecord next = SectSavedData.createTask(key, issuer, required, reward);
        TaskRecord existing = record.tasks.get(key);
        if (existing != null && existing.matchesDefinition(next)) {
            return false;
        }
        if (existing != null && existing.hasParticipants()) {
            return false;
        }
        record.tasks.put(key, next);
        return true;
    }

    private static TaskRecord createTask(String key, WanderingCultivatorEntity issuer, ItemStack required, TaskReward reward) {
        return new TaskRecord(key, issuer.getUUID(), issuer.getCultivatorName().getString(), issuer.getSectRole(), "sect.friday_cultivation.task.generic_supply", "sect.friday_cultivation.task.generic_supply.condition", SectSavedData.contributionFor(issuer.getSectRole()), required, reward.kind(), reward.stack(), new LinkedHashSet<UUID>(), new LinkedHashSet<UUID>(), new LinkedHashSet<UUID>());
    }

    public TaskActionResult acceptTask(ServerPlayer player, @Nullable WanderingCultivatorEntity issuer, String taskId) {
        TaskContext context = this.resolveTaskContext(player, issuer, taskId);
        if (context == null) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.wrong_issuer", new Object[0]);
        }
        if (!context.isPlayerMember()) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.not_member", context.sect.name);
        }
        if (context.sect.enemies.contains(player.getUUID())) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.hostile", context.sect.name);
        }
        UUID playerId = player.getUUID();
        if (context.task.completedBy.contains(playerId)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.already_completed", Component.translatable((String)context.task.titleKey));
        }
        if (!context.task.acceptedBy.add(playerId)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.already_accepted", Component.translatable((String)context.task.titleKey));
        }
        SectSavedData.refreshPlayerTaskReady(player, context.task);
        this.setDirty();
        return SectSavedData.changed("message.friday_cultivation.sect.task.accepted", Component.translatable((String)context.task.titleKey), issuer.getCultivatorName());
    }

    public TaskActionResult turnInTask(ServerPlayer player, @Nullable WanderingCultivatorEntity issuer, String taskId) {
        TaskContext context = this.resolveTaskContext(player, issuer, taskId);
        if (context == null) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.wrong_issuer", new Object[0]);
        }
        if (!context.isPlayerMember()) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.not_member", context.sect.name);
        }
        if (context.sect.enemies.contains(player.getUUID())) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.hostile", context.sect.name);
        }
        UUID playerId = player.getUUID();
        if (context.task.completedBy.contains(playerId)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.already_completed", Component.translatable((String)context.task.titleKey));
        }
        if (!context.task.acceptedBy.contains(playerId)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.not_accepted", Component.translatable((String)context.task.titleKey));
        }
        if (!SectSavedData.playerHasItems(player.getInventory(), context.task.requiredStack)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.missing_required", context.task.requiredStack.getHoverName(), context.task.requiredStack.getCount());
        }
        ItemStack reward = context.task.rewardStack.copy();
        if (context.task.rewardKind == RewardKind.ITEM && !SectSavedData.containerHasItems(issuer.getInventory(), reward)) {
            return SectSavedData.fail("message.friday_cultivation.sect.task.reward_missing", issuer.getCultivatorName(), reward.getHoverName());
        }
        context.task.readyBy.add(playerId);
        ItemStack required = context.task.requiredStack.copy();
        SectSavedData.consumePlayerItems(player.getInventory(), required);
        SectSavedData.putIntoNpcInventory(issuer, required);
        if (context.task.rewardKind == RewardKind.ITEM) {
            SectSavedData.consumeFromContainer(issuer.getInventory(), reward);
        }
        SectSavedData.giveToPlayer(player, reward.copy());
        context.member.contribution += context.task.contribution;
        context.task.completedBy.add(playerId);
        player.getInventory().setChanged();
        issuer.getInventory().setChanged();
        issuer.regenerateOffers();
        this.setDirty();
        if (reward.isEmpty()) {
            return SectSavedData.changed("message.friday_cultivation.sect.task.completed_contribution_only", Component.translatable((String)context.task.titleKey), context.task.contribution);
        }
        return SectSavedData.changed("message.friday_cultivation.sect.task.completed", Component.translatable((String)context.task.titleKey), reward.getHoverName(), context.task.contribution);
    }

    public TaskIssuerTrackResult trackTaskIssuer(ServerPlayer player, String taskId) {
        if (player == null || taskId == null || taskId.isBlank()) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.invalid", new Object[0]);
        }
        String sectId = this.sectIdOf((Entity)player);
        if (sectId == null || sectId.isBlank()) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.not_member", new Object[0]);
        }
        SectRecord sect = this.sects.get(sectId);
        if (sect == null) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.not_member", new Object[0]);
        }
        MemberRecord member = sect.members.get(player.getUUID());
        if (member == null) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.not_member", sect.name);
        }
        if (sect.enemies.contains(player.getUUID())) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.hostile", sect.name);
        }
        TaskRecord task = sect.tasks.get(taskId);
        if (task == null) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.invalid", new Object[0]);
        }
        if (task.completedBy.contains(player.getUUID())) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.completed", Component.translatable((String)task.titleKey));
        }
        WanderingCultivatorEntity issuer = SectSavedData.findLoadedIssuer(player.serverLevel(), sect, task.issuerUuid);
        if (issuer == null) {
            return SectSavedData.trackFail("message.friday_cultivation.sect.task.track.missing", Component.literal((String)task.issuerName));
        }
        return new TaskIssuerTrackResult(true, issuer.getId(), (Component)Component.translatable((String)"message.friday_cultivation.sect.task.track.marked", (Object[])new Object[]{issuer.getCultivatorName()}));
    }

    @Nullable
    private TaskContext resolveTaskContext(ServerPlayer player, @Nullable WanderingCultivatorEntity issuer, String taskId) {
        if (player == null || issuer == null || taskId == null || taskId.isBlank()) {
            return null;
        }
        String sectId = issuer.getSectId();
        if (sectId == null || sectId.isBlank()) {
            return null;
        }
        SectRecord sect = this.sects.get(sectId);
        if (sect == null) {
            return null;
        }
        TaskRecord task = sect.tasks.get(taskId);
        if (task == null || !task.issuerUuid.equals(issuer.getUUID())) {
            return null;
        }
        MemberRecord member = sect.members.get(player.getUUID());
        boolean memberInSect = member != null && sect.id.equals(this.sectIdOf((Entity)player));
        return new TaskContext(sect, member, task, memberInSect);
    }

    private static TaskActionResult fail(String key, Object ... args) {
        return new TaskActionResult(false, (Component)Component.translatable((String)key, (Object[])args));
    }

    private static TaskActionResult changed(String key, Object ... args) {
        return new TaskActionResult(true, (Component)Component.translatable((String)key, (Object[])args));
    }

    private static TaskIssuerTrackResult trackFail(String key, Object ... args) {
        return new TaskIssuerTrackResult(false, -1, (Component)Component.translatable((String)key, (Object[])args));
    }

    private static int contributionFor(SectRole role) {
        return switch (role) {
            default -> throw new IncompatibleClassChangeError();
            case ANCESTOR -> 30;
            case MASTER -> 24;
            case ELDER -> 18;
            case INNER_DISCIPLE -> 14;
            case OUTER_DISCIPLE, GUARD_DISCIPLE -> 10;
            case SERVANT, NONE -> 6;
        };
    }

    private static ItemStack requirementFor(SectRole role) {
        int count = switch (role) {
            case ANCESTOR, MASTER, ELDER -> 3;
            case INNER_DISCIPLE -> 2;
            default -> 1;
        };
        return new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get(), count);
    }

    private static TaskReward choosePrimaryRewardFor(WanderingCultivatorEntity issuer, ItemStack required) {
        TaskReward item = SectSavedData.chooseItemRewardFor(issuer, required);
        if (item.kind() != RewardKind.CONTRIBUTION_ONLY) {
            return item;
        }
        TaskReward technique = SectSavedData.chooseTechniqueRewardFor(issuer);
        if (technique.kind() != RewardKind.CONTRIBUTION_ONLY) {
            return technique;
        }
        TaskReward spell = SectSavedData.chooseSpellRewardFor(issuer);
        if (spell.kind() != RewardKind.CONTRIBUTION_ONLY) {
            return spell;
        }
        return new TaskReward(RewardKind.CONTRIBUTION_ONLY, ItemStack.EMPTY);
    }

    private static TaskReward chooseItemRewardFor(WanderingCultivatorEntity issuer, ItemStack required) {
        SimpleContainer inv = issuer.getInventory();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            if (stack.isEmpty() || stack.is((Item)ModItems.SECT_TOKEN.get()) || !required.isEmpty() && ItemStack.isSameItemSameTags((ItemStack)stack, (ItemStack)required)) continue;
            ItemStack reward = stack.copy();
            reward.setCount(1);
            return new TaskReward(RewardKind.ITEM, reward);
        }
        return new TaskReward(RewardKind.CONTRIBUTION_ONLY, ItemStack.EMPTY);
    }

    private static TaskReward chooseTechniqueRewardFor(WanderingCultivatorEntity issuer) {
        Technique technique = Technique.byId(issuer.getTechniqueId());
        Item techniqueBook = ModItems.techniqueBookItem(technique);
        if (techniqueBook != null) {
            return new TaskReward(RewardKind.TECHNIQUE, new ItemStack((ItemLike)techniqueBook));
        }
        return new TaskReward(RewardKind.CONTRIBUTION_ONLY, ItemStack.EMPTY);
    }

    private static TaskReward chooseSpellRewardFor(WanderingCultivatorEntity issuer) {
        for (String spellId : issuer.getSpellIds()) {
            Spell spell = Spell.byId(spellId);
            Item spellBook = ModItems.spellBookItem(spell);
            if (spellBook == null) continue;
            return new TaskReward(RewardKind.SPELL, new ItemStack((ItemLike)spellBook));
        }
        return new TaskReward(RewardKind.CONTRIBUTION_ONLY, ItemStack.EMPTY);
    }

    private static boolean playerHasItems(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        int needed = required.getCount();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required) || (needed -= slot.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    private static int countPlayerItems(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return 0;
        }
        int found = 0;
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required)) continue;
            found += slot.getCount();
        }
        return found;
    }

    private static void consumePlayerItems(Inventory inv, ItemStack required) {
        if (required.isEmpty()) {
            return;
        }
        int remaining = required.getCount();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required)) continue;
            int taken = Math.min(slot.getCount(), remaining);
            slot.shrink(taken);
            remaining -= taken;
        }
    }

    private static boolean containerHasItems(SimpleContainer inv, ItemStack required) {
        if (required.isEmpty()) {
            return true;
        }
        int needed = required.getCount();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required) || (needed -= slot.getCount()) > 0) continue;
            return true;
        }
        return false;
    }

    private static void consumeFromContainer(SimpleContainer inv, ItemStack required) {
        if (required.isEmpty()) {
            return;
        }
        int remaining = required.getCount();
        for (int i = 0; i < inv.getContainerSize() && remaining > 0; ++i) {
            ItemStack slot = inv.getItem(i);
            if (!ItemStack.isSameItemSameTags((ItemStack)slot, (ItemStack)required)) continue;
            int taken = Math.min(slot.getCount(), remaining);
            slot.shrink(taken);
            remaining -= taken;
        }
    }

    private static void putIntoNpcInventory(WanderingCultivatorEntity npc, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        ItemStack leftover = npc.getInventory().addItem(stack.copy());
        if (!leftover.isEmpty()) {
            npc.spawnAtLocation(leftover);
        }
    }

    private static void giveToPlayer(ServerPlayer player, ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    public boolean tickNpcTaskAutomation(WanderingCultivatorEntity worker) {
        ServerLevel level;
        block9: {
            block8: {
                Level level2;
                if (worker == null || !((level2 = worker.level()) instanceof ServerLevel)) break block8;
                level = (ServerLevel)level2;
                if (worker.hasSectMembership()) break block9;
            }
            return false;
        }
        SectRecord sect = this.sects.get(worker.getSectId());
        if (sect == null) {
            return false;
        }
        MemberRecord workerMember = sect.members.get(worker.getUUID());
        if (workerMember == null) {
            return false;
        }
        for (TaskRecord task : sect.tasks.values()) {
            WanderingCultivatorEntity issuer;
            if (task.issuerUuid.equals(worker.getUUID()) || task.completedBy.contains(worker.getUUID()) || !SectSavedData.containerHasItems(worker.getInventory(), task.requiredStack) || (issuer = SectSavedData.findLoadedIssuer(level, sect, task.issuerUuid)) == null || !issuer.isAlive() || !sect.id.equals(issuer.getSectId())) continue;
            ItemStack reward = task.rewardStack.copy();
            if (task.rewardKind == RewardKind.ITEM && !SectSavedData.containerHasItems(issuer.getInventory(), reward)) continue;
            task.acceptedBy.add(worker.getUUID());
            task.readyBy.add(worker.getUUID());
            if (worker.distanceToSqr((Entity)issuer) > 9.0) {
                worker.getNavigation().moveTo((Entity)issuer, 1.0);
                this.setDirty();
                return true;
            }
            ItemStack required = task.requiredStack.copy();
            SectSavedData.consumeFromContainer(worker.getInventory(), required);
            SectSavedData.putIntoNpcInventory(issuer, required);
            if (task.rewardKind == RewardKind.ITEM) {
                SectSavedData.consumeFromContainer(issuer.getInventory(), reward);
            }
            SectSavedData.giveRewardToNpc(worker, task, reward);
            workerMember.contribution += task.contribution;
            task.completedBy.add(worker.getUUID());
            worker.getInventory().setChanged();
            issuer.getInventory().setChanged();
            worker.regenerateOffers();
            issuer.regenerateOffers();
            this.setDirty();
            return true;
        }
        return false;
    }

    private static void giveRewardToNpc(WanderingCultivatorEntity npc, TaskRecord task, ItemStack reward) {
        Item item;
        if (npc == null || task == null || reward.isEmpty()) {
            return;
        }
        if (task.rewardKind == RewardKind.TECHNIQUE && (item = reward.getItem()) instanceof TechniqueBookItem && npc.learnSectRewardTechnique(((TechniqueBookItem)item).technique())) {
            return;
        }
        if (task.rewardKind == RewardKind.SPELL && (item = reward.getItem()) instanceof SpellBookItem && npc.learnSectRewardSpell(((SpellBookItem)item).spell())) {
            return;
        }
        SectSavedData.putIntoNpcInventory(npc, reward);
    }

    @Nullable
    private static WanderingCultivatorEntity findLoadedIssuer(ServerLevel level, SectRecord sect, UUID issuerUuid) {
        double radius = Math.max(96.0, (double)sect.radius + 64.0);
        AABB scan = new AABB(sect.center).inflate(radius, 96.0, radius);
        List matches = level.getEntitiesOfClass(WanderingCultivatorEntity.class, scan, npc -> npc.isAlive() && issuerUuid.equals(npc.getUUID()));
        return matches.isEmpty() ? null : (WanderingCultivatorEntity)((Object)matches.get(0));
    }

    private static boolean sameStackDefinition(ItemStack first, ItemStack second) {
        boolean secondEmpty;
        boolean firstEmpty = first == null || first.isEmpty();
        boolean bl = secondEmpty = second == null || second.isEmpty();
        if (firstEmpty || secondEmpty) {
            return firstEmpty == secondEmpty;
        }
        return first.getCount() == second.getCount() && ItemStack.isSameItemSameTags((ItemStack)first, (ItemStack)second);
    }

    private static void putOptionalPos(CompoundTag tag, String key, @Nullable BlockPos pos) {
        if (pos != null) {
            tag.putLong(key, pos.asLong());
        }
    }

    @Nullable
    private static BlockPos readOptionalPos(CompoundTag tag, String key) {
        return tag.contains(key, 4) ? BlockPos.of((long)tag.getLong(key)) : null;
    }

    private static void putStringSet(CompoundTag tag, String key, Set<String> values) {
        ListTag list = new ListTag();
        for (String value : values) {
            list.add(StringTag.valueOf((String)value));
        }
        tag.put(key, (Tag)list);
    }

    private static void readStringSet(CompoundTag tag, String key, Set<String> values) {
        ListTag list = tag.getList(key, 8);
        for (int i = 0; i < list.size(); ++i) {
            values.add(list.getString(i));
        }
    }

    private static void putLongSet(CompoundTag tag, String key, Set<Long> values) {
        ListTag list = new ListTag();
        for (Long value : values) {
            list.add(LongTag.valueOf((long)value));
        }
        tag.put(key, (Tag)list);
    }

    private static void readLongSet(CompoundTag tag, String key, Set<Long> values) {
        ListTag list = tag.getList(key, 4);
        for (int i = 0; i < list.size(); ++i) {
            values.add(((LongTag)list.get(i)).getAsLong());
        }
    }

    private static void putUuidSet(CompoundTag tag, String key, Set<UUID> values) {
        ListTag list = new ListTag();
        for (UUID value : values) {
            CompoundTag row = new CompoundTag();
            row.putUUID("id", value);
            list.add(row);
        }
        tag.put(key, (Tag)list);
    }

    private static void readUuidSet(CompoundTag tag, String key, Set<UUID> values) {
        ListTag list = tag.getList(key, 10);
        for (int i = 0; i < list.size(); ++i) {
            CompoundTag row = list.getCompound(i);
            if (!row.contains("id")) continue;
            values.add(UUID.fromString(row.getString("id")));
        }
    }

    private static void putUuidRoleMap(CompoundTag tag, String key, Map<UUID, SectRole> values) {
        ListTag list = new ListTag();
        for (Map.Entry<UUID, SectRole> entry : values.entrySet()) {
            if (entry.getKey() == null || entry.getValue() == null || entry.getValue() == SectRole.NONE) continue;
            CompoundTag row = new CompoundTag();
            row.putUUID("id", entry.getKey());
            row.putString("role", entry.getValue().id());
            list.add(row);
        }
        tag.put(key, (Tag)list);
    }

    private static void readUuidRoleMap(CompoundTag tag, String key, Map<UUID, SectRole> values) {
        ListTag list = tag.getList(key, 10);
        for (int i = 0; i < list.size(); ++i) {
            SectRole role;
            CompoundTag row = list.getCompound(i);
            if (!row.contains("id") || (role = SectRole.byId(row.getString("role"))) == SectRole.NONE) continue;
            values.put(UUID.fromString(row.getString("id")), role);
        }
    }

    public Component noSectMessage() {
        return Component.translatable((String)"sect.friday_cultivation.none");
    }

    public static final class SectRecord {
        public final String id;
        public final String name;
        public final String dimension;
        public BlockPos center;
        public boolean hasProtectionArray;
        public int radius;
        @Nullable
        public BlockPos corePos;
        private boolean destroyed;
        private final Map<String, BuildingRecord> buildings = new LinkedHashMap<String, BuildingRecord>();
        private final Map<UUID, MemberRecord> members = new LinkedHashMap<UUID, MemberRecord>();
        private final Set<String> spawnClaims = new LinkedHashSet<String>();
        private final Map<Long, ArrayReplacement> arrayReplacements = new LinkedHashMap<Long, ArrayReplacement>();
        private final Set<Long> configuredCores = new LinkedHashSet<Long>();
        private final Set<UUID> enemies = new LinkedHashSet<UUID>();
        private final Map<UUID, SectRole> enemyRetaliationRoles = new LinkedHashMap<UUID, SectRole>();
        private final Set<String> sameSectCombatPairs = new LinkedHashSet<String>();
        private final Map<String, TaskRecord> tasks = new LinkedHashMap<String, TaskRecord>();

        private SectRecord(String id, String name, String dimension, BlockPos center, boolean hasProtectionArray, int radius) {
            this.id = id;
            this.name = name;
            this.dimension = dimension;
            this.center = center.east();
            this.hasProtectionArray = hasProtectionArray;
            this.radius = Math.max(1, radius);
        }

        private boolean updateCenter(BlockPos next) {
            if (next == null || this.center.equals((Object)next)) {
                return false;
            }
            this.center = next.east();
            return true;
        }

        private boolean updateHasProtectionArray(boolean next) {
            if (this.hasProtectionArray == next) {
                return false;
            }
            this.hasProtectionArray = next;
            return true;
        }

        private boolean updateRadius(int next) {
            int safe = Math.max(1, next);
            if (this.radius == safe) {
                return false;
            }
            this.radius = safe;
            return true;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", this.id);
            tag.putString("name", this.name);
            tag.putString("dimension", this.dimension);
            tag.putLong("center", this.center.asLong());
            tag.putBoolean("hasProtectionArray", this.hasProtectionArray);
            tag.putInt("radius", this.radius);
            if (this.corePos != null) {
                tag.putLong("corePos", this.corePos.asLong());
            }
            tag.putBoolean("destroyed", this.destroyed);
            SectSavedData.putStringSet(tag, "spawnClaims", this.spawnClaims);
            SectSavedData.putLongSet(tag, "configuredCores", this.configuredCores);
            SectSavedData.putUuidSet(tag, "enemies", this.enemies);
            SectSavedData.putUuidRoleMap(tag, "enemyRetaliationRoles", this.enemyRetaliationRoles);
            SectSavedData.putStringSet(tag, "sameSectCombatPairs", this.sameSectCombatPairs);
            ListTag buildingList = new ListTag();
            for (BuildingRecord buildingRecord : this.buildings.values()) {
                buildingList.add(buildingRecord.save());
            }
            tag.put("buildings", (Tag)buildingList);
            ListTag memberList = new ListTag();
            for (MemberRecord memberRecord : this.members.values()) {
                memberList.add(memberRecord.save());
            }
            tag.put("members", (Tag)memberList);
            ListTag listTag = new ListTag();
            for (Map.Entry<Long, ArrayReplacement> entry : this.arrayReplacements.entrySet()) {
                CompoundTag row = new CompoundTag();
                row.putLong("pos", entry.getKey().longValue());
                row.putString("type", entry.getValue().name());
                listTag.add(row);
            }
            tag.put("arrayReplacements", (Tag)listTag);
            ListTag listTag2 = new ListTag();
            for (TaskRecord task : this.tasks.values()) {
                listTag2.add(task.save());
            }
            tag.put("tasks", (Tag)listTag2);
            return tag;
        }

        private static SectRecord load(CompoundTag tag) {
            SectRecord record = new SectRecord(tag.getString("id"), tag.getString("name"), tag.getString("dimension"), BlockPos.of((long)tag.getLong("center")), tag.getBoolean("hasProtectionArray"), tag.getInt("radius"));
            if (tag.contains("corePos", 4)) {
                record.corePos = BlockPos.of((long)tag.getLong("corePos"));
            }
            record.destroyed = tag.getBoolean("destroyed");
            SectSavedData.readStringSet(tag, "spawnClaims", record.spawnClaims);
            SectSavedData.readLongSet(tag, "configuredCores", record.configuredCores);
            SectSavedData.readUuidSet(tag, "enemies", record.enemies);
            SectSavedData.readUuidRoleMap(tag, "enemyRetaliationRoles", record.enemyRetaliationRoles);
            SectSavedData.readStringSet(tag, "sameSectCombatPairs", record.sameSectCombatPairs);
            ListTag buildingList = tag.getList("buildings", 10);
            for (int i = 0; i < buildingList.size(); ++i) {
                BuildingRecord building = BuildingRecord.load(buildingList.getCompound(i));
                record.buildings.put(building.key(), building);
            }
            ListTag memberList = tag.getList("members", 10);
            for (int i = 0; i < memberList.size(); ++i) {
                MemberRecord member = MemberRecord.load(memberList.getCompound(i));
                record.members.put(member.uuid, member);
            }
            ListTag replacementList = tag.getList("arrayReplacements", 10);
            for (int i = 0; i < replacementList.size(); ++i) {
                CompoundTag row = replacementList.getCompound(i);
                try {
                    record.arrayReplacements.put(row.getLong("pos"), ArrayReplacement.valueOf(row.getString("type")));
                    continue;
                }
                catch (IllegalArgumentException illegalArgumentException) {
                    // empty catch block
                }
            }
            ListTag taskList = tag.getList("tasks", 10);
            for (int i = 0; i < taskList.size(); ++i) {
                TaskRecord task = TaskRecord.load(taskList.getCompound(i));
                record.tasks.put(task.id, task);
            }
            return record;
        }
    }

    private record BuildingRecord(String type, BlockPos origin, int sizeX, int sizeZ) {
        String key() {
            return this.type + "@" + this.origin.getX() + "," + this.origin.getZ();
        }

        CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("type", this.type);
            tag.putLong("origin", this.origin.asLong());
            tag.putInt("sizeX", this.sizeX);
            tag.putInt("sizeZ", this.sizeZ);
            return tag;
        }

        static BuildingRecord load(CompoundTag tag) {
            return new BuildingRecord(tag.getString("type"), BlockPos.of((long)tag.getLong("origin")), tag.getInt("sizeX"), tag.getInt("sizeZ"));
        }
    }

    public static enum ArrayReplacement {
        IMMORTAL_SPIRIT_CORE,
        IMMORTAL_SECT_PROTECTION_FLAG,
        IMMORTAL_MAZE_FLAG,
        IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT,
        IMMORTAL_REJUVENATION_FLAG;


        @Nullable
        static ArrayReplacement byIndex(int index) {
            return switch (index) {
                case 0 -> IMMORTAL_SPIRIT_CORE;
                case 1 -> IMMORTAL_SECT_PROTECTION_FLAG;
                case 2 -> IMMORTAL_MAZE_FLAG;
                case 3 -> IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT;
                default -> null;
            };
        }

        boolean isFlag() {
            return this == IMMORTAL_SECT_PROTECTION_FLAG || this == IMMORTAL_MAZE_FLAG || this == IMMORTAL_REJUVENATION_FLAG;
        }

        public boolean isSpiritCore() {
            return this == IMMORTAL_SPIRIT_CORE || this == IMMORTAL_SPIRIT_CORE_REJUVENATION_SLOT;
        }

        public static int flagCount() {
            int count = 0;
            int i = 0;
            ArrayReplacement replacement;
            while ((replacement = ArrayReplacement.byIndex(i)) != null) {
                if (replacement.isFlag()) {
                    ++count;
                }
                ++i;
            }
            return count;
        }
    }

    private static final class MemberRecord {
        private final UUID uuid;
        private final String name;
        private final SectRole role;
        private final boolean player;
        @Nullable
        private final BlockPos homePos;
        @Nullable
        private BlockPos bedPos;
        @Nullable
        private BlockPos cushionPos;
        @Nullable
        private BlockPos corePos;
        private boolean sameSectImmunity;
        private int contribution;

        private MemberRecord(UUID uuid, String name, SectRole role, boolean player, @Nullable BlockPos homePos, @Nullable BlockPos bedPos, @Nullable BlockPos cushionPos, @Nullable BlockPos corePos, boolean sameSectImmunity, int contribution) {
            this.uuid = uuid;
            this.name = name == null ? "" : name;
            this.role = role == null ? SectRole.NONE : role;
            this.player = player;
            this.homePos = homePos == null ? null : homePos.east();
            this.bedPos = bedPos == null ? null : bedPos.east();
            this.cushionPos = cushionPos == null ? null : cushionPos.east();
            this.corePos = corePos == null ? null : corePos.east();
            this.sameSectImmunity = sameSectImmunity;
            this.contribution = Math.max(0, contribution);
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("uuid", this.uuid);
            tag.putString("name", this.name);
            tag.putString("role", this.role.id());
            tag.putBoolean("player", this.player);
            SectSavedData.putOptionalPos(tag, "homePos", this.homePos);
            SectSavedData.putOptionalPos(tag, "bedPos", this.bedPos);
            SectSavedData.putOptionalPos(tag, "cushionPos", this.cushionPos);
            SectSavedData.putOptionalPos(tag, "corePos", this.corePos);
            tag.putBoolean("sameSectImmunity", this.sameSectImmunity);
            tag.putInt("contribution", this.contribution);
            return tag;
        }

        private static MemberRecord load(CompoundTag tag) {
            return new MemberRecord(tag.getUUID("uuid"), tag.getString("name"), SectRole.byId(tag.getString("role")), tag.getBoolean("player"), SectSavedData.readOptionalPos(tag, "homePos"), SectSavedData.readOptionalPos(tag, "bedPos"), SectSavedData.readOptionalPos(tag, "cushionPos"), SectSavedData.readOptionalPos(tag, "corePos"), !tag.contains("sameSectImmunity", 1) || tag.getBoolean("sameSectImmunity"), tag.getInt("contribution"));
        }
    }

    private static final class TaskRecord {
        private final String id;
        private final UUID issuerUuid;
        private final String issuerName;
        private final SectRole issuerRole;
        private final String titleKey;
        private final String conditionKey;
        private final int contribution;
        private final ItemStack requiredStack;
        private final RewardKind rewardKind;
        private final ItemStack rewardStack;
        private final Set<UUID> acceptedBy;
        private final Set<UUID> completedBy;
        private final Set<UUID> readyBy;

        private TaskRecord(String id, UUID issuerUuid, String issuerName, SectRole issuerRole, String titleKey, String conditionKey, int contribution, ItemStack requiredStack, RewardKind rewardKind, ItemStack rewardStack, Set<UUID> acceptedBy, Set<UUID> completedBy, Set<UUID> readyBy) {
            this.id = id == null ? "" : id;
            this.issuerUuid = issuerUuid == null ? new UUID(0L, 0L) : issuerUuid;
            this.issuerName = issuerName == null ? "" : issuerName;
            this.issuerRole = issuerRole == null ? SectRole.NONE : issuerRole;
            this.titleKey = titleKey == null || titleKey.isBlank() ? "sect.friday_cultivation.task.generic_supply" : titleKey;
            this.conditionKey = conditionKey == null || conditionKey.isBlank() ? "sect.friday_cultivation.task.generic_supply.condition" : conditionKey;
            this.contribution = Math.max(0, contribution);
            this.requiredStack = requiredStack == null || requiredStack.isEmpty() ? new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get()) : requiredStack.copy();
            this.rewardKind = rewardKind == null ? RewardKind.CONTRIBUTION_ONLY : rewardKind;
            this.rewardStack = rewardStack == null ? ItemStack.EMPTY : rewardStack.copy();
            this.acceptedBy = acceptedBy == null ? new LinkedHashSet() : acceptedBy;
            this.completedBy = completedBy == null ? new LinkedHashSet() : completedBy;
            this.readyBy = readyBy == null ? new LinkedHashSet() : readyBy;
        }

        private CompoundTag snapshot(ServerPlayer viewer, boolean viewerMember, boolean nearIssuer, boolean rewardAvailable) {
            CompoundTag row = new CompoundTag();
            row.putString("id", this.id);
            row.putString("issuerName", this.issuerName);
            row.putString("issuerRole", this.issuerRole.id());
            row.putString("titleKey", this.titleKey);
            row.putString("conditionKey", this.conditionKey);
            row.putInt("contribution", this.contribution);
            row.putString("rewardKind", this.rewardKind.name());
            row.put("requiredStack", (Tag)this.requiredStack.save(new CompoundTag()));
            if (!this.rewardStack.isEmpty()) {
                row.put("rewardStack", (Tag)this.rewardStack.save(new CompoundTag()));
            }
            UUID viewerId = viewer.getUUID();
            boolean accepted = this.acceptedBy.contains(viewerId);
            boolean completed = this.completedBy.contains(viewerId);
            int requiredCount = this.requiredStack.isEmpty() ? 0 : this.requiredStack.getCount();
            int heldRequired = this.requiredStack.isEmpty() ? 0 : Math.min(requiredCount, SectSavedData.countPlayerItems(viewer.getInventory(), this.requiredStack));
            boolean ready = accepted && !completed && (this.readyBy.contains(viewerId) || heldRequired >= requiredCount);
            row.putBoolean("accepted", accepted);
            row.putBoolean("completed", completed);
            row.putBoolean("ready", ready);
            row.putBoolean("rewardAvailable", rewardAvailable);
            row.putInt("heldRequired", heldRequired);
            row.putInt("requiredCount", requiredCount);
            row.putBoolean("requiresIssuer", viewerMember && !completed && !nearIssuer);
            row.putBoolean("canAccept", viewerMember && nearIssuer && !accepted && !completed);
            row.putBoolean("canTurnIn", viewerMember && nearIssuer && accepted && !completed && ready && rewardAvailable);
            return row;
        }

        private CompoundTag save() {
            CompoundTag tag = new CompoundTag();
            tag.putString("id", this.id);
            tag.putUUID("issuerUuid", this.issuerUuid);
            tag.putString("issuerName", this.issuerName);
            tag.putString("issuerRole", this.issuerRole.id());
            tag.putString("titleKey", this.titleKey);
            tag.putString("conditionKey", this.conditionKey);
            tag.putInt("contribution", this.contribution);
            tag.putString("rewardKind", this.rewardKind.name());
            tag.put("requiredStack", (Tag)this.requiredStack.save(new CompoundTag()));
            if (!this.rewardStack.isEmpty()) {
                tag.put("rewardStack", (Tag)this.rewardStack.save(new CompoundTag()));
            }
            SectSavedData.putUuidSet(tag, "acceptedBy", this.acceptedBy);
            SectSavedData.putUuidSet(tag, "completedBy", this.completedBy);
            SectSavedData.putUuidSet(tag, "readyBy", this.readyBy);
            return tag;
        }

        private static TaskRecord load(CompoundTag tag) {
            UUID issuer = tag.contains("issuerUuid") ? tag.getUUID("issuerUuid") : new UUID(0L, 0L);
            ItemStack required = tag.contains("requiredStack", 10) ? ItemStack.of((CompoundTag)tag.getCompound("requiredStack")) : new ItemStack((ItemLike)ModItems.LOW_SPIRIT_STONE.get());
            ItemStack reward = tag.contains("rewardStack", 10) ? ItemStack.of((CompoundTag)tag.getCompound("rewardStack")) : ItemStack.EMPTY;
            LinkedHashSet<UUID> accepted = new LinkedHashSet<UUID>();
            LinkedHashSet<UUID> completed = new LinkedHashSet<UUID>();
            LinkedHashSet<UUID> ready = new LinkedHashSet<UUID>();
            SectSavedData.readUuidSet(tag, "acceptedBy", accepted);
            SectSavedData.readUuidSet(tag, "completedBy", completed);
            SectSavedData.readUuidSet(tag, "readyBy", ready);
            return new TaskRecord(tag.getString("id"), issuer, tag.getString("issuerName"), SectRole.byId(tag.getString("issuerRole")), tag.getString("titleKey"), tag.getString("conditionKey"), tag.getInt("contribution"), required, RewardKind.byName(tag.getString("rewardKind")), reward, accepted, completed, ready);
        }

        private boolean hasParticipants() {
            return !this.acceptedBy.isEmpty() || !this.completedBy.isEmpty() || !this.readyBy.isEmpty();
        }

        private boolean matchesDefinition(TaskRecord other) {
            if (other == null) {
                return false;
            }
            return this.id.equals(other.id) && this.issuerUuid.equals(other.issuerUuid) && this.issuerName.equals(other.issuerName) && this.issuerRole == other.issuerRole && this.titleKey.equals(other.titleKey) && this.conditionKey.equals(other.conditionKey) && this.contribution == other.contribution && this.rewardKind == other.rewardKind && SectSavedData.sameStackDefinition(this.requiredStack, other.requiredStack) && SectSavedData.sameStackDefinition(this.rewardStack, other.rewardStack);
        }
    }

    private static enum RewardKind {
        ITEM,
        TECHNIQUE,
        SPELL,
        CONTRIBUTION_ONLY;


        private static RewardKind byName(String name) {
            if (name == null || name.isBlank()) {
                return CONTRIBUTION_ONLY;
            }
            for (RewardKind kind : RewardKind.values()) {
                if (!kind.name().equals(name)) continue;
                return kind;
            }
            return CONTRIBUTION_ONLY;
        }
    }

    private record TaskReward(RewardKind kind, ItemStack stack) {
    }

    private record TaskContext(SectRecord sect, @Nullable MemberRecord member, TaskRecord task, boolean isPlayerMember) {
    }

    public record TaskActionResult(boolean changed, Component message) {
    }

    public record TaskIssuerTrackResult(boolean found, int entityId, Component message) {
    }
}

