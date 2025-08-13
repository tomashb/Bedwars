package com.example.bedwars.gen;

import org.bukkit.Location;
import java.util.UUID;

/**
 * Runtime state for a generator during a game.
 */
public final class RuntimeGen {
  public final UUID id;
  public final GeneratorType type;           // TEAM_IRON, DIAMOND, ...
  public final Location dropLoc;             // resolved to world
  public final boolean teamBase;             // true if generator belongs to a team base
  public int tier;                           // 1..3 (diamond/mid), 1 for base
  public int baseInterval;                   // base interval from config (ticks)
  public int interval;                       // effective interval (ticks)
  public int amount;                         // items per drop
  public int cap;                            // cap of items on ground
  public int cooldown;                       // ticks remaining before next drop

  public RuntimeGen(UUID id, GeneratorType type, Location dropLoc, boolean teamBase) {
    this.id = id;
    this.type = type;
    this.dropLoc = dropLoc;
    this.teamBase = teamBase;
    this.tier = 1;
  }
}
