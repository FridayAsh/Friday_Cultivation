/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.sounds.SoundEvents
 *  net.minecraft.sounds.SoundSource
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraftforge.fml.ModList
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.FridayCultivationMod;
import com.friday.cultivation.util.TooltipUtils;
import java.lang.reflect.Method;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.ModList;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CultivationCompendiumItem
extends Item {
    private static final ResourceLocation BOOK_ID = new ResourceLocation((String)"friday_cultivation", (String)"cultivation_compendium");
    private static final String PATCHOULI_API = "vazkii.patchouli.api.PatchouliAPI";

    public CultivationCompendiumItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.pass(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer)player;
        if (!ModList.get().isLoaded("patchouli")) {
            serverPlayer.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivation_compendium.patchouli_missing").withStyle(ChatFormatting.YELLOW), true);
            return InteractionResultHolder.fail(stack);
        }
        if (CultivationCompendiumItem.openPatchouliBook(serverPlayer)) {
            level.playSound(null, serverPlayer.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.75f, 1.15f);
            serverPlayer.getCooldowns().addCooldown((Item)this, 10);
            return InteractionResultHolder.consume(stack);
        }
        serverPlayer.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.cultivation_compendium.open_failed").withStyle(ChatFormatting.RED), true);
        return InteractionResultHolder.fail(stack);
    }

    private static boolean openPatchouliBook(ServerPlayer player) {
        try {
            Class<?> apiClass = Class.forName(PATCHOULI_API);
            Class<?> apiInterface = Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            Object api = apiClass.getMethod("get", new Class[0]).invoke(null, new Object[0]);
            Method openBook = apiInterface.getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class);
            openBook.invoke(api, player, BOOK_ID);
            return true;
        }
        catch (LinkageError | ReflectiveOperationException e) {
            FridayCultivationMod.LOGGER.warn("Failed to open Patchouli book {}", (Object)BOOK_ID, (Object)e);
            return false;
        }
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.cultivation_compendium")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.cultivation_compendium.hint")));
    }
}

