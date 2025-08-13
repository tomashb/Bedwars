package com.example.bedwars.shop;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;

import java.util.*;

public final class ShopItem {
  public final String id;
  public final Material mat;
  public final int amount;
  public final Map<Material, Integer> price;
  public final Map<Enchantment, Integer> enchants;
  public final String name;
  public final boolean teamColored;

  private ShopItem(
      String id,
      Material mat,
      int amount,
      Map<Material, Integer> price,
      Map<Enchantment, Integer> enchants,
      String name,
      boolean teamColored) {

    this.id = Objects.requireNonNull(id, "id");
    this.mat = Objects.requireNonNull(mat, "mat");
    this.amount = Math.max(1, amount);
    this.price = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(price, "price")));
    this.enchants = Collections.unmodifiableMap(new LinkedHashMap<>(Objects.requireNonNull(enchants, "enchants")));
    this.name = Objects.requireNonNullElse(name, "");
    this.teamColored = teamColored;
  }

  // ---------- Builder ----------
  public static Builder builder() { return new Builder(); }

  public static final class Builder {
    private String id;                                    // OBLIGATOIRE
    private Material mat = Material.STONE;                // défaut
    private int amount = 1;                               // défaut
    private final Map<Material, Integer> price = new LinkedHashMap<>();
    private final Map<Enchantment, Integer> enchants = new LinkedHashMap<>();
    private String name = "";
    private boolean teamColored = false;

    public Builder id(String id) { this.id = id; return this; }
    public Builder mat(Material m) { this.mat = m; return this; }
    public Builder amount(int a) { this.amount = a; return this; }
    public Builder price(Material currency, int qty) { this.price.put(currency, qty); return this; }
    public Builder enchant(Enchantment e, int lvl) { this.enchants.put(e, lvl); return this; }
    public Builder name(String n) { this.name = n; return this; }
    public Builder teamColored(boolean yes) { this.teamColored = yes; return this; }

    public ShopItem build() {
      if (id == null || id.isBlank()) throw new IllegalStateException("ShopItem id is required");
      return new ShopItem(id, mat, amount, price, enchants, name, teamColored);
    }
  }
}

