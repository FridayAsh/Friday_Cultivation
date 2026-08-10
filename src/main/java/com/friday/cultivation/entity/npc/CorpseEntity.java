/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.BlockPos$MutableBlockPos
 *  net.minecraft.core.Direction
 *  net.minecraft.core.Direction$Axis
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.syncher.EntityDataAccessor
 *  net.minecraft.network.syncher.EntityDataSerializer
 *  net.minecraft.network.syncher.EntityDataSerializers
 *  net.minecraft.network.syncher.SynchedEntityData
 *  net.minecraft.server.MinecraftServer
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.util.Mth
 *  net.minecraft.world.Container
 *  net.minecraft.world.Containers
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.MenuProvider
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.SimpleMenuProvider
 *  net.minecraft.world.damagesource.DamageSource
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.EntityType
 *  net.minecraft.world.entity.MoverType
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.ChestMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.BlockGetter
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.Block
 *  net.minecraft.world.level.block.Blocks
 *  net.minecraft.world.level.block.HorizontalDirectionalBlock
 *  net.minecraft.world.level.block.SkullBlock
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  net.minecraft.world.phys.Vec3
 *  net.minecraft.world.phys.shapes.VoxelShape
 *  net.minecraftforge.network.NetworkHooks
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.entity.npc;

import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.entity.npc.CultivatorNames;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModBlocks;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializer;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CorpseEntity
extends Entity {
    private static final EntityDataAccessor<String> DATA_OWNER_NAME = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.STRING);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Boolean> DATA_NPC_CORPSE = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_NPC_SKIN_VARIANT = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_NPC_DIFU_REAPER = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> DATA_NPC_SURNAME_IDX = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_NPC_GIVEN_IDX = SynchedEntityData.defineId(CorpseEntity.class, (EntityDataSerializer)EntityDataSerializers.INT);
    private static final int MAX_LIFE_TICKS = 6000;
    private static final int NPC_LOOT_SLOTS = 27;
    private final SimpleContainer lootInventory = new SimpleContainer(27);
    private int lifeTicks = 0;

    public CorpseEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = false;
        this.noCulling = true;
    }

    public void setupCorpse(ServerPlayer owner) {
        this.entityData.set(DATA_OWNER_NAME, owner.getGameProfile().getName());
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.setYRot(owner.getYRot());
        this.yRotO = owner.getYRot();
    }

    public void setupNpcCorpse(WanderingCultivatorEntity owner) {
        this.entityData.set(DATA_OWNER_NAME, owner.getCultivatorName().getString());
        this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
        this.entityData.set(DATA_NPC_CORPSE, true);
        this.entityData.set(DATA_NPC_SKIN_VARIANT, owner.getSkinVariant());
        this.entityData.set(DATA_NPC_DIFU_REAPER, owner.isDifuReaper());
        this.entityData.set(DATA_NPC_SURNAME_IDX, owner.getSurnameIdx());
        this.entityData.set(DATA_NPC_GIVEN_IDX, owner.getGivenIdx());
        this.setYRot(owner.getYRot());
        this.yRotO = owner.getYRot();
    }

    public void settleOnGround() {
        if (this.level().isClientSide) {
            return;
        }
        Level level = this.level();
        int x = Mth.floor((double)this.getX());
        int z = Mth.floor((double)this.getZ());
        int startY = Mth.floor((double)this.getY());
        int minY = level.getMinBuildHeight();
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos(x, startY, z);
        for (int y = startY; y >= minY; --y) {
            cursor.setY(y);
            VoxelShape shape = level.getBlockState((BlockPos)cursor).getCollisionShape(level, cursor);
            if (shape.isEmpty()) continue;
            double topY = (double)y + shape.max(Direction.Axis.Y);
            if (topY <= this.getY() + 1.0E-4) {
                this.setPos(this.getX(), topY, this.getZ());
                this.setDeltaMovement(Vec3.ZERO);
                this.setOnGround(true);
            }
            return;
        }
    }

    public String getOwnerName() {
        return (String)this.entityData.get(DATA_OWNER_NAME);
    }

    public Component getOwnerDisplayName() {
        if (this.isNpcCorpse()) {
            return CultivatorNames.display(this.getNpcSurnameIdx(), this.getNpcGivenIdx());
        }
        return Component.literal((String)this.getOwnerName());
    }

    public UUID getOwnerUuid() {
        return this.entityData.get(DATA_OWNER_UUID).orElse(null);
    }

    public boolean isNpcCorpse() {
        return (Boolean)this.entityData.get(DATA_NPC_CORPSE);
    }

    public int getNpcSkinVariant() {
        return (Integer)this.entityData.get(DATA_NPC_SKIN_VARIANT);
    }

    public boolean isNpcDifuReaperCorpse() {
        return (Boolean)this.entityData.get(DATA_NPC_DIFU_REAPER);
    }

    public int getNpcSurnameIdx() {
        return (Integer)this.entityData.get(DATA_NPC_SURNAME_IDX);
    }

    public int getNpcGivenIdx() {
        return (Integer)this.entityData.get(DATA_NPC_GIVEN_IDX);
    }

    public boolean moveItemIntoLoot(ItemStack stack) {
        if (stack.isEmpty()) {
            return true;
        }
        ItemStack rest = this.lootInventory.addItem(stack.copy());
        this.lootInventory.setChanged();
        if (!rest.isEmpty() && !this.level().isClientSide) {
            Containers.dropItemStack((Level)this.level(), (double)this.getX(), (double)(this.getY() + 0.2), (double)this.getZ(), (ItemStack)rest);
        }
        return rest.isEmpty();
    }

    public boolean hasLoot() {
        for (int i = 0; i < this.lootInventory.getContainerSize(); ++i) {
            if (this.lootInventory.getItem(i).isEmpty()) continue;
            return true;
        }
        return false;
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_OWNER_NAME, "");
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_NPC_CORPSE, false);
        this.entityData.define(DATA_NPC_SKIN_VARIANT, 0);
        this.entityData.define(DATA_NPC_DIFU_REAPER, false);
        this.entityData.define(DATA_NPC_SURNAME_IDX, 0);
        this.entityData.define(DATA_NPC_GIVEN_IDX, 0);
    }

    public void tick() {
        boolean stillSoul;
        ServerPlayer p;
        super.tick();
        if (this.level().isClientSide) {
            return;
        }
        if (!this.onGround()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0, -0.04, 0.0));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.96, 0.98, 0.96));
        } else if (this.getDeltaMovement().lengthSqr() > 1.0E-6) {
            this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.0, 0.5));
            this.move(MoverType.SELF, this.getDeltaMovement());
        }
        ++this.lifeTicks;
        if (this.isNpcCorpse()) {
            if (this.lifeTicks > ModCommonConfig.npcCorpseDecayTicks()) {
                this.decayIntoBones();
            }
            return;
        }
        UUID owner = this.getOwnerUuid();
        MinecraftServer server = this.getServer();
        if (owner != null && server != null && (p = server.getPlayerList().getPlayer(owner)) != null && !(stillSoul = CultivationCapability.get((Player)p).map(CultivationData::isSoulState).orElse(false).booleanValue())) {
            this.discard();
            return;
        }
        if (this.lifeTicks > 6000) {
            this.discard();
        }
    }

    private void decayIntoBones() {
        ServerLevel sl;
        BlockPos head;
        Level level = this.level();
        if (level instanceof ServerLevel && (head = this.findBonePlacement(sl = (ServerLevel)level)) != null) {
            Direction bodyDir = Direction.fromYRot((double)this.getYRot());
            int rotation = Mth.floor((double)((double)(this.getYRot() * 16.0f / 360.0f) + 0.5)) & 0xF;
            sl.setBlock(head, (BlockState)Blocks.SKELETON_SKULL.defaultBlockState().setValue((Property)SkullBlock.ROTATION, (Comparable)Integer.valueOf(rotation)), 3);
            BlockPos bonePos = head.relative(bodyDir.getOpposite());
            if (sl.getBlockState(bonePos).isAir()) {
                sl.setBlock(bonePos, (BlockState)((Block)ModBlocks.BONE_BLOCK.get()).defaultBlockState().setValue((Property)HorizontalDirectionalBlock.FACING, (Comparable)bodyDir), 3);
            }
        }
        this.discard();
    }

    @Nullable
    private BlockPos findBonePlacement(ServerLevel sl) {
        BlockPos base = this.blockPosition();
        if (sl.getBlockState(base).isAir()) {
            return base;
        }
        BlockPos above = base.above();
        if (sl.getBlockState(above).isAir()) {
            return above;
        }
        return null;
    }

    @NotNull
    public InteractionResult interact(@NotNull Player player, @NotNull InteractionHand hand) {
        if (hand != InteractionHand.MAIN_HAND || !this.isNpcCorpse()) {
            return InteractionResult.PASS;
        }
        if (this.level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!this.hasLoot()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.corpse.empty"), true);
            return InteractionResult.CONSUME;
        }
        if (player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            NetworkHooks.openScreen((ServerPlayer)sp, (MenuProvider)new SimpleMenuProvider((containerId, playerInv, p) -> ChestMenu.threeRows((int)containerId, (Inventory)playerInv, (Container)this.lootInventory), (Component)Component.translatable((String)"screen.friday_cultivation.corpse.title", (Object[])new Object[]{this.getOwnerDisplayName()})));
        }
        return InteractionResult.CONSUME;
    }

    public boolean isInvulnerable() {
        return true;
    }

    public boolean hurt(@NotNull DamageSource source, float amount) {
        return false;
    }

    public boolean isPickable() {
        return this.isNpcCorpse();
    }

    public float getPickRadius() {
        return this.isNpcCorpse() ? 0.35f : 0.0f;
    }

    public boolean isPushable() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return false;
    }

    protected void readAdditionalSaveData(@NotNull CompoundTag tag) {
        if (tag.contains("ownerName")) {
            this.entityData.set(DATA_OWNER_NAME, tag.getString("ownerName"));
        }
        if (tag.contains("ownerUuid")) {
            this.entityData.set(DATA_OWNER_UUID, Optional.of(tag.getUUID("ownerUuid")));
        }
        this.entityData.set(DATA_NPC_CORPSE, tag.getBoolean("npcCorpse"));
        this.entityData.set(DATA_NPC_SKIN_VARIANT, tag.getInt("npcSkinVariant"));
        this.entityData.set(DATA_NPC_DIFU_REAPER, tag.getBoolean("npcDifuReaper"));
        this.entityData.set(DATA_NPC_SURNAME_IDX, tag.getInt("npcSurnameIdx"));
        this.entityData.set(DATA_NPC_GIVEN_IDX, tag.getInt("npcGivenIdx"));
        if (tag.contains("LootInventory", 9)) {
            this.lootInventory.fromTag(tag.getList("LootInventory", 10));
        }
        this.lifeTicks = tag.getInt("lifeTicks");
    }

    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        tag.putString("ownerName", this.getOwnerName());
        UUID u = this.getOwnerUuid();
        if (u != null) {
            tag.putUUID("ownerUuid", u);
        }
        tag.putBoolean("npcCorpse", this.isNpcCorpse());
        tag.putInt("npcSkinVariant", this.getNpcSkinVariant());
        tag.putBoolean("npcDifuReaper", this.isNpcDifuReaperCorpse());
        tag.putInt("npcSurnameIdx", this.getNpcSurnameIdx());
        tag.putInt("npcGivenIdx", this.getNpcGivenIdx());
        tag.put("LootInventory", (Tag)this.lootInventory.createTag());
        tag.putInt("lifeTicks", this.lifeTicks);
    }

    @NotNull
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket((Entity)this);
    }
}

