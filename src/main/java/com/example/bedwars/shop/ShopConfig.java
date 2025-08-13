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

  @SuppressWarnings("unchecked")
  private static Map<String,Object> asMap(Object o) {
    return (o instanceof Map<?,?> m) ? (Map<String,Object>) m : null;
  }
  private static String asString(Object o, String def) {
    return (o == null) ? def : String.valueOf(o);
  }
  private static boolean asBool(Object o, boolean def) {
    if (o instanceof Boolean b) return b;
    if (o instanceof String s) return Boolean.parseBoolean(s.trim());
    return def;
  }
  private static int asInt(Object o, int def) {
    if (o instanceof Number n) return n.intValue();
    if (o instanceof String s) { try { return Integer.parseInt(s.trim()); } catch (Exception ignore) {} }
    return def;
  }
  private static Currency asCurrency(Object o, Currency def) {
    if (o == null) return def;
    try { return Currency.valueOf(String.valueOf(o).toUpperCase(java.util.Locale.ROOT)); }
    catch (Exception ignore) { return def; }
  }

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
      java.util.List<Map<?,?>> rawList = cats.getMapList(cKey);
      for (Map<?,?> rawAny : rawList) {
        @SuppressWarnings("unchecked")
        Map<String,Object> raw = (Map<String,Object>) rawAny;

        String id      = asString(raw.get("id"), "");
        String name    = asString(raw.get("name"), id);
        String matName = asString(raw.get("material"), "STONE");
        Material mat = Material.matchMaterial(matName);
        boolean teamCol = asBool(raw.get("team_colored"), false);
        if ("WOOL_TEAM".equalsIgnoreCase(matName)) {
          mat = Material.WHITE_WOOL;
          teamCol = true;
        }
        if (mat == null) mat = Material.STONE;

        int amount = asInt(raw.get("amount"), 1);
        boolean perm = asBool(raw.get("permanent"), false);

        Map<String,Object> costMap = asMap(raw.get("cost"));
        int cost = 0;
        Currency currency = Currency.IRON;
        if (costMap != null) {
          currency = asCurrency(costMap.get("currency"), Currency.IRON);
          cost = asInt(costMap.get("amount"), 1);
        }

        ShopItem.Builder b = ShopItem.builder()
            .id(id)
            .name(name)
            .amount(amount)
            .currency(currency)
            .cost(cost)
            .teamColored(teamCol)
            .permanent(perm)
            .mat(mat);

        Map<String,Object> enchMap = asMap(raw.get("enchants"));
        if (enchMap != null) {
          for (var e : enchMap.entrySet()) {
            Enchantment en = Enchantment.getByName(e.getKey());
            if (en != null) {
              int lvl = asInt(e.getValue(), 1);
              b.enchant(en, lvl);
            }
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

