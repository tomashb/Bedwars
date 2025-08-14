package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.gen.Generator;
import com.example.bedwars.shop.NpcData;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Locale;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.GameRule;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Handles lighting and time/weather consistency for arenas.
 */
public final class LightingService {
  private final BedwarsPlugin plugin;
  private boolean missingDarknessWarned;

  public LightingService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    // periodic sanity to remove blindness and optionally give night vision
    Bukkit.getScheduler().runTaskTimer(plugin, this::tick, 100L, 100L);
  }

  private static PotionEffectType resolveEffect(String name) {
    // 1) static constant
    try {
      Field f = PotionEffectType.class.getField(name);
      Object v = f.get(null);
      if (v instanceof PotionEffectType pet) return pet;
    } catch (NoSuchFieldException | IllegalAccessException ignored) {
    }
    // 2) traditional API
    try {
      PotionEffectType pet = PotionEffectType.getByName(name);
      if (pet != null) return pet;
    } catch (Throwable ignored) {
    }
    // 3) Registry lookup (1.19+)
    try {
      Class<?> regClz = Class.forName("org.bukkit.Registry");
      Field f = regClz.getField("POTION_EFFECT_TYPE");
      Object registry = f.get(null);
      Method get = registry.getClass().getMethod("get", NamespacedKey.class);
      Object v = get.invoke(registry, NamespacedKey.minecraft(name.toLowerCase(Locale.ROOT)));
      if (v instanceof PotionEffectType pet) return pet;
    } catch (Throwable ignored) {
    }
    return null;
  }

  /** Apply day time and clear weather according to config. */
  public void applyDayClear(Arena a) {
    World w = requireWorld(a);
    var cfg = plugin.getConfig();
    if (cfg.getBoolean("lighting.force_day", true)) {
      w.setTime(cfg.getLong("lighting.day_time", 6000L));
    }
    if (cfg.getBoolean("lighting.lock_day", true)) {
      w.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
    }
    if (cfg.getBoolean("lighting.clear_weather", true)) {
      w.setStorm(false);
      w.setThundering(false);
      w.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
    }
  }

  /** Relight all chunks for an arena after world (re)load. */
  public void relightArena(Arena a) {
    if (!plugin.getConfig().getBoolean("lighting.relight_on_world_load", true)) return;
    World w = requireWorld(a);
    for (Chunk c : getArenaChunks(a)) {
      if (!c.isLoaded()) c.load();
      relightChunk(c);
    }
  }

  private void relightChunk(Chunk c) {
    World w = c.getWorld();
    int y = w.getMaxHeight() - 2;
    int[] coords = {0, 15};
    for (int x : coords) {
      for (int z : coords) {
        Block b = c.getBlock(x, y, z);
        Material m = b.getType();
        if (m != Material.LIGHT) {
          b.setType(Material.LIGHT, false);
          b.setType(m, false);
        }
      }
    }
    w.refreshChunk(c.getX(), c.getZ());
  }

  /** Remove blindness/darkness and optionally grant night vision. */
  public void sanitizePlayer(Player p) {
    p.removePotionEffect(PotionEffectType.BLINDNESS);
    PotionEffectType darkness = resolveEffect("DARKNESS");
    if (darkness != null) {
      p.removePotionEffect(darkness);
    } else if (!missingDarknessWarned) {
      plugin.logWarning("Potion effect DARKNESS not found; running on older server?");
      missingDarknessWarned = true;
    }
    if (plugin.getConfig().getBoolean("lighting.waiting_night_vision", false) && isWaiting(p)) {
      PotionEffect nv = new PotionEffect(PotionEffectType.NIGHT_VISION, 20 * 30, 0, true, false);
      p.addPotionEffect(nv);
    }
  }

  private void tick() {
    for (Player p : Bukkit.getOnlinePlayers()) {
      if (plugin.contexts().isInArena(p)) sanitizePlayer(p);
    }
  }

  private boolean isWaiting(Player p) {
    String arenaId = plugin.contexts().getArena(p);
    if (arenaId == null) return false;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    return a != null && a.state() == GameState.WAITING;
  }

  private World requireWorld(Arena a) {
    World w = Bukkit.getWorld(a.world().name());
    if (w == null) throw new IllegalStateException("World not loaded for arena " + a.id());
    return w;
  }

  private Collection<Chunk> getArenaChunks(Arena a) {
    World w = requireWorld(a);
    int minX = Integer.MAX_VALUE, maxX = Integer.MIN_VALUE;
    int minZ = Integer.MAX_VALUE, maxZ = Integer.MIN_VALUE;
    boolean found = false;
    List<Location> locs = new ArrayList<>();
    if (a.lobby() != null) locs.add(a.lobby());
    for (TeamData td : a.teams().values()) {
      if (td.spawn() != null) locs.add(td.spawn());
      if (td.bedBlock() != null) locs.add(td.bedBlock());
    }
    for (Generator g : a.generators()) locs.add(g.location());
    for (NpcData n : a.npcs()) locs.add(n.location());
    for (Location l : locs) {
      if (l.getWorld() != w) continue;
      int cx = l.getBlockX() >> 4;
      int cz = l.getBlockZ() >> 4;
      if (cx < minX) minX = cx;
      if (cx > maxX) maxX = cx;
      if (cz < minZ) minZ = cz;
      if (cz > maxZ) maxZ = cz;
      found = true;
    }
    Set<Chunk> chunks = new HashSet<>();
    if (!found) {
      for (Chunk c : w.getLoadedChunks()) chunks.add(c);
      return chunks;
    }
    for (int x = minX; x <= maxX; x++) {
      for (int z = minZ; z <= maxZ; z++) {
        chunks.add(w.getChunkAt(x, z));
      }
    }
    return chunks;
  }
}

