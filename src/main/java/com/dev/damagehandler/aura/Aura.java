package com.dev.damagehandler.aura;

import com.dev.damagehandler.DamageHandler;
import org.bukkit.Bukkit;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Store the data of Elemental Inflection
 * of all entities
 * And count down tick of inflection time
 */
public class Aura {

    protected static final Map<UUID, AuraData> entityAura = new HashMap<>();

    public AuraData getAura(UUID uuid) {
        if (!entityAura.containsKey(uuid)) {
            return new AuraData(uuid);
        }
        else {
            return entityAura.get(uuid);
        }
    }

    public static void startTick() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DamageHandler.getInstance(), () -> {
            try {
                if (!entityAura.isEmpty()) {
                    for (UUID keys : entityAura.keySet()) {
                        for (String element : entityAura.get(keys).getMapAura().keySet()) {
                            entityAura.get(keys).reduceAura(element, 1);
                        }
                    }
                }
            } catch (ConcurrentModificationException ignored) {}
        }, 1, 1);
    }
}
