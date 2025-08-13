package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.util.Map;
import org.bukkit.entity.Player;

/** Helper for game related messages. */
public final class GameMessages {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public GameMessages(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  public String raw(String key) {
    String msg = plugin.messages().get(key);
    return msg != null ? msg : key;
  }

  public String format(String key, Map<String, ?> tokens) {
    String msg = raw(key);
    if (tokens != null) {
      for (var e : tokens.entrySet()) {
        msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
      }
    }
    return plugin.messages().get("prefix") + msg;
  }

  public void send(Player p, String key) {
    p.sendMessage(format(key, null));
  }

  public void send(Player p, String key, Map<String, ?> tokens) {
    p.sendMessage(format(key, tokens));
  }

  public void broadcast(Arena a, String key) {
    String msg = format(key, null);
    for (Player p : ctx.playersInArena(a.id())) p.sendMessage(msg);
  }

  public void broadcast(Arena a, String key, Map<String, ?> tokens) {
    String msg = format(key, tokens);
    for (Player p : ctx.playersInArena(a.id())) p.sendMessage(msg);
  }
}
