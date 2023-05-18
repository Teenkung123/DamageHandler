package com.dev.damagehandler.events;

import com.dev.damagehandler.utils.manager.EntityDataManager;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.world.ChunkUnloadEvent;

public class EntityDeath implements Listener {

    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            EntityDataManager.removeEntity(event.getEntity());
        }
    }

    public void onEntityDespawn(ChunkUnloadEvent event) {
        for (Entity entity : event.getChunk().getEntities()) {
            if (!(entity instanceof Player)) {
                EntityDataManager.removeEntity(entity);
            }
        }
    }

}
