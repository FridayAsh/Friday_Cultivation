package com.friday.cultivation.network;

import com.friday.cultivation.refining.RefiningRecipe;
import com.friday.cultivation.refining.RefiningRecipes;
import com.friday.cultivation.inventory.RefiningMenu;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class AutoFillRefiningRecipePacket {
    private final BlockPos pos;
    private final String recipeId;

    public AutoFillRefiningRecipePacket(BlockPos pos, String recipeId) {
        this.pos = pos;
        this.recipeId = recipeId;
    }

    public static void encode(AutoFillRefiningRecipePacket msg, FriendlyByteBuf buf) {
        buf.writeBlockPos(msg.pos);
        buf.writeUtf(msg.recipeId, 256);
    }

    public static AutoFillRefiningRecipePacket decode(FriendlyByteBuf buf) {
        return new AutoFillRefiningRecipePacket(buf.readBlockPos(), buf.readUtf(256));
    }

    public static void handle(AutoFillRefiningRecipePacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) return;
            AbstractContainerMenu container = player.containerMenu;
            if (!(container instanceof RefiningMenu)) return;
            RefiningMenu menu = (RefiningMenu) container;
            RefiningRecipe recipe = RefiningRecipes.byId(player.level(), msg.recipeId).orElse(null);
            if (recipe == null) return;
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
                        if (!ItemStack.isSameItemSameTags(target, pStack) || target.getCount() >= target.getMaxStackSize()) continue;
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
            menu.slotsChanged(io);
        });
        ctx.setPacketHandled(true);
    }
}
