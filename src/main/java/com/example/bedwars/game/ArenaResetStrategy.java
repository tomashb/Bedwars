package com.example.bedwars.game;

import com.example.bedwars.arena.Arena;

/** Strategy for resetting arenas. */
public interface ArenaResetStrategy {
  /** Prepare arena for reset: teleport players, stop tasks, cleanup. */
  void prepare(Arena a) throws Exception;

  /** Reset the given arena world/instance. */
  void reset(Arena a) throws Exception;

  /** Name of the strategy for logging. */
  String name();
}
