package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.shop.ShopCategory;
import com.example.bedwars.shop.ShopConfig;
import com.example.bedwars.shop.ShopItem;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.bukkit.Location;
import org.bukkit.Material;

/**
 * Service enforcing build rules and tracking placed blocks.
 */
public final class BuildRulesService {
  private final BedwarsPlugin plugin;
  private final PlacedBlocksStore placed = new PlacedBlocksStore();
  private final Set<Material> staticAllowed;
  private final Set<Material> dynamicAllowed = java.util.EnumSet.noneOf(Material.class);

  public BuildRulesService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    List<String> mats = new ArrayList<>();
    mats.addAll(plugin.getConfig().getStringList("build.extra_allowed_materials"));
    mats.addAll(plugin.getConfig().getStringList("build.allowed_materials"));
    if (mats.isEmpty()) mats = plugin.getConfig().getStringList("rules.place-allow");
    this.staticAllowed = mats.stream()
        .map(Material::matchMaterial)
        .filter(Objects::nonNull)
        .collect(Collectors.toCollection(HashSet::new));
  }

  /** Rebuild dynamic whitelist from shop config. */
  public void rebuildWhitelistFromShop(ShopConfig shop) {
    dynamicAllowed.clear();
    for (ShopCategory cat : ShopCategory.values()) {
      for (ShopItem item : shop.items(cat)) {
        Material m = item.mat;
        if (m != null && m.isBlock()) dynamicAllowed.add(m);
      }
    }
    plugin.getLogger().info("[Build] Dynamic whitelist size=" + dynamicAllowed.size());
  }

  public boolean isAllowed(Material mat) {
    if (plugin.getConfig().getBoolean("build.wool_all_colors_allowed", true)
        && mat.name().endsWith("_WOOL")) {
      return true;
    }
    boolean dynamic = plugin.getConfig().getBoolean("build.allow_all_shop_blocks", true)
        && dynamicAllowed.contains(mat);
    return dynamic || staticAllowed.contains(mat);
  }

  public void recordPlacement(String arenaId, Location loc) {
    placed.add(arenaId, loc);
  }

  public boolean wasPlaced(String arenaId, Location loc) {
    return placed.contains(arenaId, loc);
  }

  public void removePlaced(String arenaId, Location loc) {
    placed.remove(arenaId, loc);
  }

  public String arenaAt(Location loc) {
    return placed.arenaAt(loc);
  }

  public void clearArena(String arenaId) {
    placed.clearArena(arenaId);
  }
}
