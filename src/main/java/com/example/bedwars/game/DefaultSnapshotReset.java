package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;

/** Default reset strategy copying worlds from templates. */
public final class DefaultSnapshotReset implements ArenaResetStrategy {
  private final BedwarsPlugin plugin;
  private final ResetManager manager;

  public DefaultSnapshotReset(BedwarsPlugin plugin, ResetManager mgr){
    this.plugin = plugin;
    this.manager = mgr;
  }

  @Override
  public void prepare(Arena a) {
    a.setState(GameState.RESTARTING);
    Location lobby = manager.lobbySpawn();
    for (Player p : plugin.contexts().playersInArena(a.id())) {
      if (lobby != null) p.teleport(lobby);
      p.getInventory().clear();
      plugin.contexts().clear(p);
    }
    plugin.generators().cleanupArena(a.id());
    manager.cleanupOrphans(a.id());
    plugin.contexts().clearArena(a.id());
  }

  @Override
  public void reset(Arena a) throws IOException {
    Path template = manager.templatesPath().resolve(a.id());
    Path worldContainer = Bukkit.getWorldContainer().toPath();
    Path worldDir = worldContainer.resolve(a.world().name());
    World world = Bukkit.getWorld(a.world().name());
    if (world != null) Bukkit.unloadWorld(world, false);
    if (Files.exists(worldDir)) deleteDir(worldDir);
    if (Files.exists(template)) copyDir(template, worldDir);
    Bukkit.createWorld(new WorldCreator(a.world().name()));
    plugin.arenas().load(a.id());
  }

  @Override public String name(){ return "DefaultSnapshotReset"; }

  private static void deleteDir(Path path) throws IOException {
    if (!Files.exists(path)) return;
    Files.walk(path).sorted(Comparator.reverseOrder()).forEach(p -> {
      try { Files.delete(p); } catch (IOException ignored) {} });
  }

  private static void copyDir(Path src, Path dest) throws IOException {
    Files.walk(src).forEach(p -> {
      try {
        Path target = dest.resolve(src.relativize(p));
        if (Files.isDirectory(p)) {
          Files.createDirectories(target);
        } else {
          Files.copy(p, target, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }
      } catch (IOException ignored) {}
    });
  }
}
