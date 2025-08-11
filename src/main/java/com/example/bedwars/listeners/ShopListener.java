package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class ShopListener implements Listener {
    private final BedwarsPlugin plugin;
    public ShopListener(BedwarsPlugin plugin){ this.plugin=plugin; }
    @EventHandler public void onClick(InventoryClickEvent e){ plugin.shops().handleClick(e); }
}
