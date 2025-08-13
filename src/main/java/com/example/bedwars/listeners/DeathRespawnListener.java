package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/** Handles deaths and void kills. */
public final class DeathRespawnListener implements Listener {
  private final BedwarsPlugin plugin;
  private final GameService game;
  private final PlayerContextService ctx;

  public DeathRespawnListener(BedwarsPlugin plugin, GameService game, PlayerContextService ctx) {
    this.plugin = plugin; this.game = game; this.ctx = ctx;
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    e.setDeathMessage(null);
    if (plugin.getConfig().getBoolean("game.keep-inventory", false)) {
      e.setKeepInventory(true);
      e.getDrops().clear();
    }
    Player p = e.getEntity();
    if (ctx.getArena(p) == null) return;
    game.handleDeath(p);
  }

  @EventHandler
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    String arena = ctx.getArena(p);
    if (arena == null) return;
    int y = plugin.getConfig().getInt("game.void-kill-y", -5);
    if (e.getTo() != null && e.getTo().getY() <= y) {
      p.setHealth(0.0);
    }
  }
}
