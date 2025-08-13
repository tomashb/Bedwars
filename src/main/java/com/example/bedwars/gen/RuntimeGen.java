package com.example.bedwars.gen;

import org.bukkit.Location;

/**
 * Runtime state for a generator during a game.
 */
public final class RuntimeGen {
  public final GeneratorType type;
  public final Location dropLoc;     // world resolved
  public final int tier;
  public final int baseInterval;     // ticks
  public final int baseAmount;
  public int current;                // ticks remaining
  public boolean isBase;             // base generator (Forge)

  public RuntimeGen(GeneratorType type, Location dropLoc, int tier, int baseInterval, int baseAmount) {
    this.type = type;
    this.dropLoc = dropLoc;
    this.tier = tier;
    this.baseInterval = Math.max(1, baseInterval);
    this.baseAmount = Math.max(1, baseAmount);
    this.current = this.baseInterval;
  }
}
