package com.example.bedwars.gui;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

public final class RootMenu {
  private final BedwarsPlugin plugin;

  // mapping slots
  public static final int SLOT_ARENAS = 10;
  public static final int SLOT_CREATE = 12;
  public static final int SLOT_RULES = 14;
  public static final int SLOT_SHOPS = 16;
  public static final int SLOT_GENS = 28;
  public static final int SLOT_ROTATION = 30;
  public static final int SLOT_RESET = 32;
  public static final int SLOT_DIAG = 34;
  public static final int SLOT_INFO = 49;

  public RootMenu(BedwarsPlugin plugin){ this.plugin = plugin; }

  public void open(Player p) {
    String title = plugin.messages().get("admin.menu-title");
    Inventory inv = Bukkit.createInventory(new BWMenuHolder(AdminView.ROOT, null), 54, title);
    inv.setItem(SLOT_ARENAS, icon(Material.MAP,       "admin.root.arenas",     "admin.root.arenas-lore"));
    inv.setItem(SLOT_CREATE, icon(Material.LIME_WOOL, "admin.root.create",     "admin.root.create-lore"));
    inv.setItem(SLOT_RULES,  icon(Material.BOOK,      "admin.root.rules",      "admin.root.rules-lore"));
    inv.setItem(SLOT_SHOPS,  icon(Material.EMERALD,   "admin.root.shops",      "admin.root.shops-lore"));
    inv.setItem(SLOT_GENS,   icon(Material.HOPPER,    "admin.root.generators", "admin.root.generators-lore"));
    inv.setItem(SLOT_ROTATION, icon(Material.COMPASS, "admin.root.rotation",   "admin.root.rotation-lore"));
    inv.setItem(SLOT_RESET,  icon(Material.BARRIER,   "admin.root.reset",      "admin.root.reset-lore"));
    inv.setItem(SLOT_DIAG,   icon(Material.COMPARATOR, "admin.root.diagnostics","admin.root.diagnostics-lore"));
    inv.setItem(SLOT_INFO,   icon(Material.PAPER,     "admin.root.info",       "admin.root.info-lore"));
    p.openInventory(inv);
  }

  private ItemStack icon(Material mat, String nameKey, String loreKey) {
    ItemStack it = new ItemStack(mat);
    ItemMeta im = it.getItemMeta();
    if (im != null) {
      String name = plugin.messages().get(nameKey);
      String lore = plugin.messages().get(loreKey);
      im.setDisplayName(name);
      im.setLore(List.of(ChatColor.GRAY + ChatColor.stripColor(lore)));
      it.setItemMeta(im);
    }
    return it;
  }
}
