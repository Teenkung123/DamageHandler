package com.dev.damagehandler.reaction.reaction_type;

import com.dev.damagehandler.reaction.ElementalReaction;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public abstract class TriggerAuraReaction extends ElementalReaction {

    private final double gauge_unit_tax;

    public TriggerAuraReaction(String id, String display, String aura, String trigger, double gauge_unit_tax) {
        super(id, display, aura, trigger);
        this.gauge_unit_tax = gauge_unit_tax;
    }

    @Override
    public void trigger(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, @Nullable Entity damager, EntityDamageEvent.DamageCause damage_cause) {

        if (damage.getElement() == null) return;
        getAuraData(entity.getUniqueId()).removeAura(getTrigger());
        double final_gauge_unit = gauge_unit * gauge_unit_tax;
        getAuraData(entity.getUniqueId()).reduceAura(getAura(), final_gauge_unit);

        trigger(damage, gauge_unit, decay_rate, damager, entity, damage_cause);
    }

    public abstract void trigger(DamagePacket damage, double gauge_unit, String decay_rate, @Nullable Entity damager, LivingEntity entity, EntityDamageEvent.DamageCause damage_cause);
}
