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
  private ArenaMode mode = ArenaMode.EIGHT_X1;
  private int maxTeamSize = mode.teamSize;
  private final EnumSet<TeamColor> activeTeams = EnumSet.noneOf(TeamColor.class);
  private final EnumMap<TeamColor, TeamData> teams = new EnumMap<>(TeamColor.class);
  private final List<Generator> generators = new ArrayList<>();
  private final List<NpcData> npcs = new ArrayList<>();

  public Arena(String id, WorldRef world) {
    this.id = Objects.requireNonNull(id);
    this.world = Objects.requireNonNull(world);
    for (TeamColor c : TeamColor.values()) teams.put(c, new TeamData());
    activeTeams.addAll(mode.palette);
  }

  public String id() { return id; }
  public WorldRef world() { return world; }
  public GameState state() { return state; }
  public Location lobby() { return lobby; }
  public ArenaMode mode() { return mode; }
  public int maxTeamSize() { return maxTeamSize; }
  public Set<TeamColor> activeTeams() { return Collections.unmodifiableSet(activeTeams); }
  @Deprecated public Set<TeamColor> enabledTeams() { return activeTeams(); }
  public Map<TeamColor, TeamData> teams() { return Collections.unmodifiableMap(teams); }
  public List<Generator> generators() { return Collections.unmodifiableList(generators); }
  public List<NpcData> npcs() { return Collections.unmodifiableList(npcs); }

  public Arena setWorld(WorldRef world) { this.world = Objects.requireNonNull(world); return this; }
  public Arena setState(GameState s) { this.state = Objects.requireNonNull(s); return this; }
  public Arena setLobby(Location lobby) { this.lobby = Objects.requireNonNull(lobby); return this; }

  public Arena setMode(ArenaMode m) { this.mode = Objects.requireNonNull(m); return this; }
  public Arena setMaxTeamSize(int s) { this.maxTeamSize = s; return this; }
  public Arena setActiveTeams(Set<TeamColor> set) { activeTeams.clear(); activeTeams.addAll(set); return this; }
  public Arena enableTeam(TeamColor c) { activeTeams.add(c); return this; }
  public Arena disableTeam(TeamColor c) { activeTeams.remove(c); return this; }

  public TeamData team(TeamColor c) { return teams.get(c); }
  public Arena addGenerator(Generator g) { generators.add(Objects.requireNonNull(g)); return this; }
  public Arena addNpc(NpcData n) { npcs.add(Objects.requireNonNull(n)); return this; }
  public Arena removeGenerator(java.util.UUID id) {
    generators.removeIf(g -> g.id().equals(id));
    return this;
  }
  public Arena removeNpc(java.util.UUID id) {
    npcs.removeIf(n -> n.id().equals(id));
    return this;
  }
}
