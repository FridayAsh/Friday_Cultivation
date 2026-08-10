package com.friday.cultivation.client.renderer;

import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;

public class NoopEntityRenderer<T extends Entity>
extends EntityRenderer<T> {
    private static final ResourceLocation EMPTY = new ResourceLocation("textures/misc/white.png");

    public NoopEntityRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.shadowRadius = 0.0f;
    }

    @Override
    public boolean shouldRender(@NotNull T entity, @NotNull Frustum cam, double camX, double camY, double camZ) {
        return false;
    }

    @NotNull
    @Override
    public ResourceLocation getTextureLocation(@NotNull T entity) {
        return EMPTY;
    }
}
