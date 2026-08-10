/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.vertex.VertexConsumer
 *  net.minecraft.client.renderer.MultiBufferSource
 *  net.minecraft.client.renderer.RenderType
 *  org.jetbrains.annotations.NotNull
 */
package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

public class GhostBufferSource
implements MultiBufferSource {
    private final MultiBufferSource wrapped;
    private final int alphaOverride;

    public GhostBufferSource(MultiBufferSource wrapped, int alpha0to255) {
        this.wrapped = wrapped;
        this.alphaOverride = Math.max(0, Math.min(255, alpha0to255));
    }

    @NotNull
    public VertexConsumer getBuffer(@NotNull RenderType type) {
        return new AlphaOverrideConsumer(this.wrapped.getBuffer(RenderType.translucent()), this.alphaOverride);
    }

    private static final class AlphaOverrideConsumer
    implements VertexConsumer {
        private final VertexConsumer parent;
        private final int alpha;

        AlphaOverrideConsumer(VertexConsumer parent, int alpha) {
            this.parent = parent;
            this.alpha = alpha;
        }

        @NotNull
        public VertexConsumer vertex(double x, double y, double z) {
            this.parent.vertex(x, y, z);
            return this;
        }

        @NotNull
        public VertexConsumer color(int r, int g, int b, int a) {
            this.parent.color(r, g, b, this.alpha);
            return this;
        }

        @NotNull
        public VertexConsumer uv(float u, float v) {
            this.parent.uv(u, v);
            return this;
        }

        @NotNull
        public VertexConsumer overlayCoords(int u, int v) {
            this.parent.overlayCoords(u, v);
            return this;
        }

        @NotNull
        public VertexConsumer uv2(int u, int v) {
            this.parent.uv2(u, v);
            return this;
        }

        @NotNull
        public VertexConsumer normal(float x, float y, float z) {
            this.parent.normal(x, y, z);
            return this;
        }

        public void endVertex() {
            this.parent.endVertex();
        }

        public void defaultColor(int r, int g, int b, int a) {
            this.parent.defaultColor(r, g, b, this.alpha);
        }

        public void unsetDefaultColor() {
            this.parent.unsetDefaultColor();
        }
    }
}

