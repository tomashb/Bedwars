package com.example.bedwars.shop;

import org.bukkit.Material;

/**
 * Supported shop currencies and their corresponding material items.
 */
public enum Currency {
  IRON(Material.IRON_INGOT),
  GOLD(Material.GOLD_INGOT),
  EMERALD(Material.EMERALD),
  DIAMOND(Material.DIAMOND);

  private final Material material;

  Currency(Material material) {
    this.material = material;
  }

  public Material material() {
    return material;
  }
}
