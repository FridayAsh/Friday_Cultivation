package com.friday.cultivation.event;

import com.friday.cultivation.CultivationCapability;
import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.spell.Spell;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * 血咒 - 严格 1:1 复刻原模组
 * 混淆名映射: m_21223_=getHealth, m_6469_=hurt, m_6084_=isAlive, m_5634_=heal,
 *             m_7639_=getEntity, m_7640_=getDirectEntity, m_19749_=getOwner,
 *             m_9236_=level, m_8767_=sendParticles, m_20185_=getX,
 *             m_20186_=getY, m_20189_=getZ, m_20206_=getBbHeight,
 *             m_6263_=playSound, f_19797_=tickCount,
 *             f_123750_=DAMAGE_INDICATOR, f_11871_=PLAYER_HURT
 */
@Mod.EventBusSubscriber(modid = "friday_cultivation")
public final class BloodthirstCurseHandler {
    private static final float HEAL_RATIO = 0.5f;
    private static final ThreadLocal<Boolean> SUPPRESS_DAMAGE_EVENT_REWARD = ThreadLocal.withInitial(() -> false);

    private BloodthirstCurseHandler() {
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onLivingDamage(LivingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (SUPPRESS_DAMAGE_EVENT_REWARD.get().booleanValue()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target == null || event.getAmount() <= 0.0f) {
            return;
        }
        LivingEntity attacker = BloodthirstCurseHandler.resolveAttacker(event.getSource());
        if (attacker == null) {
            return;
        }
        float actualLost = Math.min(event.getAmount(), Math.max(0.0f, target.getHealth()));
        BloodthirstCurseHandler.rewardFromActualDamage(attacker, target, actualLost);
    }

    /*
     * WARNING - Removed try catching itself - possible behaviour change.
     */
    public static boolean hurtWithoutEventReward(LivingEntity target, DamageSource source, float amount) {
        SUPPRESS_DAMAGE_EVENT_REWARD.set(true);
        try {
            boolean bl = target.hurt(source, amount);
            return bl;
        }
        finally {
            SUPPRESS_DAMAGE_EVENT_REWARD.set(false);
        }
    }

    public static void rewardFromActualDamage(@Nullable LivingEntity attacker, @Nullable LivingEntity target, float actualLost) {
        if (attacker == null || target == null || actualLost <= 0.0f) {
            return;
        }
        if (!attacker.isAlive() || attacker == target) {
            return;
        }
        if (!SoulStateHandler.canOrdinaryAffect((Entity)attacker, (Entity)target)) {
            return;
        }
        if (!BloodthirstCurseHandler.hasBloodthirst(attacker)) {
            return;
        }
        float heal = actualLost * 0.5f;
        if (heal <= 0.0f) {
            return;
        }
        attacker.heal(heal);
        BloodthirstCurseHandler.spawnFeedback(attacker, heal);
    }

    private static boolean hasBloodthirst(LivingEntity attacker) {
        if (SpiritLockHandler.isEntityLocked((Entity)attacker)) {
            return false;
        }
        if (attacker instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer)attacker;
            return CultivationCapability.get((Player)player).map(data -> data.isSpellEnabled(Spell.BLOODTHIRST_CURSE)).orElse(false);
        }
        if (attacker instanceof WanderingCultivatorEntity) {
            WanderingCultivatorEntity npc = (WanderingCultivatorEntity)attacker;
            if (npc.isNpcSoulState()) {
                return false;
            }
            return NpcSpellCaster.knows(npc, Spell.BLOODTHIRST_CURSE);
        }
        return false;
    }

    @Nullable
    private static LivingEntity resolveAttacker(DamageSource source) {
        Projectile projectile;
        Entity entity;
        if (source == null) {
            return null;
        }
        Entity cause = source.getEntity();
        if (cause instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)cause;
            return living;
        }
        Entity direct = source.getDirectEntity();
        if (direct instanceof Projectile && (entity = (projectile = (Projectile)direct).getOwner()) instanceof LivingEntity) {
            LivingEntity owner = (LivingEntity)entity;
            return owner;
        }
        if (direct instanceof LivingEntity) {
            LivingEntity living = (LivingEntity)direct;
            return living;
        }
        return null;
    }

    private static void spawnFeedback(LivingEntity attacker, float heal) {
        Level level = attacker.level();
        if (!(level instanceof ServerLevel)) {
            return;
        }
        ServerLevel level2 = (ServerLevel)level;
        int count = Math.max(2, Math.min(8, (int)Math.ceil(heal)));
        level2.sendParticles((ParticleOptions)ParticleTypes.DAMAGE_INDICATOR, attacker.getX(), attacker.getY() + (double)attacker.getBbHeight() * 0.75, attacker.getZ(), count, 0.35, 0.35, 0.35, 0.03);
        if (attacker.tickCount % 10 == 0) {
            level2.playSound(null, attacker.getX(), attacker.getY(), attacker.getZ(), SoundEvents.PLAYER_HURT, SoundSource.PLAYERS, 0.25f, 0.65f);
        }
    }
}
