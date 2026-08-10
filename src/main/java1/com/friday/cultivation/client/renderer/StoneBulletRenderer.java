package com.friday.cultivation.client.renderer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.friday.cultivation.entity.spell.StoneBulletEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PointedDripstoneBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DripstoneThickness;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
public class StoneBulletRenderer
extends EntityRenderer<StoneBulletEntity> {
    private static final BlockState DRIPSTONE_TIP = (BlockState)((BlockState)Blocks.POINTED_DRIPSTONE.defaultBlockState().setValue((Property)PointedDripstoneBlock.TIP_DIRECTION, (Comparable)Direction.UP)).setValue((Property)PointedDripstoneBlock.THICKNESS, (Comparable)DripstoneThickness.TIP);
    public StoneBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.0f;
    }
    public void render(@NotNull StoneBulletEntity entity, float yaw, float partialTick, @NotNull PoseStack pose, @NotNull MultiBufferSource buffers, int packedLight) {
        pose.pushPose();
        StoneBulletRenderer.orientAlongDirection(pose, StoneBulletRenderer.renderDirection(entity));
        pose.mulPose(Axis.XP.rotationDegrees(90.0f));
        pose.mulPose(Axis.YP.rotationDegrees(((float)entity.tickCount + partialTick) * 18.0f));
        pose.scale(0.72f, 0.72f, 0.72f);
        pose.translate(-0.5, -0.5, -0.5);
        Minecraft.getInstance().getBlockRenderer().renderSingleBlock(DRIPSTONE_TIP, pose, buffers, packedLight, OverlayTexture.NO_OVERLAY);
        pose.popPose();
        super.render(entity, yaw, partialTick, pose, buffers, packedLight);
    }
    private static Vec3 renderDirection(StoneBulletEntity entity) {
        Vec3 velocity = entity.getDeltaMovement();
        if (velocity.lengthSqr() > 1.0E-6) {
            return velocity.normalize();
        }
        Vec3 look = Vec3.directionFromRotation((float)entity.getYRot(), (float)entity.getXRot());
        return look.lengthSqr() > 1.0E-6 ? look.normalize() : new Vec3(0.0, 0.0, 1.0);
    }
    private static void orientAlongDirection(PoseStack pose, Vec3 direction) {
        Vec3 dir = direction.lengthSqr() > 1.0E-6 ? direction.normalize() : new Vec3(0.0, 0.0, 1.0);
        float yaw = (float)(Mth.atan2((double)dir.x, (double)dir.z) * 57.2957763671875);
        double horizontal = Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float pitch = (float)(Mth.atan2((double)(-dir.y), (double)horizontal) * 57.2957763671875);
        pose.mulPose(Axis.YP.rotationDegrees(yaw));
        pose.mulPose(Axis.XP.rotationDegrees(pitch));
    }
    @NotNull
    public ResourceLocation getTextureLocation(@NotNull StoneBulletEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
