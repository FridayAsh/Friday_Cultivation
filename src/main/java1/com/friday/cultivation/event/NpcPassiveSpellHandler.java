package com.friday.cultivation.event;

import com.friday.cultivation.QiElement;
import com.friday.cultivation.entity.spell.SwordAuraEntity;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.physique.Physique;
import com.friday.cultivation.physique.PhysiqueBonusHelper;
import com.friday.cultivation.spell.Spell;
import com.friday.cultivation.technique.Technique;
import com.friday.cultivation.util.SpellScalingHelper;
import java.util.ArrayList;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.enchantment.FrostWalkerEnchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class NpcPassiveSpellHandler {
    private static final int SLOW_REGEN_INTERVAL_TICKS = 100;
    private static final long SLOW_REGEN_QI_COST = 5L;
    private static final int FROST_WALKER_INTERVAL_TICKS = 5;
    private static final int FROST_WALKER_RADIUS = 2;
    private static final int CLEAR_MIND_INTERVAL_TICKS = 20;
    private static final long CLEAR_MIND_QI_COST = 100L;
    private static final int SPIRIT_VISION_INTERVAL_TICKS = 40;
    private static final int SWORD_AURA_INTERVAL_TICKS = 24;
    private static final int SWORD_AURA_QI_COST = 50;
    private static final double SWORD_AURA_RANGE = 48.0;

    private NpcPassiveSpellHandler() {
    }

    public static void tick(WanderingCultivatorEntity npc) {
        if (npc.isNpcSoulState()) {
            return;
        }
        Level level = npc.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel sl = (ServerLevel) level;
        if (!npc.isAlive()) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) npc)) {
            return;
        }
        if (TimeStasisHandler.isEntityStopped((Entity) npc)) {
            return;
        }
        NpcPassiveSpellHandler.handleSlowRegen(npc, sl);
        NpcPassiveSpellHandler.handleFrostWalker(npc);
        NpcPassiveSpellHandler.handleClearMindIncantation(npc, sl);
        NpcPassiveSpellHandler.handleSpiritVision(npc);
        NpcPassiveSpellHandler.handleImmortalIncantation(npc);
        NpcPassiveSpellHandler.handlePhysiquePassives(npc);
        NpcPassiveSpellHandler.handleSwordAura(npc, sl);
    }

    private static void handleSlowRegen(WanderingCultivatorEntity npc, ServerLevel level) {
        if (!NpcPassiveSpellHandler.knows(npc, Spell.SLOW_REGEN)) {
            return;
        }
        if (npc.tickCount % 100 != 0) {
            return;
        }
        if (npc.getHealth() >= npc.getMaxHealth()) {
            return;
        }
        long cost = NpcPassiveSpellHandler.passiveCost(npc, null, 5L);
        if (npc.getCurrentQi() < cost) {
            return;
        }
        npc.deductQi(cost);
        npc.heal(1.0f);
        level.sendParticles((ParticleOptions) ParticleTypes.HEART, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.75, npc.getZ(), 2, 0.25, 0.2, 0.25, 0.01);
    }

    private static void handleFrostWalker(WanderingCultivatorEntity npc) {
        if (!NpcPassiveSpellHandler.knows(npc, Spell.FROST_WALKER)) {
            return;
        }
        if (npc.tickCount % 5 != 0) {
            return;
        }
        if (!npc.onGround()) {
            return;
        }
        BlockPos pos = npc.blockPosition();
        FrostWalkerEnchantment.onEntityMoved((LivingEntity) npc, (Level) npc.level(), (BlockPos) pos, 2);
    }

    private static void handleClearMindIncantation(WanderingCultivatorEntity npc, ServerLevel level) {
        if (!NpcPassiveSpellHandler.knows(npc, Spell.CLEAR_MIND_INCANTATION)) {
            return;
        }
        if (npc.tickCount % 20 != 0) {
            return;
        }
        ArrayList<MobEffect> harmful = new ArrayList<MobEffect>();
        for (MobEffectInstance effect : npc.getActiveEffects()) {
            if (effect.getEffect().getCategory() != MobEffectCategory.HARMFUL) continue;
            harmful.add(effect.getEffect());
        }
        if (harmful.isEmpty()) {
            return;
        }
        boolean changed = false;
        for (MobEffect effect : harmful) {
            long cost = NpcPassiveSpellHandler.passiveCost(npc, null, 100L);
            if (npc.getCurrentQi() < cost) break;
            npc.deductQi(cost);
            npc.removeEffect(effect);
            changed = true;
        }
        if (changed) {
            level.sendParticles((ParticleOptions) ParticleTypes.ENCHANT, npc.getX(), npc.getY() + (double) npc.getBbHeight() * 0.65, npc.getZ(), 10, 0.35, 0.35, 0.35, 0.08);
        }
    }

    private static void handleSpiritVision(WanderingCultivatorEntity npc) {
        if (!NpcPassiveSpellHandler.knows(npc, Spell.SPIRIT_VISION)) {
            return;
        }
        if (npc.tickCount % 40 != 0) {
            return;
        }
        npc.removeEffect(MobEffects.BLINDNESS);
        LivingEntity target = npc.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (npc.distanceToSqr((Entity) target) > 2304.0) {
            return;
        }
    }

    private static void handleImmortalIncantation(WanderingCultivatorEntity npc) {
        if (!NpcPassiveSpellHandler.hasImmortalCombo(npc)) {
            return;
        }
        npc.setRemainingFireTicks(0);
        if (npc.tickCount % 200 == 0) {
            npc.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 240, 0, false, false));
        }
    }

    private static void handlePhysiquePassives(WanderingCultivatorEntity npc) {
        if (npc.getPhysique().bonus().resistanceRegen() && npc.tickCount % 200 == 0) {
            npc.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 240, 0, false, false));
            npc.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 240, 0, false, false));
        }
    }

    private static void handleSwordAura(WanderingCultivatorEntity npc, ServerLevel level) {
        if (!NpcPassiveSpellHandler.knows(npc, Spell.SWORD_AURA)) {
            return;
        }
        if (!(npc.getMainHandItem().getItem() instanceof SwordItem)) {
            return;
        }
        if (npc.isUsingItem()) {
            return;
        }
        if ((npc.tickCount + npc.getId()) % 24 != 0) {
            return;
        }
        LivingEntity target = npc.getTarget();
        if (target == null || !target.isAlive()) {
            return;
        }
        if (npc.distanceToSqr((Entity) target) > 2304.0) {
            return;
        }
        long cost = NpcPassiveSpellHandler.passiveCost(npc, Spell.SWORD_AURA, 50L);
        if (npc.getCurrentQi() < cost) {
            return;
        }
        Vec3 eye = npc.getEyePosition();
        Vec3 dir = target.getEyePosition().subtract(eye);
        dir = dir.lengthSqr() < 1.0E-4 ? npc.getLookAngle() : dir.normalize();
        npc.getLookControl().setLookAt((Entity) target, 30.0f, 30.0f);
        npc.deductQi(cost);
        SwordAuraEntity aura = new SwordAuraEntity((Level) level, (LivingEntity) npc, eye.add(dir.scale(0.8)), dir, QiElement.METAL);
        aura.setDamageMultiplier((float) SpellScalingHelper.damageMultiplier((LivingEntity) npc, Spell.SWORD_AURA));
        level.addFreshEntity((Entity) aura);
        level.playSound(null, eye.x, eye.y, eye.z, SoundEvents.TRIDENT_THROW, SoundSource.HOSTILE, 0.65f, 1.45f);
    }

    @SubscribeEvent
    public static void onNpcEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
        if (npc.isNpcSoulState()) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) npc)) {
            return;
        }
        if (NpcPassiveSpellHandler.hasImmortalCombo(npc)) {
            event.setResult(Event.Result.DENY);
            return;
        }
        if (event.getEffectInstance().getEffect() == MobEffects.POISON && NpcPassiveSpellHandler.knows(npc, Spell.POISON_IMMUNITY)) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onNpcHurt(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
        if (npc.isNpcSoulState()) {
            return;
        }
        if (event.getAmount() <= 0.0f) {
            return;
        }
        if (TribulationHandler.isTribulationDamage(event.getSource())) {
            return;
        }
        if (SpiritLockHandler.isEntityLocked((Entity) npc)) {
            return;
        }
        double multiplier = NpcPassiveSpellHandler.immortalDamageTakenMultiplier(npc);
        if (multiplier != 1.0) {
            event.setAmount((float) ((double) event.getAmount() * multiplier));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void onNpcFireImmune(LivingHurtEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
        if (npc.isNpcSoulState()) {
            return;
        }
        if (!NpcPassiveSpellHandler.hasImmortalCombo(npc)) {
            return;
        }
        DamageSource source = event.getSource();
        if (source == null || !NpcPassiveSpellHandler.isFireDamage(source)) {
            return;
        }
        event.setCanceled(true);
        npc.setRemainingFireTicks(0);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onNpcKnockbackImmune(LivingKnockBackEvent event) {
        LivingEntity livingEntity = event.getEntity();
        if (!(livingEntity instanceof WanderingCultivatorEntity)) {
            return;
        }
        WanderingCultivatorEntity npc = (WanderingCultivatorEntity) livingEntity;
        if (npc.isNpcSoulState()) {
            return;
        }
        if (!NpcPassiveSpellHandler.hasImmortalCombo(npc)) {
            return;
        }
        event.setCanceled(true);
    }

    private static boolean knows(WanderingCultivatorEntity npc, Spell spell) {
        return NpcSpellCaster.knows(npc, spell);
    }

    private static long passiveCost(WanderingCultivatorEntity npc, Spell spell, long baseCost) {
        if (baseCost <= 0L) {
            return 0L;
        }
        double multiplier = 1.0;
        if (NpcPassiveSpellHandler.hasImmortalTechnique(npc)) {
            multiplier *= 0.5;
        }
        multiplier *= PhysiqueBonusHelper.generalQiCostMultiplier(npc.getPhysique());
        if ((multiplier *= PhysiqueBonusHelper.spellQiCostMultiplier(npc.getPhysique(), spell)) != 1.0) {
            return Math.max(1L, (long) Math.ceil((double) baseCost * multiplier));
        }
        return baseCost;
    }

    private static boolean hasImmortalTechnique(WanderingCultivatorEntity npc) {
        return Technique.IMMORTAL_INCANTATION.id().equals(npc.getTechniqueId());
    }

    private static boolean hasImmortalCombo(WanderingCultivatorEntity npc) {
        return NpcPassiveSpellHandler.hasImmortalTechnique(npc) && npc.getPhysique() == Physique.IMMORTAL_BODY;
    }

    private static double immortalDamageTakenMultiplier(WanderingCultivatorEntity npc) {
        double multiplier = 1.0;
        if (NpcPassiveSpellHandler.hasImmortalTechnique(npc) || npc.getSpellIds().contains(Spell.IMMORTAL_INCANTATION.id())) {
            multiplier *= 0.5;
        }
        return Double.isFinite(multiplier *= npc.getPhysique().bonus().damageTakenMult()) ? Math.max(0.0, multiplier) : 1.0;
    }

    private static boolean isFireDamage(DamageSource source) {
        return source.is(DamageTypes.IN_FIRE) || source.is(DamageTypes.ON_FIRE) || source.is(DamageTypes.LAVA) || source.is(DamageTypes.HOT_FLOOR) || source.is(DamageTypes.UNATTRIBUTED_FIREBALL) || source.is(DamageTypes.FIREBALL);
    }
}
