package com.dev.damagehandler.utils;

import com.dev.damagehandler.DamageHandler;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.Bukkit;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ElementalInflect {

    private static final Map<UUID, ElementalInflectData> entityElementInflect = new HashMap<>();

    @Nullable
    public ElementalInflectData getInflect(UUID uuid) {
        if (entityElementInflect.get(uuid) == null) {
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

    private static class ElementalInflectData {
        private final UUID uuid;
        private final Map<String, Long> mapElementInflect;
        public ElementalInflectData(UUID uuid) {
            this.uuid = uuid;
            this.mapElementInflect = new HashMap<>();
        }

        public Map<String, Long> getMapElementInflect() {
            return mapElementInflect;
        }

        public void addInflect(String element, long duration) {
            if (MythicLib.plugin.getElements().get(element) == null) return;
            if (this.mapElementInflect.containsKey(element) && this.mapElementInflect.get(element) < duration) return;

            this.mapElementInflect.put(element, duration);
            if (!entityElementInflect.containsKey(this.uuid)) entityElementInflect.put(this.uuid, this);
        }

        public void setInflect(String element, long duration) {
            if (MythicLib.plugin.getElements().get(element) == null) return;
            this.mapElementInflect.put(element, duration);
            if (!entityElementInflect.containsKey(this.uuid)) entityElementInflect.put(this.uuid, this);
        }

        public void removeInflect(String element) {
            if (MythicLib.plugin.getElements().get(element) == null) return;
            if (!this.mapElementInflect.containsKey(element)) return;

            this.mapElementInflect.remove(element);

            if (mapElementInflect.isEmpty()) entityElementInflect.remove(this.uuid);
        }

        public void reduceInflect(String element, long duration) {
            if (MythicLib.plugin.getElements().get(element) == null) return;
            if (!this.mapElementInflect.containsKey(element)) return;

            if (this.mapElementInflect.get(element) <= duration) {
                removeInflect(element);
            } else {
                setInflect(element, this.mapElementInflect.get(element) - duration);
            }
        }
    }
}
