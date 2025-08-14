package com.example.bedwars.services;

import com.example.bedwars.arena.Arena;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Tracks blocks placed during a game using compact indexes grouped by chunk.
 * Only blocks recorded here are eligible for removal during arena cleanup.
 */
public final class PlacedBlocksStore {
  // arena id -> chunk key -> set of packed block indexes
  private final Map<String, Long2ObjectMap<ShortOpenHashSet>> data = new HashMap<>();

  private static long chunkKey(Block b) {
    return (((long) b.getX() >> 4) << 32) | (((long) b.getZ() >> 4) & 0xFFFFFFFFL);
  }

  private static long chunkKey(Location loc) {
    return (((long) loc.getBlockX() >> 4) << 32) | (((long) loc.getBlockZ() >> 4) & 0xFFFFFFFFL);
  }

  private static short index(Block b) {
    return (short) (((b.getY() & 0xFF) << 8) | ((b.getX() & 0xF) << 4) | (b.getZ() & 0xF));
  }

  private static short index(Location loc) {
    return (short) (((loc.getBlockY() & 0xFF) << 8)
        | ((loc.getBlockX() & 0xF) << 4)
        | (loc.getBlockZ() & 0xF));
  }

  /** Record a placed block for the given arena. */
  public void record(Arena a, Block b) {
    long ck = chunkKey(b);
    short idx = index(b);
    data.computeIfAbsent(a.id(), k -> new Long2ObjectOpenHashMap<ShortOpenHashSet>())
        .computeIfAbsent(ck, k -> new ShortOpenHashSet())
        .add(idx);
  }

  /**
   * Whether the given block location is tracked as placed in the arena.
   */
  public boolean contains(String arenaId, Location loc) {
    Long2ObjectMap<ShortOpenHashSet> m = data.get(arenaId);
    if (m == null) return false;
    ShortOpenHashSet set = m.get(chunkKey(loc));
    return set != null && set.contains(index(loc));
  }

  /** Remove a placed block entry if present. */
  public void remove(String arenaId, Location loc) {
    Long2ObjectMap<ShortOpenHashSet> m = data.get(arenaId);
    if (m == null) return;
    long ck = chunkKey(loc);
    ShortOpenHashSet set = m.get(ck);
    if (set != null) {
      set.remove(index(loc));
      if (set.isEmpty()) m.remove(ck);
    }
  }

  /** Get arena id associated with location, or null. */
  public String arenaAt(Location loc) {
    long ck = chunkKey(loc);
    short idx = index(loc);
    for (var e : data.entrySet()) {
      ShortOpenHashSet set = e.getValue().get(ck);
      if (set != null && set.contains(idx)) return e.getKey();
    }
    return null;
  }

  /** Iterate chunk entries for cleanup. */
  public Collection<Long2ObjectMap.Entry<ShortOpenHashSet>> chunks(Arena a) {
    return data.getOrDefault(a.id(), Long2ObjectMaps.emptyMap()).long2ObjectEntrySet();
  }

  /** Remove all tracking data for the arena. */
  public void clear(Arena a) {
    Long2ObjectMap<ShortOpenHashSet> m = data.remove(a.id());
    if (m != null) m.clear();
  }

  /** Number of tracked chunks for arena (debug/testing). */
  public int chunkCount(Arena a) {
    Long2ObjectMap<ShortOpenHashSet> m = data.get(a.id());
    return m == null ? 0 : m.size();
  }
}

