package com.dev.damagehandler.mechanics;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.events.attack_handle.attack_priority.TriggerReaction;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
import com.dev.damagehandler.utils.ConfigLoader;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.api.skills.placeholders.PlaceholderDouble;
import io.lumine.mythic.bukkit.BukkitAdapter;
import io.lumine.mythic.lib.MythicLib;
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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

import java.util.Objects;

//This class will register the ElementDamage Skill to MythicMobs
public class elemental_damage implements ITargetedEntitySkill {
    private final String element;
    private final PlaceholderDouble amount;
    private final Double gauge_unit;

    /**
     * This is constructor for the Skill
     * Usage: ElementDamage{amount=5;element=GEO} @target
     *
     * @param config The config of the skill.
     */
    public elemental_damage(MythicLineConfig config) {
        amount = config.getPlaceholderDouble(new String[] {"amount", "a"}, 0);
        element = config.getString(new String[] {"element"});
        gauge_unit = config.getDouble(new String[] {"gauge_unit", "gu"}, 1);
    }

    /**
     * This method will trigger when the skill is cast at an entity.
     *
     * @param skillMetadata The skill metadata.
     * @param abstractEntity The entity that was targeted.
     * @return The result of the skill.
     */
    @SuppressWarnings("deprecation")
    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (BukkitAdapter.adapt(abstractEntity) != null) {
            Entity bukkittarget = BukkitAdapter.adapt(abstractEntity);
            Entity bukkitcaster = skillMetadata.getCaster().getEntity().getBukkitEntity();

            Element element1 = Objects.requireNonNull(Element.valueOf(element), ConfigLoader.getDefaultElement());
            // caster is player
            if (bukkitcaster instanceof Player) {

                //This part will damage the player
                PlayerData playerData = PlayerData.get(bukkitcaster.getUniqueId());

                DamageMetadata damage = new DamageMetadata(amount.get(skillMetadata), element1, DamageType.SKILL);
                StatMap statMap = playerData.getMMOPlayerData().getStatMap();
                PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
                AttackMetadata attack = new AttackMetadata(damage, playerMetadata);

                Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().damage(attack, (LivingEntity) bukkittarget));

                for (DamagePacket packet : damage.getPackets()) {
                    if (packet.getElement() == null) continue;
                    if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                    DamageHandler.getAura().getAura(bukkittarget.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit);
                    TriggerReaction.triggerReactions(packet, (LivingEntity) bukkittarget, bukkitcaster);
                }
            }

            // caster is not player
            else {

                DamageMetadata damage = new DamageMetadata(amount.get(skillMetadata), Objects.requireNonNull(Element.valueOf(element), ConfigLoader.getDefaultElement()), DamageType.SKILL);

                AttackMetadata attack = new AttackMetadata(damage, (LivingEntity) bukkittarget, new ASTEntityStatProvider((LivingEntity) bukkitcaster));
                Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().damage(attack, (LivingEntity) bukkittarget));

                for (DamagePacket packet : damage.getPackets()) {
                    if (packet.getElement() == null) continue;
                    if (!ConfigLoader.getAuraWhitelist().contains(packet.getElement().getId())) continue;
                    DamageHandler.getAura().getAura(bukkittarget.getUniqueId()).addAura(packet.getElement().getId(), gauge_unit);
                    TriggerReaction.triggerReactions(packet, (LivingEntity) bukkittarget, bukkitcaster);
                }
            }
        }
        return SkillResult.SUCCESS;
    }
}
