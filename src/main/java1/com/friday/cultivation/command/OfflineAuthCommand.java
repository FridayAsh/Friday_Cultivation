package com.friday.cultivation.command;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.friday.cultivation.config.ModCommonConfig;
import com.friday.cultivation.event.OfflineAuthHandler;
import com.friday.cultivation.util.OfflineAuthStore;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 离线认证命令 - 完全照搬原 mod: xiaoxiang.cultivation.command.OfflineAuthCommand（217 行完整版）。
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class OfflineAuthCommand {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private OfflineAuthCommand() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        OfflineAuthCommand.registerAliases(event, OfflineAuthCommand::registerCommand, "register", "註冊", "注册");
        OfflineAuthCommand.registerAliases(event, OfflineAuthCommand::loginCommand, "login", "登入", "登录");
        event.getDispatcher().register(Commands.literal("changepw").requires(source -> source.hasPermission(0))
                .then(Commands.argument("old_password", StringArgumentType.word())
                        .then(Commands.argument("new_password", StringArgumentType.word())
                                .executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register(Commands.literal("改密碼").requires(source -> source.hasPermission(0))
                .then(Commands.argument("old_password", StringArgumentType.word())
                        .then(Commands.argument("new_password", StringArgumentType.word())
                                .executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register(Commands.literal("改密码").requires(source -> source.hasPermission(0))
                .then(Commands.argument("old_password", StringArgumentType.word())
                        .then(Commands.argument("new_password", StringArgumentType.word())
                                .executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register(Commands.literal("xxauth").requires(source -> source.hasPermission(2))
                .then(Commands.literal("reload").executes(OfflineAuthCommand::reload))
                .then(Commands.literal("status").then(Commands.argument("player", StringArgumentType.word())
                        .executes(OfflineAuthCommand::status)))
                .then(Commands.literal("reset").then(Commands.argument("player", StringArgumentType.word())
                        .executes(OfflineAuthCommand::reset))));
    }

    private static void registerAliases(RegisterCommandsEvent event, PasswordCommand handler, String... roots) {
        for (String root : roots) {
            LiteralArgumentBuilder<CommandSourceStack> command = Commands.literal(root).requires(source -> source.hasPermission(0))
                    .then(Commands.argument("password", StringArgumentType.word())
                            .executes(ctx -> handler.run(ctx, null))
                            .then(Commands.argument("confirm", StringArgumentType.word())
                                    .executes(ctx -> handler.run(ctx, StringArgumentType.getString(ctx, "confirm")))));
            event.getDispatcher().register(command);
        }
    }

    private static int registerCommand(CommandContext<CommandSourceStack> ctx, String confirm) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String password = StringArgumentType.getString(ctx, "password");
            if (!OfflineAuthCommand.validatePassword(ctx, password)) {
                return 0;
            }
            if (confirm != null && !password.equals(confirm)) {
                ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.password_mismatch"));
                return 0;
            }
            OfflineAuthStore.RegisterResult result = OfflineAuthStore.register(player.getGameProfile().getName(), password);
            switch (result) {
                case SUCCESS: {
                    OfflineAuthHandler.markAuthenticated(player);
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.register_success"), false);
                    return 1;
                }
                case ALREADY_REGISTERED: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.already_registered"));
                    break;
                }
                case STORE_ERROR: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int loginCommand(CommandContext<CommandSourceStack> ctx, String ignoredConfirm) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String password = StringArgumentType.getString(ctx, "password");
            OfflineAuthStore.LoginResult result = OfflineAuthStore.verify(player.getGameProfile().getName(), password);
            switch (result) {
                case SUCCESS: {
                    OfflineAuthHandler.markAuthenticated(player);
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.login_success"), false);
                    return 1;
                }
                case NOT_REGISTERED: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.not_registered"));
                    break;
                }
                case BAD_PASSWORD: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.bad_password"));
                    break;
                }
                case STORE_ERROR: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int changePassword(CommandContext<CommandSourceStack> ctx) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            String oldPassword = StringArgumentType.getString(ctx, "old_password");
            String newPassword = StringArgumentType.getString(ctx, "new_password");
            if (!OfflineAuthCommand.validatePassword(ctx, newPassword)) {
                return 0;
            }
            OfflineAuthStore.ChangePasswordResult result = OfflineAuthStore.changePassword(player.getGameProfile().getName(), oldPassword, newPassword);
            switch (result) {
                case SUCCESS: {
                    ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.change_success"), false);
                    return 1;
                }
                case NOT_REGISTERED: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.not_registered"));
                    break;
                }
                case BAD_PASSWORD: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.bad_password"));
                    break;
                }
                case STORE_ERROR: {
                    ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        } catch (Exception e) {
            ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        OfflineAuthStore.reload();
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.reload_success", OfflineAuthStore.registeredCount()), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        boolean registered = OfflineAuthStore.isRegistered(playerName);
        boolean onlineAuthenticated = OfflineAuthHandler.isOnlineAuthenticated(playerName);
        ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.status", playerName,
                Component.translatable(registered ? "commands.friday_cultivation.offline_auth.boolean_yes" : "commands.friday_cultivation.offline_auth.boolean_no"),
                Component.translatable(onlineAuthenticated ? "commands.friday_cultivation.offline_auth.boolean_yes" : "commands.friday_cultivation.offline_auth.boolean_no")), false);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, "player");
        boolean removed = OfflineAuthStore.remove(playerName);
        if (removed) {
            OfflineAuthHandler.forgetAuthenticatedName(playerName);
            ctx.getSource().sendSuccess(() -> Component.translatable("commands.friday_cultivation.offline_auth.reset_success", playerName), true);
            return 1;
        }
        ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.reset_missing", playerName));
        return 0;
    }

    private static boolean ensureEnabled(CommandContext<CommandSourceStack> ctx) {
        if (ModCommonConfig.offlineAuthEnabled()) {
            return true;
        }
        ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.disabled"));
        return false;
    }

    private static boolean validatePassword(CommandContext<CommandSourceStack> ctx, String password) {
        if (password.length() < MIN_PASSWORD_LENGTH) {
            ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.password_too_short", MIN_PASSWORD_LENGTH));
            return false;
        }
        if (password.length() > MAX_PASSWORD_LENGTH) {
            ctx.getSource().sendFailure(Component.translatable("commands.friday_cultivation.offline_auth.password_too_long", MAX_PASSWORD_LENGTH));
            return false;
        }
        return true;
    }

    @FunctionalInterface
    private static interface PasswordCommand {
        int run(CommandContext<CommandSourceStack> var1, String var2);
    }
}
