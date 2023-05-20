package com.dev.damagehandler.events.attack_handle;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * The class to attaching elements to any entity when
 * deal elemental damage
 */
public class InflectElement implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onAttack(AttackEvent event) {
        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (packet.getElement() == null) continue;
            DamageHandler.getElementalInflect().getInflect(event.getEntity().getUniqueId()).addInflect(packet.getElement().getId(), ConfigLoader.getInflectTime());
        }
    }
}
