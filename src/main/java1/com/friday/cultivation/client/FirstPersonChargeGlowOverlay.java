package com.friday.cultivation.client;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.capability.CultivationData;
import com.friday.cultivation.spell.Spell;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * 第一人称蓄力发光覆盖 - 裂天剑气充能时，手持剑按充能比例放大发光渲染。
 * 完全照搬原 mod: xiaoxiang.cultivation.client.FirstPersonChargeGlowOverlay
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation", value = {Dist.CLIENT})
public final class FirstPersonChargeGlowOverlay {
    private static final long CAP_QI = 3000L;

    private FirstPersonChargeGlowOverlay() {
    }

    @SubscribeEvent
    public static void onRenderHand(RenderHandEvent event) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (player == null || mc.level == null) {
            return;
        }
        if (event.getHand() != InteractionHand.MAIN_HAND) {
            return;
        }
        CultivationData data = CultivationCapability.get((Player) player).orElse(null);
        if (data == null || !data.isCharging()) {
            return;
        }
        Spell sp = Spell.byId(data.getChargingSpellId());
        if (sp != Spell.SKY_SPLITTING_SWORD_AURA) {
            return;
        }
        ItemStack held = event.getItemStack();
        if (held.isEmpty() || !(held.getItem() instanceof SwordItem)) {
            return;
        }
        long charged = data.getChargedQi();
        float frac = Math.min(1.0f, (float) charged / 3000.0f);
        float haloScale = 1.0f + 0.1f * frac;
        boolean isLeftHandedMain = player.getMainArm() == HumanoidArm.LEFT;
        ItemDisplayContext ctx = isLeftHandedMain ? ItemDisplayContext.FIRST_PERSON_LEFT_HAND : ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        PoseStack pose = event.getPoseStack();
        MultiBufferSource buf = event.getMultiBufferSource();
        int seed = player.getId() + ctx.ordinal();
        event.setCanceled(true);
        pose.pushPose();
        pose.scale(haloScale, haloScale, haloScale);
        mc.getItemRenderer().renderStatic((LivingEntity) player, held, ctx, isLeftHandedMain, pose, buf, (Level) mc.level, 0xF000F0, OverlayTexture.NO_OVERLAY, seed);
        pose.popPose();
        mc.getItemRenderer().renderStatic((LivingEntity) player, held, ctx, isLeftHandedMain, pose, buf, (Level) mc.level, 0xF000F0, OverlayTexture.NO_OVERLAY, seed);
    }
}
