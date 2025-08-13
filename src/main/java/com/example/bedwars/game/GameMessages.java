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

  private String format(String key, Map<String, Object> params) {
    String msg = plugin.messages().get(key);
    if (msg == null) return "";
    if (params != null) {
      for (var e : params.entrySet()) {
        msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
      }
    }
    return plugin.messages().get("prefix") + msg;
  }

  public void send(Player p, String key, Map<String, Object> params) {
    p.sendMessage(format(key, params));
  }

  public void broadcast(Arena a, String key, Map<String, Object> params) {
    String msg = format(key, params);
    for (Player p : ctx.playersInArena(a.id())) p.sendMessage(msg);
  }
}
