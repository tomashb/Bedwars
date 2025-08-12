package com.example.bedwars.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class BWMenuHolder implements InventoryHolder {
  public final AdminView view;
  public final String arenaId; // null sur ROOT ou vues globales

  public BWMenuHolder(AdminView view, String arenaId) {
    this.view = view;
    this.arenaId = arenaId;
  }

  @Override
  public Inventory getInventory() {
    return null; // non utilisé
  }
}
