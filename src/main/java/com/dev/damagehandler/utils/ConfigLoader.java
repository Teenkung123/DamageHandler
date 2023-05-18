package com.dev.damagehandler.utils;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.stats.BooleanStatRegister;
import com.dev.damagehandler.stats.DoubleStatRegister;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.element.Element;
import net.Indyuce.mmoitems.MMOItems;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.*;

public class ConfigLoader {

    private static Long inflectTime;
    private static String defaultElement;
    private static Map<String, String> elementalModifier = new HashMap<>();

    private static final HashMap<String, DoubleStatRegister> doubleStats = new HashMap<>();
    private static final HashMap<String, BooleanStatRegister> booleanStats = new HashMap<>();

    public static void loadConfig() {
        FileConfiguration config = DamageHandler.getInstance().getConfig();

        inflectTime = config.getLong("General.Inflect-Time", 200);
        defaultElement = config.getString("General.Default-Element");

        for (String damageCause : Objects.requireNonNull(config.getConfigurationSection("Elemental-Modifier")).getKeys(false)) {
            elementalModifier.put(damageCause, config.getString("Elemental-Modifier."+damageCause));
        }

        //This part will load all stats from the config and then register them to MMOItems
        registerDoubleStats(config.getConfigurationSection("Stats.DOUBLE_STAT"));
        registerBooleanStats(config.getConfigurationSection("Stats.BOOLEAN_STAT"));
    }

    public static void reloadConfig() {
        DamageHandler.getInstance().reloadConfig();
        loadConfig();
    }

    public static Long getInflectTime() {
        return inflectTime;
    }
    public static String getDefaultElement() {
        return defaultElement;
    }
    public static Map<String, String> getElementalModifier() { return elementalModifier; }

    private static void registerBooleanStats(ConfigurationSection stats) {
        if (stats != null) {
            for (String stat : stats.getKeys(false)) {
                ConfigurationSection section = stats.getConfigurationSection(stat);
                if (section != null) {
                    BooleanStatRegister booleanStat = new BooleanStatRegister(section.getName(), Material.getMaterial(section.getString("Icon.Material", "STONE")), section.getString("Icon.Name"), section.getStringList("Icon.Lore").toArray(new String[0]));
                    MMOItems.plugin.getStats().register(booleanStat);
                    booleanStats.put(booleanStat.getId(), booleanStat);
                }
            }
        }
    }

    private static void registerDoubleStats(ConfigurationSection stats) {

        if (stats != null) {

            for (Element element : MythicLib.plugin.getElements().getAll()) {

                for (String stat : stats.getKeys(false)) {
                    ConfigurationSection section = stats.getConfigurationSection(stat);
                    if (section != null) {
                        switch (section.getName()) {
                            case "AST_ELEMENTAL_RESISTANCE", "AST_ELEMENTAL_DAMAGE_BONUS" -> {


                                List<String> lore = section.getStringList("Icon.Lore");
                                List<String> replacedLore = new ArrayList<>();

                                for (String s : lore) {
                                    replacedLore.add(s.replace("%element%", element.getName()));
                                }

                                DoubleStatRegister doubleStat = new DoubleStatRegister(section.getName().replace("ELEMENTAL", element.getId()), element.getIcon(), section.getString("Icon.Name", "").replace("%element%", element.getName()), replacedLore.toArray(new String[0]));
                                MMOItems.plugin.getStats().register(doubleStat);
                                doubleStats.put(doubleStat.id, doubleStat);

                            }
                        }
                    }
                }
            }

            for (String stat : stats.getKeys(false)) {
                ConfigurationSection section = stats.getConfigurationSection(stat);
                if (section != null) {
                    switch (section.getName()) {
                        case "AST_ELEMENTAL_RESISTANCE", "AST_ELEMENTAL_DAMAGE_BONUS" -> {
                        }
                        default -> {
                            DoubleStatRegister doubleStat = new DoubleStatRegister(section.getName(), Material.getMaterial(section.getString("Icon.Material", "STONE")), section.getString("Icon.Name"), section.getStringList("Icon.Lore").toArray(new String[0]));
                            MMOItems.plugin.getStats().register(doubleStat);
                            doubleStats.put(doubleStat.id, doubleStat);
                        }
                    }
                }
            }
        }
    }


    private static void registerElementSkill(ConfigurationSection section) {

    }
}
