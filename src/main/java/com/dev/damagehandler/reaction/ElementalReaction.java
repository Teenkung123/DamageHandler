package com.dev.damagehandler.reaction;

import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public abstract class ElementalReaction {

    private final String id;
    private final String aura;
    private final String trigger;

    public ElementalReaction(String id, String aura, String trigger) {
        this.id = id;
        this.aura = aura;
        this.trigger = trigger;
    }

    public String getId() {
        return this.id;
    }
    public String getAura() {
        return this.aura;
    }
    public String getTrigger() {
        return this.trigger;
    }

    public abstract void trigger(DamagePacket damage, LivingEntity entity, Entity damager);

}
