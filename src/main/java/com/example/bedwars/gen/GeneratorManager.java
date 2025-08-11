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

public class GeneratorManager {
    private final BedwarsPlugin plugin; private final com.example.bedwars.arena.ArenaManager arenas;
    private BukkitTask task;
    private final Map<GeneratorType, Long> intervals = new HashMap<>();
    private final Map<GeneratorType, Integer> amounts = new HashMap<>();

    public GeneratorManager(BedwarsPlugin plugin, com.example.bedwars.arena.ArenaManager arenas){
        this.plugin=plugin; this.arenas=arenas;
        for (GeneratorType t : GeneratorType.values()){
            long interval = plugin.getConfig().getLong("generators."+t.name()+".interval", 40);
            int amount = plugin.getConfig().getInt("generators."+t.name()+".amount", 1);
            intervals.put(t, interval); amounts.put(t, amount);
        }
        start();
    }

    private void start(){
        this.task = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (Arena a : arenas.all()) {
                if (a.getState()!=GameState.RUNNING) continue;
                World w = a.getWorld(); if (w==null) continue;
                for (Generator g : a.getGenerators()) drop(g, w);
            }
        }, 20L, 20L);
    }

    private void drop(Generator g, World world){
        Material m = switch (g.getType()){ case IRON->Material.IRON_INGOT; case GOLD->Material.GOLD_INGOT; case DIAMOND->Material.DIAMOND; case EMERALD->Material.EMERALD; };
        long key = g.getLocation().hashCode() ^ g.getType().hashCode();
        long interval = Math.max(5L, intervals.get(g.getType()) - (g.getTier()-1)*10L);
        if ((world.getFullTime()+key) % interval != 0) return;
        world.dropItem(g.getLocation().clone().add(0,1,0), new ItemStack(m, Math.max(1, amounts.get(g.getType()))));
    }
    public void shutdown(){ if (task!=null) task.cancel(); }
}
