package com.example.bedwars.api;

import com.example.bedwars.arena.*;
import com.example.bedwars.gen.*;
import com.example.bedwars.shop.*;
import org.bukkit.Location;

import java.util.Collection;
import java.util.Optional;

/**
 * Management operations for arenas.
 */
public interface ArenaApi {
  Collection<Arena> all();
  Optional<Arena> get(String id);

  Arena create(String id, WorldRef world);
  boolean delete(String id);

  void setLobby(String id, Location lobby);
  void enableTeam(String id, TeamColor team);
  void disableTeam(String id, TeamColor team);
  void setTeamSpawn(String id, TeamColor team, Location spawn);
  void setTeamBed(String id, TeamColor team, Location bedBlock);

  Generator addGenerator(String id, GeneratorType type, Location loc, int tier);
  void removeGenerator(String id, java.util.UUID generatorId);

  NpcData addNpc(String id, NpcType type, Location loc);
  void removeNpc(String id, java.util.UUID npcId);
}
