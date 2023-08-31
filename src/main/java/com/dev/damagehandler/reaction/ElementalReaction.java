package com.dev.damagehandler.reaction;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.aura.AuraData;
import com.dev.damagehandler.events.attack_handle.attack_priority.TriggerReaction;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.utils.DamageManager;
import com.dev.damagehandler.visuals.ASTDamageIndicators;
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
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Random;
import java.util.UUID;

public abstract class ElementalReaction {

    private final String id;
    private final String aura;
    private final String trigger;
    private final String display;

    public ElementalReaction(String id, String display, String aura, String trigger) {
        this.id = id;
        this.display = display;
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
        return this.display;
    }

    public AuraData getAuraData(UUID uuid) {
        return DamageHandler.getAura().getAura(uuid);
    }
    public ConfigurationSection getConfig() {
        return DamageHandler.getInstance().getConfig().getConfigurationSection("Elemental-Reaction."+id);
    }

    public void damage(double amount, Entity caster, LivingEntity target, EntityDamageEvent.DamageCause damage_cause) {
         damage(new DamageMetadata(amount, DamageType.DOT) , caster, target, damage_cause);
    }

    public void damage(double amount, Entity caster, LivingEntity target, String element, boolean damage_calculate, double aura_gauge_unit, String aura_decay_rate, EntityDamageEvent.DamageCause damage_cause) {
        Element e = Objects.requireNonNull(MythicLib.plugin.getElements().get(element));
        DamageMetadata damageMetadata = new DamageMetadata(amount, e, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target, aura_gauge_unit, aura_decay_rate, damage_cause);
    }

    public void damage(double amount, Entity caster, LivingEntity target, @NotNull Element element, boolean damage_calculate, double aura_gauge_unit, String aura_decay_rate, EntityDamageEvent.DamageCause damage_cause) {
        DamageMetadata damageMetadata = new DamageMetadata(amount, element, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target, aura_gauge_unit, aura_decay_rate, damage_cause);
    }

    public void damage(double amount, Entity caster, LivingEntity target, String element, boolean damage_calculate, EntityDamageEvent.DamageCause damage_cause) {
        Element e = Objects.requireNonNull(MythicLib.plugin.getElements().get(element));
        damage(amount, caster, target, e, damage_calculate, damage_cause);
    }
    public void damage(double amount, Entity caster, LivingEntity target, @NotNull Element element, boolean damage_calculate, EntityDamageEvent.DamageCause damage_cause) {
        DamageMetadata damageMetadata = new DamageMetadata(amount, element, damage_calculate ? DamageType.SKILL : DamageType.DOT);
        damage(damageMetadata, caster, target, damage_cause);
    }

    private void damage(DamageMetadata damage, Entity caster, LivingEntity target, EntityDamageEvent.DamageCause damage_cause) {

        if (caster instanceof Player) {
            PlayerData playerData = PlayerData.get(caster.getUniqueId());

            StatMap statMap = playerData.getMMOPlayerData().getStatMap();
            PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
            AttackMetadata attack = new AttackMetadata(damage, target, playerMetadata);

            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), () -> DamageManager.registerAttack(attack, false, true, damage_cause));

        }  else {
            AttackMetadata attack = new AttackMetadata(damage, target, caster != null ? new ASTEntityStatProvider((LivingEntity) caster) : null);
            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> DamageManager.registerAttack(attack, false, true, damage_cause));
        }
    }

    private void damage(DamageMetadata damage, Entity caster, LivingEntity target, double gauge_unit, String decay_rate, EntityDamageEvent.DamageCause damage_cause) {

        if (caster instanceof Player) {
            PlayerData playerData = PlayerData.get(caster.getUniqueId());

            StatMap statMap = playerData.getMMOPlayerData().getStatMap();
            PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
            AttackMetadata attack = new AttackMetadata(damage, target, playerMetadata);

            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), () -> DamageManager.registerAttack(attack, false, true, damage_cause));

            for (DamagePacket packet : damage.getPackets()) {
                if (packet.getElement() == null) continue;
                if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                DamageHandler.getAura().getAura(target.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit, decay_rate);
                TriggerReaction.triggerReactions(packet, gauge_unit, decay_rate, target, caster, damage_cause);
            }

        }  else {
            AttackMetadata attack = new AttackMetadata(damage, target, caster != null ? new ASTEntityStatProvider((LivingEntity) caster) : null);
            Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> DamageManager.registerAttack(attack, false, true, damage_cause));

            for (DamagePacket packet : damage.getPackets()) {
                if (packet.getElement() == null) continue;
                if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                DamageHandler.getAura().getAura(target.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit, decay_rate);
                TriggerReaction.triggerReactions(packet, gauge_unit, decay_rate, target, caster, damage_cause);
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

    public abstract void trigger(DamagePacket damage, double gauge_unit, String decay_rate, LivingEntity entity, @Nullable Entity damager, EntityDamageEvent.DamageCause damage_cause);

}
