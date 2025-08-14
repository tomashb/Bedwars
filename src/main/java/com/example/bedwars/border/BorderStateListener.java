package com.example.bedwars.border;

import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.ArenaStateChangeEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/** Applies world borders on arena state transitions. */
public final class BorderStateListener implements Listener {
  private final BorderService border;

  public BorderStateListener(BorderService border) {
    this.border = border;
  }

  @EventHandler
  public void onStateChange(ArenaStateChangeEvent event) {
    GameState ns = event.newState();
    if (ns == GameState.WAITING || ns == GameState.RUNNING) {
      border.apply(event.arena());
    }
  }
}
