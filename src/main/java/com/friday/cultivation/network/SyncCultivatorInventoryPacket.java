/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.client.ClientCultivatorSyncHooks;
import java.util.function.Supplier;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

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
            tag.add(item);
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
        ctx.enqueueWork(() -> DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> ClientCultivatorSyncHooks.applyInventory(msg.containerId, msg.inventoryTag)));
        ctx.setPacketHandled(true);
    }
}
