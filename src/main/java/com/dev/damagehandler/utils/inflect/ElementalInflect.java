package com.dev.damagehandler.utils.inflect;

import com.dev.damagehandler.DamageHandler;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElementalInflect {

    protected static final Map<UUID, ElementalInflectData> entityElementInflect = new HashMap<>();

    public ElementalInflectData getInflect(UUID uuid) {
        if (entityElementInflect.containsKey(uuid)) {
            return new ElementalInflectData(uuid);
        }
        else {
            return entityElementInflect.get(uuid);
        }
    }

    public static void startTick() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DamageHandler.getInstance(), ()->{
            if (!entityElementInflect.isEmpty()) {
                for (UUID keys : entityElementInflect.keySet()) {
                    for (String element : entityElementInflect.get(keys).getMapElementInflect().keySet()) {
                        entityElementInflect.get(keys).reduceInflect(element, 1);
                    }
                }
            }
        }, 1, 1);
    }
}
