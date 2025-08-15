package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.services.BuildRulesService;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;

/** Filters explosion damage to only affect player-placed blocks. */
public final class EntityExplodeListener implements Listener {
  private final BedwarsPlugin plugin;
  private final BuildRulesService buildRules;

  public EntityExplodeListener(BedwarsPlugin plugin, BuildRulesService br) {
    this.plugin = plugin;
    this.buildRules = br;
  }

  @EventHandler(ignoreCancelled = true)
  public void onExplode(EntityExplodeEvent e) {
    e.blockList().removeIf(b -> filter(b.getLocation(), b.getType()));
  }

  @EventHandler(ignoreCancelled = true)
  public void onBlockExplode(BlockExplodeEvent e) {
    e.blockList().removeIf(b -> filter(b.getLocation(), b.getType()));
  }

  private boolean filter(org.bukkit.Location loc, org.bukkit.Material type) {
    if (Tag.BEDS.isTagged(type)) return true;
    String arenaId = buildRules.arenaAt(loc);
    if (arenaId == null) return true;
    var arenaOpt = plugin.arenas().get(arenaId);
    if (arenaOpt.isEmpty()) return true;
    buildRules.removePlaced(arenaOpt.get(), loc);
    return false;
  }
}
