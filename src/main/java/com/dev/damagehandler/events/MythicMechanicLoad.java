package com.dev.damagehandler.events;

import com.dev.damagehandler.mechanics.damage_element;
import com.dev.damagehandler.mechanics.reduce_defense;
import com.dev.damagehandler.mechanics.reduce_resistance;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMechanicLoad implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if(event.getMechanicName().equalsIgnoreCase("damage_element"))	{
            event.register(new damage_element(event.getConfig()));
        }
        else if (event.getMechanicName().equalsIgnoreCase("reduce_defense"))	{
            event.register(new reduce_defense(event.getConfig()));
        }
        else if (event.getMechanicName().equalsIgnoreCase("reduce_resistance"))	{
            event.register(new reduce_resistance(event.getConfig()));
        }
    }

}
