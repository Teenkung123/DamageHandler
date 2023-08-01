package com.dev.damagehandler.mechanics;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.debuff.debuffs.ElementalShield;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.Entity;

public class elemental_shield implements ITargetedEntitySkill {

    private final double amount;
    private final long duration;
    private final String element;

    public elemental_shield(MythicLineConfig config) {
        amount = config.getDouble(new String[] {"amount", "a"}, 0);
        duration = config.getLong(new String[] {"duration"}, 0);
        element = config.getString(new String[] {"element"});
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (BukkitAdapter.adapt(abstractEntity) != null) {
            Entity bukkittarget = BukkitAdapter.adapt(abstractEntity);
            DamageHandler.getDebuff().getDebuff(bukkittarget.getUniqueId()).addDebuff(new ElementalShield(amount, element, duration));
        }
        return SkillResult.SUCCESS;
    }
}
