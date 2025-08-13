package com.example.bedwars.listeners;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.services.BuildRulesService;
import org.bukkit.Tag;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.SmallFireball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
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
    boolean isFireball = e.getEntity() instanceof Fireball || e.getEntity() instanceof SmallFireball;
    boolean restrict = isFireball
        ? plugin.getConfig().getBoolean("fireball.break_only_player_blocks", true)
        : plugin.getConfig().getBoolean("rules.break-only-placed", true);
    if (!restrict) return;
    e.blockList().removeIf(b -> {
      if (Tag.BEDS.isTagged(b.getType())) return true;
      if (!b.hasMetadata("bw_placed")) return true;
      String arena = buildRules.arenaAt(b.getLocation());
      if (arena == null) return true;
      buildRules.removePlaced(arena, b.getLocation());
      return false;
    });
  }
}
