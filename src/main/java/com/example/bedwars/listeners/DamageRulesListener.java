package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.arena.TeamColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/** Handles PVP rules. */
public final class DamageRulesListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public DamageRulesListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  @EventHandler
  public void onDamage(EntityDamageByEntityEvent e) {
    if (!(e.getEntity() instanceof Player victim)) return;
    if (!(e.getDamager() instanceof Player damager)) return;
    String arenaId = ctx.getArena(victim);
    if (arenaId == null || !arenaId.equals(ctx.getArena(damager))) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) { e.setCancelled(true); return; }
    boolean ff = plugin.getConfig().getBoolean("rules.friendly-fire", false);
    if (!ff) {
      TeamColor tv = ctx.getTeam(victim);
      TeamColor td = ctx.getTeam(damager);
      if (tv != null && tv == td) e.setCancelled(true);
    }
  }
}
