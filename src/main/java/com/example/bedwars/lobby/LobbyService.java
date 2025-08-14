package com.example.bedwars.lobby;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.game.PreJoinSnapshotService;
import com.example.bedwars.game.PlayerContextService;
import java.util.Optional;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

/** Handles teleporting players back to the lobby and cleaning their state. */
public final class LobbyService {
  public enum Reason { COMMAND, ITEM, DISCONNECT, GAME_END, OTHER }

  private final BedwarsPlugin plugin;
  private final PlayerContextService contexts;
  private final LobbyLocationService location;
  private final PreJoinSnapshotService snapshots;

  public LobbyService(BedwarsPlugin plugin, PlayerContextService ctx,
                      LobbyLocationService location, PreJoinSnapshotService snaps) {
    this.plugin = plugin;
    this.contexts = ctx;
    this.location = location;
    this.snapshots = snaps;
  }

  public void sendToLobby(Player p, Reason reason) {
    // remove from arena if needed
    String arenaId = contexts.getArena(p);
    if (arenaId != null) {
      plugin.game().leave(p, false);
    }
    sanitizePlayer(p);
    Optional.ofNullable(location.get().orElse(null)).ifPresentOrElse(loc -> {
      p.teleportAsync(loc);
    }, () -> {
      p.kick(org.bukkit.ChatColor.RED + "Lobby non configuré. /bwadmin lobby set");
    });
  }

  private void sanitizePlayer(Player p) {
    snapshots.restore(p);
    for (PotionEffect eff : p.getActivePotionEffects()) {
      p.removePotionEffect(eff.getType());
    }
    p.setAllowFlight(false);
    p.setFlying(false);
    p.setInvisible(false);
    p.setGameMode(GameMode.SURVIVAL);
    var mgr = Bukkit.getScoreboardManager();
    if (mgr != null) p.setScoreboard(mgr.getMainScoreboard());
    p.getInventory().setHeldItemSlot(0);
  }

  public LobbyLocationService location() { return location; }
  public PreJoinSnapshotService snapshots() { return snapshots; }
}
