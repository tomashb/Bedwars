package com.example.bedwars.border;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/** Listener preventing building or breaking outside arena borders. */
public final class BorderBuildListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final BorderService border;

  public BorderBuildListener(BedwarsPlugin plugin, PlayerContextService ctx, BorderService border) {
    this.plugin = plugin; this.ctx = ctx; this.border = border;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onPlace(BlockPlaceEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    BorderService.Settings b = border.cfg(a);
    if (!b.enabled) return;
    Location l = e.getBlockPlaced().getLocation();
    Vector2D c = border.centerFor(a);
    if ((b.clampY && (l.getY() < b.minY || l.getY() > b.maxY))
        || BorderMath.outside2D(l, c.x(), c.z(), b.radius)) {
      e.setCancelled(true);
      plugin.messages().send(p, "border.build_block_outside_message");
    }
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onBreak(BlockBreakEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    BorderService.Settings b = border.cfg(a);
    if (!b.enabled) return;
    Location l = e.getBlock().getLocation();
    Vector2D c = border.centerFor(a);
    if (BorderMath.outside2D(l, c.x(), c.z(), b.radius)) {
      e.setCancelled(true);
    }
  }
}
