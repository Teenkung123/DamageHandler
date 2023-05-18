package com.dev.damagehandler.utils.manager;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class PlayerDataManager {

    private static HashMap<Player, PlayerManager> manager = new HashMap<>();

    public static void addPlayer(Player player) {
        manager.put(player, new PlayerManager(player));
    }

    public static void removePlayer(Player player) {
        manager.remove(player);
    }

    public static PlayerManager getPlayerManager(Player player) {
        return manager.get(player);
    }

}
