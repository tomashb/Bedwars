package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/** Basic block protections depending on arena state. */
public final class BlockRulesListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;

  public BlockRulesListener(BedwarsPlugin plugin, PlayerContextService ctx) {
    this.plugin = plugin; this.ctx = ctx;
  }

  private boolean shouldProtect(String arenaId) {
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return false;
    return plugin.getConfig().getBoolean("rules.protect-lobby", true) && a.state() != GameState.RUNNING;
  }

  @EventHandler
  public void onBreak(BlockBreakEvent e) {
    String arenaId = ctx.getArena(e.getPlayer());
    if (arenaId != null && shouldProtect(arenaId)) e.setCancelled(true);
  }

  @EventHandler
  public void onPlace(BlockPlaceEvent e) {
    String arenaId = ctx.getArena(e.getPlayer());
    if (arenaId != null && shouldProtect(arenaId)) e.setCancelled(true);
  }
}
