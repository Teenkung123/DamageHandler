package com.dev.damagehandler.events;

import com.dev.damagehandler.utils.manager.PlayerDataManager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class JoinEvent implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        PlayerDataManager.addPlayer(event.getPlayer());
    }
    @EventHandler
    public void onQuit(PlayerJoinEvent event) { PlayerDataManager.removePlayer(event.getPlayer()); }



}
