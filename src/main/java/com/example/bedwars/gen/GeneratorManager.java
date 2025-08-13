package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
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
        holoService.spawnOrUpdate(arenaId, rg, 0, false);
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
      for (var entry : map.entrySet()) {
        RuntimeGen g = entry.getValue();
        g.cooldown -= 20;
        int count = countItems(arena.id(), g);
        boolean capReached = count >= g.cap;
        if (capReached) {
          holoService.spawnOrUpdate(arena.id(), g, count, true);
          continue;
        }
        if (g.cooldown > 0) {
          holoService.spawnOrUpdate(arena.id(), g, count, false);
          continue;
        }
        g.interval = balance.intervalFor(g, diamondTier.getOrDefault(arena.id(),1),
            emeraldTier.getOrDefault(arena.id(),1), 0);
        g.cooldown = g.interval;
        drop(arena.id(), g);
        holoService.spawnOrUpdate(arena.id(), g, count, false);
      }
    });
  }

  private int countItems(String arenaId, RuntimeGen g) {
    int capCount = 0;
    Keys keys = plugin.keys();
    for (Item item : g.dropLoc.getWorld().getNearbyEntitiesByType(Item.class, g.dropLoc, 2.5)) {
      var pdc = item.getPersistentDataContainer();
      String a = pdc.get(keys.ARENA_ID(), PersistentDataType.STRING);
      String gid = pdc.get(keys.GEN_ID(), PersistentDataType.STRING);
      if (arenaId.equals(a) && g.id.toString().equals(gid)) capCount += item.getItemStack().getAmount();
    }
    return capCount;
  }

  private void drop(String arenaId, RuntimeGen g) {
    Material mat = Material.matchMaterial(plugin.getConfig().getString("drops." + g.type.name(), "IRON_INGOT"));
    Collection<Player> nearby = Collections.emptyList();
    if (g.teamBase) {
      nearby = g.dropLoc.getWorld().getNearbyPlayers(g.dropLoc, 1.2, p -> arenaId.equals(plugin.contexts().getArena(p)));
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
