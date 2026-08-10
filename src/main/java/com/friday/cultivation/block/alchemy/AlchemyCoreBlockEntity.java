/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.BlockPos
 *  net.minecraft.core.NonNullList
 *  net.minecraft.core.particles.ParticleOptions
 *  net.minecraft.core.particles.ParticleTypes
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.network.Connection
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.protocol.Packet
 *  net.minecraft.network.protocol.game.ClientGamePacketListener
 *  net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket
 *  net.minecraft.resources.ResourceKey
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.ContainerHelper
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.item.ItemEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  net.minecraft.world.level.block.entity.BlockEntityType
 *  net.minecraft.world.level.block.state.BlockState
 *  net.minecraft.world.level.block.state.properties.Property
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.block.alchemy;

import com.friday.cultivation.block.alchemy.AlchemyCoreBlock;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.cultivation.alchemy.AlchemyRecipes;
import com.friday.cultivation.cultivation.alchemy.PillTier;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SpiritLockHandler;
import com.friday.cultivation.registry.ModBlockEntities;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AlchemyCoreBlockEntity
extends BlockEntity {
    public static final long MAX_QI = 100000L;
    private static final Map<ResourceKey<Level>, Set<AlchemyCoreBlockEntity>> LOADED_CORES = new ConcurrentHashMap<ResourceKey<Level>, Set<AlchemyCoreBlockEntity>>();
    public static final int TICKS_PER_PILL = 100;
    public static final int IO_INPUT_SLOTS = 6;
    public static final int IO_OUTPUT_SLOTS = 1;
    public static final int IO_TOTAL_SLOTS = 7;
    private long currentQi = 0L;
    private boolean crafting = false;
    private int craftingTicks = 0;
    private int craftingTotalTicks = 100;
    private String craftingRecipeId = "";
    private int craftingPillCount = 0;
    private int craftingResultTier = -1;
    @Nullable
    private UUID craftingPlayerUuid = null;
    private final SimpleContainer ioContainer = new SimpleContainer(7){

        public void setChanged() {
            super.setChanged();
            AlchemyCoreBlockEntity.this.setChanged();
        }
    };

    public AlchemyCoreBlockEntity(BlockPos pos, BlockState state) {
        super((BlockEntityType)ModBlockEntities.ALCHEMY_CORE.get(), pos, state);
    }

    public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel) {
            LOADED_CORES.computeIfAbsent((ResourceKey<Level>)this.level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(this);
        }
    }

    public void setRemoved() {
        this.unregisterLoadedCore();
        super.setRemoved();
    }

    private void unregisterLoadedCore() {
        if (this.level == null) {
            return;
        }
        Set<AlchemyCoreBlockEntity> set = LOADED_CORES.get(this.level.dimension());
        if (set != null) {
            set.remove((Object)this);
        }
    }

    public static void forLoaded(ServerLevel level, Consumer<AlchemyCoreBlockEntity> consumer) {
        Set<AlchemyCoreBlockEntity> set = LOADED_CORES.get(level.dimension());
        if (set == null || set.isEmpty()) {
            return;
        }
        set.removeIf(core -> core == null || core.isRemoved() || core.level != level || level.getBlockEntity(core.getBlockPos()) != core);
        for (AlchemyCoreBlockEntity core2 : set) {
            consumer.accept(core2);
        }
    }

    public long getCurrentQi() {
        return this.currentQi;
    }

    public long getMaxQi() {
        return 100000L;
    }

    public boolean isCrafting() {
        return this.crafting;
    }

    public int getCraftingTicks() {
        return this.craftingTicks;
    }

    public int getCraftingTotalTicks() {
        return this.craftingTotalTicks;
    }

    public SimpleContainer getIoContainer() {
        return this.ioContainer;
    }

    public long addQi(long amount) {
        if (amount <= 0L) {
            return 0L;
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return 0L;
        }
        long room = 100000L - this.currentQi;
        long actual = Math.min(amount, room);
        this.currentQi += actual;
        if (actual > 0L) {
            this.markDirtyAndSync();
        }
        return actual;
    }

    public boolean deductQi(long amount) {
        if (amount <= 0L) {
            return true;
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return false;
        }
        if (this.currentQi < amount) {
            return false;
        }
        this.currentQi -= amount;
        this.markDirtyAndSync();
        return true;
    }

    public void setCurrentQi(long qi) {
        this.currentQi = Math.max(0L, Math.min(100000L, qi));
        this.markDirtyAndSync();
    }

    public void beginCrafting(String recipeId, int pillCount, int resultTierOrdinal, @Nullable UUID playerUuid) {
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return;
        }
        this.crafting = true;
        this.craftingTicks = 0;
        this.craftingTotalTicks = 100 * Math.max(1, pillCount);
        this.craftingRecipeId = recipeId;
        this.craftingPillCount = pillCount;
        this.craftingResultTier = resultTierOrdinal;
        this.craftingPlayerUuid = playerUuid;
        if (this.level != null && !this.level.isClientSide) {
            this.level.setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)AlchemyCoreBlock.LIT, (Comparable)Boolean.valueOf(true)), 3);
        }
        this.markDirtyAndSync();
    }

    public void serverTick() {
        if (!this.crafting) {
            return;
        }
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) {
            return;
        }
        ++this.craftingTicks;
        if (this.craftingTicks % 5 == 0) {
            this.spawnStructureFlames();
        }
        if (this.craftingTicks >= this.craftingTotalTicks) {
            this.completeCrafting();
        } else {
            this.markDirtyAndSync();
        }
    }

    private void spawnStructureFlames() {
        Level level = this.level;
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel)level;
        BlockPos core = this.getBlockPos();
        int n = 4 + this.level.random.nextInt(3);
        for (int i = 0; i < n; ++i) {
            int dx = this.level.random.nextInt(5) - 2;
            int dz = this.level.random.nextInt(7) - 3;
            int dy = this.level.random.nextInt(4) - 1;
            double px = (double)core.getX() + 0.5 + (double)dx;
            double py = (double)(core.getY() + dy) + 0.7;
            double pz = (double)core.getZ() + 0.5 + (double)dz;
            sl.sendParticles((ParticleOptions)ParticleTypes.FLAME, px, py, pz, 1, 0.1, 0.1, 0.1, 0.01);
            if (this.level.random.nextInt(3) != 0) continue;
            sl.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, px, py + 0.3, pz, 1, 0.1, 0.05, 0.1, 0.01);
        }
        sl.sendParticles((ParticleOptions)ParticleTypes.FLAME, (double)core.getX() + 0.5, (double)core.getY() + 0.85, (double)core.getZ() + 0.5, 3, 0.15, 0.05, 0.15, 0.02);
    }

    private void completeCrafting() {
        ServerPlayer player;
        if (this.level == null || this.level.isClientSide) {
            return;
        }
        AlchemyRecipe recipe = AlchemyRecipes.byId(this.level, this.craftingRecipeId).orElse(null);
        ServerLevel sl = (ServerLevel)this.level;
        ServerPlayer serverPlayer = player = this.craftingPlayerUuid != null ? sl.getServer().getPlayerList().getPlayer(this.craftingPlayerUuid) : null;
        if (recipe != null) {
            if (this.craftingResultTier >= 0 && this.craftingResultTier < PillTier.values().length) {
                PillTier tier = PillTier.values()[this.craftingResultTier];
                Item pillItem = recipe.outputs().get((Object)tier);
                if (pillItem != null && this.craftingPillCount > 0) {
                    ItemStack output = new ItemStack((ItemLike)pillItem, this.craftingPillCount);
                    boolean placed = this.tryPushToOutputSlot(output);
                    if (!placed) {
                        this.spawnPillsAtCore(output);
                    }
                    if (player != null) {
                        int xp = AlchemyRank.xpGainFor(tier) * this.craftingPillCount;
                        CultivationCapability.get((Player)player).ifPresent(data -> {
                            boolean leveled = data.addAlchemyXp(xp);
                            CapabilityEvents.syncToClient(player);
                            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.alchemy.success_single", (Object[])new Object[]{this.craftingPillCount, tier.displayName()}).withStyle(ChatFormatting.GOLD), false);
                            if (leveled) {
                                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.alchemy.rank_up", (Object[])new Object[]{data.getAlchemyRank().displayName()}).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), false);
                                sl.playSound(null, this.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                            }
                        });
                    }
                    sl.sendParticles((ParticleOptions)ParticleTypes.HAPPY_VILLAGER, (double)this.getBlockPos().getX() + 0.5, (double)this.getBlockPos().getY() + 1.0, (double)this.getBlockPos().getZ() + 0.5, 30, 1.5, 0.5, 1.5, 0.05);
                    sl.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            } else {
                if (player != null) {
                    int failXp = AlchemyRank.xpGainForFailure() * this.craftingPillCount;
                    ServerPlayer fp = player;
                    CultivationCapability.get((Player)player).ifPresent(data -> {
                        boolean leveled = data.addAlchemyXp(failXp);
                        CapabilityEvents.syncToClient(fp);
                        fp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.alchemy.all_failed_xp", (Object[])new Object[]{this.craftingPillCount, failXp}).withStyle(ChatFormatting.RED), false);
                        if (leveled) {
                            fp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.alchemy.rank_up", (Object[])new Object[]{data.getAlchemyRank().displayName()}).withStyle(new ChatFormatting[]{ChatFormatting.GOLD, ChatFormatting.BOLD}), false);
                            sl.playSound(null, this.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                    });
                }
                sl.sendParticles((ParticleOptions)ParticleTypes.LARGE_SMOKE, (double)this.getBlockPos().getX() + 0.5, (double)this.getBlockPos().getY() + 1.0, (double)this.getBlockPos().getZ() + 0.5, 30, 1.5, 0.5, 1.5, 0.02);
                sl.playSound(null, this.getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
        }
        this.crafting = false;
        this.craftingTicks = 0;
        this.craftingRecipeId = "";
        this.craftingPillCount = 0;
        this.craftingResultTier = -1;
        this.craftingPlayerUuid = null;
        this.level.setBlock(this.getBlockPos(), (BlockState)this.getBlockState().setValue((Property)AlchemyCoreBlock.LIT, (Comparable)Boolean.valueOf(false)), 3);
        this.markDirtyAndSync();
    }

    private boolean tryPushToOutputSlot(ItemStack output) {
        ItemStack outSlot = this.ioContainer.getItem(6);
        if (outSlot.isEmpty()) {
            this.ioContainer.setItem(6, output);
            return true;
        }
        if (outSlot.getItem() == output.getItem() && outSlot.getCount() + output.getCount() <= outSlot.getMaxStackSize()) {
            outSlot.grow(output.getCount());
            this.ioContainer.setChanged();
            return true;
        }
        return false;
    }

    private void spawnPillsAtCore(ItemStack output) {
        if (this.level == null) {
            return;
        }
        ItemEntity ent = new ItemEntity(this.level, (double)this.getBlockPos().getX() + 0.5, (double)this.getBlockPos().getY() + 1.0, (double)this.getBlockPos().getZ() + 0.5, output);
        ent.setDeltaMovement(0.0, 0.2, 0.0);
        this.level.addFreshEntity((Entity)ent);
    }

    private void markDirtyAndSync() {
        this.setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("currentQi", this.currentQi);
        tag.putBoolean("crafting", this.crafting);
        tag.putInt("craftingTicks", this.craftingTicks);
        tag.putInt("craftingTotalTicks", this.craftingTotalTicks);
        tag.putString("craftingRecipeId", this.craftingRecipeId);
        tag.putInt("craftingPillCount", this.craftingPillCount);
        tag.putInt("craftingResultTier", this.craftingResultTier);
        if (this.craftingPlayerUuid != null) {
            tag.putUUID("craftingPlayerUuid", this.craftingPlayerUuid);
        }
        NonNullList<ItemStack> ioList = NonNullList.withSize(7, ItemStack.EMPTY);
        for (int i = 0; i < 7; ++i) {
            ioList.set(i, this.ioContainer.getItem(i));
        }
        ContainerHelper.saveAllItems(tag, ioList);
    }

    public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.currentQi = tag.getLong("currentQi");
        this.crafting = tag.getBoolean("crafting");
        this.craftingTicks = tag.getInt("craftingTicks");
        this.craftingTotalTicks = tag.contains("craftingTotalTicks") ? tag.getInt("craftingTotalTicks") : 100;
        this.craftingRecipeId = tag.getString("craftingRecipeId");
        this.craftingPillCount = tag.getInt("craftingPillCount");
        this.craftingResultTier = tag.getInt("craftingResultTier");
        this.craftingPlayerUuid = tag.contains("craftingPlayerUuid") ? tag.getUUID("craftingPlayerUuid") : null;
        NonNullList<ItemStack> ioList = NonNullList.withSize(7, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, ioList);
        for (int i = 0; i < 7; ++i) {
            this.ioContainer.setItem(i, ioList.get(i));
        }
    }

    @NotNull
    public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        this.saveAdditional(tag);
        return tag;
    }

    public void handleUpdateTag(@NotNull CompoundTag tag) {
        this.load(tag);
    }

    @Nullable
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create((BlockEntity)this);
    }

    public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) {
            this.load(tag);
        }
    }
}

