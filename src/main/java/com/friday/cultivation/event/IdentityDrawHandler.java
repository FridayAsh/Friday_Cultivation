/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.ChatFormatting
 *  net.minecraft.network.chat.Component
 *  net.minecraft.network.chat.MutableComponent
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.item.Item
 *  net.minecraft.world.item.ItemStack
 *  net.minecraft.world.level.ItemLike
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.living.LivingHurtEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  net.minecraftforge.network.PacketDistributor
 */
package com.friday.cultivation.event;

import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.CultivationData;
import com.friday.cultivation.cultivation.FoundationDao;
import com.friday.cultivation.cultivation.Identity;
import com.friday.cultivation.cultivation.Physique;
import com.friday.cultivation.cultivation.SpiritRoot;
import com.friday.cultivation.cultivation.draw.DrawCard;
import com.friday.cultivation.cultivation.draw.IdentityDrawDeck;
import com.friday.cultivation.cultivation.draw.IdentityDrawSampler;
import com.friday.cultivation.event.CapabilityEvents;
import com.friday.cultivation.event.OfflineAuthHandler;
import com.friday.cultivation.event.SoulStateHandler;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.OpenIdentityDrawPacket;
import com.friday.cultivation.network.OriginRandomizedPacket;
import com.friday.cultivation.registry.ModItems;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class IdentityDrawHandler {
    private static final Map<UUID, IdentityDrawDeck> DECKS = new HashMap<UUID, IdentityDrawDeck>();
    private static final Map<UUID, Boolean> LIFE_CHART_PENDING = new HashMap<UUID, Boolean>();
    private static final Random RNG = new Random();

    private IdentityDrawHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        IdentityDrawHandler.tryOpenInitialOriginAfterAuth(player2);
    }

    public static void tryOpenInitialOriginAfterAuth(ServerPlayer player) {
        if (player == null || IdentityDrawHandler.isWaitingForOfflineAuth(player)) {
            return;
        }
        CultivationCapability.get((Player)player).ifPresent(data -> IdentityDrawHandler.startInitialOriginIfNeeded(player, data));
    }

    public static void handleReveal(ServerPlayer player) {
        if (!IdentityDrawHandler.canUseIdentityFlow(player)) {
            return;
        }
        if (!LIFE_CHART_PENDING.containsKey(player.getUUID())) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.use_first").withStyle(ChatFormatting.RED), true);
            return;
        }
        IdentityDrawDeck deck = DECKS.get(player.getUUID());
        if (deck == null || !deck.canRoll()) {
            return;
        }
        IdentityDrawDeck newDeck = IdentityDrawSampler.roll(deck, RNG);
        DECKS.put(player.getUUID(), newDeck);
        IdentityDrawHandler.sendOpen(player, newDeck);
    }

    public static void handleConfirm(ServerPlayer player, int cardIndex) {
        if (!IdentityDrawHandler.canUseIdentityFlow(player)) {
            return;
        }
        IdentityDrawDeck deck = DECKS.get(player.getUUID());
        if (deck == null || !deck.canConfirm(cardIndex)) {
            return;
        }
        DrawCard chosen = deck.cardAt(cardIndex);
        if (chosen == null) {
            return;
        }
        IdentityDrawHandler.applyLifeChartOrigin(player, Identity.byId(chosen.identityId()), SpiritRoot.byId(chosen.spiritRootId()), Physique.MORTAL_BODY);
    }

    public static void handleChooseOrigin(ServerPlayer player, boolean random, String identityId, String spiritRootId, String physiqueId) {
        IdentityDrawHandler.handleChooseOrigin(player, random, identityId, spiritRootId, physiqueId, false);
    }

    public static void handleChooseOrigin(ServerPlayer player, boolean random, String identityId, String spiritRootId, String physiqueId, boolean reconfigureMode) {
        if (!IdentityDrawHandler.canUseIdentityFlow(player)) {
            return;
        }
        if (random) {
            if (reconfigureMode) {
                IdentityDrawHandler.randomizeOrigin(player, false);
            } else {
                IdentityDrawHandler.randomizeOriginFromLifeChart(player);
            }
            return;
        }
        Identity identity = Identity.byId(identityId);
        SpiritRoot spiritRoot = SpiritRoot.byId(spiritRootId);
        Physique physique = Physique.byId(physiqueId);
        if (!Identity.selectableOrigins().contains((Object)identity)) {
            identity = Identity.FARMER;
        }
        if (!spiritRoot.isSelectableRoot()) {
            spiritRoot = SpiritRoot.HEAVENLY_HIDDEN;
        }
        if (reconfigureMode) {
            IdentityDrawHandler.applyOrigin(player, identity, spiritRoot, physique, false, true, true);
        } else {
            IdentityDrawHandler.applyLifeChartOrigin(player, identity, spiritRoot, physique);
        }
    }

    public static void reincarnateRandomOrigin(ServerPlayer player) {
        IdentityDrawHandler.randomizeOrigin(player, true);
    }

    public static boolean openReincarnationFatePlate(ServerPlayer player) {
        if (!IdentityDrawHandler.canUseIdentityFlow(player)) {
            return false;
        }
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        if (data == null) {
            return false;
        }
        if (data.isSoulState() || data.isReincarnationPending() || data.isReincarnationReady()) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.blocked").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!player.getAbilities().instabuild && !IdentityDrawHandler.hasReincarnationFatePlate(player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.missing").withStyle(ChatFormatting.RED), true);
            return false;
        }
        UUID id = player.getUUID();
        DECKS.put(id, IdentityDrawSampler.sampleNew(RNG));
        LIFE_CHART_PENDING.put(id, data.hasChosenIdentity());
        IdentityDrawHandler.sendOpen(player, DECKS.get(id));
        return true;
    }

    public static void grantReincarnationFatePlate(ServerPlayer player) {
        if (player == null) {
            return;
        }
        ItemStack stack = new ItemStack((ItemLike)ModItems.REINCARNATION_FATE_PLATE.get());
        if (!player.getInventory().add(stack)) {
            player.drop(stack, false);
        }
    }

    private static void randomizeOrigin(ServerPlayer player, boolean grantStarterItems) {
        Physique physique;
        SpiritRoot spiritRoot;
        Identity identity = IdentityDrawSampler.randomIdentity(RNG);
        boolean applied = IdentityDrawHandler.applyOrigin(player, identity, spiritRoot = IdentityDrawSampler.randomSpiritRoot(RNG), physique = IdentityDrawSampler.randomPhysique(RNG), grantStarterItems, !grantStarterItems, false);
        if (applied) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OriginRandomizedPacket(identity.id(), spiritRoot.id(), physique.id(), grantStarterItems));
        }
    }

    private static void randomizeOriginFromLifeChart(ServerPlayer player) {
        Physique physique;
        SpiritRoot spiritRoot;
        Identity identity = IdentityDrawSampler.randomIdentity(RNG);
        boolean applied = IdentityDrawHandler.applyLifeChartOrigin(player, identity, spiritRoot = IdentityDrawSampler.randomSpiritRoot(RNG), physique = IdentityDrawSampler.randomPhysique(RNG), false);
        if (applied) {
            ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OriginRandomizedPacket(identity.id(), spiritRoot.id(), physique.id(), true));
        }
    }

    private static boolean applyLifeChartOrigin(ServerPlayer player, Identity identity, SpiritRoot spiritRoot, Physique physique) {
        return IdentityDrawHandler.applyLifeChartOrigin(player, identity, spiritRoot, physique, true);
    }

    private static boolean applyLifeChartOrigin(ServerPlayer player, Identity identity, SpiritRoot spiritRoot, Physique physique, boolean showMessage) {
        UUID id = player.getUUID();
        Boolean resetExistingLife = LIFE_CHART_PENDING.get(id);
        if (resetExistingLife == null) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.use_first").withStyle(ChatFormatting.RED), true);
            return false;
        }
        if (!player.getAbilities().instabuild && !IdentityDrawHandler.hasReincarnationFatePlate(player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.missing").withStyle(ChatFormatting.RED), true);
            return false;
        }
        boolean[] applied = new boolean[]{false};
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (resetExistingLife.booleanValue()) {
                IdentityDrawHandler.resetExistingLifeForLifeChart(player, data);
            } else if (!IdentityDrawHandler.consumeReincarnationFatePlate(player)) {
                return;
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
                MutableComponent identityName = Component.translatable((String)identity.translationKey());
                MutableComponent rootName = Component.translatable((String)spiritRoot.translationKey());
                MutableComponent physiqueName = Component.translatable((String)physique.translationKey());
                player.sendSystemMessage((Component)Component.translatable((String)"screen.friday_cultivation.identity_draw.confirmed", (Object[])new Object[]{identityName, rootName, physiqueName}));
            }
            applied[0] = true;
        });
        if (!applied[0]) {
            return false;
        }
        DECKS.remove(id);
        LIFE_CHART_PENDING.remove(id);
        CapabilityEvents.syncToClient(player);
        return true;
    }

    private static boolean applyOrigin(ServerPlayer player, Identity identity, SpiritRoot spiritRoot, Physique physique) {
        return IdentityDrawHandler.applyOrigin(player, identity, spiritRoot, physique, true, false, true);
    }

    private static boolean applyOrigin(ServerPlayer player, Identity identity, SpiritRoot spiritRoot, Physique physique, boolean grantStarterItems, boolean allowExistingIdentity, boolean showMessage) {
        boolean[] applied = new boolean[]{false};
        CultivationCapability.get((Player)player).ifPresent(data -> {
            if (data.hasChosenIdentity() && !allowExistingIdentity) {
                return;
            }
            data.setIdentityId(identity.id());
            data.setSpiritRoot(spiritRoot);
            data.setPhysique(physique);
            data.setSectId(identity.defaultSectId());
            if (grantStarterItems) {
                int[] range = identity.lifespanRange();
                int lo = Math.min(range[0], range[1]);
                int hi = Math.max(range[0], range[1]);
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
                MutableComponent identityName = Component.translatable((String)identity.translationKey());
                MutableComponent rootName = Component.translatable((String)spiritRoot.translationKey());
                MutableComponent physiqueName = Component.translatable((String)physique.translationKey());
                String key = grantStarterItems ? "screen.friday_cultivation.identity_draw.confirmed" : "message.friday_cultivation.origin_reconfiguration_token.applied";
                player.sendSystemMessage((Component)Component.translatable((String)key, (Object[])new Object[]{identityName, rootName, physiqueName}));
            }
            applied[0] = true;
        });
        if (!applied[0]) {
            return false;
        }
        DECKS.remove(player.getUUID());
        LIFE_CHART_PENDING.remove(player.getUUID());
        CapabilityEvents.syncToClient(player);
        return true;
    }

    private static void resetExistingLifeForLifeChart(ServerPlayer player, CultivationData data) {
        int difuReincarnationEntries = data.getDifuReincarnationEntries();
        data.copyFrom(new CultivationData());
        data.setDifuReincarnationEntries(difuReincarnationEntries);
        TechniqueEffectHandler.clearBodyTemperingHpBonus(player);
        player.stopRiding();
        player.getInventory().clearContent();
        player.removeAllEffects();
        player.setHealth(player.getMaxHealth());
        SoulStateHandler.clearDeathHostility(player);
    }

    private static boolean consumeReincarnationFatePlate(ServerPlayer player) {
        if (player.getAbilities().instabuild) {
            return true;
        }
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is((Item)ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            stack.shrink(1);
            return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.is((Item)ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            stack.shrink(1);
            return true;
        }
        return false;
    }

    private static boolean hasReincarnationFatePlate(ServerPlayer player) {
        for (ItemStack stack : player.getInventory().items) {
            if (!stack.is((Item)ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            return true;
        }
        for (ItemStack stack : player.getInventory().offhand) {
            if (!stack.is((Item)ModItems.REINCARNATION_FATE_PLATE.get())) continue;
            return true;
        }
        return false;
    }

    @SubscribeEvent
    public static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        DECKS.remove(player2.getUUID());
        LIFE_CHART_PENDING.remove(player2.getUUID());
    }

    public static IdentityDrawDeck getDeck(UUID playerId) {
        return DECKS.get(playerId);
    }

    public static boolean isDrawing(UUID playerId) {
        return DECKS.containsKey(playerId);
    }

    public static void openReconfiguration(ServerPlayer player) {
        if (!IdentityDrawHandler.canUseIdentityFlow(player)) {
            return;
        }
        LIFE_CHART_PENDING.remove(player.getUUID());
        IdentityDrawDeck deck = IdentityDrawSampler.sampleNew(RNG);
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenIdentityDrawPacket(deck, true));
    }

    private static void sendOpen(ServerPlayer player, IdentityDrawDeck deck) {
        ModNetwork.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), (Object)new OpenIdentityDrawPacket(deck));
    }

    private static void startInitialOriginIfNeeded(ServerPlayer player, CultivationData data) {
        if (data.hasChosenIdentity()) {
            DECKS.remove(player.getUUID());
            LIFE_CHART_PENDING.remove(player.getUUID());
            return;
        }
        IdentityDrawHandler.grantStartingFatePlateIfNeeded(player);
    }

    private static void grantStartingFatePlateIfNeeded(ServerPlayer player) {
        if (IdentityDrawHandler.hasReincarnationFatePlate(player)) {
            player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.initial_hint").withStyle(ChatFormatting.AQUA), true);
            return;
        }
        IdentityDrawHandler.grantReincarnationFatePlate(player);
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.reincarnation_fate_plate.initial_granted"));
    }

    private static boolean needsInitialOrigin(ServerPlayer player) {
        CultivationData data = CultivationCapability.get((Player)player).orElse(null);
        return data != null && !data.hasChosenIdentity();
    }

    private static boolean canUseIdentityFlow(ServerPlayer player) {
        if (player == null) {
            return false;
        }
        if (!IdentityDrawHandler.isWaitingForOfflineAuth(player)) {
            return true;
        }
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.actionbar"), true);
        return false;
    }

    private static boolean isWaitingForOfflineAuth(ServerPlayer player) {
        return ModCommonConfig.offlineAuthEnabled() && !OfflineAuthHandler.isAuthenticated(player);
    }
}

