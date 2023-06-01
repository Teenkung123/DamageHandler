package com.dev.damagehandler.inflect;

import com.dev.damagehandler.utils.Utils;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.dev.damagehandler.inflect.ElementalInflect.entityElementInflect;

/**
 * The data set of specified entity about Elemental Inflection
 */
public class ElementalInflectData {
    private final UUID uuid;
    private final Map<String, Long> mapElementInflect;
    public ElementalInflectData(UUID uuid) {
        this.uuid = uuid;
        this.mapElementInflect = new HashMap<>();
    }

    public Map<String, Long> getMapElementInflect() {
        return mapElementInflect;
    }

    /**
     * Get an inflected element icon
     * @return String inflected element icon
     */
    public String getInflectedIcon() {
        StringBuilder sb = new StringBuilder();
        for (String elementID : mapElementInflect.keySet()) {

            Element element = MythicLib.plugin.getElements().get(elementID);
            if (element == null) continue;

            sb.append(Utils.colorize(element.getColor()+element.getLoreIcon()+"&r"));
        }
        return sb.toString();
    }

    public void addInflect(String element, long duration) {
        if (MythicLib.plugin.getElements().get(element) == null) return;
        if (this.mapElementInflect.containsKey(element) && duration < this.mapElementInflect.get(element)) return;

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
