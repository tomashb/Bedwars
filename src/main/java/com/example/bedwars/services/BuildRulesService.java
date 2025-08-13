package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Service enforcing build rules and tracking placed blocks.
 */
public final class BuildRulesService {
  private final BedwarsPlugin plugin;
  private final PlacedBlocksStore placed = new PlacedBlocksStore();
  private final Set<Material> allowedPlace;

  public BuildRulesService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.allowedPlace = plugin.getConfig().getStringList("rules.place-allow").stream()
        .map(Material::matchMaterial)
        .filter(java.util.Objects::nonNull)
        .collect(Collectors.toCollection(HashSet::new));
  }

  public boolean isAllowed(Material mat) {
    return allowedPlace.contains(mat);
  }

  public void recordPlacement(String arenaId, Location loc) {
    placed.add(arenaId, loc);
  }

  public boolean wasPlaced(String arenaId, Location loc) {
    return placed.contains(arenaId, loc);
  }

  public void removePlaced(String arenaId, Location loc) {
    placed.remove(arenaId, loc);
  }

  public String arenaAt(Location loc) {
    return placed.arenaAt(loc);
  }

  public void clearArena(String arenaId) {
    placed.clearArena(arenaId);
  }
}
