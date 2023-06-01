package com.dev.damagehandler.events.deal_damage;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.debuff.debuffs.DefenseReduction;
import com.dev.damagehandler.debuff.debuffs.ElementalResistanceReduction;
import com.dev.damagehandler.utils.FormulaConverter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

/**
 * This class use to deal damage from Player -> Mob or Player
 */
public class PlayerAttack implements Listener {

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerAttack(PlayerAttackEvent event) {

        Player attacker = event.getAttacker().getPlayer();

        LivingEntity victim = event.getEntity();

        // loop all elemental type damage
        for (DamagePacket packet : event.getDamage().getPackets()) {

            // working only damage that have element (include physical damage)
            if (packet.getElement() == null) {
                packet.setValue(0);
                continue;
            }
            FileConfiguration config = DamageHandler.getInstance().getConfig();

            //if (event.getEntity() instanceof Player victim) {

            String Element = packet.getElement().getId();
            PlayerMetadata attackerStats = event.getAttacker();

            double AttackerCRITRate = Math.max(Math.min(attackerStats.getStat("AST_CRITICAL_RATE"), 100), 0);
            boolean isCritical = new Random().nextDouble() < AttackerCRITRate / 100;
            if (isCritical) event.getDamage().registerElementalCriticalStrike(packet.getElement());

            DefenseReduction dr = DamageHandler.getDebuff().getDebuff(victim.getUniqueId()).getActivateDebuff(DefenseReduction.class, new String[]{}, new String[]{});
            ElementalResistanceReduction er = DamageHandler.getDebuff().getDebuff(victim.getUniqueId()).getActivateDebuff(ElementalResistanceReduction.class, new String[]{"element"}, new String[]{Element});

            // add built-in placeholders to be use in configuration
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("damage", String.valueOf(packet.getValue()));
            placeholders.put("is-critical", String.valueOf(isCritical));
            placeholders.put("attacker-level", String.valueOf(PlayerData.get(attacker).getLevel()));
            placeholders.put("attacker-critical-rate", String.valueOf(attackerStats.getStat("AST_CRITICAL_RATE")));
            placeholders.put("attacker-critical-damage", String.valueOf(attackerStats.getStat("AST_CRITICAL_DAMAGE")));
            placeholders.put("attacker-attack-buff", String.valueOf(attackerStats.getStat("AST_ATTACK_DAMAGE_BUFF")));
            placeholders.put("attacker-attack-buff-percent", String.valueOf(attackerStats.getStat("AST_ATTACK_DAMAGE_BUFF_PERCENT")));
            placeholders.put("attacker-elemental-damage-bonus", String.valueOf(attackerStats.getStat("AST_"+Element+"_DAMAGE_BONUS")));
            placeholders.put("attacker-all-elemental-damage-bonus", String.valueOf(attackerStats.getStat("AST_ALL_ELEMENTAL_DAMAGE_BONUS")));
            placeholders.put("attacker-elemental-resistance", String.valueOf(attackerStats.getStat("AST_"+Element+"_RESISTANCE")));
            placeholders.put("attacker-defense", String.valueOf(attackerStats.getStat("DEFENSE")));
            placeholders.put("attacker-ignore-defense", String.valueOf(attackerStats.getStat("AST_IGNORE_DEFENSE")));
            placeholders.put("victim-elemental-resistance-reduction", String.valueOf(er != null ? er.getAmount() : 0));
            placeholders.put("victim-defense-reduction", String.valueOf(dr != null ? dr.getAmount() : 0));


            // if victim is player
            if (event.getEntity() instanceof Player player) {

                PlayerData victimData = PlayerData.get(player);
                PlayerStats victimStats = victimData.getStats();

                double defense = victimStats.getStat("DEFENSE");
                double elemental_resistance = victimStats.getStat("AST_"+Element+"_RESISTANCE");

                placeholders.put("victim-level", String.valueOf(victimData.getLevel()));
                placeholders.put("victim-critical-rate", String.valueOf(victimStats.getStat("AST_CRITICAL_RATE")));
                placeholders.put("victim-critical-damage", String.valueOf(victimStats.getStat("AST_CRITICAL_DAMAGE")));
                placeholders.put("victim-attack-buff", String.valueOf(victimStats.getStat("AST_ATTACK_DAMAGE_BUFF")));
                placeholders.put("victim-attack-buff-percent", String.valueOf(victimStats.getStat("AST_ATTACK_DAMAGE_BUFF_PERCENT")));
                placeholders.put("victim-elemental-damage-bonus", String.valueOf(victimStats.getStat("AST_"+Element+"_DAMAGE_BONUS")));
                placeholders.put("victim-all-elemental-damage-bonus", String.valueOf(victimStats.getStat("AST_ALL_ELEMENTAL_DAMAGE_BONUS")));
                placeholders.put("victim-elemental-resistance", String.valueOf(elemental_resistance ));
                placeholders.put("victim-defense", String.valueOf(defense ));
                placeholders.put("victim-ignore-defense", String.valueOf(victimStats.getStat("AST_IGNORE_DEFENSE")));

                double finalDamage = FormulaConverter.convert(config.getString("Damage-Calculation.Player-Player.formula"), Objects.requireNonNull(config.getConfigurationSection("Damage-Calculation.Player-Player.variables")), placeholders);
                packet.setValue(finalDamage);

            }
            //  if victim is not player
            else {
                // get Victim Mob and check if it is mythic mob or regular mob
                ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(victim.getUniqueId()).orElse(null);

                int VictimLevel = (mythicMob != null) ? (int) Math.round(mythicMob.getLevel()) : 1;
                double VictimDEF = (mythicMob != null) ? mythicMob.getVariables().getFloat("DEFENSE") : 0;
                double VictimElementalResistance = (mythicMob != null) ? mythicMob.getVariables().getFloat("AST_"+Element+"_RESISTANCE") : 0;

                /*
                Bukkit.broadcastMessage(ChatColor.RED+""+victim.getUniqueId());
                Bukkit.broadcastMessage(ChatColor.GREEN+""+VictimDEF);
                Bukkit.broadcastMessage(ChatColor.GREEN+""+(dr != null ? dr.getAmount() : 0));
                Bukkit.broadcastMessage(ChatColor.GREEN+""+(VictimDEF - (((dr != null ? dr.getAmount() : 0)/100)*VictimDEF)));
                 */

                placeholders.put("victim-level", String.valueOf(VictimLevel));
                placeholders.put("victim-is-mythic-mob", String.valueOf(mythicMob != null));
                placeholders.put("victim-elemental-resistance", String.valueOf(VictimElementalResistance));
                placeholders.put("victim-defense", String.valueOf(VictimDEF));
                double finalDamage = FormulaConverter.convert(config.getString("Damage-Calculation.Player-Mob.formula"), Objects.requireNonNull(config.getConfigurationSection("Damage-Calculation.Player-Mob.variables")), placeholders);

                //Bukkit.broadcastMessage(String.valueOf(finalDamage));
                packet.setValue(finalDamage);

            }
        }
    }
}
