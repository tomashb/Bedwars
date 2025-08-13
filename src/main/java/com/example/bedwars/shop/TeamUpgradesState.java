package com.example.bedwars.shop;

import java.util.ArrayDeque;
import java.util.Deque;

/**
 * Holds the state of purchased upgrades for a team.
 */
public final class TeamUpgradesState {
  private boolean sharpness = false;     // +Sharpness I on swords
  private int protection = 0;            // 0..4
  private int manicMiner = 0;            // 0..2
  private boolean healPool = false;      // regen around base
  private int forge = 0;                 // 0..4
  private final Deque<TrapType> trapQueue = new ArrayDeque<>(3);

  public boolean sharpness() { return sharpness; }
  public int protection()    { return protection; }
  public int manicMiner()    { return manicMiner; }
  public boolean healPool()  { return healPool; }
  public int forge()         { return forge; }
  public Deque<TrapType> trapQueue() { return trapQueue; }

  public void setSharpness(boolean v){ this.sharpness = v; }
  public void setProtection(int lvl){ this.protection = Math.max(0, Math.min(4, lvl)); }
  public void setManicMiner(int lvl){ this.manicMiner = Math.max(0, Math.min(2, lvl)); }
  public void setHealPool(boolean v){ this.healPool = v; }
  public void setForge(int lvl){ this.forge = Math.max(0, Math.min(4, lvl)); }
}
