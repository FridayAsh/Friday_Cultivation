package com.friday.cultivation.block.alchemy;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.alchemy.AlchemyRecipes;
import com.friday.cultivation.alchemy.PillTier;
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
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 炼丹核心方块实体 — 完整复刻原模组 AlchemyCoreBlockEntity。
 * 管理灵气存储、炼丹进度、IO容器（6输入+1输出）、多方块结构校验。
 */
public class AlchemyCoreBlockEntity extends BlockEntity {
    public static final long MAX_QI = 100000L;
    private static final Map<ResourceKey<Level>, Set<AlchemyCoreBlockEntity>> LOADED_CORES = new ConcurrentHashMap<>();
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
    @Nullable private UUID craftingPlayerUuid = null;

    private final SimpleContainer ioContainer = new SimpleContainer(7) {
        @Override public void setChanged() { super.setChanged(); AlchemyCoreBlockEntity.this.setChanged(); }
    };

    public AlchemyCoreBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ALCHEMY_CORE.get(), pos, state);
    }

    @Override public void onLoad() {
        super.onLoad();
        if (this.level instanceof ServerLevel) {
            LOADED_CORES.computeIfAbsent(this.level.dimension(), key -> ConcurrentHashMap.newKeySet()).add(this);
        }
    }

    @Override public void setRemoved() {
        this.unregisterLoadedCore();
        super.setRemoved();
    }

    private void unregisterLoadedCore() {
        if (this.level == null) return;
        Set<AlchemyCoreBlockEntity> set = LOADED_CORES.get(this.level.dimension());
        if (set != null) set.remove(this);
    }

    public static void forLoaded(ServerLevel level, Consumer<AlchemyCoreBlockEntity> consumer) {
        Set<AlchemyCoreBlockEntity> set = LOADED_CORES.get(level.dimension());
        if (set == null || set.isEmpty()) return;
        set.removeIf(core -> core == null || core.isRemoved() || core.level != level || level.getBlockEntity(core.getBlockPos()) != core);
        for (AlchemyCoreBlockEntity core : set) consumer.accept(core);
    }

    public long getCurrentQi() { return this.currentQi; }
    public long getMaxQi() { return MAX_QI; }
    public boolean isCrafting() { return this.crafting; }
    public int getCraftingTicks() { return this.craftingTicks; }
    public int getCraftingTotalTicks() { return this.craftingTotalTicks; }
    public SimpleContainer getIoContainer() { return this.ioContainer; }

    public long addQi(long amount) {
        if (amount <= 0L) return 0L;
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) return 0L;
        long room = MAX_QI - this.currentQi;
        long actual = Math.min(amount, room);
        this.currentQi += actual;
        if (actual > 0L) markDirtyAndSync();
        return actual;
    }

    public boolean deductQi(long amount) {
        if (amount <= 0L) return true;
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) return false;
        if (this.currentQi < amount) return false;
        this.currentQi -= amount;
        markDirtyAndSync();
        return true;
    }

    public void setCurrentQi(long qi) {
        this.currentQi = Math.max(0L, Math.min(MAX_QI, qi));
        markDirtyAndSync();
    }

    public void beginCrafting(String recipeId, int pillCount, int resultTierOrdinal, @Nullable UUID playerUuid) {
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) return;
        this.crafting = true;
        this.craftingTicks = 0;
        this.craftingTotalTicks = TICKS_PER_PILL * Math.max(1, pillCount);
        this.craftingRecipeId = recipeId;
        this.craftingPillCount = pillCount;
        this.craftingResultTier = resultTierOrdinal;
        this.craftingPlayerUuid = playerUuid;
        if (this.level != null && !this.level.isClientSide) {
            this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(AlchemyCoreBlock.LIT, true), 3);
        }
        markDirtyAndSync();
    }

    public void serverTick() {
        if (!this.crafting) return;
        if (this.level == null || this.level.isClientSide) return;
        if (SpiritLockHandler.isBlockLocked(this.level, this.getBlockPos())) return;
        ++this.craftingTicks;
        if (this.craftingTicks % 5 == 0) spawnStructureFlames();
        if (this.craftingTicks >= this.craftingTotalTicks) completeCrafting();
        else markDirtyAndSync();
    }

    private void spawnStructureFlames() {
        Level level = this.level;
        if (!(level instanceof ServerLevel sl)) return;
        BlockPos core = this.getBlockPos();
        int n = 4 + this.level.random.nextInt(3);
        for (int i = 0; i < n; ++i) {
            int dx = this.level.random.nextInt(5) - 2;
            int dz = this.level.random.nextInt(7) - 3;
            int dy = this.level.random.nextInt(4) - 1;
            double px = core.getX() + 0.5 + dx;
            double py = core.getY() + dy + 0.7;
            double pz = core.getZ() + 0.5 + dz;
            sl.sendParticles(ParticleTypes.FLAME, px, py, pz, 1, 0.1, 0.1, 0.1, 0.01);
            if (this.level.random.nextInt(3) == 0)
                sl.sendParticles(ParticleTypes.SMOKE, px, py + 0.3, pz, 1, 0.1, 0.05, 0.1, 0.01);
        }
        sl.sendParticles(ParticleTypes.FLAME, core.getX() + 0.5, core.getY() + 0.85, core.getZ() + 0.5, 3, 0.15, 0.05, 0.15, 0.02);
    }

    private void completeCrafting() {
        if (this.level == null || this.level.isClientSide) return;
        AlchemyRecipe recipe = AlchemyRecipes.byId(this.level, this.craftingRecipeId).orElse(null);
        ServerLevel sl = (ServerLevel) this.level;
        ServerPlayer player = this.craftingPlayerUuid != null ? sl.getServer().getPlayerList().getPlayer(this.craftingPlayerUuid) : null;
        if (recipe != null) {
            if (this.craftingResultTier >= 0 && this.craftingResultTier < PillTier.values().length) {
                PillTier tier = PillTier.values()[this.craftingResultTier];
                Item pillItem = recipe.outputs().get(tier);
                if (pillItem != null && this.craftingPillCount > 0) {
                    ItemStack output = new ItemStack(pillItem, this.craftingPillCount);
                    boolean placed = this.tryPushToOutputSlot(output);
                    if (!placed) this.spawnPillsAtCore(output);
                    if (player != null) {
                        int xp = AlchemyRank.xpGainFor(tier) * this.craftingPillCount;
                        CultivationCapability.get(player).ifPresent(data -> {
                            boolean leveled = data.addAlchemyXp(xp);
                            CapabilityEvents.syncToClient(player);
                            player.displayClientMessage(Component.translatable("message.friday_cultivation.alchemy.success_single", this.craftingPillCount, tier.displayName()).withStyle(ChatFormatting.GOLD), false);
                            if (leveled) {
                                player.displayClientMessage(Component.translatable("message.friday_cultivation.alchemy.rank_up", data.getAlchemyRank().displayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                                sl.playSound(null, this.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                            }
                        });
                    }
                    sl.sendParticles(ParticleTypes.HAPPY_VILLAGER, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.0, this.getBlockPos().getZ() + 0.5, 30, 1.5, 0.5, 1.5, 0.05);
                    sl.playSound(null, this.getBlockPos(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
            } else {
                if (player != null) {
                    int failXp = AlchemyRank.xpGainForFailure() * this.craftingPillCount;
                    ServerPlayer fp = player;
                    CultivationCapability.get(player).ifPresent(data -> {
                        boolean leveled = data.addAlchemyXp(failXp);
                        CapabilityEvents.syncToClient(fp);
                        fp.displayClientMessage(Component.translatable("message.friday_cultivation.alchemy.all_failed_xp", this.craftingPillCount, failXp).withStyle(ChatFormatting.RED), false);
                        if (leveled) {
                            fp.displayClientMessage(Component.translatable("message.friday_cultivation.alchemy.rank_up", data.getAlchemyRank().displayName()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), false);
                            sl.playSound(null, this.getBlockPos(), SoundEvents.PLAYER_LEVELUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                        }
                    });
                }
                sl.sendParticles(ParticleTypes.SMOKE, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.0, this.getBlockPos().getZ() + 0.5, 30, 1.5, 0.5, 1.5, 0.02);
                sl.playSound(null, this.getBlockPos(), SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.8f, 0.8f);
            }
        }
        this.crafting = false;
        this.craftingTicks = 0;
        this.craftingRecipeId = "";
        this.craftingPillCount = 0;
        this.craftingResultTier = -1;
        this.craftingPlayerUuid = null;
        this.level.setBlock(this.getBlockPos(), this.getBlockState().setValue(AlchemyCoreBlock.LIT, false), 3);
        markDirtyAndSync();
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
        if (this.level == null) return;
        ItemEntity ent = new ItemEntity(this.level, this.getBlockPos().getX() + 0.5, this.getBlockPos().getY() + 1.0, this.getBlockPos().getZ() + 0.5, output);
        ent.setDeltaMovement(0.0, 0.2, 0.0);
        this.level.addFreshEntity(ent);
    }

    private void markDirtyAndSync() {
        setChanged();
        if (this.level != null && !this.level.isClientSide) {
            this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
        }
    }

    @Override protected void saveAdditional(@NotNull CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putLong("currentQi", this.currentQi);
        tag.putBoolean("crafting", this.crafting);
        tag.putInt("craftingTicks", this.craftingTicks);
        tag.putInt("craftingTotalTicks", this.craftingTotalTicks);
        tag.putString("craftingRecipeId", this.craftingRecipeId);
        tag.putInt("craftingPillCount", this.craftingPillCount);
        tag.putInt("craftingResultTier", this.craftingResultTier);
        if (this.craftingPlayerUuid != null) tag.putUUID("craftingPlayerUuid", this.craftingPlayerUuid);
        NonNullList<ItemStack> ioList = NonNullList.withSize(7, ItemStack.EMPTY);
        for (int i = 0; i < 7; ++i) ioList.set(i, this.ioContainer.getItem(i));
        ContainerHelper.saveAllItems(tag, ioList);
    }

    @Override public void load(@NotNull CompoundTag tag) {
        super.load(tag);
        this.currentQi = tag.getLong("currentQi");
        this.crafting = tag.getBoolean("crafting");
        this.craftingTicks = tag.getInt("craftingTicks");
        this.craftingTotalTicks = tag.contains("craftingTotalTicks") ? tag.getInt("craftingTotalTicks") : 100;
        this.craftingRecipeId = tag.getString("craftingRecipeId");
        this.craftingPillCount = tag.getInt("craftingPillCount");
        this.craftingResultTier = tag.getInt("craftingResultTier");
        this.craftingPlayerUuid = tag.hasUUID("craftingPlayerUuid") ? tag.getUUID("craftingPlayerUuid") : null;
        NonNullList<ItemStack> ioList = NonNullList.withSize(7, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, ioList);
        for (int i = 0; i < 7; ++i) this.ioContainer.setItem(i, ioList.get(i));
    }

    @NotNull @Override public CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override public void handleUpdateTag(@NotNull CompoundTag tag) { load(tag); }

    @Nullable @Override public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override public void onDataPacket(@NotNull Connection net, @NotNull ClientboundBlockEntityDataPacket pkt) {
        CompoundTag tag = pkt.getTag();
        if (tag != null) load(tag);
    }
}
