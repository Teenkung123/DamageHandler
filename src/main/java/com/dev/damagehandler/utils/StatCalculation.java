package com.dev.damagehandler.utils;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.buff.buffs.DefenseReduction;
import com.dev.damagehandler.buff.buffs.ElementalResistanceReduction;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.damage.DamagePacket;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.Objects;
import java.util.UUID;

public class StatCalculation {

    public static double getFinalDamage(UUID victim, DamagePacket damage_packet) {
        String formula = ConfigLoader.getDamageCalculation("final-damage");
        Expression expression = new ExpressionBuilder(formula)
                .variables("total_damage", "defense_multiplier", "resistance_multiplier", "level_multiplier")
                .build()
                .setVariable("total_damage", getTotalDamage(damage_packet.getValue(), 0, 0, 0, 0))
                .setVariable("defense_multiplier", getDefenseMultiplier(victim))
                .setVariable("resistance_multiplier", damage_packet.getElement() != null ? getResistanceMultiplier(victim, damage_packet.getElement().getId()) : 1)
                .setVariable("level_multiplier", getLevelDifferentMultiplier(victim));

        return expression.evaluate();
    }

    public static double getFinalDamage(UUID attacker, UUID victim, DamagePacket damage_packet, boolean crit) {
        String formula = ConfigLoader.getDamageCalculation("final-damage");
        Expression expression = new ExpressionBuilder(formula)
                .variables("total_damage", "defense_multiplier", "resistance_multiplier", "level_multiplier")
                .build()
                .setVariable("total_damage", getTotalDamage(attacker, damage_packet, crit))
                .setVariable("defense_multiplier", getDefenseMultiplier(attacker, victim))
                .setVariable("resistance_multiplier", damage_packet.getElement() != null ? getResistanceMultiplier(victim, damage_packet.getElement().getId()) : 1)
                .setVariable("level_multiplier", getLevelDifferentMultiplier(attacker, victim));

        return expression.evaluate();
    }

    public static double getTotalDamage(UUID uuid, DamagePacket damage_packet, boolean crit) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        double damage_amount = damage_packet.getValue();
        double attack_buff_percent = 0;
        double attack_buff = 0;
        double elemental_damage_bonus = 0;
        double all_elemental_damage_bonus = 0;

