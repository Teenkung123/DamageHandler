package com.dev.damagehandler.events.indicator;

import io.lumine.mythic.lib.MythicLib;
import io.lumine.mythic.lib.UtilityMethods;
import io.lumine.mythic.lib.api.event.AttackEvent;
import io.lumine.mythic.lib.damage.DamageMetadata;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import io.lumine.mythic.lib.listener.option.GameIndicators;
import io.lumine.mythic.lib.util.CustomFont;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**
 * This class just use to override MythicLib idiot damage indicator system,
 * so I modified some code
 */
public class ASTDamageIndicators extends GameIndicators {
    private final boolean splitHolograms;
    private final String format;
    private final String crit_format;
    private final boolean enable;
    private final @Nullable CustomFont font;
    private final @Nullable CustomFont fontCrit;

    public ASTDamageIndicators(ConfigurationSection config) {
        super(config);
        this.enable = config.getBoolean("enable");
        this.splitHolograms = config.getBoolean("split-holograms");
        this.format = config.getString("format");
        this.crit_format = config.getString("crit-format");
        if (config.getBoolean("custom-font.enabled")) {
            this.font = new CustomFont(Objects.requireNonNull(config.getConfigurationSection("custom-font.normal")));
            this.fontCrit = new CustomFont(Objects.requireNonNull(config.getConfigurationSection("custom-font.crit")));
        } else {
            this.font = null;
            this.fontCrit = null;
        }
    }

    @EventHandler(
            priority = EventPriority.MONITOR,
            ignoreCancelled = true
    )
    public void a(AttackEvent event) {

        if (!this.enable) { return; }

        Entity entity = event.getEntity();

        if (!(entity instanceof Player) || !UtilityMethods.isVanished((Player)entity)) {
            List<String> holos = new ArrayList<>();
            Map<IndicatorType, Double> mappedDamage = this.mapDamage(event.getDamage());
            mappedDamage.forEach((type, val) -> {
                if (!(val < 0.02)) {
                    holos.add(type.computeFormat(val));
                }
            });
            if (this.splitHolograms) {

                for (String holo : holos) {
                    this.displayIndicator(entity, holo, this.getDirection(event.toBukkit()), io.lumine.mythic.lib.api.event.IndicatorDisplayEvent.IndicatorType.DAMAGE);
                }
            } else {
                String joined = String.join(" ", holos);
                this.displayIndicator(entity, joined, this.getDirection(event.toBukkit()), io.lumine.mythic.lib.api.event.IndicatorDisplayEvent.IndicatorType.DAMAGE);
            }
        }
    }

    private @NotNull Vector getDirection(EntityDamageEvent event) {
        if (event instanceof EntityDamageByEntityEvent) {
            Vector dir = event.getEntity().getLocation().toVector().subtract(((EntityDamageByEntityEvent)event).getDamager().getLocation().toVector()).setY(0);
            if (dir.lengthSquared() > 0.0) {
                double a = Math.atan2(dir.getZ(), dir.getX());
                a += 1.5707963267948966 * (random.nextDouble() - 0.5);
                return new Vector(Math.cos(a), 0.0, Math.sin(a));
            }
        }

        double a = random.nextDouble() * Math.PI * 2.0;
        return new Vector(Math.cos(a), 0.0, Math.sin(a));
    }

    private @NotNull Map<IndicatorType, Double> mapDamage(DamageMetadata damageMetadata) {
        Map<IndicatorType, Double> mapped = new HashMap<>();

        for (DamagePacket packet : damageMetadata.getPackets()) {
            IndicatorType type = new IndicatorType(damageMetadata, packet);
            mapped.put(type, mapped.getOrDefault(type, 0.0) + packet.getFinalValue());
        }

        return mapped;
    }

    private class IndicatorType {
        final boolean physical;
        final @Nullable Element element;
        final boolean crit;

        IndicatorType(DamageMetadata damageMetadata, DamagePacket packet) {
            boolean var10001;
            label22: {
                label24: {
                    this.physical = packet.hasType(DamageType.PHYSICAL);
                    this.element = packet.getElement();
                    if (this.physical) {
                        if (damageMetadata.isWeaponCriticalStrike()) {
                            break label24;
                        }
                    } else if (damageMetadata.isSkillCriticalStrike()) {
                        break label24;
                    }

                    if (this.element == null || !damageMetadata.isElementalCriticalStrike(this.element)) {
                        var10001 = false;
                        break label22;
                    }
                }

                var10001 = true;
            }

            this.crit = var10001;
        }

        @NotNull
        private String computeFormat(double damage) {
            CustomFont indicatorFont = this.crit && ASTDamageIndicators.this.fontCrit != null ? ASTDamageIndicators.this.fontCrit : ASTDamageIndicators.this.font;
            String formattedDamage = indicatorFont == null ? ASTDamageIndicators.this.formatNumber(damage) : indicatorFont.format(ASTDamageIndicators.this.formatNumber(damage));
            return (this.crit) ? MythicLib.plugin.getPlaceholderParser().parse(null, ASTDamageIndicators.this.crit_format.replace("{color}", (this.element != null) ? this.element.getColor() : "").replace("{icon}", (this.element != null) ? this.element.getLoreIcon() : "").replace("{value}", formattedDamage)) : MythicLib.plugin.getPlaceholderParser().parse(null, ASTDamageIndicators.this.format.replace("{color}", (this.element != null) ? this.element.getColor() : "").replace("{icon}", (this.element != null) ? this.element.getLoreIcon() : "").replace("{value}", formattedDamage));
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            } else if (o != null && this.getClass() == o.getClass()) {
                IndicatorType that = (IndicatorType)o;
                return this.physical == that.physical && Objects.equals(this.element, that.element);
            } else {
                return false;
            }
        }

        public int hashCode() {
            return Objects.hash(this.physical, this.element);
        }
    }
}
