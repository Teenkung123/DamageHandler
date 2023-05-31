package com.dev.damagehandler.utils.inflect;

import com.dev.damagehandler.DamageHandler;
import org.bukkit.Bukkit;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElementalInflect {

    protected static final Map<UUID, ElementalInflectData> entityElementInflect = new HashMap<>();

    public ElementalInflectData getInflect(UUID uuid) {
        if (!entityElementInflect.containsKey(uuid)) {
            return new ElementalInflectData(uuid);
        }
        else {
            return entityElementInflect.get(uuid);
        }
    }

    public static void startTick() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DamageHandler.getInstance(), () -> {
            try {
                if (!entityElementInflect.isEmpty()) {
                    for (UUID keys : entityElementInflect.keySet()) {
                        for (String element : entityElementInflect.get(keys).getMapElementInflect().keySet()) {
                            entityElementInflect.get(keys).reduceInflect(element, 1);
                        }
                    }
                }
            } catch (ConcurrentModificationException ignored) {}
        }, 1, 1);
    }
}