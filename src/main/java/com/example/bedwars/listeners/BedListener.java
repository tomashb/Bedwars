package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.GameService;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Bed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerInteractEvent;

/** Handles bed breaking events. */
public final class BedListener implements Listener {
  private final BedwarsPlugin plugin;
  private final GameService game;
  private final PlayerContextService ctx;

  public BedListener(BedwarsPlugin plugin, GameService game, PlayerContextService ctx) {
    this.plugin = plugin; this.game = game; this.ctx = ctx;
  }

  @EventHandler(ignoreCancelled = true)
  public void onBedEnter(PlayerBedEnterEvent e) {
    e.setCancelled(true);
  }

  @EventHandler(ignoreCancelled = true)
  public void onInteract(PlayerInteractEvent e) {
    if (e.getClickedBlock() != null && Tag.BEDS.isTagged(e.getClickedBlock().getType())) {
      e.setCancelled(true);
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBreak(BlockBreakEvent e) {
    Block b = e.getBlock();
    if (!(b.getBlockData() instanceof Bed bed)) return;
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.not_running");
      return;
    }

    Block foot = bed.getPart() == Bed.Part.FOOT ? b : b.getRelative(bed.getFacing().getOppositeFace());
    TeamColor bedTeam = null;
    for (TeamColor tc : a.enabledTeams()) {
      var loc = a.team(tc).bedBlock();
      if (loc != null && loc.getBlock().equals(foot)) { bedTeam = tc; break; }
    }
    if (bedTeam == null) {
      e.setCancelled(true);
      return;
    }

    TeamColor playerTeam = ctx.getTeam(p);
    if (bedTeam == playerTeam) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.own_bed");
      return;
    }

    e.setCancelled(true);
    e.setDropItems(false);
    Block head = foot.getRelative(((Bed) foot.getBlockData()).getFacing());
    foot.setType(Material.AIR, false);
    head.setType(Material.AIR, false);
    game.handleBedBreak(p, a, bedTeam);
  }
}
