/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.chat.Component
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FormationInscriptionKnifeItem
extends Item {
    public FormationInscriptionKnifeItem(Item.Properties properties) {
        super(properties);
    }

    public boolean isEnchantable(@NotNull ItemStack stack) {
        return stack.getCount() == 1;
    }

    public int getEnchantmentValue() {
        return 12;
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add((Component)TooltipUtils.tierElementLine(ItemTier.LOW, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_inscription_knife")));
        tooltip.add((Component)TooltipUtils.costLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_inscription_knife.cost")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add((Component)TooltipUtils.hintLine((Component)Component.translatable((String)"tooltip.friday_cultivation.formation_inscription_knife.hint")));
    }
}

