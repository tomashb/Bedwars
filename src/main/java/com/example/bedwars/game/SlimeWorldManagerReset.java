package com.example.bedwars.game;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;

/** Placeholder SWM reset implementation delegating to default behaviour. */
public final class SlimeWorldManagerReset implements ArenaResetStrategy {
  private final DefaultSnapshotReset delegate;

  public SlimeWorldManagerReset(BedwarsPlugin plugin, ResetManager mgr){
    this.delegate = new DefaultSnapshotReset(plugin, mgr);
  }

  @Override public void prepare(Arena a) throws Exception { delegate.prepare(a); }
  @Override public void reset(Arena a) throws Exception { delegate.reset(a); }
  @Override public String name(){ return "SlimeWorldManagerReset"; }
}
