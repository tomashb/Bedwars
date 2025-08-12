package com.example.bedwars.gui.placeholders;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.AdminView;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class InfoMenu {
  private final BedwarsPlugin plugin;
  public InfoMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p) {
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.INFO, null), 54,
        plugin.messages().get("admin.root.info"));
    p.openInventory(inv);
  }
}
