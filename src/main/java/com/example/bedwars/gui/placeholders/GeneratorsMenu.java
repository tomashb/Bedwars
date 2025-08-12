package com.example.bedwars.gui.placeholders;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.gui.AdminView;
import com.example.bedwars.gui.BWMenuHolder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

public final class GeneratorsMenu {
  private final BedwarsPlugin plugin;
  public GeneratorsMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p) {
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.GENERATORS, null), 54,
        plugin.messages().get("admin.root.generators"));
    p.openInventory(inv);
  }
}
