package com.example.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Definition of an item sold in the item shop.
 */
public final class ShopItem {
  public final String id;
  public final Material mat;
  public final int amount;
  public final Currency currency;
  public final int cost;
  public final Map<Enchantment, Integer> enchants;
  public final String name;
  public final boolean teamColored;
  public final boolean permanent;
  public final String bwItem;

  private ShopItem(
      String id,
      Material mat,
      int amount,
      Currency currency,
      int cost,
      Map<Enchantment, Integer> enchants,
      String name,
      boolean teamColored,
      boolean permanent,
      String bwItem) {

    this.id = Objects.requireNonNull(id, "id");
    this.mat = Objects.requireNonNull(mat, "mat");
    this.amount = Math.max(1, amount);
    this.currency = Objects.requireNonNull(currency, "currency");
    this.cost = Math.max(0, cost);
    this.enchants = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(enchants, "enchants")));
    this.name = Objects.requireNonNullElse(name, "");
    this.teamColored = teamColored;
    this.permanent = permanent;
    this.bwItem = Objects.requireNonNullElse(bwItem, "");
  }

  // ---------- Builder ----------
  public static Builder builder() { return new Builder(); }

  public static final class Builder {
    private String id;                                    // required
    private Material mat = Material.STONE;                // default
    private int amount = 1;                               // default
    private Currency currency = Currency.IRON;            // default
    private int cost = 0;                                 // default
    private final Map<Enchantment, Integer> enchants = new LinkedHashMap<>();
    private String name = "";
    private boolean teamColored = false;
    private boolean permanent = false;
    private String bwItem = "";

    public Builder id(String id) { this.id = id; return this; }
    public Builder mat(Material m) { this.mat = m; return this; }
    public Builder amount(int a) { this.amount = a; return this; }
    public Builder currency(Currency c) { this.currency = c; return this; }
    public Builder cost(int c) { this.cost = c; return this; }
    public Builder enchant(Enchantment e, int lvl) { this.enchants.put(e, lvl); return this; }
    public Builder name(String n) { this.name = n; return this; }
    public Builder teamColored(boolean yes) { this.teamColored = yes; return this; }
    public Builder permanent(boolean yes) { this.permanent = yes; return this; }
    public Builder bwItem(String tag) { this.bwItem = tag; return this; }

    public ShopItem build() {
      if (id == null || id.isBlank()) throw new IllegalStateException("ShopItem id is required");
      return new ShopItem(id, mat, amount, currency, cost, enchants, name, teamColored, permanent, bwItem);
    }
  }
}

