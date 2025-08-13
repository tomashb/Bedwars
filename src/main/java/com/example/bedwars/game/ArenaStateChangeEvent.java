package com.example.bedwars.game;

import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Simple event fired when an arena changes state.
 */
public final class ArenaStateChangeEvent extends Event {
  private static final HandlerList HANDLERS = new HandlerList();
  private final Arena arena;
  private final GameState oldState;
  private final GameState newState;

  public ArenaStateChangeEvent(Arena arena, GameState oldState, GameState newState) {
    this.arena = arena;
    this.oldState = oldState;
    this.newState = newState;
  }

  public Arena arena() { return arena; }
  public GameState oldState() { return oldState; }
  public GameState newState() { return newState; }

  @Override
  public HandlerList getHandlers() { return HANDLERS; }
  public static HandlerList getHandlerList() { return HANDLERS; }
}
