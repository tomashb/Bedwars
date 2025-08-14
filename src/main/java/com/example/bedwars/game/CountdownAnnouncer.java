package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import org.bukkit.entity.Player;

/** Announces countdown messages without spamming chat. */
public final class CountdownAnnouncer {
  private final BedwarsPlugin plugin;
  private final Set<Integer> milestones;
  private final boolean actionbarEnabled;

  public CountdownAnnouncer(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.milestones = new TreeSet<>(plugin.getConfig().getIntegerList("countdown.chat_milestones"));
    this.actionbarEnabled = plugin.getConfig().getBoolean("countdown.actionbar_enabled", true);
  }

  public void tick(Arena arena, int secondsLeft) {
    if (milestones.contains(secondsLeft)) {
      plugin.messages().broadcast(arena, "countdown.chat", Map.of("sec", secondsLeft));
    } else if (actionbarEnabled) {
      String ab = plugin.messages().format("actionbar.countdown", Map.of("sec", secondsLeft));
      for (Player p : plugin.contexts().playersInArena(arena.id())) {
        plugin.actionBar().push(p, ab, 1);
      }
    }
  }
}
