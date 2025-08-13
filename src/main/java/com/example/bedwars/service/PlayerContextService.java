package com.example.bedwars.service;

import com.example.bedwars.arena.TeamColor;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.entity.Player;

/**
 * Very small service storing the arena/team context of players.
 * This is a placeholder until a full join/leave system is implemented.
 */
public final class PlayerContextService {

  public static final class Context {
    public final String arenaId;
    public final TeamColor team;
    public Context(String arenaId, TeamColor team) {
      this.arenaId = arenaId; this.team = team;
    }
  }

  private final Map<UUID, Context> contexts = new ConcurrentHashMap<>();

  public void set(Player p, String arenaId, TeamColor team) {
    contexts.put(p.getUniqueId(), new Context(arenaId, team));
  }

  public void remove(Player p) {
    contexts.remove(p.getUniqueId());
  }

  public Optional<Context> get(Player p) {
    return Optional.ofNullable(contexts.get(p.getUniqueId()));
  }

  public Optional<Context> get(UUID id) {
    return Optional.ofNullable(contexts.get(id));
  }
}
