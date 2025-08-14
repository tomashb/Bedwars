package com.example.bedwars.gui;

import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

/** Utility methods for building GUI inventories. */
public final class GuiFactory {
  private GuiFactory() {}

  /**
   * Fills the outer border of the inventory with the given material.
   * The panes are named with a single space so they appear blank.
   */
  public static void fillBorder(Inventory inv, Material mat) {
    int size = inv.getSize();
    if (size % 9 != 0) return; // only grids
    ItemStack pane = new ItemStack(mat);
    ItemMeta meta = pane.getItemMeta();
    if (meta != null) {
      meta.setDisplayName(" ");
      pane.setItemMeta(meta);
    }
    int rows = size / 9;
    for (int r = 0; r < rows; r++) {
      for (int c = 0; c < 9; c++) {
        if (r == 0 || r == rows - 1 || c == 0 || c == 8) {
          int slot = r * 9 + c;
          inv.setItem(slot, pane);
        }
      }
    }
  }
}
