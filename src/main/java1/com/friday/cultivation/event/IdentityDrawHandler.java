package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.dao.FoundationDao;
import com.friday.cultivation.identity.Identity;
import com.friday.cultivation.identity.draw.DrawCard;
import com.friday.cultivation.identity.draw.IdentityDrawDeck;
import com.friday.cultivation.identity.draw.IdentityDrawSampler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenIdentityDrawPacket;
import com.friday.cultivation.network.OriginRandomizedPacket;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.spirit.SpiritRoot;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

/**
 * 身份抽取服务端事件处理器 — 完整复刻自原模组 com.xiaoxiang.cultivation.event.IdentityDrawHandler
 * 补全：onLivingAttack / onLivingHurt / onPlayerTick / applyOriginProtection / clearOriginProtection /
 *       restoreInvisibleIfNeeded / isSingleplayer / isWaitingForOfflineAuth / grantStartingFatePlateIfNeeded /
 *       needsInitialOrigin / consumeReincarnationFatePlate / hasReincarnationFatePlate / grantReincarnationFatePlate
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class IdentityDrawHandler {

    private static final Map<UUID, IdentityDrawDeck> DECKS = new HashMap<>();
    private static final Map<UUID, Boolean> LIFE_CHART_PENDING = new HashMap<>();
    private static final Map<UUID, Boolean> PREVIOUS_INVULNERABLE = new HashMap<>();
    private static final Map<UUID, Boolean> PREVIOUS_INVISIBLE = new HashMap<>();
    private static final Random RNG = new Random();

    private IdentityDrawHandler() {
    }

    // ═══════════════════════════════════════════
    // 登录事件
    // ═══════════════════════════════════════════

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        tryOpenInitialOriginAfterAuth(serverPlayer);
    }

    public static void tryOpenInitialOriginAfterAuth(ServerPlayer player) {
        if (player == null || isWaitingForOfflineAuth(player)) {
            return;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data != null) startInitialOriginIfNeeded(player, data);
    }

    // ═══════════════════════════════════════════
    // 抽卡/重抽/确认
    // ═══════════════════════════════════════════

    public static void handleReveal(ServerPlayer player) {
        if (!canUseIdentityFlow(player)) return;
        if (!LIFE_CHART_PENDING.containsKey(player.getUUID())) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.use_first")
                            .withStyle(ChatFormatting.RED), true);
            return;
        }
        IdentityDrawDeck deck = DECKS.get(player.getUUID());
        if (deck == null || !deck.canRoll()) return;
        IdentityDrawDeck newDeck = IdentityDrawSampler.roll(deck, RNG);
        DECKS.put(player.getUUID(), newDeck);
        sendOpen(player, newDeck);
    }

    public static void handleConfirm(ServerPlayer player, int cardIndex) {
        if (!canUseIdentityFlow(player)) return;
        IdentityDrawDeck deck = DECKS.get(player.getUUID());
        if (deck == null || !deck.canConfirm(cardIndex)) return;
        DrawCard chosen = deck.cardAt(cardIndex);
        if (chosen == null) return;
        applyLifeChartOrigin(player,
                Identity.byId(chosen.identityId()),
                SpiritRoot.byId(chosen.spiritRootId()),
                Physique.MORTAL_BODY);
    }

    public static void handleChooseOrigin(ServerPlayer player, boolean random,
                                          String identityId, String spiritRootId,
                                          String physiqueId) {
        handleChooseOrigin(player, random, identityId, spiritRootId, physiqueId, false);
    }

    public static void handleChooseOrigin(ServerPlayer player, boolean random,
                                          String identityId, String spiritRootId,
                                          String physiqueId, boolean reconfigureMode) {
        if (!canUseIdentityFlow(player)) return;
        if (random) {
            if (reconfigureMode) {
                randomizeOrigin(player, false);
            } else {
                randomizeOriginFromLifeChart(player);
            }
            return;
        }
        Identity identity = Identity.byId(identityId);
        SpiritRoot spiritRoot = SpiritRoot.byId(spiritRootId);
        Physique physique = Physique.byId(physiqueId);
        if (!Identity.selectableOrigins().contains(identity)) {
            identity = Identity.FARMER;
        }
        if (!spiritRoot.isSelectableRoot()) {
            spiritRoot = SpiritRoot.HEAVENLY_HIDDEN;
        }
        if (reconfigureMode) {
            applyOrigin(player, identity, spiritRoot, physique, false, true, true);
        } else {
            applyLifeChartOrigin(player, identity, spiritRoot, physique);
        }
    }

    public static void reincarnateRandomOrigin(ServerPlayer player) {
        randomizeOrigin(player, true);
    }

    // ═══════════════════════════════════════════
    // 轮回命盘
    // ═══════════════════════════════════════════

    public static boolean openReincarnationFatePlate(ServerPlayer player) {
        if (!canUseIdentityFlow(player)) return false;
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null) return false;
        if (data.isSoulState() || data.isReincarnationPending() || data.isReincarnationReady()) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.blocked")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!player.getAbilities().instabuild && !hasReincarnationFatePlate(player)) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.missing")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }
        UUID id = player.getUUID();
        DECKS.put(id, IdentityDrawSampler.sampleNew(RNG));
        LIFE_CHART_PENDING.put(id, data.hasChosenIdentity());
        sendOpen(player, DECKS.get(id));
        return true;
    }

    public static void grantReincarnationFatePlate(ServerPlayer player) {
        if (player == null) return;
        ItemStack stack = new ItemStack(com.friday.cultivation.item.ModItems.REINCARNATION_FATE_PLATE.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    // ═══════════════════════════════════════════
    // 随机
    // ═══════════════════════════════════════════

    private static void randomizeOrigin(ServerPlayer player, boolean grantStarterItems) {
        Identity identity = IdentityDrawSampler.randomIdentity(RNG);
        SpiritRoot spiritRoot = IdentityDrawSampler.randomSpiritRoot(RNG);
        Physique physique = IdentityDrawSampler.randomPhysique(RNG);
        boolean applied = applyOrigin(player, identity, spiritRoot, physique,
                grantStarterItems, !grantStarterItems, false);
        if (applied) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new OriginRandomizedPacket(identity.id(), spiritRoot.id(),
                            physique.id(), grantStarterItems));
        }
    }

    private static void randomizeOriginFromLifeChart(ServerPlayer player) {
        Identity identity = IdentityDrawSampler.randomIdentity(RNG);
        SpiritRoot spiritRoot = IdentityDrawSampler.randomSpiritRoot(RNG);
        Physique physique = IdentityDrawSampler.randomPhysique(RNG);
        boolean applied = applyLifeChartOrigin(player, identity, spiritRoot, physique, false);
        if (applied) {
            ModNetwork.CHANNEL.send(
                    PacketDistributor.PLAYER.with(() -> player),
                    new OriginRandomizedPacket(identity.id(), spiritRoot.id(),
                            physique.id(), true));
        }
    }

    // ═══════════════════════════════════════════
    // 应用身份
    // ═══════════════════════════════════════════

    private static boolean applyLifeChartOrigin(ServerPlayer player, Identity identity,
                                                 SpiritRoot spiritRoot, Physique physique) {
        return applyLifeChartOrigin(player, identity, spiritRoot, physique, true);
    }

    private static boolean applyLifeChartOrigin(ServerPlayer player, Identity identity,
                                                 SpiritRoot spiritRoot, Physique physique,
                                                 boolean showMessage) {
        UUID id = player.getUUID();
        Boolean resetExistingLife = LIFE_CHART_PENDING.get(id);
        if (resetExistingLife == null) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.use_first")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!player.getAbilities().instabuild && !hasReincarnationFatePlate(player)) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.missing")
                            .withStyle(ChatFormatting.RED), true);
            return false;
        }
        boolean applied = false;
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data != null) {
            if (resetExistingLife) {
                resetExistingLifeForLifeChart(player, data);
            } else if (!consumeReincarnationFatePlate(player)) {
                return false;
            }
            data.setIdentityId(identity.id());
            data.setSpiritRoot(spiritRoot);
            data.setPhysique(physique);
            data.setSectId(identity.defaultSectId());
            int[] span = identity.lifespanRange();
            int lo = Math.min(span[0], span[1]);
            int hi = Math.max(span[0], span[1]);
            data.setMortalLifespan(lo + RNG.nextInt(hi - lo + 1));
            data.setBoneAge(14 + RNG.nextInt(5));
            data.setFoundationDao(FoundationDao.NONE);
            for (ItemStack stack : identity.starterItems()) {
                if (stack.isEmpty() || player.getInventory().add(stack)) continue;
                player.drop(stack, false);
            }
            if (showMessage) {
                MutableComponent identityName = Component.translatable(identity.translationKey());
                MutableComponent rootName = Component.translatable(spiritRoot.translationKey());
                MutableComponent physiqueName = Component.translatable(physique.translationKey());
                player.sendSystemMessage(Component.translatable(
                        "screen.friday_cultivation.identity_draw.confirmed",
                        identityName, rootName, physiqueName));
            }
            applied = true;
        }
        if (!applied) return false;
        DECKS.remove(id);
        LIFE_CHART_PENDING.remove(id);
        clearOriginProtection(player);
        com.friday.cultivation.event.CapabilityEvents.syncToClient(player);
        return true;
    }

    private static boolean applyOrigin(ServerPlayer player, Identity identity,
                                        SpiritRoot spiritRoot, Physique physique) {
        return applyOrigin(player, identity, spiritRoot, physique, true, false, true);
    }

    private static boolean applyOrigin(ServerPlayer player, Identity identity,
                                        SpiritRoot spiritRoot, Physique physique,
                                        boolean grantStarterItems, boolean allowExistingIdentity,
                                        boolean showMessage) {
        boolean applied = false;
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data != null) {
            if (data.hasChosenIdentity() && !allowExistingIdentity) return false;
            data.setIdentityId(identity.id());
            data.setSpiritRoot(spiritRoot);
            data.setPhysique(physique);
            data.setSectId(identity.defaultSectId());
            if (grantStarterItems) {
                int[] span = identity.lifespanRange();
                int lo = Math.min(span[0], span[1]);
                int hi = Math.max(span[0], span[1]);
                data.setMortalLifespan(lo + RNG.nextInt(hi - lo + 1));
                data.setBoneAge(14 + RNG.nextInt(5));
                data.setFoundationDao(FoundationDao.NONE);
            }
            if (grantStarterItems) {
                for (ItemStack stack : identity.starterItems()) {
                    if (stack.isEmpty() || player.getInventory().add(stack)) continue;
                    player.drop(stack, false);
                }
            }
            if (showMessage) {
                MutableComponent identityName = Component.translatable(identity.translationKey());
                MutableComponent rootName = Component.translatable(spiritRoot.translationKey());
                MutableComponent physiqueName = Component.translatable(physique.translationKey());
                String key = grantStarterItems
                        ? "screen.friday_cultivation.identity_draw.confirmed"
                        : "message.friday_cultivation.origin_reconfiguration_token.applied";
                player.sendSystemMessage(Component.translatable(key,
                        identityName, rootName, physiqueName));
            }
            applied = true;
        }
        if (!applied) return false;
        DECKS.remove(player.getUUID());
        LIFE_CHART_PENDING.remove(player.getUUID());
        clearOriginProtection(player);
        com.friday.cultivation.event.CapabilityEvents.syncToClient(player);
        return true;
    }

    private static void resetExistingLifeForLifeChart(ServerPlayer player, CultivationData data) {
        int difuReincarnationEntries = data.getDifuReincarnationEntries();
        data.copyFrom(new CultivationData());
        data.setDifuReincarnationEntries(difuReincarnationEntries);
        player.kill();
        player.getInventory().clearContent();
        player.setExperienceLevels(0);
        player.setExperiencePoints(0);
        player.setHealth(player.getMaxHealth());
        player.stopUsingItem();
        player.setLastHurtByPlayer(null);
        player.setLastHurtByMob(player.getLastHurtByMob());
        com.friday.cultivation.event.SoulStateHandler.clearDeathHostility(player);
    }

    private static boolean consumeReincarnationFatePlate(ServerPlayer player) {
        if (player.getAbilities().instabuild) return true;
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(com.friday.cultivation.item.ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            stack.shrink(1);
            return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.is(com.friday.cultivation.item.ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static boolean hasReincarnationFatePlate(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is(com.friday.cultivation.item.ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            return true;
        }
        for (ItemStack stack : player.getInventory().armor) {
            if (!stack.is(com.friday.cultivation.item.ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            return true;
        }
        return false;
    }

    // ═══════════════════════════════════════════
    // 保护机制（补全 5 个原模组方法）
    // ═══════════════════════════════════════════

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (needsInitialOrigin(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof ServerPlayer player)) return;
        if (needsInitialOrigin(player)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        Player player = event.player;
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        if (needsInitialOrigin(serverPlayer)) {
            applyOriginProtection(serverPlayer);
        } else {
            clearOriginProtection(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        clearOriginProtection(serverPlayer);
        DECKS.remove(serverPlayer.getUUID());
        LIFE_CHART_PENDING.remove(serverPlayer.getUUID());
    }

    public static IdentityDrawDeck getDeck(UUID playerId) {
        return DECKS.get(playerId);
    }

    public static boolean isDrawing(UUID playerId) {
        return DECKS.containsKey(playerId);
    }

    public static void openReconfiguration(ServerPlayer player) {
        if (!canUseIdentityFlow(player)) return;
        LIFE_CHART_PENDING.remove(player.getUUID());
        IdentityDrawDeck deck = IdentityDrawSampler.sampleNew(RNG);
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OpenIdentityDrawPacket(deck, true));
    }

    private static void sendOpen(ServerPlayer player, IdentityDrawDeck deck) {
        ModNetwork.CHANNEL.send(
                PacketDistributor.PLAYER.with(() -> player),
                new OpenIdentityDrawPacket(deck));
    }

    private static void startInitialOriginIfNeeded(ServerPlayer player, CultivationData data) {
        if (data.hasChosenIdentity()) {
            clearOriginProtection(player);
            DECKS.remove(player.getUUID());
            LIFE_CHART_PENDING.remove(player.getUUID());
            return;
        }
        grantStartingFatePlateIfNeeded(player);
    }

    private static void grantStartingFatePlateIfNeeded(ServerPlayer player) {
        if (hasReincarnationFatePlate(player)) {
            player.displayClientMessage(
                    Component.translatable("message.friday_cultivation.reincarnation_fate_plate.initial_hint")
                            .withStyle(ChatFormatting.AQUA), true);
            return;
        }
        grantReincarnationFatePlate(player);
        player.sendSystemMessage(
                Component.translatable("message.friday_cultivation.reincarnation_fate_plate.initial_granted"));
    }

    private static boolean needsInitialOrigin(ServerPlayer player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        return data != null && !data.hasChosenIdentity();
    }

    private static boolean canUseIdentityFlow(ServerPlayer player) {
        if (player == null) return false;
        if (!isWaitingForOfflineAuth(player)) return true;
        player.displayClientMessage(
                Component.translatable("message.friday_cultivation.offline_auth.actionbar"), true);
        return false;
    }

    private static boolean isWaitingForOfflineAuth(ServerPlayer player) {
        return ModCommonConfig.offlineAuthEnabled() && !OfflineAuthHandler.isAuthenticated(player);
    }

    private static void applyOriginProtection(ServerPlayer player) {
        UUID id = player.getUUID();
        PREVIOUS_INVULNERABLE.putIfAbsent(id, player.isInvulnerable());
        player.setInvulnerable(true);
        player.invulnerableTime = 0;
        if (!isSingleplayer(player)) {
            PREVIOUS_INVISIBLE.putIfAbsent(id, player.isInvisible());
            player.setInvisible(true);
        } else {
            restoreInvisibleIfNeeded(player);
        }
    }

    private static void clearOriginProtection(ServerPlayer player) {
        UUID id = player.getUUID();
        Boolean previousInvulnerable = PREVIOUS_INVULNERABLE.remove(id);
        if (previousInvulnerable != null) {
            player.setInvulnerable(previousInvulnerable);
        }
        restoreInvisibleIfNeeded(player);
    }

    private static void restoreInvisibleIfNeeded(ServerPlayer player) {
        Boolean previousInvisible = PREVIOUS_INVISIBLE.remove(player.getUUID());
        if (previousInvisible != null) {
            player.setInvisible(previousInvisible);
        }
    }

    private static boolean isSingleplayer(ServerPlayer player) {
        return player.getServer() != null && player.getServer().isSingleplayer();
    }
}
