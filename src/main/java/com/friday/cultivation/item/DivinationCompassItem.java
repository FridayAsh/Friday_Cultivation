/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.core.Holder
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  net.minecraft.world.level.biome.Biome
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.BiomeQiProfile;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class DivinationCompassItem
extends Item {
    public DivinationCompassItem(Item.Properties props) {
        super(props);
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            BiomeQiProfile profile = BiomeQiProfile.of((Holder<Biome>)level.getBiome(player.blockPosition()));
            String densityKey = DivinationCompassItem.densityLabelKey(profile.density());
            String elementKey = "spirit_root.friday_cultivation." + profile.element().id();
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.divination_compass.reading", (Object[])new Object[]{Component.translatable((String)densityKey), Component.translatable((String)elementKey)}).withStyle(ChatFormatting.AQUA), false);
        }
        player.getCooldowns().addCooldown((Item)this, 20);
        return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide());
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add((Component)TooltipUtils.tierElementLine(ItemTier.LOW, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.divination_compass")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.divination_compass.hint")));
    }

    private static String densityLabelKey(double density) {
        if (density < 0.15) {
            return "message.friday_cultivation.density.barren";
        }
        if (density < 0.3) {
            return "message.friday_cultivation.density.thin";
        }
        if (density < 0.45) {
            return "message.friday_cultivation.density.normal";
        }
        if (density < 0.6) {
            return "message.friday_cultivation.density.rich";
        }
        return "message.friday_cultivation.density.abundant";
    }
}

