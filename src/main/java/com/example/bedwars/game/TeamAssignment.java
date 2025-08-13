package com.example.bedwars.game;

import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;
import com.example.bedwars.arena.TeamData;
import java.util.Comparator;
import java.util.Optional;

/**
 * Handles simple team auto-balance/selection.
 */
public final class TeamAssignment {
  private final PlayerContextService ctx;

  public TeamAssignment(PlayerContextService ctx) {
    this.ctx = ctx;
  }

  /** Assign or balance team for player. */
  public TeamColor assign(Arena a, org.bukkit.entity.Player p) {
    TeamColor chosen = ctx.getTeam(p);
    if (chosen != null && a.activeTeams().contains(chosen)) {
      int count = ctx.countPlayers(a.id(), chosen);
      if (count < a.maxTeamSize()) return chosen;
    }
    Optional<TeamColor> best = a.activeTeams().stream()
        .filter(t -> ctx.countPlayers(a.id(), t) < a.maxTeamSize())
        .min(Comparator.comparingInt(t -> ctx.countPlayers(a.id(), t)));
    TeamColor team = best.orElse(a.activeTeams().iterator().next());
    ctx.setTeam(p, team);
    return team;
  }
}
