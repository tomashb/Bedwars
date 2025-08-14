package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;

/** Fail-safe to handle players falling into the void without a death event. */
public final class VoidFailSafeListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public VoidFailSafeListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin;
    this.ctx = ctx;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) return;
    if (ctx.isSpectating(p)) return;
    if (e.getTo() != null && e.getTo().getY() < plugin.bounds().voidKillY()) {
      plugin.game().failSafeVoid(p);
    }
  }
}
