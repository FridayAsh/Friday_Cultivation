/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.LooseImmortalBonusHelper;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.event.CapabilityEvents;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

public class RealmSelectionPacket {
    private final String realmId;
    private final int subStageLevel;

    public RealmSelectionPacket(String realmId, int subStageLevel) {
        this.realmId = realmId;
        this.subStageLevel = subStageLevel;
    }

    public static void encode(RealmSelectionPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.realmId);
        buf.writeInt(msg.subStageLevel);
    }

    public static RealmSelectionPacket decode(FriendlyByteBuf buf) {
        return new RealmSelectionPacket(buf.readUtf(), buf.readInt());
    }

    public static void handle(RealmSelectionPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            Realm realm = Realm.byId(msg.realmId);
            CultivationData data = CultivationCapability.get((net.minecraft.world.entity.player.Player)player).orElse(null);
            if (data == null) {
                return;
            }
            data.setRealm(realm);
            if (realm == Realm.LOOSE_IMMORTAL) {
                int level = LooseImmortalBonusHelper.clampLevel(msg.subStageLevel > 0 ? msg.subStageLevel : 1);
                data.setSubStage(SubStage.EARLY);
                data.setSoulState(false);
                data.setGhostCultivator(false);
                data.setReincarnationPending(false);
                data.setReincarnationReady(false);
                data.setLooseImmortalTribulations(level);
                data.setNextLooseImmortalTribulationTick(level >= 9 ? -1L : player.level().getGameTime() + 12000000L);
            } else {
                int count = realm.subStageCount();
                int level = realm.usesNumericLevels() ? Math.max(1, Math.min(msg.subStageLevel, count)) : Math.max(0, Math.min(msg.subStageLevel, count - 1));
                SubStage sub = realm.subStageAt(level);
                data.setSubStage(sub != null ? sub : realm.firstSubStage());
            }
            CultivationData.ZhenyuanBaselineResult zhenyuan = data.syncZhenyuanToRealmBaseline(realm, data.getSubStage());
            data.setCurrentQi(data.getMaxQi() / 2L);
            data.setCultivationProgress(0L);
            // 切换境界后重置悟道进度（新境界/子阶段应有全新悟道上限与进度，避免沿用旧值）
            data.setWuDaoProgress(0L);
            // 切换境界后强制重算 MAX_HEALTH 加成 + clamp 当前生命值
            // （锻体加成保留 stored 最高层：凡人=0、锻体=当前层、高境界=stored，
            //   由 bodyTemperingHpBonus 内部判定，此处不清理记录）
            com.friday.cultivation.event.TechniqueEffectHandler.refreshMaxHealth(player);
            CapabilityEvents.syncToClient(player);
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_token.set_synced_zhenyuan", (Object[])new Object[]{realm.displayName(), zhenyuan.automaticPerAttribute(), zhenyuan.unallocatedZhenyuan()}), false);
        });
        ctx.setPacketHandled(true);
    }
}
