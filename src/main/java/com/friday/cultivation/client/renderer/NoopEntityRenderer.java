/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  net.minecraft.client.renderer.culling.Frustum
 *  net.minecraft.client.renderer.entity.EntityRenderer
 *  net.minecraft.client.renderer.entity.EntityRendererProvider$Context
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.world.entity.Entity
 *  org.jetbrains.annotations.NotNull
 */
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

    public boolean shouldRender(@NotNull T entity, @NotNull Frustum cam, double camX, double camY, double camZ) {
        return false;
    }

    @NotNull
    public ResourceLocation getBlockLightLevel(@NotNull T entity) {
        return EMPTY;
    }

    @NotNull
    public ResourceLocation getTextureLocation(T entity) {
        return new ResourceLocation("friday_cultivation", "textures/entity/noop.png");
    }
}
