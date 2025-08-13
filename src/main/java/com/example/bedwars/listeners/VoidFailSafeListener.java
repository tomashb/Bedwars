package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.DeathRespawnService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/** Fail-safe to handle players falling into the void without a death event. */
public final class VoidFailSafeListener implements Listener {
  private final BedwarsPlugin plugin;
  private final DeathRespawnService death;
  private final PlayerContextService ctx;

  public VoidFailSafeListener(BedwarsPlugin plugin, DeathRespawnService death, PlayerContextService ctx) {
    this.plugin = plugin;
    this.death = death;
    this.ctx = ctx;
  }

  @EventHandler(ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    if (ctx.getArena(p) == null) return;
    if (ctx.isSpectating(p)) return;
    if (e.getTo() != null && e.getTo().getY() < plugin.getConfig().getInt("game.void-kill-y", -5)) {
      death.handleDeath(p);
    }
  }
}
