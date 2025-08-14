package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerRespawnEvent;

/** Ensures players respawn where the death service teleports them. */
public final class PlayerRespawnListener implements Listener {
  private final PlayerContextService ctx;
  private final BedwarsPlugin plugin;

  public PlayerRespawnListener(PlayerContextService ctx, BedwarsPlugin plugin) {
    this.ctx = ctx;
    this.plugin = plugin;
  }

  @EventHandler
  public void onRespawn(PlayerRespawnEvent e) {
    Player p = e.getPlayer();
    if (ctx.getArena(p) != null) {
      e.setRespawnLocation(p.getLocation());
      plugin.scoreboard().attach(p);
    }
  }
}
