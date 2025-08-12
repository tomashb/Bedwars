package com.example.bedwars.gen;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import com.example.bedwars.arena.GameState;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles scheduled drops for all generators across arenas.
 * The scheduler ticks every second (20 ticks) and decreases a counter per generator
 * until it reaches zero, then drops the configured item and resets the counter.
 */
public class GeneratorManager {
    private final BedwarsPlugin plugin;
    private final com.example.bedwars.arena.ArenaManager arenas;
    private BukkitTask task;

    private final Map<GeneratorType, Integer> intervals = new HashMap<>();
    private final Map<GeneratorType, Integer> amounts = new HashMap<>();
    private final Map<Generator, Integer> timers = new HashMap<>();

    public GeneratorManager(BedwarsPlugin plugin, com.example.bedwars.arena.ArenaManager arenas){
        this.plugin = plugin;
        this.arenas = arenas;
        for (GeneratorType t : GeneratorType.values()){
            int interval = plugin.getConfig().getInt("generators."+t.name()+".interval", 20);
            int amount = plugin.getConfig().getInt("generators."+t.name()+".amount", 1);
            intervals.put(t, interval);
            amounts.put(t, amount);
        }
        start();
    }

    /** Starts the repeating task running every second. */
    private void start(){
        task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Arena a : arenas.all()){
                if (a.getState() != GameState.RUNNING){
                    // reset timers when arena not running
                    for (Generator g : a.getGenerators()) timers.put(g, intervals.get(g.getType()));
                    continue;
                }
                World w = a.getWorld();
                if (w == null) continue;
                for (Generator g : a.getGenerators()){
                    tickGenerator(g, w);
                }
            }
        }, 20L, 20L);
    }

    private void tickGenerator(Generator g, World w){
        int left = timers.getOrDefault(g, intervals.get(g.getType()));
        left -= 20; // scheduler runs every 20 ticks
        if (left > 0){
            timers.put(g, left);
            return;
        }
        Material m = switch (g.getType()){
            case IRON -> Material.IRON_INGOT;
            case GOLD -> Material.GOLD_INGOT;
            case DIAMOND -> Material.DIAMOND;
            case EMERALD -> Material.EMERALD;
        };
        w.dropItem(g.getLocation().clone().add(0,1,0), new ItemStack(m, Math.max(1, amounts.get(g.getType()))));
        timers.put(g, intervals.get(g.getType()));
    }

    /** Resets counters for all generators of an arena. */
    public void resetArena(Arena a){
        for (Generator g : a.getGenerators()) timers.put(g, intervals.get(g.getType()));
    }

    public void shutdown(){ if (task != null) task.cancel(); }
}

