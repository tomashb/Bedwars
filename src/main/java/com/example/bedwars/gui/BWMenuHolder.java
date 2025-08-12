package com.example.bedwars.gui;

import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

/**
 * Holder storing the current admin view and optional arena context.
 */
public class BWMenuHolder implements InventoryHolder {

    private final AdminView view;
    private final String arenaId;

    public BWMenuHolder(AdminView view, String arenaId) {
        this.view = view;
        this.arenaId = arenaId;
    }

    public AdminView getView() {
        return view;
    }

    public String getArenaId() {
        return arenaId;
    }

    @Override
    public Inventory getInventory() {
        return null; // Not used
    }
}
