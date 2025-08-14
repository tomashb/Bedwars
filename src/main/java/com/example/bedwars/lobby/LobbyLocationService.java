package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

/** Manages the lobby location loaded from config. */
public final class LobbyLocationService {
  private final BedwarsPlugin plugin;
  private Location location;

  public LobbyLocationService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    load();
  }

  public void load() {
    FileConfiguration cfg = plugin.getConfig();
    String world = cfg.getString("lobby.world", "");
    if (world == null || world.isEmpty()) {
      location = null;
      return;
    }
    World w = Bukkit.getWorld(world);
    if (w == null) {
      location = null;
      return;
    }
    double x = cfg.getDouble("lobby.x");
    double y = cfg.getDouble("lobby.y");
    double z = cfg.getDouble("lobby.z");
    float yaw = (float) cfg.getDouble("lobby.yaw");
    float pitch = (float) cfg.getDouble("lobby.pitch");
    location = new Location(w, x, y, z, yaw, pitch);
  }

  public void save() {
    if (location == null) return;
    FileConfiguration cfg = plugin.getConfig();
    cfg.set("lobby.world", location.getWorld().getName());
    cfg.set("lobby.x", location.getX());
    cfg.set("lobby.y", location.getY());
    cfg.set("lobby.z", location.getZ());
    cfg.set("lobby.yaw", location.getYaw());
    cfg.set("lobby.pitch", location.getPitch());
    plugin.saveConfig();
  }

  public Optional<Location> get() {
    return Optional.ofNullable(location);
  }

  public void setFrom(Player p) {
    this.location = p.getLocation().clone();
    save();
  }
}
