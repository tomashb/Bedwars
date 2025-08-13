package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.util.YamlIO;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Handles weighted arena rotation based on rotation.yml.
 */
public final class RotationManager {
  private final BedwarsPlugin plugin;
  private final File file;
  private boolean enabled;
  private final List<Entry> entries = new ArrayList<>();

  private static final class Entry {
    final String id; final int weight;
    Entry(String id, int weight){ this.id=id; this.weight=weight; }
  }

  public RotationManager(BedwarsPlugin plugin){
    this.plugin = plugin;
    this.file = new File(plugin.getDataFolder(), "rotation.yml");
    reload();
  }

  /** Reload configuration from disk. */
  public void reload(){
    entries.clear();
    enabled = false;
    if (!file.exists()) return;
    YamlConfiguration y = YamlIO.load(file);
    ConfigurationSection sec = y.getConfigurationSection("rotation");
    if (sec == null) return;
    enabled = sec.getBoolean("enabled", false);
    List<Map<?,?>> list = sec.getMapList("list");
    for (Map<?,?> m : list) {
      Object idObj = m.get("id");
      Object wObj = m.get("weight");
      if (idObj == null || wObj == null) continue;
      int w;
      try { w = Integer.parseInt(String.valueOf(wObj)); }
      catch (NumberFormatException ex) { continue; }
      if (w <= 0) continue;
      String id = String.valueOf(idObj);
      // only include arenas that exist
      if (plugin.arenas().get(id).isEmpty()) continue;
      entries.add(new Entry(id, w));
    }
  }

  /** Picks the next arena id based on weights, if rotation enabled. */
  public Optional<String> pickNext(){
    if (!enabled || entries.isEmpty()) return Optional.empty();
    int total = entries.stream().mapToInt(e -> e.weight).sum();
    int r = ThreadLocalRandom.current().nextInt(total);
    for (Entry e : entries) {
      if (r < e.weight) return Optional.of(e.id);
      r -= e.weight;
    }
    return Optional.empty();
  }
}
