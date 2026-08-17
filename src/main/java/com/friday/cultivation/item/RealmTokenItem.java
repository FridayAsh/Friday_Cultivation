/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionHand
 *  net.minecraft.world.InteractionResultHolder
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.Item$Properties
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.item.Rarity
 *  net.minecraft.world.item.TooltipFlag
 *  net.minecraft.world.level.Level
 *  org.jetbrains.annotations.NotNull
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.item;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.ItemTier;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.util.TooltipUtils;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RealmTokenItem
extends Item {
    private final Realm realm;
    private final int looseImmortalTribulationLevel;

    public RealmTokenItem(Item.Properties props, Realm realm) {
        this(props, realm, 0);
    }

    public RealmTokenItem(Item.Properties props, Realm realm, int looseImmortalTribulationLevel) {
        super(props.stacksTo(1).rarity(Rarity.EPIC));
        this.realm = realm;
        this.looseImmortalTribulationLevel = realm == Realm.LOOSE_IMMORTAL ? LooseImmortalBonusHelper.clampLevel(looseImmortalTribulationLevel) : 0;
    }

    public Realm realm() {
        return this.realm;
    }

    @NotNull
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer) {
            ServerPlayer sp = (ServerPlayer)player;
            CultivationCapability.get((Player)sp).ifPresent(data -> {
                data.setRealm(this.realm);
                data.setSubStage(this.realm.firstSubStage());
                int looseLevel = this.targetLooseImmortalLevel();
                if (looseLevel > 0) {
                    data.setSoulState(false);
                    data.setGhostCultivator(false);
                    data.setReincarnationPending(false);
                    data.setReincarnationReady(false);
                    data.setLooseImmortalTribulations(looseLevel);
                    data.setNextLooseImmortalTribulationTick(looseLevel >= 9 ? -1L : level.getGameTime() + 12000000L);
                }
                CultivationData.ZhenyuanBaselineResult zhenyuan = data.syncZhenyuanToRealmBaseline(this.realm, this.realm.firstSubStage());
                data.setCurrentQi(data.getMaxQi() / 2L);
                data.setCultivationProgress(0L);
                com.friday.cultivation.event.TechniqueEffectHandler.refreshMaxHealth(sp);
                CapabilityEvents.syncToClient(sp);
                sp.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_token.set_synced_zhenyuan", (Object[])new Object[]{this.targetDisplayName(), zhenyuan.automaticPerAttribute(), zhenyuan.unallocatedZhenyuan()}), false);
            });
        }
        player.getCooldowns().addCooldown((Item)this, 10);
        return InteractionResultHolder.sidedSuccess(stack, (boolean)level.isClientSide());
    }

    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        tooltip.add((Component)TooltipUtils.tierElementLine(ItemTier.SUPREME, QiElement.PURE));
        TooltipUtils.addBlank(tooltip);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add((Component)TooltipUtils.statsLine((Component)Component.translatable((String)"tooltip.friday_cultivation.realm_token", (Object[])new Object[]{this.targetDisplayName().copy().withStyle(ChatFormatting.GOLD)})));
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.warning");
        tooltip.add((Component)TooltipUtils.warningLine((Component)Component.translatable((String)"tooltip.friday_cultivation.realm_token.dev_only")));
    }

    private int targetLooseImmortalLevel() {
        if (this.realm != Realm.LOOSE_IMMORTAL) {
            return 0;
        }
        return this.looseImmortalTribulationLevel > 0 ? this.looseImmortalTribulationLevel : 1;
    }

    private Component targetDisplayName() {
        int looseLevel = this.targetLooseImmortalLevel();
        if (looseLevel > 0) {
            return RealmTokenItem.looseImmortalLevelName(looseLevel);
        }
        return this.realm.displayName();
    }

    private static Component looseImmortalLevelName(int level) {
        return switch (LooseImmortalBonusHelper.clampLevel(level)) {
            case 1 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.1");
            case 2 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.2");
            case 3 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.3");
            case 4 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.4");
            case 5 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.5");
            case 6 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.6");
            case 7 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.7");
            case 8 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.8");
            case 9 -> Component.translatable((String)"realm.friday_cultivation.loose_immortal.level.9");
            default -> Component.translatable((String)"realm.friday_cultivation.loose_immortal");
        };
    }
}

