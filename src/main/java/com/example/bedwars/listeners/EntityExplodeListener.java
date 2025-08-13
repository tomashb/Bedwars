package com.example.bedwars.listeners;

import com.example.bedwars.services.BuildRulesService;
import org.bukkit.Tag;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityExplodeEvent;

/** Filters explosion damage to only affect player-placed blocks. */
public final class EntityExplodeListener implements Listener {
  private final BuildRulesService buildRules;

  public EntityExplodeListener(BuildRulesService br) {
    this.buildRules = br;
  }

  @EventHandler(ignoreCancelled = true)
  public void onExplode(EntityExplodeEvent e) {
    e.blockList().removeIf(b -> {
      if (Tag.BEDS.isTagged(b.getType())) return true;
      String arena = buildRules.arenaAt(b.getLocation());
      if (arena == null) return true;
      buildRules.removePlaced(arena, b.getLocation());
      return false;
    });
  }
}
