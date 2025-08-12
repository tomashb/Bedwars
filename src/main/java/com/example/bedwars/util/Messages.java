package com.example.bedwars.util;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

public final class Messages {
  private final FileConfiguration cfg;

  public Messages(JavaPlugin plugin) {
    File f = new File(plugin.getDataFolder(), "messages.yml");
    if (!f.exists()) {
      plugin.saveResource("messages.yml", false);
    }
    this.cfg = YamlConfiguration.loadConfiguration(f);
  }

  public String get(String path) {
    String raw = cfg.getString(path, path);
    return ChatColor.translateAlternateColorCodes('&', raw);
  }

  public String format(String path, Map<String, ?> placeholders) {
    String msg = get(path);
    for (Map.Entry<String, ?> e : placeholders.entrySet()) {
      msg = msg.replace("{" + e.getKey() + "}", String.valueOf(e.getValue()));
    }
    return msg;
  }

  public List<String> getList(String path) {
    return cfg.getStringList(path).stream()
        .map(s -> ChatColor.translateAlternateColorCodes('&', s))
        .collect(Collectors.toList());
  }
}
