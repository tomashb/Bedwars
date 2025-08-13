package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.example.bedwars.util.DirUtils;
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
    Path runtime = worldContainer.resolve(a.world().name());
    Path tmp = worldContainer.resolve(a.world().name() + ".tmp");
    Path backup = worldContainer.resolve(a.world().name() + "__old__");

    World world = Bukkit.getWorld(a.world().name());
    if (world != null) Bukkit.unloadWorld(world, false);

    if (Files.exists(backup)) DirUtils.deleteDir(backup);
    if (Files.exists(runtime)) {
      Files.move(runtime, backup, StandardCopyOption.ATOMIC_MOVE);
    }

    try {
      if (Files.exists(tmp)) DirUtils.deleteDir(tmp);
      Files.createDirectories(tmp);
      DirUtils.copyDir(template, tmp);
      Files.move(tmp, runtime, StandardCopyOption.ATOMIC_MOVE);
      DirUtils.deleteDir(backup);
    } catch (IOException ex) {
      if (Files.exists(tmp)) DirUtils.deleteDir(tmp);
      if (!Files.exists(runtime) && Files.exists(backup)) {
        Files.move(backup, runtime, StandardCopyOption.ATOMIC_MOVE);
      }
      throw ex;
    }

    Bukkit.createWorld(new WorldCreator(a.world().name()));
    plugin.arenas().load(a.id());
  }

  @Override public String name(){ return "DefaultSnapshotReset"; }

}
