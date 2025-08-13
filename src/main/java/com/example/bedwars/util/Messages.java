package com.example.bedwars.util;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

/** Utility for retrieving, formatting and sending plugin messages. */
public final class Messages {
  private final BedwarsPlugin plugin;
  private final FileConfiguration cfg;

  public Messages(BedwarsPlugin plugin) {
    this.plugin = plugin;
    File f = new File(plugin.getDataFolder(), "messages.yml");
    if (!f.exists()) {
      plugin.saveResource("messages.yml", false);
    }
    this.cfg = YamlConfiguration.loadConfiguration(f);
  }

  /**
   * Retrieves a message by key and translates color codes.
   * If the key is missing, the key itself is returned.
   */
  public String get(String path) {
    String raw = cfg.getString(path, path);
    return ChatColor.translateAlternateColorCodes('&', raw);
  }

  /** Formats a message with the given placeholders and color codes. */
  public String format(String path, Map<String, ?> placeholders) {
    String msg = cfg.getString(path, path);
    if (placeholders != null) {
      for (Map.Entry<String, ?> e : placeholders.entrySet()) {
        msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
      }
    }
    return ChatColor.translateAlternateColorCodes('&', msg);
  }

  /** Sends a formatted message to a player without placeholders. */
  public void send(Player p, String key) {
    send(p, key, Map.of());
  }

  /** Sends a formatted message with placeholders to a player. */
  public void send(Player p, String key, Map<String, ?> tokens) {
    p.sendMessage(format(key, tokens));
  }

  /** Broadcasts a message to all players in an arena without placeholders. */
  public void broadcast(Arena a, String key) {
    for (Player p : plugin.contexts().playersInArena(a.id())) {
      send(p, key);
    }
  }

  /** Broadcasts a formatted message with placeholders to an arena. */
  public void broadcast(Arena a, String key, Map<String, ?> tokens) {
    String msg = format(key, tokens);
    for (Player p : plugin.contexts().playersInArena(a.id())) {
      p.sendMessage(msg);
    }
  }

  public List<String> getList(String path) {
    return cfg.getStringList(path).stream()
        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
        .collect(Collectors.toList());
  }
}
