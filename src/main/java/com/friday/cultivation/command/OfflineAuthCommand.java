/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.brigadier.arguments.ArgumentType
 *  com.mojang.brigadier.arguments.StringArgumentType
 *  com.mojang.brigadier.builder.LiteralArgumentBuilder
 *  com.mojang.brigadier.builder.RequiredArgumentBuilder
 *  com.mojang.brigadier.context.CommandContext
 *  net.minecraft.commands.CommandSourceStack
 *  net.minecraft.commands.Commands
 *  net.minecraft.network.chat.Component
 *  net.minecraft.server.level.ServerPlayer
 *  net.minecraftforge.event.RegisterCommandsEvent
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 */
package com.friday.cultivation.command;

import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
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

@Mod.EventBusSubscriber(modid="friday_cultivation")
public final class OfflineAuthCommand {
    private static final int MIN_PASSWORD_LENGTH = 4;
    private static final int MAX_PASSWORD_LENGTH = 64;

    private OfflineAuthCommand() {
    }

    @SubscribeEvent
    public static void onRegister(RegisterCommandsEvent event) {
        OfflineAuthCommand.registerAliases(event, OfflineAuthCommand::registerCommand, "register", "\u8a3b\u518a", "\u6ce8\u518c");
        OfflineAuthCommand.registerAliases(event, OfflineAuthCommand::loginCommand, "login", "\u767b\u5165", "\u767b\u5f55");
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"changepw").requires(source -> source.hasPermission(0))).then(Commands.argument((String)"old_password", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"new_password", (ArgumentType)StringArgumentType.word()).executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"\u6539\u5bc6\u78bc").requires(source -> source.hasPermission(0))).then(Commands.argument((String)"old_password", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"new_password", (ArgumentType)StringArgumentType.word()).executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"\u6539\u5bc6\u7801").requires(source -> source.hasPermission(0))).then(Commands.argument((String)"old_password", (ArgumentType)StringArgumentType.word()).then(Commands.argument((String)"new_password", (ArgumentType)StringArgumentType.word()).executes(OfflineAuthCommand::changePassword))));
        event.getDispatcher().register((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)"xxauth").requires(source -> source.hasPermission(2))).then(Commands.literal((String)"reload").executes(OfflineAuthCommand::reload))).then(Commands.literal((String)"status").then(Commands.argument((String)"player", (ArgumentType)StringArgumentType.word()).executes(OfflineAuthCommand::status)))).then(Commands.literal((String)"reset").then(Commands.argument((String)"player", (ArgumentType)StringArgumentType.word()).executes(OfflineAuthCommand::reset))));
    }

    private static void registerAliases(RegisterCommandsEvent event, PasswordCommand handler, String ... roots) {
        for (String root : roots) {
            LiteralArgumentBuilder command = (LiteralArgumentBuilder)((LiteralArgumentBuilder)Commands.literal((String)root).requires(source -> source.hasPermission(0))).then(((RequiredArgumentBuilder)Commands.argument((String)"password", (ArgumentType)StringArgumentType.word()).executes(ctx -> handler.run((CommandContext<CommandSourceStack>)ctx, null))).then(Commands.argument((String)"confirm", (ArgumentType)StringArgumentType.word()).executes(ctx -> handler.run((CommandContext<CommandSourceStack>)ctx, StringArgumentType.getString((CommandContext)ctx, (String)"confirm")))));
            event.getDispatcher().register(command);
        }
    }

    private static int registerCommand(CommandContext<CommandSourceStack> ctx, String confirm) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            String password = StringArgumentType.getString(ctx, (String)"password");
            if (!OfflineAuthCommand.validatePassword(ctx, password)) {
                return 0;
            }
            if (confirm != null && !password.equals(confirm)) {
                ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.password_mismatch"));
                return 0;
            }
            OfflineAuthStore.RegisterResult result = OfflineAuthStore.register(player.getGameProfile().getName(), password);
            switch (result) {
                case SUCCESS: {
                    OfflineAuthHandler.markAuthenticated(player);
                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.register_success"), false);
                    return 1;
                }
                case ALREADY_REGISTERED: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.already_registered"));
                    break;
                }
                case STORE_ERROR: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int loginCommand(CommandContext<CommandSourceStack> ctx, String ignoredConfirm) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            String password = StringArgumentType.getString(ctx, (String)"password");
            OfflineAuthStore.LoginResult result = OfflineAuthStore.verify(player.getGameProfile().getName(), password);
            switch (result) {
                case SUCCESS: {
                    OfflineAuthHandler.markAuthenticated(player);
                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.login_success"), false);
                    return 1;
                }
                case NOT_REGISTERED: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.not_registered"));
                    break;
                }
                case BAD_PASSWORD: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.bad_password"));
                    break;
                }
                case STORE_ERROR: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int changePassword(CommandContext<CommandSourceStack> ctx) {
        if (!OfflineAuthCommand.ensureEnabled(ctx)) {
            return 0;
        }
        try {
            ServerPlayer player = ((CommandSourceStack)ctx.getSource()).getPlayerOrException();
            String oldPassword = StringArgumentType.getString(ctx, (String)"old_password");
            String newPassword = StringArgumentType.getString(ctx, (String)"new_password");
            if (!OfflineAuthCommand.validatePassword(ctx, newPassword)) {
                return 0;
            }
            OfflineAuthStore.ChangePasswordResult result = OfflineAuthStore.changePassword(player.getGameProfile().getName(), oldPassword, newPassword);
            switch (result) {
                case SUCCESS: {
                    ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.change_success"), false);
                    return 1;
                }
                case NOT_REGISTERED: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.not_registered"));
                    break;
                }
                case BAD_PASSWORD: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.bad_password"));
                    break;
                }
                case STORE_ERROR: {
                    ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.store_error"));
                }
            }
        }
        catch (Exception e) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.player_only"));
        }
        return 0;
    }

    private static int reload(CommandContext<CommandSourceStack> ctx) {
        OfflineAuthStore.reload();
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.reload_success", (Object[])new Object[]{OfflineAuthStore.registeredCount()}), true);
        return 1;
    }

    private static int status(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, (String)"player");
        boolean registered = OfflineAuthStore.isRegistered(playerName);
        boolean onlineAuthenticated = OfflineAuthHandler.isOnlineAuthenticated(playerName);
        ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.status", (Object[])new Object[]{playerName, Component.translatable((String)(registered ? "commands.friday_cultivation.offline_auth.boolean_yes" : "commands.friday_cultivation.offline_auth.boolean_no")), Component.translatable((String)(onlineAuthenticated ? "commands.friday_cultivation.offline_auth.boolean_yes" : "commands.friday_cultivation.offline_auth.boolean_no"))}), false);
        return 1;
    }

    private static int reset(CommandContext<CommandSourceStack> ctx) {
        String playerName = StringArgumentType.getString(ctx, (String)"player");
        boolean removed = OfflineAuthStore.remove(playerName);
        if (removed) {
            OfflineAuthHandler.forgetAuthenticatedName(playerName);
            ((CommandSourceStack)ctx.getSource()).sendSuccess(() -> Component.translatable((String)"commands.friday_cultivation.offline_auth.reset_success", (Object[])new Object[]{playerName}), true);
            return 1;
        }
        ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.reset_missing", (Object[])new Object[]{playerName}));
        return 0;
    }

    private static boolean ensureEnabled(CommandContext<CommandSourceStack> ctx) {
        if (ModCommonConfig.offlineAuthEnabled()) {
            return true;
        }
        ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.disabled"));
        return false;
    }

    private static boolean validatePassword(CommandContext<CommandSourceStack> ctx, String password) {
        if (password.length() < 4) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.password_too_short", (Object[])new Object[]{4}));
            return false;
        }
        if (password.length() > 64) {
            ((CommandSourceStack)ctx.getSource()).sendFailure((Component)Component.translatable((String)"commands.friday_cultivation.offline_auth.password_too_long", (Object[])new Object[]{64}));
            return false;
        }
        return true;
    }

    @FunctionalInterface
    private static interface PasswordCommand {
        public int run(CommandContext<CommandSourceStack> var1, String var2);
    }
}

