package com.dev.damagehandler.events.attack_handle.attack_priority;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.listener.events.MobAttackEvent;
import com.dev.damagehandler.reaction.ElementalReaction;
import com.dev.damagehandler.reaction.reaction_type.TriggerAuraReaction;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.*;

public class TriggerReaction {

    @AttackHandle(priority = 1)
    public void attack(PlayerAttackEvent event) {
        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (Arrays.asList(packet.getTypes()).contains(DamageType.SKILL) || Arrays.asList(packet.getTypes()).contains(DamageType.DOT)) continue;
            if (packet.getElement() == null) continue;
            if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
            DamageHandler.getAura().getAura(event.getEntity().getUniqueId()).addAura(packet.getElement().getId(), ConfigLoader.getDefaultGaugeUnit(), ConfigLoader.getDefaultDecayRate());
            triggerReactions(packet, ConfigLoader.getDefaultGaugeUnit(), ConfigLoader.getDefaultDecayRate(), event.getEntity(), event.getAttacker().getPlayer(), event.toBukkit().getCause());
        }
    }

    @AttackHandle(priority = 1)
    public void attack(AttackEvent a) {

        try {
            if (!a.getAttack().isPlayer()) {
                EntityDamageByEntityEvent e = null;
                LivingEntity attacker = null;

                if (a.getAttack().getAttacker() instanceof ASTEntityStatProvider statProvider) {
                    e = new EntityDamageByEntityEvent(statProvider.getEntity(), a.getEntity(), a.toBukkit().getCause(), a.getDamage().getDamage());
                } else {
                    if (a.toBukkit() instanceof EntityDamageByEntityEvent b) {
                        e = b;
                    }
                }

                if (e != null) {
                    MobAttackEvent event = new MobAttackEvent(e, a.getAttack());

                    Entity damager = event.getDamager();
                    if (damager instanceof Projectile projectile) {

                        // if damager is not Thrown Potion
                        if (!(projectile instanceof AreaEffectCloud || projectile instanceof ThrownPotion)) {
                            // if shooter is living entity
                            if (projectile.getShooter() instanceof LivingEntity) {
                                attacker = (LivingEntity) projectile.getShooter();
                            }
                        }

                    } else if (damager instanceof LivingEntity) {

                        attacker = (LivingEntity) event.getDamager();

                    }
                }

                for (DamagePacket packet : a.getDamage().getPackets()) {
                    if (Arrays.asList(packet.getTypes()).contains(DamageType.SKILL) || Arrays.asList(packet.getTypes()).contains(DamageType.DOT)) continue;
                    if (packet.getElement() == null) continue;
                    if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                    DamageHandler.getAura().getAura(a.getEntity().getUniqueId()).addAura(packet.getElement().getId(), ConfigLoader.getDefaultGaugeUnit(), ConfigLoader.getDefaultDecayRate());
                    triggerReactions(packet, ConfigLoader.getDefaultGaugeUnit(), ConfigLoader.getDefaultDecayRate(), a.getEntity(), attacker, a.toBukkit().getCause());
                }
            }

        } catch (NullPointerException ignored) {}
    }

    public static void triggerReactions(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, Entity damager, EntityDamageEvent.DamageCause damage_cause) {
        if (damage.getElement() == null) return;

        List<ElementalReaction> reactions = new ArrayList<>(DamageHandler.getReaction().getElementalReactions().values());
        Map<String, Integer> reaction_priority = ConfigLoader.getReactionPriority(damage.getElement().getId());
        if (reaction_priority != null) {
            reactions.sort(Comparator.comparingInt(o -> {
                if (!reaction_priority.containsKey(o.getId())) return 0;
                return reaction_priority.get(o.getId());
            }));
        }
        for (ElementalReaction elementalReaction : reactions) {
            if (elementalReaction instanceof TriggerAuraReaction) {
                if (elementalReaction.getTrigger().equals(damage.getElement().getId()) && DamageHandler.getAura().getAura(entity.getUniqueId()).getMapAura().containsKey(elementalReaction.getAura())) {
                    elementalReaction.trigger(damage, gauge_unit, decay_rate, entity, damager, damage_cause);
                }
            }
        }
    }
}
