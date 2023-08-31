package com.dev.damagehandler.reaction.reactions;

import com.dev.damagehandler.reaction.reaction_type.TriggerAuraReaction;
import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.utils.StatCalculation;
import com.dev.damagehandler.utils.Utils;
import io.lumine.mythic.bukkit.MythicBukkit;
import io.lumine.mythic.core.mobs.ActiveMob;
import io.lumine.mythic.lib.damage.DamagePacket;
import net.Indyuce.mmocore.api.player.PlayerData;
import net.Indyuce.mmocore.api.player.stats.PlayerStats;
import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageEvent;
import org.jetbrains.annotations.Nullable;

public class Overloaded extends TriggerAuraReaction {
    public Overloaded() {
        super("OVERLOADED", ConfigLoader.getReactionDisplay("OVERLOADED"), ConfigLoader.getAuraElement("OVERLOADED"), ConfigLoader.getTriggerElement("OVERLOADED"), ConfigLoader.getGaugeUnitTax("OVERLOADED"));
    }

    @Override
    public void trigger(DamagePacket damage, double gauge_unit, String decay_rate, @Nullable Entity damager, LivingEntity entity, EntityDamageEvent.DamageCause damage_cause) {

        int attacker_level = 1;
        double elemental_mastery = 0;
        double resistance_multiplier = StatCalculation.getResistanceMultiplier(entity.getUniqueId(), getConfig().getString("resistance-element"));

        if (damager != null) {
            if (damager instanceof Player player) {
                PlayerData playerData = PlayerData.get(player);
                PlayerStats playerStats = playerData.getStats();

                elemental_mastery = playerStats.getStat("AST_ELEMENTAL_MASTERY");
                attacker_level = playerData.getLevel();
            } else {
                ActiveMob mythicMob = MythicBukkit.inst().getMobManager().getActiveMob(damager.getUniqueId()).orElse(null);
                attacker_level = (mythicMob != null) ? (int) mythicMob.getLevel() : 1;
            }
        }

        String formula = getConfig().getString("damage-formula");
        assert formula != null;
        Expression expression = new ExpressionBuilder(formula)
                .variables("attacker_level", "elemental_mastery", "resistance_multiplier")
                .build()
                .setVariable("attacker_level", attacker_level)
                .setVariable("elemental_mastery", elemental_mastery)
                .setVariable("resistance_multiplier", resistance_multiplier);

        double final_damage = expression.evaluate();
        damage(final_damage, damager, entity, damage_cause);

        for (String indicator : getConfig().getStringList("damage-indicator")) {
            displayIndicator(indicator.replace("{damage}", Utils.Format(final_damage)), entity);
        }
    }
}
