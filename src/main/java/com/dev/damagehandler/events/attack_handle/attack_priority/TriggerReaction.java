package com.dev.damagehandler.events.attack_handle.attack_priority;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.listener.events.MobAttackEvent;
import com.dev.damagehandler.reaction.ElementalReaction;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.api.event.PlayerAttackEvent;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import org.bukkit.entity.*;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

import java.util.Arrays;

public class TriggerReaction {

    @AttackHandle(priority = 1)
    public void attack(PlayerAttackEvent event) {
        for (DamagePacket packet : event.getDamage().getPackets()) {
            if (Arrays.asList(packet.getTypes()).contains(DamageType.SKILL)) continue;
            if (packet.getElement() == null) continue;
            if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
            DamageHandler.getAura().getAura(event.getEntity().getUniqueId()).addAura(packet.getElement().getId(), ConfigLoader.getDefaultGaugeUnit());
            triggerReactions(packet, event.getEntity(), event.getAttack().getPlayer());
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

                        // return if damager is Thrown Potion
                        if (projectile instanceof AreaEffectCloud || projectile instanceof ThrownPotion) {
                            return;
                        }
                        // return if shooter is not living entity
                        if (!(projectile.getShooter() instanceof LivingEntity)) {
                            return;
                        }

                        attacker = (LivingEntity) projectile.getShooter();

                    } else if (damager instanceof LivingEntity) {

                        attacker = (LivingEntity) event.getDamager();

                    }
                }

                for (DamagePacket packet : a.getDamage().getPackets()) {
                    if (Arrays.asList(packet.getTypes()).contains(DamageType.SKILL)) continue;
                    if (packet.getElement() == null) continue;
                    if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                    DamageHandler.getAura().getAura(a.getEntity().getUniqueId()).addAura(packet.getElement().getId(), ConfigLoader.getDefaultGaugeUnit());
                    triggerReactions(packet, a.getEntity(), attacker);
                }
            }

        } catch (NullPointerException ignored) {}
    }

    public static void triggerReactions(DamagePacket damage, LivingEntity entity, Entity damager) {
        if (damage.getElement() == null) return;
        for (ElementalReaction elementalReaction : DamageHandler.getReaction().getElementalReactions().values()) {
            if (elementalReaction.getTrigger().equals(damage.getElement().getId()) && DamageHandler.getAura().getAura(entity.getUniqueId()).getMapAura().containsKey(elementalReaction.getAura())) {
                elementalReaction.trigger(damage, entity, damager);
            }
        }
    }
}
