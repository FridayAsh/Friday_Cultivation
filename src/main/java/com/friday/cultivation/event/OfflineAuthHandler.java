/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.exceptions.CommandSyntaxException
 *  net.minecraft.ChatFormatting
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerLevel
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraft.world.InteractionResult
 *  net.minecraft.world.entity.Entity
 *  net.minecraft.world.entity.LivingEntity
 *  net.minecraft.world.entity.player.Player
 *  net.minecraft.world.phys.Vec3
 *  net.minecraftforge.event.CommandEvent
 *  net.minecraftforge.event.ServerChatEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.event.TickEvent$PlayerTickEvent
 *  net.minecraftforge.event.entity.item.ItemTossEvent
 *  net.minecraftforge.event.entity.living.LivingAttackEvent
 *  net.minecraftforge.event.entity.player.AttackEntityEvent
 *  net.minecraftforge.event.entity.player.EntityItemPickupEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$BreakSpeed
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedInEvent
 *  net.minecraftforge.event.entity.player.PlayerEvent$PlayerLoggedOutEvent
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteract
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$EntityInteractSpecific
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$LeftClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickBlock
 *  net.minecraftforge.event.entity.player.PlayerInteractEvent$RightClickItem
 *  net.minecraftforge.event.level.BlockEvent$BreakEvent
 *  net.minecraftforge.event.level.BlockEvent$EntityPlaceEvent
 *  net.minecraftforge.eventbus.api.Event$Result
 *  net.minecraftforge.eventbus.api.EventPriority
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.event;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.event.IdentityDrawHandler;
import com.friday.cultivation.util.OfflineAuthStore;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class OfflineAuthHandler {
    private static final Set<String> AUTH_COMMANDS = Set.of("register", "login", "\u8a3b\u518a", "\u6ce8\u518c", "\u767b\u5165", "\u767b\u5f55");
    private static final Map<UUID, PendingSession> PENDING = new ConcurrentHashMap<UUID, PendingSession>();
    private static final Map<UUID, String> AUTHENTICATED_NAMES = new ConcurrentHashMap<UUID, String>();

    private OfflineAuthHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!OfflineAuthHandler.enabled()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        String name = OfflineAuthHandler.normalizedName(player2);
        AUTHENTICATED_NAMES.remove(player2.getUUID());
        PENDING.put(player2.getUUID(), PendingSession.capture(player2));
        OfflineAuthHandler.sendPrompt(player2, OfflineAuthStore.isRegistered(name));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        PENDING.remove(event.getEntity().getUUID());
        AUTHENTICATED_NAMES.remove(event.getEntity().getUUID());
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!OfflineAuthHandler.enabled()) {
            return;
        }
        ServerPlayer player = OfflineAuthHandler.sourcePlayer(event);
        if (player == null || OfflineAuthHandler.isAuthenticated(player)) {
            return;
        }
        String input = event.getParseResults().getReader().getString().trim();
        String root = input.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (AUTH_COMMANDS.contains(root)) {
            return;
        }
        event.setCanceled(true);
        ((CommandSourceStack)event.getParseResults().getContext().getSource()).sendFailure((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.only_auth_commands"));
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onServerChat(ServerChatEvent event) {
        if (!OfflineAuthHandler.enabled() || OfflineAuthHandler.isAuthenticated(event.getPlayer())) {
            return;
        }
        event.setCanceled(true);
        event.getPlayer().sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.chat_blocked"));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        int timeoutSeconds;
        if (!OfflineAuthHandler.enabled() || event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer)) {
            return;
        }
        ServerPlayer player2 = (ServerPlayer)player;
        PendingSession session = PENDING.get(player2.getUUID());
        if (session == null) {
            return;
        }
        OfflineAuthHandler.freeze(player2, session);
        long ageTicks = player2.serverLevel().getGameTime() - session.joinGameTime;
        if (ageTicks % 100L == 0L) {
            player2.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.actionbar"), true);
        }
        if ((timeoutSeconds = ModCommonConfig.offlineAuthLoginTimeoutSeconds()) > 0 && ageTicks >= (long)timeoutSeconds * 20L) {
            player2.connection.disconnect(Component.translatable((String)"message.friday_cultivation.offline_auth.timeout_kick"));
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        Entity attacker = event.getSource().getEntity();
        if (OfflineAuthHandler.requiresAuth((Entity)target) || OfflineAuthHandler.requiresAuth(attacker)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setNewSpeed(0.0f);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        if (OfflineAuthHandler.requiresAuth(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onItemToss(ItemTossEvent event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority=EventPriority.HIGHEST)
    public static void onItemPickup(EntityItemPickupEvent event) {
        if (OfflineAuthHandler.requiresAuth((Entity)event.getEntity())) {
            event.setCanceled(true);
        }
    }

    public static boolean isAuthenticated(ServerPlayer player) {
        if (!OfflineAuthHandler.enabled()) {
            return true;
        }
        return !PENDING.containsKey(player.getUUID()) && AUTHENTICATED_NAMES.containsKey(player.getUUID());
    }

    public static boolean isOnlineAuthenticated(String playerName) {
        String normalized = OfflineAuthStore.normalizeName(playerName);
        return AUTHENTICATED_NAMES.containsValue(normalized);
    }

    public static void forgetAuthenticatedName(String playerName) {
        String normalized = OfflineAuthStore.normalizeName(playerName);
        AUTHENTICATED_NAMES.entrySet().removeIf(entry -> ((String)entry.getValue()).equals(normalized));
    }

    public static void markAuthenticated(ServerPlayer player) {
        PENDING.remove(player.getUUID());
        AUTHENTICATED_NAMES.put(player.getUUID(), OfflineAuthHandler.normalizedName(player));
        player.displayClientMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.unlocked"), true);
        IdentityDrawHandler.tryOpenInitialOriginAfterAuth(player);
    }

    private static boolean enabled() {
        return ModCommonConfig.offlineAuthEnabled();
    }

    private static boolean requiresAuth(Entity entity) {
        ServerPlayer player;
        return OfflineAuthHandler.enabled() && entity instanceof ServerPlayer && !OfflineAuthHandler.isAuthenticated(player = (ServerPlayer)entity);
    }

    private static ServerPlayer sourcePlayer(CommandEvent event) {
        CommandSourceStack source = (CommandSourceStack)event.getParseResults().getContext().getSource();
        try {
            return source.getPlayerOrException();
        }
        catch (CommandSyntaxException ignored) {
            return null;
        }
    }

    private static void sendPrompt(ServerPlayer player, boolean registered) {
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.header").withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage((Component)Component.translatable((String)(registered ? "message.friday_cultivation.offline_auth.login_hint" : "message.friday_cultivation.offline_auth.register_hint")));
        player.sendSystemMessage((Component)Component.translatable((String)"message.friday_cultivation.offline_auth.name_warning", (Object[])new Object[]{player.getGameProfile().getName()}).withStyle(ChatFormatting.YELLOW));
    }

    private static void freeze(ServerPlayer player, PendingSession session) {
        if (player.serverLevel() != session.level) {
            return;
        }
        Vec3 pos = player.position();
        if (pos.distanceToSqr(session.position) > 1.0E-4) {
            player.teleportTo(session.position.x, session.position.y, session.position.z);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.hurtMarked = true;
    }

    private static String normalizedName(ServerPlayer player) {
        return OfflineAuthStore.normalizeName(player.getGameProfile().getName());
    }

    private record PendingSession(ServerLevel level, Vec3 position, long joinGameTime) {
        static PendingSession capture(ServerPlayer player) {
            return new PendingSession(player.serverLevel(), player.position(), player.serverLevel().getGameTime());
        }
    }
}

