package com.dev.damagehandler;

import com.dev.damagehandler.events.*;
import com.dev.damagehandler.events.attack_handle.ElementModifier;
import com.dev.damagehandler.events.deal_damage.MiscAttack;
import com.dev.damagehandler.events.deal_damage.MobAttack;
import com.dev.damagehandler.events.deal_damage.PlayerAttack;
import com.dev.damagehandler.listener.AttackEventListener;
import com.dev.damagehandler.utils.ConfigLoader;
import com.dev.damagehandler.events.indicator.ASTDamageIndicators;
import com.dev.damagehandler.utils.ElementalInflect;
import com.dev.damagehandler.utils.manager.EntityDataManager;
import com.dev.damagehandler.events.attack_handle.RemoveVanillaDamage;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.UUID;

public final class DamageHandler extends JavaPlugin {

    /**
     * API Docs:
     * ExpressionBuilder:
     * https://www.objecthunter.net/exp4j/apidocs/index.html
     */

    // TODO: เลียงตามลำดับ
    //  1. Level Difference Multiplier (waiting for DJKlaKung)
    //  2. Elemental Inflection
    //  3. Resistance Reduction & Defense Reduction
    //  4. Entity Elemental Inflection Status
    //  5. Elemental Reaction
    //  6. Configurable Damage Equation (Done)
    //  7. More...

    private static DamageHandler instance;
    private static ElementalInflect elementalInflect;

    @Override
    public void onEnable() {
        instance = this;
        elementalInflect = new ElementalInflect();
        loadResource(this, "config.yml");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        ConfigLoader.loadConfig();

        //Register EventListener
        Bukkit.getPluginManager().registerEvents(new MythicMechanicLoad(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerAttack(), this);
        Bukkit.getPluginManager().registerEvents(new MobAttack(), this);
        Bukkit.getPluginManager().registerEvents(new MiscAttack(), this);
        Bukkit.getPluginManager().registerEvents(new ElementModifier(), this);
        Bukkit.getPluginManager().registerEvents(new JoinEvent(), this);
        Bukkit.getPluginManager().registerEvents(new EntityDeath(), this);
        Bukkit.getPluginManager().registerEvents(new AttackEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ASTDamageIndicators(getConfig().getConfigurationSection("Indicators")), this);
        Bukkit.getPluginManager().registerEvents(new RemoveVanillaDamage(), this);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi reload all");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mmocore reload");

        //This will loop every 5 minute to free the memory
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            EntityDataManager.getManager().forEach((entity, entityManager) -> {
                if (entity.isDead()) {
                    EntityDataManager.removeEntity(entity);
                }
            });
        }, 20, 300);

    }

    public static DamageHandler getInstance() {
        return instance;
    }
    public static ElementalInflect getElementalInflect() { return elementalInflect; }

    //What the hell is this?
    private static File loadResource(Plugin plugin, String resource) {
        File folder = plugin.getDataFolder();
        if (!folder.exists())
            folder.mkdir();
        File resourceFile = new File(folder, resource);
        try {
            //if (!resourceFile.exists()) {
            resourceFile.createNewFile();
            try (InputStream in = plugin.getResource(resource);
                 OutputStream out = new FileOutputStream(resourceFile)) {
                ByteStreams.copy(in, out);
            }
            //}
        } catch (Exception e) { e.printStackTrace();
            e.printStackTrace();
        }
        return resourceFile;
    }

}
