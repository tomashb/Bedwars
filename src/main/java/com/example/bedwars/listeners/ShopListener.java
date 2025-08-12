package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.util.Keys;
import org.bukkit.entity.Villager;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

public class ShopListener implements Listener {
    private final BedwarsPlugin plugin;
    public ShopListener(BedwarsPlugin plugin){ this.plugin=plugin; }
    @EventHandler public void onClick(InventoryClickEvent e){ plugin.shops().handleClick(e); }
    @EventHandler public void onNpcInteract(PlayerInteractEntityEvent e){
        if (!(e.getRightClicked() instanceof Villager v)) return;
        String tag = v.getPersistentDataContainer().get(Keys.NPC, PersistentDataType.STRING);
        if (tag == null) return;
        e.setCancelled(true);
        plugin.shops().open(e.getPlayer());
    }
}
