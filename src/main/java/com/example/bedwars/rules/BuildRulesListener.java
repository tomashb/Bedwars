package com.example.bedwars.rules;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.gen.Generator;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.services.BuildRulesService;
import com.example.bedwars.shop.NpcData;
import java.util.Set;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import com.example.bedwars.game.GameService;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
// removed metadata usage

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

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
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

    Material type = e.getBlockPlaced().getType();
    if (type.name().endsWith("_BED")) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.map_protected");
      return;
    }

    Location loc = e.getBlockPlaced().getLocation().add(0.5, 0.0, 0.5);
    if (nearNPC(a, loc)) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.no_build_npc");
      return;
    }
    if (nearGenerator(a, loc)) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.no_build_generator");
      return;
    }

    if (!buildRules.isAllowed(type)) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.map_protected");
      return;
    }
    if (plugin.getConfig().getBoolean("build.track_placed_blocks", true)) {
      buildRules.recordPlaced(a, e.getBlockPlaced());
    }
  }

  @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null || ctx.isSpectating(p)) {
      e.setCancelled(true);
      return;
    }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) {
      e.setCancelled(true);
      plugin.messages().send(p, "rules.map_protected");
      return;
    }

    Block b = e.getBlock();
    Material type = b.getType();
    if (type.name().endsWith("_BED")) {
      TeamColor owner = a.beds().ownerOf(b.getLocation());
      if (owner == null) {
        e.setCancelled(true);
        plugin.messages().send(p, "rules.map_protected");
        return;
      }
      TeamColor mine = ctx.getTeam(p);
      if (owner == mine) {
        e.setCancelled(true);
        plugin.messages().send(p, "rules.bed_ally_protected");
        return;
      }
      if (a.beds().isBroken(owner)) {
        e.setCancelled(true);
        return;
      }
      e.setCancelled(true);
      plugin.game().destroyBed(a, owner, GameService.BedBreakCause.PLAYER, p);
      return;
    }

    if (buildRules.wasPlaced(a, b.getLocation())) {
      buildRules.removePlaced(a, b.getLocation());
      return;
    }

    e.setCancelled(true);
    plugin.messages().send(p, "rules.map_protected");
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBucket(PlayerBucketEmptyEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || !allowedStates.contains(a.state())) {
      e.setCancelled(true);
      plugin.messages().send(p, "errors.not_running");
      return;
    }
    if (!plugin.getConfig().getBoolean("build.track_placed_blocks", true)) return;
    buildRules.recordPlaced(a, e.getBlock());
  }

  private boolean nearNPC(Arena a, Location loc) {
    double r = plugin.getConfig().getDouble("build.no_build.npc_radius", 2.0);
    double r2 = r * r;
    for (NpcData n : a.npcs()) {
      Location nl = n.location().clone().add(0.5, 0.0, 0.5);
      if (nl.getWorld() != loc.getWorld()) continue;
      if (nl.distanceSquared(loc) <= r2) return true;
    }
    return false;
  }

  private boolean nearGenerator(Arena a, Location loc) {
    double rb = plugin.getConfig().getDouble("build.no_build.gen.base_radius", 1.5);
    double rd = plugin.getConfig().getDouble("build.no_build.gen.diamond_radius", 1.5);
    double re = plugin.getConfig().getDouble("build.no_build.gen.emerald_radius", 1.5);
    for (Generator g : a.generators()) {
      Location gl = g.location().clone().add(0.5, 0.0, 0.5);
      if (gl.getWorld() != loc.getWorld()) continue;
      double r2;
      GeneratorType type = g.type();
      switch (type) {
        case DIAMOND -> r2 = rd * rd;
        case EMERALD -> r2 = re * re;
        default -> r2 = rb * rb;
      }
      if (gl.distanceSquared(loc) <= r2) return true;
    }
    return false;
  }
}
