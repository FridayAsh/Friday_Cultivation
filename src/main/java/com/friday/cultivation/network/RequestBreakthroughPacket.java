/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.network.FriendlyByteBuf
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraftforge.network.NetworkEvent$Context
 */
package com.friday.cultivation.network;

import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.GoldenCoreDao;
import com.friday.cultivation.cultivation.LifespanHelper;
import com.friday.cultivation.cultivation.realm.Realm;
import com.friday.cultivation.cultivation.realm.SubStage;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.registry.ModItems;
import java.util.function.Supplier;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

public class RequestBreakthroughPacket {
    private final String foundationDaoId;
    private final String goldenCoreDaoId;

    public RequestBreakthroughPacket() {
        this("", "");
    }

    public RequestBreakthroughPacket(FoundationDao foundationDao, GoldenCoreDao goldenCoreDao) {
        this(foundationDao == null ? "" : foundationDao.id(), goldenCoreDao == null ? "" : goldenCoreDao.id());
    }

    public RequestBreakthroughPacket(String foundationDaoId, String goldenCoreDaoId) {
        this.foundationDaoId = foundationDaoId == null ? "" : foundationDaoId;
        this.goldenCoreDaoId = goldenCoreDaoId == null ? "" : goldenCoreDaoId;
    }

    public static void encode(RequestBreakthroughPacket msg, FriendlyByteBuf buf) {
        buf.writeUtf(msg.foundationDaoId);
        buf.writeUtf(msg.goldenCoreDaoId);
    }

    public static RequestBreakthroughPacket decode(FriendlyByteBuf buf) {
        return new RequestBreakthroughPacket(buf.readUtf(), buf.readUtf());
    }

    public static void handle(RequestBreakthroughPacket msg, Supplier<NetworkEvent.Context> ctxSup) {
        NetworkEvent.Context ctx = ctxSup.get();
        ctx.enqueueWork(() -> {
            ServerPlayer player = ctx.getSender();
            if (player == null) {
                return;
            }
            CultivationCapability.get((Player)player).ifPresent(data -> {
                if (!data.canBreakthrough()) {
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.breakthrough.not_ready"), true);
                    return;
                }
                if (data.getRealm() == Realm.MORTAL) {
                    TribulationHandler.completeBreakthroughWithoutTribulation(player, data);
                    return;
                }
                if (data.getRealm() == Realm.TRUE_IMMORTAL && data.getSubStage().isPeakFor(Realm.TRUE_IMMORTAL)) {
                    // 突破大帝前置：须曾亲手击杀过一位大帝生灵（条件之一，后续可扩展）
                    if (!data.hasKilledGreatEmperor()) {
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.breakthrough.great_emperor_requirement"), false);
                        return;
                    }
                }
                int waves = data.getRealm().tribulationCount(data.getSubStage());
                int boltsPerWave = data.getRealm().tribulationBoltsPerWave(data.getSubStage());
                int damage = data.getRealm().tribulationStrikeDamage();
                int boneAge = LifespanHelper.displayBoneAge(data);
                if (data.getRealm() == Realm.QI_REFINING && data.getSubStage().isPeakFor(Realm.QI_REFINING)) {
                    FoundationDao selected = FoundationDao.byId(msg.foundationDaoId);
                    if (selected == FoundationDao.NONE) {
                        selected = data.bestEligibleFoundationDao(boneAge);
                    }
                    if (!data.isEligibleFoundationDao(selected, boneAge)) {
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.foundation.requirement"), false);
                        return;
                    }
                    data.setPendingFoundationDao(selected);
                    waves = RequestBreakthroughPacket.foundationTribulationWaves(selected);
                    boltsPerWave = 1;
                    damage = waves > 0 ? Realm.QI_REFINING.tribulationStrikeDamage() : 0;
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.foundation.dao_chosen", (Object[])new Object[]{Component.translatable((String)selected.translationKey())}), false);
                } else if (data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage().isPeakFor(Realm.FOUNDATION_BUILDING)) {
                    boolean hasBloodTalisman = RequestBreakthroughPacket.hasItem(player, (Item)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get());
                    GoldenCoreDao selected = GoldenCoreDao.byId(msg.goldenCoreDaoId);
                    if (selected == GoldenCoreDao.NONE) {
                        selected = data.bestEligibleGoldenCoreDao(boneAge, hasBloodTalisman);
                    }
                    if (!data.isEligibleGoldenCoreDao(selected, boneAge, hasBloodTalisman)) {
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.golden_core.requirement"), false);
                        return;
                    }
                    if (selected == GoldenCoreDao.BLOOD && data.getFoundationDao() != FoundationDao.BLOOD) {
                        RequestBreakthroughPacket.consumeItem(player, (Item)ModItems.BLOOD_TRANSFORMATION_TALISMAN.get(), 1);
                        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.golden_core.blood_talisman_consumed"), false);
                    }
                    data.setPendingGoldenCoreDao(selected);
                    waves = selected.tribulationStrikes();
                    boltsPerWave = 1;
                    damage = selected.tribulationDamage();
                    player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.golden_core.dao_chosen", (Object[])new Object[]{Component.translatable((String)selected.translationKey())}), false);
                }
                if (waves <= 0) {
                    TribulationHandler.completeBreakthroughWithoutTribulation(player, data);
                    return;
                }
                TribulationHandler.beginTribulation(player, data, waves, boltsPerWave, damage);
                player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.tribulation.start", (Object[])new Object[]{Realm.formatTribulationCount(waves, boltsPerWave), damage}), false);
            });
        });
        ctx.setPacketHandled(true);
    }

    private static boolean hasItem(ServerPlayer player, Item item) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(item)) continue;
            return true;
        }
        return false;
    }

    private static void consumeItem(ServerPlayer player, Item item, int count) {
        int remaining = Math.max(0, count);
        for (ItemStack stack : player.getInventory().items) {
            if (remaining <= 0) {
                return;
            }
            if (!stack.is(item)) continue;
            int take = Math.min(remaining, stack.getCount());
            stack.shrink(take);
            remaining -= take;
        }
    }

    private static int foundationTribulationWaves(FoundationDao dao) {
        return switch (dao) {
            case EARTH -> 1;
            case HEAVEN -> 3;
            default -> 0;
        };
    }
}

