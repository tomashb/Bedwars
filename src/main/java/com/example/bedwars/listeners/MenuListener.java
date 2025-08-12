package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class MenuListener implements Listener {
  private final BedwarsPlugin plugin;

  public MenuListener(BedwarsPlugin plugin){ this.plugin = plugin; }

  @EventHandler
  public void onClick(InventoryClickEvent e) {
    Inventory top = e.getView().getTopInventory();
    if (!(top.getHolder() instanceof BWMenuHolder holder)) return;

    // Bloquer toute interaction par défaut
    e.setCancelled(true);
    if (!(e.getWhoClicked() instanceof Player p)) return;
    if (!p.hasPermission("bedwars.admin")) { p.sendMessage(plugin.messages().get("admin.no-perm")); return; }
    if (e.getClickedInventory() != top) return; // on ignore l'inventaire bas
    if (e.getCurrentItem() == null) return;

    int slot = e.getRawSlot();

    if (holder.view == AdminView.ROOT) {
      switch (slot) {
        case RootMenu.SLOT_ARENAS    -> plugin.menus().open(AdminView.ARENAS, p, null);
        case RootMenu.SLOT_CREATE    -> plugin.menus().open(AdminView.ARENAS, p, null); // Étape 4: lancera le wizard
        case RootMenu.SLOT_RULES     -> plugin.menus().open(AdminView.RULES_EVENTS, p, null);
        case RootMenu.SLOT_SHOPS     -> plugin.menus().open(AdminView.NPC_SHOPS, p, null);
        case RootMenu.SLOT_GENS      -> plugin.menus().open(AdminView.GENERATORS, p, null);
        case RootMenu.SLOT_ROTATION  -> plugin.menus().open(AdminView.ROTATION, p, null);
        case RootMenu.SLOT_RESET     -> plugin.menus().open(AdminView.RESET, p, null);
        case RootMenu.SLOT_DIAG      -> plugin.menus().open(AdminView.DIAGNOSTICS, p, null);
        case RootMenu.SLOT_INFO      -> plugin.menus().open(AdminView.INFO, p, null);
        default -> {}
      }
    }

    // Bloquer shift-click, swap, number keys (sécurité UX)
    if (e.getClick().isShiftClick() || e.getClick() == ClickType.NUMBER_KEY) e.setCancelled(true);
  }
}
