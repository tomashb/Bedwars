package com.example.bedwars.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import java.util.LinkedHashMap;
import java.util.Map;
import com.example.bedwars.arena.WorldRef;

/**
 * Helper methods to serialise a Bukkit {@link Location} to a map and back.
 */
public final class YamlLocation {
  private YamlLocation() {
  }

  public static Map<String, Object> toMap(Location l) {
    Map<String, Object> m = new LinkedHashMap<>();
    m.put("x", l.getX());
    m.put("y", l.getY());
    m.put("z", l.getZ());
    m.put("yaw", (double) l.getYaw());
    m.put("pitch", (double) l.getPitch());
    return m;
  }

  public static Location fromMap(WorldRef world, ConfigurationSection s) {
    double x = s.getDouble("x");
    double y = s.getDouble("y");
    double z = s.getDouble("z");
    float yaw = (float) s.getDouble("yaw", 0.0);
    float pitch = (float) s.getDouble("pitch", 0.0);
    World bukkitWorld = Bukkit.getWorld(world.name());
    return new Location(bukkitWorld, x, y, z, yaw, pitch);
  }
}
