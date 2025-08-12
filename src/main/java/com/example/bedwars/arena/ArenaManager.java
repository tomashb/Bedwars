package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.generator.GeneratorType;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Handles loading, saving and simple manipulation of arena
 * configurations. Only a subset of full BedWars features are
 * implemented but the structure allows future expansion.
 */
public class ArenaManager {

    private final BedwarsPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

    @SuppressWarnings("unchecked")
    private Map<String, Object> mso(Object o) { return (Map<String, Object>) o; }

    private int intOf(Object o) { return ((Number) o).intValue(); }

    private double dblOf(Object o) { return ((Number) o).doubleValue(); }

    public ArenaManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        loadArenas();
    }

    private void loadArenas() {
        File folder = new File(plugin.getDataFolder(), "arenas");
        if (!folder.exists()) {
            folder.mkdirs();
            plugin.saveResource("arenas/example.yml", false);
        }
        arenas.clear();
        File[] files = folder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (files == null) {
            return;
        }
        for (File file : files) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String id = cfg.getString("id", file.getName().replace(".yml", ""));
            Arena arena = new Arena(plugin, id);
            arena.setWorld(cfg.getString("world", ""));
            if (cfg.isString("lobby")) {
                arena.setLobby(parseLocation(arena.getWorld(), cfg.getString("lobby")));
            }
            for (String t : cfg.getStringList("enabled-teams")) {
                try {
                    arena.enableTeam(TeamColor.valueOf(t.toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                }
            }
            ConfigurationSection teams = cfg.getConfigurationSection("teams");
            if (teams != null) {
                for (String key : teams.getKeys(false)) {
                    try {
                        TeamColor color = TeamColor.valueOf(key.toUpperCase(Locale.ROOT));
                        String spawn = teams.getString(key + ".spawn");
                        if (spawn != null) {
                            arena.setTeamSpawn(color, parseLocation(arena.getWorld(), spawn));
                        }
                        String bed = teams.getString(key + ".bed");
                        if (bed != null) {
                            String[] parts = bed.split(",");
                            if (parts.length >= 3) {
                                Location loc = parseLocation(arena.getWorld(), parts[0] + "," + parts[1] + "," + parts[2] + ",0,0");
                                arena.setTeamBed(color, loc);
                            }
                        }
                    } catch (IllegalArgumentException ignored) {
                    }
                }
            }
            ConfigurationSection shops = cfg.getConfigurationSection("shops");
            if (shops != null) {
                for (String s : shops.getStringList("item")) {
                    arena.addItemShop(parseLocation(arena.getWorld(), s));
                }
                for (String s : shops.getStringList("upgrade")) {
                    arena.addUpgradeShop(parseLocation(arena.getWorld(), s));
                }
            }
            for (Object obj : cfg.getMapList("generators")) {
                try {
                    Map<String, Object> map = mso(obj);
                    GeneratorType type = GeneratorType.valueOf(String.valueOf(map.get("type")));
                    String locStr = String.valueOf(map.get("loc"));
                    int tier = map.containsKey("tier") ? intOf(map.get("tier")) : 1;
                    arena.addGenerator(type, parseLocation(arena.getWorld(), locStr), tier);
                } catch (Exception ignored) {
                }
            }
            arenas.put(id.toLowerCase(Locale.ROOT), arena);
        }
    }

    public boolean exists(String id) {
        return arenas.containsKey(id.toLowerCase(Locale.ROOT));
    }

    /**
     * Create a new arena configuration with default values and persist it.
     */
    public boolean create(String id, String world) {
        if (exists(id)) {
            return false;
        }
        Arena arena = new Arena(plugin, id);
        arena.setWorld(world);
        arena.enableTeam(TeamColor.RED);
        arena.enableTeam(TeamColor.BLUE);
        arenas.put(id.toLowerCase(Locale.ROOT), arena);
        save(id);
        return true;
    }

    /** Save an arena configuration to its YAML file. */
    public void save(String id) {
        Arena arena = arenas.get(id.toLowerCase(Locale.ROOT));
        if (arena == null) {
            return;
        }
        File file = new File(plugin.getDataFolder(), "arenas/" + id + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("id", arena.getName());
        cfg.set("world", arena.getWorld());
        cfg.set("state", "WAITING");
        cfg.set("lobby", arena.getLobby() == null ? null : formatLocation(arena.getLobby()));
        cfg.set("enabled-teams", arena.getEnabledTeams().stream().map(Enum::name).toList());
        Map<String, Object> teams = new LinkedHashMap<>();
        for (TeamColor color : arena.getEnabledTeams()) {
            Map<String, Object> data = new LinkedHashMap<>();
            Location spawn = arena.getTeamSpawn(color);
            data.put("spawn", spawn == null ? null : formatLocation(spawn));
            Arena.BedData bed = arena.getTeamBed(color);
            data.put("bed", bed == null ? null : formatBlock(bed.block));
            teams.put(color.name(), data);
        }
        cfg.set("teams", teams);
        cfg.set("shops.item", arena.getItemShops().stream().map(this::formatLocation).toList());
        cfg.set("shops.upgrade", arena.getUpgradeShops().stream().map(this::formatLocation).toList());
        List<Map<String, Object>> gens = new ArrayList<>();
        for (Arena.GeneratorSpec spec : arena.getGenerators()) {
            Map<String, Object> g = new HashMap<>();
            g.put("type", spec.type.name());
            g.put("loc", formatLocation(spec.loc));
            g.put("tier", spec.tier);
            gens.add(g);
        }
        cfg.set("generators", gens);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arena " + id + ": " + e.getMessage());
        }
    }

    /** Reload a single arena from disk. */
    public void reload(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        File file = new File(plugin.getDataFolder(), "arenas/" + id + ".yml");
        if (!file.exists()) {
            return;
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
        Arena arena = new Arena(plugin, id);
        arena.setWorld(cfg.getString("world", ""));
        if (cfg.isString("lobby")) {
            arena.setLobby(parseLocation(arena.getWorld(), cfg.getString("lobby")));
        }
        for (String t : cfg.getStringList("enabled-teams")) {
            try {
                arena.enableTeam(TeamColor.valueOf(t.toUpperCase(Locale.ROOT)));
            } catch (IllegalArgumentException ignored) {
            }
        }
        ConfigurationSection teams = cfg.getConfigurationSection("teams");
        if (teams != null) {
            for (String keyTeam : teams.getKeys(false)) {
                try {
                    TeamColor color = TeamColor.valueOf(keyTeam.toUpperCase(Locale.ROOT));
                    String spawn = teams.getString(keyTeam + ".spawn");
                    if (spawn != null) {
                        arena.setTeamSpawn(color, parseLocation(arena.getWorld(), spawn));
                    }
                    String bed = teams.getString(keyTeam + ".bed");
                    if (bed != null) {
                        String[] parts = bed.split(",");
                        if (parts.length >= 3) {
                            Location loc = parseLocation(arena.getWorld(), parts[0] + "," + parts[1] + "," + parts[2] + ",0,0");
                            arena.setTeamBed(color, loc);
                        }
                    }
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        ConfigurationSection shops = cfg.getConfigurationSection("shops");
        if (shops != null) {
            for (String s : shops.getStringList("item")) {
                arena.addItemShop(parseLocation(arena.getWorld(), s));
            }
            for (String s : shops.getStringList("upgrade")) {
                arena.addUpgradeShop(parseLocation(arena.getWorld(), s));
            }
        }
        for (Object obj : cfg.getMapList("generators")) {
            try {
                Map<String, Object> map = mso(obj);
                GeneratorType type = GeneratorType.valueOf(String.valueOf(map.get("type")));
                String locStr = String.valueOf(map.get("loc"));
                int tier = map.containsKey("tier") ? intOf(map.get("tier")) : 1;
                arena.addGenerator(type, parseLocation(arena.getWorld(), locStr), tier);
            } catch (Exception ignored) {
            }
        }
        arenas.put(key, arena);
    }

    public Map<String, Arena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name.toLowerCase(Locale.ROOT)));
    }

    public void joinArena(Player player, String name) {
        getArena(name).ifPresentOrElse(arena -> arena.addPlayer(player),
                () -> player.sendMessage(plugin.messages().get("error.no_arena")));
    }

    public void leaveArena(Player player) {
        arenas.values().forEach(arena -> {
            if (arena.getState() != GameState.RESTARTING) {
                arena.removePlayer(player);
            }
        });
    }

    public void setArenaSpawn(String id, Location loc) {
        getArena(id).ifPresent(a -> a.setLobby(loc));
    }

    public void setTeamSpawn(String id, TeamColor team, Location loc) {
        getArena(id).ifPresent(a -> a.setTeamSpawn(team, loc));
    }

    public void setTeamBed(String id, TeamColor team, Location loc) {
        getArena(id).ifPresent(a -> a.setTeamBed(team, loc));
    }

    public void addGenerator(String id, GeneratorType type, Location loc, int tier) {
        getArena(id).ifPresent(a -> a.addGenerator(type, loc, tier));
    }

    public boolean delete(String id) {
        String key = id.toLowerCase(Locale.ROOT);
        Arena arena = arenas.remove(key);
        File file = new File(plugin.getDataFolder(), "arenas/" + id + ".yml");
        if (file.exists()) {
            file.delete();
        }
        var arenaKey = plugin.arenaKey();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (entity instanceof Player) continue;
                String tag = entity.getPersistentDataContainer().get(arenaKey, PersistentDataType.STRING);
                if (tag != null && tag.equalsIgnoreCase(id)) {
                    entity.remove();
                }
            }
        }
        plugin.generators().removeGenMarkers(id);
        return arena != null;
    }

    public void addItemShop(String id, Location loc) {
        getArena(id).ifPresent(a -> a.addItemShop(loc));
    }

    public void addUpgradeShop(String id, Location loc) {
        getArena(id).ifPresent(a -> a.addUpgradeShop(loc));
    }

    private String formatLocation(Location loc) {
        return String.format(Locale.ROOT, "%d,%d,%d,%.1f,%.1f",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ(), loc.getYaw(), loc.getPitch());
    }

    private String formatBlock(Location loc) {
        return String.format(Locale.ROOT, "%d,%d,%d",
                loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    private Location parseLocation(String world, String str) {
        String[] p = str.split(",");
        World w = Bukkit.getWorld(world);
        double x = Double.parseDouble(p[0]);
        double y = Double.parseDouble(p[1]);
        double z = Double.parseDouble(p[2]);
        float yaw = p.length > 3 ? Float.parseFloat(p[3]) : 0f;
        float pitch = p.length > 4 ? Float.parseFloat(p[4]) : 0f;
        return new Location(w, x, y, z, yaw, pitch);
    }
}
