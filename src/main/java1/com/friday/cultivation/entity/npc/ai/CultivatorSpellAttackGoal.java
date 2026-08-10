package com.friday.cultivation.entity.npc.ai;

import com.friday.cultivation.entity.npc.NpcSpellCaster;
import com.friday.cultivation.entity.npc.WanderingCultivatorEntity;
import com.friday.cultivation.spell.Spell;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

public class CultivatorSpellAttackGoal extends Goal {
    private static final double MAX_RANGE = 40.0;
    private static final int HIGH_IMPACT_MEGA_COOLDOWN_TICKS = 500;
    private final WanderingCultivatorEntity npc;
    private int cooldown = 0;
    private int highImpactMegaCooldown = 0;

    public CultivatorSpellAttackGoal(WanderingCultivatorEntity npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {
        if (this.npc.isTradingFreeze()) {
            return false;
        }
        if (this.cooldown > 0) {
            --this.cooldown;
            if (this.highImpactMegaCooldown > 0) {
                --this.highImpactMegaCooldown;
            }
            return false;
        }
        if (this.highImpactMegaCooldown > 0) {
            --this.highImpactMegaCooldown;
        }
        if (this.npc.isUsingItem()) {
            return false;
        }
        if (!this.hasAnyCastableSpell()) {
            return false;
        }
        LivingEntity target = this.npc.getTarget();
        boolean hasTarget = target != null && target.isAlive() && (double) this.npc.distanceTo((Entity) target) <= 40.0;
        boolean lowHp = this.npc.getHealth() < this.npc.getMaxHealth() * 0.5f;
        return hasTarget || lowHp && this.hasAnyAffordableSelfBuff();
    }

    @Override
    public void start() {
        Spell pick;
        Spell mega;
        Spell selfPick;
        LivingEntity target = this.npc.getTarget();
        boolean lowHp = this.npc.getHealth() < this.npc.getMaxHealth() * 0.5f;
        if (lowHp && (selfPick = this.pickAffordable(this.getCastableLearned(true))) != null) {
            NpcSpellCaster.cast(this.npc, selfPick, (LivingEntity) this.npc);
            this.cooldown = 30 + this.npc.getRandom().nextInt(20);
            return;
        }
        if (target != null && this.shouldCharge(target) && (mega = this.pickChargeable()) != null) {
            boolean ok = NpcSpellCaster.castMega(this.npc, mega, target);
            if (ok && NpcSpellCaster.isHighImpactMega(mega)) {
                this.highImpactMegaCooldown = 500 + this.npc.getRandom().nextInt(200);
                this.cooldown = 140 + this.npc.getRandom().nextInt(80);
            } else {
                this.cooldown = ok ? 70 + this.npc.getRandom().nextInt(40) : 30;
            }
            return;
        }
        if (target != null && (pick = this.pickAffordable(this.getCastableLearned(false))) != null) {
            boolean ok = NpcSpellCaster.cast(this.npc, pick, target);
            this.cooldown = ok ? 10 + this.npc.getRandom().nextInt(20) : 20;
            return;
        }
        this.cooldown = 30;
    }

    private boolean shouldCharge(LivingEntity target) {
        int combatTicks = this.npc.getCombatTicks();
        float roll = this.npc.getRandom().nextFloat();
        if (combatTicks > 400 && roll < 0.1f) {
            return true;
        }
        if (target.getMaxHealth() > this.npc.getMaxHealth() * 1.5f && roll < 0.12f) {
            return true;
        }
        if (combatTicks > 240 && target.getHealth() > target.getMaxHealth() * 0.5f && roll < 0.08f) {
            return true;
        }
        return roll < 0.01f;
    }

    private Spell pickChargeable() {
        long qi = this.npc.getCurrentQi();
        ArrayList<Spell> chargeables = new ArrayList<>();
        for (String id : this.npc.getSpellIds()) {
            Spell s = Spell.byId(id);
            if (s == null
                    || !NpcSpellCaster.CHARGEABLE_SUPPORTED.contains(s)
                    || qi < NpcSpellCaster.megaCost(this.npc, s)
                    || NpcSpellCaster.isHighImpactMega(s) && (this.highImpactMegaCooldown > 0
                    || NpcSpellCaster.hasNearbyHighImpactSpell(this.npc, 160.0))) continue;
            chargeables.add(s);
        }
        if (chargeables.isEmpty()) {
            return null;
        }
        return chargeables.get(this.npc.getRandom().nextInt(chargeables.size()));
    }

    private Spell pickAffordable(List<Spell> candidates) {
        ArrayList<Spell> affordable = new ArrayList<>();
        long qi = this.npc.getCurrentQi();
        for (Spell s : candidates) {
            if (NpcSpellCaster.spellCost(this.npc, s) > qi) continue;
            affordable.add(s);
        }
        if (affordable.isEmpty()) {
            return null;
        }
        return affordable.get(this.npc.getRandom().nextInt(affordable.size()));
    }

    private List<Spell> getCastableLearned(boolean selfBuffOnly) {
        ArrayList<Spell> result = new ArrayList<>();
        for (String id : this.npc.getSpellIds()) {
            Spell s = Spell.byId(id);
            if (s == null || !NpcSpellCaster.SUPPORTED.contains(s)) continue;
            if (selfBuffOnly) {
                if (!NpcSpellCaster.SELF_BUFF.contains(s)) continue;
                result.add(s);
                continue;
            }
            if (!NpcSpellCaster.isCombatSpell(s)) continue;
            result.add(s);
        }
        return result;
    }

    private boolean hasAnyCastableSpell() {
        for (String id : this.npc.getSpellIds()) {
            Spell s = Spell.byId(id);
            if (s == null || !NpcSpellCaster.isCombatSpell(s) && !NpcSpellCaster.SELF_BUFF.contains(s)) continue;
            return true;
        }
        return false;
    }

    private boolean hasAnyAffordableSelfBuff() {
        long qi = this.npc.getCurrentQi();
        for (String id : this.npc.getSpellIds()) {
            Spell s = Spell.byId(id);
            if (s == null || !NpcSpellCaster.SELF_BUFF.contains(s) || NpcSpellCaster.spellCost(this.npc, s) > qi) continue;
            return true;
        }
        return false;
    }
}
