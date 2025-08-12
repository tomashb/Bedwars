package com.example.bedwars.api;

import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.TeamColor;

/**
 * Validation helpers for arena setup.
 */
public interface SetupApi {
  boolean isArenaReady(Arena a);
  boolean isTeamConfigured(Arena a, TeamColor t);
}
