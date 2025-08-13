package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/** Handles bed breaking events. */
public final class BedListener implements Listener {
  public BedListener(BedwarsPlugin plugin, GameService game, PlayerContextService ctx) {}

  @EventHandler(ignoreCancelled = true)
  public void onBedEnter(PlayerBedEnterEvent e) {
    e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent e) {
    if (e.getClickedBlock() != null && Tag.BEDS.isTagged(e.getClickedBlock().getType())) {
      e.setCancelled(true);
    }
  }

  // Bed breaking handled in BuildRulesListener; this listener only blocks sleep/interact.
}
