package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.api.ArenaApi;
import com.example.bedwars.gen.Generator;
import com.example.bedwars.gen.GeneratorType;
import com.example.bedwars.shop.NpcData;
import com.example.bedwars.shop.NpcType;
import com.example.bedwars.util.YamlIO;
import com.example.bedwars.util.YamlLocation;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manager handling creation, persistence and lookup of arenas.
 */
public final class ArenaManager implements ArenaApi {
  private final BedwarsPlugin plugin;
  private final Map<String, Arena> arenas = new ConcurrentHashMap<>();
  private final File dir;

  public ArenaManager(BedwarsPlugin plugin) {
    this.plugin = plugin;
    this.dir = new File(plugin.getDataFolder(), "arenas");
    if (!dir.exists()) dir.mkdirs();
  }

  @Override
  public Collection<Arena> all() {
    return Collections.unmodifiableCollection(arenas.values());
  }

  @Override
  public Optional<Arena> get(String id) {
    return Optional.ofNullable(arenas.get(id));
  }

  @Override
  public Arena create(String id, WorldRef world) {
    if (arenas.containsKey(id)) throw new IllegalArgumentException("Arena already exists: " + id);
    Arena a = new Arena(id, world);
    a.enableTeam(TeamColor.RED).enableTeam(TeamColor.BLUE);
    arenas.put(id, a);
    save(id);
    return a;
  }

  @Override
  public boolean delete(String id) {
    Arena removed = arenas.remove(id);
    File f = new File(dir, id + ".yml");
    boolean file = !f.exists() || f.delete();
    // TODO cleanup entities tagged with bw_arena=id when gameplay is wired
    return removed != null && file;
  }

  public void loadAll() {
    arenas.clear();
    File[] files = dir.listFiles((d, name) -> name.endsWith(".yml"));
    if (files == null) return;
    for (File f : files) {
      try {
        load(stripExt(f.getName()));
      } catch (Exception ex) {
        plugin.getLogger().warning("Failed to load arena " + f.getName() + ": " + ex.getMessage());
      }
    }
  }

  public Optional<Arena> load(String id) {
    File f = new File(dir, id + ".yml");
    if (!f.exists()) return Optional.empty();
    YamlConfiguration y = YamlIO.load(f);

    String world = y.getString("world");
    if (world == null || world.isEmpty()) {
      plugin.getLogger().warning("Arena " + id + " missing world");
      return Optional.empty();
    }
    Arena a = new Arena(id, new WorldRef(world));
    a.setState(GameState.WAITING);

    if (y.isConfigurationSection("lobby")) {
      a.setLobby(YamlLocation.fromMap(a.world(), y.getConfigurationSection("lobby")));
    }

    for (String t : y.getStringList("enabled-teams")) {
      try {
        a.enableTeam(TeamColor.valueOf(t));
      } catch (IllegalArgumentException ex) {
        plugin.getLogger().warning("Unknown team " + t + " in arena " + id);
      }
    }

    ConfigurationSection teams = y.getConfigurationSection("teams");
    if (teams != null) {
      for (String key : teams.getKeys(false)) {
        TeamColor tc;
        try {
          tc = TeamColor.valueOf(key);
        } catch (IllegalArgumentException ex) {
          plugin.getLogger().warning("Unknown team section " + key + " in arena " + id);
          continue;
        }
        ConfigurationSection sec = teams.getConfigurationSection(key);
        if (sec == null) continue;
        TeamData td = a.team(tc);
        td.setMaxPlayers(sec.getInt("maxPlayers", 4));
        if (sec.isConfigurationSection("spawn")) {
          td.setSpawn(YamlLocation.fromMap(a.world(), sec.getConfigurationSection("spawn")));
        }
        if (sec.isConfigurationSection("bed")) {
          ConfigurationSection b = sec.getConfigurationSection("bed");
          Location bed = new Location(Bukkit.getWorld(a.world().name()),
              b.getDouble("x"), b.getDouble("y"), b.getDouble("z"));
          td.setBedBlock(bed);
        }
      }
    }

    List<Map<?, ?>> genList = y.getMapList("generators");
    for (Map<?, ?> map : genList) {
      try {
        UUID gid = UUID.fromString(String.valueOf(map.get("id")));
        GeneratorType type = GeneratorType.valueOf(String.valueOf(map.get("type")));
        Map<String, Object> locMap = castMap(map.get("location"));
        YamlConfiguration temp = new YamlConfiguration();
        temp.createSection("loc", locMap);
        Location loc = YamlLocation.fromMap(a.world(), temp.getConfigurationSection("loc"));
        Generator g = new Generator(gid, type, loc)
            .setTier(getInt(map, "tier", 1))
            .setIntervalTicks(getInt(map, "interval", 120))
            .setAmount(getInt(map, "amount", 1));
        a.addGenerator(g);
      } catch (Exception ex) {
        plugin.getLogger().warning("Invalid generator in arena " + id + ": " + ex.getMessage());
      }
    }

    List<Map<?, ?>> npcList = y.getMapList("npcs");
    for (Map<?, ?> map : npcList) {
      try {
        UUID nid = UUID.fromString(String.valueOf(map.get("id")));
        NpcType type = NpcType.valueOf(String.valueOf(map.get("type")));
        Map<String, Object> locMap = castMap(map.get("location"));
        YamlConfiguration temp = new YamlConfiguration();
        temp.createSection("loc", locMap);
        Location loc = YamlLocation.fromMap(a.world(), temp.getConfigurationSection("loc"));
        a.addNpc(new NpcData(nid, type, loc));
      } catch (Exception ex) {
        plugin.getLogger().warning("Invalid npc in arena " + id + ": " + ex.getMessage());
      }
    }

    arenas.put(id, a);
    return Optional.of(a);
  }

