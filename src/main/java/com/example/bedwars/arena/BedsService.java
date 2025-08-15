package com.example.bedwars.arena;

import java.util.EnumMap;
import java.util.Map;
import org.bukkit.Location;

/**
 * Stores bed positions for teams inside an arena and provides lookup helpers.
 */
public final class BedsService {
  private final EnumMap<TeamColor, BedData> beds = new EnumMap<>(TeamColor.class);

  public Map<TeamColor, BedData> all() {
    return java.util.Collections.unmodifiableMap(beds);
  }

  public void set(TeamColor team, Location head, Location foot) {
    beds.put(team, new BedData(head, foot));
  }

  public BedData get(TeamColor team) {
    return beds.get(team);
  }

  public void remove(TeamColor team) {
    beds.remove(team);
  }

  public boolean isBroken(TeamColor team) {
    return !beds.containsKey(team);
  }

  /** Finds the owner team of a bed block. */
  public TeamColor ownerOf(Location loc) {
    for (var e : beds.entrySet()) {
      BedData bd = e.getValue();
      if (BedData.sameBlock(bd.head(), loc) || BedData.sameBlock(bd.foot(), loc)) {
        return e.getKey();
      }
    }
    return null;
  }
}

