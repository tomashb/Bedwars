package com.example.bedwars.arena;

import org.bukkit.Location;

/** Holds the two blocks composing a team's bed. */
public record BedData(Location head, Location foot) {
  public BedData {
    head = head.clone();
    foot = foot.clone();
  }

  /**
   * Compares two locations by block coordinates and world.
   */
  public static boolean sameBlock(Location a, Location b) {
    if (a == null || b == null) return false;
    if (a.getWorld() == null || b.getWorld() == null) return false;
    return a.getWorld().equals(b.getWorld())
        && a.getBlockX() == b.getBlockX()
        && a.getBlockY() == b.getBlockY()
        && a.getBlockZ() == b.getBlockZ();
  }
}

