package com.example.bedwars.border;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import java.io.File;
import java.util.EnumSet;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

/** Service providing border configuration and world border application per arena. */
public final class BorderService {

  /** Action to take when a player crosses the border. */
  public enum Action { PUSH_BACK, TELEPORT, CANCEL }

  /** Border settings for an arena. */
  public static final class Settings {
    boolean enabled = true;
    double radius = 120;
    Double centerX; // null means auto
    Double centerZ;
    int warning = 6;
    boolean damageEnabled = false;
    double damageAmount = 0.2;
    double damageBuffer = 2.0;
    boolean clampY = true;
    double minY = 1.0;
    double maxY = 128.0;
    Action onCross = Action.PUSH_BACK;
    double pushStrength = 0.8;
  }

  private final BedwarsPlugin plugin;
  private final Map<String, Settings> cache = new ConcurrentHashMap<>();
  private final Map<String, Vector2D> centers = new ConcurrentHashMap<>();

  public BorderService(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  /** Get settings for an arena. */
  public Settings cfg(Arena a) {
    return cache.computeIfAbsent(a.id(), id -> load(a));
  }

  private Settings load(Arena a) {
    Settings s = new Settings();
    ConfigurationSection base = plugin.getConfig().getConfigurationSection("border");
    if (base != null) applySection(s, base);
    File f = new File(plugin.getDataFolder(), "arenas/" + a.id() + ".yml");
    if (f.exists()) {
      YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
      ConfigurationSection sec = y.getConfigurationSection("border");
      if (sec != null) applySection(s, sec);
    }
    return s;
  }

  private void applySection(Settings s, ConfigurationSection sec) {
    s.enabled = sec.getBoolean("enabled", s.enabled);
    if (sec.isDouble("radius")) s.radius = sec.getDouble("radius");
    if (sec.isConfigurationSection("center")) {
      ConfigurationSection c = sec.getConfigurationSection("center");
      s.centerX = c.getDouble("x");
      s.centerZ = c.getDouble("z");
    }
    s.warning = sec.getInt("warning", s.warning);
    if (sec.isConfigurationSection("damage")) {
      ConfigurationSection d = sec.getConfigurationSection("damage");
      s.damageEnabled = d.getBoolean("enabled", s.damageEnabled);
      s.damageAmount = d.getDouble("amount", s.damageAmount);
      s.damageBuffer = d.getDouble("buffer", s.damageBuffer);
    }
    if (sec.isConfigurationSection("clamp_y")) {
      ConfigurationSection c = sec.getConfigurationSection("clamp_y");
      s.clampY = c.getBoolean("enabled", s.clampY);
      s.minY = c.getDouble("min", s.minY);
      s.maxY = c.getDouble("max", s.maxY);
    }
    if (sec.isConfigurationSection("actions")) {
      ConfigurationSection c = sec.getConfigurationSection("actions");
      String act = c.getString("on_cross", s.onCross.name());
      try { s.onCross = Action.valueOf(act); } catch (IllegalArgumentException ignored) {}
      s.pushStrength = c.getDouble("push_back_strength", s.pushStrength);
    }
  }

  /** Compute center for arena using config or team spawns. */
  public Vector2D centerFor(Arena a) {
    return centers.computeIfAbsent(a.id(), id -> {
      Settings s = cfg(a);
      if (s.centerX != null && s.centerZ != null) {
        return new Vector2D(s.centerX, s.centerZ);
      }
      // Average team spawns of active teams
      double sumX = 0, sumZ = 0; int count = 0;
      for (TeamColor c : EnumSet.copyOf(a.activeTeams())) {
        Location l = a.team(c).spawn();
        if (l != null) { sumX += l.getX(); sumZ += l.getZ(); count++; }
      }
      if (count == 0) return new Vector2D(0.0, 0.0);
      return new Vector2D(sumX / count, sumZ / count);
    });
  }

  /** Apply world border to arena world. */
  public void apply(Arena a) {
    Settings s = cfg(a);
    if (!s.enabled) return;
    World w = Bukkit.getWorld(a.world().name());
    if (w == null) return;
    WorldBorder wb = w.getWorldBorder();
    Vector2D c = centerFor(a);
    wb.setCenter(c.x(), c.z());
    wb.setSize(s.radius * 2.0);
    wb.setWarningDistance(s.warning);
    if (s.damageEnabled) {
      wb.setDamageAmount(s.damageAmount);
      wb.setDamageBuffer(s.damageBuffer);
    } else {
      wb.setDamageAmount(0.0);
    }
  }

  /** Clear cached settings for an arena. */
  public void reload(String arenaId) {
    cache.remove(arenaId);
    centers.remove(arenaId);
  }
}
