package com.example.bedwars.gen;

import org.bukkit.Location;
import java.util.Objects;
import java.util.UUID;

/**
 * Resource generator spawning items over time.
 */
public final class Generator {
  private final UUID id = UUID.randomUUID();
  private final GeneratorType type;
  private Location location;
  private int tier = 1;
  private int intervalTicks = 20 * 120; // default interval
  private int amount = 1;

  public Generator(GeneratorType type, Location location) {
    this.type = Objects.requireNonNull(type);
    this.location = Objects.requireNonNull(location);
  }

  public UUID id() { return id; }
  public GeneratorType type() { return type; }
  public Location location() { return location; }
  public int tier() { return tier; }
  public int intervalTicks() { return intervalTicks; }
  public int amount() { return amount; }

  public Generator setLocation(Location loc) { this.location = Objects.requireNonNull(loc); return this; }
  public Generator setTier(int tier) { this.tier = Math.max(1, Math.min(3, tier)); return this; }
  public Generator setIntervalTicks(int ticks) { this.intervalTicks = Math.max(1, ticks); return this; }
  public Generator setAmount(int amount) { this.amount = Math.max(1, amount); return this; }
}
