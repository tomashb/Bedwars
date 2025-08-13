package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;

/** Prevents removing team armor pieces from inventory. */
public final class ArmorLockListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public ArmorLockListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  private boolean enabled() {
    return plugin.getConfig().getBoolean("gameplay.lock_armor", true);
  }

  private static boolean isArmor(Material m) {
    return m != null && (m.name().endsWith("_HELMET") || m.name().endsWith("_CHESTPLATE")
        || m.name().endsWith("_LEGGINGS") || m.name().endsWith("_BOOTS"));
  }

  @EventHandler(ignoreCancelled = true)
  public void onInvClick(InventoryClickEvent e) {
    if (!enabled()) return;
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    if (!(e.getClickedInventory() instanceof PlayerInventory)) return;
    int slot = e.getSlot();
    if (slot >= 36 && slot <= 39) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.cannot_remove_armor");
      return;
    }
    if (e.isShiftClick()) {
      ItemStack cur = e.getCurrentItem();
      if (cur != null && isArmor(cur.getType())) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.cannot_remove_armor");
      }
    }
    if (e.getClick() == ClickType.DROP || e.getClick() == ClickType.CONTROL_DROP) {
      if (slot >= 36 && slot <= 39) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.cannot_remove_armor");
      }
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onDrag(InventoryDragEvent e) {
    if (!enabled()) return;
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    if (e.getRawSlots().stream().anyMatch(s -> s >= 36 && s <= 39)) {
      e.setCancelled(true);
      if (e.getWhoClicked() instanceof Player p)
        plugin.messages().send(p, "errors.cannot_remove_armor");
    }
  }
}
