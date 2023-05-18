package com.dev.damagehandler.utils.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerManager {

    private Player player;
    private final HashMap<String, Long> InflectedElements = new HashMap<>();
    public PlayerManager(Player player) {
        this.player = player;
    }

    public void addElement(String element, long time) {
        InflectedElements.put(element, System.currentTimeMillis() + time);
    }

    public void removeElement(String element) {
        InflectedElements.remove(element);
    }

    public HashMap<String, Long> getInflectedElements() {
        return InflectedElements;
    }
}
