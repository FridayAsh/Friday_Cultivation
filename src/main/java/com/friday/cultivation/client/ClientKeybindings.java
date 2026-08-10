/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.InputConstants$Key
 *  com.mojang.blaze3d.platform.InputConstants$Type
 *  net.minecraft.client.KeyMapping
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.Options
 *  net.minecraft.client.gui.screens.Screen
 *  net.minecraft.client.player.LocalPlayer
 *  net.minecraft.world.effect.MobEffect
 *  net.minecraft.world.effect.MobEffectInstance
 *  net.minecraft.world.entity.player.Player
 *  net.minecraftforge.api.distmarker.Dist
 *  net.minecraftforge.client.event.RegisterKeyMappingsEvent
 *  net.minecraftforge.client.settings.IKeyConflictContext
 *  net.minecraftforge.client.settings.KeyConflictContext
 *  net.minecraftforge.event.TickEvent$ClientTickEvent
 *  net.minecraftforge.event.TickEvent$Phase
 *  net.minecraftforge.eventbus.api.SubscribeEvent
 *  net.minecraftforge.fml.common.Mod$EventBusSubscriber
 *  org.lwjgl.glfw.GLFW
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.friday.cultivation.client.ChargingPreviewHandler;
import com.friday.cultivation.client.screen.CultivationScreen;
import com.friday.cultivation.client.screen.SpellWheelScreen;
import com.friday.cultivation.cultivation.CultivationCapability;
import com.friday.cultivation.cultivation.spell.Spell;
import com.friday.cultivation.network.AirJumpPacket;
import com.friday.cultivation.network.BeginChargeSpellPacket;
import com.friday.cultivation.network.CastSpellPacket;
import com.friday.cultivation.network.EndChargeSpellPacket;
import com.friday.cultivation.network.ModNetwork;
import com.friday.cultivation.network.ShadowStepPacket;
import com.friday.cultivation.registry.ModEffects;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.IKeyConflictContext;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(modid="friday_cultivation", value={Dist.CLIENT})
public final class ClientKeybindings {
    public static final String CATEGORY = "key.categories.friday_cultivation";
    public static final KeyMapping OPEN_CULTIVATION_SCREEN = new KeyMapping("key.friday_cultivation.open_screen", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 71, "key.categories.friday_cultivation");
    public static final KeyMapping OPEN_SPELL_WHEEL = new KeyMapping("key.friday_cultivation.spell_wheel", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 86, "key.categories.friday_cultivation");
    public static final KeyMapping CAST_SPELL = new KeyMapping("key.friday_cultivation.cast_spell", (IKeyConflictContext)KeyConflictContext.IN_GAME, InputConstants.Type.KEYSYM, 82, "key.categories.friday_cultivation");
    private static boolean wasCastDown = false;
    private static boolean clientCharging = false;
    private static boolean wasJumpDown = false;
    private static boolean wasOnGround = true;
    private static int clientAirJumpsUsed = 0;
    private static final long DOUBLE_TAP_WINDOW_MS = 400L;
    private static final boolean[] wasDirDown = new boolean[6];
    private static final long[] lastDirPressTime = new long[6];
    private static final int DIR_FORWARD = 0;
    private static final int DIR_BACK = 1;
    private static final int DIR_LEFT = 2;
    private static final int DIR_RIGHT = 3;
    private static final int DIR_UP = 4;
    private static final int DIR_DOWN = 5;

    private ClientKeybindings() {
    }

