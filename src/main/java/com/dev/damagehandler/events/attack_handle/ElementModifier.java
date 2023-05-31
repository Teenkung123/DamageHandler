package com.dev.damagehandler.events.attack_handle;

import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import de.tr7zw.nbtapi.NBTItem;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

import java.util.Objects;

/**
 * This class use to modify damage element of each damage cause
 * and set element of non-element damage to default element
 * and operation of disable regular damage will happen here
 */
public class ElementModifier implements Listener {

    /** Call when any entity attack
     * Use to set ALL non-elemental damage to default element
     */
    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityAttack(AttackEvent event) {

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
                if (event.getAttack().getAttacker() instanceof ASTEntityStatProvider statProvider) {
                    EntityDamageByEntityEvent e = new EntityDamageByEntityEvent(statProvider.getEntity(), event.getEntity(), event.toBukkit().getCause(), event.getDamage().getDamage());
                    ActiveMob attackerMythicMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getDamager().getUniqueId()).orElse(null);
                    if (attackerMythicMob != null && attackerMythicMob.getVariables().getInt("AST_DISABLE_REGULAR_DAMAGE") == 1) {
                        event.getDamage().getPackets().get(0).setValue(0);
                        return;
                    }
                } else {
                    if (event.toBukkit() instanceof EntityDamageByEntityEvent e) {
                        ActiveMob attackerMythicMob = MythicBukkit.inst().getMobManager().getActiveMob(e.getDamager().getUniqueId()).orElse(null);
                        if (attackerMythicMob != null && attackerMythicMob.getVariables().getInt("AST_DISABLE_REGULAR_DAMAGE") == 1) {
                            event.getDamage().getPackets().get(0).setValue(0);
                            return;
                        }
                    }
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
