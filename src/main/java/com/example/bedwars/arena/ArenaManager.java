package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.io.IOException;
import org.bukkit.configuration.file.YamlConfiguration;

/**
 * Loads arenas from the plugin's "arenas" folder and allows players
 * to join them. Only minimal data (arena name) is parsed.
 */
public class ArenaManager {

    private final BedwarsPlugin plugin;
    private final Map<String, Arena> arenas = new HashMap<>();

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
        for (File file : folder.listFiles((dir, name) -> name.endsWith(".yml"))) {
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(file);
            String name = cfg.getString("name", file.getName().replace(".yml", ""));
            arenas.put(name.toLowerCase(), new Arena(plugin, name));
        }
    }

    /**
     * Creates a new arena configuration and adds it to the manager.
     * Only the arena name and world are persisted.
     *
     * @param id    arena identifier
     * @param world target world name
     * @return true if created, false if an arena with this id already exists
     */
    public boolean createArena(String id, String world) {
        String key = id.toLowerCase();
        if (arenas.containsKey(key)) {
            return false;
        }
        File file = new File(plugin.getDataFolder(), "arenas/" + id + ".yml");
        YamlConfiguration cfg = new YamlConfiguration();
        cfg.set("name", id);
        cfg.set("world", world);
        try {
            cfg.save(file);
        } catch (IOException e) {
            plugin.getLogger().warning("Could not save arena " + id + ": " + e.getMessage());
        }
        arenas.put(key, new Arena(plugin, id));
        return true;
    }

    /**
     * Deletes an arena and removes its configuration file.
     *
     * @param id arena identifier
     * @return true if deleted, false otherwise
     */
    public boolean deleteArena(String id) {
        String key = id.toLowerCase();
        Arena arena = arenas.remove(key);
        if (arena == null) {
            return false;
        }
        File file = new File(plugin.getDataFolder(), "arenas/" + id + ".yml");
        if (file.exists() && !file.delete()) {
            plugin.getLogger().warning("Could not delete arena file " + file.getName());
        }
        return true;
    }

    public Map<String, Arena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name.toLowerCase()));
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
}
