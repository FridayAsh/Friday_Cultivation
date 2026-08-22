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
import com.friday.cultivation.cultivation.RealmTransition;
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
            if (!ServerAuthorization.canSelectRealm(player)) {
                ServerAuthorization.reject(player, "realm_selection_denied");
                return;
            }
            Realm realm = Realm.byId(msg.realmId);
            CultivationData data = CultivationCapability.get((net.minecraft.world.entity.player.Player)player).orElse(null);
            if (data == null) {
                return;
            }
            SubStage targetSub;
            int looseLevel = 0;
            if (realm == Realm.LOOSE_IMMORTAL) {
                looseLevel = LooseImmortalBonusHelper.clampLevel(msg.subStageLevel > 0 ? msg.subStageLevel : 1);
                targetSub = SubStage.EARLY;
            } else {
                int count = realm.subStageCount();
                int level = realm.usesNumericLevels() ? Math.max(1, Math.min(msg.subStageLevel, count)) : Math.max(0, Math.min(msg.subStageLevel, count - 1));
                SubStage sub = realm.subStageAt(level);
                targetSub = sub != null ? sub : realm.firstSubStage();
            }
            RealmTransition.Result transition = RealmTransition.apply(data,
                    RealmTransition.Request.realmSelection(realm, targetSub, looseLevel, player.level().getGameTime()));
            com.friday.cultivation.event.TechniqueEffectHandler.refreshMaxHealth(player);
            CapabilityEvents.syncToClient(player);
            CultivationData.ZhenyuanBaselineResult zhenyuan = transition.zhenyuan();
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.realm_token.set_synced_zhenyuan", (Object[])new Object[]{realm.displayName(), zhenyuan.automaticPerAttribute(), zhenyuan.unallocatedZhenyuan()}), false);
        });
        ctx.setPacketHandled(true);
    }
}