  public void save(String id) {
    Arena a = arenas.get(id);
    if (a == null) return;
    YamlConfiguration y = new YamlConfiguration();
    y.set("id", a.id());
    y.set("world", a.world().name());
    y.set("state", a.state().name());

    if (a.lobby() != null) {
      y.createSection("lobby", YamlLocation.toMap(a.lobby()));
    }

    List<String> enabled = a.enabledTeams().stream().map(Enum::name).toList();
    y.set("enabled-teams", enabled);

    ConfigurationSection teams = y.createSection("teams");
    for (var entry : a.teams().entrySet()) {
      ConfigurationSection t = teams.createSection(entry.getKey().name());
      TeamData td = entry.getValue();
      t.set("maxPlayers", td.maxPlayers());
      if (td.spawn() != null) {
        t.createSection("spawn", YamlLocation.toMap(td.spawn()));
      }
      if (td.bedBlock() != null) {
        Map<String, Object> bed = new LinkedHashMap<>();
        bed.put("x", td.bedBlock().getX());
        bed.put("y", td.bedBlock().getY());
        bed.put("z", td.bedBlock().getZ());
        t.createSection("bed", bed);
      }
    }

    List<Map<String, Object>> gens = new ArrayList<>();
    for (Generator g : a.generators()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", g.id().toString());
      m.put("type", g.type().name());
      m.put("tier", g.tier());
      m.put("interval", g.intervalTicks());
      m.put("amount", g.amount());
      m.put("location", YamlLocation.toMap(g.location()));
      gens.add(m);
    }
    y.set("generators", gens);

    List<Map<String, Object>> npcs = new ArrayList<>();
    for (NpcData n : a.npcs()) {
      Map<String, Object> m = new LinkedHashMap<>();
      m.put("id", n.id().toString());
      m.put("type", n.type().name());
      m.put("location", YamlLocation.toMap(n.location()));
      npcs.add(m);
    }
    y.set("npcs", npcs);

    try {
      YamlIO.save(new File(dir, id + ".yml"), y);
    } catch (IOException ex) {
      plugin.getLogger().severe("Failed to save arena " + id + ": " + ex.getMessage());
    }
  }

  public void saveAll() {
    arenas.keySet().forEach(this::save);
  }

  // === API operations ===
  @Override
  public void setLobby(String id, Location lobby) {
    Arena a = arenas.get(id);
    if (a != null) a.setLobby(lobby);
  }

  @Override
  public void enableTeam(String id, TeamColor team) {
    Arena a = arenas.get(id);
    if (a != null) a.enableTeam(team);
  }

  @Override
  public void disableTeam(String id, TeamColor team) {
    Arena a = arenas.get(id);
    if (a != null) a.disableTeam(team);
  }

  @Override
  public void setTeamSpawn(String id, TeamColor team, Location spawn) {
    Arena a = arenas.get(id);
    if (a != null) a.team(team).setSpawn(spawn);
  }

  @Override
  public void setTeamBed(String id, TeamColor team, Location bedBlock) {
    Arena a = arenas.get(id);
    if (a != null) a.team(team).setBedBlock(bedBlock);
  }

  @Override
  public Generator addGenerator(String id, GeneratorType type, Location loc, int tier) {
    Arena a = arenas.get(id);
    if (a == null) throw new IllegalArgumentException("Arena not found: " + id);
    Generator g = new Generator(type, loc).setTier(tier);
    a.addGenerator(g);
    return g;
  }

  @Override
  public void removeGenerator(String id, UUID generatorId) {
    Arena a = arenas.get(id);
    if (a == null) return;
    a.removeGenerator(generatorId);
  }

  @Override
  public NpcData addNpc(String id, NpcType type, Location loc) {
    Arena a = arenas.get(id);
    if (a == null) throw new IllegalArgumentException("Arena not found: " + id);
    NpcData n = new NpcData(type, loc);
    a.addNpc(n);
    return n;
  }

  @Override
  public void removeNpc(String id, UUID npcId) {
    Arena a = arenas.get(id);
    if (a == null) return;
    a.removeNpc(npcId);
  }

  private static int getInt(Map<?, ?> map, String key, int def) {
    Object o = map.get(key);
    if (o instanceof Number n) return n.intValue();
    try {
      return Integer.parseInt(String.valueOf(o));
    } catch (Exception ex) {
      return def;
    }
  }

  private static Map<String, Object> castMap(Object o) {
    Map<String, Object> m = new LinkedHashMap<>();
    if (o instanceof Map<?, ?> map) {
      for (Map.Entry<?, ?> e : map.entrySet()) {
        m.put(String.valueOf(e.getKey()), e.getValue());
      }
    }
    return m;
  }

  private static String stripExt(String name) {
    int i = name.lastIndexOf('.');
    return i < 0 ? name : name.substring(0, i);
  }
}
