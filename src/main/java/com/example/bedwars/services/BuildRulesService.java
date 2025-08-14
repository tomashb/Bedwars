package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.shop.ShopCategory;
import com.example.bedwars.shop.ShopConfig;
import com.example.bedwars.shop.ShopItem;
import com.example.bedwars.arena.Arena;
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
  private final ArenaCleaner cleaner;
  private final Set<Material> staticAllowed;
  private final Set<Material> dynamicAllowed = java.util.EnumSet.noneOf(Material.class);

  public BuildRulesService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.cleaner = new ArenaCleaner(plugin, placed);
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

  public PlacedBlocksStore store() { return placed; }

  public void recordPlaced(Arena a, org.bukkit.block.Block b) {
    placed.record(a, b);
  }

  public boolean wasPlaced(Arena a, Location loc) {
    return placed.contains(a.id(), loc);
  }

  public void removePlaced(Arena a, Location loc) {
    placed.remove(a.id(), loc);
  }

  public String arenaAt(Location loc) {
    return placed.arenaAt(loc);
  }

  /** Start asynchronous cleanup of placed blocks for the arena. */
  public void cleanupPlaced(Arena arena) {
    cleaner.cleanupPlacedBlocks(arena);
  }

  /** Remove all placed blocks synchronously. */
  public void cleanupPlacedSync(Arena arena) {
    cleaner.cleanupSync(arena);
  }
}
