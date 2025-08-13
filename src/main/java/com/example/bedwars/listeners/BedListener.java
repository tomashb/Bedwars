package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;

/** Handles bed breaking events. */
public final class BedListener implements Listener {
  private final BedwarsPlugin plugin;
  private final GameService game;
  private final PlayerContextService ctx;

  public BedListener(BedwarsPlugin plugin, GameService game, PlayerContextService ctx) {
    this.plugin = plugin; this.game = game; this.ctx = ctx;
  }

  @EventHandler
  public void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) { e.setCancelled(true); return; }
    Block b = e.getBlock();
    if (!b.getType().name().endsWith("_BED")) return;
    TeamColor teamBed = null;
    for (TeamColor tc : a.enabledTeams()) {
      if (a.team(tc).bedBlock() != null && a.team(tc).bedBlock().getBlock().equals(b)) {
        teamBed = tc; break;
      }
    }
    if (teamBed == null) return;
    TeamColor playerTeam = ctx.getTeam(p);
    boolean breakOnlyEnemy = plugin.getConfig().getBoolean("rules.break-only-enemy-bed", true);
    if (breakOnlyEnemy && teamBed == playerTeam) { e.setCancelled(true); return; }
    game.handleBedBreak(p, a, teamBed);
  }
}
