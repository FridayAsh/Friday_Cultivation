package com.friday.cultivation.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import com.friday.cultivation.client.renderer.BuddhaFireLotusRenderer;
import com.friday.cultivation.client.renderer.FireballRenderHelper;
import com.friday.cultivation.client.renderer.HeavenPiercingConeRenderer;
import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.entity.spell.GreatFireballEntity;
import com.friday.cultivation.entity.spell.HeavenPiercingConeEntity;
import java.util.Optional;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * 充能预览处理器 - 玩家长按蓄力法术时显示充能预览（火球/穿天锥/佛怒火莲/金丹自爆）。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.ChargingPreviewHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChargingPreviewHandler {
    private static final int BUDDHA_FIRE_LOTUS_PREVIEW_FLAGS = 42;
    private static final float BUDDHA_FIRE_LOTUS_READY_QI = 10000.0f;
    private static final float CORE_SELF_DESTRUCT_READY_QI = 1000.0f;
    private static final float BUDDHA_FIRE_LOTUS_LOTUS_REVEAL_PROGRESS = 0.88f;
    private static final double BUDDHA_FIRE_LOTUS_TARGET_RANGE = 96.0;
    private static final double BUDDHA_FIRE_LOTUS_TARGET_INFLATE = 0.75;
    private static final DustParticleOptions BUDDHA_CYAN_FIRE_DUST = new DustParticleOptions(new Vector3f(0.12f, 1.0f, 0.78f), 1.15f);
    private static final DustParticleOptions BUDDHA_WHITE_FIRE_DUST = new DustParticleOptions(new Vector3f(0.95f, 0.98f, 1.0f), 1.05f);
    private static final DustParticleOptions CORE_GOLD_DUST = new DustParticleOptions(new Vector3f(1.0f, 0.74f, 0.12f), 1.1f);
    private static int buddhaFireLotusTargetId = -1;

    private ChargingPreviewHandler() {
    }

    public static int currentBuddhaFireLotusTargetId() {
        return buddhaFireLotusTargetId;
    }

    private static float computeFireballScale(int chargedQi) {
        return GreatFireballEntity.renderDiameterForCharge(chargedQi);
    }

    private static double computeFireballDistance(float diameter) {
        return Math.max(3.0, (double) diameter * 0.7);
    }

    private static LocalCharge getLocalCharge() {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return null;
        }
        return CultivationCapability.get((Player) player).map(data -> {
            if (!data.isCharging()) {
                return null;
            }
            Spell spell = Spell.byId(data.getChargingSpellId());
            if (spell != Spell.GREAT_FIREBALL && spell != Spell.HEAVEN_PIERCING_CONE && spell != Spell.BUDDHA_FIRE_LOTUS && spell != Spell.CORE_SELF_DESTRUCT) {
                return null;
            }
            return new LocalCharge(spell, (int) Math.min(Integer.MAX_VALUE, data.getChargedQi()), data.getChargingTicks());
        }).orElse(null);
    }

    @SubscribeEvent
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        LocalCharge charge = ChargingPreviewHandler.getLocalCharge();
        if (charge == null) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null) {
            return;
        }
        Camera camera = event.getCamera();
        Vec3 camPos = camera.getPosition();
        float partial = event.getPartialTick();
        Vec3 eye = player.getEyePosition(partial);
        Vec3 look = player.getViewVector(partial).normalize();
        PoseStack pose = event.getPoseStack();
        pose.pushPose();
        if (charge.spell() == Spell.GREAT_FIREBALL) {
            float scale = ChargingPreviewHandler.computeFireballScale(charge.chargedQi());
            double dist = ChargingPreviewHandler.computeFireballDistance(scale);
            Vec3 center = eye.add(look.scale(dist)).add(0.0, -0.3, 0.0);
            pose.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
            float age = mc.level == null ? 0.0f : (float) (mc.level.getGameTime() & 0xFFFFFFL) + partial;
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            FireballRenderHelper.render(pose, buffers, scale, age);
            buffers.endBatch();
        } else if (charge.spell() == Spell.HEAVEN_PIERCING_CONE) {
            Vec3 center = ChargingPreviewHandler.heavenPiercingConePreviewCenter(eye, look);
            pose.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
            MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
            int stage = HeavenPiercingConeEntity.stageForChargeTicks(charge.chargingTicks());
            HeavenPiercingConeRenderer.renderCone(pose, buffers, look, stage, charge.chargingTicks(), (float) charge.chargingTicks() + partial, 0.9f);
            buffers.endBatch();
        } else if (charge.spell() == Spell.BUDDHA_FIRE_LOTUS) {
            ChargingPreviewHandler.renderBuddhaFireLotusPreview(mc, pose, camPos, eye, look, charge, partial);
            ChargingPreviewHandler.renderBuddhaFireLotusTargetOutline(mc, pose, camPos, partial);
        } else if (charge.spell() == Spell.CORE_SELF_DESTRUCT) {
            ChargingPreviewHandler.renderCoreSelfDestructWorld(mc, pose, camPos, player, charge, partial);
        }
        pose.popPose();
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        LocalCharge charge = ChargingPreviewHandler.getLocalCharge();
        if (charge == null) {
            return;
        }
        if (charge.spell() == Spell.CORE_SELF_DESTRUCT) {
            if (event.getHand() == InteractionHand.MAIN_HAND) {
                ChargingPreviewHandler.renderCoreSelfDestructFirstPerson(event.getPoseStack(), charge, event.getPartialTick());
            }
            return;
        }
        if (charge.spell() != Spell.BUDDHA_FIRE_LOTUS) {
            return;
        }
        event.setCanceled(true);
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null || player.isSpectator()) {
            return;
        }
        EntityRenderer renderer = mc.getEntityRenderDispatcher().getRenderer((Entity) player);
        if (!(renderer instanceof PlayerRenderer)) {
            return;
        }
        PlayerRenderer playerRenderer = (PlayerRenderer) renderer;
        float progress = Math.min(1.0f, (float) charge.chargedQi() / 10000.0f);
        ChargingPreviewHandler.renderClaspingArm(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), playerRenderer, player, HumanoidArm.RIGHT, progress);
        ChargingPreviewHandler.renderClaspingArm(event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight(), playerRenderer, player, HumanoidArm.LEFT, progress);
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        LocalCharge charge = ChargingPreviewHandler.getLocalCharge();
        if (charge == null) {
            buddhaFireLotusTargetId = -1;
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        Vec3 eye = player.getEyePosition();
        Vec3 look = player.getViewVector(1.0f).normalize();
        if (charge.spell() == Spell.GREAT_FIREBALL) {
            ChargingPreviewHandler.spawnFireballChargeParticles(mc, player, charge.chargedQi(), eye, look);
        } else if (charge.spell() == Spell.HEAVEN_PIERCING_CONE) {
            ChargingPreviewHandler.spawnHeavenPiercingConeParticles(mc, charge.chargingTicks(), ChargingPreviewHandler.heavenPiercingConePreviewCenter(eye, look), look);
        } else if (charge.spell() == Spell.BUDDHA_FIRE_LOTUS) {
            ChargingPreviewHandler.updateBuddhaFireLotusTarget(mc, player);
            ChargingPreviewHandler.spawnBuddhaFireLotusParticles(mc, charge.chargedQi(), ChargingPreviewHandler.buddhaFireLotusPreviewCenter(eye, look), look);
        } else if (charge.spell() == Spell.CORE_SELF_DESTRUCT) {
            buddhaFireLotusTargetId = -1;
            ChargingPreviewHandler.spawnCoreSelfDestructParticles(mc, player, charge);
        } else {
            buddhaFireLotusTargetId = -1;
        }
    }

    private static void renderCoreSelfDestructWorld(Minecraft mc, PoseStack pose, Vec3 camPos, LocalPlayer player, LocalCharge charge, float partial) {
        float progress = Math.min(1.0f, (float) charge.chargedQi() / 1000.0f);
        float age = mc.level == null ? partial : (float) (mc.level.getGameTime() & 0xFFFFFFL) + partial;
        Vec3 look = player.getViewVector(partial).normalize();
        double x = Mth.lerp((double) partial, (double) player.xo, (double) player.getX());
        double y = Mth.lerp((double) partial, (double) player.yo, (double) player.getY()) + (double) player.getBbHeight() * 0.58;
        double z = Mth.lerp((double) partial, (double) player.zo, (double) player.getZ());
        Vec3 center = new Vec3(x, y, z).add(look.scale(0.18 + (double) progress * 0.22));
        pose.pushPose();
        pose.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
        pose.mulPose(mc.getEntityRenderDispatcher().cameraOrientation());
        ChargingPreviewHandler.renderGoldenCoreCharge(pose, progress, age, 0.22f + progress * 0.15f, 1.0f);
        pose.popPose();
    }

    private static void renderCoreSelfDestructFirstPerson(PoseStack pose, LocalCharge charge, float partial) {
        Minecraft mc = Minecraft.getInstance();
        float progress = Math.min(1.0f, (float) charge.chargedQi() / 1000.0f);
        float age = mc.level == null ? partial : (float) (mc.level.getGameTime() & 0xFFFFFFL) + partial;
        float emerge = ChargingPreviewHandler.smooth(progress / 0.28f);
        pose.pushPose();
        pose.translate(0.0f, -0.54f + emerge * 0.08f, -0.52f - emerge * 0.58f);
        ChargingPreviewHandler.renderGoldenCoreCharge(pose, progress, age, 0.12f + progress * 0.11f, 1.18f);
        pose.popPose();
    }

    private static void renderGoldenCoreCharge(PoseStack pose, float progress, float age, float radius, float rayScale) {
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Matrix4f mat = pose.last().pose();
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
        ChargingPreviewHandler.addDisc(buffer, mat, radius * 1.55f, 16761916, 0.18f + progress * 0.24f, 24, age * 0.05f);
        ChargingPreviewHandler.addDisc(buffer, mat, radius, 16767082, 0.72f, 24, -age * 0.08f);
        ChargingPreviewHandler.addDisc(buffer, mat, radius * 0.54f, 16774328, 0.86f, 20, age * 0.13f);
        tesselator.end();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        ChargingPreviewHandler.addCoreCracks(buffer, mat, radius, progress, age);
        ChargingPreviewHandler.addCoreRays(buffer, mat, radius, progress, age, rayScale);
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void addDisc(BufferBuilder buffer, Matrix4f mat, float radius, int color, float alpha, int segments, float rotation) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f);
        for (int i = 0; i < segments; ++i) {
            double a0 = (double) rotation + (double) i * Math.PI * 2.0 / (double) segments;
            double a1 = (double) rotation + (double) (i + 1) * Math.PI * 2.0 / (double) segments;
            buffer.vertex(mat, 0.0f, 0.0f, 0.0f).color(r, g, b, a).endVertex();
            buffer.vertex(mat, (float) Math.cos(a0) * radius, (float) Math.sin(a0) * radius, 0.0f).color(r, g, b, a).endVertex();
            buffer.vertex(mat, (float) Math.cos(a1) * radius, (float) Math.sin(a1) * radius, 0.0f).color(r, g, b, a).endVertex();
            buffer.vertex(mat, 0.0f, 0.0f, 0.0f).color(r, g, b, a).endVertex();
        }
    }

    private static void addCoreCracks(BufferBuilder buffer, Matrix4f mat, float radius, float progress, float age) {
        int count = 2 + (int) (progress * 8.0f);
        for (int i = 0; i < count; ++i) {
            double angle = (double) i * 2.399963 + (double) age * 0.015;
            float start = radius * (0.18f + 0.07f * (float) (i % 3));
            float end = radius * (0.56f + progress * 0.38f);
            float wobble = (float) Math.sin(age * 0.18f + (float) i) * radius * 0.05f;
            int color = progress > 0.55f ? 0xFFF0A0 : 7025920;
            float alpha = 0.45f + progress * 0.45f;
            ChargingPreviewHandler.addLine(buffer, mat, Math.cos(angle) * (double) start, Math.sin(angle) * (double) start, 0.012f, Math.cos(angle) * (double) end - Math.sin(angle) * (double) wobble, Math.sin(angle) * (double) end + Math.cos(angle) * (double) wobble, 0.014f, color, alpha);
        }
    }

    private static void addCoreRays(BufferBuilder buffer, Matrix4f mat, float radius, float progress, float age, float rayScale) {
        if (progress < 0.32f) {
            return;
        }
        RenderSystem.lineWidth(1.5f + progress * 2.0f);
        int count = 3 + (int) (progress * 16.0f);
        for (int i = 0; i < count; ++i) {
            double angle = (double) age * 0.11 + (double) i * Math.PI * 2.0 / (double) count;
            float inner = radius * (0.66f + (float) (i % 2) * 0.12f);
            float outer = radius * (1.2f + progress * (1.35f + (float) (i % 4) * 0.18f)) * rayScale;
            float flicker = 0.72f + 0.28f * (float) Math.sin(age * 0.43f + (float) i * 1.7f);
            ChargingPreviewHandler.addLine(buffer, mat, Math.cos(angle) * (double) inner, Math.sin(angle) * (double) inner, 0.018f, Math.cos(angle) * (double) outer, Math.sin(angle) * (double) outer, 0.02f, i % 3 == 0 ? 16775633 : 16765786, (0.26f + progress * 0.44f) * flicker);
        }
    }

    private static void addLine(BufferBuilder buffer, Matrix4f mat, double x1, double y1, double z1, double x2, double y2, double z2, int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f);
        buffer.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
    }

    private static void renderBuddhaFireLotusPreview(Minecraft mc, PoseStack pose, Vec3 camPos, Vec3 eye, Vec3 look, LocalCharge charge, float partial) {
        Vec3 center = ChargingPreviewHandler.buddhaFireLotusPreviewCenter(eye, look);
        float progress = Math.min(1.0f, (float) charge.chargedQi() / 10000.0f);
        float age = mc.level == null ? partial : (float) (mc.level.getGameTime() & 0xFFFFFFL) + partial;
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        if (progress >= 0.88f) {
            float formed = ChargingPreviewHandler.smooth((progress - 0.88f) / 0.120000005f);
            float scale = 0.08f + formed * 0.5f;
            float alpha = 0.18f + formed * 0.76f;
            ChargingPreviewHandler.renderPreviewLotus(pose, buffers, camPos, center, 42, charge.chargedQi(), age, scale, alpha, formed);
        }
        buffers.endBatch();
    }

    private static void renderPreviewLotus(PoseStack pose, MultiBufferSource buffers, Vec3 camPos, Vec3 center, int flags, int chargedQi, float age, float scale, float alpha, float bloom) {
        pose.pushPose();
        pose.translate(center.x - camPos.x, center.y - camPos.y, center.z - camPos.z);
        BuddhaFireLotusRenderer.renderLotus(pose, buffers, flags, chargedQi, age, scale, alpha, age * 0.95f, bloom);
        pose.popPose();
    }

    private static void updateBuddhaFireLotusTarget(Minecraft mc, LocalPlayer player) {
        LivingEntity target = ChargingPreviewHandler.findBuddhaFireLotusTarget(mc, player, 1.0f);
        buddhaFireLotusTargetId = target == null ? -1 : target.getId();
    }

    private static LivingEntity findBuddhaFireLotusTarget(Minecraft mc, LocalPlayer player, float partial) {
        if (mc.level == null) {
            return null;
        }
        Vec3 eye = player.getEyePosition(partial);
        Vec3 look = player.getViewVector(partial).normalize();
        if (look.lengthSqr() < 1.0E-6) {
            return null;
        }
        Vec3 end = eye.add(look.scale(96.0));
        double blockDist = ChargingPreviewHandler.clientBlockDistance(mc, player, eye, end);
        AABB scan = new AABB(eye, end).inflate(2.75);
        LivingEntity best = null;
        double bestDist = blockDist;
        for (LivingEntity entity : mc.level.getEntitiesOfClass(LivingEntity.class, scan, e -> e != player && e.isAlive() && e.isPickable())) {
            double dist;
            Optional<Vec3> hit = entity.getBoundingBox().inflate(0.75).clip(eye, end);
            if (hit.isEmpty() || !((dist = eye.distanceTo(hit.get())) <= blockDist + 0.35) || !(dist < bestDist)) continue;
            bestDist = dist;
            best = entity;
        }
        return best;
    }

    private static double clientBlockDistance(Minecraft mc, LocalPlayer player, Vec3 eye, Vec3 end) {
        BlockHitResult hit = mc.level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, (Entity) player));
        return hit.getType() == HitResult.Type.MISS ? 96.0 : eye.distanceTo(hit.getLocation());
    }

    private static void renderBuddhaFireLotusTargetOutline(Minecraft mc, PoseStack pose, Vec3 camPos, float partial) {
        LivingEntity target;
        if (buddhaFireLotusTargetId < 0 || mc.level == null) {
            return;
        }
        Entity entity = mc.level.getEntity(buddhaFireLotusTargetId);
        if (!(entity instanceof LivingEntity) || !(target = (LivingEntity) entity).isAlive()) {
            buddhaFireLotusTargetId = -1;
            return;
        }
        double x = Mth.lerp((double) partial, (double) target.xo, (double) target.getX());
        double y = Mth.lerp((double) partial, (double) target.yo, (double) target.getY());
        double z = Mth.lerp((double) partial, (double) target.zo, (double) target.getZ());
        AABB box = target.getBoundingBox().move(x - target.getX(), y - target.getY(), z - target.getZ()).inflate(0.08).move(-camPos.x, -camPos.y, -camPos.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableCull();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.lineWidth(3.0f);
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        Tesselator tesselator = Tesselator.getInstance();
        BufferBuilder buffer = tesselator.getBuilder();
        buffer.begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        Matrix4f mat = pose.last().pose();
        ChargingPreviewHandler.addOutlineBox(buffer, mat, box, 4257279, 0.95f);
        ChargingPreviewHandler.addOutlineBox(buffer, mat, box.inflate(0.055), 0xF4FEFF, 0.68f);
        tesselator.end();
        RenderSystem.lineWidth(1.0f);
        RenderSystem.depthMask(true);
        RenderSystem.enableDepthTest();
        RenderSystem.enableCull();
        RenderSystem.disableBlend();
    }

    private static void addOutlineBox(BufferBuilder buffer, Matrix4f mat, AABB box, int color, float alpha) {
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.minY, box.minZ, box.maxX, box.minY, box.minZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.minY, box.maxZ, box.maxX, box.minY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.maxY, box.minZ, box.maxX, box.maxY, box.minZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.maxY, box.maxZ, box.maxX, box.maxY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.minY, box.minZ, box.minX, box.minY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.maxX, box.minY, box.minZ, box.maxX, box.minY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.maxY, box.minZ, box.minX, box.maxY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.maxX, box.maxY, box.minZ, box.maxX, box.maxY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.minY, box.minZ, box.minX, box.maxY, box.minZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.maxX, box.minY, box.minZ, box.maxX, box.maxY, box.minZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.minX, box.minY, box.maxZ, box.minX, box.maxY, box.maxZ, color, alpha);
        ChargingPreviewHandler.addOutlineLine(buffer, mat, box.maxX, box.minY, box.maxZ, box.maxX, box.maxY, box.maxZ, color, alpha);
    }

    private static void addOutlineLine(BufferBuilder buffer, Matrix4f mat, double x1, double y1, double z1, double x2, double y2, double z2, int color, float alpha) {
        int r = color >> 16 & 0xFF;
        int g = color >> 8 & 0xFF;
        int b = color & 0xFF;
        int a = (int) (Mth.clamp(alpha, 0.0f, 1.0f) * 255.0f);
        buffer.vertex(mat, (float) x1, (float) y1, (float) z1).color(r, g, b, a).endVertex();
        buffer.vertex(mat, (float) x2, (float) y2, (float) z2).color(r, g, b, a).endVertex();
    }

    private static void renderClaspingArm(PoseStack pose, MultiBufferSource buffers, int packedLight, PlayerRenderer playerRenderer, LocalPlayer player, HumanoidArm arm, float progress) {
        boolean right = arm == HumanoidArm.RIGHT;
        float side = right ? 1.0f : -1.0f;
        float approach = ChargingPreviewHandler.smooth(progress / 0.8f);
        float reveal = ChargingPreviewHandler.smooth((progress - 0.88f) / 0.12f);
        float shoulderX = side * 0.64000005f;
        float shoulderY = -0.6f;
        float shoulderZ = -0.71999997f;
        float arcDegrees = Mth.lerp(reveal, Mth.lerp(approach, 0.0f, 58.0f), 34.0f);
        float palmYaw = Mth.lerp(reveal, Mth.lerp(approach, 45.0f, 112.0f), 88.0f);
        float palmPitch = Mth.lerp(reveal, Mth.lerp(approach, 0.0f, 28.0f), 14.0f);
        float wristRoll = Mth.lerp(reveal, Mth.lerp(approach, 0.0f, 16.0f), 8.0f);
        pose.pushPose();
        pose.translate(shoulderX, shoulderY, shoulderZ);
        pose.mulPose(Axis.XP.rotationDegrees(side * arcDegrees));
        pose.mulPose(Axis.YP.rotationDegrees(side * palmYaw));
        pose.mulPose(Axis.ZP.rotationDegrees(palmPitch));
        pose.mulPose(Axis.XP.rotationDegrees(side * wristRoll));
        pose.translate(side * -1.0f, 3.6f, 3.5f);
        pose.mulPose(Axis.XP.rotationDegrees(side * 120.0f));
        pose.mulPose(Axis.ZP.rotationDegrees(200.0f));
        pose.mulPose(Axis.YP.rotationDegrees(side * -135.0f));
        pose.translate(side * 5.6f, 0.0f, 0.0f);
        if (right) {
            playerRenderer.renderRightHand(pose, buffers, packedLight, (AbstractClientPlayer) player);
        } else {
            playerRenderer.renderLeftHand(pose, buffers, packedLight, (AbstractClientPlayer) player);
        }
        pose.popPose();
    }

    private static float smooth(float value) {
        float clamped = Mth.clamp(value, 0.0f, 1.0f);
        return clamped * clamped * (3.0f - 2.0f * clamped);
    }

    private static Vec3 buddhaFireLotusPreviewCenter(Vec3 eye, Vec3 look) {
        return eye.add(look.normalize().scale(1.2)).add(0.0, -0.18, 0.0);
    }

    private static Vec3 horizontalRight(Vec3 look) {
        Vec3 right = new Vec3(-look.z, 0.0, look.x);
        if (right.lengthSqr() < 1.0E-6) {
            return new Vec3(1.0, 0.0, 0.0);
        }
        return right.normalize();
    }

    private static Vec3 heavenPiercingConePreviewCenter(Vec3 eye, Vec3 look) {
        Vec3 forward = HeavenPiercingConeEntity.safeDirection(look);
        return eye.add(forward.scale(2.35)).add(HeavenPiercingConeEntity.rightSideOffset(forward, 0.85, 0.45));
    }

    private static void spawnFireballChargeParticles(Minecraft mc, LocalPlayer player, int chargedQi, Vec3 eye, Vec3 look) {
        float scale = ChargingPreviewHandler.computeFireballScale(chargedQi);
        double dist = ChargingPreviewHandler.computeFireballDistance(scale);
        Vec3 center = eye.add(look.scale(dist)).add(0.0, -0.3, 0.0);
        double px = player.getX() + (mc.level.random.nextDouble() - 0.5) * (double) player.getBbWidth();
        double py = player.getY() + mc.level.random.nextDouble() * (double) player.getBbHeight();
        double pz = player.getZ() + (mc.level.random.nextDouble() - 0.5) * (double) player.getBbWidth();
        mc.level.addParticle((ParticleOptions) ParticleTypes.FLAME, px, py, pz, (center.x - px) * 0.3, (center.y - py) * 0.3, (center.z - pz) * 0.3);
        float visualR = scale * 0.6f;
        int sparkCount = (int) Math.max(3.0f, Math.min(20.0f, visualR));
        for (int i = 0; i < sparkCount; ++i) {
            double a = mc.level.random.nextDouble() * Math.PI * 2.0;
            double b = (mc.level.random.nextDouble() - 0.5) * Math.PI;
            double sx = Math.cos(a) * Math.cos(b);
            double sy = Math.sin(b);
            double sz = Math.sin(a) * Math.cos(b);
            SimpleParticleType particle = switch (mc.level.random.nextInt(4)) {
                case 0 -> ParticleTypes.LAVA;
                case 1 -> ParticleTypes.SMALL_FLAME;
                default -> ParticleTypes.FLAME;
            };
            double speed = 0.04 + mc.level.random.nextDouble() * 0.06;
            mc.level.addParticle(particle, center.x + sx * (double) visualR, center.y + sy * (double) visualR, center.z + sz * (double) visualR, sx * speed, sy * speed + 0.02, sz * speed);
        }
    }

    private static void spawnHeavenPiercingConeParticles(Minecraft mc, int chargingTicks, Vec3 center, Vec3 look) {
        int stage = HeavenPiercingConeEntity.stageForChargeTicks(chargingTicks);
        mc.level.addParticle((ParticleOptions) new BlockParticleOption(ParticleTypes.FALLING_DUST, Blocks.DEEPSLATE.defaultBlockState()), center.x, center.y, center.z, 0.0, 0.0, 0.0);
        if (stage == 1) {
            mc.level.addParticle((ParticleOptions) ParticleTypes.DRIPPING_WATER, center.x, center.y - 0.12, center.z, 0.0, -0.02, 0.0);
            return;
        }
        int cloudCount = stage >= 3 ? 4 : 2;
        for (int i = 0; i < cloudCount; ++i) {
            Vec3 offset = ChargingPreviewHandler.randomRingOffset(mc).scale(0.18 + (double) stage * 0.03);
            mc.level.addParticle((ParticleOptions) ParticleTypes.CLOUD, center.x + offset.x, center.y + offset.y, center.z + offset.z, -look.x * 0.03, -look.y * 0.03, -look.z * 0.03);
        }
        if (stage >= 3) {
            int pressureCount = stage >= 4 ? 4 : 2;
            for (int i = 0; i < pressureCount; ++i) {
                Vec3 tip = center.add(look.scale(0.55)).add(ChargingPreviewHandler.randomRingOffset(mc).scale(0.08));
                mc.level.addParticle((ParticleOptions) ParticleTypes.CLOUD, tip.x, tip.y, tip.z, -look.x * 0.05, -look.y * 0.05, -look.z * 0.05);
            }
        }
    }

    private static void spawnBuddhaFireLotusParticles(Minecraft mc, int chargedQi, Vec3 center, Vec3 look) {
        float progress = Math.min(1.0f, (float) chargedQi / 10000.0f);
        float merge = ChargingPreviewHandler.smooth(progress / 0.88f);
        Vec3 right = ChargingPreviewHandler.horizontalRight(look);
        double handOffset = 0.86 + -0.825 * (double) merge;
        double handYOffset = -0.5 + 0.4 * (double) merge;
        Vec3 handBase = center.add(look.scale(-0.05)).add(0.0, handYOffset, 0.0);
        Vec3 mergeTarget = center.add(0.0, -0.08, 0.0);
        Vec3 left = handBase.subtract(right.scale(handOffset));
        Vec3 rightPos = handBase.add(right.scale(handOffset));
        ChargingPreviewHandler.spawnPalmFireCluster(mc, left, mergeTarget, right, look, BUDDHA_CYAN_FIRE_DUST, (ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, progress, true);
        ChargingPreviewHandler.spawnPalmFireCluster(mc, rightPos, mergeTarget, right, look, BUDDHA_WHITE_FIRE_DUST, (ParticleOptions) ParticleTypes.CLOUD, progress, false);
        if (progress >= 0.88f) {
            mc.level.addParticle((ParticleOptions) ParticleTypes.SOUL_FIRE_FLAME, center.x, center.y, center.z, 0.0, 0.012, 0.0);
            mc.level.addParticle((ParticleOptions) BUDDHA_WHITE_FIRE_DUST, center.x, center.y + 0.03, center.z, 0.0, 0.018, 0.0);
        }
    }

    private static void spawnPalmFireCluster(Minecraft mc, Vec3 origin, Vec3 target, Vec3 right, Vec3 look, DustParticleOptions dust, ParticleOptions flame, float progress, boolean cyan) {
        double pull = 0.08 + (double) progress * 0.055;
        Vec3 inward = target.subtract(origin).scale(pull);
        for (int i = 0; i < 3; ++i) {
            Vec3 pos = origin.add(ChargingPreviewHandler.randomPalmJitter(mc, right, look, 0.12 + (double) progress * 0.08));
            Vec3 vel = inward.add(0.0, 0.018 + mc.level.random.nextDouble() * 0.018, 0.0);
            mc.level.addParticle((ParticleOptions) dust, pos.x, pos.y + 0.04, pos.z, vel.x, vel.y, vel.z);
        }
        Vec3 flamePos = origin.add(ChargingPreviewHandler.randomPalmJitter(mc, right, look, 0.08));
        Vec3 flameVel = inward.scale(0.72).add(0.0, cyan ? 0.018 : 0.01, 0.0);
        mc.level.addParticle(flame, flamePos.x, flamePos.y, flamePos.z, flameVel.x, flameVel.y, flameVel.z);
    }

    private static Vec3 randomPalmJitter(Minecraft mc, Vec3 right, Vec3 look, double radius) {
        double side = (mc.level.random.nextDouble() - 0.5) * radius;
        double vertical = (mc.level.random.nextDouble() - 0.5) * radius * 0.72;
        double depth = (mc.level.random.nextDouble() - 0.5) * radius * 0.46;
        return right.scale(side).add(0.0, vertical, 0.0).add(look.scale(depth));
    }

    private static void spawnCoreSelfDestructParticles(Minecraft mc, LocalPlayer player, LocalCharge charge) {
        float progress = Math.min(1.0f, (float) charge.chargedQi() / 1000.0f);
        Vec3 look = player.getViewVector(1.0f).normalize();
        Vec3 chest = player.position().add(0.0, (double) player.getBbHeight() * 0.58, 0.0).add(look.scale(0.18 + (double) progress * 0.22));
        int sparks = progress > 0.5f ? 3 : 1;
        for (int i = 0; i < sparks; ++i) {
            Vec3 offset = ChargingPreviewHandler.randomRingOffset(mc).scale(0.12 + (double) progress * 0.24);
            mc.level.addParticle((ParticleOptions) CORE_GOLD_DUST, chest.x + offset.x, chest.y + offset.y * 0.55, chest.z + offset.z, offset.x * 0.015, 0.008 + (double) progress * 0.012, offset.z * 0.015);
        }
        if (progress > 0.72f && mc.level.random.nextInt(3) == 0) {
            Vec3 offset = ChargingPreviewHandler.randomRingOffset(mc).scale(0.18 + (double) progress * 0.34);
            mc.level.addParticle((ParticleOptions) ParticleTypes.END_ROD, chest.x + offset.x, chest.y + offset.y * 0.45, chest.z + offset.z, offset.x * 0.02, offset.y * 0.01, offset.z * 0.02);
        }
    }

    private static Vec3 randomRingOffset(Minecraft mc) {
        double angle = mc.level.random.nextDouble() * Math.PI * 2.0;
        double y = (mc.level.random.nextDouble() - 0.5) * 0.8;
        return new Vec3(Math.cos(angle), y, Math.sin(angle));
    }

    private record LocalCharge(Spell spell, int chargedQi, int chargingTicks) {
    }
}
