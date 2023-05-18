package com.dev.damagehandler.mechanics;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.stats.provider.ASTEntityStatProvider;
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
public class ElementDamage implements ITargetedEntitySkill {
    private static String element;
    private static PlaceholderDouble amount;

    /**
     * This is constructor for the Skill
     * Usage: ElementDamage{amount=5;type=MAGIC;element=GEO} @Target
     *
     * @param config The config of the skill.
     */

    public ElementDamage(MythicLineConfig config) {
        amount = config.getPlaceholderDouble(new String[] {"amount", "a"}, 0);
        element = config.getString(new String[] {"element"});
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
            // caster is player
            if (bukkitcaster instanceof Player) {

                //This part will damage the player
                PlayerData playerData = PlayerData.get(bukkitcaster.getUniqueId());

                DamageMetadata damage = new DamageMetadata(amount.get(skillMetadata), Objects.requireNonNull(Element.valueOf(element), "PHYSICAL"), DamageType.PHYSICAL);
                StatMap statMap = playerData.getMMOPlayerData().getStatMap();
                PlayerMetadata playerMetadata = new PlayerMetadata(statMap, EquipmentSlot.MAIN_HAND);
                AttackMetadata attack = new AttackMetadata(damage, playerMetadata);

                Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().damage(attack, (LivingEntity) bukkittarget));
            }

            // caster is not player
            else {

                DamageMetadata damage = new DamageMetadata(amount.get(skillMetadata), Objects.requireNonNull(Element.valueOf(element), "PHYSICAL"), DamageType.PHYSICAL);

                AttackMetadata attack = new AttackMetadata(damage, (LivingEntity) bukkittarget, new ASTEntityStatProvider((LivingEntity) bukkitcaster));
                Bukkit.getScheduler().runTask(DamageHandler.getInstance(), ()-> MythicLib.plugin.getDamage().damage(attack, (LivingEntity) bukkittarget));
            }
        }
        return SkillResult.SUCCESS;
    }
}
