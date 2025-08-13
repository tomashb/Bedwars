package com.example.bedwars.rules;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.services.BuildRulesService;
import java.util.Set;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.Material;
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
      plugin.messages().send(p, "errors.map_protected");
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
    BlockData data = b.getBlockData();
    if (data instanceof Bed bed) {
      Block foot = (bed.getPart() == Bed.Part.FOOT) ? b : b.getRelative(bed.getFacing().getOppositeFace());
      TeamColor bedTeam = null;
      for (TeamColor tc : a.enabledTeams()) {
        var loc = a.team(tc).bedBlock();
        if (loc != null && loc.getBlock().equals(foot)) { bedTeam = tc; break; }
      }
      if (bedTeam == null) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.map_protected");
        return;
      }
      TeamColor playerTeam = ctx.getTeam(p);
      if (bedTeam == playerTeam) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.own_bed");
        return;
      }
      if (a.team(bedTeam).bedBlock() == null) {
        e.setCancelled(true);
        plugin.messages().send(p, "errors.bed_already_broken");
        return;
      }
      e.setCancelled(true);
      e.setDropItems(false);
      Block head = foot.getRelative(((Bed) foot.getBlockData()).getFacing());
      foot.setType(Material.AIR, false);
      head.setType(Material.AIR, false);
      plugin.game().handleBedBreak(p, a, bedTeam);
      return;
    }

    if (plugin.getConfig().getBoolean("rules.break-only-placed", true) && !buildRules.wasPlaced(arenaId, b.getLocation())) {
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
