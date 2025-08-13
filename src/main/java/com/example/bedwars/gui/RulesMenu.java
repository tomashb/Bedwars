package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

/** Simple placeholder rules GUI for players. */
public final class RulesMenu {
  private final BedwarsPlugin plugin;
  public RulesMenu(BedwarsPlugin plugin) { this.plugin = plugin; }

  public void open(Player p) {
    Inventory inv = Bukkit.createInventory(null, 27, "Règles");
    p.openInventory(inv);
  }
}
