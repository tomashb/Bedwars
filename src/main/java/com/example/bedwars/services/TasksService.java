package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import java.util.Map;

/**
 * Provides simple counts of plugin tasks for diagnostics.
 */
public final class TasksService {
  private final BedwarsPlugin plugin;

  public TasksService(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  /**
   * Returns counts of running tasks related to an arena.
   *
   * <p>The numbers are best-effort and mainly for debug output.</p>
   */
  public Map<String, Integer> summary(String arenaId) {
    int sb = plugin.getConfig().getBoolean("scoreboard.enabled", true) ? 1 : 0;
    int ab = plugin.getConfig().getBoolean("actionbar.enabled", true) ? 1 : 0;
    int gens = plugin.generators().runtimeCount(arenaId);
    int other = 0;
    return Map.of("sb", sb, "ab", ab, "gens", gens, "others", other);
  }
}

