package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.PlayerModelPart;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class VoidEscapeClientEffects {
    private static final long TWIST_DURATION_MS = 500L;
    private static final long ENTRY_RECORD_GRACE_MS = 700L;
    private static final float ENTRY_ALPHA = 1.0f;
    private static final float VOID_BODY_ALPHA = 1.0f;
    private static final float VOID_ARM_ALPHA = 0.38f;
    private static final Map<UUID, Long> ENTRY_START_MS = new HashMap<UUID, Long>();
    private static final Map<UUID, Boolean> WAS_ACTIVE = new HashMap<UUID, Boolean>();
    private static long localNauseaUntilMs = 0L;
    private static boolean renderingCustomPlayer = false;
    private static boolean renderingCustomArm = false;
    private static boolean localPhaseApplied = false;
    public static final IGuiOverlay STABILITY_OVERLAY = (gui, gfx, partialTick, screenW, screenH) -> VoidEscapeClientEffects.renderStability(gfx, screenW, screenH);
    private static final int BAR_W = 100;
    private static final int BAR_H = 8;
    private static final int BAR_BOTTOM_OFFSET = 75;
    private static final int INK_BLACK = -15067628;
    private static final int BG_TRACK = -869653472;
    private static final int VOID_CYAN = -9445121;
    private static final int VOID_CYAN_DEEP = -12677192;
    private static final int TEXT_LIGHT = -1641222;

    private VoidEscapeClientEffects() {
    }

    public static void startEntryEffect(int entityId) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(entityId);
        if (!(entity instanceof Player)) {
            return;
        }
        Player player = (Player) entity;
        long now = System.currentTimeMillis();
        ENTRY_START_MS.put(player.getUUID(), now);
        WAS_ACTIVE.put(player.getUUID(), true);
        if (mc.player != null && player.getUUID().equals(mc.player.getUUID())) {
            localNauseaUntilMs = now + 500L;
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            ENTRY_START_MS.clear();
            WAS_ACTIVE.clear();
            localNauseaUntilMs = 0L;
            VoidEscapeClientEffects.resetLocalPhaseSnapshot();
            return;
        }
        long now = System.currentTimeMillis();
        for (Player player : mc.level.players()) {
            UUID uuid = player.getUUID();
            boolean active = VoidEscapeClientEffects.isInVoidEscape(player);
            boolean wasActive = WAS_ACTIVE.getOrDefault(uuid, false);
            if (active && !wasActive) {
                ENTRY_START_MS.putIfAbsent(uuid, now);
                if (mc.player != null && uuid.equals(mc.player.getUUID())) {
                    localNauseaUntilMs = now + 500L;
                }
            }
            WAS_ACTIVE.put(uuid, active);
        }
        VoidEscapeClientEffects.tickLocalVoidMovement(mc);
        Iterator<Map.Entry<UUID, Long>> it = ENTRY_START_MS.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<UUID, Long> entry = it.next();
            if (now - entry.getValue() <= 700L) continue;
            it.remove();
        }
    }

    private static void tickLocalVoidMovement(Minecraft mc) {
        LocalPlayer player = mc.player;
        if (player == null) {
            VoidEscapeClientEffects.resetLocalPhaseSnapshot();
            return;
        }
        if (!VoidEscapeClientEffects.isInVoidEscape(player)) {
            VoidEscapeClientEffects.restoreLocalVoidMovement(player);
            return;
        }
        localPhaseApplied = true;
        player.noPhysics = true;
        player.setNoGravity(true);
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.xxa = 0.0f;
    }

    private static void restoreLocalVoidMovement(LocalPlayer player) {
        if (!localPhaseApplied) {
            return;
        }
        localPhaseApplied = false;
        player.xxa = 0.0f;
        if (player.isSpectator()) {
            player.noPhysics = true;
            return;
        }
        player.noPhysics = false;
        player.setNoGravity(false);
        if (player.isFallFlying()) {
            player.getAbilities().mayfly = true;
        } else if (VoidEscapeClientEffects.hasEnabledPassiveFlight(player)) {
            player.getAbilities().mayfly = true;
        } else {
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
        }
    }

    private static void resetLocalPhaseSnapshot() {
        localPhaseApplied = false;
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderPlayerPre(RenderPlayerEvent.Pre event) {
        if (renderingCustomPlayer) {
            return;
        }
        Player player = event.getEntity();
        long now = System.currentTimeMillis();
        Long explicitStart = ENTRY_START_MS.get(player.getUUID());
        boolean active = VoidEscapeClientEffects.isInVoidEscape(player);
        if (!active && explicitStart == null) {
            return;
        }
        if (explicitStart != null && (now - explicitStart) < 500L) {
            event.setCanceled(true);
            VoidEscapeClientEffects.renderTwistingPlayer(event, player, now - explicitStart);
            return;
        }
        if (!active) {
            event.setCanceled(true);
            return;
        }
        event.setCanceled(true);
        if (VoidEscapeClientEffects.isLocalPlayer(player)) {
            VoidEscapeClientEffects.renderStableVoidPlayer(event, player);
        }
    }

    private static void renderTwistingPlayer(RenderPlayerEvent.Pre event, Player player, long elapsed) {
        float t = Mth.clamp((float) elapsed / 500.0f, 0.0f, 1.0f);
        float envelope = Mth.sin((float) Math.PI * t);
        float alpha = t < 0.88f ? 1.0f : 1.0f - Mth.clamp((t - 0.88f) / 0.12f, 0.0f, 1.0f);
        if (alpha <= 0.02f) {
            return;
        }
        VoidEscapeClientEffects.renderCustomPlayer(event, player, envelope * 0.32f, (float) elapsed * 0.12f, alpha * 1.0f, false);
    }

    private static void renderStableVoidPlayer(RenderPlayerEvent.Pre event, Player player) {
        VoidEscapeClientEffects.renderCustomPlayer(event, player, 0.0f, 0.0f, 1.0f, true);
    }

    private static void renderCustomPlayer(RenderPlayerEvent.Pre event, Player player, float warpAmplitude, float warpPhase, float alpha, boolean forceInvisibleForTranslucency) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        if (!(player instanceof AbstractClientPlayer)) {
            return;
        }
        AbstractClientPlayer clientPlayer = (AbstractClientPlayer) player;
        PlayerModel model = (PlayerModel) playerRenderer.getModel();
        VoidEscapeClientEffects.configureModel(model, clientPlayer);
        float partial = event.getPartialTick();
        float yaw = Mth.rotLerp(partial, clientPlayer.yHeadRotO, clientPlayer.yHeadRot);
        boolean invisible = clientPlayer.isInvisible();
        renderingCustomPlayer = true;
        try {
            clientPlayer.setInvisible(forceInvisibleForTranslucency);
            playerRenderer.render(clientPlayer, yaw, partial, event.getPoseStack(), new VoidAlphaWarpBufferSource(event.getMultiBufferSource(), warpAmplitude, warpPhase, alpha), event.getPackedLight());
        } finally {
            clientPlayer.setInvisible(invisible);
            renderingCustomPlayer = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onRenderArm(RenderArmEvent event) {
        if (renderingCustomArm) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || event.getPlayer() != player) {
            return;
        }
        if (!VoidEscapeClientEffects.isInVoidEscape(player)) {
            return;
        }
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer(player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        event.setCanceled(true);
        boolean invisible = player.isInvisible();
        renderingCustomArm = true;
        try {
            player.setInvisible(false);
            FirstPersonVoidArmBufferSource buffers = new FirstPersonVoidArmBufferSource(event.getMultiBufferSource(), player.getSkinTextureLocation(), 0.38f);
            if (event.getArm() == HumanoidArm.RIGHT) {
                playerRenderer.renderRightHand(event.getPoseStack(), buffers, event.getPackedLight(), player);
            } else {
                playerRenderer.renderLeftHand(event.getPoseStack(), buffers, event.getPackedLight(), player);
            }
        } finally {
            player.setInvisible(invisible);
            renderingCustomArm = false;
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        float strength = VoidEscapeClientEffects.localVoidWarpStrength(event.getPartialTick());
        if (strength <= 0.0f) {
            return;
        }
        float age = (float) mc.player.tickCount + (float) event.getPartialTick();
        float waveA = Mth.sin(age * 2.65f);
        float waveB = Mth.sin(age * 4.1f + 1.7f);
        event.setRoll(event.getRoll() + waveA * strength * 17.0f);
        event.setYaw(event.getYaw() + waveB * strength * 1.65f);
        event.setPitch(event.getPitch() + Mth.cos(age * 3.25f) * strength * 1.25f);
    }

    @SubscribeEvent
    public static void onComputeFov(ViewportEvent.ComputeFov event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()) {
            return;
        }
        float strength = VoidEscapeClientEffects.localVoidWarpStrength(event.getPartialTick());
        if (strength <= 0.0f) {
            return;
        }
        float age = (float) mc.player.tickCount + (float) event.getPartialTick();
        double pulse = 1.0 + (double) (Mth.sin(age * 3.35f) * strength) * 0.055;
        event.setFOV(event.getFOV() * pulse);
    }

    private static float localVoidWarpStrength(double partialTick) {
        long now = System.currentTimeMillis();
        if (now >= localNauseaUntilMs) {
            return 0.0f;
        }
        float remaining = (float) (localNauseaUntilMs - now) / 500.0f;
        float t = 1.0f - Mth.clamp(remaining, 0.0f, 1.0f);
        return Mth.sin((float) Math.PI * t);
    }

    private static void configureModel(PlayerModel<AbstractClientPlayer> model, AbstractClientPlayer player) {
        model.setAllVisible(true);
        model.hat.visible = player.isModelPartShown(PlayerModelPart.HAT);
        model.jacket.visible = player.isModelPartShown(PlayerModelPart.JACKET);
        model.leftPants.visible = player.isModelPartShown(PlayerModelPart.LEFT_PANTS_LEG);
        model.rightPants.visible = player.isModelPartShown(PlayerModelPart.RIGHT_PANTS_LEG);
        model.leftSleeve.visible = player.isModelPartShown(PlayerModelPart.LEFT_SLEEVE);
        model.rightSleeve.visible = player.isModelPartShown(PlayerModelPart.RIGHT_SLEEVE);
        model.crouching = player.isCrouching();
        model.riding = false;
        model.young = false;
        model.swimAmount = 0.0f;
        model.leftArmPose = HumanoidModel.ArmPose.EMPTY;
        model.rightArmPose = HumanoidModel.ArmPose.EMPTY;
    }

    private static void renderStability(GuiGraphics gfx, int screenW, int screenH) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null || !data.isVoidEscapeActive()) {
            return;
        }
        int stability = data.getVoidEscapeStability();
        int max = 10;
        float progress = max <= 0 ? 0.0f : (float) stability / (float) max;
        int x = (screenW - 100) / 2;
        int y = screenH - 75;
        gfx.fill(x - 1, y - 1, x + 100 + 1, y + 8 + 1, -15067628);
        gfx.fill(x, y, x + 100, y + 8, -869653472);
        int fillW = Math.round(100.0f * progress);
        if (fillW > 0) {
            int half = fillW / 2;
            gfx.fill(x, y, x + half, y + 8, -12677192);
            gfx.fill(x + half, y, x + fillW, y + 8, -9445121);
        }
        for (int i = 1; i < max; ++i) {
            int sx = x + 100 * i / max;
            gfx.fill(sx, y, sx + 1, y + 8, -15067628);
        }
        Font font = mc.font;
        MutableComponent label = Component.translatable("hud.friday_cultivation.void_escape.stability").withStyle(ChatFormatting.AQUA);
        gfx.drawString(font, label, x, y - 11, -1641222, true);
        String num = stability + "/" + max;
        int numW = font.width(num);
        gfx.drawString(font, num, x + 100 - numW, y - 11, -1641222, true);
    }

    private static boolean isInVoidEscape(Player player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        return data != null && data.isVoidEscapeActive();
    }

    private static boolean hasEnabledPassiveFlight(Player player) {
        CultivationData data = CultivationCapability.get(player).orElse(null);
        if (data == null) {
            return false;
        }
        return data.isSoulState() && data.isSpellEnabled(Spell.GHOST_FLIGHT) || data.isSpellEnabled(Spell.QI_FLIGHT) && data.getCurrentQi() > 0L;
    }

    private static boolean isLocalPlayer(Player player) {
        Minecraft mc = Minecraft.getInstance();
        return mc.player != null && player.getUUID().equals(mc.player.getUUID());
    }

    private static final class VoidAlphaWarpBufferSource
    implements MultiBufferSource {
        private final MultiBufferSource parent;
        private final float amplitude;
        private final float phase;
        private final float alpha;

        private VoidAlphaWarpBufferSource(MultiBufferSource parent, float amplitude, float phase, float alpha) {
            this.parent = parent;
            this.amplitude = amplitude;
            this.phase = phase;
            this.alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        }

        @NotNull
        @Override
        public VertexConsumer getBuffer(@NotNull RenderType type) {
            return new VoidAlphaWarpVertexConsumer(this.parent.getBuffer(type), this.amplitude, this.phase, this.alpha);
        }
    }

    private static final class FirstPersonVoidArmBufferSource
    implements MultiBufferSource {
        private final MultiBufferSource parent;
        private final ResourceLocation skin;
        private final float alpha;

        private FirstPersonVoidArmBufferSource(MultiBufferSource parent, ResourceLocation skin, float alpha) {
            this.parent = parent;
            this.skin = skin;
            this.alpha = Mth.clamp(alpha, 0.0f, 1.0f);
        }

        @NotNull
        @Override
        public VertexConsumer getBuffer(@NotNull RenderType type) {
            return new VoidAlphaWarpVertexConsumer(this.parent.getBuffer(RenderType.entityTranslucent(this.skin)), 0.0f, 0.0f, this.alpha);
        }
    }

    private static final class VoidAlphaWarpVertexConsumer
    implements VertexConsumer {
        private final VertexConsumer parent;
        private final float amplitude;
        private final float phase;
        private final float alpha;

        private VoidAlphaWarpVertexConsumer(VertexConsumer parent, float amplitude, float phase, float alpha) {
            this.parent = parent;
            this.amplitude = amplitude;
            this.phase = phase;
            this.alpha = alpha;
        }

        @NotNull
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            double band = Math.sin(y * 18.0 + (double) this.phase) + 0.42 * Math.sin(y * 33.0 - (double) this.phase * 1.35);
            double lateral = band * (double) this.amplitude;
            double depth = Math.cos(y * 15.0 + (double) this.phase * 0.8) * (double) this.amplitude * 0.28;
            this.parent.vertex(x + lateral, y, z + depth);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            this.parent.color(r, g, b, Math.round((float) a * this.alpha));
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv(float u, float v) {
            this.parent.uv(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer overlayCoords(int u, int v) {
            this.parent.overlayCoords(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer uv2(int u, int v) {
            this.parent.uv2(u, v);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer normal(float x, float y, float z) {
            this.parent.normal(x, y, z);
            return this;
        }

        @Override
        public void endVertex() {
            this.parent.endVertex();
        }

        @Override
        public void defaultColor(int r, int g, int b, int a) {
            this.parent.defaultColor(r, g, b, a);
        }

        @Override
        public void unsetDefaultColor() {
            this.parent.unsetDefaultColor();
        }
    }
}
