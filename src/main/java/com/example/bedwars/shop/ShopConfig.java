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
  private final Map<String, ShopItem> byId = new HashMap<>();

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
    byId.clear();
    File f = new File(plugin.getDataFolder(), "shop.yml");
    if (!f.exists()) plugin.saveResource("shop.yml", false);
    YamlConfiguration y = YamlConfiguration.loadConfiguration(f);

    for (ShopCategory cat : ShopCategory.values()) {
      java.util.List<Map<?,?>> rawList = y.getMapList(cat.name());
      if (rawList == null) continue;
      List<ShopItem> list = new ArrayList<>();
      for (Map<?,?> rawAny : rawList) {
        @SuppressWarnings("unchecked")
        Map<String,Object> raw = (Map<String,Object>) rawAny;

        String id   = asString(raw.get("id"), "");
        String name = asString(raw.get("name"), id);
        String icon = asString(raw.get("icon"), "STONE");
        Material mat = Material.matchMaterial(icon);
        boolean teamCol = false;
        int amount = 1;

        Map<String,Object> give = asMap(raw.get("give"));
        if (give != null) {
          amount = asInt(give.get("amount"), 1);
          String matName = asString(give.get("material"), "");
          String teamMat = asString(give.get("material_by_team_color"), "");
          if (!teamMat.isEmpty()) {
            mat = Material.WHITE_WOOL;
            teamCol = true;
          } else if (!matName.isEmpty()) {
            Material m2 = Material.matchMaterial(matName);
            if (m2 != null) mat = m2;
          }
        }
        if (mat == null) mat = Material.STONE;

        Map<String,Object> priceMap = asMap(raw.get("price"));
        Currency currency = Currency.IRON;
        int cost = 0;
        if (priceMap != null && !priceMap.isEmpty()) {
          var entry = priceMap.entrySet().iterator().next();
          currency = asCurrency(entry.getKey(), Currency.IRON);
          cost = asInt(entry.getValue(), 1);
        }

        ShopItem.Builder b = ShopItem.builder()
            .id(id)
            .name(name)
            .amount(amount)
            .currency(currency)
            .cost(cost)
            .teamColored(teamCol)
            .mat(mat);

        Map<String,Object> meta = asMap(raw.get("meta"));
        Map<String,Object> enchMap = (meta != null) ? asMap(meta.get("enchant")) : null;
        if (enchMap != null) {
          for (var e : enchMap.entrySet()) {
            Enchantment en = Enchantment.getByName(e.getKey());
            if (en != null) {
              int lvl = asInt(e.getValue(), 1);
              b.enchant(en, lvl);
            }
          }
        }
        if (meta != null) {
          String typeName = asString(meta.get("type"), "");
          if (!typeName.isEmpty()) {
            try {
              org.bukkit.potion.PotionType pt = org.bukkit.potion.PotionType.valueOf(typeName.toUpperCase(java.util.Locale.ROOT));
              int amp = asInt(meta.get("amplifier"), 1);
              int secs = asInt(meta.get("seconds"), 30);
              boolean hide = asBool(meta.get("hide_particles"), false);
              b.mat(Material.POTION);
              b.bwItem(id);
              b.potion(new PotionSpec(pt, amp, secs, hide));
            } catch (Exception ignore) {}
          }
        }
        ShopItem built = b.build();
        list.add(built);
        byId.put(id, built);
      }
      items.put(cat, list);
    }

    if (plugin.buildRules() != null) {
      plugin.buildRules().rebuildWhitelistFromShop(this);
    }
  }

  public List<ShopItem> items(ShopCategory cat) {
    return items.getOrDefault(cat, List.of());
  }

  public ShopItem byId(String id) {
    return byId.get(id);
  }
}

