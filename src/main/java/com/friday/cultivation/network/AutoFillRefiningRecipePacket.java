/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.SimpleContainer
 *  net.minecraft.world.entity.player.Inventory
 *  net.minecraft.world.inventory.AbstractContainerMenu
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.refining.RefiningRecipe;
import com.friday.cultivation.cultivation.refining.RefiningRecipes;
import com.friday.cultivation.inventory.RefiningMenu;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class AutoFillRefiningRecipePacket {
    private final String recipeId;

    public AutoFillRefiningRecipePacket(String recipeId) {
        this.recipeId = recipeId;
    }

    public static void encode(AutoFillRefiningRecipePacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.recipeId, 256);
    }

    public static AutoFillRefiningRecipePacket decode(FriendlyByteBuf buf) {
        return new AutoFillRefiningRecipePacket(buf.readUtf(256));
    }

    public static void handle(AutoFillRefiningRecipePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            AbstractContainerMenu patt1344$temp = player.containerMenu;
            if (!(patt1344$temp instanceof RefiningMenu)) {
                return;
            }
            RefiningMenu menu = (RefiningMenu)patt1344$temp;
            RefiningRecipe recipe = RefiningRecipes.byId(player.level(), msg.recipeId).orElse(null);
            if (recipe == null) {
                return;
            }
            SimpleContainer io = menu.getIoContainer();
            Inventory pInv = player.getInventory();
            for (RefiningRecipe.IngredientEntry ing : recipe.ingredientList()) {
                for (int i = 0; i < pInv.getContainerSize(); ++i) {
                    ItemStack pStack = pInv.getItem(i);
                    if (pStack.isEmpty() || !ing.ingredient().test(pStack)) continue;
                    int remaining = pStack.getCount();
                    for (int j = 0; j < 6 && remaining > 0; ++j) {
                        ItemStack target = io.getItem(j);
                        if (target.isEmpty()) {
                            int take = Math.min(remaining, pStack.getMaxStackSize());
                            ItemStack moved = pStack.copy();
                            moved.setCount(take);
                            io.setItem(j, moved);
                            remaining -= take;
                            continue;
                        }
                        if (!ItemStack.isSameItemSameTags((ItemStack)target, (ItemStack)pStack) || target.getCount() >= target.getMaxStackSize()) continue;
                        int space = target.getMaxStackSize() - target.getCount();
                        int take = Math.min(remaining, space);
                        target.grow(take);
                        io.setChanged();
                        remaining -= take;
                    }
                    int taken = pStack.getCount() - remaining;
                    pStack.shrink(taken);
                }
            }
            menu.broadcastChanges();
        });
        ctx.setPacketHandled(true);
    }
}

