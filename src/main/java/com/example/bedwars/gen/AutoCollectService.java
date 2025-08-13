package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.ops.Keys;
import org.bukkit.Location;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Handles optional auto collection of generator drops.
 */
public final class AutoCollectService {
  private final BedwarsPlugin plugin;
  private final double radius;

  public AutoCollectService(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.radius = plugin.getConfig().getDouble("generators.auto_collect_radius", 0.0);
  }

  public boolean isEnabled() {
    return radius > 0.0;
  }

  /**
   * Try to give the item directly to the player. If inventory is full the item
   * will be dropped at the generator location.
   */
  public void giveOrDrop(Player p, ItemStack stack, Location dropLoc, String arenaId, UUID genId) {
    if (p != null && isEnabled() && p.getLocation().distanceSquared(dropLoc) <= radius * radius) {
      var leftover = p.getInventory().addItem(stack);
      if (leftover.isEmpty()) {
        p.getWorld().playSound(p.getLocation(), org.bukkit.Sound.ENTITY_ITEM_PICKUP, 0.2f, 1f);
        return;
      }
      // fallback to drop remaining items
      stack = leftover.values().iterator().next();
    }
    Item item = dropLoc.getWorld().dropItem(dropLoc, stack);
    var pdc = item.getPersistentDataContainer();
    Keys keys = plugin.keys();
    pdc.set(keys.ARENA_ID(), PersistentDataType.STRING, arenaId);
    pdc.set(keys.GEN_ID(), PersistentDataType.STRING, genId.toString());
  }
}
