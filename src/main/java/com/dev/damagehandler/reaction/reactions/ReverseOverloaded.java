package com.dev.damagehandler.reaction.reactions;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.reaction.ElementalReaction;
import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.utils.Utils;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ReverseOverloaded extends ElementalReaction {
    public ReverseOverloaded() {
        super("REVERSE_OVERLOADED", ConfigLoader.getAuraElement("REVERSE_OVERLOADED"), ConfigLoader.getTriggerElement("REVERSE_OVERLOADED"));
    }

    @Override
    public void trigger(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, Entity damager) {
        if (damage.getElement() == null) return;
        DamageHandler.getAura().getAura(entity.getUniqueId()).removeAura(getTrigger());
        double final_gauge_unit = gauge_unit * ConfigLoader.getGaugeUnitTax(getId());
        DamageHandler.getAura().getAura(entity.getUniqueId()).reduceAura(getAura(), final_gauge_unit);

        displayIndicator(getDisplay(), entity);
        displayIndicator("&c100", entity);

        damage(100, damager, entity);
    }
}
