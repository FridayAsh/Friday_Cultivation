package com.friday.cultivation.network;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.dao.FoundationDao;
import com.friday.cultivation.dao.GoldenCoreDao;
import com.friday.cultivation.event.TribulationHandler;
import com.friday.cultivation.item.ModItems;
import com.friday.cultivation.LifespanHelper;
import com.friday.cultivation.realm.Realm;
import com.friday.cultivation.realm.SubStage;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * 请求突破包 — 严格 1:1 复刻原 mod com.xiaoxiang.cultivation.network.RequestBreakthroughPacket。
 * 玩家请求境界突破；炼气圆满/筑基圆满时携带所选道基/金丹道，服务端校验并开启天劫。
 */
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
            CultivationCapability.get(player).ifPresent(data -> {
                if (!data.canBreakthrough()) {
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.breakthrough.not_ready"), true);
                    return;
                }
                if (data.getRealm() == Realm.MORTAL) {
                    TribulationHandler.completeBreakthroughWithoutTribulation(player, data);
                    return;
                }
                int waves = data.getRealm().tribulationCount(data.getSubStage());
                int boltsPerWave = data.getRealm().tribulationBoltsPerWave(data.getSubStage());
                int damage = data.getRealm().tribulationStrikeDamage();
                int boneAge = LifespanHelper.displayBoneAge(data);
                if (data.getRealm() == Realm.QI_REFINING && data.getSubStage() == SubStage.PEAK) {
                    FoundationDao selected = FoundationDao.byId(msg.foundationDaoId);
                    if (selected == FoundationDao.NONE) {
                        selected = data.bestEligibleFoundationDao(boneAge);
                    }
                    if (!data.isEligibleFoundationDao(selected, boneAge)) {
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.foundation.requirement"), false);
                        return;
                    }
                    data.setPendingFoundationDao(selected);
                    waves = RequestBreakthroughPacket.foundationTribulationWaves(selected);
                    boltsPerWave = 1;
                    damage = waves > 0 ? Realm.QI_REFINING.tribulationStrikeDamage() : 0;
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.foundation.dao_chosen", Component.translatable(selected.translationKey())), false);
                } else if (data.getRealm() == Realm.FOUNDATION_BUILDING && data.getSubStage() == SubStage.PEAK) {
                    boolean hasBloodTalisman = RequestBreakthroughPacket.hasItem(player, ModItems.BLOOD_TRANSFORMATION_TALISMAN.get());
                    GoldenCoreDao selected = GoldenCoreDao.byId(msg.goldenCoreDaoId);
                    if (selected == GoldenCoreDao.NONE) {
                        selected = data.bestEligibleGoldenCoreDao(boneAge, hasBloodTalisman);
                    }
                    if (!data.isEligibleGoldenCoreDao(selected, boneAge, hasBloodTalisman)) {
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.golden_core.requirement"), false);
                        return;
                    }
                    if (selected == GoldenCoreDao.BLOOD && data.getFoundationDao() != FoundationDao.BLOOD) {
                        RequestBreakthroughPacket.consumeItem(player, ModItems.BLOOD_TRANSFORMATION_TALISMAN.get(), 1);
                        player.displayClientMessage(Component.translatable("message.friday_cultivation.golden_core.blood_talisman_consumed"), false);
                    }
                    data.setPendingGoldenCoreDao(selected);
                    waves = selected.tribulationStrikes();
                    boltsPerWave = 1;
                    damage = selected.tribulationDamage();
                    player.displayClientMessage(Component.translatable("message.friday_cultivation.golden_core.dao_chosen", Component.translatable(selected.translationKey())), false);
                }
                if (waves <= 0) {
                    TribulationHandler.completeBreakthroughWithoutTribulation(player, data);
                    return;
                }
                TribulationHandler.beginTribulation(player, data, waves, boltsPerWave, damage);
                player.displayClientMessage(Component.translatable("message.friday_cultivation.tribulation.start", Realm.formatTribulationCount(waves, boltsPerWave), damage), false);
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
