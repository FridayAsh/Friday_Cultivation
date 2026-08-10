package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 回溯丹 — 完整复刻原模组 RecallPillItem。
 * 右键使用，恢复前世修为和背包。NBT 中存储前世数据。
 */
public class RecallPillItem extends Item {
    private static final String TAG_CULTIVATION = "PastCultivation";
    private static final String TAG_INVENTORY = "PastInventory";
    private static final String TAG_REALM_ID = "PastRealmId";
    private static final String TAG_IDENTITY_ID = "PastIdentityId";

    public RecallPillItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack createFrom(CompoundTag cultivationNbt, ListTag inventoryNbt, String realmId, String identityId) {
        ItemStack stack = new ItemStack((ItemLike) ModItems.RECALL_PILL.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_CULTIVATION, (Tag) cultivationNbt);
        tag.put(TAG_INVENTORY, (Tag) inventoryNbt);
        tag.putString(TAG_REALM_ID, realmId);
        tag.putString(TAG_IDENTITY_ID, identityId);
        return stack;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) {
            return InteractionResultHolder.success(stack);
        }
        if (!(player instanceof ServerPlayer)) {
            return InteractionResultHolder.consume(stack);
        }
        ServerPlayer sp = (ServerPlayer) player;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CULTIVATION)) {
            return InteractionResultHolder.consume(stack);
        }
        CultivationData ic = CultivationCapability.get(player).orElse(null);
        if (ic != null) {
            boolean blocked = ic.isSoulState() || ic.isReincarnationPending();
            if (blocked) {
                sp.displayClientMessage(Component.translatable("message.friday_cultivation.recall_pill.blocked").withStyle(ChatFormatting.RED), true);
                return InteractionResultHolder.fail(stack);
            }
            CompoundTag cultivation = tag.getCompound(TAG_CULTIVATION).copy();
            ListTag inventory = tag.getList(TAG_INVENTORY, 10).copy();
            stack.shrink(1);
            MinecraftServer server = sp.getServer();
            if (server != null) {
                server.execute(() -> restorePastLife(sp, cultivation, inventory));
            }
        }
        return InteractionResultHolder.consume(stack);
    }

    private static void restorePastLife(ServerPlayer sp, CompoundTag cultivation, ListTag inventory) {
        CultivationData ic = CultivationCapability.get(sp).orElse(null);
        if (ic != null) {
            ic.deserializeNBT(cultivation);
            ic.setSoulState(false);
            ic.setReincarnationPending(false);
            ic.setSoulTicks(0);
            TechniqueLoadoutHelper.NormalizationResult result = TechniqueLoadoutHelper.normalizeForCurrentState(ic, sp.getRandom());
            TechniqueLoadoutHelper.notifyNormalization(sp, ic, result);
        }
        sp.getInventory().clearContent();
        sp.getInventory().load(inventory);
        sp.setHealth(sp.getMaxHealth());
        CapabilityEvents.syncToClient(sp);
        SoulStateHandler.broadcastSouls(sp.getServer());
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 0.8f, 1.2f);
        sp.sendSystemMessage(Component.translatable("message.friday_cultivation.recall_pill.used").withStyle(ChatFormatting.AQUA));
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.usage"));
        tooltip.add(TooltipUtils.descriptionLine(Component.translatable("tooltip.friday_cultivation.recall_pill")));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_REALM_ID)) {
            Realm realm = Realm.byId(tag.getString(TAG_REALM_ID));
            Identity identity = Identity.byId(tag.getString(TAG_IDENTITY_ID));
            TooltipUtils.addBlank(tooltip);
            tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.recall_pill.past_life",
                    Component.translatable(identity.translationKey()).getString().formatted(ChatFormatting.GOLD),
                    realm.displayName().getString().formatted(ChatFormatting.GOLD))));
        }
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, Component.translatable("tooltip.friday_cultivation.section.warning"));
        tooltip.add(TooltipUtils.warningLine(Component.translatable("tooltip.friday_cultivation.recall_pill.warning")));
    }
}
