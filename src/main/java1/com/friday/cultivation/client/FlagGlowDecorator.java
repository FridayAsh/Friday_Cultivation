package com.friday.cultivation.client;

import com.friday.cultivation.ItemTier;
import com.friday.cultivation.block.formation.FormationFlagBlock;
import com.friday.cultivation.util.TooltipUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.IItemDecorator;

/**
 * 阵旗光晕装饰器（完全照搬原模组 com.xiaoxiang.cultivation.client.FlagGlowDecorator）。
 * 阵旗物品（FormationFlagBlock）在物品栏图标上显示品阶色光晕。
 */
public class FlagGlowDecorator implements IItemDecorator {
    private static final int CYCLE_TICKS = 60;
    private static final int[] LAYER_SIZES = new int[]{4, 8, 12, 16};
    private static final int[] LAYER_BASE_ALPHA = new int[]{175, 115, 55, 20};

    @Override
    public boolean render(GuiGraphics gui, Font font, ItemStack stack, int x, int y) {
        Item item = stack.getItem();
        if (!(item instanceof BlockItem)) {
            return false;
        }
        BlockItem blockItem = (BlockItem) item;
        Block block = blockItem.getBlock();
        if (!(block instanceof FormationFlagBlock)) {
            return false;
        }
        FormationFlagBlock flagBlock = (FormationFlagBlock) block;
        ItemTier tier = flagBlock.flagTier();
        int rgb = FlagGlowDecorator.tierColorRgb(tier);
        Minecraft mc = Minecraft.getInstance();
        long time = mc.level != null ? mc.level.getGameTime() : 0L;
        double phase = (double) (time % (long) CYCLE_TICKS) / (double) CYCLE_TICKS;
        float pulse = (float) (0.65 + 0.35 * Math.sin(phase * 2.0 * Math.PI));
        for (int i = 0; i < LAYER_SIZES.length; ++i) {
            int alpha = (int) ((float) LAYER_BASE_ALPHA[i] * pulse);
            if (alpha < 3) continue;
            int color = alpha << 24 | rgb;
            int sz = LAYER_SIZES[i];
            int inset = (16 - sz) / 2;
            gui.fill(x + inset, y + inset, x + inset + sz, y + inset + sz, color);
        }
        return true;
    }

    private static int tierColorRgb(ItemTier tier) {
        if (tier == ItemTier.IMMORTAL) {
            return 16726559;
        }
        Integer rgbObj = TooltipUtils.tierFormatting(tier).getColor();
        return rgbObj == null ? 0xFFFFFF : rgbObj & 0xFFFFFF;
    }
}
