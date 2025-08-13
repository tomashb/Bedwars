package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import com.example.bedwars.game.ArenaStateChangeEvent;
import java.util.HashMap;
import java.util.Map;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

/**
 * Core game loop and state machine.
 */
public final class GameService {
  private final BedwarsPlugin plugin;
  private final PlayerContextService contexts;
  private final TeamAssignment teamAssignment;
  private final KitService kit;
  private final SpectatorService spectator;
  private final GameMessages messages;
  private final Map<String, Integer> countdownTasks = new HashMap<>();
  private final Map<String, Integer> timerTasks = new HashMap<>();

  public GameService(BedwarsPlugin plugin, PlayerContextService ctx, TeamAssignment ta,
                     KitService kit, SpectatorService spec, GameMessages msgs) {
    this.plugin = plugin;
    this.contexts = ctx;
    this.teamAssignment = ta;
    this.kit = kit;
    this.spectator = spec;
    this.messages = msgs;
  }

  public void join(Player p, String arenaId) {
    if (contexts.getArena(p) != null) {
      messages.send(p, "game.already-in", Map.of());
      return;
    }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) {
      messages.send(p, "game.no-arena", Map.of());
      return;
    }
    if (a.state() != GameState.WAITING && a.state() != GameState.STARTING) {
      messages.send(p, "game.no-arena", Map.of());
      return;
    }
    contexts.join(p, arenaId);
    p.getInventory().clear();
    if (a.lobby() != null) p.teleport(a.lobby());
    messages.send(p, "game.join", Map.of("arena", arenaId));
    // auto assign
    TeamColor team = teamAssignment.assign(a, p);
    messages.send(p, "game.assign-team", Map.of("team", team.display));

