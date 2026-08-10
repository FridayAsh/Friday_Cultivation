/*
 * Decompiled with CFR 0.152.
 * 
 * Could not load the following classes:
 *  com.mojang.blaze3d.platform.NativeImage
 *  com.mojang.blaze3d.platform.NativeImage$Format
 *  net.minecraft.client.Minecraft
 *  net.minecraft.client.gui.GuiGraphics
 *  net.minecraft.client.renderer.texture.AbstractTexture
 *  net.minecraft.client.renderer.texture.DynamicTexture
 *  net.minecraft.resources.ResourceLocation
 *  net.minecraft.server.packs.resources.Resource
 */
package com.friday.cultivation.client.screen;

import com.mojang.blaze3d.platform.NativeImage;
import com.friday.cultivation.FridayCultivationMod;
import com.friday.cultivation.cultivation.spell.Spell;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

final class SpellIconRenderHelper {
    private static final Map<ResourceLocation, ResourceLocation> GRAYSCALE_CACHE = new HashMap<ResourceLocation, ResourceLocation>();

    private SpellIconRenderHelper() {
    }

    static void blitSpellIcon(GuiGraphics gfx, Spell spell, int x, int y, int size) {
        SpellIconRenderHelper.blitSpellIcon(gfx, spell, x, y, size, false);
    }

    static void blitSpellIcon(GuiGraphics gfx, Spell spell, int x, int y, int size, boolean grayscale) {
        int textureSize = spell.iconTextureSize();
        ResourceLocation texture = grayscale ? SpellIconRenderHelper.grayscaleTexture(spell.iconTexture()) : spell.iconTexture();
        gfx.blit(texture, x, y, size, size, 0.0f, 0.0f, textureSize, textureSize, textureSize, textureSize);
    }

    private static ResourceLocation grayscaleTexture(ResourceLocation source) {
        return GRAYSCALE_CACHE.computeIfAbsent(source, SpellIconRenderHelper::createGrayscaleTexture);
    }

    /*
     * Enabled aggressive exception aggregation
     */
    private static ResourceLocation createGrayscaleTexture(ResourceLocation source) {
        Minecraft mc = Minecraft.getInstance();
        Optional resource = mc.getResourceManager().getResource(source);
        if (resource.isEmpty()) {
            return source;
        }
        try (InputStream in = ((Resource)resource.get()).open();){
            ResourceLocation resourceLocation;
            block17: {
                NativeImage original = NativeImage.read((InputStream)in);
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
                    ResourceLocation generated = SpellIconRenderHelper.grayscaleResourceLocation(source);
                    mc.getTextureManager().register(generated, (AbstractTexture)new DynamicTexture(grayscale));
                    resourceLocation = generated;
                    if (original == null) break block17;
                }
                catch (Throwable throwable) {
                    if (original != null) {
                        try {
                            original.close();
                        }
                        catch (Throwable throwable2) {
                            throwable.addSuppressed(throwable2);
                        }
                    }
                    throw throwable;
                }
                original.close();
            }
            return resourceLocation;
        }
        catch (IOException | IllegalStateException ex) {
            FridayCultivationMod.LOGGER.warn("Failed to create grayscale spell icon for {}", (Object)source, (Object)ex);
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

