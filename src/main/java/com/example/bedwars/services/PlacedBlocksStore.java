package com.example.bedwars.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Location;

/**
 * Tracks blocks placed by players per arena.
 */
public final class PlacedBlocksStore {
  private final Map<String, String> placed = new ConcurrentHashMap<>();

  private String key(Location loc) {
    return loc.getWorld().getName()+":"+loc.getBlockX()+","+loc.getBlockY()+","+loc.getBlockZ();
  }

  /** Record a placed block for the given arena. */
  public void add(String arenaId, Location loc) {
    placed.put(key(loc), arenaId);
  }

  /** Check if the block at location belongs to the given arena and was placed. */
  public boolean contains(String arenaId, Location loc) {
    return arenaId.equals(placed.get(key(loc)));
  }

  /** Remove a placed block entry. */
  public void remove(String arenaId, Location loc) {
    String k = key(loc);
    if (arenaId.equals(placed.get(k))) placed.remove(k);
  }

  /** Get arena id associated with location, or null. */
  public String arenaAt(Location loc) {
    return placed.get(key(loc));
  }

  /** Clear all placed blocks for an arena. */
  public void clearArena(String arenaId) {
    placed.entrySet().removeIf(e -> arenaId.equals(e.getValue()));
  }
}
