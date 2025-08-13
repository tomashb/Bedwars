package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.PlayerInventory;

/** Prevents removing team armor from legs and feet slots. */
public final class ArmorLockListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public ArmorLockListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  private boolean enabled() {
    return plugin.getConfig().getBoolean("armor.lock_legs_feet", true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInvClick(InventoryClickEvent e) {
    if (!enabled()) return;
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    if (!(e.getClickedInventory() instanceof PlayerInventory)) return;
    int slot = e.getSlot();
    if (slot == 36 || slot == 37) {
      e.setCancelled(true);
    }
  }

  @EventHandler(ignoreCancelled = true)
  public void onDrag(InventoryDragEvent e) {
    if (!enabled()) return;
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (ctx.getArena(p) == null) return;
    if (e.getRawSlots().contains(36) || e.getRawSlots().contains(37)) {
      e.setCancelled(true);
    }
  }
}
