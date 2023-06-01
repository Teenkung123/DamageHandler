package com.dev.damagehandler.events.attack_handle;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.debuff.debuffs.ElementalShield;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

public class ShieldRefutation implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onAttack(AttackEvent event) {

        Bukkit.broadcastMessage(DamageHandler.getDebuff().getDebuff(event.getEntity().getUniqueId()).getActivateDebuffs().toString());

        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (packet.getElement() == null) continue;

            ElementalShield shield = DamageHandler.getDebuff().getDebuff(event.getEntity().getUniqueId()).getActivateDebuff(ElementalShield.class, new String[]{"element"}, new String[]{packet.getElement().getId()});
            if (shield == null) continue;

            if (shield.getAmount() > packet.getValue()) {
                shield.setAmount(shield.getAmount() - packet.getValue());
                packet.setValue(0);
            } else if (shield.getAmount() < packet.getValue()) {
                DamageHandler.getDebuff().getDebuff(event.getEntity().getUniqueId()).removeDebuff(shield.getUniqueId());
                packet.setValue(packet.getValue() - shield.getAmount());
            } else {
                DamageHandler.getDebuff().getDebuff(event.getEntity().getUniqueId()).removeDebuff(shield.getUniqueId());
                packet.setValue(0);
            }
        }
    }
}
