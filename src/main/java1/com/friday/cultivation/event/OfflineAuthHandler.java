package com.friday.cultivation.event;

import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.util.OfflineAuthStore;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 离线登录验证事件处理器（严格照搬原模组 com.xiaoxiang.cultivation.event.OfflineAuthHandler）。
 * <p>玩家上线时若启用，先冻在原地只能执行 {@code register / login} 命令。通过验证后
 * {@link #markAuthenticated} 会清空状态并打开初始命格 UI。</p>
 *
 * <p>说明：1.20.1 Forge 中 {@code EntityItemPickupEvent.getEntity()} 类型与原 mod 不同
 * （返回 {@code ItemEntity} 而非 {@code Player}），该订阅项暂时省略；其余原 mod 中的
 * 订阅与静态 API 完全保留。</p>
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class OfflineAuthHandler {
    private static final Set<String> AUTH_COMMANDS = Set.of(
            "register", "login",
            "註冊", "注册", "登入", "登录");
    private static final Map<UUID, PendingSession> PENDING = new ConcurrentHashMap<>();
    private static final Map<UUID, String> AUTHENTICATED_NAMES = new ConcurrentHashMap<>();

    private OfflineAuthHandler() {
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!enabled()) {
            return;
        }
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        AUTHENTICATED_NAMES.remove(sp.getUUID());
        PENDING.put(sp.getUUID(), PendingSession.capture(sp));
        sendPrompt(sp, OfflineAuthStore.isRegistered(normalizedName(sp)));
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        Player p = event.getEntity();
        PENDING.remove(p.getUUID());
        AUTHENTICATED_NAMES.remove(p.getUUID());
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onCommand(CommandEvent event) {
        if (!enabled()) {
            return;
        }
        ServerPlayer player = sourcePlayer(event);
        if (player == null || isAuthenticated(player)) {
            return;
        }
        String input = event.getParseResults().getReader().getString().trim();
        String root = input.split("\\s+", 2)[0].toLowerCase(Locale.ROOT);
        if (AUTH_COMMANDS.contains(root)) {
            return;
        }
        event.setCanceled(true);
        CommandSourceStack src = (CommandSourceStack) event.getParseResults().getContext().getSource();
        src.sendSystemMessage(Component.translatable("message.friday_cultivation.offline_auth.only_auth_commands"));
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onServerChat(ServerChatEvent event) {
        if (!enabled() || isAuthenticated(event.getPlayer())) {
            return;
        }
        event.setCanceled(true);
        event.getPlayer().sendSystemMessage(
                Component.translatable("message.friday_cultivation.offline_auth.chat_blocked"));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (!enabled() || event.phase != TickEvent.Phase.END) {
            return;
        }
        Player player = event.player;
        if (!(player instanceof ServerPlayer sp)) {
            return;
        }
        PendingSession session = PENDING.get(sp.getUUID());
        if (session == null) {
            return;
        }
        freeze(sp, session);
        long ageTicks = sp.level().getGameTime() - session.joinGameTime;
        if (ageTicks % 100L == 0L) {
            sp.displayClientMessage(Component.translatable("message.friday_cultivation.offline_auth.actionbar"), true);
        }
        int timeoutSeconds = ModCommonConfig.offlineAuthLoginTimeoutSeconds();
        if (timeoutSeconds > 0 && ageTicks >= (long) timeoutSeconds * 20L) {
            sp.connection.disconnect(
                    Component.translatable("message.friday_cultivation.offline_auth.timeout_kick"));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onAttackEntity(AttackEntityEvent event) {
        if (requiresAuth(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        Object direct = event.getSource().getDirectEntity();
        Player targetP = target instanceof Player ? (Player) target : null;
        Player directP = direct instanceof Player ? (Player) direct : null;
        if (requiresAuth(targetP) || requiresAuth(directP)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (requiresAuth(event.getEntity())) {
            event.setNewSpeed(0.0f);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (requiresAuth(event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRightClickItem(PlayerInteractEvent.RightClickItem event) {
        if (requiresAuth(event.getEntity())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onLeftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (requiresAuth(event.getEntity())) {
            event.setUseBlock(Event.Result.DENY);
            event.setUseItem(Event.Result.DENY);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if (requiresAuth(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if (requiresAuth(event.getEntity())) {
            event.setCancellationResult(InteractionResult.FAIL);
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (requiresAuth(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onBlockPlace(BlockEvent.EntityPlaceEvent event) {
        Object ent = event.getEntity();
        Player p = ent instanceof Player ? (Player) ent : null;
        if (requiresAuth(p)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onItemToss(ItemTossEvent event) {
        if (requiresAuth(event.getPlayer())) {
            event.setCanceled(true);
        }
    }

    public static boolean isAuthenticated(ServerPlayer player) {
        if (!enabled()) {
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
        AUTHENTICATED_NAMES.entrySet().removeIf(entry -> entry.getValue().equals(normalized));
    }

    public static void markAuthenticated(ServerPlayer player) {
        PENDING.remove(player.getUUID());
        AUTHENTICATED_NAMES.put(player.getUUID(), normalizedName(player));
        player.displayClientMessage(
                Component.translatable("message.friday_cultivation.offline_auth.unlocked"), true);
        IdentityDrawHandler.tryOpenInitialOriginAfterAuth(player);
    }

    private static boolean enabled() {
        return ModCommonConfig.offlineAuthEnabled();
    }

    private static boolean requiresAuth(Player player) {
        return enabled() && player instanceof ServerPlayer sp && !isAuthenticated(sp);
    }

    private static ServerPlayer sourcePlayer(CommandEvent event) {
        CommandSourceStack source = (CommandSourceStack) event.getParseResults().getContext().getSource();
        try {
            return source.getPlayer();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static void sendPrompt(ServerPlayer player, boolean registered) {
        player.sendSystemMessage(
                Component.translatable("message.friday_cultivation.offline_auth.header")
                        .withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable(
                registered
                        ? "message.friday_cultivation.offline_auth.login_hint"
                        : "message.friday_cultivation.offline_auth.register_hint"));
        player.sendSystemMessage(Component.translatable(
                "message.friday_cultivation.offline_auth.name_warning",
                player.getGameProfile().getName())
                .withStyle(ChatFormatting.YELLOW));
    }

    private static void freeze(ServerPlayer player, PendingSession session) {
        if (player.level() != session.level) {
            return;
        }
        Vec3 pos = player.position();
        if (pos.distanceToSqr(session.position) > 1.0E-4) {
            player.teleportTo(session.position.x, session.position.y, session.position.z);
        }
        player.setDeltaMovement(Vec3.ZERO);
        player.setNoGravity(true);
    }

    private static String normalizedName(ServerPlayer player) {
        return OfflineAuthStore.normalizeName(player.getGameProfile().getName());
    }

    /**
     * 验证中的回话状态：玩家位置 + 加入游戏的时间 + 当前 level 引用。
     */
    public static final class PendingSession {
        private final Vec3 position;
        private final Level level;
        private final long joinGameTime;

        private PendingSession(Vec3 position, Level level, long joinGameTime) {
            this.position = position;
            this.level = level;
            this.joinGameTime = joinGameTime;
        }

        public static PendingSession capture(ServerPlayer player) {
            return new PendingSession(player.position(), player.level(), player.level().getGameTime());
        }
    }
}
