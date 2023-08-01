package com.dev.damagehandler.aura;

import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.utils.Utils;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.dev.damagehandler.aura.Aura.entityAura;

/**
 * The data set of specified entity about Elemental Inflection
 */
public class AuraData {
    private final UUID uuid;
    private final Map<String, Long> mapAura;
    public AuraData(UUID uuid) {
        this.uuid = uuid;
        this.mapAura = new HashMap<>();
    }

    public Map<String, Long> getMapAura() {
        return mapAura;
    }

    /**
     * Get an inflected element icon
     * @return String inflected element icon
     */
    public String getAuraIcon() {
        StringBuilder sb = new StringBuilder();
        for (String elementID : mapAura.keySet()) {

            Element element = MythicLib.plugin.getElements().get(elementID);
            if (element == null) continue;

            sb.append(Utils.colorize(element.getColor()+element.getLoreIcon()+"&r"));
        }
        return sb.toString();
    }

    public void addAura(String element, double gauge_unit) {
        addAura(element, (long)(Math.floor(ConfigLoader.getAuraTime() * gauge_unit)));
    }

    public void reduceAura(String element, double gauge_unit) {
        reduceAura(element, (long)(Math.floor(ConfigLoader.getAuraTime() * gauge_unit)));
    }

    protected void addAura(String element, long duration) {
        if (MythicLib.plugin.getElements().get(element) == null) return;
        if (this.mapAura.containsKey(element) && duration < this.mapAura.get(element)) return;

        this.mapAura.put(element, duration);
        if (!entityAura.containsKey(this.uuid)) entityAura.put(this.uuid, this);
    }

    protected void setAura(String element, long duration) {
        if (MythicLib.plugin.getElements().get(element) == null) return;
        this.mapAura.put(element, duration);
        if (!entityAura.containsKey(this.uuid)) entityAura.put(this.uuid, this);
    }

    protected void removeAura(String element) {
        if (MythicLib.plugin.getElements().get(element) == null) return;
        if (!this.mapAura.containsKey(element)) return;

        this.mapAura.remove(element);

        if (mapAura.isEmpty()) entityAura.remove(this.uuid);
    }

    protected void reduceAura(String element, long duration) {
        if (MythicLib.plugin.getElements().get(element) == null) return;
        if (!this.mapAura.containsKey(element)) return;

        if (this.mapAura.get(element) <= duration) {
            removeAura(element);
        } else {
            setAura(element, this.mapAura.get(element) - duration);
        }
    }
}
