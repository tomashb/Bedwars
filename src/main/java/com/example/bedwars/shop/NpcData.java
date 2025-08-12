package com.example.bedwars.shop;

import org.bukkit.Location;
import java.util.Objects;
import java.util.UUID;

/**
 * Data for a shop NPC.
 */
public final class NpcData {
  private final UUID id = UUID.randomUUID();
  private final NpcType type;
  private Location location;

  public NpcData(NpcType type, Location location) {
    this.type = Objects.requireNonNull(type);
    this.location = Objects.requireNonNull(location);
  }

  public UUID id() { return id; }
  public NpcType type() { return type; }
  public Location location() { return location; }

  public NpcData setLocation(Location location) { this.location = Objects.requireNonNull(location); return this; }
}
