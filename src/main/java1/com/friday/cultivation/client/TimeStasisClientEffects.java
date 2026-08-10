package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.friday.cultivation.entity.SkyTrailEntity;
import com.friday.cultivation.entity.spell.GreatFireballEntity;
import com.friday.cultivation.entity.spell.HeavenPiercingConeEntity;
import com.friday.cultivation.entity.spell.MeteorEntity;
import com.friday.cultivation.entity.spell.MushroomCloudEntity;
import com.friday.cultivation.entity.spell.QiOrbEntity;
import com.friday.cultivation.entity.spell.ShockwaveEntity;
import com.friday.cultivation.entity.spell.SkySplittingSwordAuraEntity;
import com.friday.cultivation.entity.spell.SwordAuraEntity;
import com.friday.cultivation.entity.spell.SwordProjectileEntity;
import com.friday.cultivation.entity.spell.XiaoxiangFireballEntity;
import com.friday.cultivation.registry.ModEffects;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.gui.overlay.IGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;

@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT)
public final class TimeStasisClientEffects {
    public static final IGuiOverlay OVERLAY = (gui, graphics, partialTick, screenWidth, screenHeight) -> TimeStasisClientEffects.renderOverlay(graphics, screenWidth, screenHeight);
    private static final List<ActiveDomain> DOMAINS = new ArrayList<ActiveDomain>();
    private static final Map<Integer, Long> SINGLE_TARGETS = new ConcurrentHashMap<Integer, Long>();
    private static final Map<Integer, ClientFrozenSnapshot> CLIENT_FROZEN = new ConcurrentHashMap<Integer, ClientFrozenSnapshot>();
    private static final ResourceLocation DESATURATE_SHADER = new ResourceLocation("minecraft", "shaders/post/desaturate.json");
    private static final long EXPANSION_MS = 650L;
    private static boolean tintingEntity = false;
    private static boolean desaturateShaderActive = false;

    private TimeStasisClientEffects() {
    }

