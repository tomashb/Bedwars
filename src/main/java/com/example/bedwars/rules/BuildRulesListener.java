package com.example.bedwars.rules;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.services.BuildRulesService;
import java.util.Set;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * Handles build protections: only allow breaking player placed blocks and
 * whitelisted placements depending on the arena state.
 */
public final class BuildRulesListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final BuildRulesService buildRules;
  private final Set<GameState> allowedStates;

  public BuildRulesListener(BedwarsPlugin plugin, PlayerContextService ctx, BuildRulesService br) {
    this.plugin = plugin; this.ctx = ctx; this.buildRules = br;
    this.allowedStates = plugin.getConfig().getStringList("rules.allow-build-states")
        .stream().map(s -> {
          try { return GameState.valueOf(s); } catch (IllegalArgumentException ex) { return null; }
        }).filter(java.util.Objects::nonNull).collect(java.util.stream.Collectors.toSet());
    if (this.allowedStates.isEmpty()) this.allowedStates.add(GameState.RUNNING);
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) { e.setCancelled(true); return; }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || !allowedStates.contains(a.state())) {
      e.setCancelled(true);
      p.sendMessage(plugin.messages().get("prefix") + plugin.messages().get("build.not_allowed"));
      return;
    }
    if (!buildRules.isAllowed(e.getBlockPlaced().getType())) { e.setCancelled(true); return; }
    buildRules.recordPlacement(arenaId, e.getBlockPlaced().getLocation());
  }

  @EventHandler(ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) { e.setCancelled(true); return; }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || !allowedStates.contains(a.state())) { e.setCancelled(true); return; }
    Block b = e.getBlock();
    if (Tag.BEDS.isTagged(b.getType())) { return; }
    if (plugin.getConfig().getBoolean("rules.break-only-placed", true) && !buildRules.wasPlaced(arenaId, b.getLocation())) {
      e.setCancelled(true);
    } else {
      buildRules.removePlaced(arenaId, b.getLocation());
    }
  }
}
