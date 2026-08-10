package com.friday.cultivation.item;

import com.friday.cultivation.util.TooltipUtils;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
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
import org.slf4j.Logger;

import java.lang.reflect.Method;
import java.util.List;

/**
 * 修仙典籍（严格照搬原模组 com.xiaoxiang.cultivation.item.CultivationCompendiumItem）。
 * <p>运行时通过反射调用 Patchouli 的 {@code PatchouliAPI.get().openBookGUI()}，
 * 若 Patchouli 未加载则提示玩家；右键成功打开后冷却 10 tick。</p>
 */
public class CultivationCompendiumItem extends Item {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final ResourceLocation BOOK_ID =
            new ResourceLocation("friday_cultivation", "cultivation_compendium");
    private static final String PATCHOULI_API = "vazkii.patchouli.api.PatchouliAPI";

    public CultivationCompendiumItem(Item.Properties properties) {
        super(properties);
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide()) {
            return InteractionResultHolder.pass(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.fail(stack);
        }
        ServerPlayer serverPlayer = (ServerPlayer) player;
        if (!ModList.get().isLoaded("patchouli")) {
            serverPlayer.displayClientMessage(
                    Component.translatable("message.friday_cultivation.cultivation_compendium.patchouli_missing")
                            .withStyle(ChatFormatting.YELLOW),
                    true);
            return InteractionResultHolder.consume(stack);
        }
        if (openPatchouliBook(serverPlayer)) {
            BlockPos pos = serverPlayer.blockPosition();
            level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 0.75f, 1.15f);
            serverPlayer.getCooldowns().addCooldown(this, 10);
            return InteractionResultHolder.success(stack);
        }
        serverPlayer.displayClientMessage(
                Component.translatable("message.friday_cultivation.cultivation_compendium.open_failed")
                        .withStyle(ChatFormatting.RED),
                true);
        return InteractionResultHolder.consume(stack);
    }

    private static boolean openPatchouliBook(ServerPlayer player) {
        try {
            Class<?> apiClass = Class.forName(PATCHOULI_API);
            Class<?> apiInterface = Class.forName("vazkii.patchouli.api.PatchouliAPI$IPatchouliAPI");
            Object api = apiClass.getMethod("get").invoke(null);
            Method openBook = apiInterface.getMethod("openBookGUI", ServerPlayer.class, ResourceLocation.class);
            openBook.invoke(api, player, BOOK_ID);
            return true;
        } catch (LinkageError | ReflectiveOperationException e) {
            LOGGER.warn("Failed to open Patchouli book {}", BOOK_ID, e);
            return false;
        }
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level,
                                @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add(TooltipUtils.descriptionLine(
                Component.translatable("tooltip.friday_cultivation.cultivation_compendium")));
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(
                Component.translatable("tooltip.friday_cultivation.cultivation_compendium.hint")));
    }
}