    int count = contexts.countPlayers(arenaId);
    int min = plugin.getConfig().getInt("game.min-players", 2);
    if (a.state() == GameState.WAITING && count >= min) start(arenaId);
  }

  public void leave(Player p, boolean toLobby) {
    String arenaId = contexts.getArena(p);
    if (arenaId == null) {
      messages.send(p, "game.not-in-arena", Map.of());
      return;
    }
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    contexts.clear(p);
    if (a != null && toLobby && a.lobby() != null) p.teleport(a.lobby());
    messages.send(p, "game.left", Map.of());
    if (a != null && a.state() == GameState.RUNNING) {
      checkVictory(a);
    }
  }

  public void start(String arenaId) {
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) {
      return;
    }
    if (a.state() != GameState.WAITING && a.state() != GameState.STARTING) return;
    int players = contexts.countPlayers(arenaId);
    int min = plugin.getConfig().getInt("game.min-players", 2);
    if (players < min) {
      messages.broadcast(a, "game.not-enough", Map.of("count", players, "min", min));
      return;
    }
    a.setState(GameState.STARTING);
    Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(a, GameState.WAITING, GameState.STARTING));
    int task = new BukkitRunnable() {
      int sec = plugin.getConfig().getInt("game.countdown", 20);
      @Override public void run() {
        if (a.state() != GameState.STARTING) { cancel(); return; }
        if (sec <= 0) { cancel(); beginRunning(a); return; }
        messages.broadcast(a, "game.starting-in", Map.of("sec", sec));
        sec--;
      }
    }.runTaskTimer(plugin, 0L, 20L).getTaskId();
    countdownTasks.put(arenaId, task);
  }

  private void beginRunning(Arena a) {
    a.setState(GameState.RUNNING);
    Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(a, GameState.STARTING, GameState.RUNNING));
    for (Player p : contexts.playersInArena(a.id())) {
      TeamColor team = contexts.getTeam(p);
      if (team == null) team = teamAssignment.assign(a, p);
      TeamData td = a.team(team);
      Location spawn = td.spawn();
      if (spawn != null) p.teleport(spawn);
      kit.giveStartKit(p, team);
      plugin.upgrades().applySharpness(a.id(), team);
      plugin.upgrades().applyProtection(a.id(), team, td.upgrades().protection());
      plugin.upgrades().applyManicMiner(a.id(), team, td.upgrades().manicMiner());
      contexts.markAlive(p);
    }
    messages.broadcast(a, "game.started", Map.of());

    // start game timer for global tier announcements
    int timerTask = new BukkitRunnable() {
      int sec = 0;
      @Override public void run() {
        if (a.state() != GameState.RUNNING) { cancel(); return; }
        sec++;
        plugin.generators().onGlobalTime(a.id(), sec);
      }
    }.runTaskTimer(plugin, 20L, 20L).getTaskId();
    timerTasks.put(a.id(), timerTask);
  }

  public void stop(String arenaId, String reason) {
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    if (a.state() == GameState.STARTING) {
      Integer t = countdownTasks.remove(arenaId);
      if (t != null) Bukkit.getScheduler().cancelTask(t);
    }
    Integer timer = timerTasks.remove(arenaId);
    if (timer != null) Bukkit.getScheduler().cancelTask(timer);
    endGame(a, null);
    messages.broadcast(a, "game.stopped", Map.of());
  }

  public void forceWin(String arenaId, TeamColor team) {
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null) return;
    endGame(a, team);
  }

  private void checkVictory(Arena a) {
    var alive = contexts.aliveTeams(a.id());
    if (alive.size() <= 1) {
      TeamColor winner = alive.isEmpty() ? null : alive.iterator().next();
      endGame(a, winner);
    }
  }

  private void endGame(Arena a, TeamColor winner) {
    a.setState(GameState.ENDING);
    Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(a, GameState.RUNNING, GameState.ENDING));
    if (winner != null) messages.broadcast(a, "game.victory", Map.of("team", winner.display));
    for (Player p : contexts.playersInArena(a.id())) {
      if (a.lobby() != null) p.teleport(a.lobby());
      p.getInventory().clear();
      spectator.fromSpectator(p);
      contexts.clear(p);
    }
    plugin.generators().cleanupArena(a.id());
    Integer timer = timerTasks.remove(a.id());
    if (timer != null) Bukkit.getScheduler().cancelTask(timer);
    a.setState(GameState.RESTARTING);
    Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(a, GameState.ENDING, GameState.RESTARTING));
    plugin.arenas().load(a.id());
    a.setState(GameState.WAITING);
    Bukkit.getPluginManager().callEvent(new ArenaStateChangeEvent(a, GameState.RESTARTING, GameState.WAITING));
    contexts.clearArena(a.id());
  }

  public void handleBedBreak(Player p, Arena a, TeamColor broken) {
    a.team(broken).setBedBlock(null);
    messages.broadcast(a, "game.bed-destroyed", Map.of("team", broken.display, "player", p.getName()));
    for (Player pl : contexts.playersInArena(a.id())) {
      if (contexts.getTeam(pl) == broken) {
        messages.send(pl, "game.bed-destroyed-you", Map.of());
      }
    }
  }

  public void handleDeath(Player p) {
    String arenaId = contexts.getArena(p);
    if (arenaId == null) return;
    Arena a = plugin.arenas().get(arenaId).orElse(null);
    if (a == null || a.state() != GameState.RUNNING) return;
    TeamColor team = contexts.getTeam(p);
    contexts.markDead(p);
    TeamData td = a.team(team);
    if (td.bedBlock() != null) {
      int respawn = plugin.getConfig().getInt("game.respawn-seconds", 5);
      new BukkitRunnable() {
        int sec = respawn;
        public void run() {
          if (sec <= 0) {
            cancel();
            contexts.markAlive(p);
            Location spawn = td.spawn();
            if (spawn != null) p.teleport(spawn);
            kit.giveRespawnKit(p, team);
            plugin.upgrades().applySharpness(arenaId, team);
            plugin.upgrades().applyProtection(arenaId, team, td.upgrades().protection());
            plugin.upgrades().applyManicMiner(arenaId, team, td.upgrades().manicMiner());
            messages.send(p, "game.respawn-now", Map.of());
          } else {
            messages.send(p, "game.respawn-in", Map.of("sec", sec));
            sec--;
          }
        }
      }.runTaskTimer(plugin, 20L, 20L);
    } else {
      spectator.toSpectator(p, a);
      messages.send(p, "game.spectating", Map.of());
      messages.broadcast(a, "game.eliminated", Map.of("player", p.getName()));
      checkVictory(a);
    }
  }
}
