package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import java.util.Map;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/** Handles death, spectator mode and timed respawn. */
public final class DeathRespawnService {
  private final BedwarsPlugin plugin;
  private final PlayerContextService ctx;
  private final KitService kit;
  private final SpectatorService spectator;
  private final GameMessages messages;
  private final GameService game;

  public DeathRespawnService(BedwarsPlugin plugin, PlayerContextService ctx,
                             KitService kit, SpectatorService spectator,
                             GameMessages messages, GameService game) {
    this.plugin = plugin;
    this.ctx = ctx;
    this.kit = kit;
    this.spectator = spectator;
    this.messages = messages;
    this.game = game;
  }

  /** Called when a player dies. */
  public void handleDeath(Player p) {
    String arenaId = ctx.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) return;
    TeamColor team = ctx.getTeam(p);
    ctx.markDead(p);
    ctx.setSpectating(p, true);

    TeamData td = a.team(team);
    boolean hasBed = td.bedBlock() != null;
    spectator.toSpectator(p, a);

    if (!hasBed) {
      p.sendTitle(plugin.messages().get("eliminated_title"),
          plugin.messages().get("eliminated_sub"), 0, 60, 20);
      messages.broadcast(a, "game.eliminated", Map.of("player", p.getName()));
      game.checkVictory(a);
      return;
    }

    final int delay = plugin.getConfig().getInt("respawn.delay_seconds", 5);
    BukkitRunnable task = new BukkitRunnable() {
      int sec = delay;
      @Override public void run() {
        if (!p.isOnline() || a.state() != GameState.RUNNING) { cancelTask(); return; }
        if (sec > 0) {
          p.sendTitle(
              plugin.messages().format("respawn.countdown_title", Map.of("sec", sec)),
              plugin.messages().get("respawn.countdown_sub"), 0, 20, 5);
          sec--; return;
        }
        // Respawn now
        ctx.markAlive(p);
        ctx.setSpectating(p, false);
        Location spawn = td.spawn();
        if (spawn != null) p.teleport(spawn);
        spectator.fromSpectator(p);
        p.setGameMode(org.bukkit.GameMode.SURVIVAL);
        kit.giveRespawnKit(p, team);
        if (td.upgrades().sharpness()) plugin.upgrades().applySharpness(arenaId, team);
        plugin.upgrades().applyProtection(arenaId, team, td.upgrades().protection());
        plugin.upgrades().applyManicMiner(arenaId, team, td.upgrades().manicMiner());
        p.sendMessage(plugin.messages().get("respawn.respawned"));
        ctx.clearRespawnTask(p);
        cancel();
      }
      private void cancelTask() { ctx.clearRespawnTask(p); cancel(); }
    };
    int id = task.runTaskTimer(plugin, 0L, 20L).getTaskId();
    ctx.setRespawnTask(p, id);
  }

  /** Cancels respawn timers for players whose bed was destroyed while dead. */
  public void handleBedDestroyed(Arena a, TeamColor team) {
    for (Player pl : ctx.playersInArena(a.id())) {
      if (ctx.getTeam(pl) != team) continue;
      int task = ctx.getRespawnTask(pl);
      if (task != -1) {
        org.bukkit.Bukkit.getScheduler().cancelTask(task);
        ctx.clearRespawnTask(pl);
        pl.sendTitle(plugin.messages().get("eliminated_title"),
            plugin.messages().get("eliminated_sub"), 0, 60, 20);
        messages.broadcast(a, "game.eliminated", Map.of("player", pl.getName()));
      }
    }
    game.checkVictory(a);
  }
}
