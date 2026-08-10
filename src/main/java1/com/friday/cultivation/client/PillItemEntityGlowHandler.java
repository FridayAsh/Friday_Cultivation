package com.friday.cultivation.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 丹药物品实体发光处理器 - 客户端渲染时给地上的丹药添加发光环。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.PillItemEntityGlowHandler
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PillItemEntityGlowHandler {
    private PillItemEntityGlowHandler() {
    }

    public static boolean isPill(ItemStack stack) {
        if (stack == null || stack.isEmpty()) return false;
        String id = stack.getItem().getClass().getSimpleName().toLowerCase();
        return id.contains("pill");
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) return;
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || player.level() == null) return;
        AABB scanBox = player.getBoundingBox().inflate(32.0);
        for (ItemEntity item : player.level().getEntitiesOfClass(ItemEntity.class, scanBox)) {
            ItemStack stack = item.getItem();
            if (!isPill(stack)) continue;
            if (player.level().getGameTime() % 5 != 0) continue;
            double baseY = item.getY() + 0.2;
            for (int i = 0; i < 2; ++i) {
                player.level().addParticle((ParticleOptions) ParticleTypes.ENTITY_EFFECT,
                        item.getX() + (Math.random() - 0.5) * 0.4, baseY + i * 0.2, item.getZ() + (Math.random() - 0.5) * 0.4,
                        0.9, 0.4, 0.7);
            }
        }
    }
}
