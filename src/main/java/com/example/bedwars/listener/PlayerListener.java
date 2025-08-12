package com.example.bedwars.listener;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.persistence.PersistentDataType;

/**
 * Basic listener that ensures players are removed from their arena
 * when leaving the server.
 */
public class PlayerListener implements Listener {

    private final BedwarsPlugin plugin;

    public PlayerListener(BedwarsPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        plugin.arenas().leaveArena(event.getPlayer());
    }

    @EventHandler
    public void onNpcInteract(PlayerInteractEntityEvent event) {
        var pdc = event.getRightClicked().getPersistentDataContainer();
        String type = pdc.get(plugin.npcKey(), PersistentDataType.STRING);
        if (type != null) {
            event.setCancelled(true);
            if (type.equalsIgnoreCase("item")) {
                event.getPlayer().sendMessage(plugin.messages().get("npc.item-shop"));
            } else if (type.equalsIgnoreCase("upgrade")) {
                event.getPlayer().sendMessage(plugin.messages().get("npc.upgrade-shop"));
            }
        }
    }
}
