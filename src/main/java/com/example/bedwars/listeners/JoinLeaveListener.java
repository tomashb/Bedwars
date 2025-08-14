package com.example.bedwars.listeners;

import com.example.bedwars.lobby.LobbyService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

/** Handles players leaving the server during games. */
public final class JoinLeaveListener implements Listener {
  private final LobbyService lobby;

  public JoinLeaveListener(LobbyService lobby) {
    this.lobby = lobby;
  }

  @EventHandler
  public void onQuit(PlayerQuitEvent e) {
    lobby.sendToLobby(e.getPlayer(), LobbyService.Reason.DISCONNECT);
  }
}
