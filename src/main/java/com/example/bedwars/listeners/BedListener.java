package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
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

  @EventHandler(ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    Block b = e.getBlock();
    if (!Tag.BEDS.isTagged(b.getType())) return;
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) { e.setCancelled(true); return; }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) { e.setCancelled(true); return; }

    Bed bed = (Bed) b.getBlockData();
    Block head = bed.getPart() == Bed.Part.HEAD ? b : b.getRelative(bed.getFacing());
    TeamColor bedTeam = null;
    for (TeamColor tc : a.enabledTeams()) {
      var loc = a.team(tc).bedBlock();
      if (loc != null && loc.getBlock().equals(head)) { bedTeam = tc; break; }
    }
    if (bedTeam == null) { e.setCancelled(true); return; }

    TeamColor playerTeam = ctx.getTeam(p);
    if (bedTeam == playerTeam) {
      p.sendMessage("§cC’est votre lit !");
      e.setCancelled(true);
      return;
    }

    game.handleBedBreak(p, a, bedTeam);
    e.setCancelled(false);
  }
}
