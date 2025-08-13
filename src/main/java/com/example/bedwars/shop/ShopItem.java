package com.example.bedwars.shop;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

/**
 * Definition of a purchasable item in the shop.
 */
public final class ShopItem {
  public final String id;
  public final Material mat;
  public final int amount;
  public final Map<Material,Integer> price;
  public final Map<Enchantment,Integer> enchants;
  public final String name;
  public final boolean teamColored;

  public ShopItem(String id, Material mat, int amount,
                  Map<Material,Integer> price,
                  Map<Enchantment,Integer> enchants,
                  String name, boolean teamColored) {
    this.id = id;
    this.mat = mat;
    this.amount = amount;
    this.price = Collections.unmodifiableMap(new HashMap<>(price));
    this.enchants = Collections.unmodifiableMap(new HashMap<>(enchants));
    this.name = name;
    this.teamColored = teamColored;
  }

  public static Builder builder(String id) { return new Builder(id); }

  public static final class Builder {
    private final String id;
    private Material mat = Material.STONE;
    private int amount = 1;
    private final Map<Material,Integer> price = new HashMap<>();
    private final Map<Enchantment,Integer> enchants = new HashMap<>();
    private String name = id;
    private boolean teamColored = false;

    public Builder(String id) { this.id = id; }

    public Builder mat(Material m) { this.mat = m; return this; }
    public Builder amount(int a) { this.amount = a; return this; }
    public Builder price(Material m, int qty) { this.price.put(m, qty); return this; }
    public Builder enchant(Enchantment e, int lvl) { this.enchants.put(e, lvl); return this; }
    public Builder name(String n) { this.name = n; return this; }
    public Builder teamColored(boolean b) { this.teamColored = b; return this; }

    public ShopItem build() {
      return new ShopItem(id, mat, amount, price, enchants, name, teamColored);
    }
  }
}
