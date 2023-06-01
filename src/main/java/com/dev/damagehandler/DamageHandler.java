package com.dev.damagehandler;

import com.dev.damagehandler.commands.core;
import com.dev.damagehandler.debuff.Debuff;
import com.dev.damagehandler.events.MythicMechanicLoad;
import com.dev.damagehandler.events.attack_handle.ElementModifier;
import com.dev.damagehandler.events.attack_handle.InflectElement;
import com.dev.damagehandler.events.attack_handle.RemoveVanillaDamage;
import com.dev.damagehandler.events.attack_handle.ShieldRefutation;
import com.dev.damagehandler.events.deal_damage.MiscAttack;
import com.dev.damagehandler.events.deal_damage.MobAttack;
import com.dev.damagehandler.events.deal_damage.PlayerAttack;
import com.dev.damagehandler.events.indicator.ASTDamageIndicators;
import com.dev.damagehandler.inflect.ElementalInflect;
import com.dev.damagehandler.listener.AttackEventListener;
import com.dev.damagehandler.utils.ConfigLoader;
import com.google.common.io.ByteStreams;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Objects;

public final class DamageHandler extends JavaPlugin {

    /**
     * API Docs:
     * ExpressionBuilder:
     * https://www.objecthunter.net/exp4j/apidocs/index.html
     */

    // TODO: เลียงตามลำดับ
    //  1. Level Difference Multiplier (waiting for DJKlaKung)
    //  2. Elemental Inflection (Done)
    //  3. Resistance Reduction & Defense Reduction (Done)
    //  4. Entity Elemental Inflection Status
    //  5. Elemental Reaction
    //  6. Configurable Damage Equation (Done)
    //  7. More...

    private static DamageHandler instance;
    private static ElementalInflect elementalInflect;
    private static Debuff debuff;

    @Override
    public void onEnable() {
        instance = this;
        elementalInflect = new ElementalInflect();
        debuff = new Debuff();
        loadResource(this, "config.yml");
        getConfig().options().copyDefaults();
        saveDefaultConfig();
        ConfigLoader.loadConfig();
        ElementalInflect.startTick();
        Debuff.startTick();

        Objects.requireNonNull(Bukkit.getPluginCommand("damagehandle")).setExecutor(new core());

        //Register EventListener
        Bukkit.getPluginManager().registerEvents(new MythicMechanicLoad(), this);
        Bukkit.getPluginManager().registerEvents(new PlayerAttack(), this);
        Bukkit.getPluginManager().registerEvents(new MobAttack(), this);
        Bukkit.getPluginManager().registerEvents(new MiscAttack(), this);
        Bukkit.getPluginManager().registerEvents(new ElementModifier(), this);
        Bukkit.getPluginManager().registerEvents(new AttackEventListener(), this);
        Bukkit.getPluginManager().registerEvents(new ASTDamageIndicators(getConfig().getConfigurationSection("Indicators")), this);
        Bukkit.getPluginManager().registerEvents(new RemoveVanillaDamage(), this);
        Bukkit.getPluginManager().registerEvents(new InflectElement(), this);
        Bukkit.getPluginManager().registerEvents(new ShieldRefutation(), this);

        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mm reload");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mi reload all");
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "mmocore reload");


    }

    public static DamageHandler getInstance() {
        return instance;
    }
    public static ElementalInflect getElementalInflect() { return elementalInflect; }
    public static Debuff getDebuff() { return debuff; }

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
