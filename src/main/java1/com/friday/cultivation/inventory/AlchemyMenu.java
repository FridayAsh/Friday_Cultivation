package com.friday.cultivation.inventory;

import com.friday.cultivation.alchemy.AlchemyRecipe;
import com.friday.cultivation.alchemy.AlchemyRecipes;
import com.friday.cultivation.block.alchemy.AlchemyCoreBlockEntity;
import com.friday.cultivation.block.alchemy.AlchemyFurnaceStructure;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.registry.ModBlocks;
import com.friday.cultivation.registry.ModMenuTypes;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.DataSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

/**
 * 炼丹菜单 — 完整复刻原模组 AlchemyMenu。
 * 6输入槽 + 1输出槽 + 6个DataSlot（灵气low/high/max low/high/炼丹ticks/总ticks）。
 */
public class AlchemyMenu extends AbstractContainerMenu {
    public static final int INPUT_SLOTS = 6;
    public static final int OUTPUT_SLOTS = 1;

    private final BlockPos corePos;
    private final SimpleContainer ioContainer;
    private final Player player;
    private final AlchemyCoreBlockEntity blockEntityRef;
    private final DataSlot qiLow = DataSlot.standalone();
    private final DataSlot qiHigh = DataSlot.standalone();
    private final DataSlot maxQiLow = DataSlot.standalone();
    private final DataSlot maxQiHigh = DataSlot.standalone();
    private final DataSlot craftingTicks = DataSlot.standalone();
    private final DataSlot craftingTotalTicks = DataSlot.standalone();

    public AlchemyMenu(int containerId, Inventory inv, BlockPos corePos) {
        super(ModMenuTypes.ALCHEMY.get(), containerId);
        this.corePos = corePos;
        this.player = inv.player;
        BlockEntity be = inv.player.level().getBlockEntity(corePos);
        this.blockEntityRef = be instanceof AlchemyCoreBlockEntity ? (AlchemyCoreBlockEntity) be : null;
        this.ioContainer = this.blockEntityRef != null ? this.blockEntityRef.getIoContainer() : new SimpleContainer(7);
        this.addSlots(inv);
        this.addDataSlot(this.qiLow);
        this.addDataSlot(this.qiHigh);
        this.addDataSlot(this.maxQiLow);
        this.addDataSlot(this.maxQiHigh);
        this.addDataSlot(this.craftingTicks);
        this.addDataSlot(this.craftingTotalTicks);
        if (this.blockEntityRef != null) {
            this.updateQiDataSlots(this.blockEntityRef.getCurrentQi(), this.blockEntityRef.getMaxQi());
            this.craftingTicks.set(this.blockEntityRef.isCrafting() ? this.blockEntityRef.getCraftingTicks() : 0);
            this.craftingTotalTicks.set(this.blockEntityRef.getCraftingTotalTicks());
        }
    }

    public int getCraftingTicks() {
        return this.craftingTicks.get();
    }

    public int getCraftingTotalTicks() {
        return this.craftingTotalTicks.get();
    }

    public boolean isCrafting() {
        return this.craftingTicks.get() > 0;
    }

    public AlchemyMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readBlockPos());
    }

    private void addSlots(Inventory inv) {
        for (int row = 0; row < 2; ++row) {
            for (int col = 0; col < 3; ++col) {
                this.addSlot(new Slot(this.ioContainer, col + row * 3, 100 + col * 18, 32 + row * 18));
            }
        }
        this.addSlot(new Slot(this.ioContainer, 6, 178, 41) {
            @Override public boolean mayPlace(@NotNull ItemStack stack) { return false; }
        });
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.addSlot(new Slot(inv, col + row * 9 + 9, 98 + col * 18, 120 + row * 18));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.addSlot(new Slot(inv, col, 98 + col * 18, 180));
        }
    }

    public BlockPos getCorePos() {
        return this.corePos;
    }

    public SimpleContainer getIoContainer() {
        return this.ioContainer;
    }

    public Player getPlayer() {
        return this.player;
    }

    public AlchemyCoreBlockEntity getBlockEntity() {
        return this.blockEntityRef;
    }

    public long getCurrentQi() {
        return AlchemyMenu.packLong(this.qiHigh.get(), this.qiLow.get());
    }

    public long getMaxQi() {
        return AlchemyMenu.packLong(this.maxQiHigh.get(), this.maxQiLow.get());
    }

    private static long packLong(int high, int low) {
        return (long) high << 32 | (long) low & 0xFFFFFFFFL;
    }

    private void updateQiDataSlots(long current, long max) {
        this.qiLow.set((int) (current & 0xFFFFFFFFL));
        this.qiHigh.set((int) (current >>> 32));
        this.maxQiLow.set((int) (max & 0xFFFFFFFFL));
        this.maxQiHigh.set((int) (max >>> 32));
    }

    @Override
    public void broadcastChanges() {
        if (this.blockEntityRef != null && !this.player.level().isClientSide) {
            this.updateQiDataSlots(this.blockEntityRef.getCurrentQi(), this.blockEntityRef.getMaxQi());
            this.craftingTicks.set(this.blockEntityRef.isCrafting() ? this.blockEntityRef.getCraftingTicks() : 0);
            this.craftingTotalTicks.set(this.blockEntityRef.getCraftingTotalTicks());
        }
        super.broadcastChanges();
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (player.level().getBlockState(this.corePos).getBlock() != ModBlocks.ALCHEMY_CORE.get()) {
            return false;
        }
        if (!AlchemyFurnaceStructure.isComplete(player.level(), this.corePos)) {
            return false;
        }
        return player.distanceToSqr(this.corePos.getX() + 0.5, this.corePos.getY() + 0.5, this.corePos.getZ() + 0.5) <= 64.0;
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        int totalIo;
        Slot slot = this.slots.get(slotIndex);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack copy = stack.copy();
        int playerInvStart = totalIo = 7;
        int playerInvEnd = totalIo + 36;
        if (slotIndex < totalIo ? !this.moveItemStackTo(stack, playerInvStart, playerInvEnd, true) : !this.moveItemStackTo(stack, 0, 6, false)) {
            return ItemStack.EMPTY;
        }
        if (stack.isEmpty()) {
            slot.set(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        return copy;
    }

    @Override
    public void removed(@NotNull Player player) {
        super.removed(player);
    }

    public int countPossiblePillsForRecipe(AlchemyRecipe recipe) {
        double qiCostMultiplier = PhysiqueBonusHelper.alchemyQiCostMultiplier(this.player);
        long effectiveQi = qiCostMultiplier <= 0.0 ? this.getCurrentQi() : (long) Math.floor((double) this.getCurrentQi() / qiCostMultiplier);
        return recipe.countPossiblePills(this.ioContainer, effectiveQi);
    }

    public List<AlchemyRecipe> getAllRecipes() {
        return AlchemyRecipes.all(this.player.level());
    }

    public AlchemyRecipe findRecipeByMaterials() {
        boolean hasInput = false;
        for (int i = 0; i < 6; ++i) {
            ItemStack s = this.ioContainer.getItem(i);
            if (s.isEmpty()) continue;
            hasInput = true;
            break;
        }
        if (!hasInput) {
            return null;
        }
        for (AlchemyRecipe r : AlchemyRecipes.all(this.player.level())) {
            if (!r.matchesIngredients(this.ioContainer, 6)) continue;
            return r;
        }
        return null;
    }
}
