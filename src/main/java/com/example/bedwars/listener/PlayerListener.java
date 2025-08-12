package com.example.bedwars.listener;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

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
        plugin.getArenaManager().leaveArena(event.getPlayer());
    }
}
