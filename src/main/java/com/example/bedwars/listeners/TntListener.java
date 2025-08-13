package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.game.PlayerContextService;
import com.example.bedwars.services.BuildRulesService;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

/** Auto-primes TNT placed by players. */
public final class TntListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final BuildRulesService buildRules;

  public TntListener(BedwarsPlugin plugin, PlayerContextService ctx, BuildRulesService br) {
    this.plugin = plugin; this.ctx = ctx; this.buildRules = br;
  }

  @EventHandler(ignoreCancelled = true)
  public void onPlaceTnt(BlockPlaceEvent e) {
    if (e.getBlockPlaced().getType() != Material.TNT) return;
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) { e.setCancelled(true); return; }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) { e.setCancelled(true); return; }
    Location loc = e.getBlockPlaced().getLocation().add(0.5, 0, 0.5);
    e.setCancelled(true);
    e.getBlockPlaced().setType(Material.AIR);
    buildRules.removePlaced(arenaId, e.getBlockPlaced().getLocation());
    TNTPrimed t = (TNTPrimed) loc.getWorld().spawnEntity(loc, EntityType.TNT);
    t.setFuseTicks(plugin.getConfig().getInt("tnt.fuse_ticks", 40));
    if (plugin.getConfig().getBoolean("tnt.attribute_owner", true)) t.setSource(p);
  }
}
