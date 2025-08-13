package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import java.io.File;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

/**
 * Loads and provides access to shop configuration (items and upgrades).
 */
public final class ShopConfig {
  private final BedwarsPlugin plugin;
  private final Map<String, Material> currencies = new HashMap<>();
  private final Map<ShopCategory, List<ShopItem>> items = new EnumMap<>(ShopCategory.class);

  public static final class UpgradeDef {
    public final List<Map<Material,Integer>> costs;
    public final int maxLevel;
    public final String name;
    public UpgradeDef(List<Map<Material,Integer>> costs, int maxLevel, String name) {
      this.costs = costs; this.maxLevel = maxLevel; this.name = name;
    }
  }
  private final Map<UpgradeType, UpgradeDef> upgrades = new EnumMap<>(UpgradeType.class);

  public static final class TrapDef {
    public final Map<Material,Integer> cost;
    public final String name;
    public TrapDef(Map<Material,Integer> cost, String name) {
      this.cost = cost; this.name = name;
    }
  }
  private final Map<TrapType, TrapDef> traps = new EnumMap<>(TrapType.class);

  public ShopConfig(BedwarsPlugin plugin) {
    this.plugin = plugin;
    load();
  }

  private void load() {
    File f = new File(plugin.getDataFolder(), "shop.yml");
    if (!f.exists()) plugin.saveResource("shop.yml", false);
    YamlConfiguration y = YamlConfiguration.loadConfiguration(f);

    ConfigurationSection cur = y.getConfigurationSection("currencies");
    if (cur != null) {
      for (String k : cur.getKeys(false)) {
        Material m = Material.matchMaterial(cur.getString(k, ""));
        if (m != null) currencies.put(k.toUpperCase(Locale.ROOT), m);
      }
    }

    ConfigurationSection itemsSec = y.getConfigurationSection("items");
    if (itemsSec != null) {
      for (String catKey : itemsSec.getKeys(false)) {
        ShopCategory cat;
        try { cat = ShopCategory.valueOf(catKey); } catch (IllegalArgumentException ex) { continue; }
        List<ShopItem> list = new ArrayList<>();
        for (Map<?,?> map : itemsSec.getMapList(catKey)) {
          String id = String.valueOf(map.get("id"));
          String matStr = String.valueOf(map.get("mat"));

          ShopItem.Builder b = ShopItem.builder()
              .id(id)
              .amount(map.containsKey("amount") ? ((Number) map.get("amount")).intValue() : 1)
              .name(map.containsKey("name") ? String.valueOf(map.get("name")) : "");

          if ("WOOL_TEAM".equalsIgnoreCase(matStr)) {
            b.mat(Material.WHITE_WOOL).teamColored(true);
          } else {
            Material mat = Material.matchMaterial(matStr);
            if (mat != null) b.mat(mat);
          }

          Map<Material,Integer> price = parseCost(castMap(map.get("price")));
          for (var e : price.entrySet()) {
            b.price(e.getKey(), e.getValue());
          }

          Object enSec = map.get("enchants");
          if (enSec instanceof Map<?,?> m2) {
            for (var e : m2.entrySet()) {
              Enchantment en = Enchantment.getByName(String.valueOf(e.getKey()));
              if (en != null) b.enchant(en, ((Number) e.getValue()).intValue());
            }
          }

          list.add(b.build());
        }
        items.put(cat, list);
      }
    }

    ConfigurationSection upSec = y.getConfigurationSection("upgrades");
    if (upSec != null) {
      for (String upKey : upSec.getKeys(false)) {
        if (upKey.equalsIgnoreCase("TRAPS")) {
          ConfigurationSection tSec = upSec.getConfigurationSection(upKey);
          if (tSec != null) {
            for (String t : tSec.getKeys(false)) {
              TrapType tt;
              try { tt = TrapType.valueOf(t.toUpperCase(Locale.ROOT)); } catch (Exception ex) { continue; }
              ConfigurationSection sec = tSec.getConfigurationSection(t);
              if (sec == null) continue;
              Map<Material,Integer> cost = parseCost(sec.getConfigurationSection("cost"));
              String name = sec.getString("name", t);
              traps.put(tt, new TrapDef(cost, name));
            }
          }
        } else {
          UpgradeType type;
          try { type = UpgradeType.valueOf(upKey); } catch (IllegalArgumentException ex) { continue; }
          ConfigurationSection sec = upSec.getConfigurationSection(upKey);
          if (sec == null) continue;
          String name = sec.getString("name", upKey);
          int max = sec.getInt("maxLevel", 0);
          List<Map<Material,Integer>> costs = new ArrayList<>();
          if (sec.isConfigurationSection("cost")) {
            costs.add(parseCost(sec.getConfigurationSection("cost")));
          }
          for (Map<?,?> m : sec.getMapList("costs")) {
            costs.add(parseCost(castMap(m)));
          }
          if (max == 0) max = costs.size();
          upgrades.put(type, new UpgradeDef(costs, max, name));
        }
      }
    }
  }

  private Map<Material,Integer> parseCost(ConfigurationSection sec) {
    if (sec == null) return Map.of();
    Map<Material,Integer> m = new EnumMap<>(Material.class);
    for (String k : sec.getKeys(false)) {
      Material mat = currencies.get(k.toUpperCase(Locale.ROOT));
      if (mat != null) m.put(mat, sec.getInt(k));
    }
    return m;
  }

  private Map<Material,Integer> parseCost(Map<String,Object> raw) {
    if (raw == null) return Map.of();
    Map<Material,Integer> m = new EnumMap<>(Material.class);
    for (var e : raw.entrySet()) {
      Material mat = currencies.get(e.getKey().toUpperCase(Locale.ROOT));
      if (mat != null) m.put(mat, ((Number)e.getValue()).intValue());
    }
    return m;
  }

  @SuppressWarnings("unchecked")
  private Map<String,Object> castMap(Object o) {
    if (o instanceof Map<?,?> map) {
      Map<String,Object> res = new LinkedHashMap<>();
      for (var e : map.entrySet()) {
        res.put(String.valueOf(e.getKey()), e.getValue());
      }
      return res;
    }
    return new LinkedHashMap<>();
  }

  public List<ShopItem> items(ShopCategory cat) {
    return items.getOrDefault(cat, List.of());
  }

  public UpgradeDef upgrade(UpgradeType t) { return upgrades.get(t); }
  public TrapDef trap(TrapType t) { return traps.get(t); }
}
