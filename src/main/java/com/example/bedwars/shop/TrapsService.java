package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

/**
 * Minimal trap queue handling.
 */
public final class TrapsService {
  private final BedwarsPlugin plugin;
  private final Map<String, Deque<TrapType>> queues = new HashMap<>();

  public TrapsService(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  private String key(String arenaId, TeamColor team) {
    return arenaId + ":" + team.name();
  }

  public Deque<TrapType> getQueue(String arenaId, TeamColor team) {
    return queues.computeIfAbsent(key(arenaId, team), k -> new ArrayDeque<>(3));
  }
}
