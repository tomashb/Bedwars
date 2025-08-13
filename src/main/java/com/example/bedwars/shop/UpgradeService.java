package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.service.PlayerContextService;
import com.example.bedwars.service.PlayerContextService.Context;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.enchantments.Enchantment;

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

  private void applySharpness(Player p) {
    ItemStack is = p.getInventory().getItemInMainHand();
    if (is == null) return;
    Material m = is.getType();
    if (!m.name().endsWith("_SWORD")) return;

    ItemMeta meta = is.getItemMeta();
    if (meta == null) return;

    meta.addEnchant(Enchantment.SHARPNESS, 1, true);
    is.setItemMeta(meta);
  }

  private void applyProtection(Player p, int level) {
    if (level <= 0) return;
    for (ItemStack armor : p.getInventory().getArmorContents()) {
      if (armor == null) continue;
      ItemMeta meta = armor.getItemMeta();
      if (meta == null) continue;
      meta.addEnchant(Enchantment.PROTECTION, level, true);
      armor.setItemMeta(meta);
    }
  }

  private void applyManicMiner(Player p, int level) {
    PotionEffectType type = PotionEffectType.HASTE;
    if (level <= 0) {
      p.removePotionEffect(type);
      return;
    }
    int amplifier = Math.max(0, level - 1);
    p.addPotionEffect(new PotionEffect(type, Integer.MAX_VALUE, amplifier, false, false, false));
  }

  public void applySharpness(String arenaId, TeamColor team) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      applySharpness(p);
    }
  }

  public void applyProtection(String arenaId, TeamColor team, int level) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      applyProtection(p, level);
    }
  }

  public void applyManicMiner(String arenaId, TeamColor team, int level) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      applyManicMiner(p, level);
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
