package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.ops.Keys;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Central scheduler handling generator ticks and tier changes.
 */
public final class GeneratorManager {
  private final BedwarsPlugin plugin;
  private final Map<String, Map<UUID, RuntimeGen>> runtime = new ConcurrentHashMap<>();
  private final Map<String, Integer> diamondTier = new ConcurrentHashMap<>();
  private final Map<String, Integer> emeraldTier = new ConcurrentHashMap<>();
  private final GenBalance balance;
  private final GenSplitService splitService;
  private final AutoCollectService autoCollect;
  private final GenHologramService holoService;
  private final GenTelemetry telemetry = new GenTelemetry();

  private int taskId = -1;

  public GeneratorManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.balance = new GenBalance(plugin);
    this.splitService = new GenSplitService(plugin);
    this.autoCollect = new AutoCollectService(plugin);
    this.holoService = new GenHologramService(plugin);
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

  public int diamondTier(String arenaId) {
    return diamondTier.getOrDefault(arenaId, 1);
  }

  public int emeraldTier(String arenaId) {
    return emeraldTier.getOrDefault(arenaId, 1);
  }

  public int cooldownSeconds(String arenaId, UUID genId) {
    Map<UUID, RuntimeGen> m = runtime.get(arenaId);
    if (m == null) return 0;
    RuntimeGen g = m.get(genId);
    if (g == null) return 0;
    return Math.max(0, g.cooldown / 20);
  }

  // Called at STARTING->RUNNING and on arena reload
  public void refreshArena(String arenaId) {
    plugin.arenas().get(arenaId).ifPresent(arena -> {
      Map<UUID, RuntimeGen> map = new LinkedHashMap<>();
      World w = Bukkit.getWorld(arena.world().name());
      if (w == null) return;

      double yOff = plugin.getConfig().getDouble("generators.spawn_offset_y", 0.5);
      for (var g : arena.generators()) {
        var loc = g.location().clone();
        loc.setWorld(w);
        loc.add(0, yOff, 0);
        boolean base = g.type().name().startsWith("TEAM");
        RuntimeGen rg = new RuntimeGen(g.id(), g.type(), loc, base);
        rg.tier = g.tier();
        rg.baseInterval = balance.baseInterval(rg);
        rg.interval = rg.baseInterval;
        rg.amount = balance.amount(rg);
        rg.cap = balance.capFor(rg);
        rg.cooldown = rg.interval;
        map.put(g.id(), rg);
      }
      runtime.put(arenaId, map);
      diamondTier.put(arenaId, 1);
      emeraldTier.put(arenaId, 1);
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
        if (ent instanceof Item item && pdc.has(keys.GEN_ID(), PersistentDataType.STRING)) item.remove();
      });
      holoService.removeAll(arenaId);
    });
  }

  /** Hook called by the listener when an arena changes state. */
  public void onArenaStateChange(Arena arena, GameState oldS, GameState newS) {
    if (oldS == GameState.STARTING && newS == GameState.RUNNING) {
      GenUtils.removeSetupMarkers(arena);
      refreshArena(arena.id());
    }
    if (oldS == GameState.RUNNING &&
        (newS == GameState.ENDING || newS == GameState.RESTARTING || newS == GameState.WAITING)) {
      cleanupArena(arena.id());
    }
  }

  private void tickAll() {
    plugin.arenas().all().forEach(arena -> {
      if (arena.state() != GameState.RUNNING) return;
      Map<UUID, RuntimeGen> map = runtime.get(arena.id());
      if (map == null) return;
      boolean holoEnabled = plugin.getConfig().getBoolean("holograms.enabled", true);
      for (var entry : map.entrySet()) {
        RuntimeGen g = entry.getValue();
        g.cooldown -= 20;
        World w = g.dropLoc.getWorld();
        if (w == null) {
          plugin.getLogger().warning("Generator has null world in arena " + arena.id());
          continue;
        }
        int count = GenUtils.countTaggedItemsAround(w, plugin.keys(), arena.id(), g.id, g.dropLoc, 2.5);
        boolean capReached = count >= g.cap;
        if (!capReached && g.cooldown <= 0) {
          g.interval = balance.intervalFor(g, diamondTier.getOrDefault(arena.id(),1),
              emeraldTier.getOrDefault(arena.id(),1), 0);
          g.cooldown = g.interval;
          drop(arena.id(), g);
        }
        boolean show = holoEnabled && plugin.getConfig().getBoolean("holograms.types." + g.type.name(), true)
            && (g.type == GeneratorType.DIAMOND || g.type == GeneratorType.EMERALD);
        if (show) {
          int tier = g.type == GeneratorType.DIAMOND ?
              plugin.game().diamondTier(arena.id()) : plugin.game().emeraldTier(arena.id());
          int seconds = Math.max(0, g.cooldown / 20);
          holoService.updateOrCreate(arena.id(), g.id, g.dropLoc, g.type, tier, seconds);
        }
      }
    });
  }

  private void drop(String arenaId, RuntimeGen g) {
    Material mat = Material.matchMaterial(plugin.getConfig().getString("drops." + g.type.name(), "IRON_INGOT"));
    List<Player> nearby = List.of();
    var world = g.dropLoc.getWorld();
    if (world == null) {
      plugin.getLogger().warning("Generator drop location missing world for arena " + arenaId);
      return;
    }
    if (g.teamBase) {
      nearby = GenUtils.nearbyPlayers(world, g.dropLoc, 1.1).stream()
          .filter(p -> arenaId.equals(plugin.contexts().getArena(p)))
          .toList();
    }
    splitService.distribute(g, mat, g.amount, nearby, autoCollect, arenaId);
    telemetry.recordDrop(g.type, g.amount);
  }

  // Called every second from GameService
  public void onGlobalTime(String arenaId, int sec) {
    var cfg = plugin.getConfig();
    var diamondTimes = cfg.getIntegerList("global_tiers.diamond.announce_times_sec");
    var emeraldTimes = cfg.getIntegerList("global_tiers.emerald.announce_times_sec");
    int dTier = diamondTier.getOrDefault(arenaId,1);
    int eTier = emeraldTier.getOrDefault(arenaId,1);
    if (dTier == 1 && !diamondTimes.isEmpty() && sec >= diamondTimes.get(0)) {
      diamondTier.put(arenaId, 2);
      String msg = plugin.messages().format("gens.diamond_t2", Map.of());
      plugin.contexts().playersInArena(arenaId).forEach(p -> p.sendMessage(msg));
    } else if (dTier == 2 && diamondTimes.size() > 1 && sec >= diamondTimes.get(1)) {
      diamondTier.put(arenaId, 3);
      String msg = plugin.messages().format("gens.diamond_t3", Map.of());
      plugin.contexts().playersInArena(arenaId).forEach(p -> p.sendMessage(msg));
    }
    if (eTier == 1 && !emeraldTimes.isEmpty() && sec >= emeraldTimes.get(0)) {
      emeraldTier.put(arenaId, 2);
      String msg = plugin.messages().format("gens.emerald_t2", Map.of());
      plugin.contexts().playersInArena(arenaId).forEach(p -> p.sendMessage(msg));
    } else if (eTier == 2 && emeraldTimes.size() > 1 && sec >= emeraldTimes.get(1)) {
      emeraldTier.put(arenaId, 3);
      String msg = plugin.messages().format("gens.emerald_t3", Map.of());
      plugin.contexts().playersInArena(arenaId).forEach(p -> p.sendMessage(msg));
    }
  }
}
