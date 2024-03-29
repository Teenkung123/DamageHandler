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

    private static String defaultElement;
    private static Map<String, String> elementalModifier = new HashMap<>();
    private static List<String> auraWhitelist;
    private static HashMap<String, Map<String, Integer>> reactionPriority = new HashMap<>();

    private static final HashMap<String, DoubleStatRegister> doubleStats = new HashMap<>();
    private static final HashMap<String, BooleanStatRegister> booleanStats = new HashMap<>();

    public static void loadConfig() {
        FileConfiguration config = DamageHandler.getInstance().getConfig();

        defaultElement = config.getString("General.default-element");
        auraWhitelist = config.getStringList("General.aura-whitelist");

        for (String damageCause : Objects.requireNonNull(config.getConfigurationSection("Elemental-Modifier")).getKeys(false)) {
            elementalModifier.put(damageCause, config.getString("Elemental-Modifier."+damageCause));
        }

        for (String element : Objects.requireNonNull(config.getConfigurationSection("Reaction-Priority")).getKeys(false)) {
            Map<String, Integer> priority = new HashMap<>();
            int i = 1;
            for (String reaction : config.getStringList("Reaction-Priority."+element)) {
                priority.put(reaction, i);
                i++;
            }
            reactionPriority.put(element, priority);
        }

        //This part will load all stats from the config and then register them to MMOItems
        registerDoubleStats(config.getConfigurationSection("Stats.DOUBLE_STAT"));
        registerBooleanStats(config.getConfigurationSection("Stats.BOOLEAN_STAT"));
    }

    public static void reloadConfig() {
        DamageHandler.getInstance().reloadConfig();
        loadConfig();
    }
    public static Map<String, Integer> getReactionPriority(String element) {
        return reactionPriority.get(element);
    }

    public static String getAuraElement(String reaction_id) {
        return DamageHandler.getInstance().getConfig().getString("Elemental-Reaction."+reaction_id+".aura-element");
    }

    public static String getReactionDisplay(String reaction_id) {
        return DamageHandler.getInstance().getConfig().getString("Elemental-Reaction."+reaction_id+".display");
    }
    public static String getTriggerElement(String reaction_id) {
        return DamageHandler.getInstance().getConfig().getString("Elemental-Reaction."+reaction_id+".trigger-element");
    }
    public static double getGaugeUnitTax(String reaction_id) {
        return DamageHandler.getInstance().getConfig().getDouble("Elemental-Reaction."+reaction_id+".gauge-unit-tax");
    }
    public static Long getDecayRate(String suffix) {
        return DamageHandler.getInstance().getConfig().getLong("General.decay-rate."+suffix);
    }
    public static Double getDefaultGaugeUnit() { return Double.parseDouble(Objects.requireNonNull(DamageHandler.getInstance().getConfig().getString("General.default-gauge-unit")).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[0]); }
    public static String getDefaultDecayRate() { return Objects.requireNonNull(DamageHandler.getInstance().getConfig().getString("General.default-gauge-unit")).split("(?<=\\D)(?=\\d)|(?<=\\d)(?=\\D)")[1]; }
    public static String getDefaultGauge() { return DamageHandler.getInstance().getConfig().getString("General.default-gauge-unit"); }
    public static String getSpecialAuraIcon(String aura_id) {
        return DamageHandler.getInstance().getConfig().getString("Special-Aura."+aura_id+".icon");
    }
    public static String getSpecialAuraColor(String aura_id) {
        return DamageHandler.getInstance().getConfig().getString("Special-Aura."+aura_id+".color");
    }

    public static String getDamageCalculation(String section) {
        return DamageHandler.getInstance().getConfig().getString("Damage-Calculation."+section);
    }

    public static String getDefaultElement() {
        return defaultElement;
    }
    public static Map<String, String> getElementalModifier() { return elementalModifier; }
    public static List<String> getAuraWhitelist() { return auraWhitelist; }
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
}