        if (entity instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            PlayerStats playerStats = playerData.getStats();

            damage_amount = crit ? damage_packet.getValue() * (1 + (playerStats.getStat("AST_CRITICAL_DAMAGE")/100)) : damage_packet.getValue();
            attack_buff = playerStats.getStat("AST_ATTACK_DAMAGE_BUFF");
            attack_buff_percent = playerStats.getStat("AST_ATTACK_DAMAGE_BUFF_PERCENT");
            elemental_damage_bonus = (damage_packet.getElement() == null) ? 0 : playerStats.getStat("AST_"+damage_packet.getElement()+"_DAMAGE_BONUS");
            all_elemental_damage_bonus = playerStats.getStat("AST_ALL_ELEMENTAL_DAMAGE_BONUS");
        }
        return getTotalDamage(damage_amount, attack_buff_percent, attack_buff, elemental_damage_bonus, all_elemental_damage_bonus);
    }

    public static double getTotalDamage(double damage_amount, double attack_buff_percent, double attack_buff, double elemental_damage_bonus, double all_elemental_damage_bonus) {


        String formula = ConfigLoader.getDamageCalculation("total-damage");
        Expression expression = new ExpressionBuilder(formula)
                .variables("damage", "attack_buff_percent", "attack_buff", "elemental_damage_bonus", "all_elemental_damage_bonus")
                .build()
                .setVariable("damage", damage_amount)
                .setVariable("attack_buff_percent", attack_buff_percent)
                .setVariable("attack_buff", attack_buff)
                .setVariable("elemental_damage_bonus", elemental_damage_bonus)
                .setVariable("all_elemental_damage_bonus", all_elemental_damage_bonus);

        return expression.evaluate();
    }

    public static double getDefenseMultiplier(UUID victim) {
        return getDefenseMultiplier(victim, 0, 1);
    }

    public static double getDefenseMultiplier(UUID attacker, UUID victim) {
        Entity entity = Bukkit.getEntity(attacker);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        double ignore_defense = 0;
        int level;
        if (entity instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            PlayerStats playerStats = playerData.getStats();

            ignore_defense = playerStats.getStat("AST_IGNORE_DEFENSE");
            level = playerData.getLevel();
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(attacker).orElse(null);
            level = mythicMob != null ? (int) mythicMob.getLevel() : 1 ;
        }

        return getDefenseMultiplier(victim, ignore_defense, level);
    }

    public static double getDefenseMultiplier(UUID uuid, double ignore_defense, int attacker_level) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        double defense = getDefense(uuid);
        return getDefenseMultiplier(defense, ignore_defense, attacker_level);
    }

    public static double getDefenseMultiplier(double defense, double ignore_defense, int attacker_level) {

        String formula = ConfigLoader.getDamageCalculation("defense-multiplier");
        Expression expression = new ExpressionBuilder(formula)
                .variables("attacker_ignore_defense", "victim_defense", "attacker_level")
                .build()
                .setVariable("attacker_ignore_defense", ignore_defense)
                .setVariable("victim_defense", defense)
                .setVariable("attacker_level", attacker_level);

        return expression.evaluate();
    }

    public static double getResistanceMultiplier(UUID uuid, String element) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        double elemental_resistance = getResistance(uuid, element);
        return getResistanceMultiplier(elemental_resistance);
    }

    public static double getResistanceMultiplier(double elemental_resistance) {

        String formula = "0";
        for (String s : Objects.requireNonNull(DamageHandler.getInstance().getConfig().getConfigurationSection("Damage-Calculation.resistance-multiplier")).getKeys(false)) {
            if (s.contains("<=")) {
                if (elemental_resistance <= Double.parseDouble(s.split("<=")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            } else if (s.contains(">=")) {
                if (elemental_resistance >= Double.parseDouble(s.split(">=")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            } else if (s.contains("<")) {
                if (elemental_resistance < Double.parseDouble(s.split("<")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            } else if (s.contains(">")) {
                if (elemental_resistance > Double.parseDouble(s.split(">")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            } else if (s.contains("!=")) {
                if (elemental_resistance != Double.parseDouble(s.split("!=")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            } else if (s.contains("=")) {
                if (elemental_resistance == Double.parseDouble(s.split("=")[1])) {
                    formula = ConfigLoader.getDamageCalculation("resistance-multiplier."+s);
                    break;
                }
            }
        }
        Expression expression = new ExpressionBuilder(formula)
                .variables("elemental_resistance")
                .build()
                .setVariable("elemental_resistance", elemental_resistance);

        return expression.evaluate();
    }

    public static double getLevelDifferentMultiplier(UUID victim) {
        Entity entity = Bukkit.getEntity(victim);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        int level;
        if (entity instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            level = playerData.getLevel();
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(victim).orElse(null);
            level = mythicMob != null ? (int) mythicMob.getLevel() : 1;
        }

        return getLevelDifferentMultiplier(entity instanceof Player ? "player" : "mob", 1, level);
    }

    public static double getLevelDifferentMultiplier(UUID attacker, UUID victim) {

        Entity entity1 = Bukkit.getEntity(attacker);
        if (entity1 == null) return 0;
        if (!entity1.isValid()) return 0;

        Entity entity2 = Bukkit.getEntity(victim);
        if (entity2 == null) return 0;
        if (!entity2.isValid()) return 0;

        int attacker_level;
        int victim_level;
        if (entity1 instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            attacker_level = playerData.getLevel();
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(attacker).orElse(null);
            attacker_level = mythicMob != null ? (int) mythicMob.getLevel() : 1;
        }
        if (entity2 instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            victim_level = playerData.getLevel();
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(victim).orElse(null);
            victim_level = mythicMob != null ? (int) mythicMob.getLevel() : 1;
        }

        return getLevelDifferentMultiplier(entity2 instanceof Player ? "player" : "mob", attacker_level, victim_level);
    }

    public static double getLevelDifferentMultiplier(String victimType, int attacker_level, int victim_level) {

        String formula = victimType.equals("player") ? ConfigLoader.getDamageCalculation("level-multiplier.player") : ConfigLoader.getDamageCalculation("level-multiplier.mob");
        Expression expression = new ExpressionBuilder(formula)
                .variables("attacker_level", "victim_level")
                .build()
                .setVariable("attacker_level", attacker_level)
                .setVariable("victim_level", victim_level);

        return expression.evaluate();
    }

    public static double getResistance(UUID uuid, String element) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        ElementalResistanceReduction er = DamageHandler.getBuff().getBuff(uuid).getActivateBuff(ElementalResistanceReduction.class, new String[]{"element"}, new String[]{element});
        double elemental_resistance;

        if (entity instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            PlayerStats playerStats = playerData.getStats();

            elemental_resistance = playerStats.getStat("AST_"+element+"_RESISTANCE");
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(uuid).orElse(null);
            elemental_resistance = (mythicMob != null) ? mythicMob.getVariables().getFloat("AST_"+element+"_RESISTANCE") : 0;
        }

        return getResistance(elemental_resistance, er == null ? 0 : er.getAmount());
    }

    public static double getResistance(double pure_resistance, double resistance_reduction) {
        return pure_resistance - (resistance_reduction/100 * Math.abs(pure_resistance));
    }

    public static double getDefense(UUID uuid) {
        Entity entity = Bukkit.getEntity(uuid);
        if (entity == null) return 0;
        if (!entity.isValid()) return 0;

        DefenseReduction dr = DamageHandler.getBuff().getBuff(uuid).getActivateBuff(DefenseReduction.class, new String[]{}, new String[]{});
        double defense;

        if (entity instanceof Player player) {
            PlayerData playerData = PlayerData.get(player);
            PlayerStats playerStats = playerData.getStats();

            defense = playerStats.getStat("DEFENSE");
        } else {
            ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(uuid).orElse(null);
            defense = (mythicMob != null) ? mythicMob.getVariables().getFloat("DEFENSE") : 0;
        }

        return defense - (dr == null ? 0 : dr.getAmount()/100 * defense);
    }
}

