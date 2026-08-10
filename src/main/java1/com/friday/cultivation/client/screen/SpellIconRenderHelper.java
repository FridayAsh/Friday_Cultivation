package com.friday.cultivation.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.friday.cultivation.spell.Spell;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * 法术图标渲染辅助（完全照搬原模组 SpellIconRenderHelper）
 * 渲染法术图标，支持灰度（禁用状态）版本。
 */
final class SpellIconRenderHelper {
    private static final Map<ResourceLocation, ResourceLocation> GRAYSCALE_CACHE = new HashMap<>();

    private SpellIconRenderHelper() {
    }

    static void blitSpellIcon(GuiGraphics gfx, Spell spell, int x, int y, int size) {
        blitSpellIcon(gfx, spell, x, y, size, false);
    }

    static void blitSpellIcon(GuiGraphics gfx, Spell spell, int x, int y, int size, boolean grayscale) {
        int textureSize = spell.iconTextureSize();
        ResourceLocation texture = grayscale ? grayscaleTexture(spell.iconTexture()) : spell.iconTexture();
        gfx.blit(texture, x, y, size, size, 0.0f, 0.0f, textureSize, textureSize, textureSize, textureSize);
    }

    private static ResourceLocation grayscaleTexture(ResourceLocation source) {
        return GRAYSCALE_CACHE.computeIfAbsent(source, SpellIconRenderHelper::createGrayscaleTexture);
    }

    private static ResourceLocation createGrayscaleTexture(ResourceLocation source) {
        Minecraft mc = Minecraft.getInstance();
        Optional<Resource> resource = mc.getResourceManager().getResource(source);
        if (resource.isEmpty()) {
            return source;
        }
        try (InputStream in = resource.get().open()) {
            NativeImage original = NativeImage.read(in);
            try {
                NativeImage grayscale = new NativeImage(NativeImage.Format.RGBA, original.getWidth(), original.getHeight(), false);
                for (int y = 0; y < original.getHeight(); ++y) {
                    for (int x = 0; x < original.getWidth(); ++x) {
                        int pixel = original.getPixelRGBA(x, y);
                        int alpha = pixel >>> 24 & 0xFF;
                        int red = pixel & 0xFF;
                        int green = pixel >>> 8 & 0xFF;
                        int blue = pixel >>> 16 & 0xFF;
                        int gray = SpellIconRenderHelper.clamp((red * 30 + green * 59 + blue * 11) / 100);
                        grayscale.setPixelRGBA(x, y, alpha << 24 | gray << 16 | gray << 8 | gray);
                    }
                }
                ResourceLocation generated = grayscaleResourceLocation(source);
                mc.getTextureManager().register(generated, new DynamicTexture(grayscale));
                return generated;
            } finally {
                original.close();
            }
        } catch (IOException | IllegalStateException ex) {
            org.slf4j.LoggerFactory.getLogger("friday_cultivation").warn("Failed to create grayscale spell icon for {}", source, ex);
            return source;
        }
    }

    private static int clamp(int value) {
        if (value < 0) {
            return 0;
        }
        return Math.min(value, 255);
    }

    private static ResourceLocation grayscaleResourceLocation(ResourceLocation source) {
        String path = source.getPath().replace('/', '_').replace(".png", "");
        return new ResourceLocation("friday_cultivation", "dynamic/grayscale_spell_icon/" + path);
    }
}
