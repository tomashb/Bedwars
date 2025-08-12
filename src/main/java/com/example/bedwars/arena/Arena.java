package com.example.bedwars.arena;

import com.example.bedwars.gen.Generator;
import com.example.bedwars.shop.NpcData;

import org.bukkit.Location;
import java.util.*;

/**
 * Representation of a BedWars arena with teams, generators and NPCs.
 */
public final class Arena {
  private final String id;
  private WorldRef world;
  private GameState state = GameState.WAITING;

  private Location lobby; // null if undefined
  private final EnumSet<TeamColor> enabledTeams = EnumSet.noneOf(TeamColor.class);
  private final EnumMap<TeamColor, TeamData> teams = new EnumMap<>(TeamColor.class);
  private final List<Generator> generators = new ArrayList<>();
  private final List<NpcData> npcs = new ArrayList<>();

  public Arena(String id, WorldRef world) {
    this.id = Objects.requireNonNull(id);
    this.world = Objects.requireNonNull(world);
    for (TeamColor c : TeamColor.values()) teams.put(c, new TeamData());
  }

  public String id() { return id; }
  public WorldRef world() { return world; }
  public GameState state() { return state; }
  public Location lobby() { return lobby; }
  public Set<TeamColor> enabledTeams() { return Collections.unmodifiableSet(enabledTeams); }
  public Map<TeamColor, TeamData> teams() { return Collections.unmodifiableMap(teams); }
  public List<Generator> generators() { return Collections.unmodifiableList(generators); }
  public List<NpcData> npcs() { return Collections.unmodifiableList(npcs); }

  public Arena setWorld(WorldRef world) { this.world = Objects.requireNonNull(world); return this; }
  public Arena setState(GameState s) { this.state = Objects.requireNonNull(s); return this; }
  public Arena setLobby(Location lobby) { this.lobby = Objects.requireNonNull(lobby); return this; }

  public Arena enableTeam(TeamColor c) { enabledTeams.add(c); return this; }
  public Arena disableTeam(TeamColor c) { enabledTeams.remove(c); return this; }

  public TeamData team(TeamColor c) { return teams.get(c); }
  public Arena addGenerator(Generator g) { generators.add(Objects.requireNonNull(g)); return this; }
  public Arena addNpc(NpcData n) { npcs.add(Objects.requireNonNull(n)); return this; }
}
