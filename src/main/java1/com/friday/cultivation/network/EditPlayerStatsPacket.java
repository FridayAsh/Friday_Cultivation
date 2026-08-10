package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.alchemy.AlchemyRank;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import com.friday.cultivation.refining.RefiningRank;
import com.friday.cultivation.event.CapabilityEvents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 编辑玩家属性包（严格照搬原模组 com.xiaoxiang.cultivation.network.EditPlayerStatsPacket）。
 * <p>客户端 → 服务端，仅 debug 用。把 6 项属性 + 境界 + 副阶 + 炼器 + 炼丹 + 骨龄写入 {@link ICultivation}。</p>
 */
public class EditPlayerStatsPacket {
    private static final int ATTR_CAP = 9999;
    private static final int BONE_AGE_CAP = 1000000;
    private final int constitution;
    private final int physique;
    private final int agility;
    private final int spellPower;
    private final int qiSea;
    private final int defense;
    private final int realmOrd;
    private final int subOrd;
    private final int refining;
    private final int alchemy;
    private final int boneAgeYears;

    public EditPlayerStatsPacket(int constitution, int physique, int agility, int spellPower,
                                 int qiSea, int defense, int realmOrd, int subOrd,
                                 int refining, int alchemy, int boneAgeYears) {
        this.constitution = constitution;
        this.physique = physique;
        this.agility = agility;
        this.spellPower = spellPower;
        this.qiSea = qiSea;
        this.defense = defense;
        this.realmOrd = realmOrd;
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
        b.writeVarInt(m.realmOrd);
        b.writeVarInt(m.subOrd);
        b.writeVarInt(m.refining);
        b.writeVarInt(m.alchemy);
        b.writeVarInt(m.boneAgeYears);
    }

    public static EditPlayerStatsPacket decode(FriendlyByteBuf b) {
        return new EditPlayerStatsPacket(
                b.readVarInt(), b.readVarInt(), b.readVarInt(),
                b.readVarInt(), b.readVarInt(), b.readVarInt(),
                b.readVarInt(), b.readVarInt(),
                b.readVarInt(), b.readVarInt(), b.readVarInt());
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
            CultivationData ic = CultivationCapability.get((Player) player).orElse(null);
            if (ic == null) {
                return;
            }
            ic.setAttrConstitution(clamp(m.constitution, 0, ATTR_CAP));
            ic.setAttrPhysique(clamp(m.physique, 0, ATTR_CAP));
            ic.setAttrAgility(clamp(m.agility, 0, ATTR_CAP));
            ic.setAttrSpellPower(clamp(m.spellPower, 0, ATTR_CAP));
            ic.setAttrQiSea(clamp(m.qiSea, 0, ATTR_CAP));
            ic.setDefense(clamp(m.defense, 0, ATTR_CAP));
            Realm[] realms = Realm.values();
            SubStage[] subs = SubStage.values();
            Realm newRealm = realms[clamp(m.realmOrd, 0, realms.length - 1)];
            SubStage newSub = subs[clamp(m.subOrd, 0, subs.length - 1)];
            boolean realmChanged = ic.getRealm() != newRealm || ic.getSubStage() != newSub;
            ic.setRealm(newRealm);
            ic.setSubStage(newSub);
            ic.setRefining(clamp(m.refining, 0, RefiningRank.values().length - 1));
            ic.setAlchemy(clamp(m.alchemy, 0, AlchemyRank.values().length - 1));
            ic.setBoneAge(clamp(m.boneAgeYears, 0, BONE_AGE_CAP));
            if (realmChanged) {
                ic.setCultivationProgress(0L);
                ic.setCurrentQi(ic.getMaxQi() / 2L);
            }
            ic.setCurrentQi(ic.getCurrentQi());
            ic.setCultivationProgress(ic.getCultivationProgress());
            CapabilityEvents.syncToClient((ServerPlayer) player);
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.stat_editor.applied"), true);
        });
        ctx.setPacketHandled(true);
    }
}
