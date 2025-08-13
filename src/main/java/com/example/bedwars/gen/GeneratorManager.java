package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.ops.Keys;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.UUID;

/**
 * Central scheduler handling generator ticks.
 */
public final class GeneratorManager {
  private final BedwarsPlugin plugin;
  private final Map<String, Map<UUID, RuntimeGen>> runtime = new HashMap<>();
  private int taskId = -1;

  public GeneratorManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  public void start() {
    if (taskId != -1) return;
    taskId = new BukkitRunnable() {
      @Override public void run() { tickAll(); }
    }.runTaskTimer(plugin, 20L, 20L).getTaskId();
  }

  public void stop() {
    if (taskId != -1) {
      Bukkit.getScheduler().cancelTask(taskId);
      taskId = -1;
    }
    runtime.keySet().forEach(this::cleanupArena);
    runtime.clear();
  }

  // Called at STARTING->RUNNING and on arena reload
  public void refreshArena(String arenaId) {
    plugin.arenas().get(arenaId).ifPresent(arena -> {
      Map<UUID, RuntimeGen> map = new LinkedHashMap<>();
      World w = Bukkit.getWorld(arena.world().name());
      if (w == null) {
        plugin.getLogger().warning("World not loaded for arena " + arenaId);
        return;
      }

      double yOff = plugin.getConfig().getDouble("generators.spawn-offset-y", 0.5);
      for (var g : arena.generators()) {
        var loc = g.location().clone();
        loc.setWorld(w);
        loc.add(0, yOff, 0);

        int interval = g.intervalTicks() > 0 ? g.intervalTicks()
            : plugin.getConfig().getInt("generators.base-intervals." + g.type().name(), 120);
        int amount = g.amount() > 0 ? g.amount()
            : plugin.getConfig().getInt("generators.base-amounts." + g.type().name(), 1);

        RuntimeGen rg = new RuntimeGen(g.type(), loc, g.tier(), interval, amount);
        rg.isBase = GenUtils.isBaseGenerator(arena, g,
            plugin.getConfig().getDouble("generators.base-radius", 10.0));
        map.put(g.id(), rg);

        if (plugin.getConfig().getBoolean("holograms.enabled", true)) {
          GenUtils.spawnOrUpdateHolo(plugin, arenaId, g.id(), rg);
        }
      }
      runtime.put(arenaId, map);
    });
  }

  public void cleanupArena(String arenaId) {
    plugin.arenas().get(arenaId).ifPresent(arena -> {
      World w = Bukkit.getWorld(arena.world().name());
      if (w == null) return;

      Keys keys = plugin.keys();
      w.getEntities().forEach(ent -> {
        var pdc = ent.getPersistentDataContainer();
        String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
        if (!arenaId.equals(a)) return;
        if (pdc.has(keys.GEN_HOLO(), PersistentDataType.STRING)) ent.remove();
        if (ent instanceof Item item && pdc.has(keys.GEN_ID(), PersistentDataType.STRING)) item.remove();
      });
    });
  }

  private void tickAll() {
    plugin.arenas().all().forEach(arena -> {
      if (arena.state() != GameState.RUNNING) return;
      Map<UUID, RuntimeGen> map = runtime.get(arena.id());
      if (map == null) return;

      double forgeMul = GenUtils.forgeMultiplier(arena);
      for (var entry : map.entrySet()) {
        UUID genId = entry.getKey();
        RuntimeGen rg = entry.getValue();

        rg.current -= 20;
        if (rg.current > 0) {
          GenUtils.updateHolo(plugin, arena.id(), genId, rg);
          continue;
        }

        int amount = rg.baseAmount;
        if (rg.isBase && (rg.type == GeneratorType.IRON || rg.type == GeneratorType.GOLD)) {
          amount = Math.max(1, (int) Math.round(amount * forgeMul));
        }
        GenUtils.drop(plugin, arena.id(), genId, rg, amount);
        rg.current = rg.baseInterval;
        GenUtils.updateHolo(plugin, arena.id(), genId, rg);
      }
    });
  }

  // Called when arena state changes
  public void onArenaStateChange(Arena arena, GameState oldS, GameState newS) {
    if (oldS == GameState.STARTING && newS == GameState.RUNNING) {
      GenUtils.removeSetupMarkers(arena);
      refreshArena(arena.id());
    }
    if (oldS == GameState.RUNNING && (newS == GameState.ENDING || newS == GameState.RESTARTING || newS == GameState.WAITING)) {
      cleanupArena(arena.id());
    }
  }
}
