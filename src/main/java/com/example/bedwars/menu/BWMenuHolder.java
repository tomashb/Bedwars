package com.example.bedwars.menu;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

public final class BWMenuHolder implements InventoryHolder {
    public final AdminView view;
    public final String arenaId;

    public BWMenuHolder(AdminView view, String arenaId) {
        this.view = view;
        this.arenaId = arenaId;
    }

    @Override
    public Inventory getInventory() {
        return null; // non utilisé
    }
}
