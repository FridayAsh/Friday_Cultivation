package com.friday.cultivation.item;

import com.friday.cultivation.BiomeQiProfile;
import com.friday.cultivation.ItemTier;
import com.friday.cultivation.spirit.QiElement;
import com.friday.cultivation.util.TooltipUtils;
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

import java.util.List;

/**
 * 占卜罗盘（严格照搬原模组 com.xiaoxiang.cultivation.item.DivinationCompassItem）
 */
public class DivinationCompassItem extends Item {

    public DivinationCompassItem(Item.Properties props) {
        super(props);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide()) {
            BiomeQiProfile profile = BiomeQiProfile.of((Holder<Biome>)level.getBiome(player.blockPosition()));
            String densityKey = densityLabelKey(profile.density());
            String elementKey = "spirit_root.friday_cultivation." + profile.element().id();
            player.sendSystemMessage(Component.translatable("message.friday_cultivation.divination_compass.reading",
                    Component.translatable(densityKey),
                    Component.translatable(elementKey)).withStyle(ChatFormatting.AQUA));
        }
        player.getCooldowns().addCooldown((Item)this, 20);
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add(TooltipUtils.tierElementLine(ItemTier.LOW, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.divination_compass")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.divination_compass.hint")));
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