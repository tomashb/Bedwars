package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Menu allowing admins to choose an arena mode. */
public final class ArenaModeMenu {
  private final BedwarsPlugin plugin;
  public static final int SLOT_8X1 = 1;
  public static final int SLOT_8X2 = 3;
  public static final int SLOT_4X3 = 5;
  public static final int SLOT_4X4 = 7;
  public static final int SLOT_BACK = 8;

  public ArenaModeMenu(BedwarsPlugin plugin) { this.plugin = plugin; }

  public void open(Player p, String arenaId) {
    String title = plugin.messages().get("editor.mode-title").replace("{arena}", arenaId);
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ARENA_MODE, arenaId), 9, title);
    inv.setItem(SLOT_8X1, icon(Material.LIME_WOOL, "§a8 équipes de 1"));
    inv.setItem(SLOT_8X2, icon(Material.GREEN_WOOL, "§a8 équipes de 2"));
    inv.setItem(SLOT_4X3, icon(Material.CYAN_WOOL, "§a4 équipes de 3"));
    inv.setItem(SLOT_4X4, icon(Material.BLUE_WOOL, "§a4 équipes de 4"));
    inv.setItem(SLOT_BACK, icon(Material.COMPASS, "Retour"));
    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, String name) {
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if (im != null) { im.setDisplayName(name); it.setItemMeta(im); }
    return it;
  }
}
