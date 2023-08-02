package com.dev.damagehandler.reaction;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.aura.AuraData;
import com.dev.damagehandler.events.attack_handle.attack_priority.TriggerReaction;
import com.dev.damagehandler.events.indicator.ASTDamageIndicators;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.IndicatorDisplayEvent;
import io.lumine.mythic.lib.api.player.EquipmentSlot;
import io.lumine.mythic.lib.api.stat.StatMap;
import io.lumine.mythic.lib.damage.AttackMetadata;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.player.PlayerMetadata;
import net.Indyuce.mmocore.api.player.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public abstract class ElementalReaction {

    private final String id;
    private final String aura;
    private final String trigger;

    public ElementalReaction(String id, String aura, String trigger) {
        this.id = id;
        this.aura = aura;
        this.trigger = trigger;
    }

    public String getId() {
        return this.id;
    }
    public String getAura() {
        return this.aura;
    }
    public String getTrigger() {
        return this.trigger;
    }
    public String getDisplay() {
        return ConfigLoader.getReactionDisplay(id);
    }

    public AuraData getAuraData(UUID uuid) {
        return DamageHandler.getAura().getAura(uuid);
    }

    public void damage(double amount, Entity caster, LivingEntity target) {
         damage(new DamageMetadata(amount, DamageType.DOT) , caster, target);
    }

    public void damage(double amount, Entity caster, LivingEntity target, String element, boolean damage_calculate, double aura_gauge_unit, String aura_decay_rate) {
        Element e = Objects.requireNonNull(MythicLib.plugin.getElements().get(element));
        DamageMetadata damageMetadata = new DamageMetadata(amount, e, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target, aura_gauge_unit, aura_decay_rate);
    }

    public void damage(double amount, Entity caster, LivingEntity target, @NotNull Element element, boolean damage_calculate, double aura_gauge_unit, String aura_decay_rate) {
        DamageMetadata damageMetadata = new DamageMetadata(amount, element, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target, aura_gauge_unit, aura_decay_rate);
    }

    public void damage(double amount, Entity caster, LivingEntity target, String element, boolean damage_calculate) {
        Element e = Objects.requireNonNull(MythicLib.plugin.getElements().get(element));
        damage(amount, caster, target, e, damage_calculate);
    }
    public void damage(double amount, Entity caster, LivingEntity target, @NotNull Element element, boolean damage_calculate) {
        DamageMetadata damageMetadata = new DamageMetadata(amount, element, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target);
    }

    private void damage(DamageMetadata damage, Entity caster, LivingEntity target) {

        if (caster instanceof Player) {
            PlayerData playerData = PlayerData.get(caster.getUniqueId());

            StatMap statMap = playerData.getMMOPlayerData().getStatMap();
            PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
            AttackMetadata attack = new AttackMetadata(damage, target, playerMetadata);

            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), () -> MythicLib.plugin.getDamage().registerAttack(attack, false, true));

        }  else {
            AttackMetadata attack = new AttackMetadata(damage, target, new ASTEntityStatProvider((LivingEntity) caster));
            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().registerAttack(attack, false, true));
        }
    }

    private void damage(DamageMetadata damage, Entity caster, LivingEntity target, double gauge_unit, String decay_rate) {

        if (caster instanceof Player) {
            PlayerData playerData = PlayerData.get(caster.getUniqueId());

            StatMap statMap = playerData.getMMOPlayerData().getStatMap();
            PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
            AttackMetadata attack = new AttackMetadata(damage, target, playerMetadata);

            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), () -> MythicLib.plugin.getDamage().registerAttack(attack, false, true));

            for (DamagePacket packet : damage.getPackets()) {
                if (packet.getElement() == null) continue;
                if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                DamageHandler.getAura().getAura(target.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit, decay_rate);
                TriggerReaction.triggerReactions(packet, gauge_unit, decay_rate, target, caster);
            }

        }  else {
            AttackMetadata attack = new AttackMetadata(damage, target, new ASTEntityStatProvider((LivingEntity) caster));
            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().registerAttack(attack, false, true));

            for (DamagePacket packet : damage.getPackets()) {
                if (packet.getElement() == null) continue;
                if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                DamageHandler.getAura().getAura(target.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit, decay_rate);
                TriggerReaction.triggerReactions(packet, gauge_unit, decay_rate, target, caster);
            }
        }
    }

    public void displayIndicator(String text, Entity entity) {
        if (!(entity instanceof Player) || !UtilityMethods.isVanished((Player)entity)) {
            ConfigurationSection config = DamageHandler.getInstance().getConfig().getConfigurationSection("Indicators");
            ASTDamageIndicators indicators = new ASTDamageIndicators(config);
            assert config != null;
            String format = config.getString("shield-attack-format");
            assert format != null;
            double a = new Random().nextDouble() * Math.PI * 2.0;

            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()->indicators.displayIndicator(entity, indicators.computeFormat(0, false, text, null), new Vector(Math.cos(a), 0.0, Math.sin(a)), IndicatorDisplayEvent.IndicatorType.DAMAGE));
        }
    }

    public abstract void trigger(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, Entity damager);

}
