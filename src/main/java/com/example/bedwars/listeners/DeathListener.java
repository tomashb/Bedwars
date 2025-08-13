package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.DeathRespawnService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;

/** Redirects all death events to the DeathRespawnService. */
public final class DeathListener implements Listener {
  private final BedwarsPlugin plugin;
  private final DeathRespawnService death;
  private final PlayerContextService ctx;

  public DeathListener(BedwarsPlugin plugin, DeathRespawnService death, PlayerContextService ctx) {
    this.plugin = plugin;
    this.death = death;
    this.ctx = ctx;
  }

  @EventHandler
  public void onDeath(PlayerDeathEvent e) {
    e.setDeathMessage(null);
    e.getDrops().clear();
    e.setKeepInventory(true);
    Player p = e.getEntity();
    if (ctx.getArena(p) == null) return;
    death.handleDeath(p);
    Bukkit.getScheduler().runTask(plugin, () -> p.spigot().respawn());
  }
}
