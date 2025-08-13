package com.example.bedwars.services;

import com.example.bedwars.arena.Arena;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Tracks blocks placed by players per arena using packed coordinates for
 * efficient storage and lookup.
 */
public final class PlacedBlocksStore {
  private final Map<String, LongOpenHashSet> byArena = new ConcurrentHashMap<>();

  private static long pack(Location loc) {
    long x = loc.getBlockX() & 0x3FFFFFFL;
    long y = loc.getBlockY() & 0xFFFL;
    long z = loc.getBlockZ() & 0x3FFFFFFL;
    return (x << 38) | (y << 26) | z;
  }

  /** Record a placed block for the given arena. */
  public void add(String arenaId, Location loc) {
    byArena.computeIfAbsent(arenaId, k -> new LongOpenHashSet()).add(pack(loc));
  }

  /** Check if the block at location belongs to the given arena and was placed. */
  public boolean contains(String arenaId, Location loc) {
    LongOpenHashSet set = byArena.get(arenaId);
    return set != null && set.contains(pack(loc));
  }

  /** Remove a placed block entry. */
  public void remove(String arenaId, Location loc) {
    LongOpenHashSet set = byArena.get(arenaId);
    if (set != null) set.remove(pack(loc));
  }

  /** Get arena id associated with location, or null. */
  public String arenaAt(Location loc) {
    long k = pack(loc);
    for (var e : byArena.entrySet()) {
      if (e.getValue().contains(k)) return e.getKey();
    }
    return null;
  }

  /** Clear tracked entries for an arena without modifying the world. */
  public void clearArena(String arenaId) {
    byArena.remove(arenaId);
  }

  /**
   * Remove all tracked blocks in an arena's world by setting them to AIR.
   */
  public void clearAll(JavaPlugin plugin, Arena a) {
    LongOpenHashSet set = byArena.get(a.id());
    if (set == null || set.isEmpty()) return;
    World w = a.world();
    for (long k : set) {
      int x = (int) (k >> 38);
      int y = (int) ((k >> 26) & 0xFFF);
      int z = (int) (k & 0x3FFFFFFL);
      Block b = w.getBlockAt(x, y, z);
      b.setType(Material.AIR, false);
      b.removeMetadata("bw_placed", plugin);
    }
    set.clear();
  }
}

