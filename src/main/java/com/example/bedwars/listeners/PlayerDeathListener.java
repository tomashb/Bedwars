package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.DeathRespawnService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/** Redirects death events to DeathRespawnService and handles void kills. */
public final class PlayerDeathListener implements Listener {
  private final BedwarsPlugin plugin;
  private final DeathRespawnService death;
  private final PlayerContextService ctx;

  public PlayerDeathListener(BedwarsPlugin plugin, DeathRespawnService death, PlayerContextService ctx) {
    this.plugin = plugin;
    this.death = death;
    this.ctx = ctx;
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    e.setDeathMessage(null);
    if (plugin.getConfig().getBoolean("respawn.clear_drops_on_death", true)) {
      e.getDrops().clear();
      e.setKeepInventory(true);
    }
    Player p = e.getEntity();
    if (ctx.getArena(p) == null) return;
    death.handleDeath(p);
    Bukkit.getScheduler().runTask(plugin, () -> p.spigot().respawn());
  }

  @EventHandler
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    if (ctx.getArena(p) == null) return;
    int y = plugin.getConfig().getInt("game.void-kill-y", -5);
    if (e.getTo() != null && e.getTo().getY() <= y) {
      p.setHealth(0.0);
    }
  }
}
