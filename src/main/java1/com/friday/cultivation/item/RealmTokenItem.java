package com.friday.cultivation.item;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import com.friday.cultivation.util.TooltipUtils;
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

import java.util.List;

/**
 * 境界令牌 - 强制将玩家境界设置为指定 Realm + SubStage.EARLY。
 * 严格 1:1 复刻原 mod RealmTokenItem。
 */
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
        this.looseImmortalTribulationLevel = realm == Realm.LOOSE_IMMORTAL
                ? LooseImmortalBonusHelper.clampLevel(looseImmortalTribulationLevel)
                : 0;
    }

    public Realm realm() {
        return this.realm;
    }

    public int targetLooseImmortalLevel() {
        return this.looseImmortalTribulationLevel;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(@NotNull Level level, @NotNull Player player, @NotNull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide() && player instanceof ServerPlayer sp) {
            CultivationData iData = CultivationCapability.get(sp).orElse(null);
            if (iData != null) {
                iData.setRealm(this.realm);
                iData.setSubStage(SubStage.EARLY);
                int looseLevel = this.targetLooseImmortalLevel();
                if (looseLevel > 0) {
                    iData.setSoulState(false);
                    iData.setGhostCultivator(false);
                    iData.setReincarnationPending(false);
                    iData.setReincarnationReady(false);
                    iData.setLooseImmortalTribulations(looseLevel);
                    iData.setNextLooseImmortalTribulationTick(looseLevel >= 9 ? -1L : level.getGameTime() + 12000000L);
                }
                CultivationData.ZhenyuanBaselineResult zhenyuan = iData.syncZhenyuanToRealmBaseline(this.realm, SubStage.EARLY);
                iData.setCurrentQi(iData.getMaxQi() / 2L);
                iData.setCultivationProgress(0L);
                CapabilityEvents.syncToClient(sp);
                if (zhenyuan != null) {
                    sp.sendSystemMessage(Component.translatable("message.friday_cultivation.realm_token.set_synced_zhenyuan",
                            this.targetDisplayName(),
                            zhenyuan.automaticPerAttribute(),
                            zhenyuan.unallocatedZhenyuan()));
                }
            }
        }
        player.getCooldowns().addCooldown(this, 10);
        return InteractionResultHolder.consume(stack);
    }

    private Component targetDisplayName() {
        return this.realm.displayName();
    }

    @Override
    public void appendHoverText(@NotNull ItemStack stack, @Nullable Level level, @NotNull List<Component> tooltip, @NotNull TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.effect");
        tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.realm_token.set_realm", this.realm.displayName())));
        if (this.looseImmortalTribulationLevel > 0) {
            TooltipUtils.addSection(tooltip, "tooltip.friday_cultivation.section.loose_immortal");
            tooltip.add(TooltipUtils.statsLine(Component.translatable("tooltip.friday_cultivation.realm_token.loose_immortal", this.looseImmortalTribulationLevel)));
        }
        TooltipUtils.addBlank(tooltip);
        tooltip.add(TooltipUtils.hintLine(Component.translatable("tooltip.friday_cultivation.realm_token.hint")));
    }
}
