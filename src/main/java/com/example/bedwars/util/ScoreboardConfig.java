package com.example.bedwars.util;

import com.example.bedwars.BedwarsPlugin;
import java.io.File;
import java.util.List;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

/** Configuration holder for scoreboard settings. */
public final class ScoreboardConfig {
  private final FileConfiguration cfg;

  public ScoreboardConfig(BedwarsPlugin plugin) {
    File f = new File(plugin.getDataFolder(), "scoreboard.yml");
    if (!f.exists()) {
      plugin.saveResource("scoreboard.yml", false);
    }
    this.cfg = YamlConfiguration.loadConfiguration(f);
  }

  public boolean enabled() {
    return cfg.getBoolean("scoreboard.enabled", true);
  }

  public int refreshWaitingTicks() {
    return cfg.getInt("scoreboard.refresh_waiting_ticks", 20);
  }

  public int refreshRunningTicks() {
    return cfg.getInt("scoreboard.refresh_running_ticks", 20);
  }

  public String waitingTitle() {
    return cfg.getString("scoreboard.waiting.title", "&6BedWars");
  }

  public List<String> waitingLines() {
    return cfg.getStringList("scoreboard.waiting.lines");
  }

  public int tipsRotateSeconds() {
    return cfg.getInt("scoreboard.tips.rotate_seconds", 5);
  }

  public List<String> tipsPool() {
    return cfg.getStringList("scoreboard.tips.pool");
  }

  public String footer() {
    return cfg.getString("scoreboard.footer", "");
  }
}
