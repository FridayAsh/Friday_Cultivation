/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.core.BlockPos
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.DataSlot
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.LevelReader
 *  net.minecraft.world.level.block.entity.BlockEntity
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.inventory;

import com.friday.cultivation.block.refining.RefiningCoreBlockEntity;
import com.friday.cultivation.block.refining.RefiningFurnaceStructure;
import com.friday.cultivation.cultivation.refining.RefiningRecipe;
import com.friday.cultivation.cultivation.refining.RefiningRecipes;
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
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.NotNull;

public class RefiningMenu
extends AbstractContainerMenu {
    public static final int INPUT_SLOTS = 6;
    public static final int OUTPUT_SLOTS = 1;
    private final BlockPos corePos;
    private final SimpleContainer ioContainer;
    private final Player player;
    private final RefiningCoreBlockEntity blockEntityRef;
    private final DataSlot qiLow = DataSlot.standalone();
    private final DataSlot qiHigh = DataSlot.standalone();
    private final DataSlot maxQiLow = DataSlot.standalone();
    private final DataSlot maxQiHigh = DataSlot.standalone();
    private final DataSlot craftingTicks = DataSlot.standalone();
    private final DataSlot craftingTotalTicks = DataSlot.standalone();
    private final DataSlot autoRetry = DataSlot.standalone();

    public RefiningMenu(int containerId, Inventory inv, BlockPos corePos) {
        super((MenuType)ModMenuTypes.REFINING.get(), containerId);
        RefiningCoreBlockEntity rce;
        this.corePos = corePos;
        this.player = inv.player;
        BlockEntity be = inv.player.level().getBlockEntity(corePos);
        this.blockEntityRef = be instanceof RefiningCoreBlockEntity ? (rce = (RefiningCoreBlockEntity)be) : null;
        this.ioContainer = this.blockEntityRef != null ? this.blockEntityRef.getIoContainer() : new SimpleContainer(7);
        this.addSlots(inv);
        this.addDataSlot(this.qiLow);
        this.addDataSlot(this.qiHigh);
        this.addDataSlot(this.maxQiLow);
        this.addDataSlot(this.maxQiHigh);
        this.addDataSlot(this.craftingTicks);
        this.addDataSlot(this.craftingTotalTicks);
        this.addDataSlot(this.autoRetry);
        if (this.blockEntityRef != null) {
            this.updateQiDataSlots(this.blockEntityRef.getCurrentQi(), this.blockEntityRef.getMaxQi());
            this.craftingTicks.set(this.blockEntityRef.isCrafting() ? this.blockEntityRef.getCraftingTicks() : 0);
            this.craftingTotalTicks.set(this.blockEntityRef.getCraftingTotalTicks());
            this.autoRetry.set(this.blockEntityRef.isAutoRetryUntilSuccess() ? 1 : 0);
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

    public RefiningMenu(int containerId, Inventory inv, FriendlyByteBuf buf) {
        this(containerId, inv, buf.readBlockPos());
    }

    private void addSlots(Inventory inv) {
        int col;
        int row;
        for (row = 0; row < 2; ++row) {
            for (col = 0; col < 3; ++col) {
                this.addSlot(new Slot((Container)this.ioContainer, col + row * 3, 100 + col * 18, 32 + row * 18));
            }
        }
        this.addSlot(new Slot((Container)this.ioContainer, 6, 178, 41){

            public boolean mayPlace(@NotNull ItemStack stack) {
                return false;
            }
        });
        for (row = 0; row < 3; ++row) {
            for (col = 0; col < 9; ++col) {
                this.addSlot(new Slot((Container)inv, col + row * 9 + 9, 98 + col * 18, 120 + row * 18));
            }
        }
        for (int col2 = 0; col2 < 9; ++col2) {
            this.addSlot(new Slot((Container)inv, col2, 98 + col2 * 18, 180));
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

    public RefiningCoreBlockEntity getBlockEntity() {
        return this.blockEntityRef;
    }

    public long getCurrentQi() {
        return RefiningMenu.packLong(this.qiHigh.get(), this.qiLow.get());
    }

    public long getMaxQi() {
        return RefiningMenu.packLong(this.maxQiHigh.get(), this.maxQiLow.get());
    }

    private static long packLong(int high, int low) {
        return (long)high << 32 | (long)low & 0xFFFFFFFFL;
    }

    private void updateQiDataSlots(long current, long max) {
        this.qiLow.set((int)(current & 0xFFFFFFFFL));
        this.qiHigh.set((int)(current >>> 32));
        this.maxQiLow.set((int)(max & 0xFFFFFFFFL));
        this.maxQiHigh.set((int)(max >>> 32));
    }

    public void broadcastChanges() {
        if (this.blockEntityRef != null && !this.player.level().isClientSide) {
            this.updateQiDataSlots(this.blockEntityRef.getCurrentQi(), this.blockEntityRef.getMaxQi());
            this.craftingTicks.set(this.blockEntityRef.isCrafting() ? this.blockEntityRef.getCraftingTicks() : 0);
            this.craftingTotalTicks.set(this.blockEntityRef.getCraftingTotalTicks());
            this.autoRetry.set(this.blockEntityRef.isAutoRetryUntilSuccess() ? 1 : 0);
        }
        super.broadcastChanges();
    }

    public boolean isAutoRetryEnabled() {
        return this.autoRetry.get() != 0;
    }

    public boolean onTake(@NotNull Player player) {
        if (player.level().getBlockState(this.corePos).getBlock() != ModBlocks.REFINING_CORE.get()) {
            return false;
        }
        if (!RefiningFurnaceStructure.isComplete((LevelReader)player.level(), this.corePos)) {
            return false;
        }
        return player.distanceToSqr((double)this.corePos.getX() + 0.5, (double)this.corePos.getY() + 0.5, (double)this.corePos.getZ() + 0.5) <= 64.0;
    }

    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        int totalIo;
        Slot slot = (Slot)this.slots.get(slotIndex);
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

    public void removed(@NotNull Player player) {
        super.removed(player);
    }

    public int countPossibleItemsForRecipe(RefiningRecipe recipe) {
        return recipe.countPossibleItems(this.ioContainer, this.getCurrentQi());
    }

    public List<RefiningRecipe> getAllRecipes() {
        return RefiningRecipes.all(this.player.level());
    }

    public RefiningRecipe findRecipeByMaterials() {
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
        for (RefiningRecipe r : RefiningRecipes.all(this.player.level())) {
            if (!r.matchesIngredients(this.ioContainer, 6)) continue;
            return r;
        }
        return null;
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        if (player.level().getBlockState(this.corePos).getBlock() != ModBlocks.REFINING_CORE.get()) {
            return false;
        }
        if (!RefiningFurnaceStructure.isComplete((LevelReader)player.level(), this.corePos)) {
            return false;
        }
        return player.distanceToSqr((double)this.corePos.getX() + 0.5, (double)this.corePos.getY() + 0.5, (double)this.corePos.getZ() + 0.5) <= 64.0;
    }
}
