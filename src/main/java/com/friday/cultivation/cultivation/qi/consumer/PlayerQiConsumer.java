/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.network.PacketDistributor
 *  org.jetbrains.annotations.Nullable
 */
package com.friday.cultivation.cultivation.qi.consumer;

import com.friday.cultivation.cultivation.CultivationBonusCategory;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDaoBonusHelper;
import com.friday.cultivation.cultivation.GoldenCoreDaoBonusHelper;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.PhysiqueBonusHelper;
import com.friday.cultivation.cultivation.QiElement;
import com.friday.cultivation.cultivation.SpiritRootBonusHelper;
import com.friday.cultivation.cultivation.ZhenyuanBonusHelper;
import com.friday.cultivation.cultivation.qi.IQiConsumer;
import com.friday.cultivation.cultivation.qi.PlayerQiAbsorptionHelper;
import com.friday.cultivation.cultivation.technique.TechniqueBonusHelper;
import com.friday.cultivation.event.NascentSoulOutOfBodyHandler;
import com.friday.cultivation.event.RealmPressureHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.QiAbsorbedPacket;
import com.friday.cultivation.network.SyncCultivationDataPacket;
import com.friday.cultivation.registry.ModEffects;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

public final class PlayerQiConsumer
implements IQiConsumer {
    public static final double BASE_ATTRACTION_RADIUS = 14.0;
    public static final double MEDITATION_RANGE_BONUS = 10.0;
    public static final double MEDITATION_EFFICIENCY_BONUS = 10.0;
    private final ServerPlayer player;
    private final CultivationData data;

    private PlayerQiConsumer(ServerPlayer player, CultivationData data) {
        this.player = player;
        this.data = data;
    }

    @Nullable
    public static PlayerQiConsumer wrap(ServerPlayer player) {
        if (player == null) {
            return null;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return null;
        }
        if (player.hasEffect((MobEffect)ModEffects.MERIDIAN_FROZEN.get())) {
            return null;
        }
        if (!PlayerQiAbsorptionHelper.canAutoAbsorb(player, data)) {
            return null;
        }
        return new PlayerQiConsumer(player, data);
    }

    public ServerPlayer player() {
        return this.player;
    }

    @Override
    public Vec3 position() {
        return NascentSoulOutOfBodyHandler.qiAbsorptionPosition(this.player);
    }

    @Override
    public double attractRadius() {
        double r = 14.0 + (double)TechniqueBonusHelper.qiAbsorbRangeBonus((Player)this.player);
        if (this.data.isMeditating()) {
            r += 10.0;
        }
        return r;
    }

    @Override
    public boolean wantsMore() {
        return this.data.getCurrentQi() < this.data.getMaxQi() || this.data.getCultivationProgress() < this.data.getMaxCultivation();
    }

    @Override
    public int receiveQi(QiElement element, int baseAmount) {
        int amount = PlayerQiConsumer.finalAbsorbAmount((Player)this.player, this.data, element, baseAmount);
        if (amount <= 0) {
            return 0;
        }
        long beforeCultivation = this.data.getCultivationProgress();
        int qiGained = this.data.absorbQi(amount, element);
        long cultivationGained = this.data.getCultivationProgress() - beforeCultivation;
        if (qiGained > 0 || cultivationGained > 0L) {
            Vec3 visualPos = this.position();
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> this.player), (Object)new QiAbsorbedPacket(visualPos.x, visualPos.y, visualPos.z, element.ordinal()));
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> this.player), (Object)new SyncCultivationDataPacket(this.data));
        }
        return cultivationGained > 0L ? amount : qiGained;
    }

    public static int cultivationEfficiencyPerParticle(Player player, CultivationData data, QiElement element) {
        return PlayerQiConsumer.finalAbsorbAmount(player, data, element, 1);
    }

    public static int nominalQiRecoveryPerSecond(Player player, CultivationData data, QiElement element) {
        if (data != null && !data.isBonusCategoryEnabled(CultivationBonusCategory.QI_RECOVERY)) {
            return 0;
        }
        long recovery = ZhenyuanBonusHelper.qiSeaRecoveryPerSecond(data) + (long)FoundationDaoBonusHelper.qiRecoveryPerSecondBonus(player) + (long)GoldenCoreDaoBonusHelper.qiRecoveryPerSecondBonus(player) + (long)LooseImmortalBonusHelper.qiRecoveryPerSecondBonus(player);
        recovery = RealmPressureHandler.applyQiRecoveryPenalty((LivingEntity)player, recovery);
        return (int)Math.min(Integer.MAX_VALUE, Math.max(0L, recovery));
    }

    public static double finalAbsorbMultiplier(Player player, CultivationData data, QiElement element) {
        double realmBase = PlayerQiAbsorptionHelper.baseAbsorbMultiplier(data);
        if (realmBase <= 0.0) {
            return 0.0;
        }
        if (data != null && !data.isBonusCategoryEnabled(CultivationBonusCategory.CULTIVATION_EFFICIENCY)) {
            return realmBase;
        }
        double mult = realmBase * SpiritRootBonusHelper.qiAbsorptionMultiplier(player) * TechniqueBonusHelper.qiAbsorbMultiplier(player);
        mult *= PhysiqueBonusHelper.qiAbsorbElementMultiplier(player, element);
        mult += (double)(FoundationDaoBonusHelper.cultivationEfficiencyBonus(player) + GoldenCoreDaoBonusHelper.cultivationEfficiencyBonus(player) + LooseImmortalBonusHelper.cultivationEfficiencyBonus(player));
        if (data != null && data.isMeditating()) {
            mult += 10.0;
        }
        return mult;
    }

    private static int finalAbsorbAmount(Player player, CultivationData data, QiElement element, int baseAmount) {
        if (baseAmount <= 0) {
            return 0;
        }
        double mult = PlayerQiConsumer.finalAbsorbMultiplier(player, data, element);
        if (mult <= 0.0) {
            return 0;
        }
        return Math.max(1, (int)Math.ceil(mult * (double)baseAmount));
    }
}

