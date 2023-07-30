package com.dev.damagehandler.inflect;

import com.dev.damagehandler.DamageHandler;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.UUID;

public class InflectVisualizer {

    public static final HashMap<UUID, TextDisplay> mapHologram = new HashMap<>();

    public static void start() {

        Bukkit.getScheduler().runTaskTimer(DamageHandler.getInstance(), () -> {
            try {
                for (UUID uuid : mapHologram.keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || entity.isDead() || !ElementalInflect.entityElementInflect.containsKey(uuid)) {
                        TextDisplay textDisplay = mapHologram.get(uuid);
                        textDisplay.remove();
                        mapHologram.remove(uuid);
                    }
                }

                for (UUID uuid : ElementalInflect.entityElementInflect.keySet()) {
                    Entity entity = Bukkit.getEntity(uuid);
                    if (entity == null || entity.isDead()) {
                        if (!mapHologram.containsKey(uuid)) continue;

                        TextDisplay textDisplay = mapHologram.get(uuid);
                        textDisplay.remove();

                        mapHologram.remove(uuid);

                    /*
                    PacketContainer ridingPacket = manager.createPacket(PacketType.Play.Server.MOUNT);

                    ridingPacket.getIntegers().write(0, entity.getEntityId());
                    int[] holos = {textDisplay.getEntityId(), textDisplay.getEntityId(), textDisplay.getEntityId(), textDisplay.getEntityId()};
                    ridingPacket.getIntegerArrays().write(0, holos);

                    for (Entity nearbyEntity : entity.getNearbyEntities(30, 30, 30)) {
                        if (!(nearbyEntity instanceof Player player)) continue;
                        manager.sendServerPacket(player, ridingPacket);
                    }

                     */

                        continue;
                    }

                    BoundingBox boundingBox = entity.getBoundingBox();
                    float scale = (float)Math.max(boundingBox.getHeight() * 0.5, 1);
                    double height = boundingBox.getHeight() * 1.3;

                    Location spawnLocation = new Location(entity.getWorld(), entity.getLocation().getX(), entity.getLocation().getY()+ height, entity.getLocation().getZ());

                    if (!mapHologram.containsKey(uuid)) {
                        TextDisplay textDisplay = entity.getWorld().spawn(spawnLocation, TextDisplay.class);
                        textDisplay.setBillboard(Display.Billboard.CENTER);
                        textDisplay.setText(DamageHandler.getElementalInflect().getInflect(uuid).getInflectedIcon());
                        textDisplay.setTransformation(new Transformation(textDisplay.getTransformation().getTranslation(), textDisplay.getTransformation().getLeftRotation(), new Vector3f(scale), textDisplay.getTransformation().getRightRotation()));
                        textDisplay.setShadowed(true);
                        textDisplay.setBrightness(new Display.Brightness(15, 15));
                        mapHologram.put(uuid, textDisplay);

                    } else {
                        TextDisplay textDisplay = mapHologram.get(uuid);
                        textDisplay.setText(DamageHandler.getElementalInflect().getInflect(uuid).getInflectedIcon());
                        textDisplay.teleport(spawnLocation);
                    }
                }
            } catch (Exception ignored) {}
        }, 1, 1);
    }
}
