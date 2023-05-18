package com.dev.damagehandler.utils.manager;

import org.bukkit.entity.Entity;

import java.util.HashMap;

public class EntityManager {

    private Entity entity;
    private final HashMap<String, Long> InflectedElements = new HashMap<>();
    public EntityManager(Entity entity) {
        this.entity = entity;
    }

    public void addElement(String element, long time) {
        InflectedElements.put(element, System.currentTimeMillis() + time);
    }

    public void removeElement(String element) {
        InflectedElements.remove(element);
    }
}
