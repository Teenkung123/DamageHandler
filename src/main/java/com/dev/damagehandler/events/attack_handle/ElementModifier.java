package com.dev.damagehandler.events.attack_handle;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.commands.core;
import com.dev.damagehandler.events.attack_handle.attack_priority.TriggerReaction;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.utils.Utils;
import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.core.skills.variables.VariableRegistry;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Objects;

/**
 * This class use to modify damage element of each damage cause
 * and set element of non-element damage to default element
 * and operation of disable regular damage will happen here
 */
public class ElementModifier implements Listener {

    @EventHandler
    public void onInteract(PlayerInteractEntityEvent e) {
        core.entity = e.getRightClicked();
    }


    /** Call when any entity attack
     * Use to set ALL non-elemental damage to default element
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityAttack(AttackEvent event) {
        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (Arrays.asList(packet.getTypes()).contains(DamageType.DOT)) return;
        }

        Element defaultElement = Objects.requireNonNull(Element.valueOf(ConfigLoader.getDefaultElement()));

        // if damage doesn't have element
        if (event.getDamage().collectElements().size() == 0) {

            // if attacker is player -> check if player's weapon is disabled regular damage -> if yes then set regular damage to 0
            if (event instanceof PlayerAttackEvent e) {
                Player attacker = event.getAttack().getPlayer();
                ItemStack item = attacker.getInventory().getItem(e.getAttacker().getActionHand().toBukkit());
                if (item != null && !item.getType().equals(Material.AIR)) {
                    NBTItem nbt = new NBTItem(item);
                    byte disable_regular_damage = nbt.getByte("MMOITEMS_AST_DISABLE_REGULAR_DAMAGE");
                    if (disable_regular_damage == 1) {
                        event.getDamage().getPackets().get(0).setValue(0);
                        return;
                    }
                }

            // if attacker is mob -> check if mob is disable to deal regular damage -> if yes then set regular damage to 0
            } else {
                try {
                    if (event.getAttack().getAttacker() instanceof ASTEntityStatProvider statProvider) {
                        EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(statProvider.getEntity(), event.getEntity(), event.toBukkit().getCause(), event.getDamage().getDamage());
                        ActiveMob attackerMythicMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getDamager().getUniqueId()).orElse(null);
                        if (attackerMythicMob != null && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_AMOUNT") && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_ELEMENT") && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT")) {

                            VariableRegistry variables = attackerMythicMob.getVariables();
                            double damage_amount = variables.getFloat("AST_ELEMENTAL_DAMAGE_AMOUNT");
                            Element element = Element.valueOf(variables.getString("AST_ELEMENTAL_DAMAGE_ELEMENT"));
                            if (element == null) return;

                            event.getDamage().getPackets().get(0).setTypes(new DamageType[]{DamageType.SKILL});
                            event.getDamage().getPackets().get(0).setElement(element);
                            event.getDamage().getPackets().get(0).setValue(damage_amount);

                            double gauge_unit = Double.parseDouble(Utils.splitTextAndNumber(variables.getString("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT"))[0]);
                            String decay_rate = Utils.splitTextAndNumber(variables.getString("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT"))[1];
                            DamageHandler.getAura().getAura(event.getEntity().getUniqueId()).addAura(element.getId(), gauge_unit, decay_rate);
                            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> TriggerReaction.triggerReactions(event.getDamage().getPackets().get(0), gauge_unit, decay_rate, event.getEntity(), e.getDamager(), event.toBukkit().getCause()));

                            return;
                        }
                    } else {
                        if (event.toBukkit() instanceof EntityDamageByEntityEvent e) {
                            ActiveMob attackerMythicMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getDamager().getUniqueId()).orElse(null);
                            if (attackerMythicMob != null && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_AMOUNT") && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_ELEMENT") && attackerMythicMob.getVariables().has("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT")) {

                                VariableRegistry variables = attackerMythicMob.getVariables();
                                double damage_amount = variables.getFloat("AST_ELEMENTAL_DAMAGE_AMOUNT");
                                Element element = Element.valueOf(variables.getString("AST_ELEMENTAL_DAMAGE_ELEMENT"));
                                if (element == null) return;

                                event.getDamage().getPackets().get(0).setTypes(new DamageType[]{DamageType.SKILL});
                                event.getDamage().getPackets().get(0).setElement(element);
                                event.getDamage().getPackets().get(0).setValue(damage_amount);

                                double gauge_unit = Double.parseDouble(Utils.splitTextAndNumber(variables.getString("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT"))[0]);
                                String decay_rate = Utils.splitTextAndNumber(variables.getString("AST_ELEMENTAL_DAMAGE_GAUGE_UNIT"))[1];
                                DamageHandler.getAura().getAura(event.getEntity().getUniqueId()).addAura(element.getId(), gauge_unit, decay_rate);
                                Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> TriggerReaction.triggerReactions(event.getDamage().getPackets().get(0), gauge_unit, decay_rate, event.getEntity(), e.getDamager(), event.toBukkit().getCause()));

                                return;
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // set to specify element if the damage cause is have elemental modifier (in config "Elemental-Modifier")
            Element element;
            if (ConfigLoader.getElementalModifier().containsKey(event.toBukkit().getCause().name())) {
                element = Element.valueOf(ConfigLoader.getElementalModifier().get(event.toBukkit().getCause().name()));
            }
            else {
                element = defaultElement;
            }
            event.getDamage().getPackets().get(0).setElement(element);

        }

        // if have more than 1 element
        else {
            if (event.getDamage().getPackets().size() == 1) { return; }

            for (DamagePacket packet : event.getDamage().getPackets()) {

                // if damage is non-element then do same as above
                if (packet.getElement() == null) {

                    if (event instanceof PlayerAttackEvent e) {
                        Player attacker = event.getAttack().getPlayer();
                        ItemStack item = attacker.getInventory().getItem(e.getAttacker().getActionHand().toBukkit());
                        if (item != null && !item.getType().equals(Material.AIR)) {
                            NBTItem nbt = new NBTItem(item);
                            byte disable_regular_damage = nbt.getByte("MMOITEMS_AST_DISABLE_REGULAR_DAMAGE");
                            if (disable_regular_damage == 1) {
                                packet.setValue(0);
                                return;
                            }
                        }
                    }

                    packet.setElement(defaultElement);
                }
            }
        }
    }
}
