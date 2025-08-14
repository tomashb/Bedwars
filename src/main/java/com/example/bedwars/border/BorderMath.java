package com.example.bedwars.border;

import org.bukkit.Location;

/** Geometry helpers for border calculations. */
public final class BorderMath {
  private BorderMath() {}

  /** Returns whether the point is outside a circle. */
  public static boolean outside2D(Location loc, double cx, double cz, double radius) {
    double dx = loc.getX() - cx;
    double dz = loc.getZ() - cz;
    return (dx * dx + dz * dz) > radius * radius;
  }

  /** Clamp the location inside the circle on the X/Z plane. */
  public static Location clampInside(Location to, double cx, double cz, double radius) {
    double dx = to.getX() - cx;
    double dz = to.getZ() - cz;
    double dist = Math.sqrt(dx * dx + dz * dz);
    if (dist <= radius) return to;
    double k = radius / dist;
    return new Location(to.getWorld(), cx + dx * k, to.getY(), cz + dz * k, to.getYaw(), to.getPitch());
  }
}
