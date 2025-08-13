package com.example.bedwars.game;

import com.example.bedwars.arena.TeamColor;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Tracks which arena and team players belong to during games.
 */
public final class PlayerContextService {

  public static final class Context {
    public final String arenaId;
    private TeamColor team;
    private boolean alive = true;
    private boolean spectating = false;
    private int respawnTask = -1;
    private int armorTier = 0;

    public Context(String arenaId, TeamColor team) {
      this.arenaId = arenaId;
      this.team = team;
    }

    public TeamColor team() { return team; }
    public void team(TeamColor t) { this.team = t; }
    public boolean alive() { return alive; }
    public void alive(boolean a) { this.alive = a; }
    public boolean spectating() { return spectating; }
    public void spectating(boolean s) { this.spectating = s; }
    public int respawnTask() { return respawnTask; }
    public void respawnTask(int id) { this.respawnTask = id; }

    public int armorTier() { return armorTier; }
    public void armorTier(int tier) { this.armorTier = tier; }
  }

  private final Map<UUID, Context> contexts = new ConcurrentHashMap<>();

  public void join(Player p, String arenaId) {
    contexts.put(p.getUniqueId(), new Context(arenaId, null));
  }

  public void clear(Player p) { contexts.remove(p.getUniqueId()); }

  public void clearArena(String arenaId) {
    contexts.entrySet().removeIf(e -> e.getValue().arenaId.equals(arenaId));
  }

  public String getArena(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c == null ? null : c.arenaId;
  }

  public boolean isInArena(Player p) {
    return contexts.containsKey(p.getUniqueId());
  }

  public TeamColor getTeam(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c == null ? null : c.team();
  }

  public void setTeam(Player p, TeamColor team) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.team(team);
  }

  public void markDead(Player p) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.alive(false);
  }

  public void markAlive(Player p) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.alive(true);
  }

  /** Returns whether the player is marked as alive in their context. */
  public boolean isAlive(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c != null && c.alive();
  }

  public boolean isSpectating(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c != null && c.spectating();
  }

  public void setSpectating(Player p, boolean s) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.spectating(s);
  }

  public int getRespawnTask(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c == null ? -1 : c.respawnTask();
  }

  public void setRespawnTask(Player p, int id) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.respawnTask(id);
  }

  public void clearRespawnTask(Player p) {
    setRespawnTask(p, -1);
  }

  public int getArmorTier(Player p) {
    Context c = contexts.get(p.getUniqueId());
    return c == null ? 0 : c.armorTier();
  }

  public void setArmorTier(Player p, int tier) {
    Context c = contexts.get(p.getUniqueId());
    if (c != null) c.armorTier(tier);
  }

  public Collection<Player> playersInArena(String arenaId) {
    return Bukkit.getOnlinePlayers().stream()
        .filter(p -> arenaId.equals(getArena(p)))
        .collect(Collectors.toList());
  }

  public int countPlayers(String arenaId) {
    return (int) Bukkit.getOnlinePlayers().stream()
        .filter(p -> arenaId.equals(getArena(p)))
        .count();
  }

  public int countPlayers(String arenaId, TeamColor team) {
    return (int) Bukkit.getOnlinePlayers().stream()
        .filter(p -> arenaId.equals(getArena(p)) && team == getTeam(p))
        .count();
  }

  /** Counts alive players for a given team in an arena. */
  public int aliveCount(String arenaId, TeamColor team) {
    return (int) Bukkit.getOnlinePlayers().stream()
        .filter(p -> arenaId.equals(getArena(p)) && team == getTeam(p))
        .filter(this::isAlive)
        .count();
  }

  public Set<TeamColor> aliveTeams(String arenaId) {
    return Bukkit.getOnlinePlayers().stream()
        .filter(p -> arenaId.equals(getArena(p)))
        .filter(p -> {
          Context c = contexts.get(p.getUniqueId());
          return c != null && c.alive() && c.team() != null;
        })
        .map(p -> contexts.get(p.getUniqueId()).team())
        .collect(Collectors.toSet());
  }
}
