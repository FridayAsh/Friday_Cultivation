package com.friday.cultivation.inventory;

import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.List;

/**
 * 散修菜单 - 玩家右键散修 NPC 时打开的交易容器菜单。
 * 完全照搬原 mod: xiaoxiang.cultivation.inventory.WanderingCultivatorMenu
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public class WanderingCultivatorMenu extends AbstractContainerMenu {
    public static final int NPC_INVENTORY_SIZE = 9;
    private final Container npcContainer = new SimpleContainer(NPC_INVENTORY_SIZE);
    private final int npcEntityId;
    private final Player player;
    private final Player tradingPlayer;
    private MerchantOffers offers = new MerchantOffers();
    @javax.annotation.Nullable
    private net.minecraft.nbt.ListTag npcInventoryTag;
    private boolean playerSlotsVisible = false;
    @javax.annotation.Nullable
    private final WanderingCultivatorEntity npcRef;
    private final SimpleContainer sellContainer = new SimpleContainer(1);
    private final java.util.List<Slot> playerInventorySlots = new java.util.ArrayList<>();
    @javax.annotation.Nullable
    private Slot sellSlot;
    private boolean sellSlotVisible = true;

    public WanderingCultivatorMenu(int containerId, Inventory playerInv, int npcEntityId) {
        super(com.friday.cultivation.registry.ModMenuTypes.WANDERING_CULTIVATOR.get(), containerId);
        this.npcEntityId = npcEntityId;
        this.player = playerInv.player;
        this.tradingPlayer = playerInv.player;
        net.minecraft.world.entity.Entity e = playerInv.player.level().getEntity(npcEntityId);
        this.npcRef = e instanceof WanderingCultivatorEntity ? (WanderingCultivatorEntity) e : null;
        for (int i = 0; i < NPC_INVENTORY_SIZE; ++i) {
            this.addSlot(new Slot(npcContainer, i, 8 + i * 18, 18));
        }
        for (int row = 0; row < 3; ++row) {
            for (int col = 0; col < 9; ++col) {
                this.playerInventorySlots.add(this.addSlot(new Slot(playerInv, col + row * 9 + 9, 8 + col * 18, 86 + row * 18)));
            }
        }
        for (int i = 0; i < 9; ++i) {
            this.playerInventorySlots.add(this.addSlot(new Slot(playerInv, i, 8 + i * 18, 144)));
        }
        this.sellSlot = this.addSlot(new Slot(this.sellContainer, 0, 192, 222) {
            @Override
            public boolean isActive() {
                return WanderingCultivatorMenu.this.sellSlotVisible && WanderingCultivatorMenu.this.playerSlotsVisible;
            }
        });
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        ItemStack result = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            result = stack.copy();
            if (index < NPC_INVENTORY_SIZE) {
                if (!this.moveItemStackTo(stack, NPC_INVENTORY_SIZE, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(stack, 0, NPC_INVENTORY_SIZE, false)) {
                    return ItemStack.EMPTY;
                }
            }
            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }
        return result;
    }

    @Override
    public boolean stillValid(Player player) {
        if (!(player.level().getEntity(this.npcEntityId) instanceof WanderingCultivatorEntity npc)) {
            return false;
        }
        return player.distanceTo(npc) < 8.0;
    }

    public int getNpcEntityId() {
        return this.npcEntityId;
    }

    public Container getNpcContainer() {
        return this.npcContainer;
    }

    /** 散修 NPC 引用（照搬原模组 getCultivator） */
    @javax.annotation.Nullable
    public WanderingCultivatorEntity getCultivator() {
        return this.npcRef;
    }

    /** 卖货槽（照搬原模组 getSellContainer） */
    public SimpleContainer getSellContainer() {
        return this.sellContainer;
    }

    /**
     * 严格 1:1 复刻原 mod WanderingCultivatorMenu.setNpcInventoryTag。
     * 将 ListTag 中的物品装入 NPC 容器槽位。
     * 由 SyncCultivatorInventoryPacket.handle 调用。
     */
    public void setNpcInventoryTag(ListTag inventoryTag) {
        for (int i = 0; i < NPC_INVENTORY_SIZE; ++i) {
            if (i < inventoryTag.size()) {
                this.npcContainer.setItem(i, ItemStack.of(inventoryTag.getCompound(i)));
            } else {
                this.npcContainer.setItem(i, ItemStack.EMPTY);
            }
        }
    }

    /**
     * 严格 1:1 复刻原 mod WanderingCultivatorMenu.setOffers。
     * 由 SyncCultivatorOffersPacket.handle 调用。
     */
    public void setOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    /** 交易报价（照搬原模组 getOffers：服务端优先读 NPC 实时 offers） */
    public MerchantOffers getOffers() {
        if (this.npcRef != null && !this.tradingPlayer.level().isClientSide() && this.npcRef.getOffers() != null) {
            return this.npcRef.getOffers();
        }
        return this.offers;
    }

    /** 卖货槽位（照搬原模组 getSellSlot） */
    @javax.annotation.Nullable
    public Slot getSellSlot() {
        return this.sellSlot;
    }

    /** 玩家背包槽列表（照搬原模组 getPlayerInventorySlots） */
    public java.util.List<Slot> getPlayerInventorySlots() {
        return java.util.Collections.unmodifiableList(this.playerInventorySlots);
    }

    /** 显示背包（照搬原模组 getDisplayInventory：先 NBT tag 后 NPC 实际容器） */
    public java.util.List<ItemStack> getDisplayInventory() {
        java.util.ArrayList<ItemStack> result = new java.util.ArrayList<>();
        if (this.npcInventoryTag == null) {
            if (this.npcRef != null) {
                net.minecraft.world.SimpleContainer inv = this.npcRef.getInventory();
                for (int i = 0; i < inv.getContainerSize(); ++i) {
                    result.add(inv.getItem(i));
                }
            }
            return result;
        }
        for (int i = 0; i < this.npcInventoryTag.size(); ++i) {
            result.add(ItemStack.of(this.npcInventoryTag.getCompound(i)));
        }
        return result;
    }

    /** 实体 id（照搬原模组 getEntityId） */
    public int getEntityId() {
        return this.npcEntityId;
    }

    /** 卖货槽可见性（照搬原模组 setSellSlotVisible） */
    public void setSellSlotVisible(boolean visible) {
        this.sellSlotVisible = visible;
    }

    /** 玩家槽可见性（照搬原模组 setPlayerSlotsVisible） */
    public void setPlayerSlotsVisible(boolean visible) {
        this.playerSlotsVisible = visible;
    }

    /** 交易玩家（照搬原模组 tradingPlayer 字段） */
    public Player getTradingPlayer() {
        return this.tradingPlayer;
    }

    private class DetailToggleSlot
            extends Slot {
        DetailToggleSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean isActive() {
            return WanderingCultivatorMenu.this.playerSlotsVisible;
        }
    }

    @SubscribeEvent
    public static void onRightClick(PlayerInteractEvent.EntityInteract event) {
        if (event.getTarget() instanceof WanderingCultivatorEntity npc) {
            Player player = event.getEntity();
            if (!player.level().isClientSide()) {
                net.minecraftforge.network.NetworkHooks.openScreen((net.minecraft.server.level.ServerPlayer) player,
                        new net.minecraft.world.SimpleMenuProvider((id, inv, menuPlayer) -> new com.friday.cultivation.inventory.WanderingCultivatorMenu(id, inv, npc.getId()), npc.getDisplayName()),
                        buf -> buf.writeVarInt(npc.getId()));
            }
        }
    }
}
