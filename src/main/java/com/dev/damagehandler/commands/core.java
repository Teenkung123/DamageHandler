package com.dev.damagehandler.commands;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.dev.damagehandler.utils.StatCalculation;
import io.lumine.mythic.lib.damage.DamagePacket;
import io.lumine.mythic.lib.damage.DamageType;
import io.lumine.mythic.lib.element.Element;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class core implements CommandExecutor, TabExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        Player player = (Player) sender;

        ProtocolManager manager = ProtocolLibrary.getProtocolManager();

        if (args.length >= 1) {
            switch (args[0]) {
                case "spawn" -> {
                    PacketContainer spawnPacket = manager.createPacket(PacketType.Play.Server.SPAWN_ENTITY);
                    spawnPacket.getIntegers().write(0, 21785);
                    spawnPacket.getUUIDs().write(0, UUID.randomUUID());
                    spawnPacket.getEntityTypeModifier().write(0, EntityType.ZOMBIE);
                    spawnPacket.getDoubles().write(0, 441.5);
                    spawnPacket.getDoubles().write(1, 72.0);
                    spawnPacket.getDoubles().write(2, 175.5);
                    manager.sendServerPacket(player, spawnPacket);
                }
                case "remove" -> {
                    PacketContainer packetContainer = manager.createPacket(PacketType.Play.Server.ENTITY_DESTROY);
                    List<Integer> i = new ArrayList<>();
                    i.add(21785);
                    packetContainer.getIntLists().write(0, i);
                    manager.sendServerPacket(player, packetContainer);
                }
                case "test" -> {
                    if (args.length >= 2) {
                        DamagePacket packet = new DamagePacket(Double.parseDouble(args[1]), Element.valueOf("CRYO"), DamageType.PHYSICAL);
                        player.sendMessage(String.valueOf(StatCalculation.getTotalDamage(player.getUniqueId(), packet, true)));
                    }
                }
            }
        }

        return false;
    }

    @Nullable
    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        List<String> output = new ArrayList<>();
        if (args.length == 1) {
            List<String> list = Arrays.asList("reload", "spawn", "remove", "test");
            output = tabComplete(args[0], list);
        }

        return output;
    }

    public static List<String> tabComplete(String a, List<String> arg) {
        List<String> matches = new ArrayList<>();
        String search = a.toLowerCase(Locale.ROOT);
        for (String s : arg) {
            if (s.toLowerCase(Locale.ROOT).startsWith(search)) {
                matches.add(s);
            }
        }
        return matches;
    }
}
