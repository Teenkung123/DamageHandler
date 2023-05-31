package com.dev.damagehandler.debuff;


import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.debuff.debuffs.DebuffStatus;
import org.bukkit.Bukkit;

import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Debuff {
    public static final Map<UUID, DebuffData> mapDebuffData = new HashMap<>();

    public DebuffData getDebuff(UUID uuid) {
        if (!mapDebuffData.containsKey(uuid)) {
            return new DebuffData(uuid);
        }
        else {
            return mapDebuffData.get(uuid);
        }
    }

    public static void startTick() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(DamageHandler.getInstance(), () -> {
            try {
                if (!mapDebuffData.isEmpty()) {
                    for (UUID keys : mapDebuffData.keySet()) {
                        for (DebuffStatus debuff : mapDebuffData.get(keys).getTotalDebuffs()) {
                            mapDebuffData.get(keys).reduceDuration(debuff.getUniqueId(), 1);
                        }
                    }
                }
            } catch (ConcurrentModificationException ignored) {}
        }, 1, 1);
    }
}
