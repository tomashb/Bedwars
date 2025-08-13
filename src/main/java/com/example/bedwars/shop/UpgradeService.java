package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.game.PlayerContextService;
import java.io.File;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies upgrades to players based on team state and handles diamond purchases.
 */
public final class UpgradeService {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final Map<UpgradeType, UpgradeDef> defs = new EnumMap<>(UpgradeType.class);
  private final Map<TrapType, TrapDef> traps = new EnumMap<>(TrapType.class);
  private final Map<String,Integer> healTasks = new HashMap<>();

  public static final class UpgradeDef {
    public final int costDiamond;
    public final boolean perLevel;
    public final int max;
    public final String name;
    public UpgradeDef(int costDiamond, boolean perLevel, int max, String name) {
      this.costDiamond = costDiamond; this.perLevel = perLevel; this.max = max; this.name = name;
    }
  }
  public static final class TrapDef {
    public final int costDiamond; public final String name;
    public TrapDef(int costDiamond, String name){ this.costDiamond = costDiamond; this.name = name; }
  }

  public UpgradeService(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
    loadDefs();
  }

  private void loadDefs() {
    File f = new File(plugin.getDataFolder(), "upgrades.yml");
    if (!f.exists()) plugin.saveResource("upgrades.yml", false);
    YamlConfiguration y = YamlConfiguration.loadConfiguration(f);
    ConfigurationSection up = y.getConfigurationSection("upgrades");
    if (up == null) return;
    for (String k : up.getKeys(false)) {
      ConfigurationSection sec = up.getConfigurationSection(k);
      if (sec == null) continue;
      if (k.startsWith("trap_")) {
        TrapType t;
        try { t = TrapType.valueOf(k.substring(5).toUpperCase()); } catch (Exception ex) { continue; }
        traps.put(t, new TrapDef(sec.getInt("cost_diamond"), sec.getString("name", k)));
      } else {
        UpgradeType ut;
        try { ut = UpgradeType.valueOf(k.toUpperCase()); } catch (Exception ex) { continue; }
        int cost = sec.getInt("cost_diamond");
        boolean per = sec.getBoolean("per_level", false);
        int max = sec.getInt("max", 1);
        String name = sec.getString("name", k);
        defs.put(ut, new UpgradeDef(cost, per, max, name));
      }
    }
  }

  // Accessors
  public UpgradeDef def(UpgradeType t) { return defs.get(t); }
  public TrapDef trapDef(TrapType t) { return traps.get(t); }

  private boolean matches(Player p, String arenaId, TeamColor team) {
    String ar = ctx.getArena(p);
    return ar != null && ar.equals(arenaId) && ctx.getTeam(p) == team;
  }

  private void applySharpness(Player p) {
    for (ItemStack is : p.getInventory().getContents()) {
      if (is == null) continue;
      Material m = is.getType();
      if (!m.name().endsWith("_SWORD")) continue;
      ItemMeta meta = is.getItemMeta();
      if (meta == null) continue;
      meta.addEnchant(Enchantment.SHARPNESS, 1, true);
      is.setItemMeta(meta);
    }
    ItemStack off = p.getInventory().getItemInOffHand();
    if (off != null && off.getType().name().endsWith("_SWORD")) {
      ItemMeta meta = off.getItemMeta();
      if (meta != null) {
        meta.addEnchant(Enchantment.SHARPNESS, 1, true);
        off.setItemMeta(meta);
      }
    }
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
    // placeholder - actual generator boosting is implemented elsewhere
  }

  // === Diamond purchase helpers ===
  public int countDiamonds(Player p) {
    return count(p.getInventory(), Material.DIAMOND);
  }

  public boolean tryBuyDiamonds(Player p, int cost) {
    int have = countDiamonds(p);
    if (have < cost) {
      p.sendMessage(plugin.messages().format("upgrades.need_diamond", Map.of("cost", cost)));
      return false;
    }
    remove(p.getInventory(), Material.DIAMOND, cost);
    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
    return true;
  }

  private int count(Inventory inv, Material m) {
    int total = 0;
    for (ItemStack is : inv.getContents()) {
      if (is != null && is.getType() == m) total += is.getAmount();
    }
    return total;
  }

  private void remove(Inventory inv, Material m, int amount) {
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack is = inv.getItem(i);
      if (is == null || is.getType() != m) continue;
      int take = Math.min(amount, is.getAmount());
      is.setAmount(is.getAmount() - take);
      amount -= take;
      if (amount <= 0) break;
    }
  }
}
