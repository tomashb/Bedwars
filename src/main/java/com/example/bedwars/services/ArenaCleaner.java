package com.example.bedwars.services;

import com.example.bedwars.BedwarsPlugin;
import com.example.bedwars.arena.Arena;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.shorts.ShortIterator;
import it.unimi.dsi.fastutil.shorts.ShortOpenHashSet;
import java.util.Objects;
import org.bukkit.Bukkit;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.scheduler.BukkitTask;

/**
 * Removes blocks recorded in {@link PlacedBlocksStore} progressively to avoid
 * lag spikes. Designed to be triggered at the end of a game.
 */
public final class ArenaCleaner {
  private final BedwarsPlugin plugin;
  private final PlacedBlocksStore store;
  private final int blocksPerTick;

  public ArenaCleaner(BedwarsPlugin plugin, PlacedBlocksStore store) {
    this.plugin = plugin;
    this.store = store;
    this.blocksPerTick = plugin.getConfig().getInt("cleanup.blocks_per_tick", 400);
  }

  /** Start an asynchronous, budgeted cleanup task for the arena. */
  public BukkitTask cleanupPlacedBlocks(Arena a) {
    World w = Bukkit.getWorld(a.world().name());
    Objects.requireNonNull(w, "World not loaded: " + a.world().name());
    var it = store.chunks(a).iterator();
    final BukkitTask[] handle = new BukkitTask[1];
    handle[0] = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
      int budget = blocksPerTick;
      while (it.hasNext() && budget > 0) {
        Long2ObjectMap.Entry<ShortOpenHashSet> e = it.next();
        int chunkX = (int) (e.getLongKey() >> 32);
        int chunkZ = (int) e.getLongKey();
        Chunk c = w.getChunkAt(chunkX, chunkZ);
        ShortOpenHashSet set = e.getValue();
        ShortIterator si = set.iterator();
        while (si.hasNext() && budget-- > 0) {
          short idx = si.nextShort();
          int y = (idx >> 8) & 0xFF;
          int lx = (idx >> 4) & 0xF;
          int lz = idx & 0xF;
          Block b = c.getBlock((chunkX << 4) + lx, y, (chunkZ << 4) + lz);
          b.setType(Material.AIR, false);
        }
        if (set.isEmpty()) it.remove();
      }
      if (!it.hasNext()) {
        store.clear(a);
        handle[0].cancel();
      }
    }, 1L, 1L);
    return handle[0];
  }

  /** Remove all recorded blocks synchronously (used on plugin disable). */
  public void cleanupSync(Arena a) {
    World w = Bukkit.getWorld(a.world().name());
    Objects.requireNonNull(w, "World not loaded: " + a.world().name());
    for (Long2ObjectMap.Entry<ShortOpenHashSet> e : store.chunks(a)) {
      int chunkX = (int) (e.getLongKey() >> 32);
      int chunkZ = (int) e.getLongKey();
      Chunk c = w.getChunkAt(chunkX, chunkZ);
      ShortOpenHashSet set = e.getValue();
      ShortIterator si = set.iterator();
      while (si.hasNext()) {
        short idx = si.nextShort();
        int y = (idx >> 8) & 0xFF;
        int lx = (idx >> 4) & 0xF;
        int lz = idx & 0xF;
        Block b = c.getBlock((chunkX << 4) + lx, y, (chunkZ << 4) + lz);
        b.setType(Material.AIR, false);
      }
    }
    store.clear(a);
  }
}

