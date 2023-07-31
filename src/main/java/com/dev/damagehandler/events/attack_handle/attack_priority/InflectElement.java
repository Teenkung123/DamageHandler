package com.dev.damagehandler.events.attack_handle.attack_priority;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;

/**
 * The class to attaching elements to any entity when
 * deal elemental damage
 */
public class InflectElement {
    @AttackHandle(priority = 1)
    public void attack(AttackEvent event) {
        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (packet.getElement() == null) continue;
            if (!ConfigLoader.getInflectWhitelist().contains(packet.getElement().getId())) continue;
            DamageHandler.getElementalInflect().getInflect(event.getEntity().getUniqueId()).addInflect(packet.getElement().getId(), ConfigLoader.getInflectTime());
        }
    }
}
