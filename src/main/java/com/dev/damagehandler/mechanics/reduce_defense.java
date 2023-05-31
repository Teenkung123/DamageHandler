package com.dev.damagehandler.mechanics;

import com.dev.damagehandler.DamageHandler;
import com.dev.damagehandler.utils.debuff.debuffs.DefenseReduction;
import io.lumine.mythic.api.adapters.AbstractEntity;
import io.lumine.mythic.api.config.MythicLineConfig;
import io.lumine.mythic.api.skills.ITargetedEntitySkill;
import io.lumine.mythic.api.skills.SkillMetadata;
import io.lumine.mythic.api.skills.SkillResult;
import io.lumine.mythic.bukkit.BukkitAdapter;
import org.bukkit.entity.Entity;

public class reduce_defense implements ITargetedEntitySkill {
    private final double amount;
    private final int duration;

    public reduce_defense(MythicLineConfig config) {
        amount = config.getDouble(new String[] {"amount", "a"}, 0);
        duration = config.getInteger(new String[] {"duration"});
    }

    @Override
    public SkillResult castAtEntity(SkillMetadata skillMetadata, AbstractEntity abstractEntity) {
        if (BukkitAdapter.adapt(abstractEntity) != null) {
            Entity bukkittarget = BukkitAdapter.adapt(abstractEntity);
            DamageHandler.getDebuff().getDebuff(bukkittarget.getUniqueId()).addDebuff(new DefenseReduction(amount, duration));
        }
        return SkillResult.SUCCESS;
    }
}
