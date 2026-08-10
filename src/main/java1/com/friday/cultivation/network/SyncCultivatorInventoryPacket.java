package com.friday.cultivation.network;

import com.friday.cultivation.inventory.WanderingCultivatorMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 同步散修背包包 - 严格 1:1 复刻原模组
 * 混淆名映射: m_91087_=getInstance, f_91074_=player, f_36096_=containerMenu,
 *             f_38840_=containerId, m_130130_=writeVarInt, m_130242_=readVarInt,
 *             m_130079_=writeNbt, m_130260_=readNbt, m_128365_=put,
 *             m_128437_=getList, m_6643_=getContainerSize, m_8020_=getItem,
 *             m_41619_=isEmpty, m_41739_=save
 */
public class SyncCultivatorInventoryPacket {
    private final int containerId;
    private final ListTag inventoryTag;

    public SyncCultivatorInventoryPacket(int containerId, ListTag inventoryTag) {
        this.containerId = containerId;
        this.inventoryTag = inventoryTag;
    }

    public static ListTag toTag(SimpleContainer inv) {
        ListTag tag = new ListTag();
        for (int i = 0; i < inv.getContainerSize(); ++i) {
            ItemStack stack = inv.getItem(i);
            CompoundTag item = new CompoundTag();
            if (!stack.isEmpty()) {
                stack.save(item);
            }
            tag.add((Tag)item);
        }
        return tag;
    }

    public static void encode(SyncCultivatorInventoryPacket msg, FriendlyByteBuf buf) {
        buf.writeVarInt(msg.containerId);
        CompoundTag wrapper = new CompoundTag();
        wrapper.put("Inv", (Tag)msg.inventoryTag);
        buf.writeNbt(wrapper);
    }

    public static SyncCultivatorInventoryPacket decode(FriendlyByteBuf buf) {
        int id = buf.readVarInt();
        CompoundTag wrapper = buf.readNbt();
        ListTag inv = wrapper != null ? wrapper.getList("Inv", 10) : new ListTag();
        return new SyncCultivatorInventoryPacket(id, inv);
    }

    public static void handle(SyncCultivatorInventoryPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            AbstractContainerMenu patt2629$temp;
            LocalPlayer player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            if (player.containerMenu.containerId == msg.containerId && (patt2629$temp = player.containerMenu) instanceof WanderingCultivatorMenu) {
                WanderingCultivatorMenu menu = (WanderingCultivatorMenu)patt2629$temp;
                menu.setNpcInventoryTag(msg.inventoryTag);
            }
        });
        ctx.setPacketHandled(true);
    }
}
