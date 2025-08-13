package com.example.bedwars.listeners;

import com.example.bedwars.game.GameService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Handles players leaving the server during games. */
public final class JoinLeaveListener implements Listener {
  private final GameService game;

  public JoinLeaveListener(GameService game) {
    this.game = game;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    game.leave(e.getPlayer(), false);
  }
}
