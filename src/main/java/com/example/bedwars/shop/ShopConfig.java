package com.example.bedwars.shop;

import com.example.bedwars.BedwarsPlugin;
import java.io.File;
import java.util.*;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;

/**
 * Loads and provides access to item shop configuration.
 */
public final class ShopConfig {
  private final BedwarsPlugin plugin;
  private final Map<ShopCategory, List<ShopItem>> items = new EnumMap<>(ShopCategory.class);

  public ShopConfig(BedwarsPlugin plugin) {
    this.plugin = plugin;
    load();
  }

  public void load() {
    items.clear();
    File f = new File(plugin.getDataFolder(), "shop.yml");
    if (!f.exists()) plugin.saveResource("shop.yml", false);
    YamlConfiguration y = YamlConfiguration.loadConfiguration(f);

    ConfigurationSection cats = y.getConfigurationSection("categories");
    if (cats == null) return;
    for (String cKey : cats.getKeys(false)) {
      ShopCategory cat;
      try { cat = ShopCategory.valueOf(cKey.toUpperCase(Locale.ROOT)); }
      catch (IllegalArgumentException ex) { continue; }
      List<ShopItem> list = new ArrayList<>();
      for (Map<?,?> raw : cats.getMapList(cKey)) {
        String id = String.valueOf(raw.get("id"));
        String name = String.valueOf(raw.get("name"));
        String matStr = String.valueOf(raw.get("material"));
        int amount = raw.containsKey("amount") ? ((Number)raw.get("amount")).intValue() : 1;
        boolean teamCol = Boolean.parseBoolean(String.valueOf(raw.getOrDefault("team_colored", false)));
        boolean perm = Boolean.parseBoolean(String.valueOf(raw.getOrDefault("permanent", false)));
        ConfigurationSection costSec = null;
        Object costObj = raw.get("cost");
        Currency currency = Currency.IRON;
        int cost = 0;
        if (costObj instanceof Map<?,?> map) {
          Object cur = map.get("currency");
          Object amt = map.get("amount");
          if (cur != null) {
            try { currency = Currency.valueOf(String.valueOf(cur).toUpperCase(Locale.ROOT)); } catch (Exception ignore) {}
          }
          if (amt instanceof Number) cost = ((Number)amt).intValue();
        }

        ShopItem.Builder b = ShopItem.builder().id(id).name(name).amount(amount)
            .currency(currency).cost(cost).teamColored(teamCol).permanent(perm);
        if ("WOOL_TEAM".equalsIgnoreCase(matStr)) {
          b.mat(Material.WHITE_WOOL).teamColored(true);
        } else {
          Material m = Material.matchMaterial(matStr);
          if (m != null) b.mat(m);
        }
        Object enchObj = raw.get("enchants");
        if (enchObj instanceof Map<?,?> emap) {
          for (var e : emap.entrySet()) {
            Enchantment en = Enchantment.getByName(String.valueOf(e.getKey()));
            if (en != null) b.enchant(en, ((Number)e.getValue()).intValue());
          }
        }
        list.add(b.build());
      }
      items.put(cat, list);
    }
  }

  public List<ShopItem> items(ShopCategory cat) {
    return items.getOrDefault(cat, List.of());
  }
}

