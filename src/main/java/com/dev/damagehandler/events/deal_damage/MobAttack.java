package com.dev.damagehandler.events.deal_damage;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.listener.events.MiscAttackEvent;
import com.dev.damagehandler.listener.events.MobAttackEvent;
import com.dev.damagehandler.utils.FormulaConverter;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.damage.DamagePacket;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * This class use to deal damage from Mob -> Mob or Player
 */
public class MobAttack implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onMobAttack(MobAttackEvent event) {

        LivingEntity attacker;
        Entity damager = event.getDamager();
        if (damager instanceof Projectile projectile) {

            // return if damager is Thrown Potion
            if (projectile instanceof AreaEffectCloud || projectile instanceof ThrownPotion) {
                new MiscAttack().onMiscAttack(new MiscAttackEvent(event.toBukkit(), event.getAttack()));
                return;
            }
            // return if shooter is not living entity
            if (!(projectile.getShooter() instanceof LivingEntity)) {
                new MiscAttack().onMiscAttack(new MiscAttackEvent(event.toBukkit(), event.getAttack()));
                return;
            }

            attacker = (LivingEntity) projectile.getShooter();

        } else if (damager instanceof LivingEntity) {

            attacker = (LivingEntity) event.getDamager();

        } else {
            return;
        }

        LivingEntity victim = event.getEntity();

        for (DamagePacket packet : event.getDamage().getPackets()) {
            // get Vi Mob and check if it is mythic mob or regular mob
            ActiveMob attackerMythicMob = MythicBukkit.inst().getMobManager().getActiveMob(attacker.getUniqueId()).orElse(null);

            // working only damage that have element (include physical damage)
            if (packet.getElement() == null) {
                packet.setValue(0);
                continue;
            }
            FileConfiguration config = DamageHandler.getInstance().getConfig();

            // get base damage
            String Element = packet.getElement().getId();

            int AttackerLevel = (attackerMythicMob != null) ? (int) Math.round(attackerMythicMob.getLevel()) : 1;
            double AttackerDEF = (attackerMythicMob != null) ? attackerMythicMob.getVariables().getFloat("DEFENSE") : 0;
            double AttackerElementalResistance = (attackerMythicMob != null) ? attackerMythicMob.getVariables().getFloat("AST_"+Element+"_RESISTANCE") : 0;

            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("damage", String.valueOf(packet.getValue()));
            placeholders.put("attacker-level", String.valueOf(AttackerLevel));
            placeholders.put("attacker-is-mythic-mob", String.valueOf(attackerMythicMob != null));
            placeholders.put("attacker-elemental-resistance", String.valueOf(AttackerElementalResistance));
            placeholders.put("attacker-defense", String.valueOf(AttackerDEF));

            // if victim is player
            if (victim instanceof Player player) {

                PlayerData victimData = PlayerData.get(player);
                PlayerStats victimStats = victimData.getStats();

                placeholders.put("victim-level", String.valueOf(victimData.getLevel()));
                placeholders.put("victim-critical-rate", String.valueOf(victimStats.getStat("AST_CRITICAL_RATE")));
                placeholders.put("victim-critical-damage", String.valueOf(victimStats.getStat("AST_CRITICAL_DAMAGE")));
                placeholders.put("victim-attack-buff", String.valueOf(victimStats.getStat("AST_ATTACK_DAMAGE_BUFF")));
                placeholders.put("victim-attack-buff-percent", String.valueOf(victimStats.getStat("AST_ATTACK_DAMAGE_BUFF_PERCENT")));
                placeholders.put("victim-elemental-damage-bonus", String.valueOf(victimStats.getStat("AST_"+Element+"_DAMAGE_BONUS")));
                placeholders.put("victim-all-elemental-damage-bonus", String.valueOf(victimStats.getStat("AST_ALL_ELEMENTAL_DAMAGE_BONUS")));
                placeholders.put("victim-elemental-resistance", String.valueOf(victimStats.getStat("AST_"+Element+"_RESISTANCE")));
                placeholders.put("victim-defense", String.valueOf(victimStats.getStat("DEFENSE")));
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
