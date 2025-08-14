package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import org.bukkit.configuration.ConfigurationSection;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

/**
 * Resolves and exposes generator profile settings for arenas.
 */
public final class GeneratorProfileService {
  public enum GenProfile { SOLO_DOUBLES, TRIOS_QUADS }

  public static final class BaseSpec {
    public final int interval;
    public final int cap;
    public BaseSpec(int interval, int cap) {
      this.interval = interval;
      this.cap = cap;
    }
  }

  public static final class ForgeMult {
    public final double iron;
    public final double gold;
    public ForgeMult(double iron, double gold) {
      this.iron = iron;
      this.gold = gold;
    }
  }

  public static final class ProfileSpec {
    public final BaseSpec iron;
    public final BaseSpec gold;
    public final Map<Integer, ForgeMult> forge = new HashMap<>();
    public ProfileSpec(BaseSpec iron, BaseSpec gold) {
      this.iron = iron;
      this.gold = gold;
    }
  }

  private final BedwarsPlugin plugin;
  private final Map<GenProfile, ProfileSpec> profiles = new EnumMap<>(GenProfile.class);
  private final Map<String, GenProfile> active = new HashMap<>();

  public GeneratorProfileService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    load();
  }

  private void load() {
    ConfigurationSection root = plugin.getConfig().getConfigurationSection("generators.profiles");
    if (root == null) return;
    for (String key : root.getKeys(false)) {
      GenProfile prof;
      switch (key.toLowerCase()) {
        case "solo_doubles" -> prof = GenProfile.SOLO_DOUBLES;
        case "trios_quads" -> prof = GenProfile.TRIOS_QUADS;
        default -> { continue; }
      }
      ConfigurationSection sec = root.getConfigurationSection(key);
      if (sec == null) continue;
      ConfigurationSection base = sec.getConfigurationSection("base");
      if (base == null) continue;
      ConfigurationSection ironSec = base.getConfigurationSection("iron");
      ConfigurationSection goldSec = base.getConfigurationSection("gold");
      BaseSpec iron = new BaseSpec(
          ironSec != null ? ironSec.getInt("interval_ticks", 20) : 20,
          ironSec != null ? ironSec.getInt("cap", 48) : 48);
      BaseSpec gold = new BaseSpec(
          goldSec != null ? goldSec.getInt("interval_ticks", 100) : 100,
          goldSec != null ? goldSec.getInt("cap", 16) : 16);
      ProfileSpec ps = new ProfileSpec(iron, gold);
      ConfigurationSection forge = sec.getConfigurationSection("forge_multipliers");
      if (forge != null) {
        for (String lvlStr : forge.getKeys(false)) {
          int lvl;
          try { lvl = Integer.parseInt(lvlStr); } catch (Exception ex) { continue; }
          ConfigurationSection fsec = forge.getConfigurationSection(lvlStr);
          double im = fsec.getDouble("iron", 1.0);
          double gm = fsec.getDouble("gold", 1.0);
          ps.forge.put(lvl, new ForgeMult(im, gm));
        }
      }
      profiles.put(prof, ps);
    }
  }

  /** Resolve profile for arena based on config and arena properties. */
  public GenProfile resolve(Arena arena) {
    String mode = plugin.getConfig().getString("generators.profile", "auto");
    if (!"auto".equalsIgnoreCase(mode)) {
      return switch (mode.toLowerCase()) {
        case "solo_doubles" -> GenProfile.SOLO_DOUBLES;
        case "trios_quads" -> GenProfile.TRIOS_QUADS;
        default -> GenProfile.SOLO_DOUBLES;
      };
    }
    int perTeam = arena.maxTeamSize();
    return perTeam <= 2 ? GenProfile.SOLO_DOUBLES : GenProfile.TRIOS_QUADS;
  }

  public ProfileSpec spec(GenProfile p) { return profiles.get(p); }

  public void setActive(String arenaId, GenProfile p) { active.put(arenaId, p); }

  public GenProfile getActive(String arenaId) { return active.getOrDefault(arenaId, GenProfile.SOLO_DOUBLES); }

  public int baseInterval(GenProfile p, GeneratorType type) {
    ProfileSpec s = spec(p); if (s == null) return 20;
    return switch (type) {
      case TEAM_IRON -> s.iron.interval;
      case TEAM_GOLD -> s.gold.interval;
      default -> 20;
    };
  }

  public int cap(GenProfile p, GeneratorType type) {
    ProfileSpec s = spec(p); if (s == null) return 0;
    return switch (type) {
      case TEAM_IRON -> s.iron.cap;
      case TEAM_GOLD -> s.gold.cap;
      default -> 0;
    };
  }

  public double forgeMultiplier(GenProfile p, int level, GeneratorType type) {
    ProfileSpec s = spec(p); if (s == null) return 1.0;
    ForgeMult fm = s.forge.get(level);
    if (fm == null) fm = s.forge.getOrDefault(0, new ForgeMult(1.0,1.0));
    return switch (type) {
      case TEAM_IRON -> fm.iron;
      case TEAM_GOLD -> fm.gold;
      default -> 1.0;
    };
  }
}

