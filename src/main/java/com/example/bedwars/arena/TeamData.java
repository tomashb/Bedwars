package com.example.bedwars.arena;

import org.bukkit.Location;
import java.util.Objects;

import com.example.bedwars.shop.TeamUpgradesState;

/**
 * Data describing a team in an arena.
 */
public final class TeamData {
  private Location spawn;        // null until defined
  private Location bedBlock;     // "head" block of the bed
  private int maxPlayers = 4;
  private final TeamUpgradesState upgrades = new TeamUpgradesState();

  public Location spawn() { return spawn; }
  public Location bedBlock() { return bedBlock; }
  public int maxPlayers() { return maxPlayers; }
  public TeamUpgradesState upgrades() { return upgrades; }

  public TeamData setSpawn(Location loc) { this.spawn = Objects.requireNonNull(loc); return this; }
  public TeamData setBedBlock(Location loc) { this.bedBlock = Objects.requireNonNull(loc); return this; }
  public TeamData setMaxPlayers(int max) { this.maxPlayers = Math.max(1, max); return this; }
}