    public static void onDomain(double x, double y, double z, double radius, int durationTicks, int casterEntityId) {
        if (durationTicks <= 0) {
            DOMAINS.removeIf(domain -> domain.casterEntityId() == casterEntityId);
            return;
        }
        long now = System.currentTimeMillis();
        DOMAINS.add(new ActiveDomain(new Vec3(x, y, z), radius, now, now + (long)durationTicks * 50L, casterEntityId));
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level != null && player != null && player.position().distanceToSqr(x, y, z) <= (radius + 16.0) * (radius + 16.0)) {
            mc.level.playLocalSound(x, y, z, SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 0.9f, 0.55f, false);
        }
    }

    public static void onTargetStasis(int entityId, int durationTicks, boolean frozen) {
        if (frozen) {
            SINGLE_TARGETS.put(entityId, System.currentTimeMillis() + (long)Math.max(1, durationTicks) * 50L);
        } else {
            SINGLE_TARGETS.remove(entityId);
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            DOMAINS.clear();
            SINGLE_TARGETS.clear();
            CLIENT_FROZEN.clear();
            TimeStasisClientEffects.disableDesaturateShader(mc);
            return;
        }
        long now = System.currentTimeMillis();
        DOMAINS.removeIf(domain -> now >= domain.endMs());
        SINGLE_TARGETS.entrySet().removeIf(entry -> now >= entry.getValue());
        TimeStasisClientEffects.applyClientEntityFreeze(mc);
        CLIENT_FROZEN.entrySet().removeIf(entry -> mc.level.getEntity(entry.getKey()) == null);
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        TimeStasisClientEffects.updateDesaturateShader(mc);
        if (TimeStasisClientEffects.shouldBlockLocalPlayer(mc.player)) {
            mc.player.input.leftImpulse = 0.0f;
            mc.player.input.forwardImpulse = 0.0f;
            mc.player.input.up = false;
            mc.player.input.down = false;
            mc.player.setDeltaMovement(Vec3.ZERO);
        }
    }

    @SubscribeEvent
    public static void onRenderLivingPre(RenderLivingEvent.Pre<?, ?> event) {
        if (!TimeStasisClientEffects.shouldTint(event.getEntity())) {
            return;
        }
        RenderSystem.setShaderColor(0.62f, 0.62f, 0.62f, 1.0f);
        tintingEntity = true;
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        if (!tintingEntity || !TimeStasisClientEffects.shouldTint(event.getEntity())) {
            return;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        tintingEntity = false;
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        if (DOMAINS.isEmpty()) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null || mc.player == null) {
            return;
        }
        long now = System.currentTimeMillis();
        Vec3 cam = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        pose.pushPose();
        pose.translate(-cam.x, -cam.y, -cam.z);
        Matrix4f mat = pose.last().pose();
        for (ActiveDomain domain : DOMAINS) {
            if (mc.player.position().distanceToSqr(domain.center()) > (domain.radius() + 96.0) * (domain.radius() + 96.0)) continue;
            double expand = TimeStasisClientEffects.domainExpansion(domain, now);
            double radius = domain.radius() * expand;
            float alpha = (float)(0.16 + 0.08 * expand);
            TimeStasisClientEffects.drawDomainSphere(tesselator, buffer, mat, domain.center(), radius, alpha);
        }
        RenderSystem.lineWidth(2.5f);
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        for (ActiveDomain domain : DOMAINS) {
            if (mc.player.position().distanceToSqr(domain.center()) > (domain.radius() + 96.0) * (domain.radius() + 96.0)) continue;
            double expand = TimeStasisClientEffects.domainExpansion(domain, now);
            double radius = domain.radius() * expand;
            float alpha = (float)(0.45 + 0.12 * (1.0 - expand));
            TimeStasisClientEffects.drawDomainRings(buffer, mat, domain.center(), radius, alpha);
        }
        tesselator.end();
        pose.popPose();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.enableCull();
        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }

    private static void renderOverlay(GuiGraphics graphics, int screenWidth, int screenHeight) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.options.hideGui) {
            return;
        }
        boolean canAct = TimeStasisClientEffects.canActInStoppedTime(player);
        boolean insideDomain = TimeStasisClientEffects.isInsideExpandedDomain(player.position());
        boolean frozen = !canAct && (TimeStasisClientEffects.isSingleTargetFrozen(player.getId()) || insideDomain);
        if (!frozen && !insideDomain) {
            return;
        }
        float alpha = frozen ? 0.42f : 0.16f;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        graphics.fill(0, 0, screenWidth, screenHeight, a << 24 | 0xB8BCC4);
        TimeStasisClientEffects.renderDomainCountdown(graphics, player, screenWidth);
    }

    private static boolean shouldTint(LivingEntity entity) {
        return !TimeStasisClientEffects.canActInStoppedTime(entity) && (TimeStasisClientEffects.isSingleTargetFrozen(entity.getId()) || TimeStasisClientEffects.isInsideExpandedDomain(entity.position()));
    }

    private static boolean isLocalPlayerVisuallyFrozen(LocalPlayer player) {
        return !TimeStasisClientEffects.canActInStoppedTime(player) && (TimeStasisClientEffects.isSingleTargetFrozen(player.getId()) || TimeStasisClientEffects.isInsideExpandedDomain(player.position()));
    }

    private static boolean shouldBlockLocalPlayer(LocalPlayer player) {
        return !TimeStasisClientEffects.canActInStoppedTime(player) && (TimeStasisClientEffects.isSingleTargetFrozen(player.getId()) || TimeStasisClientEffects.isInsideAnyDomain(player.position()));
    }

    private static boolean canActInStoppedTime(Entity entity) {
        if (entity instanceof LocalPlayer && ((LocalPlayer)entity).isSpectator()) {
            return true;
        }
        if (TimeStasisClientEffects.isDomainCaster(entity.getId())) {
            return true;
        }
        return entity instanceof LivingEntity && ((LivingEntity)entity).hasEffect(ModEffects.TIME_STASIS_FLOW.get());
    }

    private static boolean isDomainCaster(int entityId) {
        long now = System.currentTimeMillis();
        for (ActiveDomain domain : DOMAINS) {
            if (now >= domain.endMs() || domain.casterEntityId() != entityId) continue;
            return true;
        }
        return false;
    }

    private static boolean isSingleTargetFrozen(int entityId) {
        Long endMs = SINGLE_TARGETS.get(entityId);
        if (endMs == null) {
            return false;
        }
        if (System.currentTimeMillis() < endMs) {
            return true;
        }
        SINGLE_TARGETS.remove(entityId);
        return false;
    }

    private static boolean isInsideExpandedDomain(Vec3 pos) {
        long now = System.currentTimeMillis();
        for (ActiveDomain domain : DOMAINS) {
            if (now >= domain.endMs()) continue;
            double expansion = TimeStasisClientEffects.domainExpansion(domain, now);
            double radius = domain.radius() * expansion;
            if (!(pos.distanceToSqr(domain.center()) <= radius * radius)) continue;
            return true;
        }
        return false;
    }

    private static boolean isInsideAnyDomain(Vec3 pos) {
        long now = System.currentTimeMillis();
        for (ActiveDomain domain : DOMAINS) {
            if (now >= domain.endMs() || !(pos.distanceToSqr(domain.center()) <= domain.radius() * domain.radius())) continue;
            return true;
        }
        return false;
    }

    private static void updateDesaturateShader(Minecraft mc) {
        boolean shouldUseShader = mc.player != null && (TimeStasisClientEffects.isLocalPlayerVisuallyFrozen(mc.player) || TimeStasisClientEffects.isInsideExpandedDomain(mc.player.position()));
        if (shouldUseShader && !desaturateShaderActive) {
            mc.gameRenderer.loadEffect(DESATURATE_SHADER);
            desaturateShaderActive = true;
        } else if (!shouldUseShader && desaturateShaderActive) {
            TimeStasisClientEffects.disableDesaturateShader(mc);
        }
    }

    private static void disableDesaturateShader(Minecraft mc) {
        if (!desaturateShaderActive) {
            return;
        }
        mc.gameRenderer.shutdownEffect();
        desaturateShaderActive = false;
    }

    private static double domainExpansion(ActiveDomain domain, long now) {
        return Math.min(1.0, Math.max(0.05, (double)(now - domain.startMs()) / 650.0));
    }

    private static void applyClientEntityFreeze(Minecraft mc) {
        if (mc.level == null) {
            return;
        }
        for (Entity entity : mc.level.entitiesForRendering()) {
            if (TimeStasisClientEffects.shouldFreezeClientEntity(entity)) {
                TimeStasisClientEffects.freezeClientEntity(entity);
                continue;
            }
            TimeStasisClientEffects.thawClientEntity(entity);
        }
    }

    private static boolean shouldFreezeClientEntity(Entity entity) {
        if (entity == null || entity.isRemoved() || TimeStasisClientEffects.canActInStoppedTime(entity)) {
            return false;
        }
        return TimeStasisClientEffects.isSingleTargetFrozen(entity.getId()) || TimeStasisClientEffects.isInsideAnyDomain(entity.position());
    }

    private static void freezeClientEntity(Entity entity) {
        ClientFrozenSnapshot snapshot = CLIENT_FROZEN.computeIfAbsent(entity.getId(), ignored -> ClientFrozenSnapshot.capture(entity));
        entity.setDeltaMovement(TimeStasisClientEffects.shouldPreserveClientFrozenVelocity(entity) ? snapshot.deltaMovement() : Vec3.ZERO);
        entity.tickCount = snapshot.tickCount();
        entity.moveTo(snapshot.position().x, snapshot.position().y, snapshot.position().z);
        entity.xo = snapshot.position().x;
        entity.yo = snapshot.position().y;
        entity.zo = snapshot.position().z;
        entity.setYRot(snapshot.yRot());
        entity.setXRot(snapshot.xRot());
        entity.hasImpulse = true;
    }

    private static void thawClientEntity(Entity entity) {
        ClientFrozenSnapshot snapshot = CLIENT_FROZEN.remove(entity.getId());
        if (snapshot == null) {
            return;
        }
        entity.setDeltaMovement(entity instanceof LocalPlayer ? Vec3.ZERO : snapshot.deltaMovement());
        entity.hasImpulse = true;
    }

    private static boolean shouldPreserveClientFrozenVelocity(Entity entity) {
        return entity instanceof SwordAuraEntity || entity instanceof SwordProjectileEntity || entity instanceof SkySplittingSwordAuraEntity || entity instanceof MeteorEntity || entity instanceof HeavenPiercingConeEntity || entity instanceof GreatFireballEntity || entity instanceof XiaoxiangFireballEntity || entity instanceof ShockwaveEntity || entity instanceof MushroomCloudEntity || entity instanceof SkyTrailEntity || entity instanceof QiOrbEntity;
    }

    private static void renderDomainCountdown(GuiGraphics graphics, LocalPlayer player, int screenWidth) {
        ActiveDomain domain = TimeStasisClientEffects.activeDomainAt(player.position());
        if (domain == null) {
            return;
        }
        double remainingSeconds = Math.max(0.0, (double)(domain.endMs() - System.currentTimeMillis()) / 1000.0);
        MutableComponent text = Component.translatable("hud.friday_cultivation.time_stasis.remaining", String.format(Locale.ROOT, "%.1f", remainingSeconds));
        Minecraft mc = Minecraft.getInstance();
        int width = mc.font.width(text);
        graphics.drawString(mc.font, text, (screenWidth - width) / 2, 28, 14543103, true);
    }

    private static ActiveDomain activeDomainAt(Vec3 pos) {
        long now = System.currentTimeMillis();
        ActiveDomain best = null;
        for (ActiveDomain domain : DOMAINS) {
            if (now >= domain.endMs()) continue;
            double radius = domain.radius() * TimeStasisClientEffects.domainExpansion(domain, now);
            if (pos.distanceToSqr(domain.center()) > radius * radius || best != null && domain.endMs() >= best.endMs()) continue;
            best = domain;
        }
        return best;
    }

    private static void drawDomainRings(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, float alpha) {
        int color = 12106948;
        TimeStasisClientEffects.addCircle(buffer, mat, center, radius, 0, alpha, color);
        TimeStasisClientEffects.addCircle(buffer, mat, center, radius, 1, alpha * 0.75f, color);
        TimeStasisClientEffects.addCircle(buffer, mat, center, radius, 2, alpha * 0.75f, color);
    }

    private static void drawDomainSphere(Tesselator tesselator, BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, float alpha) {
        int latitudes = 18;
        int longitudes = 48;
        int color = 1514018;
        for (int lat = 0; lat < latitudes; ++lat) {
            double theta0 = Math.PI * (double)lat / (double)latitudes;
            double theta1 = Math.PI * (double)(lat + 1) / (double)latitudes;
            buffer.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);
            for (int lon = 0; lon <= longitudes; ++lon) {
                double phi = Math.PI * 2 * (double)lon / (double)longitudes;
                TimeStasisClientEffects.addSphereVertex(buffer, mat, center, radius, theta0, phi, color, alpha);
                TimeStasisClientEffects.addSphereVertex(buffer, mat, center, radius, theta1, phi, color, alpha);
            }
            tesselator.end();
        }
    }

    private static void addSphereVertex(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, double theta, double phi, int color, float alpha) {
        double sin = Math.sin(theta);
        double x = center.x + radius * sin * Math.cos(phi);
        double y = center.y + radius * Math.cos(theta);
        double z = center.z + radius * sin * Math.sin(phi);
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        buffer.vertex(mat, (float)x, (float)y, (float)z).color(r, g, b, a).endVertex();
    }

    private static void addCircle(BufferBuilder buffer, Matrix4f mat, Vec3 center, double radius, int plane, float alpha, int color) {
        int segments = 96;
        for (int i = 0; i < segments; ++i) {
            double a0 = (double)i * Math.PI * 2.0 / (double)segments;
            double a1 = (double)(i + 1) * Math.PI * 2.0 / (double)segments;
            Vec3 p0 = TimeStasisClientEffects.circlePoint(center, radius, plane, a0);
            Vec3 p1 = TimeStasisClientEffects.circlePoint(center, radius, plane, a1);
            TimeStasisClientEffects.addLine(buffer, mat, p0, p1, color, alpha);
        }
    }

    private static Vec3 circlePoint(Vec3 center, double radius, int plane, double angle) {
        double c = Math.cos(angle) * radius;
        double s = Math.sin(angle) * radius;
        return switch (plane) {
            case 1 -> center.add(c, s, 0.0);
            case 2 -> center.add(0.0, c, s);
            default -> center.add(c, 0.0, s);
        };
    }

    private static void addLine(BufferBuilder buffer, Matrix4f mat, Vec3 from, Vec3 to, int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = Math.max(0, Math.min(255, (int)(alpha * 255.0f)));
        buffer.vertex(mat, (float)from.x, (float)from.y, (float)from.z).color(r, g, b, a).endVertex();
        buffer.vertex(mat, (float)to.x, (float)to.y, (float)to.z).color(r, g, b, a).endVertex();
    }

    private record ActiveDomain(Vec3 center, double radius, long startMs, long endMs, int casterEntityId) {
    }

    private record ClientFrozenSnapshot(Vec3 position, Vec3 deltaMovement, float yRot, float xRot, int tickCount) {
        static ClientFrozenSnapshot capture(Entity entity) {
            return new ClientFrozenSnapshot(entity.position(), entity.getDeltaMovement(), entity.getYRot(), entity.getXRot(), entity.tickCount);
        }
    }
}
