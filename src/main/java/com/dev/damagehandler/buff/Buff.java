package com.dev.damagehandler.buff;


import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.buff.buffs.BuffStatus;
import org.bukkit.Bukkit;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Store the data of Buffs of all entities
 * And count down tick of buff time
 */
public class Buff {
    public static final Map<UUID, BuffData> mapBuffData = new HashMap<>();

    public BuffData getBuff(UUID uuid) {
        if (!mapBuffData.containsKey(uuid)) {
            return new BuffData(uuid);
        }
        else {
            return mapBuffData.get(uuid);
        }
    }

    public static void startTick() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DamageHandler.getInstance(), () -> {
            try {
                if (!mapBuffData.isEmpty()) {
                    for (UUID keys : mapBuffData.keySet()) {
                        for (BuffStatus buff : mapBuffData.get(keys).getTotalBuffs()) {
                            mapBuffData.get(keys).reduceDuration(buff.getUniqueId(), 1);
                        }
                    }
                }
            } catch (ConcurrentModificationException ignored) {}
        }, 1, 1);
    }
}
