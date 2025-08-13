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
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.metadata.FixedMetadataValue;

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

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onPlace(BlockPlaceEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || !allowedStates.contains(a.state())) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.not_running");
      return;
    }
    if (plugin.getConfig().getBoolean("build.require_permission", false)
        && !p.hasPermission("bedwars.build.place")) { e.setCancelled(true); return; }
    if (!buildRules.isAllowed(e.getBlockPlaced().getType())) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.not_allowed_block");
      return;
    }
    if (e.isCancelled() && plugin.getConfig().getBoolean("build.bypass_external_protection", false)) {
      e.setCancelled(false);
    }
    e.getBlockPlaced().setMetadata("bw_placed", new FixedMetadataValue(plugin, true));
    buildRules.recordPlacement(arenaId, e.getBlockPlaced().getLocation());
  }

  @EventHandler(priority = EventPriority.HIGHEST)
  public void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || !allowedStates.contains(a.state())) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.not_running");
      return;
    }
    Block b = e.getBlock();
    if (Tag.BEDS.isTagged(b.getType())) { return; }
    if (plugin.getConfig().getBoolean("rules.break-only-placed", true) && !b.hasMetadata("bw_placed")) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.map_protected");
    } else {
      if (e.isCancelled() && plugin.getConfig().getBoolean("build.bypass_external_protection", false)) {
        e.setCancelled(false);
      }
      b.removeMetadata("bw_placed", plugin);
      buildRules.removePlaced(arenaId, b.getLocation());
    }
  }
}
