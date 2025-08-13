package com.example.bedwars.arena;

import java.util.List;

/**
 * Preset arena configurations for team counts and sizes.
 */
public enum ArenaMode {
  EIGHT_X1(8, 1, List.of(TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW,
      TeamColor.AQUA, TeamColor.WHITE, TeamColor.PINK, TeamColor.GRAY)),
  EIGHT_X2(8, 2, List.of(TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW,
      TeamColor.AQUA, TeamColor.WHITE, TeamColor.PINK, TeamColor.GRAY)),
  FOUR_X3(4, 3, List.of(TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW)),
  FOUR_X4(4, 4, List.of(TeamColor.RED, TeamColor.BLUE, TeamColor.GREEN, TeamColor.YELLOW));

  public final int teams;
  public final int teamSize;
  public final List<TeamColor> palette;

  ArenaMode(int teams, int teamSize, List<TeamColor> palette) {
    this.teams = teams;
    this.teamSize = teamSize;
    this.palette = palette;
  }

  /** Human-readable display like "8x2". */
  public String display() {
    return teams + "x" + teamSize;
  }
}
