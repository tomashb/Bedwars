package com.example.bedwars.generator;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

/**
 * Central scheduler for arena resource generators.
 */
public class GeneratorManager {

    private final BedwarsPlugin plugin;
    private final Map<GeneratorType, Settings> defaults = new EnumMap<>(GeneratorType.class);
    private final Map<String, List<Generator>> generators = new HashMap<>();
    private final NamespacedKey genMarkerKey;

    private record Settings(int interval, int amount) {}

    public GeneratorManager(BedwarsPlugin plugin) {
        this.plugin = plugin;
        this.genMarkerKey = new NamespacedKey(plugin, "bw_gen_marker");
        loadDefaults();
        startTask();
    }

    private void loadDefaults() {
        ConfigurationSection section = plugin.getConfig().getConfigurationSection("generators");
        if (section == null) {
            return;
        }
        for (String key : section.getKeys(false)) {
            try {
                GeneratorType type = GeneratorType.valueOf(key.toUpperCase(Locale.ROOT));
                int interval = section.getInt(key + ".interval", 200);
                int amount = section.getInt(key + ".amount", 1);
                defaults.put(type, new Settings(interval, amount));
            } catch (IllegalArgumentException ignored) {
            }
        }
    }

    private void startTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                for (Arena arena : plugin.arenas().getArenas().values()) {
                    if (arena.getState() != GameState.RUNNING) {
                        continue;
                    }
                    List<Generator> list = generators.get(arena.getName());
                    if (list != null) {
                        list.forEach(Generator::tick);
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void addGenerator(String arenaId, GeneratorType type, org.bukkit.Location loc) {
        Settings settings = defaults.get(type);
        if (settings == null) {
            return;
        }
        generators.computeIfAbsent(arenaId, k -> new ArrayList<>())
                .add(new Generator(type, loc, settings.interval, settings.amount));
    }

    public void onArenaStart(String arenaId) {
        List<Generator> list = generators.get(arenaId);
        if (list != null) {
            list.forEach(Generator::reset);
        }
        removeGenMarkers(arenaId);
    }

    /**
     * Remove setup markers tagged for the given arena.
     */
    public void removeGenMarkers(String arenaId) {
        NamespacedKey arenaKey = plugin.arenaKey();
        for (World world : plugin.getServer().getWorlds()) {
            for (Entity entity : world.getEntities()) {
                PersistentDataContainer pdc = entity.getPersistentDataContainer();
                if (arenaId.equals(pdc.get(arenaKey, PersistentDataType.STRING))
                        && pdc.has(genMarkerKey, PersistentDataType.BYTE)) {
                    entity.remove();
                }
            }
        }
    }
}
