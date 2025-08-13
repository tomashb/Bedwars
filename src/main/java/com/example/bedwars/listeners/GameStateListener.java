package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.ArenaStateChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener relaying arena state transitions to the generator manager.
 */
public final class GameStateListener implements Listener {
  private final BedwarsPlugin plugin;

  public GameStateListener(BedwarsPlugin plugin) {
    this.plugin = plugin;
  }

  @EventHandler
  public void onStateChange(ArenaStateChangeEvent event) {
    plugin.generators().onArenaStateChange(event.arena(), event.oldState(), event.newState());
  }
}
