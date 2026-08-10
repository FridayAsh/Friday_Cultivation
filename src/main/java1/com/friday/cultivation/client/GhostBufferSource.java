package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.NotNull;

/**
 * Ghost渲染缓冲源 — 完整复刻原模组 GhostBufferSource。
 * 包装另一个 MultiBufferSource，将所有顶点的 alpha 通道覆盖为固定值，实现半透明 ghost 渲染。
 */
public class GhostBufferSource implements MultiBufferSource {
    private final MultiBufferSource wrapped;
    private final int alphaOverride;

    public GhostBufferSource(MultiBufferSource wrapped, int alpha0to255) {
        this.wrapped = wrapped;
        this.alphaOverride = Math.max(0, Math.min(255, alpha0to255));
    }

    @NotNull
    @Override
    public VertexConsumer getBuffer(@NotNull RenderType type) {
        return new AlphaOverrideConsumer(this.wrapped.getBuffer(RenderType.translucent()), this.alphaOverride);
    }

    private static final class AlphaOverrideConsumer implements VertexConsumer {
        private final VertexConsumer parent;
        private final int alpha;

        AlphaOverrideConsumer(VertexConsumer parent, int alpha) {
            this.parent = parent;
            this.alpha = alpha;
        }

        @NotNull
        @Override
        public VertexConsumer vertex(double x, double y, double z) {
            this.parent.vertex(x, y, z);
            return this;
        }

        @NotNull
        @Override
        public VertexConsumer color(int r, int g, int b, int a) {
            this.parent.color(r, g, b, this.alpha);
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
            this.parent.defaultColor(r, g, b, this.alpha);
        }

        @Override
        public void unsetDefaultColor() {
            this.parent.unsetDefaultColor();
        }
    }
}
