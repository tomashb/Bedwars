package com.example.bedwars.shop;

import java.util.Map;
import java.util.StringJoiner;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

/**
 * Utility methods for costs in shops.
 */
public final class PriceUtil {
  private PriceUtil() {}

  public static boolean hasAndRemove(Player p, Map<Material,Integer> cost) {
    Inventory inv = p.getInventory();
    for (Map.Entry<Material,Integer> e : cost.entrySet()) {
      int have = count(inv, e.getKey());
      if (have < e.getValue()) return false;
    }
    // remove
    for (Map.Entry<Material,Integer> e : cost.entrySet()) {
      remove(inv, e.getKey(), e.getValue());
    }
    return true;
  }

  private static int count(Inventory inv, Material m) {
    int total = 0;
    for (ItemStack is : inv.getContents()) {
      if (is != null && is.getType() == m) total += is.getAmount();
    }
    return total;
  }

  private static void remove(Inventory inv, Material m, int amount) {
    for (int i = 0; i < inv.getSize(); i++) {
      ItemStack is = inv.getItem(i);
      if (is == null || is.getType() != m) continue;
      int take = Math.min(amount, is.getAmount());
      is.setAmount(is.getAmount() - take);
      amount -= take;
      if (amount <= 0) break;
    }
  }

  public static String formatCost(Map<Material,Integer> cost) {
    StringJoiner join = new StringJoiner(", ");
    for (Map.Entry<Material,Integer> e : cost.entrySet()) {
      String name = e.getKey().name().toLowerCase().replace('_', ' ');
      join.add(e.getValue() + " " + name);
    }
    return join.toString();
  }
}
