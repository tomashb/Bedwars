package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

/** Chooses reset strategy and provides helpers like lobby location and cleanup. */
public final class ResetManager {
  private final BedwarsPlugin plugin;
  private final ArenaResetStrategy defaultStrategy;
  private final ArenaResetStrategy slimeStrategy;
  private final Location lobbySpawn;
  private final Path templatesPath;

  public ResetManager(BedwarsPlugin plugin){
    this.plugin = plugin;
    var cfg = plugin.getConfig();
    String worldName = cfg.getString("reset.lobby_world", "world");
    ConfigurationSection loc = cfg.getConfigurationSection("reset.lobby_spawn");
    Location lobby = null;
    World world = Bukkit.getWorld(worldName);
    if (world != null && loc != null){
      lobby = new Location(world,
          loc.getDouble("x"),
          loc.getDouble("y"),
          loc.getDouble("z"),
          (float)loc.getDouble("yaw", 0.0),
          (float)loc.getDouble("pitch", 0.0));
    }
    this.lobbySpawn = lobby;
    this.templatesPath = Paths.get(cfg.getString("reset.templates_path", "plugins/Bedwars/templates"));
    this.defaultStrategy = new DefaultSnapshotReset(plugin, this);
    this.slimeStrategy = Bukkit.getPluginManager().getPlugin("SlimeWorldManager") != null
        ? new SlimeWorldManagerReset(plugin, this) : null;
  }

  public ArenaResetStrategy strategyFor(Arena a){
    return slimeStrategy != null ? slimeStrategy : defaultStrategy;
  }

  public Location lobbySpawn(){ return lobbySpawn; }
  public Path templatesPath(){ return templatesPath; }

  /** Remove entities tagged with arena id across worlds. */
  public int cleanupOrphans(String arenaId){
    int removed = 0;
    NamespacedKey key = plugin.keys().ARENA_ID();
    for (World w : Bukkit.getWorlds()) {
      for (Entity e : w.getEntities()) {
        if (e instanceof Player) continue;
        String tag = e.getPersistentDataContainer().get(key, PersistentDataType.STRING);
        if (arenaId.equals(tag)) { e.remove(); removed++; }
      }
    }
    return removed;
  }
}
