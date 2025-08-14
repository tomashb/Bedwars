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
  private int[] trapCosts = {1,2,4};
  private int trapSlots = 3;
  private final Map<String,Integer> healTasks = new HashMap<>();

  public static final class UpgradeDef {
    public final String name;
    private final int[] soloCosts;
    private final int[] teamCosts;

    public UpgradeDef(String name, int[] soloCosts, int[] teamCosts) {
      this.name = name;
      this.soloCosts = soloCosts != null ? soloCosts : new int[0];
      this.teamCosts = teamCosts != null ? teamCosts : new int[0];
    }

    public int maxLevel() { return Math.max(soloCosts.length, teamCosts.length); }

    public int costForLevel(int teamSize, int level) {
      int[] arr = teamSize <= 2 ? soloCosts : teamCosts;
      return (level >=1 && level <= arr.length) ? arr[level-1] : Integer.MAX_VALUE;
    }
  }
  public static final class TrapDef {
    public final String name; public final Material icon;
    public TrapDef(String name, Material icon){
      this.name = name; this.icon = icon != null ? icon : Material.BARRIER;
    }
  }

  public UpgradeService(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
    loadDefs();
  }

  private void loadDefs() {
    File f = new File(plugin.getDataFolder(), "upgrades.json");
    if (!f.exists()) plugin.saveResource("upgrades.json", false);
    YamlConfiguration y = YamlConfiguration.loadConfiguration(f);

    // parse upgrade items
    for (Map<?,?> any : y.getMapList("items")) {
      @SuppressWarnings("unchecked") Map<String,Object> raw = (Map<String,Object>) any;
      String id = String.valueOf(raw.get("id"));
      UpgradeType ut;
      try { ut = UpgradeType.valueOf(id.toUpperCase()); } catch (Exception ex) { continue; }
      String name = String.valueOf(raw.getOrDefault("name", id));
      @SuppressWarnings("unchecked") Map<String,Object> costsMap = (Map<String,Object>) raw.get("costs");
      int[] solo = toIntArray(costsMap != null ? costsMap.get("SOLO_DOUBLES") : null);
      int[] team = toIntArray(costsMap != null ? costsMap.get("THREES_FOURS") : null);
      defs.put(ut, new UpgradeDef(name, solo, team));
    }

    // parse traps
    ConfigurationSection trapsSec = y.getConfigurationSection("traps");
    if (trapsSec != null) {
      trapSlots = trapsSec.getInt("max_slots", 3);
      java.util.List<?> q = trapsSec.getList("slot_costs");
      if (q != null && !q.isEmpty()) {
        trapCosts = q.stream().mapToInt(o -> ((Number)o).intValue()).toArray();
      }
      for (Map<?,?> rawAny : trapsSec.getMapList("list")) {
        @SuppressWarnings("unchecked") Map<String,Object> raw = (Map<String,Object>) rawAny;
        String id = String.valueOf(raw.get("id"));
        TrapType t;
        try { t = TrapType.valueOf(id.toUpperCase()); } catch (Exception ex) { continue; }
        String name = String.valueOf(raw.getOrDefault("name", id));
        Material icon = Material.matchMaterial(String.valueOf(raw.getOrDefault("icon", "TRIPWIRE_HOOK")));
        traps.put(t, new TrapDef(name, icon));
      }
    }
  }

  private static int[] toIntArray(Object obj) {
    if (obj instanceof java.util.List<?> list) {
      int[] arr = new int[list.size()];
      for (int i = 0; i < list.size(); i++) {
        Object v = list.get(i);
        if (v instanceof Number n) arr[i] = n.intValue();
        else {
          try { arr[i] = Integer.parseInt(String.valueOf(v)); } catch (Exception ex) { arr[i] = 0; }
        }
      }
      return arr;
    }
    return new int[0];
  }

  // Accessors
  public UpgradeDef def(UpgradeType t) { return defs.get(t); }
  public TrapDef trapDef(TrapType t) { return traps.get(t); }
  public int trapCost(int queueSize) {
    int idx = Math.min(queueSize, trapCosts.length - 1);
    return trapCosts[idx];
  }
  public int trapSlots() { return trapSlots; }

  private boolean matches(Player p, String arenaId, TeamColor team) {
    String ar = ctx.getArena(p);
    return ar != null && ar.equals(arenaId) && ctx.getTeam(p) == team;
  }

  private void applySharpness(Player p) {
    for (ItemStack is : p.getInventory().getContents()) {
      if (is == null) continue;
      Material m = is.getType();
      if (!m.name().endsWith("_SWORD") && !m.name().endsWith("_AXE")) continue;
      ItemMeta meta = is.getItemMeta();
      if (meta == null) continue;
      meta.addEnchant(Enchantment.SHARPNESS, 1, true);
      is.setItemMeta(meta);
    }
    ItemStack off = p.getInventory().getItemInOffHand();
    if (off != null) {
      Material m = off.getType();
      if (m.name().endsWith("_SWORD") || m.name().endsWith("_AXE")) {
        ItemMeta meta = off.getItemMeta();
        if (meta != null) {
          meta.addEnchant(Enchantment.SHARPNESS, 1, true);
          off.setItemMeta(meta);
        }
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

  private void applyCushioned(Player p, int level) {
    if (level <= 0) return;
    ItemStack boots = p.getInventory().getBoots();
    if (boots == null) return;
    ItemMeta meta = boots.getItemMeta();
    if (meta == null) return;
    meta.addEnchant(Enchantment.PROTECTION_FALL, level, true);
    boots.setItemMeta(meta);
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

  public void applyCushionedBoots(String arenaId, TeamColor team, int level) {
    for (Player p : plugin.getServer().getOnlinePlayers()) {
      if (!matches(p, arenaId, team)) continue;
      applyCushioned(p, level);
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
    plugin.generators().recomputeForge(arenaId);
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
