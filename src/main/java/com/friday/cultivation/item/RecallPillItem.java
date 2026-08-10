/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.nbt.CompoundTag
 *  net.minecraft.nbt.ListTag
 *  net.minecraft.nbt.Tag
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.MinecraftServer
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
 *  net.minecraft.world.level.ItemLike
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.technique.TechniqueLoadoutHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.registry.ModItems;
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

public class RecallPillItem
extends Item {
    private static final String TAG_CULTIVATION = "PastCultivation";
    private static final String TAG_INVENTORY = "PastInventory";
    private static final String TAG_REALM_ID = "PastRealmId";
    private static final String TAG_IDENTITY_ID = "PastIdentityId";

    public RecallPillItem(Item.Properties properties) {
        super(properties);
    }

    public static ItemStack createFrom(CompoundTag cultivationNbt, ListTag inventoryNbt, String realmId, String identityId) {
        ItemStack stack = new ItemStack((ItemLike)ModItems.RECALL_PILL.get());
        CompoundTag tag = stack.getOrCreateTag();
        tag.put(TAG_CULTIVATION, (Tag)cultivationNbt);
        tag.put(TAG_INVENTORY, (Tag)inventoryNbt);
        tag.putString(TAG_REALM_ID, realmId);
        tag.putString(TAG_IDENTITY_ID, identityId);
        return stack;
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
        ServerPlayer sp = (ServerPlayer)player;
        CompoundTag tag = stack.getTag();
        if (tag == null || !tag.contains(TAG_CULTIVATION)) {
            return InteractionResultHolder.pass(stack);
        }
        boolean blocked = CultivationCapability.get((Player)sp).map(d -> d.isSoulState() || d.isReincarnationPending()).orElse(false);
        if (blocked) {
            sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.recall_pill.blocked").withStyle(ChatFormatting.RED), true);
            return InteractionResultHolder.fail(stack);
        }
        CompoundTag cultivation = tag.getCompound(TAG_CULTIVATION).copy();
        ListTag inventory = tag.getList(TAG_INVENTORY, 10).copy();
        stack.shrink(1);
        MinecraftServer server = sp.getServer();
        if (server != null) {
            server.execute(() -> RecallPillItem.restorePastLife(sp, cultivation, inventory));
        }
        return InteractionResultHolder.consume(stack);
    }

    private static void restorePastLife(ServerPlayer sp, CompoundTag cultivation, ListTag inventory) {
        CultivationCapability.get((Player)sp).ifPresent(data -> {
            data.deserializeNBT(cultivation);
            data.setSoulState(false);
            data.setReincarnationPending(false);
            data.setSoulTicks(0);
            TechniqueLoadoutHelper.NormalizationResult result = TechniqueLoadoutHelper.normalizeForCurrentState(data, sp.getRandom());
            TechniqueLoadoutHelper.notifyNormalization(sp, data, result);
        });
        sp.getInventory().clearContent();
        sp.getInventory().setChanged();
        sp.setHealth(sp.getMaxHealth());
        CapabilityEvents.syncToClient(sp);
        SoulStateHandler.broadcastSouls(sp.getServer());
        sp.level().playSound(null, sp.blockPosition(), SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.8f, 1.2f);
        sp.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.recall_pill.used").withStyle(ChatFormatting.AQUA));
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.usage");
        tooltip.add((Component)TooltipUtils.descriptionLine((Component)Component.translatable((String)"tooltip.friday_cultivation.recall_pill")));
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(TAG_REALM_ID)) {
            Realm realm = Realm.byId(tag.getString(TAG_REALM_ID));
            Identity identity = Identity.byId(tag.getString(TAG_IDENTITY_ID));
            TooltipUtils.addBlank(tooltip);
            tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.recall_pill.past_life", (Object[])new Object[]{Component.translatable((String)identity.translationKey()).copy().withStyle(ChatFormatting.GOLD), realm.displayName().copy().withStyle(ChatFormatting.GOLD)})));
        }
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
        tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.recall_pill.warning")));
    }
}

