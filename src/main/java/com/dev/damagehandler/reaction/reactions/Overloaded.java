package com.dev.damagehandler.reaction.reactions;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.reaction.ElementalReaction;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.scheduler.BukkitTask;

import java.util.concurrent.atomic.AtomicInteger;

public class Overloaded extends ElementalReaction {
    public Overloaded() {
        super("OVERLOADED", ConfigLoader.getAuraElement("OVERLOADED"), ConfigLoader.getTriggerElement("OVERLOADED"));
    }

    @Override
    public void trigger(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, Entity damager) {
        if (damage.getElement() == null) return;
        getAuraData(entity.getUniqueId()).removeAura(getTrigger());
        double final_gauge_unit = gauge_unit * ConfigLoader.getGaugeUnitTax(getId());
        getAuraData(entity.getUniqueId()).reduceAura(getAura(), final_gauge_unit);

        displayIndicator(getDisplay(), entity);
        displayIndicator("&c100", entity);

        damage(100, damager, entity);
    }
}
