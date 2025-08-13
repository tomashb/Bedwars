package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Reads balancing values from configuration and exposes helper methods used by
 * the {@link GeneratorManager}.
 */
public final class GenBalance {
  private final BedwarsPlugin plugin;

  private final Map<Integer, Double> forgeGoldMul = new HashMap<>();
  private final Map<Integer, Double> forgeEmeraldMul = new HashMap<>();
  private final int teamIronInterval;
  private final int teamGoldInterval;
  private final int teamEmeraldInterval;
  private final int teamIronAmount;
  private final int teamGoldAmount;
  private final int teamEmeraldAmount;
  private final int teamIronCap;
  private final int teamGoldCap;
  private final int teamEmeraldCap;
  private final int diamondTier1;
  private final int diamondTier2;
  private final int diamondTier3;
  private final int emeraldTier1;
  private final int emeraldTier2;
  private final int emeraldTier3;

  public GenBalance(BedwarsPlugin plugin) {
    this.plugin = plugin;
    var cfg = plugin.getConfig();

    ConfigurationSection team = cfg.getConfigurationSection("generators.team");
    teamIronInterval = team.getConfigurationSection("iron").getInt("interval_ticks", 8);
    teamIronAmount = team.getConfigurationSection("iron").getInt("amount", 1);
    teamIronCap = team.getConfigurationSection("iron").getInt("cap", 48);
    teamGoldInterval = team.getConfigurationSection("gold").getInt("interval_ticks", 180);
    teamGoldAmount = team.getConfigurationSection("gold").getInt("amount", 1);
    teamGoldCap = team.getConfigurationSection("gold").getInt("cap", 12);
    teamEmeraldInterval = team.getConfigurationSection("emerald").getInt("interval_ticks", 1200);
    teamEmeraldAmount = team.getConfigurationSection("emerald").getInt("amount", 1);
    teamEmeraldCap = team.getConfigurationSection("emerald").getInt("cap", 4);

    ConfigurationSection upgrades = cfg.getConfigurationSection("generators.upgrades");
    for (String key : upgrades.getKeys(false)) {
      ConfigurationSection s = upgrades.getConfigurationSection(key);
      forgeGoldMul.put(levelFromKey(key), s.getDouble("gold_interval_multiplier", 1.0));
      forgeEmeraldMul.put(levelFromKey(key), s.getDouble("emerald_interval_multiplier", 1.0));
    }

    ConfigurationSection diamond = cfg.getConfigurationSection("global_tiers.diamond");
    diamondTier1 = diamond.getInt("tier1_interval_ticks", 600);
    diamondTier2 = diamond.getInt("tier2_interval_ticks", 400);
    diamondTier3 = diamond.getInt("tier3_interval_ticks", 300);
    ConfigurationSection emerald = cfg.getConfigurationSection("global_tiers.emerald");
    emeraldTier1 = emerald.getInt("tier1_interval_ticks", 1300);
    emeraldTier2 = emerald.getInt("tier2_interval_ticks", 1000);
    emeraldTier3 = emerald.getInt("tier3_interval_ticks", 640);
  }

  private int levelFromKey(String key) {
    return switch (key) {
      case "forge_I" -> 1;
      case "forge_II" -> 2;
      case "forge_III" -> 3;
      case "molten" -> 4;
      default -> 0;
    };
  }

  public int diamondIntervalForTier(int tier) {
    return switch (tier) {
      case 2 -> diamondTier2;
      case 3 -> diamondTier3;
      default -> diamondTier1;
    };
  }

  public int emeraldIntervalForTier(int tier) {
    return switch (tier) {
      case 2 -> emeraldTier2;
      case 3 -> emeraldTier3;
      default -> emeraldTier1;
    };
  }

  public int capFor(RuntimeGen g) {
    return switch (g.type) {
      case TEAM_IRON -> teamIronCap;
      case TEAM_GOLD -> teamGoldCap;
      case TEAM_EMERALD -> teamEmeraldCap;
      case DIAMOND -> plugin.getConfig().getInt("entity_caps.diamond_island", 8);
      case EMERALD -> plugin.getConfig().getInt("entity_caps.mid_emerald", 8);
    };
  }

  public int baseInterval(RuntimeGen g) {
    return switch (g.type) {
      case TEAM_IRON -> teamIronInterval;
      case TEAM_GOLD -> teamGoldInterval;
      case TEAM_EMERALD -> teamEmeraldInterval;
      case DIAMOND -> diamondTier1;
      case EMERALD -> emeraldTier1;
    };
  }

  public int amount(RuntimeGen g) {
    return switch (g.type) {
      case TEAM_IRON -> teamIronAmount;
      case TEAM_GOLD -> teamGoldAmount;
      case TEAM_EMERALD -> teamEmeraldAmount;
      default -> 1;
    };
  }

  public double forgeGoldMul(int level) {
    return forgeGoldMul.getOrDefault(level, 1.0);
  }

  public boolean forgeEmeraldEnabled(int level) {
    return level >= 2; // forge_II and above
  }

  public double forgeEmeraldMul(int level) {
    return forgeEmeraldMul.getOrDefault(level, 1.0);
  }

  /**
   * Compute effective interval for the generator depending on tiers and upgrades.
   */
  public int intervalFor(RuntimeGen g, int diamondTier, int emeraldTier, int teamForge) {
    if (g.type == GeneratorType.DIAMOND) {
      return diamondIntervalForTier(diamondTier);
    }
    if (g.type == GeneratorType.EMERALD) {
      return emeraldIntervalForTier(emeraldTier);
    }
    if (g.type == GeneratorType.TEAM_GOLD) {
      double mul = forgeGoldMul(teamForge);
      return Math.max(1, (int) Math.round(teamGoldInterval * mul));
    }
    if (g.type == GeneratorType.TEAM_EMERALD) {
      if (!forgeEmeraldEnabled(teamForge)) return Integer.MAX_VALUE;
      double mul = forgeEmeraldMul(teamForge);
      return Math.max(20, (int) Math.round(teamEmeraldInterval * mul));
    }
    return teamIronInterval;
  }
}
