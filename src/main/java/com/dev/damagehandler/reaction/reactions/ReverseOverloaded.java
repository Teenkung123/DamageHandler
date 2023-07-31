package com.dev.damagehandler.reaction.reactions;

import com.dev.damagehandler.reaction.ElementalReaction;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

public class ReverseOverloaded extends ElementalReaction {
    public ReverseOverloaded() {
        super("REVERSE_OVERLOADED", ConfigLoader.getAuraElement("REVERSE_OVERLOADED"), ConfigLoader.getTriggerElement("REVERSE_OVERLOADED"));
    }

    @Override
    public void trigger(DamagePacket damage, LivingEntity entity, Entity damager) {
        Bukkit.broadcastMessage(ChatColor.GOLD+"Reverse Overloaded "+(damager != null));
    }
}
