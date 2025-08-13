package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.service.PlayerContextService;
import com.example.bedwars.service.PlayerContextService.Context;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies upgrades to players based on team state.
 * This implementation is intentionally simple and may be expanded later.
 */
public final class UpgradeService {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final Map<String,Integer> healTasks = new HashMap<>();

  public UpgradeService(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  private boolean matches(Player p, String arenaId, TeamColor team) {
    return ctx.get(p).map(c -> c.arenaId.equals(arenaId) && c.team == team).orElse(false);
  }

  public void applySharpness(String arenaId, TeamColor team) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      for (ItemStack is : p.getInventory().getContents()) {
        if (is != null && is.getType().name().endsWith("_SWORD")) {
          is.addEnchantment(Enchantment.DAMAGE_ALL, 1);
        }
      }
    }
  }

  public void applyProtection(String arenaId, TeamColor team, int level) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      ItemStack[] armor = p.getInventory().getArmorContents();
      for (ItemStack is : armor) {
        if (is != null && is.getType() != org.bukkit.Material.AIR) {
          is.addEnchantment(Enchantment.PROTECTION_ENVIRONMENTAL, level);
        }
      }
      p.getInventory().setArmorContents(armor);
    }
  }

  public void applyManicMiner(String arenaId, TeamColor team, int level) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      if (level > 0) {
        p.addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, level-1, true, false, false));
      } else {
        p.removePotionEffect(PotionEffectType.FAST_DIGGING);
      }
    }
  }

  public void applyHealPool(String arenaId, TeamColor team, boolean enable) {
    String key = arenaId + ":" + team.name();
    if (enable) {
      if (healTasks.containsKey(key)) return;
      int task = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
        plugin.arenas().get(arenaId).ifPresent(a -> {
          TeamData td = a.team(team);
          if (td.spawn() == null) return;
          for (Player p : plugin.getServer().getOnlinePlayers()) {
            if (!matches(p, arenaId, team)) continue;
            if (p.getLocation().distanceSquared(td.spawn()) <= 36) {
              p.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, 60, 0, true, false, false));
            }
          }
        });
      }, 0L, 40L).getTaskId();
      healTasks.put(key, task);
    } else {
      Integer id = healTasks.remove(key);
      if (id != null) plugin.getServer().getScheduler().cancelTask(id);
    }
  }

  public void applyForge(String arenaId, TeamColor team, int level) {
    // placeholder - actual generator boosting is implemented later
  }
}
