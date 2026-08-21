/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.RealmTransition;
import com.friday.cultivation.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.RealmTopology;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.cultivation.refining.RefiningRank;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.TechniqueEffectHandler;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

public class EditPlayerStatsPacket {
    private static final int ATTR_CAP = 9999;
    private static final int BONE_AGE_CAP = 1000000;
    private final int constitution;
    private final int physique;
    private final int agility;
    private final int spellPower;
    private final int qiSea;
    private final int defense;
    private final String realmId;
    private final int subOrd;
    private final int refining;
    private final int alchemy;
    private final int boneAgeYears;

    public EditPlayerStatsPacket(int constitution, int physique, int agility, int spellPower, int qiSea, int defense, String realmId, int subOrd, int refining, int alchemy, int boneAgeYears) {
        this.constitution = constitution;
        this.physique = physique;
        this.agility = agility;
        this.spellPower = spellPower;
        this.qiSea = qiSea;
        this.defense = defense;
        this.realmId = realmId == null ? Realm.MORTAL.id() : realmId;
        this.subOrd = subOrd;
        this.refining = refining;
        this.alchemy = alchemy;
        this.boneAgeYears = boneAgeYears;
    }

    public static void encode(EditPlayerStatsPacket m, FriendlyByteBuf b) {
        b.writeVarInt(m.constitution);
        b.writeVarInt(m.physique);
        b.writeVarInt(m.agility);
        b.writeVarInt(m.spellPower);
        b.writeVarInt(m.qiSea);
        b.writeVarInt(m.defense);
        b.writeUtf(m.realmId);
        b.writeVarInt(m.subOrd);
        b.writeVarInt(m.refining);
        b.writeVarInt(m.alchemy);
        b.writeVarInt(m.boneAgeYears);
    }

    public static EditPlayerStatsPacket decode(FriendlyByteBuf b) {
        return new EditPlayerStatsPacket(b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readUtf(64), b.readVarInt(), b.readVarInt(), b.readVarInt(), b.readVarInt());
    }

    private static int clamp(int v, int lo, int hi) {
        return Math.max(lo, Math.min(hi, v));
    }

    public static void handle(EditPlayerStatsPacket m, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            if (!ServerAuthorization.canEditPlayerStats(player)) {
                ServerAuthorization.reject(player, "stat_edit_denied");
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                data.setAttrConstitution(EditPlayerStatsPacket.clamp(m.constitution, 0, 9999));
                data.setAttrPhysique(EditPlayerStatsPacket.clamp(m.physique, 0, 9999));
                data.setAttrAgility(EditPlayerStatsPacket.clamp(m.agility, 0, 9999));
                data.setAttrSpellPower(EditPlayerStatsPacket.clamp(m.spellPower, 0, 9999));
                data.setAttrQiSea(EditPlayerStatsPacket.clamp(m.qiSea, 0, 9999));
                data.setDefense(EditPlayerStatsPacket.clamp(m.defense, 0, 9999));
                Realm newRealm = RealmTopology.find(m.realmId).orElse(Realm.MORTAL);
                SubStage newSub = newRealm.subStageAt(m.subOrd);
                if (newSub == null) {
                    newSub = newRealm.firstSubStage();
                }
                RealmTransition.apply(data, RealmTransition.Request.adminEdit(newRealm, newSub));
                data.setRefining(EditPlayerStatsPacket.clamp(m.refining, 0, RefiningRank.values().length - 1));
                data.setAlchemy(EditPlayerStatsPacket.clamp(m.alchemy, 0, AlchemyRank.values().length - 1));
                data.setBoneAge(EditPlayerStatsPacket.clamp(m.boneAgeYears, 0, 1000000));
                data.setCurrentQi(data.getCurrentQi());
                data.setCultivationProgress(data.getCultivationProgress());
                TechniqueEffectHandler.refreshMaxHealth(player);
                CapabilityEvents.syncToClient(player);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.stat_editor.applied"), true);
            });
        });
        ctx.setPacketHandled(true);
    }
}

