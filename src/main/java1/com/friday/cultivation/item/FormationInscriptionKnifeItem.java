package com.friday.cultivation.item;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.util.TooltipUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 阵法铭刻刀（严格照搬原模组 com.xiaoxiang.cultivation.item.FormationInscriptionKnifeItem）
 */
public class FormationInscriptionKnifeItem extends Item {

    public FormationInscriptionKnifeItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public boolean isDamageable(@NotNull ItemStack stack) {
        return stack.getMaxDamage() == 1;
    }

    @Override
    public int getEnchantmentValue() {
        return 12;
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        tooltip.add(TooltipUtils.tierElementLine(ItemTier.LOW, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.formation_inscription_knife")));
        tooltip.add(TooltipUtils.costLine(Component.translatable("tooltip.friday_cultivation.formation_inscription_knife.cost")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.formation_inscription_knife.hint")));
    }
}