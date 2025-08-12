package com.example.bedwars.menu;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;

public class MenuListener implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent e) {
        Inventory top = e.getView().getTopInventory();
        if (!(top.getHolder() instanceof BWMenuHolder holder)) {
            return;
        }
        e.setCancelled(true);
        if (e.getClickedInventory() != top) {
            return;
        }
        if (e.getCurrentItem() == null || e.getCurrentItem().getType().isAir()) {
            return;
        }
        int slot = e.getRawSlot();
        Player player = (Player) e.getWhoClicked();
        switch (holder.view) {
            case ROOT -> {
                switch (slot) {
                    case 10 -> player.sendMessage("Open arenas list");
                    case 12 -> {
                        player.closeInventory();
                        player.sendMessage("Enter arena id in chat.");
                    }
                    case 14 -> player.sendMessage("Open rules & events");
                    case 16 -> player.sendMessage("Open NPC & shops");
                    case 28 -> player.sendMessage("Open rotation");
                    case 30 -> player.sendMessage("Open reset");
                    case 32 -> player.sendMessage("Open diagnostics");
                    default -> {}
                }
            }
            default -> {}
        }
    }
}
