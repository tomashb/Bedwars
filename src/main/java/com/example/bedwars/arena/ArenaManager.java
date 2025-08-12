package com.example.bedwars.arena;

import com.example.bedwars.BedwarsPlugin;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    public Map<String, Arena> getArenas() {
        return Collections.unmodifiableMap(arenas);
    }

    public Optional<Arena> getArena(String name) {
        return Optional.ofNullable(arenas.get(name.toLowerCase()));
    }

    public void joinArena(Player player, String name) {
        getArena(name).ifPresentOrElse(arena -> arena.addPlayer(player),
                () -> player.sendMessage("§cArène inconnue."));
    }

    public void leaveArena(Player player) {
        arenas.values().forEach(arena -> {
            if (arena.getState() != GameState.RESTARTING) {
                arena.removePlayer(player);
            }
        });
    }
}