    public static void onRegisterKeys(RegisterKeyMappingsEvent event) {
        event.register(OPEN_CULTIVATION_SCREEN);
        event.register(OPEN_SPELL_WHEEL);
        event.register(CAST_SPELL);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        boolean isDown;
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.screen == null) {
            while (OPEN_CULTIVATION_SCREEN.consumeClick()) {
                mc.setScreen((Screen)new CultivationScreen());
            }
            while (OPEN_SPELL_WHEEL.consumeClick()) {
                mc.setScreen((Screen)new SpellWheelScreen());
            }
        }
        boolean bl = isDown = mc.screen == null && ClientKeybindings.isCastKeyHeldRaw();
        if (isDown && !wasCastDown) {
            Spell primed = ClientKeybindings.currentPrimedSpell(mc.player);
            if (primed != null && primed.chargeable()) {
                ModNetwork.CHANNEL.sendToServer((Object)new BeginChargeSpellPacket());
                clientCharging = true;
            } else {
                ModNetwork.CHANNEL.sendToServer((Object)new CastSpellPacket());
                clientCharging = false;
            }
        } else if (!isDown && wasCastDown && clientCharging) {
            ModNetwork.CHANNEL.sendToServer((Object)new EndChargeSpellPacket(ChargingPreviewHandler.currentBuddhaFireLotusTargetId()));
            clientCharging = false;
        }
        wasCastDown = isDown;
        while (CAST_SPELL.consumeClick()) {
        }
        if (mc.screen == null) {
            ClientKeybindings.tickAirJump(mc);
            ClientKeybindings.tickShadowStep(mc);
        }
    }

    private static void tickAirJump(Minecraft mc) {
        MobEffectInstance e;
        Options opts = mc.options;
        if (opts == null || mc.player == null) {
            return;
        }
        boolean jumpDown = opts.keyJump.isDown();
        boolean onGround = mc.player.onGround();
        if (onGround && !wasOnGround) {
            clientAirJumpsUsed = 0;
        }
        if (jumpDown && !wasJumpDown && !onGround && (e = mc.player.getEffect((MobEffect)ModEffects.DIVINE_STRIDE.get())) != null && e.getAmplifier() >= 3 && clientAirJumpsUsed < 3) {
            ModNetwork.CHANNEL.sendToServer((Object)new AirJumpPacket());
            ++clientAirJumpsUsed;
        }
        wasJumpDown = jumpDown;
        wasOnGround = onGround;
    }

    private static void tickShadowStep(Minecraft mc) {
        if (mc.player == null) {
            return;
        }
        if (!mc.player.hasEffect((MobEffect)ModEffects.SHADOW_STEP.get())) {
            for (int i = 0; i < 6; ++i) {
                ClientKeybindings.wasDirDown[i] = false;
                ClientKeybindings.lastDirPressTime[i] = 0L;
            }
            return;
        }
        Options opts = mc.options;
        boolean[] curDown = new boolean[]{opts.keyUp.isDown(), opts.keyDown.isDown(), opts.keyLeft.isDown(), opts.keyRight.isDown(), opts.keyJump.isDown(), opts.keyShift.isDown()};
        long now = System.currentTimeMillis();
        for (int i = 0; i < 6; ++i) {
            if (curDown[i] && !wasDirDown[i]) {
                long lastPress = lastDirPressTime[i];
                if (lastPress > 0L && now - lastPress <= 400L) {
                    ModNetwork.CHANNEL.sendToServer((Object)new ShadowStepPacket((byte)i));
                    ClientKeybindings.lastDirPressTime[i] = 0L;
                } else {
                    ClientKeybindings.lastDirPressTime[i] = now;
                }
            }
            ClientKeybindings.wasDirDown[i] = curDown[i];
        }
    }

    private static boolean isCastKeyHeldRaw() {
        InputConstants.Key key = CAST_SPELL.getKey();
        long window = Minecraft.getInstance().getWindow().getWindow();
        int code = key.getValue();
        if (code < 0) {
            return false;
        }
        return switch (key.getType()) {
            case KEYSYM -> {
                if (GLFW.glfwGetKey((long)window, (int)code) == 1) {
                    yield true;
                }
                yield false;
            }
            case MOUSE -> {
                if (GLFW.glfwGetMouseButton((long)window, (int)code) == 1) {
                    yield true;
                }
                yield false;
            }
            default -> false;
        };
    }

    private static Spell currentPrimedSpell(LocalPlayer player) {
        return CultivationCapability.get((Player)player).map(d -> {
            String sid = d.getSelectedSpellId();
            if (sid.isEmpty()) {
                return null;
            }
            return Spell.byId(sid);
        }).orElse(null);
    }
}

