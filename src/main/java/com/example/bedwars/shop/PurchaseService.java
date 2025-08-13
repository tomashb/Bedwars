package com.example.bedwars.shop;

import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Utility for counting and removing currencies from player inventories.
 */
public final class PurchaseService {
  private PurchaseService() {}

  public static int count(Player p, Currency c) {
    return count(p.getInventory(), c);
  }

  private static int count(Inventory inv, Currency c) {
    int total = 0;
    for (ItemStack is : inv.getContents()) {
      if (is != null && is.getType() == c.material()) total += is.getAmount();
    }
    return total;
  }

  public static boolean tryBuy(Player p, Currency c, int amount) {
    Inventory inv = p.getInventory();
    if (count(inv, c) < amount) return false;
    remove(inv, c, amount);
    return true;
  }

  private static void remove(Inventory inv, Currency c, int amount) {
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack is = inv.getItem(i);
      if (is == null || is.getType() != c.material()) continue;
      int take = Math.min(amount, is.getAmount());
      is.setAmount(is.getAmount() - take);
      amount -= take;
      if (amount <= 0) break;
    }
  }
}
