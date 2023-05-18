package com.dev.damagehandler.events;

import com.dev.damagehandler.mechanics.ElementDamage;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMechanicLoad implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if(event.getMechanicName().equalsIgnoreCase("ElementDamage"))	{
            event.register(new ElementDamage(event.getConfig()));
        }
    }

}
