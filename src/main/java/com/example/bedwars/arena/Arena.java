package com.example.bedwars.arena;

import com.example.bedwars.gen.Generator;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.*;

public class Arena {
    private final String name;
    private final String worldName;
    private GameState state = GameState.DISABLED;

    private Location lobby;
    private final Map<TeamColor, Location> spawns = new EnumMap<>(TeamColor.class);
    private final Map<TeamColor, Location> beds = new EnumMap<>(TeamColor.class);
    private final Map<TeamColor, Boolean> bedAlive = new EnumMap<>(TeamColor.class);
    private final Map<TeamColor, Set<UUID>> teamPlayers = new EnumMap<>(TeamColor.class);
    private final Set<TeamColor> enabledTeams = new HashSet<>();

    private final List<Generator> generators = new ArrayList<>();
    private Location itemShop, upgradeShop;

    public Arena(String name, String worldName) {
        this.name = name; this.worldName = worldName;
        for (TeamColor t : TeamColor.values()) {
            bedAlive.put(t, true); teamPlayers.put(t, new HashSet<>());
        }
    }

    public String getName(){ return name; } public String getWorldName(){ return worldName; }
    public World getWorld(){ return Bukkit.getWorld(worldName); }
    public GameState getState(){ return state; } public void setState(GameState s){ state=s; }

    public Location getLobby(){ return lobby; } public void setLobby(Location l){ lobby=l; }
    public void setSpawn(TeamColor c, Location l){ spawns.put(c,l); } public Location getSpawn(TeamColor c){ return spawns.get(c); }
    public void setBed(TeamColor c, Location l){ beds.put(c,l); } public Location getBed(TeamColor c){ return beds.get(c); }
    public boolean isBedAlive(TeamColor c){ return bedAlive.getOrDefault(c,true); } public void setBedAlive(TeamColor c, boolean b){ bedAlive.put(c,b); }

    public Location getItemShop(){ return itemShop; } public void setItemShop(Location l){ itemShop=l; }
    public Location getUpgradeShop(){ return upgradeShop; } public void setUpgradeShop(Location l){ upgradeShop=l; }

    public void addPlayer(TeamColor t, Player p){ teamPlayers.get(t).add(p.getUniqueId()); }
    public int getTeamSize(TeamColor t){ return teamPlayers.get(t).size(); }
    public void removePlayer(UUID id){ for (Set<UUID> s : teamPlayers.values()) s.remove(id); }
    public TeamColor getTeamOf(UUID id){ for (var e:teamPlayers.entrySet()) if (e.getValue().contains(id)) return e.getKey(); return null; }
    public Collection<UUID> getAllPlayers(){ Set<UUID> a=new HashSet<>(); for (Set<UUID> s:teamPlayers.values()) a.addAll(s); return a; }

    public void broadcast(String msg){ for(UUID id:getAllPlayers()){ Player p=Bukkit.getPlayer(id); if(p!=null) p.sendMessage(com.example.bedwars.util.C.PREFIX+msg);} }

    public List<Generator> getGenerators(){ return generators; }
    public boolean isConfigured(){ return lobby!=null && !spawns.isEmpty() && !beds.isEmpty(); }

    public Set<TeamColor> getEnabledTeams(){ return enabledTeams; }
    public boolean isTeamEnabled(TeamColor t){ return enabledTeams.isEmpty() || enabledTeams.contains(t); }
    public void setTeamEnabled(TeamColor t, boolean on){ if(on) enabledTeams.add(t); else enabledTeams.remove(t); }
    public boolean hasSpawn(TeamColor t){ return spawns.get(t)!=null; }
    public boolean hasBed(TeamColor t){ return beds.get(t)!=null; }
}
