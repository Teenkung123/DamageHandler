package com.dev.damagehandler.utils.manager;

import org.bukkit.entity.Entity;

import java.util.HashMap;

public class EntityDataManager {

    private static HashMap<Entity, EntityManager> manager = new HashMap<>();

    public static void addEntity(Entity entity) {
        manager.put(entity, new EntityManager(entity));
    }

    public static void removeEntity(Entity entity) {
        manager.remove(entity);
    }

    public static EntityManager getEntityManager(Entity entity) {
        if (!manager.containsKey(entity)) {
            addEntity(entity);
        }
        return manager.get(entity);
    }

    public static HashMap<Entity, EntityManager> getManager() { return manager; }

}
