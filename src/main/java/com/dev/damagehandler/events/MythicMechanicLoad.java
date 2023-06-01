package com.dev.damagehandler.events;

import com.dev.damagehandler.mechanics.elemental_damage;
import com.dev.damagehandler.mechanics.elemental_shield;
import com.dev.damagehandler.mechanics.reduce_defense;
import com.dev.damagehandler.mechanics.reduce_resistance;
import io.lumine.mythic.bukkit.events.MythicMechanicLoadEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class MythicMechanicLoad implements Listener {

    @EventHandler
    public void onMythicMechanicLoad(MythicMechanicLoadEvent event) {
        if(event.getMechanicName().equalsIgnoreCase("elemental_damage"))	{
            event.register(new elemental_damage(event.getConfig()));
        }
        else if (event.getMechanicName().equalsIgnoreCase("reduce_defense"))	{
            event.register(new reduce_defense(event.getConfig()));
        }
        else if (event.getMechanicName().equalsIgnoreCase("reduce_resistance"))	{
            event.register(new reduce_resistance(event.getConfig()));
        }
        else if (event.getMechanicName().equalsIgnoreCase("elemental_shield"))	{
            event.register(new elemental_shield(event.getConfig()));
        }
    }

}
