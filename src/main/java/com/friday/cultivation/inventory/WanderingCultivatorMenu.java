/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.world.Container
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.inventory.MenuType
 *  net.minecraft.world.inventory.Slot
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.trading.MerchantOffers
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.inventory;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.registry.ModMenuTypes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class WanderingCultivatorMenu
extends AbstractContainerMenu {
    private final int entityId;
    @Nullable
    private final WanderingCultivatorEntity npcRef;
    private final Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();
    @Nullable
    private ListTag npcInventoryTag;
    private final SimpleContainer sellContainer = new SimpleContainer(1);
    private final List<Slot> playerInventorySlots = new ArrayList<Slot>();
    @Nullable
    private Slot sellSlot;
    private boolean sellSlotVisible = true;
    private boolean playerSlotsVisible = false;

    public WanderingCultivatorMenu(int containerId, Inventory inv, WanderingCultivatorEntity npc) {
        super((MenuType)ModMenuTypes.WANDERING_CULTIVATOR.get(), containerId);
        this.entityId = npc.getId();
        this.npcRef = npc;
        this.tradingPlayer = inv.player;
        this.addPlayerSlots(inv);
    }

    public WanderingCultivatorMenu(int containerId, Inventory inv, int entityId) {
        super((MenuType)ModMenuTypes.WANDERING_CULTIVATOR.get(), containerId);
        WanderingCultivatorEntity wc;
        this.entityId = entityId;
        this.tradingPlayer = inv.player;
        Entity e = inv.player.level().getEntity(entityId);
        this.npcRef = e instanceof WanderingCultivatorEntity ? (wc = (WanderingCultivatorEntity)e) : null;
        this.addPlayerSlots(inv);
    }

    private void addPlayerSlots(Inventory inv) {
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.playerInventorySlots.add(this.addSlot(new DetailToggleSlot(inv, col + row * 9 + 9, 8 + col * 18, 160 + row * 18)));
            }
        }
        for (int col = 0; col < 9; ++col) {
            this.playerInventorySlots.add(this.addSlot(new DetailToggleSlot(inv, col, 8 + col * 18, 218)));
        }
        this.sellSlot = this.addSlot(new Slot((Container)this.sellContainer, 0, 192, 222){

            public boolean allowModification() {
                return WanderingCultivatorMenu.this.sellSlotVisible && WanderingCultivatorMenu.this.playerSlotsVisible;
            }
        });
    }

    public SimpleContainer getSellContainer() {
        return this.sellContainer;
    }

    public List<Slot> getPlayerInventorySlots() {
        return Collections.unmodifiableList(this.playerInventorySlots);
    }

    @Nullable
    public Slot getSellSlot() {
        return this.sellSlot;
    }

    public void setSellSlotVisible(boolean visible) {
        this.sellSlotVisible = visible;
    }

    public void setPlayerSlotsVisible(boolean visible) {
        this.playerSlotsVisible = visible;
    }

    public int getEntityId() {
        return this.entityId;
    }

    @Nullable
    public WanderingCultivatorEntity getCultivator() {
        return this.npcRef;
    }

    public void setOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    public MerchantOffers getOffers() {
        if (this.npcRef != null && !this.tradingPlayer.level().isClientSide && this.npcRef.getOffers() != null) {
            return this.npcRef.getOffers();
        }
        return this.offers;
    }

    public void setNpcInventoryTag(@Nullable ListTag tag) {
        this.npcInventoryTag = tag;
    }

    public List<ItemStack> getDisplayInventory() {
        ArrayList<ItemStack> result = new ArrayList<ItemStack>();
        if (this.npcInventoryTag == null) {
            if (this.npcRef != null) {
                SimpleContainer inv = this.npcRef.getInventory();
                for (int i = 0; i < inv.getContainerSize(); ++i) {
                    result.add(inv.getItem(i));
                }
            }
            return result;
        }
        for (int i = 0; i < this.npcInventoryTag.size(); ++i) {
            result.add(ItemStack.of((CompoundTag)this.npcInventoryTag.getCompound(i)));
        }
        return result;
    }

    public boolean onTake(@NotNull Player player) {
        return this.npcRef != null && this.npcRef.isAlive();
    }

    public void canPlaceItem(@NotNull Player player) {
        ItemStack stack;
        super.removed(player);
        if (this.npcRef != null && !player.level().isClientSide && this.npcRef.getTradingPlayer() == player) {
            this.npcRef.setTradingPlayer(null);
        }
        if (!(player.level().isClientSide || (stack = this.sellContainer.removeItemNoUpdate(0)).isEmpty() || player.getInventory().add(stack))) {
            player.drop(stack, false);
        }
    }

    @NotNull
    public ItemStack quickMoveStack(@NotNull Player player, int slotIndex) {
        return ItemStack.EMPTY;
    }

    private class DetailToggleSlot
    extends Slot {
        DetailToggleSlot(Inventory inventory, int index, int x, int y) {
            super((Container)inventory, index, x, y);
        }

        public boolean allowModification() {
            return WanderingCultivatorMenu.this.playerSlotsVisible;
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.npcRef != null && this.npcRef.isAlive() && player.distanceToSqr((double)this.npcRef.getX() + 0.5, (double)this.npcRef.getEyeY(), (double)this.npcRef.getZ() + 0.5) <= 64.0;
    }
}
