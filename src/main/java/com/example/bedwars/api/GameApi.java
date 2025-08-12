package com.example.bedwars.api;

import com.example.bedwars.arena.Arena;

/**
 * Arena game state machine API.
 */
public interface GameApi {
  void start(Arena arena) throws IllegalStateException;
  void stop(Arena arena);
}
