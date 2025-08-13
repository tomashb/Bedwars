package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.example.bedwars.util.DirUtils;
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
  private final String runtimePrefix;
  private final boolean preserveOnDisable;
  private final boolean atomicCopy;

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
    this.runtimePrefix = cfg.getString("reset.runtime_prefix", "bw_");
    this.preserveOnDisable = cfg.getBoolean("reset.preserve_on_disable", true);
    this.atomicCopy = cfg.getBoolean("reset.atomic_copy", true);
    this.defaultStrategy = new DefaultSnapshotReset(plugin, this);
    this.slimeStrategy = Bukkit.getPluginManager().getPlugin("SlimeWorldManager") != null
        ? new SlimeWorldManagerReset(plugin, this) : null;
  }

  public ArenaResetStrategy strategyFor(Arena a){
    return slimeStrategy != null ? slimeStrategy : defaultStrategy;
  }

  public Location lobbySpawn(){ return lobbySpawn; }
  public Path templatesPath(){ return templatesPath; }
  public String runtimePrefix(){ return runtimePrefix; }
  public boolean preserveOnDisable(){ return preserveOnDisable; }
  public boolean atomicCopy(){ return atomicCopy; }

  /** Restore arena worlds from templates at startup if missing. */
  public void restoreWorldsOnEnable(){
    plugin.logInfo(plugin.msg("reset.restoring_at_start"));
    for (Arena a : plugin.arenas().all()) {
      String name = a.world().name();
      Path runtime = Bukkit.getWorldContainer().toPath().resolve(name);
      Path template = templatesPath.resolve(a.id());
      try {
        if (!Files.exists(runtime) && Files.exists(template)) {
          DirUtils.copyDir(template, runtime);
        }
        if (Bukkit.getWorld(name) == null) {
          Bukkit.createWorld(new org.bukkit.WorldCreator(name));
        }
      } catch (IOException ex) {
        plugin.logSevere("[Reset] Failed to restore world %s: %s", name, ex.getMessage());
      }
    }
  }

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
