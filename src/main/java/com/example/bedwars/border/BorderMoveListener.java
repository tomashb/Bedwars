package com.example.bedwars.border;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.game.PlayerContextService;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.util.Vector;

/** Listener handling player movement relative to arena borders. */
public final class BorderMoveListener implements Listener {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final BorderService border;

  public BorderMoveListener(BedwarsPlugin plugin, PlayerContextService ctx, BorderService border) {
    this.plugin = plugin; this.ctx = ctx; this.border = border;
  }

  @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
  public void onMove(PlayerMoveEvent e) {
    Player p = e.getPlayer();
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    BorderService.Settings b = border.cfg(a);
    if (!b.enabled) return;
    if (ctx.isSpectating(p)) return;
    Location to = e.getTo();
    if (to == null) return;

    int voidY = plugin.bounds().voidKillY();
    if (a.state() == com.example.bedwars.arena.GameState.RUNNING && to.getY() < voidY) {
      return; // fail-safe listener will handle void
    }

    if (a.state() != com.example.bedwars.arena.GameState.RUNNING && b.clampY && (to.getY() < b.minY || to.getY() > b.maxY)) {
      double clampedY = Math.max(b.minY, Math.min(b.maxY, to.getY()));
      Location tp = to.clone();
      tp.setY(clampedY);
      p.teleport(tp, PlayerTeleportEvent.TeleportCause.PLUGIN);
      plugin.messages().send(p, "bounds.outside");
      return;
    }

    Vector2D c = border.centerFor(a);
    double r = b.radius;
    if (to.getY() >= voidY && BorderMath.outside2D(to, c.x(), c.z(), r)) {
      switch (b.onCross) {
        case PUSH_BACK -> {
          e.setCancelled(true);
          Vector push = new Vector(c.x() - to.getX(), 0, c.z() - to.getZ())
              .normalize().multiply(b.pushStrength);
          p.setVelocity(push);
        }
        case TELEPORT -> {
          Location tp = BorderMath.clampInside(to, c.x(), c.z(), r);
          p.teleport(tp, PlayerTeleportEvent.TeleportCause.PLUGIN);
        }
        default -> e.setCancelled(true);
      }
      plugin.messages().send(p, "bounds.outside");
      return;
    }

    // Near-border warning
    double dist = Math.hypot(to.getX() - c.x(), to.getZ() - c.z());
    if (r - dist <= b.warning && to.getY() >= voidY) {
      plugin.actionBar().push(p, plugin.messages().msg("bounds.outside"), 1);
    }
  }
}
